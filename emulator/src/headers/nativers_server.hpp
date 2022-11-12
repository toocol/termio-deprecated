#ifndef NATIVERSSERVER_HPP
#define NATIVERSSERVER_HPP

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

// ---------------------------------------- VERSION
// ----------------------------------------

/// major version number
#define NATIVERS_VERSION_MAJOR 0

/// minor version number
#define NATIVERS_VERSION_MINOR 1

/// patch number
#define NATIVERS_VERSION_PATCH 0

/// complete version number
#define NATIVERS_VERSION_CODE                                      \
  (NATIVERS_VERSION_MAJOR * 10000 + NATIVERS_VERSION_MINOR * 100 + \
   NATIVERS_VERSION_PATCH)

/// version number as string
#define NATIVERS_VERSION_STRING "0.1.0"

// ---------------------------------------- INCLUDES
// ----------------------------------------

#include <chrono>
#include <iostream>
#include <queue>
#include <thread>

// force boost to be included as header only, also on windows
#define BOOST_ALL_NO_LIB 1

#include <boost/thread/xtime.hpp>

#define _USE_MATH_DEFINES
#include <cmath>

// in case M_PI hasn't been defined we do it manually
// see https://github.com/miho/NativeFX/issues/12
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif  // M_PI

#include "args.hxx"
#include "shared_memory.h"

// ---------------------------------------- NATIVERS CODE
// ----------------------------------------

namespace nativers {

struct NATIVE_EVENT {
  std::string type;
  std::string evt;
};

namespace ipc = boost::interprocess;

typedef std::function<void(const std::string& name, uchar* primary_buffer,
                           uchar* secondary_buffer, int W, int H)>
    redraw_callback;
typedef std::function<void(const std::string& name, event* evt)> event_callback;

/**
 * Test function to verify buffer data functionality.
 */
inline void setRgba(uchar* buffer_data, int buffer_w, int buffer_h, int x,
                    int y, uchar r, uchar g, uchar b, uchar a) {
  buffer_data[y * buffer_w * 4 + x * 4 + 0] = b;  // B
  buffer_data[y * buffer_w * 4 + x * 4 + 1] = g;  // G
  buffer_data[y * buffer_w * 4 + x * 4 + 2] = r;  // R
  buffer_data[y * buffer_w * 4 + x * 4 + 3] = a;  // A
}

/**
 * Deletes pre-existing shared memory objects.
 *
 * @param name name of the shared memory object to delete
 * @return status of this operation
 */
inline int deleteSharedMem(const std::string& name) {
  std::string info_name = name + IPC_INFO_NAME;
  std::string buffer_name = name + IPC_BUFF_NAME;
  std::string evt_mq_name = name + IPC_EVT_MQ_NAME;
  std::string evt_mq_native_name = name + IPC_EVT_MQ_NATIVE_NAME;

  ipc::shared_memory_object::remove(info_name.c_str());
  ipc::shared_memory_object::remove(buffer_name.c_str());
  ipc::message_queue::remove(evt_mq_name.c_str());
  ipc::message_queue::remove(evt_mq_native_name.c_str());

  std::cout << "> deleted shared-mem" << std::endl;
  std::cout << "  -> name:   " << name << std::endl;

  return NRS_SUCCESS;
}

inline uchar* createSharedBuffer(std::string buffer_name, int w, int h) {
  ipc::shared_memory_object* shm_buffer;
  ipc::mapped_region* buffer_region;
  // create the shared memory buffer object.
  shm_buffer = new ipc::shared_memory_object(
      ipc::create_only, buffer_name.c_str(), ipc::read_write);

  // set the size of the shared image buffer (w*h*#channels*sizeof(uchar))
  shm_buffer->truncate(w * h * /*#channels*/ 4 *
                       /*channel size*/ sizeof(uchar));

  // map the shared memory buffer object in this process
  buffer_region = new ipc::mapped_region(*shm_buffer, ipc::read_write);

  // get the address of the shared image buffer
  void* buffer_addr = buffer_region->get_address();

  // cast shared memory pointer to correct uchar type
  uchar* buffer_data = new (buffer_addr) uchar;

  return buffer_data;
}

struct SshPropery {
  std::string host;
  std::string user;
  std::string password;
};

class SharedCanvas final {
 private:
  std::size_t MAX_SIZE;

  std::string name;

  std::string info_name;
  std::string buffer_name;
  std::string primary_buffer_name;
  std::string secondary_buffer_name;
  std::string evt_mq_name;
  std::string evt_mq_native_name;

  uchar* primary_buffer;
  uchar* secondary_buffer;
  shared_memory_info* info_data;
  ipc::shared_memory_object* shm_info;
  ipc::message_queue* evt_mq;
  ipc::message_queue* evt_mq_native;
  void* evt_mq_msg_buff;
  STATUS status;
  int W;
  int H;

  std::queue<NATIVE_EVENT*> event_queue;

  SharedCanvas(const std::string& name, uchar* primary_buffer,
               uchar* secondary_buffer, ipc::shared_memory_object* shm_info,
               shared_memory_info* info_data, ipc::message_queue* evt_mq,
               void* evt_mq_msg_buff, ipc::message_queue* evt_mq_native, int w,
               int h, STATUS status) {
    this->name = name;
    this->primary_buffer = primary_buffer;
    this->secondary_buffer = secondary_buffer;
    this->shm_info = shm_info;
    this->info_data = info_data;
    this->evt_mq = evt_mq;
    this->evt_mq_msg_buff = evt_mq_msg_buff;
    this->evt_mq_native = evt_mq_native;

    this->W = w;
    this->H = h;

    this->status = status;

    this->info_name = name + IPC_INFO_NAME;
    this->buffer_name = name + IPC_BUFF_NAME;
    this->primary_buffer_name = this->buffer_name + IPC_PRIMARY_BUFFER_NAME;
    this->secondary_buffer_name = this->buffer_name + IPC_SECONDARY_BUFFER_NAME;
    this->evt_mq_name = name + IPC_EVT_MQ_NAME;
    this->evt_mq_native_name = name + IPC_EVT_MQ_NAME;

    this->MAX_SIZE = max_event_message_size();
  }

  void sendNativeEvent(std::string type, std::string evt) {
    // process events
    unsigned int priority = 0;

    // timed locking of resources
    boost::system_time const timeout =
        boost::get_system_time() +
        boost::posix_time::milliseconds(LOCK_TIMEOUT);

    native_event nevt;

    store_shared_string(type, nevt.type, IPC_NUM_NATIVE_EVT_TYPE_SIZE);
    store_shared_string(evt, nevt.evt_msg, IPC_NUM_NATIVE_EVT_MSG_SIZE);

    bool result = evt_mq_native->timed_send(&nevt, sizeof(native_event),
                                            priority, timeout);
    if (!result) {
      std::cerr << "[" + name +
                       "] can't send messages, message queue not accessible."
                << std::endl;
    }
  }

  void watchNativeEvent() {
    while (true) {
      while (!event_queue.empty()) {
        NATIVE_EVENT* evt = event_queue.front();
        event_queue.pop();
        sendNativeEvent(evt->type, evt->evt);
        delete evt;
      }
      Sleep(1);
    }
  }

  void detachNativeEventThread() {
    std::thread(&SharedCanvas::watchNativeEvent, this).detach();
  }

 public:
  static SharedCanvas* create(const std::string& name) {
    std::string info_name = name + IPC_INFO_NAME;
    std::string buffer_name = name + IPC_BUFF_NAME;
    std::string evt_mq_name = name + IPC_EVT_MQ_NAME;
    std::string evt_mq_native_name = name + IPC_EVT_MQ_NATIVE_NAME;
    std::string primary_buffer_name = buffer_name + IPC_PRIMARY_BUFFER_NAME;
    std::string secondary_buffer_name = buffer_name + IPC_SECONDARY_BUFFER_NAME;

    std::cout << "> creating shared-mem" << std::endl;
    std::cout << "  -> name:   " << name << std::endl;

    ipc::shared_memory_object* shm_info;
    ipc::message_queue* evt_mq;
    ipc::message_queue* evt_mq_native;

    try {
      // create the shared memory info object.
      shm_info = new ipc::shared_memory_object(
          ipc::create_only, info_name.c_str(), ipc::read_write);

      evt_mq = create_evt_mq(evt_mq_name);
      evt_mq_native = create_evt_mq_native(evt_mq_native_name);
    } catch (const ipc::interprocess_exception&) {
      // remove shared memory objects
      ipc::shared_memory_object::remove(info_name.c_str());
      ipc::shared_memory_object::remove(buffer_name.c_str());
      ipc::shared_memory_object::remove(primary_buffer_name.c_str());
      ipc::shared_memory_object::remove(secondary_buffer_name.c_str());
      ipc::message_queue::remove(evt_mq_name.c_str());
      ipc::message_queue::remove(evt_mq_native_name.c_str());

      std::cout << "> deleted pre-existing shared-mem" << std::endl;
      std::cout << "  -> name:   " << name << std::endl;

      // create the shared memory info object.
      shm_info = new ipc::shared_memory_object(
          ipc::create_only, info_name.c_str(), ipc::read_write);

      evt_mq = create_evt_mq(evt_mq_name);
      evt_mq_native = create_evt_mq_native(evt_mq_native_name);

      std::cout << "> created shared-mem" << std::endl;
      std::cout << "  -> name:   " << name << std::endl;
    }

    // set the shm size
    shm_info->truncate(sizeof(struct shared_memory_info));

    // map the shared memory info object in this process
    ipc::mapped_region* info_region =
        new ipc::mapped_region(*shm_info, ipc::read_write);

    // get the adress of the info object
    void* info_addr = info_region->get_address();

    // construct the shared structure in memory
    shared_memory_info* info_data = new (info_addr) shared_memory_info;

    // init c-strings of info_data struct
#ifdef _MSVC_LANG
    strcpy_s(info_data->client_to_server_msg, "");
    strcpy_s(info_data->client_to_server_res, "");
#else
    strcpy(info_data->client_to_server_msg, "");
    strcpy(info_data->client_to_server_res, "");
#endif

    int W = info_data->w;
    int H = info_data->h;

    uchar* primary_buffer = createSharedBuffer(primary_buffer_name, W, H);
    uchar* secondary_buffer = createSharedBuffer(secondary_buffer_name, W, H);

    std::size_t MAX_SIZE = max_event_message_size();
    void* evt_mq_msg_buff = malloc(MAX_SIZE);

    info_data->buffer_ready = true;

    SharedCanvas* sc = new SharedCanvas(
        name, primary_buffer, secondary_buffer, shm_info, info_data, evt_mq,
        evt_mq_msg_buff, evt_mq_native, W, H, NRS_SUCCESS);
    sc->detachNativeEventThread();
    return sc;
  }

  int terminate() { return deleteSharedMem(name); }

  bool hasFocus() { return info_data->focus; }

  int sharedStringType() { return info_data->shared_string_type; }

  /**
   * Must invoke responseSharedString() after getSharedString()
   */
  std::string getSharedString() {
    std::string sharedString =
        get_shared_string(info_data->client_to_server_msg);
    return sharedString;
  }

  void responseSharedString(std::string resp) {
    store_shared_string(resp, info_data->client_to_server_res);
    info_data->shared_string_type = NRS_SHARED_DEFAULT;
    info_data->client_to_server_res_semaphore.post();
  }

  bool isBufferReady() {
    // timed locking of resources
    boost::system_time const timeout =
        boost::get_system_time() +
        boost::posix_time::milliseconds(LOCK_TIMEOUT);
    bool locking_success = info_data->mutex.timed_lock(timeout);

    if (!locking_success) {
      std::cerr << "[" + name + "] "
                << "ERROR: cannot connect to '" << name << "':" << std::endl;
      std::cerr << " -> But we are unable to lock the resources." << std::endl;
      std::cerr << " -> Client not running?." << std::endl;
      return false;
    }

    bool result = info_data->buffer_ready;

    info_data->mutex.unlock();

    return result;
  }

  int bufferStatus() { return info_data->renderer_side_buffer_status; }

  void toggleBuffer() {
    info_data->renderer_side_buffer_status =
        -info_data->renderer_side_buffer_status;
  }

  bool isDirty() {
    int buffer_status = SharedCanvas::bufferStatus();
    bool is_dirty = false;
    if (buffer_status == PRIMARY_BUFFER) {
      is_dirty = info_data->primary_dirty;
    } else if (buffer_status == SECONDARY_BUFFER) {
      is_dirty = info_data->secondary_dirty;
    } else {
      std::cerr << "[isDirty] Invalid buffer status: " << buffer_status
                << std::endl;
      return false;
    }
    return is_dirty;
  }

  void setDirty(bool dirty) {
    int buffer_status = SharedCanvas::bufferStatus();
    if (buffer_status == PRIMARY_BUFFER) {
      info_data->primary_dirty = dirty;
    } else if (buffer_status == SECONDARY_BUFFER) {
      info_data->secondary_dirty = dirty;
    } else {
      std::cerr << "[setDirty] Invalid buffer status: " << buffer_status
                << std::endl;
      return;
    }
  }

  bool lock() {
    // timed locking of resources
    boost::system_time const timeout =
        boost::get_system_time() +
        boost::posix_time::milliseconds(LOCK_TIMEOUT);

    int buffer_status = SharedCanvas::bufferStatus();
    bool locking_success = false;
    if (buffer_status == PRIMARY_BUFFER) {
      locking_success = info_data->primary_buffer_mutex.timed_lock(timeout);
    } else if (buffer_status == SECONDARY_BUFFER) {
      locking_success = info_data->secondary_buffer_mutex.timed_lock(timeout);
    } else {
      std::cerr << "[lock] Invalid buffer status: " << buffer_status
                << std::endl;
    }

    if (!locking_success) {
      std::cerr << "[" + name + "] -> " << buffer_status
                << "ERROR DRAW: cannot connect to '" << name
                << "':" << std::endl;
      std::cerr << " -> But we are unable to lock the resources." << std::endl;
      std::cerr << " -> Client not running?." << std::endl;
      return NRS_ERROR | NRS_CONNECTION_ERROR;
    }
    return locking_success;
  }

  void unlock() {
    int buffer_status = SharedCanvas::bufferStatus();
    if (buffer_status == PRIMARY_BUFFER) {
      info_data->primary_buffer_mutex.unlock();
    } else if (buffer_status == SECONDARY_BUFFER) {
      info_data->secondary_buffer_mutex.unlock();
    } else {
      std::cerr << "[unlock] Invalid buffer status: " << buffer_status
                << std::endl;
    }
  }

  int draw(redraw_callback redraw) {
    bool is_dirty = SharedCanvas::isDirty();

    // if still is dirty it means that the client hasn't drawn the previous
    // frame. in this case we just wait with updating the buffer until the
    // client draws the content.
    if (is_dirty) {
      return NRS_SUCCESS;
    }
    redraw(name, primary_buffer, secondary_buffer, W, H);

    SharedCanvas::setDirty(true);

    return NRS_SUCCESS;
  }

  int resize(redraw_callback resized) {
    if (info_data->buffer_ready) {
      return NRS_SUCCESS;
    }

    int new_W = info_data->w;
    int new_H = info_data->h;

    if (new_W != W || new_H != H) {
      W = new_W;
      H = new_H;

      std::cout << "[" + name + "]"
                << "> resize to W: " << W << ", H: " << H << std::endl;

      ipc::shared_memory_object::remove(primary_buffer_name.c_str());
      primary_buffer = createSharedBuffer(primary_buffer_name, W, H);

      ipc::shared_memory_object::remove(secondary_buffer_name.c_str());
      secondary_buffer = createSharedBuffer(secondary_buffer_name, W, H);

      info_data->buffer_ready = true;

      resized(name, primary_buffer, secondary_buffer, W, H);
    }

    info_data->resize_semaphore.post();
    return NRS_SUCCESS;
  }

  void pushNativeEvent(std::string type, std::string evt) {
    NATIVE_EVENT* nv = new NATIVE_EVENT{type, evt};
    event_queue.push(nv);
  }

  void processEvents(event_callback events) {
    // process events
    ipc::message_queue::size_type recvd_size;
    unsigned int priority;

    while (evt_mq->get_num_msg() > 0) {
      bool result =
          evt_mq->try_receive(evt_mq_msg_buff, MAX_SIZE, recvd_size, priority);

      if (!result) {
        std::cerr
            << "[" + name +
                   "] ERROR: can't read messages, message queue not accessible."
            << std::endl;
      }

      event* evt = static_cast<event*>(evt_mq_msg_buff);

      // terminate if termination event was sent
      if (evt->type & NRS_TERMINATION_EVENT) {
        std::cerr << "[" + name + "] termination requested." << std::endl;
        this->terminate();
        std::cerr << "[" + name + "] done." << std::endl;
        std::exit(0);
      }

      events(name, evt);

    }  // end while has event messages
  }

  int get_status() { return this->status; }

  bool is_valid() { return this->status == NRS_SUCCESS; }
};
}  // end namespace nativers
#endif
