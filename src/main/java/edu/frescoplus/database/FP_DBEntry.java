package edu.frescoplus.database;


public class FP_DBEntry<K>
{
    public K identifier;
    public Object data;

    public FP_DBEntry(K identifier,Object data)
    {
        this.identifier = identifier;
        this.data = data;
    }

    public static String toString(FP_DBEntry entry)
    {
        return entry.data.toString();
    }
}
