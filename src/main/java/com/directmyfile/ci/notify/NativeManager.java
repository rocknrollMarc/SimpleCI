package com.directmyfile.ci.notify;

import com.directmyfile.ci.Main;

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

        for (String lib : libs) {
            String libName = System.mapLibraryName(lib);
            InputStream is = NativeManager.class.getResourceAsStream("/natives/" + libName);
            OutputStream os = null;
            try {
                File file = new File("natives", libName);
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

        }
    }
}
