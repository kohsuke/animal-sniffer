package org.jvnet.animal_sniffer.ant;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.jvnet.animal_sniffer.SignatureBuilder;

import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
public class BuildSignatureTask extends Task {

    private File dest;

    public void setDest(File dest) {
        this.dest = dest;
    }

    public void execute() throws BuildException {
        try {
            SignatureBuilder builder = new SignatureBuilder(new FileOutputStream(dest));
            builder.process(new File(System.getProperty("java.home"),"lib/rt.jar"));
            builder.close();
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }
}
