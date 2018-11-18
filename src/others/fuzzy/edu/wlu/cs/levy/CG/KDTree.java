package others.fuzzy.edu.wlu.cs.levy.CG;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class KDTree<T>
{
    final long m_timeout;
    private final int m_K;
    private KDNode<T> m_root;
    private int m_count;

    public KDTree(int k)
    {
        this(k, 0L);
    }
    public KDTree(int k, long timeout) {
        this.m_timeout = timeout;
        this.m_K = k;
        this.m_root = null;
    }

    public void insert(double[] key, T value)
            throws KeySizeException, KeyDuplicateException
    {
        edit(key, new Editor.Inserter(value));
    }

    public void edit(double[] key, Editor<T> editor)
            throws KeySizeException, KeyDuplicateException
    {
        if (key.length != this.m_K) {
            throw new KeySizeException();
        }

        synchronized (this)
        {
            if (null == this.m_root) {
                this.m_root = KDNode.create(new HPoint(key), editor);
                this.m_count = (this.m_root.deleted ? 0 : 1);
                return;
            }
        }

        this.m_count += KDNode.edit(new HPoint(key), editor, this.m_root, 0, this.m_K);
    }

    public T search(double[] key)
            throws KeySizeException
    {
        if (key.length != this.m_K) {
            throw new KeySizeException();
        }

        KDNode kd = KDNode.srch(new HPoint(key), this.m_root, this.m_K);

        return kd == null ? null : kd.v;
    }

    public void delete(double[] key)
            throws KeySizeException, KeyMissingException
    {
        delete(key, false);
    }

    public void delete(double[] key, boolean optional)
            throws KeySizeException, KeyMissingException
    {
        if (key.length != this.m_K) {
            throw new KeySizeException();
        }
        KDNode t = KDNode.srch(new HPoint(key), this.m_root, this.m_K);
        if (t == null) {
            if (!optional) {
                throw new KeyMissingException();
            }

        }
        else if (KDNode.del(t))
            this.m_count -= 1;
    }

    public T nearest(double[] key)
            throws KeySizeException
    {
        List nbrs = nearest(key, 1, null);
        return nbrs.get(0);
    }

    public List<T> nearest(double[] key, int n)
            throws KeySizeException, IllegalArgumentException
    {
        return nearest(key, n, null);
    }

    public List<T> nearestEuclidean(double[] key, double dist)
            throws KeySizeException
    {
        return nearestDistance(key, dist, new EuclideanDistance());
    }

    public List<T> nearestHamming(double[] key, double dist)
            throws KeySizeException
    {
        return nearestDistance(key, dist, new HammingDistance());
    }

    public List<T> nearest(double[] key, int n, Checker<T> checker)
            throws KeySizeException, IllegalArgumentException
    {
        if (n <= 0) {
            return new LinkedList();
        }

        NearestNeighborList nnl = getnbrs(key, n, checker);

        n = nnl.getSize();
        Stack nbrs = new Stack();

        for (int i = 0; i < n; i++) {
            KDNode kd = (KDNode)nnl.removeHighest();
            nbrs.push(kd.v);
        }

        return nbrs;
    }

    public List<T> range(double[] lowk, double[] uppk)
            throws KeySizeException
    {
        if (lowk.length != uppk.length) {
            throw new KeySizeException();
        }

        if (lowk.length != this.m_K) {
            throw new KeySizeException();
        }

        List found = new LinkedList();
        KDNode.rsearch(new HPoint(lowk), new HPoint(uppk), this.m_root, 0, this.m_K, found);

        List o = new LinkedList();
        for (KDNode node : found) {
            o.add(node.v);
        }
        return o;
    }

    public int size()
    {
        return this.m_count;
    }

    public String toString() {
        return this.m_root.toString(0);
    }

    private NearestNeighborList<KDNode<T>> getnbrs(double[] key) throws KeySizeException
    {
        return getnbrs(key, this.m_count, null);
    }

    private NearestNeighborList<KDNode<T>> getnbrs(double[] key, int n, Checker<T> checker)
            throws KeySizeException
    {
        if (key.length != this.m_K) {
            throw new KeySizeException();
        }

        NearestNeighborList nnl = new NearestNeighborList(n);

        HRect hr = HRect.infiniteHRect(key.length);
        double max_dist_sqd = 1.7976931348623157E+308D;
        HPoint keyp = new HPoint(key);

        if (this.m_count > 0) {
            long timeout = this.m_timeout > 0L ? System.currentTimeMillis() + this.m_timeout : 0L;

            KDNode.nnbr(this.m_root, keyp, hr, max_dist_sqd, 0, this.m_K, nnl, checker, timeout);
        }

        return nnl;
    }

    private List<T> nearestDistance(double[] key, double dist, DistanceMetric metric)
            throws KeySizeException
    {
        NearestNeighborList nnl = getnbrs(key);
        int n = nnl.getSize();
        Stack nbrs = new Stack();

        for (int i = 0; i < n; i++) {
            KDNode kd = (KDNode)nnl.removeHighest();
            HPoint p = kd.k;
            if (metric.distance(kd.k.coord, key) < dist) {
                nbrs.push(kd.v);
            }
        }

        return nbrs;
    }
}