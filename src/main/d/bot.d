module bot;

import irc.irc;
import std.conv; 

private IRCBot bot;

extern (C) {

    void init(const(char)* nick, const(char)* user, const(char)* realName, const(char)* addr, short port) {
        bot = new IRCBot(new IRCConfig(to!string(nick), to!string(user), to!string(realName), getAddress(to!string(addr), port)[0]));
    }

    void connect() {

    }
}

// In order to prevent unresolved symbol errors at runtime
// since this is a shared library
extern (C) export void _Dmain() {}

// Initialization functions
static import core.runtime;

private bool initialized;

extern (C) export void D_init() {
    if (initialized)
        return;
    initialized = true;
    core.runtime.Runtime.initialize();
}

extern (C) export void D_done() {
    if (!initialized)
        return;
    initialized = false;
    core.runtime.Runtime.terminate();
}
