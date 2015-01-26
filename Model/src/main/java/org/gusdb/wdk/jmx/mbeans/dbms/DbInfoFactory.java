package org.gusdb.wdk.jmx.mbeans.dbms;

import org.gusdb.fgputil.db.pool.DatabaseInstance;

public class DbInfoFactory {

    /**
     * Returns an implementation of DBInfo appropriate for the given platform
     * 
     * @param platform platform for which DBInfo is desired
     * @return appropriate implementation of DBInfo
     * @throws IllegalArgumentException if no DBInfo exists for the given platform
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
