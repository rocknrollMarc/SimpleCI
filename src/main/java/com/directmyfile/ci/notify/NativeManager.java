package com.directmyfile.ci.notify;

import com.directmyfile.ci.core.CI;
import com.directmyfile.ci.core.Main;

import java.io.*;

final class NativeManager {

    /* Exposed to the JNI */
    static NativeManager manager;

    private static native void Dinit();

    private static native void Ddone();

    private static native void botInit(String host, short port, String nick, String user, String commandPrefix);

    static native void msg(String channel, String msg);

    static native boolean isInChannel(String channel);

    static native void addAdmin(String user);

    static native void join(String channel);

    static native void disconnect();

    static native boolean loop();

    static native void connect();

    public final IRCBot bot;

    public NativeManager(IRCBot bot) {
        this.bot = bot;
        manager = this;
    }

    public static void init(String host, short port, String nick, String user, String prefix) {
        Dinit();
        botInit(host, port, nick, user, prefix);
    }

    public static void startLoop() {
        Main.setBotState(true);
        connect();
        while (Main.isRunning() && loop()) {
            try {
                // Fake work to prevent cpu cycles being from being wasted
                Thread.sleep(0, 1);
            } catch (InterruptedException ignored) {
            }
        }

        disconnect();
        Ddone();
        Main.setBotState(false);
    }

    /* Lib extraction */
    static void loadNatives() {
        extractNatives();
        System.load(new File("natives", System.mapLibraryName("bot")).getAbsolutePath());
    }

    private static void extractNatives() {
        File nativesDir = new File("natives");
        if (!nativesDir.exists())
            nativesDir.mkdir();
        nativesDir.deleteOnExit();

        String[] libs = {
                "bot"
        };

        String[] unrequired = {
                "libphobos2.so.0.64"
        };

        for (String lib : libs)
            extractNative(lib, true);
        for (String lib : unrequired)
            extract(lib, false);
    }

    private static void extractNative(String lib, boolean required) {
        extract(System.mapLibraryName(lib), required);
    }

    private static void extract(String lib, boolean required) {
        try {
            InputStream is = NativeManager.class.getResourceAsStream("/natives/" + lib);
            OutputStream os = null;
            try {
                if (is == null) {
                    if (required) {
                        CI.getLogger().error("Unable to open required lib \"" + lib + "\" for reading");
                        CI.getLogger().error("Things may not work well, resort to manual extraction");
                    }
                    return;
                }
                File file = new File("natives", lib);
                if (file.exists())
                    file.delete();

                os = new FileOutputStream(file);
                {
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = is.read(buffer)) != -1)
                        os.write(buffer, 0, read);
                }
                os.flush();
            } catch (IOException e) {
                if (required)
                    e.printStackTrace(System.err);
            } finally {
                try {
                    if (os != null)
                        os.close();
                    if (is != null)
                        is.close();
                } catch (IOException ignored) {
                }
            }
        } catch (Throwable t) {
            if (required)
                throw new RuntimeException(t);
        }
    }
}
