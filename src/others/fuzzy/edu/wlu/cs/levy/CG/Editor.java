package others.fuzzy.edu.wlu.cs.levy.CG;

public abstract interface Editor<T>
{
    public abstract T edit(T paramT)
            throws KeyDuplicateException;

    public static class Replacer<T> extends Editor.BaseEditor<T>
    {
        public Replacer(T val)
        {
            super();
        }
        public T edit(T current) {
            return this.val;
        }
    }

    public static class OptionalInserter<T> extends Editor.BaseEditor<T>
    {
        public OptionalInserter(T val)
        {
            super();
        }
        public T edit(T current) {
            return current == null ? this.val : current;
        }
    }

    public static class Inserter<T> extends Editor.BaseEditor<T>
    {
        public Inserter(T val)
        {
            super();
        }
        public T edit(T current) throws KeyDuplicateException {
            if (current == null) {
                return this.val;
            }
            throw new KeyDuplicateException();
        }
    }

    public static abstract class BaseEditor<T>
            implements Editor<T>
    {
        final T val;

        public BaseEditor(T val)
        {
            this.val = val;
        }

        public abstract T edit(T paramT)
                throws KeyDuplicateException;
    }
}