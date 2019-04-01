//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package basics.javaAddons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SortedList<T extends Comparable<T>> {
    private List<T> list = new ArrayList();
    private boolean sorted = true;

    public SortedList() {
    }

    public void add(T element) {
        this.list.add(element);
        this.sorted = false;
    }

    public T get(int index) {
        return this.list.get(index);
    }

    public List<T> getList() {
        return this.list;
    }

    public boolean contains(T element) {
        if (!this.sorted) {
            Collections.sort(this.list);
            this.sorted = true;
        }

        int searchIndex = Collections.binarySearch(this.list, element);
        return searchIndex != -1;
    }
}
