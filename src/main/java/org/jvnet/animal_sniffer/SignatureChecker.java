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
import java.util.zip.GZIPInputStream;

/**
 * Checks the signature against classes in this list.
 * @author Kohsuke Kawaguchi
 */
public class SignatureChecker extends ClassFileVisitor {
    private final Map/*<String, Set<String>>*/ signatures = new HashMap();

    public static void main(String[] args) throws Exception {
        new SignatureChecker(new FileInputStream("signature")).process(new File("target/classes"));
    }

    public SignatureChecker(InputStream in) throws IOException {
        try {
            ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(in));
            while(true) {
                String name = (String) ois.readObject();
                if(name.equals("<EOF>"))    return; // finished
                Set sigs = (Set) ois.readObject();
                signatures.put(name,sigs);
            }
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError(e.getMessage());
        }
    }

    protected void process(String name, InputStream image) throws IOException {
        System.out.println(name);
        ClassReader cr = new ClassReader(image);
        cr.accept(new EmptyVisitor() {
            public void visitMethodInsn(int opcode, String owner, String name, String desc) {
                check(owner, name + desc);
            }

            public void visitTypeInsn(int opcode, String type) {
                Set sigs = (Set) signatures.get(type);
                if(sigs==null)
                    error("Undefined reference: "+type);
            }

            public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                check(owner, name + '#' + desc);
            }
        }, 0);
    }

    private void check(String owner, String sig) {
        Set sigs = (Set) signatures.get(owner);
        if(sigs==null || !sigs.contains(sig))
            error("Undefined reference: "+owner+'.'+sig);
    }

    private void error(String msg) {
        System.err.println(msg);
    }
}
