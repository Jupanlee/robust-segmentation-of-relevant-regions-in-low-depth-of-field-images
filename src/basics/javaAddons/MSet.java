//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package basics.javaAddons;

import java.awt.Point;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MSet<Item extends MSet.MItem> extends MQueue<Item> {
    public final int MAX_ID_SIZE = 150000000;
    private boolean[] objectIDs;

    public MSet(int capacity) {
        super(capacity);
        this.clear();
    }

    public boolean add(Item item) {
        int id = item.getID();
        if (this.objectIDs[id]) {
            return true;
        } else {
            this.objectIDs[id] = true;
            return super.add(item);
        }
    }

    public void clear() {
        super.clear();
        this.objectIDs = new boolean[150000000];
    }

    public Item removeFirst() {
        Item item = super.removeFirst();
        this.objectIDs[item.getID()] = false;
        return item;
    }

    public static void main(String[] args) {
        int MAX_POINTS = 10000000;
        MSet<MSet.MPoint> mSet = new MSet(MAX_POINTS);
        HashSet<MSet.MPoint> hashSet = new HashSet();
        test(mSet);
        test(hashSet);
        if (mSet.size() == hashSet.size()) {
            System.out.println("Equal elements count == " + mSet.size());
        } else {
            System.err.println("ERROR!");
        }

    }

    public static void test(Collection c) {
        Random rnd = new Random(0L);
        long start = System.currentTimeMillis();
        int var4 = 5000000;

        while(var4-- > 0) {
            MSet.MPoint p = new MSet.MPoint();
            p.x = rnd.nextInt(1000);
            p.y = rnd.nextInt(1000);
            c.add(p);
        }

        System.out.println("Time: " + (System.currentTimeMillis() - start) + "ms for testing " + c.getClass());
    }

    static class MPoint extends Point implements MSet.MItem {
        public static final int MAX_WIDTH = 1000;

        MPoint() {
        }

        public int getID() {
            return this.y * 1000 + this.x;
        }
    }

    public interface MItem {
        int getID();
    }
}
