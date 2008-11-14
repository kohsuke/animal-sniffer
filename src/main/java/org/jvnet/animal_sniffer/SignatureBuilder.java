package org.jvnet.animal_sniffer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.commons.EmptyVisitor;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.util.Set;
import java.util.HashSet;
import java.util.zip.GZIPOutputStream;

/**
 * Builds up a signature list from the given classes.
 *
 * @author Kohsuke Kawaguchi
 */
public class SignatureBuilder extends ClassFileVisitor {
    public static void main(String[] args) throws IOException {
        SignatureBuilder builder = new SignatureBuilder(new FileOutputStream("signature"));
        builder.process(new File(System.getProperty("java.home"),"lib/rt.jar"));
        builder.close();
    }

    private final ObjectOutputStream oos;

    public SignatureBuilder(OutputStream out) throws IOException {
        oos = new ObjectOutputStream(new GZIPOutputStream(out));
    }

    public void close() throws IOException {
        oos.writeObject("<EOF>");   // EOF marker
        oos.close();
    }

    protected void process(String name, InputStream image) throws IOException {
        System.out.println(name);
        ClassReader cr = new ClassReader(image);
        SignatureVisitor v = new SignatureVisitor();
        cr.accept(v,0);
        v.end();
    }

    private class SignatureVisitor extends EmptyVisitor {
        String className;
        Set signatures;

        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.className = name;
            signatures = new HashSet();
        }

        public void end() throws IOException {
            oos.writeObject(className);
            oos.writeObject(signatures);
        }

        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            signatures.add(name+desc);
            return null;
        }

        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            signatures.add(name+"#"+desc);
            return null;
        }
    }
}
