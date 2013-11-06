module test;

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