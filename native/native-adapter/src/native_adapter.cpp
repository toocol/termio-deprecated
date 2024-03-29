// force boost to be included as header only, also on windows
#define BOOST_ALL_NO_LIB 1

#include "native_adapter.h"
#include <boost/thread/xtime.hpp>
#include <iostream>
#include <string>
#include <vector>
#include "shared_memory.h"

namespace ipc = boost::interprocess;

using namespace nativers;
using namespace std;

std::vector<std::string> names;
std::vector<shared_memory_info*> connections;
// for client events that are sent to the server
std::vector<ipc::message_queue*> evt_msg_queues;
// for native server events sent to the java clinet
std::vector<ipc::message_queue*> evt_msg_queues_native;

std::vector<void*> primary_buffers;
std::vector<void*> secondary_buffers;

std::vector<ipc::shared_memory_object*> shm_infos;
std::vector<ipc::mapped_region*> info_regions;
std::vector<ipc::shared_memory_object*> primary_buffer_objects;
std::vector<ipc::mapped_region*> primary_buffer_regions;
std::vector<ipc::shared_memory_object*> secondary_buffer_objects;
std::vector<ipc::mapped_region*> secondary_buffer_regions;
std::map<i32, native_event*> evt_ptrs;

bool fire_mouse_event(i32 key, i32 evt_type, f64 x, f64 y, f64 amount,
                      i32 buttons, i32 modifiers, i32 click_count,
                      i64 timestamp) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return false;
  }

  mouse_event evt;
  evt.type |= evt_type;
  evt.timestamp = (long)timestamp;
  evt.click_count = click_count;
  evt.x = x;
  evt.y = y;
  evt.amount = amount;
  evt.buttons = buttons;
  evt.modifiers = modifiers;

  // timed locking of resources
  boost::system_time const timeout =
      boost::get_system_time() + boost::posix_time::milliseconds(EVENT_TIMEOUT);

  bool result = evt_msg_queues[key]->timed_send(
      &evt,         // data to send
      sizeof(evt),  // size of the data (check it fits into max_size)
      0,            // msg priority
      timeout       // timeout
  );

  return result;
}

bool fire_key_event(i32 key, i32 evt_type, const string& chars, i32 key_code,
                    i32 modifiers, i64 timestamp) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return false;
  }

  key_event evt;
  evt.type |= evt_type;
  evt.timestamp = (long)timestamp;
  store_key_codes(chars, evt.chars);
  evt.key_code = key_code;
  evt.modifiers = modifiers;

  // timed locking of resources
  boost::system_time const timeout =
      boost::get_system_time() + boost::posix_time::milliseconds(EVENT_TIMEOUT);

  bool result = evt_msg_queues[key]->timed_send(
      &evt,         // data to send
      sizeof(evt),  // size of the data (check it fits into max_size)
      0,            // msg priority
      timeout       // timeout
  );

  return result;
}

void update_primary_buffer_connection(int key) {
  if (key >= connections.size()) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return;
  }

  string name = names[key];
  string info_name = get_info_name(name);
  string buffer_name = get_buffer_name(name);
  string primary_buffer_name = buffer_name + IPC_PRIMARY_BUFFER_NAME;

  try {
    /*
     * Create shared primary buffer
     */
    // create a shared memory object.
    ipc::shared_memory_object* pm_buffer =
        new ipc::shared_memory_object(ipc::open_only,               // only open
                                      primary_buffer_name.c_str(),  // name
                                      ipc::read_write  // read-write mode
        );
    if (primary_buffer_objects[key] != NULL) {
      delete primary_buffer_objects[key];
    }
    primary_buffer_objects[key] = pm_buffer;
    // map the whole shared memory in this process
    ipc::mapped_region* pm_buffer_region =
        new ipc::mapped_region(*pm_buffer,      // what to map
                               ipc::read_write  // map it as read-write
        );
    if (primary_buffer_regions[key] != NULL) {
      delete primary_buffer_regions[key];
    }
    primary_buffer_regions[key] = pm_buffer_region;
    // get the address of the mapped region
    void* pm_buffer_addr = pm_buffer_region->get_address();
    primary_buffers[key] = pm_buffer_addr;
  } catch (...) {
    std::cerr << "ERROR: cannot connect to '" << info_name
              << "'. Server probably not running." << std::endl;
  }
}

void update_secondary_buffer_connection(int key) {
  if (key >= connections.size()) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return;
  }

  string name = names[key];
  string info_name = get_info_name(name);
  string buffer_name = get_buffer_name(name);
  string secondary_buffer_name = buffer_name + IPC_SECONDARY_BUFFER_NAME;

  try {
    /*
     * Create shared secondary buffer
     */
    // create a shared memory object.
    ipc::shared_memory_object* sec_buffer =
        new ipc::shared_memory_object(ipc::open_only,  // only open
                                      secondary_buffer_name.c_str(),  // name
                                      ipc::read_write  // read-write mode
        );
    if (secondary_buffer_objects[key] != NULL) {
      delete secondary_buffer_objects[key];
    }
    secondary_buffer_objects[key] = sec_buffer;
    // map the whole shared memory in this process
    ipc::mapped_region* sec_buffer_region =
        new ipc::mapped_region(*sec_buffer,     // what to map
                               ipc::read_write  // map it as read-write
        );
    if (secondary_buffer_regions[key] != NULL) {
      delete secondary_buffer_regions[key];
    }
    secondary_buffer_regions[key] = sec_buffer_region;
    // get the address of the mapped region
    void* sec_buffer_addr = sec_buffer_region->get_address();
    secondary_buffers[key] = sec_buffer_addr;

  } catch (...) {
    std::cerr << "ERROR: cannot connect to '" << info_name
              << "'. Server probably not running." << std::endl;
  }
}

REXPORT i32 RCALL next_key() { return (i32)connections.size(); }

REXPORT i32 RCALL connect_to(cstring cname) {
  using namespace ipc;
  std::string name = cname;
  // setup key and names for new connection
  int key = (int)connections.size();
  std::string info_name = get_info_name(name);
  std::string evt_msg_queue_name = get_evt_msg_queue_name(name);
  std::string evt_msg_queue_native_name = get_evt_msg_queue_native_name(name);
  std::string buffer_name = get_buffer_name(name);
  std::string primary_buffer_name = buffer_name + IPC_PRIMARY_BUFFER_NAME;
  std::string secondary_buffer_name = buffer_name + IPC_SECONDARY_BUFFER_NAME;
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

    /*
     * Create primary buffer.
     */
    // create a shared memory object.
    shared_memory_object* pm_buffer =
        new shared_memory_object(open_only,                    // only open
                                 primary_buffer_name.c_str(),  // name
                                 read_write  // read-write mode
        );
    primary_buffer_objects.push_back(pm_buffer);
    // map the whole shared memory in this process
    mapped_region* pm_buffer_region =
        new mapped_region(*pm_buffer,  // What to map
                          read_write   // Map it as read-write
        );
    primary_buffer_regions.push_back(pm_buffer_region);
    // get the address of the mapped region
    void* pm_buffer_addr = pm_buffer_region->get_address();
    primary_buffers.push_back(pm_buffer_addr);

    /*
     * Create secondary buffer.
     */
    // create a shared memory object.
    shared_memory_object* sec_buffer =
        new shared_memory_object(open_only,                      // only open
                                 secondary_buffer_name.c_str(),  // name
                                 read_write  // read-write mode
        );
    secondary_buffer_objects.push_back(sec_buffer);
    // map the whole shared memory in this process
    mapped_region* sec_buffer_region =
        new mapped_region(*sec_buffer,  // What to map
                          read_write    // Map it as read-write
        );
    secondary_buffer_regions.push_back(sec_buffer_region);
    // get the address of the mapped region
    void* sec_buffer_addr = sec_buffer_region->get_address();
    secondary_buffers.push_back(sec_buffer_addr);

    info_data->mutex.unlock();
  } catch (...) {
    std::cerr << "ERROR: cannot connect to '" << info_name
              << "'. Server probably not running." << std::endl;

    return -1;
  }

  return key;
}

REXPORT bool RCALL terminate_at(i32 key) {
  if (key >= connections.size()) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return false;
  }

  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return false;
  }

  termination_event evt;
  evt.type |= NRS_TERMINATION_EVENT;
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
  delete primary_buffer_objects[key];
  delete primary_buffer_regions[key];
  delete secondary_buffer_objects[key];
  delete secondary_buffer_regions[key];
  delete evt_msg_queues[key];
  delete evt_msg_queues_native[key];

  connections[key] = NULL;
  primary_buffers[key] = NULL;
  secondary_buffers[key] = NULL;
  shm_infos[key] = NULL;
  info_regions[key] = NULL;
  primary_buffer_objects[key] = NULL;
  primary_buffer_regions[key] = NULL;
  secondary_buffer_objects[key] = NULL;
  secondary_buffer_regions[key] = NULL;
  evt_msg_queues[key] = NULL;
  evt_msg_queues_native[key] = NULL;

  return true;
}

REXPORT bool RCALL is_connected(i32 key) {
  return key < connections.size() && connections[key] != NULL;
}

REXPORT cstring RCALL send_msg(i32 key, cstring msg, i32 sharedStringType) {
  shared_memory_info* info_data = NULL;
  if (key >= connections.size()) {
    return "ERROR: key not available";
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

REXPORT bool RCALL has_native_events(i32 key) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return false;
  }
  return evt_msg_queues_native[key]->get_num_msg() > 0;
}

REXPORT void* RCALL get_native_event(i32 key) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return nullptr;
  }

  // process events
  ipc::message_queue::size_type recvd_size;
  unsigned int priority;

  native_event* nevt = new native_event;

  bool result = evt_msg_queues_native[key]->try_receive(
      nevt, sizeof(native_event), recvd_size, priority);

  if (!result) {
    std::cerr << "[" << key
              << "] ERROR: can't read messages, message queue not accessible."
              << std::endl;
    return nullptr;
  }

  if (evt_ptrs[key]) {
    delete evt_ptrs[key];
  }
  evt_ptrs.insert(pair<int, native_event*>(key, nevt));

  return (void*)nevt;
}

REXPORT void RCALL drop_native_event(i32 key) {
  if (evt_ptrs[key]) {
    delete evt_ptrs[key];
  }
}

REXPORT void RCALL resize(i32 key, i32 w, i32 h) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return;
  }

  shared_memory_info* info_data = connections[key];

  int prev_w = connections[key]->w;
  int prev_h = connections[key]->h;

  info_data->w = w;
  info_data->h = h;

  if (prev_w != w || prev_h != h) {
    info_data->buffer_ready = false;
  }

  info_data->resize_semaphore.wait();
}

REXPORT void RCALL toggle_buffer(i32 key) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return;
  }
  shared_memory_info* info_data = connections[key];
  info_data->consume_side_buffer_status =
      -info_data->consume_side_buffer_status;
}

REXPORT bool RCALL is_dirty(i32 key) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return false;
  }
  shared_memory_info* info_data = connections[key];
  int buffer_status = info_data->consume_side_buffer_status;
  if (buffer_status == PRIMARY_BUFFER) {
    return info_data->primary_dirty;
  } else if (buffer_status == SECONDARY_BUFFER) {
    return info_data->secondary_dirty;
  } else {
    std::cerr << "Invalid buffer status: " << buffer_status << std::endl;
    return false;
  }
}

REXPORT void RCALL redraw(i32 key, i32 x, i32 y, i32 w, i32 h) {}

REXPORT void RCALL set_dirty(i32 key, bool dirty) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return;
  }
  shared_memory_info* info_data = connections[key];
  int buffer_status = info_data->consume_side_buffer_status;
  if (buffer_status == PRIMARY_BUFFER) {
    info_data->primary_dirty = dirty;
  } else if (buffer_status == SECONDARY_BUFFER) {
    info_data->secondary_dirty = dirty;
  } else {
    std::cerr << "Inavalid buffer status: " << buffer_status << std::endl;
  }
}

REXPORT void RCALL set_buffer_ready(i32 key, bool value) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
  } else {
    connections[key]->buffer_ready = value;
  }
}

REXPORT bool RCALL is_buffer_ready(i32 key) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
  } else {
    return connections[key]->buffer_ready;
  }

  return false;
}

REXPORT i32 RCALL get_w(i32 key) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return -1;
  }

  return connections[key]->w;
}

REXPORT i32 RCALL get_h(i32 key) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return -1;
  }

  return connections[key]->h;
}

REXPORT bool RCALL request_focus(i32 key, bool focus, i64 timestamp) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return false;
  }

  connections[key]->focus = focus;

  focus_event evt;
  evt.type |= NRS_FOCUS_EVENT;
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

REXPORT bool RCALL create_ssh_session(i32 key, i64 session_id, cstring host,
                                      cstring user, cstring password,
                                      i64 timestamp) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return false;
  }

  create_ssh_session_event evt;
  evt.type |= NRS_CREATE_SSH_SESSION_EVENT;
  evt.sessionId = (long)session_id;
  evt.timestamp = (long)timestamp;

  store_shared_string(host, evt.host, IPC_SSH_INFO_SIZE + 1);
  store_shared_string(user, evt.user, IPC_SSH_INFO_SIZE + 1);
  store_shared_string(password, evt.password, IPC_SSH_INFO_SIZE + 1);

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

REXPORT bool RCALL shell_startup(i32 key, i64 session_id, cstring param, i64 timestamp) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return false;
  }

  shell_startup_event evt;
  evt.type |= NRS_SHELL_STARTUP;
  evt.sessionId = (long)session_id;
  evt.timestamp = (long)timestamp;

  store_shared_string(param, evt.param, IPC_SHELL_STARTUP_PARAM_SIZE + 1);

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

REXPORT void* RCALL get_primary_buffer(i32 key) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return NULL;
  }

  update_primary_buffer_connection(key);

  return primary_buffers[key];
}

REXPORT void* RCALL get_secondary_buffer(i32 key) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return NULL;
  }

  update_secondary_buffer_connection(key);

  return secondary_buffers[key];
}

REXPORT bool RCALL lock(i32 key) {
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

REXPORT bool RCALL lock_timeout(i32 key, i64 rtimeout) {
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

REXPORT void RCALL unlock(i32 key) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
  } else {
    connections[key]->mutex.unlock();
  }
}

REXPORT void RCALL wait_for_buffer_changes(i32 key) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
  } else {
    while (!connections[key]->buffer_semaphore.try_wait()) {
    }
  }
}

REXPORT bool RCALL has_buffer_changes(i32 key) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
  } else {
    return connections[key]->buffer_semaphore.try_wait();
  }

  return false;
}

REXPORT i32 RCALL buffer_status(i32 key) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return 0;
  }
  return connections[key]->consume_side_buffer_status;
}

REXPORT bool RCALL lock_buffer(i32 key) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return false;
  }
  shared_memory_info* info_data = connections[key];
  int buffer_status = info_data->consume_side_buffer_status;
  // try to lock (returns true if successful, false if wasn't successful
  // within the specified LOCK_TIMEOUT)
  boost::system_time const timeout =
      boost::get_system_time() + boost::posix_time::milliseconds(LOCK_TIMEOUT);
  if (buffer_status == PRIMARY_BUFFER) {
    return connections[key]->primary_buffer_mutex.timed_lock(timeout);
  } else if (buffer_status == SECONDARY_BUFFER) {
    return connections[key]->secondary_buffer_mutex.timed_lock(timeout);
  } else {
    std::cerr << "Invalid buffer status: " << buffer_status << std::endl;
    return false;
  }
}

REXPORT void RCALL unlock_buffer(i32 key) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return;
  }
  shared_memory_info* info_data = connections[key];
  int buffer_status = info_data->consume_side_buffer_status;
  if (buffer_status == PRIMARY_BUFFER) {
    return connections[key]->primary_buffer_mutex.unlock();
  } else if (buffer_status == SECONDARY_BUFFER) {
    return connections[key]->secondary_buffer_mutex.unlock();
  } else {
    std::cerr << "Invalid buffer status: " << buffer_status << std::endl;
    return;
  }
}

REXPORT bool RCALL fire_mouse_pressed_event(i32 key, i32 click_count, f64 x, f64 y, i32 buttons,
                                            i32 modifiers, i64 timestamp) {
  return fire_mouse_event(key, NRS_MOUSE_PRESSED, x, y, 0.0, buttons, modifiers,
                          click_count, (long)timestamp);
}

REXPORT bool RCALL fire_mouse_released_event(i32 key, f64 x, f64 y, i32 buttons,
                                             i32 modifiers, i64 timestamp) {
  return fire_mouse_event(key, NRS_MOUSE_RELEASED, x, y, 0.0, buttons,
                          modifiers, 0, (long)timestamp);
}

REXPORT bool RCALL fire_mouse_clicked_event(i32 key, f64 x, f64 y, i32 buttons,
                                            i32 modifiers, i32 click_count,
                                            i64 timestamp) {
  return fire_mouse_event(key, NRS_MOUSE_CLICKED, x, y, 0.0, buttons, modifiers,
                          click_count, (long)timestamp);
}

REXPORT bool RCALL fire_mouse_entered_event(i32 key, f64 x, f64 y,
                                            i32 modifiers, i64 timestamp) {
  return fire_mouse_event(key, NRS_MOUSE_ENTERED, x, y, 0.0, 0, modifiers, 0,
                          (long)timestamp);
}

REXPORT bool RCALL fire_mouse_exited_event(i32 key, i32 modifiers,
                                           i64 timestamp) {
  return fire_mouse_event(key, NRS_MOUSE_EXITED, 0.0, 0.0, 0.0, 0, modifiers, 0,
                          (long)timestamp);
}

REXPORT bool RCALL fire_mouse_move_event(i32 key, f64 x, f64 y, i32 modifiers,
                                         i64 timestamp) {
  return fire_mouse_event(key, NRS_MOUSE_MOVED, x, y, 0.0, 0, modifiers, 0,
                          (long)timestamp);
}

REXPORT bool RCALL fire_mouse_wheel_event(i32 key, f64 x, f64 y, f64 amount,
                                          i32 modifiers, i64 timestamp) {
  return fire_mouse_event(key, NRS_MOUSE_WHEEL, x, y, amount, 0, modifiers, 0,
                          (long)timestamp);
}

REXPORT bool RCALL fire_key_pressed_event(i32 key, cstring characters,
                                          i32 key_code, i32 modifiers,
                                          i64 timestamp) {
  return fire_key_event(key, NRS_KEY_PRESSED, characters, key_code, modifiers,
                        (long)timestamp);
}

REXPORT bool RCALL fire_key_released_event(i32 key, cstring characters,
                                           i32 key_code, i32 modifiers,
                                           i64 timestamp) {
  return fire_key_event(key, NRS_KEY_RELEASED, characters, key_code, modifiers,
                        (long)timestamp);
}

REXPORT bool RCALL fire_key_typed_event(i32 key, cstring characters,
                                        i32 key_code, i32 modifiers,
                                        i64 timestamp) {
  return fire_key_event(key, NRS_KEY_TYPED, characters, key_code, modifiers,
                        (long)timestamp);
}
