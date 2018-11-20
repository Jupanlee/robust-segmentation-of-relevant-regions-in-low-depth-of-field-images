package basics.javaAddons;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class MQueue<Item> implements List<Item>, Iterable<Item>
{
    private Item[] items = null;
    private int indexOfFirstElement = 0;
    private int indexOfLastElement = -1;
    private int size = 0;

    private void init(int maxSize) {
        this.items = new Item[maxSize];
    }

    public MQueue(int maxSize) {
        init(maxSize);
    }

    public void clear() {
        this.size = 0;
        this.indexOfFirstElement = 0;
        this.indexOfLastElement = -1;
    }

    public boolean isEmpty() {
        return this.size == 0;
    }

    public Item getFirst() {
        return this.items[this.indexOfFirstElement];
    }

    public Item get(int index) {
        return this.items[(this.indexOfFirstElement + index)];
    }

    public Object[] toArray()
    {
        return Arrays.copyOfRange(this.items, this.indexOfFirstElement, this.indexOfLastElement + 1);
    }

    public Item getLast()
    {
        return get(this.size - 1);
    }

    public int size() {
        return this.size;
    }

    public boolean addAll(Collection<Item> collection) {
        for (Iterator i$ = collection.iterator(); i$.hasNext(); ) { Item item = i$.next();
            if (!add(item)) return false;
        }
        return true;
    }

    public boolean add(Item item) {
        if (this.size == this.items.length) return false;

        this.indexOfLastElement = ((this.indexOfLastElement + 1) % this.items.length);

        this.items[this.indexOfLastElement] = item;
        this.size += 1;

        return true;
    }

    public Item set(int index, Item element)
    {
        Object last = this.items[(this.indexOfFirstElement + index)];
        this.items[(this.indexOfFirstElement + index)] = element;
        return last;
    }

    public Item removeFirst() {
        Object item = getFirst();
        this.items[this.indexOfFirstElement] = null;

        this.indexOfFirstElement = ((this.indexOfFirstElement + 1) % this.items.length);
        this.size -= 1;

        return item;
    }

    public Item remove(int index) {
        if (index == 0) return removeFirst();
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Iterator<Item> iterator()
    {
        return new MQueueIterator(this);
    }
    @Deprecated
    public boolean contains(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Deprecated
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Deprecated
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Deprecated
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Deprecated
    public boolean addAll(int index, Collection<? extends Item> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Deprecated
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Deprecated
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Deprecated
    public void add(int index, Item element) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Deprecated
    public int indexOf(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Deprecated
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Deprecated
    public ListIterator<Item> listIterator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Deprecated
    public ListIterator<Item> listIterator(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Deprecated
    public List<Item> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private class MQueueIterator
            implements Iterator<Item>
    {
        MQueue<Item> mQueue;
        int index = -1;

        public MQueueIterator(MQueue q) {
            this.mQueue = q;
        }

        public boolean hasNext() {
            return this.index + 1 < this.mQueue.size();
        }

        public Item next() {
            this.index += 1;
            return this.mQueue.get(this.index);
        }

        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}