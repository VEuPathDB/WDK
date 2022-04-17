package org.gusdb.wdk.jmx.mbeans.dbms;

import org.gusdb.fgputil.db.pool.DatabaseInstance;

public class DbInfoFactory {

    /**
     * Returns an implementation of DBInfo appropriate for the given db
     * 
     * @param db instance for which info should be extracted
     * @return appropriate implementation of DBInfo
     * @throws IllegalArgumentException if no DBInfo exists for the given db
     */
    public static DbInfo getDbInfo(DatabaseInstance db) {
        switch (db.getConfig().getPlatformEnum()) {
        case ORACLE:
            return new OracleDbInfo(db);
        case POSTGRESQL:
            return new PostgresDbInfo(db);
        default:
            throw new IllegalArgumentException("No DBInfo class for this platform.");
        }
    }
}
