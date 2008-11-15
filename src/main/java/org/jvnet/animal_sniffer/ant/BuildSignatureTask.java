package org.jvnet.animal_sniffer.ant;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.FileSet;
import org.jvnet.animal_sniffer.SignatureBuilder;

import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
public class BuildSignatureTask extends Task {

    private File dest;
    private FileSet src;

    public void setDest(File dest) {
        this.dest = dest;
    }

    public void execute() throws BuildException {
        try {
            SignatureBuilder builder = new SignatureBuilder(new FileOutputStream(dest));
            process(builder,"lib/rt.jar");
            process(builder,"lib/jce.jar");
            process(builder,"lib/jsse.jar");
            builder.close();
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }

    private void process(SignatureBuilder builder, String name) throws IOException {
        File f = new File(System.getProperty("java.home"), name);
        if(f.exists())
            builder.process(f);
    }
}
