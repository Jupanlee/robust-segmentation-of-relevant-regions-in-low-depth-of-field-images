//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package basics.javaAddons;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class MQueue<Item> implements List<Item>, Iterable<Item> {
    private Object[] items = null;
    private int indexOfFirstElement = 0;
    private int indexOfLastElement = -1;
    private int size = 0;

    private void init(int maxSize) {
        this.items = new Object[maxSize];
    }

    public MQueue(int maxSize) {
        this.init(maxSize);
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
        return (Item)this.items[this.indexOfFirstElement];
    }

    public Item get(int index) {
        return (Item)this.items[this.indexOfFirstElement + index];
    }

    public Object[] toArray() {
        return Arrays.copyOfRange(this.items, this.indexOfFirstElement, this.indexOfLastElement + 1);
    }

    public Item getLast() {
        return this.get(this.size - 1);
    }

    public int size() {
        return this.size;
    }

    public boolean addAll(Collection<? extends Item> collection) {
        Iterator i$ = collection.iterator();

        Object item;
        do {
            if (!i$.hasNext()) {
                return true;
            }

            item = i$.next();
        } while(this.add((Item)item));

        return false;
    }

    public boolean add(Item item) {
        if (this.size == this.items.length) {
            return false;
        } else {
            this.indexOfLastElement = (this.indexOfLastElement + 1) % this.items.length;
            this.items[this.indexOfLastElement] = item;
            ++this.size;
            return true;
        }
    }

    public Item set(int index, Item element) {
        Item last = (Item)this.items[this.indexOfFirstElement + index];
        this.items[this.indexOfFirstElement + index] = element;
        return last;
    }

    public Item removeFirst() {
        Item item = this.getFirst();
        this.items[this.indexOfFirstElement] = null;
        this.indexOfFirstElement = (this.indexOfFirstElement + 1) % this.items.length;
        --this.size;
        return item;
    }

    public Item remove(int index) {
        if (index == 0) {
            return this.removeFirst();
        } else {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public Iterator<Item> iterator() {
        return new MQueue.MQueueIterator(this);
    }

    /** @deprecated */
    @Deprecated
    public boolean contains(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** @deprecated */
    @Deprecated
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** @deprecated */
    @Deprecated
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** @deprecated */
    @Deprecated
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** @deprecated */
    @Deprecated
    public boolean addAll(int index, Collection<? extends Item> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** @deprecated */
    @Deprecated
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** @deprecated */
    @Deprecated
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** @deprecated */
    @Deprecated
    public void add(int index, Item element) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** @deprecated */
    @Deprecated
    public int indexOf(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** @deprecated */
    @Deprecated
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** @deprecated */
    @Deprecated
    public ListIterator<Item> listIterator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** @deprecated */
    @Deprecated
    public ListIterator<Item> listIterator(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** @deprecated */
    @Deprecated
    public List<Item> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private class MQueueIterator implements Iterator<Item> {
        MQueue<Item> mQueue;
        int index = -1;

        public MQueueIterator(MQueue q) {
            this.mQueue = q;
        }

        public boolean hasNext() {
            return this.index + 1 < this.mQueue.size();
        }

        public Item next() {
            ++this.index;
            return this.mQueue.get(this.index);
        }

        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
