package com.directmyfile.ci.notify;

import java.io.*;

class NativeManager {

    static native void init(String host, short port, String nick, String user);
    
    static void loadNatives() {
        extractNatives();
        System.load(new File("natives", System.mapLibraryName("bot")).getAbsolutePath());
        System.load(new File("natives", System.mapLibraryName("wrapper")).getAbsolutePath());
    }

    private static void extractNatives() {
        File nativesDir = new File("natives");
        if (!nativesDir.exists())
            nativesDir.mkdir();
        nativesDir.deleteOnExit();

        String[] libs = new String[] { "bot", "wrapper" };
        for (String lib : libs) {
            String libName  = System.mapLibraryName(lib);
            InputStream  is = NativeManager.class.getResourceAsStream("/natives/" + libName);
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
                } catch (IOException ignored) {}
            }

        }
    }
}
