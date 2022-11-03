#ifndef _SHARED_MEMORY_H_
#define _SHARED_MEMORY_H_

/*
 * Copyright 2019-2019 Michael Hoffer <info@michaelhoffer.de>. All rights
 * reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * If you use this software for scientific research then please cite the
 * following publication(s):
 *
 * M. Hoffer, C. Poliwoda, & G. Wittum. (2013). Visual reflection library:
 * a framework for declarative GUI programming on the Java platform.
 * Computing and Visualization in Science, 2013, 16(4),
 * 181–192. http://doi.org/10.1007/s00791-014-0230-y
 */

#include <boost/interprocess/containers/string.hpp>
#include <boost/interprocess/ipc/message_queue.hpp>
#include <boost/interprocess/mapped_region.hpp>
#include <boost/interprocess/shared_memory_object.hpp>
#include <boost/interprocess/sync/interprocess_mutex.hpp>
#include <boost/interprocess/sync/interprocess_semaphore.hpp>
#include <map>

#define IPC_MSG_SIZE 4096
#define IPC_KEY_EVT_NUM_CHARS 8

#define IPC_SSH_PROPERTY_SIZE 10
#define IPC_SSH_INFO_SIZE 128

#define IPC_NUM_NATIVE_EVT_TYPE_SIZE 128
#define IPC_NUM_NATIVE_EVT_MSG_SIZE 1024

#define IPC_NUM_EVT_MSGS 100

#define IPC_INFO_NAME "_info_"
#define IPC_BUFF_NAME "_buff_"
#define IPC_EVT_MQ_NAME "_evt_mq_"
#define IPC_EVT_MQ_NATIVE_NAME "_evt_mq_native_"

// instead of Qt stuff, we use plain c++ & boost
// for the client lib
// therefore, we need to declare uchar (was provided by qt before)
typedef unsigned char uchar;

#include <boost/interprocess/allocators/allocator.hpp>
#include <boost/interprocess/offset_ptr.hpp>
#include <string>

#define LOCK_TIMEOUT 20  // milliseconds

namespace nativers {

typedef boost::interprocess::basic_string<char> shared_string;

/**
 * Status indicates success as well as different types of errors.
 */
enum STATUS {
  NRS_SUCCESS = 0,
  NRS_ERROR = 1,
  NRS_CONNECTION_ERROR = 2,
  NRS_TIMEOUT_ERROR = 4,
  NRS_ARGS_ERROR = 8
};

enum MOUSE_BTN {
  NRS_NO_BTN = 0,
  NRS_PRIMARY_BTN = 1,
  NRS_SECONDARY_BTN = 2,
  NRS_MIDDLE_BTN = 4
};

enum MODIFIER {
  NRS_NO_KEY = 0x00000000,
  NRS_SHIFT_KEY = 0x02000000,
  NRS_CONTROL_KEY = 0x04000000,
  NRS_ALT_KEY = 0x08000000,
  NRS_META_KEY = 0x10000000
};

enum EVENT_TYPE {
  NRS_NO_EVENT = 0,
  NRS_EVENT = 1,
  NRS_MOUSE_EVENT = 2,
  NRS_MOUSE_MOVED = 2 << 1,
  NRS_MOUSE_ENTERED = 2 << 2,
  NRS_MOUSE_EXITED = 2 << 3,
  NRS_MOUSE_RELEASED = 2 << 4,
  NRS_MOUSE_PRESSED = 2 << 5,
  NRS_MOUSE_CLICKED = 2 << 6,
  NRS_MOUSE_WHEEL = 2 << 7,

  NRS_KEY_EVENT = 2 << 8,
  NRS_KEY_PRESSED = 2 << 9,
  NRS_KEY_RELEASED = 2 << 10,
  NRS_KEY_TYPED = 2 << 11,

  NRS_REDRAW_EVENT = 2 << 12,
  NRS_TERMINATION_EVENT = 2 << 13,

  NRS_FOCUS_EVENT = 2 << 14,
  NRS_INPUT_TEXT_EVENT = 2 << 15,
  NRS_CREATE_SSH_SESSION_EVENT = 2 << 16
};

enum SHARED_STRING_TYPE {
  NRS_SHARED_DEFAULT = 0,
  NRS_SEND_TEXT = 1,
  NRS_REQUEST_SIZE = 2
};

struct event {
  int type = 0;
  long timestamp = 0;
};

struct mouse_event {
  int type = NRS_MOUSE_EVENT;
  long timestamp = 0;

  int buttons = NRS_NO_BTN;
  int modifiers = NRS_NO_KEY;
  int click_count = 0;
  double amount = 0;
  double x = 0;
  double y = 0;
};

struct key_event {
  int type = NRS_KEY_EVENT;
  long timestamp = 0;

  int modifiers = NRS_NO_KEY;
  char chars[IPC_KEY_EVT_NUM_CHARS +
             1];     // not initialized since it is not allowed
  int key_code = 0;  // 0 is defined as "unknown key"
};

struct redraw_event : event {
  int type = NRS_REDRAW_EVENT;
  long timestamp = 0;

  double x = 0;
  double y = 0;
  double w = 0;
  double h = 0;
};

struct termination_event {
  int type = NRS_TERMINATION_EVENT;
  long timestamp = 0;
};

struct focus_event {
  int type = NRS_FOCUS_EVENT;
  long timestamp = 0;

  bool focus = false;
};

struct create_ssh_session_event {
  int type = NRS_CREATE_SSH_SESSION_EVENT;
  int indicator = 0;
  long sessionId = 0;
  long timestamp = 0;

  char host[IPC_SSH_INFO_SIZE + 1];
  char user[IPC_SSH_INFO_SIZE + 1];
  char password[IPC_SSH_INFO_SIZE + 1];
};

/**
 * Event that is used to communicate events from native servers back to the
 * client Java API. It's intended to be used in a boost message queue. That's
 * why we don't use more complex types such as std::string etc.
 */
struct native_event {
  char type[IPC_NUM_NATIVE_EVT_TYPE_SIZE +
            1];  // not initialized since it is not allowed
  char evt_msg[IPC_NUM_NATIVE_EVT_MSG_SIZE +
               1];  // not initialized since it is not allowed
};

inline void store_shared_string(std::string str, char* str_to_store_to,
                                size_t size) {
  // copy client_to_server_msg
  for (size_t idx = 0; idx < str.size(); ++idx) {
    str_to_store_to[idx] = str[idx];
  }
  // fill unused entries with '\0'
  for (size_t idx = str.size(); idx < size + 1; ++idx) {
    str_to_store_to[idx] = '\0';
  }
}

inline void store_shared_string(std::string str, char* str_to_store_to) {
  // copy client_to_server_msg
  for (size_t idx = 0; idx < str.size(); ++idx) {
    str_to_store_to[idx] = str[idx];
  }
  // fill unused entries with '\0'
  for (size_t idx = str.size(); idx < IPC_MSG_SIZE + 1; ++idx) {
    str_to_store_to[idx] = '\0';
  }
}

inline void store_key_codes(std::string str, char* str_to_store_to) {
  // copy client_to_server_msg
  for (size_t idx = 0; idx < str.size(); ++idx) {
    str_to_store_to[idx] = str[idx];
  }
  // fill unused entries with '\0'
  for (size_t idx = str.size(); idx < IPC_KEY_EVT_NUM_CHARS + 1; ++idx) {
    str_to_store_to[idx] = '\0';
  }
}

inline std::string get_shared_string(char* str_to_store_to) {
  std::string shared_string = "";
  for (size_t idx = 0; idx < IPC_MSG_SIZE + 1; ++idx) {
    if (str_to_store_to[idx] == '\0') {
      break;
    }
    shared_string += str_to_store_to[idx];
    str_to_store_to[idx] = '\0';
  }
  return shared_string;
}

struct shared_memory_info {
  shared_memory_info()
      : buffer_semaphore(0),
        resize_semaphore(0),
        client_to_server_msg_semaphore(0),
        client_to_server_res_semaphore(0),
        img_buffer_size(0),
        shared_string_type(NRS_SHARED_DEFAULT),
        w(1280),
        h(800),
        dirty(false),
        buffer_ready(true),
        focus(false) {}

  // mutex to protect access
  boost::interprocess::interprocess_mutex mutex;

  boost::interprocess::interprocess_semaphore buffer_semaphore;
  boost::interprocess::interprocess_semaphore resize_semaphore;

  boost::interprocess::interprocess_semaphore client_to_server_msg_semaphore;
  boost::interprocess::interprocess_semaphore client_to_server_res_semaphore;

  int img_buffer_size;
  int shared_string_type;

  int w;
  int h;
  bool dirty;
  bool buffer_ready;
  bool focus;

  char client_to_server_msg[IPC_MSG_SIZE +
                            1];  // not initialized since it is not allowed
  char client_to_server_res[IPC_MSG_SIZE +
                            1];  // not initialized since it is not allowed
};

struct shared_memory_buffer {};

inline std::string get_info_name(int key, std::string name) {
  return name + IPC_INFO_NAME;
}

inline std::string get_evt_msg_queue_name(int key, std::string name) {
  return name + IPC_EVT_MQ_NAME;
}

inline std::string get_evt_msg_queue_native_name(int key, std::string name) {
  return name + IPC_EVT_MQ_NATIVE_NAME;
}

inline std::string get_buffer_name(int key, std::string name) {
  return name + IPC_BUFF_NAME;
}

inline boost::interprocess::message_queue* open_evt_mq(
    std::string evt_msg_queue_name) {
  boost::interprocess::message_queue* evt_msg_queue =
      new boost::interprocess::message_queue(
          boost::interprocess::open_only,  // only open (don't create)
          evt_msg_queue_name.c_str()       // name
      );

  return evt_msg_queue;
}

inline std::size_t max_event_message_size() {
  return (std::max)({sizeof(event), sizeof(mouse_event), sizeof(key_event),
                     sizeof(redraw_event), sizeof(focus_event),
                     sizeof(create_ssh_session_event)});
}

inline boost::interprocess::message_queue* create_evt_mq(
    std::string evt_msg_queue_name) {
  // find the maximum event message size
  std::size_t max_evt_struct_size =
      (std::max)({sizeof(event), sizeof(mouse_event), sizeof(key_event),
                  sizeof(redraw_event), sizeof(focus_event),
                  sizeof(create_ssh_session_event)});

  boost::interprocess::message_queue* evt_msg_queue =
      new boost::interprocess::message_queue(
          boost::interprocess::create_only,  // only open (don't create)
          evt_msg_queue_name.c_str(),        // name
          IPC_NUM_EVT_MSGS,                  // max message number
          max_evt_struct_size                // max message size
      );

  return evt_msg_queue;
}

inline boost::interprocess::message_queue* create_evt_mq_native(
    std::string evt_msg_queue_name) {
  // find the maximum event message size
  std::size_t max_evt_struct_size = sizeof(native_event);

  boost::interprocess::message_queue* evt_msg_queue =
      new boost::interprocess::message_queue(
          boost::interprocess::create_only,  // only open (don't create)
          evt_msg_queue_name.c_str(),        // name
          IPC_NUM_EVT_MSGS,                  // max message number
          max_evt_struct_size                // max message size
      );

  return evt_msg_queue;
}

}  // namespace nativers
#endif
