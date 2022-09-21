// force boost to be included as header only, also on windows
#define BOOST_ALL_NO_LIB 1

#include <boost/thread/xtime.hpp>
#include <string>
#include <vector>

#include "com_toocol_termio_platform_nativefx_NativeBinding.h"
#include "jnitypeconverter.h"
#include "sharedmemory.h"

namespace ipc = boost::interprocess;

using namespace nativefx;

std::vector<std::string> names;
std::vector<shared_memory_info *> connections;
// for java events that are sent to the server
std::vector<ipc::message_queue *> evt_msg_queues;
// for native server events sent to the java clinet
std::vector<ipc::message_queue *> evt_msg_queues_native;
std::vector<void *> buffers;

std::vector<ipc::shared_memory_object *> shm_infos;
std::vector<ipc::mapped_region *> info_regions;
std::vector<ipc::shared_memory_object *> shm_buffers;
std::vector<ipc::mapped_region *> buffer_regions;

JNIEnv *jni_env;

bool fire_mouse_event(jint key, int evt_type, double x, double y, double amount,
                      int buttons, int modifiers, int click_count,
                      long timestamp) {
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

bool fire_key_event(int key, int evt_type, std::string const &chars,
                    int key_code, int modifiers, long timestamp) {
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

/*
 * Class:     com_toocol_termio_platform_nativefx_NativeBinding
 * Method:    nextKey
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_com_toocol_termio_platform_nativefx_NativeBinding_nextKey(JNIEnv *env,
                                                               jclass cls) {
  return connections.size();
}

/*
 * Class:     com_toocol_termio_platform_nativefx_NativeBinding
 * Method:    connectTo
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL
Java_com_toocol_termio_platform_nativefx_NativeBinding_connectTo(
    JNIEnv *env, jclass cls, jstring jname) {
  jni_env = env;

  using namespace boost::interprocess;
  std::string name = stringJ2C(env, jname);

  // setup key and names for new connection
  int key = connections.size();
  std::string info_name = get_info_name(key, name);
  std::string evt_msg_queue_name = get_evt_msg_queue_name(key, name);
  std::string evt_msg_queue_native_name =
      get_evt_msg_queue_native_name(key, name);
  std::string buffer_name = get_buffer_name(key, name);
  names.push_back(name);

  try {
    // open the shared memory object.
    shared_memory_object *shm_info =
        new shared_memory_object(open_only,          // only open (don't create)
                                 info_name.c_str(),  // name
                                 read_write          // read-write mode
        );

    shm_infos.push_back(shm_info);

    // map the whole shared memory in this process
    mapped_region *info_region =
        new mapped_region(*shm_info,  // What to map
                          read_write  // Map it as read-write
        );

    info_regions.push_back(info_region);

    // get the address of the mapped region
    void *info_addr = info_region->get_address();

    // construct the shared structure in memory
    shared_memory_info *info_data =
        static_cast<shared_memory_info *>(info_addr);
    connections.push_back(info_data);

    // create mq (for java clinet events transferred to server)
    ipc::message_queue *evt_msg_queue = open_evt_mq(evt_msg_queue_name);
    evt_msg_queues.push_back(evt_msg_queue);

    // create mq (for native server events transferred to java client)
    ipc::message_queue *evt_msg_queue_native =
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
    shared_memory_object *shm_buffer =
        new shared_memory_object(open_only,            // only open
                                 buffer_name.c_str(),  // name
                                 read_write            // read-write mode
        );

    shm_buffers.push_back(shm_buffer);

    // map the whole shared memory in this process
    mapped_region *buffer_region =
        new mapped_region(*shm_buffer,  // What to map
                          read_write    // Map it as read-write
        );

    buffer_regions.push_back(buffer_region);

    // get the address of the mapped region
    void *buffer_addr = buffer_region->get_address();

    buffers.push_back(buffer_addr);

    info_data->mutex.unlock();
  } catch (...) {
    std::cerr << "ERROR: cannot connect to '" << info_name
              << "'. Server probably not running." << std::endl;

    return -1;
  }

  return key;
}

void update_buffer_connection(int key) {
  if (key >= connections.size()) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return;
  }

  std::string name = names[key];
  std::string info_name = get_info_name(key, name);
  std::string buffer_name = get_buffer_name(key, name);

  try {
    // create a shared memory object.
    ipc::shared_memory_object *shm_buffer =
        new ipc::shared_memory_object(ipc::open_only,       // only open
                                      buffer_name.c_str(),  // name
                                      ipc::read_write       // read-write mode
        );

    if (shm_buffers[key] != NULL) {
      delete shm_buffers[key];
    }

    shm_buffers[key] = shm_buffer;

    // map the whole shared memory in this process
    ipc::mapped_region *buffer_region =
        new ipc::mapped_region(*shm_buffer,     // what to map
                               ipc::read_write  // map it as read-write
        );

    if (buffer_regions[key] != NULL) {
      delete buffer_regions[key];
    }

    buffer_regions[key] = buffer_region;

    // get the address of the mapped region
    void *buffer_addr = buffer_region->get_address();

    buffers[key] = buffer_addr;

  } catch (...) {
    std::cerr << "ERROR: cannot connect to '" << info_name
              << "'. Server probably not running." << std::endl;

    return;
  }
}

void fire_native_event(int key, std::string type, std::string evt) {
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

/*
 * Class:     com_toocol_termio_platform_nativefx_NativeBinding
 * Method:    terminate
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL
Java_com_toocol_termio_platform_nativefx_NativeBinding_terminate(JNIEnv *env,
                                                                 jclass cls,
                                                                 jint key) {
  if (key >= connections.size()) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return false;
  }

  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return boolC2J(false);
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

  return boolC2J(true);
}

/*
 * Class:     com_toocol_termio_platform_nativefx_NativeBinding
 * Method:    isConnected
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL
Java_com_toocol_termio_platform_nativefx_NativeBinding_isConnected(JNIEnv *env,
                                                                   jclass cls,
                                                                   jint key) {
  namespace ipc = boost::interprocess;

  return boolC2J(key < connections.size() && connections[key] != NULL);
}

/*
 * Class:     com_toocol_termio_platform_nativefx_NativeBinding
 * Method:    sendMsg
 * Signature: (ILjava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_com_toocol_termio_platform_nativefx_NativeBinding_sendMsg(JNIEnv *env,
                                                               jclass cls,
                                                               jint key,
                                                               jstring jmsg) {
  shared_memory_info *info_data = NULL;

  if (key >= connections.size()) {
    return stringC2J(env, "ERROR: key not available");
  }

  info_data = connections[key];

  std::string msg = stringJ2C(env, jmsg);

  // send a message to server
  store_shared_string(msg, info_data->client_to_server_msg);
  info_data->client_to_server_msg_semaphore.post();
  // return result from server
  info_data->client_to_server_res_semaphore.wait();
  return stringC2J(env, info_data->client_to_server_res);
}

/*
 * Class:     com_toocol_termio_platform_nativefx_NativeBinding
 * Method:    processNativeEvents
 * Signature: (I)V
 */
JNIEXPORT void JNICALL
Java_com_toocol_termio_platform_nativefx_NativeBinding_processNativeEvents(
    JNIEnv *env, jclass cls, jint key) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return;
  }

  // process events
  ipc::message_queue::size_type recvd_size;
  unsigned int priority;

  native_event nevt;

  while (evt_msg_queues_native[key]->get_num_msg() > 0) {
    // timed locking of resources
    boost::system_time const timeout =
        boost::get_system_time() +
        boost::posix_time::milliseconds(LOCK_TIMEOUT);

    bool result = evt_msg_queues_native[key]->timed_receive(
        &nevt, sizeof(native_event), recvd_size, priority, timeout);

    if (!result) {
      std::cerr << "[" << key
                << "] ERROR: can't read messages, message queue not accessible."
                << std::endl;
    }

    fire_native_event(key, nevt.type, nevt.evt_msg);
  }
}

/*
 * Class:     com_toocol_termio_platform_nativefx_NativeBinding
 * Method:    resize
 * Signature: (III)V
 */
JNIEXPORT void JNICALL
Java_com_toocol_termio_platform_nativefx_NativeBinding_resize(JNIEnv *env,
                                                              jclass cls,
                                                              jint key, jint w,
                                                              jint h) {
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

/*
 * Class:     com_toocol_termio_platform_nativefx_NativeBinding
 * Method:    isDirty
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL
Java_com_toocol_termio_platform_nativefx_NativeBinding_isDirty(JNIEnv *env,
                                                               jclass cls,
                                                               jint key) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
  } else {
    return boolC2J(connections[key]->dirty);
  }

  return false;
}

/*
 * Class:     com_toocol_termio_platform_nativefx_NativeBinding
 * Method:    redraw
 * Signature: (IIIII)V
 */
JNIEXPORT void JNICALL
Java_com_toocol_termio_platform_nativefx_NativeBinding_redraw(
    JNIEnv *env, jclass cls, jint key, jint x, jint y, jint w, jint h) {}

/*
 * Class:     com_toocol_termio_platform_nativefx_NativeBinding
 * Method:    setDirty
 * Signature: (IZ)V
 */
JNIEXPORT void JNICALL
Java_com_toocol_termio_platform_nativefx_NativeBinding_setDirty(
    JNIEnv *env, jclass cls, jint key, jboolean dirty) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
  } else {
    connections[key]->dirty = boolJ2C(dirty);
  }
}

/*
 * Class:     com_toocol_termio_platform_nativefx_NativeBinding
 * Method:    setBufferReady
 * Signature: (IZ)V
 */
JNIEXPORT void JNICALL
Java_com_toocol_termio_platform_nativefx_NativeBinding_setBufferReady(
    JNIEnv *env, jclass cls, jint key, jboolean value) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
  } else {
    connections[key]->buffer_ready = boolJ2C(value);
  }
}

/*
 * Class:     com_toocol_termio_platform_nativefx_NativeBinding
 * Method:    isBufferReady
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL
Java_com_toocol_termio_platform_nativefx_NativeBinding_isBufferReady(
    JNIEnv *env, jclass cls, jint key) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
  } else {
    return boolC2J(connections[key]->buffer_ready);
  }

  return false;
}

/*
 * Class:     com_toocol_termio_platform_nativefx_NativeBinding
 * Method:    getW
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL
Java_com_toocol_termio_platform_nativefx_NativeBinding_getW(JNIEnv *env,
                                                            jclass cls,
                                                            jint key) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return -1;
  }

  return connections[key]->w;
}

/*
 * Class:     com_toocol_termio_platform_nativefx_NativeBinding
 * Method:    getH
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL
Java_com_toocol_termio_platform_nativefx_NativeBinding_getH(JNIEnv *env,
                                                            jclass cls,
                                                            jint key) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return -1;
  }

  return connections[key]->h;
}

/*
 * Class:     com_toocol_termio_platform_nativefx_NativeBinding
 * Method:    fireMousePressedEvent
 * Signature: (IDDIIJ)Z
 */
JNIEXPORT jboolean JNICALL
Java_com_toocol_termio_platform_nativefx_NativeBinding_fireMousePressedEvent(
    JNIEnv *env, jclass cls, jint key, jdouble x, jdouble y, jint buttons,
    jint modifiers, jlong timestamp) {
  bool result = fire_mouse_event(key, NFX_MOUSE_MOVED, x, y, 0.0, buttons,
                                 modifiers, 0, timestamp);
  return boolC2J(result);
}

/*
 * Class:     com_toocol_termio_platform_nativefx_NativeBinding
 * Method:    fireMouseReleasedEvent
 * Signature: (IDDIIJ)Z
 */
JNIEXPORT jboolean JNICALL
Java_com_toocol_termio_platform_nativefx_NativeBinding_fireMouseReleasedEvent(
    JNIEnv *env, jclass cls, jint key, jdouble x, jdouble y, jint buttons,
    jint modifiers, jlong timestamp) {
  bool result = fire_mouse_event(key, NFX_MOUSE_RELEASED, x, y, 0.0, buttons,
                                 modifiers, 0, timestamp);
  return boolC2J(result);
}

/*
 * Class:     com_toocol_termio_platform_nativefx_NativeBinding
 * Method:    fireMouseClickedEvent
 * Signature: (IDDIIIJ)Z
 */
JNIEXPORT jboolean JNICALL
Java_com_toocol_termio_platform_nativefx_NativeBinding_fireMouseClickedEvent(
    JNIEnv *env, jclass cls, jint key, jdouble x, jdouble y, jint buttons,
    jint modifiers, jint click_count, jlong timestamp) {
  bool result = fire_mouse_event(key, NFX_MOUSE_CLICKED, x, y, 0.0, buttons,
                                 modifiers, click_count, timestamp);
  return boolC2J(result);
}

/*
 * Class:     com_toocol_termio_platform_nativefx_NativeBinding
 * Method:    fireMouseEnteredEvent
 * Signature: (IDDIIJ)Z
 */
JNIEXPORT jboolean JNICALL
Java_com_toocol_termio_platform_nativefx_NativeBinding_fireMouseEnteredEvent(
    JNIEnv *env, jclass cls, jint key, jdouble x, jdouble y, jint buttons,
    jint modifiers, jint click_count, jlong timestamp) {
  bool result = fire_mouse_event(key, NFX_MOUSE_ENTERED, x, y, 0.0, buttons,
                                 modifiers, 0, timestamp);
  return boolC2J(result);
}

/*
 * Class:     com_toocol_termio_platform_nativefx_NativeBinding
 * Method:    fireMouseExitedEvent
 * Signature: (IDDIIJ)Z
 */
JNIEXPORT jboolean JNICALL
Java_com_toocol_termio_platform_nativefx_NativeBinding_fireMouseExitedEvent(
    JNIEnv *env, jclass cls, jint key, jdouble x, jdouble y, jint buttons,
    jint modifiers, jint click_count, jlong timestamp) {
  bool result = fire_mouse_event(key, NFX_MOUSE_EXITED, x, y, 0.0, buttons,
                                 modifiers, 0, timestamp);
  return boolC2J(result);
}

/*
 * Class:     com_toocol_termio_platform_nativefx_NativeBinding
 * Method:    fireMouseMoveEvent
 * Signature: (IDDIIJ)Z
 */
JNIEXPORT jboolean JNICALL
Java_com_toocol_termio_platform_nativefx_NativeBinding_fireMouseMoveEvent(
    JNIEnv *env, jclass cls, jint key, jdouble x, jdouble y, jint buttons,
    jint modifiers, jlong timestamp) {
  bool result = fire_mouse_event(key, NFX_MOUSE_MOVED, x, y, 0.0, buttons,
                                 modifiers, 0, timestamp);
  return boolC2J(result);
}

/*
 * Class:     com_toocol_termio_platform_nativefx_NativeBinding
 * Method:    fireMouseWheelEvent
 * Signature: (IDDDIIJ)Z
 */
JNIEXPORT jboolean JNICALL
Java_com_toocol_termio_platform_nativefx_NativeBinding_fireMouseWheelEvent(
    JNIEnv *env, jclass cls, jint key, jdouble x, jdouble y, jdouble amount,
    jint buttons, jint modifiers, jlong timestamp) {
  bool result = fire_mouse_event(key, NFX_MOUSE_WHEEL, x, y, amount, buttons,
                                 modifiers, 0, timestamp);
  return boolC2J(result);
}

/*
 * Class:     com_toocol_termio_platform_nativefx_NativeBinding
 * Method:    fireKeyPressedEvent
 * Signature: (ILjava/lang/String;IIJ)Z
 */
JNIEXPORT jboolean JNICALL
Java_com_toocol_termio_platform_nativefx_NativeBinding_fireKeyPressedEvent(
    JNIEnv *env, jclass cls, jint key, jstring characters, jint keyCode,
    jint modifiers, jlong timestamp) {
  std::string chars = stringJ2C(env, characters);

  bool result = fire_key_event(key, NFX_KEY_PRESSED, chars, keyCode, modifiers,
                               timestamp);
  return boolC2J(result);
}

/*
 * Class:     com_toocol_termio_platform_nativefx_NativeBinding
 * Method:    fireKeyReleasedEvent
 * Signature: (ILjava/lang/String;IIJ)Z
 */
JNIEXPORT jboolean JNICALL
Java_com_toocol_termio_platform_nativefx_NativeBinding_fireKeyReleasedEvent(
    JNIEnv *env, jclass cls, jint key, jstring characters, jint keyCode,
    jint modifiers, jlong timestamp) {
  std::string chars = stringJ2C(env, characters);

  bool result = fire_key_event(key, NFX_KEY_RELEASED, chars, keyCode, modifiers,
                               timestamp);
  return boolC2J(result);
}

/*
 * Class:     com_toocol_termio_platform_nativefx_NativeBinding
 * Method:    fireKeyTypedEvent
 * Signature: (ILjava/lang/String;IIJ)Z
 */
JNIEXPORT jboolean JNICALL
Java_com_toocol_termio_platform_nativefx_NativeBinding_fireKeyTypedEvent(
    JNIEnv *env, jclass cls, jint key, jstring characters, jint keyCode,
    jint modifiers, jlong timestamp) {
  std::string chars = stringJ2C(env, characters);

  bool result =
      fire_key_event(key, NFX_KEY_TYPED, chars, keyCode, modifiers, timestamp);
  return boolC2J(result);
}

/*
 * Class:     com_toocol_termio_platform_nativefx_NativeBinding
 * Method:    requestFocus
 * Signature: (IZ)Z
 */
JNIEXPORT jboolean JNICALL
Java_com_toocol_termio_platform_nativefx_NativeBinding_requestFocus(
    JNIEnv *env, jclass cls, jint key, jboolean focus, jlong timestamp) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return boolC2J(false);
  }

  connections[key]->focus = focus;

  focus_event evt;
  evt.type |= NFX_FOCUS_EVENT;
  evt.focus = focus;
  evt.timestamp = timestamp;

  // timed locking of resources
  boost::system_time const timeout =
      boost::get_system_time() + boost::posix_time::milliseconds(LOCK_TIMEOUT);

  bool result = evt_msg_queues[key]->timed_send(
      &evt,         // data to send
      sizeof(evt),  // size of the data (check it fits into max_size)
      0,              // msg priority
      timeout         // timeout
  );

  return boolC2J(result);
}

/*
 * Class:     com_toocol_termio_platform_nativefx_NativeBinding
 * Method:    getBuffer
 * Signature: (I)Ljava/nio/ByteBuffer;
 */
JNIEXPORT jobject JNICALL
Java_com_toocol_termio_platform_nativefx_NativeBinding_getBuffer(JNIEnv *env,
                                                                 jclass cls,
                                                                 jint key) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return NULL;
  }

  update_buffer_connection(key);

  void *buffer_addr = buffers[key];

  jobject result = env->NewDirectByteBuffer(
      buffer_addr, connections[key]->w * connections[key]->h * 4);

  return result;
}

/*
 * Class:     com_toocol_termio_platform_nativefx_NativeBinding
 * Method:    lock
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL
Java_com_toocol_termio_platform_nativefx_NativeBinding_lock__I(JNIEnv *env,
                                                               jclass cls,
                                                               jint key) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return false;
  } else {
    // try to lock (returns true if successful, false if wasn't successful
    // within the specified LOCK_TIMEOUT)
    boost::system_time const timeout =
        boost::get_system_time() +
        boost::posix_time::milliseconds(LOCK_TIMEOUT);
    return boolC2J(connections[key]->mutex.timed_lock(timeout));
  }
}

/*
 * Class:     com_toocol_termio_platform_nativefx_NativeBinding
 * Method:    lock
 * Signature: (IJ)Z
 */
JNIEXPORT jboolean JNICALL
Java_com_toocol_termio_platform_nativefx_NativeBinding_lock__IJ(
    JNIEnv *env, jclass cls, jint key, jlong jtimeout) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
    return false;
  } else {
    // try to lock (returns true if successful, false if wasn't successful
    // within the specified LOCK_TIMEOUT)
    boost::system_time const timeout =
        boost::get_system_time() + boost::posix_time::milliseconds(jtimeout);
    return boolC2J(connections[key]->mutex.timed_lock(timeout));
  }
}

/*
 * Class:     com_toocol_termio_platform_nativefx_NativeBinding
 * Method:    unlock
 * Signature: (I)V
 */
JNIEXPORT void JNICALL
Java_com_toocol_termio_platform_nativefx_NativeBinding_unlock(JNIEnv *env,
                                                              jclass cls,
                                                              jint key) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
  } else {
    connections[key]->mutex.unlock();
  }
}

/*
 * Class:     com_toocol_termio_platform_nativefx_NativeBinding
 * Method:    waitForBufferChanges
 * Signature: (I)V
 */
JNIEXPORT void JNICALL
Java_com_toocol_termio_platform_nativefx_NativeBinding_waitForBufferChanges(
    JNIEnv *env, jclass cls, jint key) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
  } else {
    while (!connections[key]->buffer_semaphore.try_wait()) {
    }
  }
}

/*
 * Class:     com_toocol_termio_platform_nativefx_NativeBinding
 * Method:    hasBufferChanges
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL
Java_com_toocol_termio_platform_nativefx_NativeBinding_hasBufferChanges(
    JNIEnv *env, jclass cls, jint key) {
  if (key >= connections.size() || connections[key] == NULL) {
    std::cerr << "ERROR: key not available: " << key << std::endl;
  } else {
    return boolC2J(connections[key]->buffer_semaphore.try_wait());
  }

  return false;
}

/*
 * Class:     com_toocol_termio_platform_nativefx_NativeBinding
 * Method:    lockBuffer
 * Signature: (I)V
 */
JNIEXPORT void JNICALL
Java_com_toocol_termio_platform_nativefx_NativeBinding_lockBuffer(JNIEnv *env,
                                                                  jclass cls,
                                                                  jint key) {}

/*
 * Class:     com_toocol_termio_platform_nativefx_NativeBinding
 * Method:    unlockBuffer
 * Signature: (I)V
 */
JNIEXPORT void JNICALL
Java_com_toocol_termio_platform_nativefx_NativeBinding_unlockBuffer(JNIEnv *env,
                                                                    jclass cls,
                                                                    jint key) {}
