package others.fuzzy.edu.wlu.cs.levy.CG;

public class KeyDuplicateException extends KDException
{
    public static final long serialVersionUID = 1L;

    protected KeyDuplicateException()
    {
        super("Key already in tree");
    }
}