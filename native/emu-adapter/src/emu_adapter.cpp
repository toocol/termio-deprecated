// force boost to be included as header only, also on windows
#define BOOST_ALL_NO_LIB 1

#include "emu_adapter.h"
#include <boost/thread/xtime.hpp>
#include <string>
#include <vector>
#include "shared_memory.h"

namespace ipc = boost::interprocess;

using namespace nativefx;

std::vector<std::string> names;
std::vector<shared_memory_info*> connections;
// for java events that are sent to the server
std::vector<ipc::message_queue*> evt_msg_queues;
// for native server events sent to the java clinet
std::vector<ipc::message_queue*> evt_msg_queues_native;
std::vector<void*> buffers;

std::vector<ipc::shared_memory_object*> shm_infos;
std::vector<ipc::mapped_region*> info_regions;
std::vector<ipc::shared_memory_object*> shm_buffers;
std::vector<ipc::mapped_region*> buffer_regions;

rbool fire_mouse_event(ri32 key, ri32 evt_type, rf64 x, rf64 y, rf64 amount,
                       ri32 buttons, ri32 modifiers, ri32 click_count,
                       ri64 timestamp) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return boolC2J(false);
  }

  mouse_event evt;
  evt.type |= evt_type;
  evt.timestamp = timestamp;
  evt.click_count = click_count;
  evt.x = x;
  evt.y = y;
  evt.amount = amount;
  evt.buttons = buttons;
  evt.modifiers = modifiers;

  // timed locking of resources
  boost::system_time const timeout =
      boost::get_system_time() + boost::posix_time::milliseconds(LOCK_TIMEOUT);

  bool result = evt_msg_queues[key]->timed_send(
      &evt,         // data to send
      sizeof(evt),  // size of the data (check it fits into max_size)
      0,            // msg priority
      timeout       // timeout
  );

  return result;
}

rbool fire_key_event(ri32 key, ri32 evt_type, const rstring& chars,
                     ri32 key_code, ri32 modifiers, ri64 timestamp) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return boolC2J(false);
  }

  key_event evt;
  evt.type |= evt_type;
  evt.timestamp = timestamp;
  store_key_codes(chars, evt.chars);
  evt.key_code = key_code;
  evt.modifiers = modifiers;

  // timed locking of resources
  boost::system_time const timeout =
      boost::get_system_time() + boost::posix_time::milliseconds(LOCK_TIMEOUT);

  bool result = evt_msg_queues[key]->timed_send(
      &evt,         // data to send
      sizeof(evt),  // size of the data (check it fits into max_size)
      0,            // msg priority
      timeout       // timeout
  );

  return result;
}

void update_buffer_connection(int key) {
  if (key >= connections.size()) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return;
  }

  rstring name = names[key];
  rstring info_name = get_info_name(key, name);
  rstring buffer_name = get_buffer_name(key, name);

  try {
    // create a shared memory object.
    ipc::shared_memory_object* shm_buffer =
        new ipc::shared_memory_object(ipc::open_only,       // only open
                                      buffer_name.c_str(),  // name
                                      ipc::read_write       // read-write mode
        );

    if (shm_buffers[key] != NULL) {
      delete shm_buffers[key];
    }

    shm_buffers[key] = shm_buffer;

    // map the whole shared memory in this process
    ipc::mapped_region* buffer_region =
        new ipc::mapped_region(*shm_buffer,     // what to map
                               ipc::read_write  // map it as read-write
        );

    if (buffer_regions[key] != NULL) {
      delete buffer_regions[key];
    }

    buffer_regions[key] = buffer_region;

    // get the address of the mapped region
    void* buffer_addr = buffer_region->get_address();

    buffers[key] = buffer_addr;

  } catch (...) {
    std::cerr << "ERROR: cannot connect to '" << info_name
              << "'. Server probably not running." << std::endl;

    return;
  }
}

void fire_native_event(int key, rstring type, rstring evt) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return;
  }

  jclass cls =
      jni_env->FindClass("com/toocol/termio/platform/nativefx/NativeBinding");

  jmethodID fireNativeEventMethod = jni_env->GetStaticMethodID(
      cls, "fireNativeEvent", "(ILjava/lang/String;Ljava/lang/String;)V");

  if (fireNativeEventMethod == NULL) {
    std::cerr << "ERROR: cannot fire native events. Method not found by JNI"
              << std::endl;
    return;
  }

  jni_env->CallVoidMethod(cls, fireNativeEventMethod, key,
                          stringC2J(jni_env, type), stringC2J(jni_env, evt));
}

REXPORT ri32 RCALL next_key(){return (ri32)connections.size()}

REXPORT ri32 RCALL connect_to(rstring name) {
  // setup key and names for new connection
  int key = (int)connections.size();
  std::string info_name = get_info_name(key, name);
  std::string evt_msg_queue_name = get_evt_msg_queue_name(key, name);
  std::string evt_msg_queue_native_name =
      get_evt_msg_queue_native_name(key, name);
  std::string buffer_name = get_buffer_name(key, name);
  names.push_back(name);

  try {
    // open the shared memory object.
    shared_memory_object* shm_info =
        new shared_memory_object(open_only,          // only open (don't create)
                                 info_name.c_str(),  // name
                                 read_write          // read-write mode
        );

    shm_infos.push_back(shm_info);

    // map the whole shared memory in this process
    mapped_region* info_region =
        new mapped_region(*shm_info,  // What to map
                          read_write  // Map it as read-write
        );

    info_regions.push_back(info_region);

    // get the address of the mapped region
    void* info_addr = info_region->get_address();

    // construct the shared structure in memory
    shared_memory_info* info_data = static_cast<shared_memory_info*>(info_addr);
    connections.push_back(info_data);

    // create mq (for java clinet events transferred to server)
    ipc::message_queue* evt_msg_queue = open_evt_mq(evt_msg_queue_name);
    evt_msg_queues.push_back(evt_msg_queue);

    // create mq (for native server events transferred to java client)
    ipc::message_queue* evt_msg_queue_native =
        open_evt_mq(evt_msg_queue_native_name);
    evt_msg_queues_native.push_back(evt_msg_queue_native);

    // timed locking of resources
    boost::system_time const timeout =
        boost::get_system_time() +
        boost::posix_time::milliseconds(LOCK_TIMEOUT);
    bool locking_success = connections[key]->mutex.timed_lock(timeout);

    if (!locking_success) {
      std::cerr << "ERROR: cannot connect to '" << info_name
                << "':" << std::endl;
      std::cerr << " -> The shared memory seems to exist." << std::endl;
      std::cerr << " -> But we are unable to lock the resources." << std::endl;
      std::cerr << " -> Server not running?." << std::endl;
      return -1;
    }

    // create a shared memory object.
    shared_memory_object* shm_buffer =
        new shared_memory_object(open_only,            // only open
                                 buffer_name.c_str(),  // name
                                 read_write            // read-write mode
        );

    shm_buffers.push_back(shm_buffer);

    // map the whole shared memory in this process
    mapped_region* buffer_region =
        new mapped_region(*shm_buffer,  // What to map
                          read_write    // Map it as read-write
        );

    buffer_regions.push_back(buffer_region);

    // get the address of the mapped region
    void* buffer_addr = buffer_region->get_address();

    buffers.push_back(buffer_addr);

    info_data->mutex.unlock();
  } catch (...) {
    std::cerr << "ERROR: cannot connect to '" << info_name
              << "'. Server probably not running." << std::endl;

    return -1;
  }

  return key;
}

REXPORT rbool RCALL terminate(ri32 key) {
  if (key >= connections.size()) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return false;
  }

  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return false;
  }

  termination_event evt;
  evt.type |= NFX_TERMINATION_EVENT;
  evt.timestamp = 0;

  // timed locking of resources
  boost::system_time const timeout =
      boost::get_system_time() + boost::posix_time::milliseconds(LOCK_TIMEOUT);

  bool result = evt_msg_queues[key]->timed_send(
      &evt,         // data to send
      sizeof(evt),  // size of the data (check it fits into max_size)
      0,            // msg priority
      timeout       // timeout
  );

  // return result;

  names[key] = "";  // NULL?

  // already deleted because it's located in the shared memory location
  // delete connections[key];
  // delete buffers[key];

  delete shm_infos[key];
  delete info_regions[key];
  delete shm_buffers[key];
  delete buffer_regions[key];
  delete evt_msg_queues[key];
  delete evt_msg_queues_native[key];

  connections[key] = NULL;
  buffers[key] = NULL;
  shm_infos[key] = NULL;
  info_regions[key] = NULL;
  shm_buffers[key] = NULL;
  buffer_regions[key] = NULL;
  evt_msg_queues[key] = NULL;
  evt_msg_queues_native[key] = NULL;

  return true;
}

REXPORT rbool RCALL is_connected(ri32 key) {
  return key < connections.size() && connections[key] != NULL;
}

REXPORT rstring RCALL send_msg(ri32 key, rstring msg, ri32 sharedStringType) {
  shared_memory_info* info_data = NULL;
  if (key >= connections.size()) {
    return stringC2J(env, "ERROR: key not available");
  }

  info_data = connections[key];
  info_data->shared_string_type = sharedStringType;

  // send a message to server
  store_shared_string(msg, info_data->client_to_server_msg);
  info_data->client_to_server_msg_semaphore.post();
  // return result from server
  info_data->client_to_server_res_semaphore.wait();
  return info_data->client_to_server_res;
}

REXPORT void RCALL process_native_events(ri32 key) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return;
  }

  // process events
  ipc::message_queue::size_type recvd_size;
  unsigned int priority;

  native_event nevt;

  while (evt_msg_queues_native[key]->get_num_msg() > 0) {
    bool result = evt_msg_queues_native[key]->try_receive(
        &nevt, sizeof(native_event), recvd_size, priority);

    if (!result) {
      std::cerr << "[" << key
                << "] ERROR: can't read messages, message queue not accessible."
                << std::endl;
    }

    fire_native_event(key, nevt.type, nevt.evt_msg);
  }
}

REXPORT void RCALL resize(ri32 key, ri32 w, ri32 h) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return;
  }

  int prev_w = connections[key]->w;
  int prev_h = connections[key]->h;

  connections[key]->w = w;
  connections[key]->h = h;

  if (prev_w != w || prev_h != h) {
    connections[key]->buffer_ready = false;
  }
}

REXPORT rbool RCALL is_dirty(ri32 key) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
  } else {
    return connections[key]->dirty;
  }

  return false;
}

REXPORT void RCALL redraw(ri32 key, ri32 x, ri32 y, ri32 w, ri32 h) {}

REXPORT void RCALL set_dirty(ri32 key, rbool dirty) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
  } else {
    connections[key]->dirty = boolJ2C(dirty);
  }
}

REXPORT void RCALL set_buffer_ready(ri32 key, rbool value) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
  } else {
    connections[key]->buffer_ready = boolJ2C(value);
  }
}

REXPORT rbool RCALL is_buffer_ready(ri32 key) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
  } else {
    return connections[key]->buffer_ready;
  }

  return false;
}

REXPORT ri32 RCALL get_w(ri32 key) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return -1;
  }

  return connections[key]->w;
}

REXPORT ri32 RCALL get_h(ri32 key) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return -1;
  }

  return connections[key]->h;
}

REXPORT rbool RCALL fire_mouse_pressed_event(ri32 key, rf64 x, rf64 y,
                                             ri32 buttons, ri32 modifiers,
                                             ri64 timestamp) {
  return fire_mouse_event(key, NFX_MOUSE_MOVED, x, y, 0.0, buttons, modifiers,
                          0, (long)timestamp);
}

REXPORT rbool RCALL fire_mouse_released_event(ri32 key, rf64 x, rf64 y,
                                              ri32 buttons, ri32 modifiers,
                                              ri64 timestamp) {
  return fire_mouse_event(key, NFX_MOUSE_RELEASED, x, y, 0.0, buttons,
                          modifiers, 0, (long)timestamp);
}

REXPORT rbool RCALL fire_mouse_clicked_event(ri32 key, rf64 x, rf64 y,
                                             ri32 buttons, ri32 modifiers,
                                             ri32 click_count, ri64 timestamp) {
  return fire_mouse_event(key, NFX_MOUSE_CLICKED, x, y, 0.0, buttons, modifiers,
                          click_count, (long)timestamp);
}

REXPORT rbool RCALL fire_mouse_entered_event(ri32 key, rf64 x, rf64 y,
                                             ri32 buttons, ri32 modifiers,
                                             ri32 click_count, ri64 timestamp) {
  return fire_mouse_event(key, NFX_MOUSE_ENTERED, x, y, 0.0, buttons, modifiers,
                          0, (long)timestamp);
}

REXPORT rbool RCALL fire_mouse_exited_event(ri32 key, rf64 x, rf64 y,
                                            ri32 buttons, ri32 modifiers,
                                            ri32 click_count, ri64 timestamp) {
  return fire_mouse_event(key, NFX_MOUSE_EXITED, x, y, 0.0, buttons, modifiers,
                          0, (long)timestamp);
}

REXPORT rbool RCALL fire_mouse_move_event(ri32 key, rf64 x, rf64 y,
                                          ri32 buttons, ri32 modifiers,
                                          ri64 timestamp) {
  return fire_mouse_event(key, NFX_MOUSE_MOVED, x, y, 0.0, buttons, modifiers,
                          0, (long)timestamp);
}

REXPORT rbool RCALL fire_mouse_wheel_event(ri32 key, rf64 x, rf64 y,
                                           rf64 amount, ri32 buttons,
                                           ri32 modifiers, ri64 timestamp) {
  return fire_mouse_event(key, NFX_MOUSE_WHEEL, x, y, amount, buttons,
                          modifiers, 0, (long)timestamp);
}

REXPORT rbool RCALL fire_key_pressed_event(ri32 key, rstring characters,
                                           ri32 key_code, ri32 modifiers,
                                           ri64 timestamp) {
  return fire_key_event(key, NFX_KEY_PRESSED, characters, key_code, modifiers,
                        (long)timestamp);
}

REXPORT rbool RCALL fire_key_released_event(ri32 key, rstring characters,
                                            ri32 key_code, ri32 modifiers,
                                            ri64 timestamp) {
  return fire_key_event(key, NFX_KEY_RELEASED, characters, key_code, modifiers,
                        (long)timestamp);
}

REXPORT rbool RCALL fire_key_typed_event(ri32 key, rstring characters,
                                         ri32 key_code, ri32 modifiers,
                                         ri64 timestamp) {
  return fire_key_event(key, NFX_KEY_TYPED, characters, key_code, modifiers,
                        (long)timestamp);
}

REXPORT rbool RCALL request_focus(ri32 key, rbool focus, ri64 timestamp) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return false;
  }

  connections[key]->focus = focus;

  focus_event evt;
  evt.type |= NFX_FOCUS_EVENT;
  evt.focus = focus;
  evt.timestamp = (long)timestamp;

  // timed locking of resources
  boost::system_time const timeout =
      boost::get_system_time() + boost::posix_time::milliseconds(LOCK_TIMEOUT);

  return evt_msg_queues[key]->timed_send(
      &evt,         // data to send
      sizeof(evt),  // size of the data (check it fits into max_size)
      0,            // msg priority
      timeout       // timeout
  );
}

REXPORT rbool RCALL create_ssh_session(ri32 key, ri64 session_id, rstring host,
                                       rstring user, rstring password,
                                       ri64 timestamp) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return boolC2J(false);
  }

  create_ssh_session_event evt;
  evt.type |= NFX_CREATE_SSH_SESSION_EVENT;
  evt.sessionId = (long)session_id;
  evt.timestamp = (long)timestamp;

  store_shared_string(stringJ2C(env, host), evt.host, IPC_SSH_INFO_SIZE + 1);
  store_shared_string(stringJ2C(env, user), evt.user, IPC_SSH_INFO_SIZE + 1);
  store_shared_string(stringJ2C(env, password), evt.password,
                      IPC_SSH_INFO_SIZE + 1);

  // timed locking of resources
  boost::system_time const timeout =
      boost::get_system_time() + boost::posix_time::milliseconds(LOCK_TIMEOUT);

  return evt_msg_queues[key]->timed_send(
      &evt,         // data to send
      sizeof(evt),  // size of the data (check it fits into max_size)
      0,            // msg priority
      timeout       // timeout
  );
}

REXPORT void* RCALL get_buffer(ri32 key) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return NULL;
  }

  update_buffer_connection(key);

  return buffers[key];
}

REXPORT rbool RCALL lock(ri32 key) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return false;
  } else {
    // try to lock (returns true if successful, false if wasn't successful
    // within the specified LOCK_TIMEOUT)
    boost::system_time const timeout =
        boost::get_system_time() +
        boost::posix_time::milliseconds(LOCK_TIMEOUT);
    return connections[key]->mutex.timed_lock(timeout);
  }
}

REXPORT rbool RCALL lock(ri32 key, ri64 rtimeout) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return false;
  } else {
    // try to lock (returns true if successful, false if wasn't successful
    // within the specified LOCK_TIMEOUT)
    boost::system_time const timeout =
        boost::get_system_time() + boost::posix_time::milliseconds(rtimeout);
    return connections[key]->mutex.timed_lock(timeout);
  }
}

REXPORT void RCALL unlock(ri32 key) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
  } else {
    connections[key]->mutex.unlock();
  }
}

REXPORT void RCALL wait_for_buffer_changes(ri32 key) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
  } else {
    while (!connections[key]->buffer_semaphore.try_wait()) {
    }
  }
}

REXPORT rbool RCALL has_buffer_changes(ri32 key) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
  } else {
    return connections[key]->buffer_semaphore.try_wait();
  }

  return false;
}

REXPORT void RCALL lock_buffer(ri32 key) {}

REXPORT void RCALL unlock_buffer(ri32 key) {}