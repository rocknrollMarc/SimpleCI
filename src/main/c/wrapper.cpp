#include <jni.h>

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
