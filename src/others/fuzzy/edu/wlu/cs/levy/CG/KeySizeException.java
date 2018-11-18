package others.fuzzy.edu.wlu.cs.levy.CG;

public class KeySizeException extends KDException
{
    public static final long serialVersionUID = 2L;

    protected KeySizeException()
    {
        super("Key size mismatch");
    }
}