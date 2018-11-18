package others.fuzzy.edu.wlu.cs.levy.CG;

class HRect
{
    protected HPoint min;
    protected HPoint max;

    protected HRect(int ndims)
    {
        this.min = new HPoint(ndims);
        this.max = new HPoint(ndims);
    }

    protected HRect(HPoint vmin, HPoint vmax)
    {
        this.min = ((HPoint)vmin.clone());
        this.max = ((HPoint)vmax.clone());
    }

    protected Object clone()
    {
        return new HRect(this.min, this.max);
    }

    protected HPoint closest(HPoint t)
    {
        HPoint p = new HPoint(t.coord.length);

        for (int i = 0; i < t.coord.length; i++) {
            if (t.coord[i] <= this.min.coord[i]) {
                p.coord[i] = this.min.coord[i];
            }
            else if (t.coord[i] >= this.max.coord[i]) {
                p.coord[i] = this.max.coord[i];
            }
            else {
                p.coord[i] = t.coord[i];
            }
        }

        return p;
    }

    protected static HRect infiniteHRect(int d)
    {
        HPoint vmin = new HPoint(d);
        HPoint vmax = new HPoint(d);

        for (int i = 0; i < d; i++) {
            vmin.coord[i] = (-1.0D / 0.0D);
            vmax.coord[i] = (1.0D / 0.0D);
        }

        return new HRect(vmin, vmax);
    }

    protected HRect intersection(HRect r)
    {
        HPoint newmin = new HPoint(this.min.coord.length);
        HPoint newmax = new HPoint(this.min.coord.length);

        for (int i = 0; i < this.min.coord.length; i++) {
            newmin.coord[i] = Math.max(this.min.coord[i], r.min.coord[i]);
            newmax.coord[i] = Math.min(this.max.coord[i], r.max.coord[i]);
            if (newmin.coord[i] >= newmax.coord[i]) return null;
        }

        return new HRect(newmin, newmax);
    }

    protected double area()
    {
        double a = 1.0D;

        for (int i = 0; i < this.min.coord.length; i++) {
            a *= (this.max.coord[i] - this.min.coord[i]);
        }

        return a;
    }

    public String toString() {
        return this.min + "\n" + this.max + "\n";
    }
}