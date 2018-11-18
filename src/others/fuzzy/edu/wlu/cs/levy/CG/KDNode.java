package others.fuzzy.edu.wlu.cs.levy.CG;

import java.util.List;

class KDNode<T>
{
    protected HPoint k;
    T v;
    protected KDNode<T> left;
    protected KDNode<T> right;
    protected boolean deleted;

    protected static <T> int edit(HPoint key, Editor<T> editor, KDNode<T> t, int lev, int K)
            throws KeyDuplicateException
    {
        KDNode next_node = null;
        int next_lev = (lev + 1) % K;
        synchronized (t) {
            if (key.equals(t.k)) {
                boolean was_deleted = t.deleted;
                t.v = editor.edit(t.deleted ? null : t.v);
                t.deleted = (t.v == null);

                if (t.deleted == was_deleted)
                    return 0;
                if (was_deleted) {
                    return -1;
                }
                return 1;
            }
            if (key.coord[lev] > t.k.coord[lev]) {
                next_node = t.right;
                if (next_node == null) {
                    t.right = create(key, editor);
                    return t.right.deleted ? 0 : 1;
                }
            }
            else {
                next_node = t.left;
                if (next_node == null) {
                    t.left = create(key, editor);
                    return t.left.deleted ? 0 : 1;
                }
            }
        }

        return edit(key, editor, next_node, next_lev, K);
    }

    protected static <T> KDNode<T> create(HPoint key, Editor<T> editor) throws KeyDuplicateException
    {
        KDNode t = new KDNode(key, editor.edit(null));
        if (t.v == null) {
            t.deleted = true;
        }
        return t;
    }

    protected static <T> boolean del(KDNode<T> t) {
        synchronized (t) {
            if (!t.deleted) {
                t.deleted = true;
                return true;
            }
        }
        return false;
    }

    protected static <T> KDNode<T> srch(HPoint key, KDNode<T> t, int K)
    {
        for (int lev = 0; t != null; lev = (lev + 1) % K)
        {
            if ((!t.deleted) && (key.equals(t.k))) {
                return t;
            }
            if (key.coord[lev] > t.k.coord[lev]) {
                t = t.right;
            }
            else {
                t = t.left;
            }
        }

        return null;
    }

    protected static <T> void rsearch(HPoint lowk, HPoint uppk, KDNode<T> t, int lev, int K, List<KDNode<T>> v)
    {
        if (t == null) return;
        if (lowk.coord[lev] <= t.k.coord[lev]) {
            rsearch(lowk, uppk, t.left, (lev + 1) % K, K, v);
        }
        if (!t.deleted) {
            int j = 0;

            while ((j < K) && (lowk.coord[j] <= t.k.coord[j]) && (uppk.coord[j] >= t.k.coord[j])) {
                j++;
            }
            if (j == K) v.add(t);
        }
        if (uppk.coord[lev] > t.k.coord[lev])
            rsearch(lowk, uppk, t.right, (lev + 1) % K, K, v);
    }

    protected static <T> void nnbr(KDNode<T> kd, HPoint target, HRect hr, double max_dist_sqd, int lev, int K, NearestNeighborList<KDNode<T>> nnl, Checker<T> checker, long timeout)
    {
        if (kd == null) {
            return;
        }

        if ((timeout > 0L) && (timeout < System.currentTimeMillis())) {
            return;
        }

        int s = lev % K;

        HPoint pivot = kd.k;
        double pivot_to_target = HPoint.sqrdist(pivot, target);

        HRect left_hr = hr;
        HRect right_hr = (HRect)hr.clone();
        left_hr.max.coord[s] = pivot.coord[s];
        right_hr.min.coord[s] = pivot.coord[s];

        boolean target_in_left = target.coord[s] < pivot.coord[s];
        HRect further_hr;
        KDNode nearer_kd;
        HRect nearer_hr;
        KDNode further_kd;
        HRect further_hr;
        if (target_in_left) {
            KDNode nearer_kd = kd.left;
            HRect nearer_hr = left_hr;
            KDNode further_kd = kd.right;
            further_hr = right_hr;
        }
        else
        {
            nearer_kd = kd.right;
            nearer_hr = right_hr;
            further_kd = kd.left;
            further_hr = left_hr;
        }

        nnbr(nearer_kd, target, nearer_hr, max_dist_sqd, lev + 1, K, nnl, checker, timeout);

        KDNode nearest = (KDNode)nnl.getHighest();
        double dist_sqd;
        double dist_sqd;
        if (!nnl.isCapacityReached()) {
            dist_sqd = 1.7976931348623157E+308D;
        }
        else {
            dist_sqd = nnl.getMaxPriority();
        }

        max_dist_sqd = Math.min(max_dist_sqd, dist_sqd);

        HPoint closest = further_hr.closest(target);
        if (HPoint.sqrdist(closest, target) < max_dist_sqd)
        {
            if (pivot_to_target < dist_sqd)
            {
                nearest = kd;

                dist_sqd = pivot_to_target;

                if ((!kd.deleted) && ((checker == null) || (checker.usable(kd.v)))) {
                    nnl.insert(kd, dist_sqd);
                }

                if (nnl.isCapacityReached()) {
                    max_dist_sqd = nnl.getMaxPriority();
                }
                else {
                    max_dist_sqd = 1.7976931348623157E+308D;
                }

            }

            nnbr(further_kd, target, further_hr, max_dist_sqd, lev + 1, K, nnl, checker, timeout);
        }
    }

    private KDNode(HPoint key, T val)
    {
        this.k = key;
        this.v = val;
        this.left = null;
        this.right = null;
        this.deleted = false;
    }

    protected String toString(int depth) {
        String s = new StringBuilder().append(this.k).append("  ").append(this.v).append(this.deleted ? "*" : "").toString();
        if (this.left != null) {
            s = new StringBuilder().append(s).append("\n").append(pad(depth)).append("L ").append(this.left.toString(depth + 1)).toString();
        }
        if (this.right != null) {
            s = new StringBuilder().append(s).append("\n").append(pad(depth)).append("R ").append(this.right.toString(depth + 1)).toString();
        }
        return s;
    }

    private static String pad(int n) {
        String s = "";
        for (int i = 0; i < n; i++) {
            s = new StringBuilder().append(s).append(" ").toString();
        }
        return s;
    }

    private static void hrcopy(HRect hr_src, HRect hr_dst) {
        hpcopy(hr_src.min, hr_dst.min);
        hpcopy(hr_src.max, hr_dst.max);
    }

    private static void hpcopy(HPoint hp_src, HPoint hp_dst) {
        for (int i = 0; i < hp_dst.coord.length; i++)
            hp_dst.coord[i] = hp_src.coord[i];
    }
}