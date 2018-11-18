//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package basics;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class ProSet<T> extends HashSet<T> {
    public ProSet(Collection<? extends T> c) {
        super(c);
    }

    public ProSet(T t) {
        this.add(t);
    }

    public ProSet() {
    }

    public T remove() {
        Iterator<T> iterator = this.iterator();
        T t = iterator.next();
        iterator.remove();
        return t;
    }
}
