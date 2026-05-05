#include <jni.h>

/*
 * Compatibility stub kept only so the legacy native target still builds on
 * arm64. Runtime URI handling now goes through java.awt.Desktop in
 * GURLHandler, so no Carbon AppleEvent registration is required here.
 */

#define OS_NATIVE(func)	Java_org_limewire_ui_swing_GURLHandler_##func

JNIEXPORT jint JNICALL OS_NATIVE(InstallEventHandler)
    (JNIEnv *env, jobject self)
{
    return 0;
}

JNIEXPORT jint JNICALL OS_NATIVE(RemoveEventHandler)
    (JNIEnv *env, jobject self) 
{
    return 0;
}
