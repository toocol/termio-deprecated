#ifndef _EMU_ADAPTER_H_
#define _EMU_ADAPTER_H_

#include "shared_memory.h"

#define REXPORT __declspec(dllexport)
#define RCALL __stdcall

typedef long ri32;
typedef __int64 ri64;
typedef double rf64;
typedef bool rbool;
typedef std::string rstring;

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
#endif