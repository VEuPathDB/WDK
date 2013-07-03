package org.gusdb.wdk.jmx.mbeans.dbms;

import org.gusdb.fgputil.db.platform.SupportedPlatform;

public class DBInfoFactory {

    /**
     * Returns an implementation of AbstractDBInfo appropriate for the given platform
     * 
     * @param platform platform for which DBInfo is desired
     * @return appropriate implementation of AbstractDBInfo
     * @throws IllegalArgumentException if no DBInfo exists for the given platform
     */
    public static AbstractDBInfo getDbInfo(SupportedPlatform platform) {
        switch (platform) {
        case ORACLE:
            return new OracleDBInfo();
        case POSTGRES:
            return new PostgreSQLDBInfo();
        default:
            throw new IllegalArgumentException("No DBInfo class for this platform.");
        }
    }
}
