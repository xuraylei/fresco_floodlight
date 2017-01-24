package edu.frescoplus.database;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by baykovr on 11/10/15.
 *
 * The FRESCO database provides a high level interface between fp modules and the NOS.
 *
 *
 * TODO
 * 1. DB Streams, packets,events and app data
 * 2. DB Stream Selectors/Filters (lambdas)
 *
 * // packets, events and app data
 */
public class FP_DB
{
    public ArrayList<String> packets;
    public ArrayList<String> events;
    public HashMap<String,ArrayList<FP_DBEntry>> app_tables;

    public FP_DB()
    {
        packets = new ArrayList<String>();

        events  = new ArrayList<String>();

        app_tables = new HashMap<String, ArrayList<FP_DBEntry> >();
    }
    public void makeTable(String id, ArrayList<FP_DBEntry> content )
    {
        app_tables.put(id, content);
    }

    public ArrayList<String> getTableAsStringArray(String tableName)
    {
        ArrayList<FP_DBEntry> table = app_tables.get(tableName);

        ArrayList<String> stringTable = new ArrayList<String>();

        
        for (FP_DBEntry entry : table)
        {
            stringTable.add(entry.toString());
        }
        return stringTable;
    }
}
