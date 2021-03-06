package eu.powet.FOTAA.jna;

import com.sun.jna.Native;


import java.io.*;


public class NativeLoader {

    public static synchronized FotaaJNA getInstance() {
        configure();
        return singleton;
    }


    private static FotaaJNA singleton = null;

    private static void configure()
    {
        if (singleton == null) {
            try {
                File folder = new File(System.getProperty("java.io.tmpdir") + File.separator + "native");
                if (folder.exists()) {
                    deleteOldFile(folder);
                }
                folder.mkdirs();
                String absolutePath = copyFileFromStream(getPath("flash.so"), folder.getAbsolutePath(), "flash" + getExtension());
               // NativeLibrary.addSearchPath("flash", folder.getAbsolutePath());

                singleton = (FotaaJNA) Native.loadLibrary(absolutePath, FotaaJNA.class);
                Native.synchronizedLibrary(singleton);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getExtension() {
        if (System.getProperty("os.name").toLowerCase().contains("nux")) {
            return ".so";
        }
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            return ".dynlib";
        }
        return null;
    }

    public static String getPath(String lib) {
        if (System.getProperty("os.name").toLowerCase().contains("nux")) {
            if (is64()) {
                return "nix64/"+lib;
            } else {
                return "nix32/"+lib;
            }
        }
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            return "osx/"+lib;
        }
        return null;
    }

    public static boolean is64() {
        String os = System.getProperty("os.arch").toLowerCase();
        return (os.contains("64"));
    }

    /* Utility fonctions */
    public static void deleteOldFile(File folder) {
        if (folder.isDirectory()) {
            for (File f : folder.listFiles()) {
                if (f.isFile()) {
                    f.delete();
                } else {
                    deleteOldFile(f);
                }
            }
        }
        folder.delete();
    }

    public static String copyFileFromStream(String inputFile, String path, String targetName) throws IOException {
        InputStream inputStream = NativeLoader.class.getClassLoader().getResourceAsStream(inputFile);
        if (inputStream != null) {
            File copy = new File(path + File.separator + targetName);
            copy.deleteOnExit();
            OutputStream outputStream = new FileOutputStream(copy);
            byte[] bytes = new byte[1024];
            int length = inputStream.read(bytes);
  
            while (length > -1) {
                outputStream.write(bytes, 0, length);
                length = inputStream.read(bytes);
            }
            inputStream.close();
            outputStream.flush();
            outputStream.close();
            return copy.getAbsolutePath();
        }
        return null;
    }


}
