package others.fuzzy.edu.wlu.cs.levy.CG;

public class KeyMissingException extends KDException
{
    public static final long serialVersionUID = 3L;

    public KeyMissingException()
    {
        super("Key not found");
    }
}