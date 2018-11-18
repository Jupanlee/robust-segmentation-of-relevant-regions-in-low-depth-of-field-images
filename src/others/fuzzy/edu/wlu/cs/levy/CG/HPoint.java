package others.fuzzy.edu.wlu.cs.levy.CG;

class HPoint
{
    protected double[] coord;

    protected HPoint(int n)
    {
        this.coord = new double[n];
    }

    protected HPoint(double[] x)
    {
        this.coord = new double[x.length];
        for (int i = 0; i < x.length; i++) this.coord[i] = x[i];
    }

    protected Object clone()
    {
        return new HPoint(this.coord);
    }

    protected boolean equals(HPoint p)
    {
        for (int i = 0; i < this.coord.length; i++) {
            if (this.coord[i] != p.coord[i])
                return false;
        }
        return true;
    }

    protected static double sqrdist(HPoint x, HPoint y)
    {
        return EuclideanDistance.sqrdist(x.coord, y.coord);
    }

    public String toString()
    {
        String s = "";
        for (int i = 0; i < this.coord.length; i++) {
            s = s + this.coord[i] + " ";
        }
        return s;
    }
}