package org.jvnet.animal_sniffer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.commons.EmptyVisitor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.zip.GZIPInputStream;

/**
 * Checks the signature against classes in this list.
 * @author Kohsuke Kawaguchi
 */
public class SignatureChecker extends ClassFileVisitor {
    private final Map/*<String, Clazz>*/ signatures = new HashMap();

    /**
     * Classes in this packages are considered to be resolved elsewhere and
     * thus not a subject of the error checking when referenced.
     */
    private final Set ignoredPackages;

    public static void main(String[] args) throws Exception {
        Set ignoredPackages = new HashSet();
        ignoredPackages.add("org/jvnet/animal_sniffer");
        ignoredPackages.add("org/objectweb/*");
        new SignatureChecker(new FileInputStream("signature"),ignoredPackages).process(new File("target/classes"));
    }

    public SignatureChecker(InputStream in, Set ignoredPackages) throws IOException {
        this.ignoredPackages = ignoredPackages;
        try {
            ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(in));
            while(true) {
                Clazz c = (Clazz) ois.readObject();
                if(c==null)    return; // finished
                signatures.put(c.name,c);
            }
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError(e.getMessage());
        }
    }

    protected void process(String name, InputStream image) throws IOException {
        System.out.println(name);
        ClassReader cr = new ClassReader(image);

        final Set warned = new HashSet();

        cr.accept(new EmptyVisitor() {
            public void visitMethodInsn(int opcode, String owner, String name, String desc) {
                check(owner, name + desc);
            }

            public void visitTypeInsn(int opcode, String type) {
                if(shouldBeIgnored(type))   return;
                Clazz sigs = (Clazz) signatures.get(type);
                if(sigs==null)
                    error("Undefined reference: "+type);
            }

            public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                check(owner, name + '#' + desc);
            }

            private void check(String owner, String sig) {
                if(shouldBeIgnored(owner))   return;
                for( Clazz c = (Clazz) signatures.get(owner); c!=null; c=(Clazz)signatures.get(c.superClass) ) 
                    if(c.signatures.contains(sig))
                        return; // found it
                error("Undefined reference: "+owner+'.'+sig);
            }

            private boolean shouldBeIgnored(String type) {
                String pkg = type.substring(0,type.lastIndexOf('/'));
                if(ignoredPackages.contains(pkg))
                    return true;

                // check wildcard form
                while(true) {
                    if(ignoredPackages.contains(pkg+"/*"))
                        return true;
                    int idx=pkg.lastIndexOf('/');
                    if(idx<0)   return false;
                    pkg = pkg.substring(0,idx);
                }
            }

            private void error(String msg) {
                if(warned.add(msg))
                    System.err.println(msg);
            }
        }, 0);
    }
}
