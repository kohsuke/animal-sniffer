package org.jvnet.animal_sniffer;

import java.util.Set;
import java.io.Serializable;

/**
 * @author Kohsuke Kawaguchi
 */
public class Clazz implements Serializable {
    public final String name;
    public final Set signatures;
    public final String superClass;

    public Clazz(String name, Set signatures, String superClass) {
        this.name = name;
        this.signatures = signatures;
        this.superClass = superClass;
    }

    private static final long serialVersionUID = 1L;
}
