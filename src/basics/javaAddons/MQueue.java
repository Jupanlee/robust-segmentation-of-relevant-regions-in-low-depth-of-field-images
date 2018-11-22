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

public class MQueue<MItem> implements List<MItem>, Iterable<MItem> {
    private MItem[] items = null;
    private int indexOfFirstElement = 0;
    private int indexOfLastElement = -1;
    private int size = 0;

    private void init(int maxSize) {
//        this.items = new MItem[maxSize];
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

    public MItem getFirst() {
        return this.items[this.indexOfFirstElement];
    }

    public MItem get(int index) {
        return this.items[this.indexOfFirstElement + index];
    }

    public Object[] toArray() {
        return Arrays.copyOfRange(this.items, this.indexOfFirstElement, this.indexOfLastElement + 1);
    }

    public MItem getLast() {
        return this.get(this.size - 1);
    }

    public int size() {
        return this.size;
    }

    public boolean addAll(Collection<? extends MItem> collection) {
        Iterator<? extends  MItem> it = collection.iterator();

        MItem item;
        do {
            if (!it.hasNext()) {
                return true;
            }

            item = it.next();
        } while(this.add(item));

        return false;
    }

    public boolean add(MItem item) {
        if (this.size == this.items.length) {
            return false;
        } else {
            this.indexOfLastElement = (this.indexOfLastElement + 1) % this.items.length;
            this.items[this.indexOfLastElement] = item;
            ++this.size;
            return true;
        }
    }

    public MItem set(int index, MItem element) {
        MItem last = this.items[this.indexOfFirstElement + index];
        this.items[this.indexOfFirstElement + index] = element;
        return last;
    }

    public MItem removeFirst() {
        MItem item = this.getFirst();
        this.items[this.indexOfFirstElement] = null;
        this.indexOfFirstElement = (this.indexOfFirstElement + 1) % this.items.length;
        --this.size;
        return item;
    }

    public MItem remove(int index) {
        if (index == 0) {
            return this.removeFirst();
        } else {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public Iterator<MItem> iterator() {
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
    public boolean addAll(int index, Collection<? extends MItem> c) {
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
    public void add(int index, MItem element) {
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
    public ListIterator<MItem> listIterator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** @deprecated */
    @Deprecated
    public ListIterator<MItem> listIterator(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** @deprecated */
    @Deprecated
    public List<MItem> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private class MQueueIterator implements Iterator<MItem> {
        MQueue<MItem> mQueue;
        int index = -1;

        public MQueueIterator(MQueue q) {
            this.mQueue = q;
        }

        public boolean hasNext() {
            return this.index + 1 < this.mQueue.size();
        }

        public MItem next() {
            ++this.index;
            return this.mQueue.get(this.index);
        }

        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
