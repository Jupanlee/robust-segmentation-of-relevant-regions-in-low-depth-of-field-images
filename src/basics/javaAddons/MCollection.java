package basics.javaAddons;

import java.util.Collection;

public abstract interface MCollection<Item>
{
    public abstract int size();

    public abstract boolean isEmpty();

    public abstract boolean add(Item paramItem);

    public abstract boolean addAll(Collection<? extends Item> paramCollection);

    public abstract Item getFirst();

    public abstract void clear();

    public abstract Item remove();
}