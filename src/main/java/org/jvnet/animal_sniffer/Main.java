package org.jvnet.animal_sniffer;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.DataInputStream;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.Enumeration;

/**
 * @author Kohsuke Kawaguchi
 */
public class Main {
    public static void main(String[] args) throws IOException {
        if(args.length==0) {
            System.err.println("Usage: java -jar animal-sniffer.jar [JAR/CLASS FILES]");
            System.exit(-1);
        }

        for (int i = 0; i < args.length; i++) {
            process(new File(args[i]));
        }
    }

    private static void process(File file) throws IOException {
        if(file.isDirectory())
            processDirectory(file);
        else
        if(file.getName().endsWith(".class"))
            processClassFile(file);
        else
        if(file.getName().endsWith(".jar"))
            processJarFile(file);

        // ignore other files
    }

    private static void processDirectory(File dir) throws IOException {
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++)
            process(files[i]);
    }

    private static void processJarFile(File file) throws IOException {
        JarFile jar = new JarFile(file);

        Enumeration e = jar.entries();
        while (e.hasMoreElements()) {
            JarEntry x =  (JarEntry)e.nextElement();
            if(!x.getName().endsWith(".class")) continue;
            InputStream is = jar.getInputStream(x);
            try {
                process(file.getName()+':'+x.getName(),is);
            } finally {
                is.close();
            }
        }

    }

    private static void processClassFile(File file) throws IOException {
        InputStream in = new FileInputStream(file);
        try {
            process(file.getPath(),in);
        } finally {
            in.close();
        }
    }

    private static void process(String name, InputStream image) throws IOException {
        DataInputStream dis = new DataInputStream(image);
        byte[] buf = new byte[8];
        dis.readFully(buf);

        System.out.println(u2(buf[6],buf[7])+"."+u2(buf[4],buf[5])+" "+name);
    }

    private static int u2(byte u, byte d) {
        return ((int)u)*256+d;
    }
}
