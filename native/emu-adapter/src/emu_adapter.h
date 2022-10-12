#ifndef _EMU_ADAPTER_H_
#define _EMU_ADAPTER_H_

#include "shared_memory.h"

#define REXPORT __declspec(dllexport)
#define RCALL __stdcall

typedef long i32;
typedef __int64 i64;
typedef double f64;
typedef std::string rstring;

/**
 * Acquire next avaliable key of connection.
 */
REXPORT i32 RCALL next_key();

/**
 * Connect to the shared memory by name.
 *
 * @param name
 */
REXPORT i32 RCALL connect_to(rstring);

/**
 * Terminate the shared memory connection by key.
 *
 * @param key
 */
REXPORT bool RCALL terminate(i32);

/**
 * Judge whether the shared memory corresponding to the key is connected.
 *
 * @param key
 */
REXPORT bool RCALL is_connected(i32);

/**
 * Block send msg to shared memory server side with response.
 *
 * @param key
 * @param msg
 * @param timestamp
 */
REXPORT rstring RCALL send_msg(i32, rstring, i32);

/**
 * Process the native events which store in the shared memory.
 *
 * @param key
 */
REXPORT void RCALL process_native_events(i32);

/**
 * Resize the teminal emulator.
 *
 * @param key
 * @param width
 * @param height
 */
REXPORT void RCALL resize(i32, i32, i32);

/**
 * When the native image buffer was changed, the property of dirty was true.
 *
 * @param key
 */
REXPORT bool RCALL is_dirty(i32);

/**
 * Client request redraw the native image buffer.
 */
REXPORT void RCALL redraw(i32, i32, i32, i32, i32);

/**
 * Set the native image buffer was dirty.
 */
REXPORT void RCALL set_dirty(i32, bool);

/**
 * Set true when the native image buffer was rendering completed,
 * set false otherwise.
 *
 * @param key
 * @param isBufferReady
 */
REXPORT void RCALL set_buffer_ready(i32, bool);

/**
 * Get the native image buffer redering state.
 *
 * @param key
 */
REXPORT bool RCALL is_buffer_ready(i32);

/*
 * Get the width of native image buffer.
 *
 * @param key
 */
REXPORT i32 RCALL get_w(i32);

/**
 * Get the height of native image buffer.
 *
 * @param key
 */
REXPORT i32 RCALL get_h(i32);

/*
 * Tell terminal emulator to request focus or not.
 *
 * @param key
 * @param isFocus
 * @param timestamp
 */
REXPORT bool RCALL request_focus(i32, bool, i64);

/*
 * Create a ssh sesison.
 *
 * @param key
 * @param sessionId
 * @param host
 * @param user
 * @param password
 * @param timestamp
 */
REXPORT bool RCALL create_ssh_session(i32, i64, rstring, rstring, rstring, i64);

/**
 * Get the native image buffer.
 *
 * @param key
 */
REXPORT void* RCALL get_buffer(i32);

REXPORT bool RCALL lock(i32);

REXPORT bool RCALL lock(i32, i64);

REXPORT void RCALL unlock(i32);

REXPORT void RCALL wait_for_buffer_changes(i32);

REXPORT bool RCALL has_buffer_changes(i32);

REXPORT void RCALL lock_buffer(i32);

REXPORT void RCALL unlock_buffer(i32);

REXPORT bool RCALL fire_mouse_pressed_event(i32 key, f64 x, f64 y, i32 buttons,
                                            i32 modifiers, i64 timestamp);

REXPORT bool RCALL fire_mouse_released_event(i32 key, f64 x, f64 y, i32 buttons,
                                             i32 modifiers, i64 timestamp);

REXPORT bool RCALL fire_mouse_clicked_event(i32 key, f64 x, f64 y, i32 buttons,
                                            i32 modifiers, i32 click_count,
                                            i64 timestamp);

REXPORT bool RCALL fire_mouse_entered_event(i32 key, f64 x, f64 y, i32 buttons,
                                            i32 modifiers, i32 click_count,
                                            i64 timestamp);

REXPORT bool RCALL fire_mouse_exited_event(i32 key, f64 x, f64 y, i32 buttons,
                                           i32 modifiers, i32 click_count,
                                           i64 timestamp);

REXPORT bool RCALL fire_mouse_move_event(i32 key, f64 x, f64 y, i32 buttons,
                                         i32 modifiers, i64 timestamp);

REXPORT bool RCALL fire_mouse_wheel_event(i32 key, f64 x, f64 y, f64 amount,
                                          i32 buttons, i32 modifiers,
                                          i64 timestamp);

REXPORT bool RCALL fire_key_pressed_event(i32 key, rstring characters,
                                          i32 key_code, i32 modifiers,
                                          i64 timestamp);

REXPORT bool RCALL fire_key_released_event(i32 key, rstring characters,
                                           i32 key_code, i32 modifiers,
                                           i64 timestamp);

REXPORT bool RCALL fire_key_typed_event(i32 key, rstring characters,
                                        i32 key_code, i32 modifiers,
                                        i64 timestamp);
#endif