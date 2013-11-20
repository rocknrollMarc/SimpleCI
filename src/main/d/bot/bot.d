module bot.bot;

import bot.core;
import std.conv;

alias to!string tos;

private shared BotCore core;

extern (C) export {

    void init(const(char)* nick, const(char)* user, const(char)* realName, 
                                                        const(char)* addr, short port, const(char)* comPref) {
        core = cast(shared) new BotCore(tos(nick), tos(user), tos(realName), tos(addr), port, tos(comPref));
    }

    bool inChannel(const(char)* chan) {
        return (cast(BotCore) core).isInChannel(tos(chan));
    }

    bool loop() {
        return (cast(BotCore) core).loop();
    }

    void addAdmin(const(char)* user) {
        (cast(BotCore) core).addAdmin(tos(user));
    }

    void join(const(char)* chan) {
        (cast(BotCore) core).join(tos(chan));
    }

    void msg(const(char)* chan, const(char)* _msg) {
        (cast(BotCore) core).msg(tos(chan), tos(_msg));
    }

    void disconnect() {
        if (core)
            (cast(BotCore) core).disconnect();
        core = null;
    }

    void Java_com_directmyfile_ci_notify_NativeManager_connect() {
        (cast(BotCore) core).connect();
    }
}

// In order to prevent unresolved symbol errors at runtime
// since this is a shared library
extern (C) export void _Dmain() {}

// Initialization functions
private bool initialized;

extern (C) export {
    void Java_com_directmyfile_ci_notify_NativeManager_Dinit() {
        if (initialized)
            return;
        initialized = true;

        import core.runtime;
        Runtime.initialize();
    }

    void Java_com_directmyfile_ci_notify_NativeManager_Ddone() {
        if (!initialized)
            return;
        initialized = false;

        import core.runtime;
        Runtime.terminate();
    }
}
