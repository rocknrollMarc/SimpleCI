#include <jni.h>
#include <string>

/* Wrapper D initializing and deinitialization */
static void con() __attribute__((constructor));
static void decon() __attribute__((destructor));

extern "C" {
    void D_init();
    void D_done();
}

void con() {
    D_init();
}

void decon() {
    D_done();
}

JNIEnv* _env;

extern "C" {
    /* D functions */
    void init(const char*, const char*, const char*, const char*, short);
    void connect();

    /* Java functions */
    JNIEXPORT void JNICALL Java_com_directmyfile_ci_notify_NativeManager_init(JNIEnv* env, jclass clazz, jstring host, short port, jstring nick, jstring user) {
        _env = env;
        
        const char* _host = env->GetStringUTFChars(host, 0);
        const char* _nick = env->GetStringUTFChars(nick, 0);
        const char* _user = env->GetStringUTFChars(user, 0);

        init(_nick, _user, _user, _host, port);

        env->ReleaseStringUTFChars(host, _host);
        env->ReleaseStringUTFChars(nick, _nick);
        env->ReleaseStringUTFChars(user, _user);
    }
}
