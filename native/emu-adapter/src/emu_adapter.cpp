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

REXPORT ri32 RCALL next_key();

REXPORT ri32 RCALL connect_to(rstring);

REXPORT rbool RCALL terminate(ri32);

REXPORT rbool RCALL is_connected(ri32);

REXPORT rstring RCALL send_msg(ri32, rstring, ri32);

REXPORT void RCALL process_native_events(ri32);

REXPORT void RCALL resize(ri32, ri32, ri32);

REXPORT bool RCALL is_dirty(ri32);

REXPORT void RCALL redraw(ri32, ri32, ri32, ri32, ri32);

REXPORT void RCALL set_dirty(ri32, rbool);

REXPORT void RCALL set_buffer_ready(ri32, rbool);

REXPORT rbool RCALL is_buffer_ready(ri32);

REXPORT ri32 RCALL get_w(ri32);

REXPORT ri32 RCALL get_h(ri32);

REXPORT rbool RCALL fire_mouse_pressed_event(ri32, rf64, rf64, ri32, ri32,
                                             ri64);

REXPORT rbool RCALL fire_mouse_released_event(ri32, rf64, rf64, ri32, ri32,
                                              ri64);

REXPORT rbool RCALL fire_mouse_clicked_event(ri32, rf64, rf64, ri32, ri32, ri32,
                                             ri64);

REXPORT rbool RCALL fire_mouse_entered_event(ri32, rf64, rf64, ri32, ri32,
                                             ri64);

REXPORT rbool RCALL fire_mouse_exited_event(ri32, rf64, rf64, ri32, ri32, ri64);

REXPORT rbool RCALL fire_mouse_move_event(ri32, rf64, rf64, ri32, ri32, ri64);

REXPORT rbool RCALL fire_mouse_wheel_event(ri32, rf64, rf64, rf64, ri32, ri32,
                                           ri64);

REXPORT rbool RCALL fire_key_pressed_event(ri32, rstring, ri32, ri32, ri64);

REXPORT rbool RCALL fire_key_released_event(ri32, rstring, ri32, ri32, ri64);

REXPORT rbool RCALL fire_key_typed_event(ri32, rstring, ri32, ri32, ri64);

REXPORT rbool RCALL request_focus(ri32, rbool, ri64);

REXPORT rbool RCALL create_ssh_session(ri32, ri64, rstring, rstring, rstring,
                                       ri64);

REXPORT void* RCALL get_buffer(ri32);

REXPORT rbool RCALL lock(ri32);

REXPORT rbool RCALL lock(ri32, ri64);

REXPORT void RCALL unlock(ri32);

REXPORT void RCALL wait_for_buffer_changes(ri32);

REXPORT rbool RCALL has_buffer_changes(ri32);

REXPORT void RCALL lock_buffer(ri32);

REXPORT void RCALL unlock_buffer(ri32);