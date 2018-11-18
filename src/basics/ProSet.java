package basics;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class ProSet<T> extends HashSet<T>
{
    public ProSet(Collection<? extends T> c)
    {
        super(c);
    }

    public ProSet(T t) {
        add(t);
    }

    public ProSet()
    {
    }

    public T remove()
    {
        Iterator iterator = iterator();
        Object t = iterator.next();
        iterator.remove();
        return t;
    }
}