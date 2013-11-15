#include <jni.h>
#include <string>
#include <iostream>

using namespace std;

/* States and objects from D to process within the JNI on a loop */
bool g_postReadyEvent = false;
bool g_listJobs = false;
bool g_loadJobs = false;
bool g_status = false;
bool g_build = false;

const char* g_channel;
const char* g_arg_1;

bool statesNeedProcessing() {
    return g_postReadyEvent ||
            g_listJobs ||
            g_status ||
            g_loadJobs ||
            g_build;
}

void resetState() {
    g_postReadyEvent = false;
    g_listJobs = false;
    g_loadJobs = false;
    g_status = false;
    g_build = false;

    g_channel = NULL;
    g_arg_1 = NULL;
}

/* Main code */
extern "C" {
    /* D functions */
    void init(const char*, const char*, const char*, const char*, short, const char*);
    void msg(const char*, const char*);
    bool inChannel(const char*);
    void addAdmin(const char*);
    void join(const char*);
    void disconnect();
    bool loop();

    /* Java functions */
    JNIEXPORT void JNICALL Java_com_directmyfile_ci_notify_NativeManager_botInit(JNIEnv* env, jclass clazz,
                                                    jstring host, short port, jstring nick, jstring user, jstring comPref) {
        const char* l_host = env->GetStringUTFChars(host, 0);
        const char* l_nick = env->GetStringUTFChars(nick, 0);
        const char* l_user = env->GetStringUTFChars(user, 0);
        const char* l_comPref = env->GetStringUTFChars(comPref, 0);

        init(l_nick, l_user, l_user, l_host, port, l_comPref);

        env->ReleaseStringUTFChars(host, l_host);
        env->ReleaseStringUTFChars(nick, l_nick);
        env->ReleaseStringUTFChars(user, l_user);
        env->ReleaseStringUTFChars(comPref, l_comPref);
    }

    JNIEXPORT void JNICALL Java_com_directmyfile_ci_notify_NativeManager_disconnect(JNIEnv* env, jclass clazz) {
        disconnect();
    }

    JNIEXPORT jboolean JNICALL Java_com_directmyfile_ci_notify_NativeManager_loop(JNIEnv* env, jclass clazz) {
        if (loop()) {
            if (!statesNeedProcessing())
                return JNI_TRUE;

            jclass bot_class = env->FindClass("com/directmyfile/ci/notify/IRCBot");
            jobject bot_obj;
            jmethodID method;
            {
                jclass manager_class = env->FindClass("com/directmyfile/ci/notify/NativeManager");
                jfieldID manager_obj_id = env->GetStaticFieldID(manager_class, "manager", "Lcom/directmyfile/ci/notify/NativeManager;");
                jobject manager_obj = env->GetStaticObjectField(manager_class, manager_obj_id);

                jfieldID bot_obj_id = env->GetFieldID(manager_class, "bot", "Lcom/directmyfile/ci/notify/IRCBot;");
                bot_obj = env->GetObjectField(manager_obj, bot_obj_id);
            }

            if (g_postReadyEvent) {
                method = env->GetMethodID(bot_class, "onReady", "()V");
                env->CallVoidMethod(bot_obj, method);
            }

            if (g_listJobs) {
                method = env->GetMethodID(bot_class, "listJobs", "(Ljava/lang/String;)V");
                jstring channel = env->NewStringUTF(g_channel);

                env->CallVoidMethod(bot_obj, method, channel);
            }

            if (g_status) {
                method = env->GetMethodID(bot_class, "status", "(Ljava/lang/String;Ljava/lang/String;)V");
                jstring channel = env->NewStringUTF(g_channel);
                jstring jobName = g_arg_1 != NULL ? env->NewStringUTF(g_arg_1) : NULL;

                env->CallVoidMethod(bot_obj, method, channel, jobName);
            }

            if (g_loadJobs) {
                method = env->GetMethodID(bot_class, "loadJobs", "(Ljava/lang/String;)V");
                jstring channel = env->NewStringUTF(g_channel);
                env->CallVoidMethod(bot_obj, method, channel);
            }

            if (g_build) {
                method = env->GetMethodID(bot_class, "build", "(Ljava/lang/String;Ljava/lang/String;)V");
                jstring channel = env->NewStringUTF(g_channel);
                jstring jobName = g_arg_1 != NULL ? env->NewStringUTF(g_arg_1) : NULL;

                env->CallVoidMethod(bot_obj, method, channel, jobName);
            }

            resetState();
            return JNI_TRUE;
        } else {
            return JNI_FALSE;
        }
    }

    JNIEXPORT void JNICALL Java_com_directmyfile_ci_notify_NativeManager_addAdmin(JNIEnv* env, jclass clazz, jstring user) {
        const char* l_user = env->GetStringUTFChars(user, 0);
        addAdmin(l_user);
        env->ReleaseStringUTFChars(user, l_user);
    }

    JNIEXPORT void JNICALL Java_com_directmyfile_ci_notify_NativeManager_join(JNIEnv* env, jclass clazz, jstring chan) {
        const char* l_chan = env->GetStringUTFChars(chan, 0);
        join(l_chan);
        env->ReleaseStringUTFChars(chan, l_chan);
    }

    JNIEXPORT void JNICALL Java_com_directmyfile_ci_notify_NativeManager_msg(JNIEnv* env, jclass clazz,
                                                                                jstring chan, jstring message) {
        const char* l_chan = env->GetStringUTFChars(chan, 0);
        const char* l_message  = env->GetStringUTFChars(message, 0);
        msg(l_chan, l_message);
        env->ReleaseStringUTFChars(chan, l_chan);
        env->ReleaseStringUTFChars(message, l_message);
    }

    JNIEXPORT jboolean JNICALL Java_com_directmyfile_ci_notify_NativeManager_isInChannel(JNIEnv* env, jclass clazz, jstring chan) {
        const char* l_chan = env->GetStringUTFChars(chan, 0);
        bool flag = inChannel(l_chan);
        env->ReleaseStringUTFChars(chan, l_chan);

        if (flag)
            return JNI_TRUE;
        else
            return JNI_FALSE;
    }
}

/* Functions for D */
extern "C" {

    void postReadyEvent() {
        g_postReadyEvent = true;
    }

    void listJobs(const char* chan) {
        g_listJobs = true;
        g_channel = chan;
    }

    void loadJobs(const char* chan) {
        g_loadJobs = true;
        g_channel = chan;
    }

    void build(const char* chan, const char* jobName) {
        g_build = true;
        g_channel = chan;
        g_arg_1 = jobName;
    }

    void status(const char* chan, const char* jobName) {
        g_status = true;
        g_channel = chan;
        g_arg_1 = jobName;
    }
}
