//
// Created by user on 2022/4/14.
//
#include "com_toocol_termio_common_jni_TermioJNI.h"
#include <conio.h>

/*
 * Class:     com_toocol_termio_utilities_jni_TermioJNI
 * Method:    chooseFiles
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_toocol_termio_utilities_jni_TermioJNI_chooseFiles
  (JNIEnv *env, jobject obj);

/*
 * Class:     com_toocol_termio_utilities_jni_TermioJNI
 * Method:    chooseDirectory
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_toocol_termio_utilities_jni_TermioJNI_chooseDirectory
  (JNIEnv *env, jobject obj);

/*
 * Class:     com_toocol_termio_utilities_jni_TermioJNI
 * Method:    getWindowWidth
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_toocol_termio_utilities_jni_TermioJNI_getWindowWidth
  (JNIEnv *env, jobject obj);

/*
 * Class:     com_toocol_termio_utilities_jni_TermioJNI
 * Method:    getWindowHeight
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_toocol_termio_utilities_jni_TermioJNI_getWindowHeight
  (JNIEnv *env, jobject obj);

/*
 * Class:     com_toocol_termio_utilities_jni_TermioJNI
 * Method:    getCursorPosition
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_toocol_termio_utilities_jni_TermioJNI_getCursorPosition
  (JNIEnv *env, jobject obj);

/*
 * Class:     com_toocol_termio_utilities_jni_TermioJNI
 * Method:    setCursorPosition
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_com_toocol_termio_utilities_jni_TermioJNI_setCursorPosition
  (JNIEnv *env, jobject obj, jint, jint);

/*
 * Class:     com_toocol_termio_utilities_jni_TermioJNI
 * Method:    cursorBackLine
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_toocol_termio_utilities_jni_TermioJNI_cursorBackLine
  (JNIEnv *env, jobject obj, jint);

/*
 * Class:     com_toocol_termio_utilities_jni_TermioJNI
 * Method:    showCursor
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_toocol_termio_utilities_jni_TermioJNI_showCursor
  (JNIEnv *env, jobject obj);

/*
 * Class:     com_toocol_termio_utilities_jni_TermioJNI
 * Method:    hideCursor
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_toocol_termio_utilities_jni_TermioJNI_hideCursor
  (JNIEnv *env, jobject obj);

/*
 * Class:     com_toocol_termio_utilities_jni_TermioJNI
 * Method:    cursorLeft
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_toocol_termio_utilities_jni_TermioJNI_cursorLeft
  (JNIEnv *env, jobject obj);

/*
 * Class:     com_toocol_termio_utilities_jni_TermioJNI
 * Method:    cursorRight
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_toocol_termio_utilities_jni_TermioJNI_cursorRight
  (JNIEnv *env, jobject obj);
