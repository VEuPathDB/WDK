/**
 * 
 */
package org.gusdb.wdk.model.dbms;

/**
 * @author xingao
 * 
 */
public class QueryInfo {

    private int queryId;
    private String queryName;
    private String queryChecksum;
    private String cacheTable;

    /**
     * @return the queryId
     */
    public int getQueryId() {
        return queryId;
    }

    /**
     * @param queryId
     *            the queryId to set
     */
    public void setQueryId(int queryId) {
        this.queryId = queryId;
    }

    /**
     * @return the queryName
     */
    public String getQueryName() {
        return queryName;
    }

    /**
     * @param queryName
     *            the queryName to set
     */
    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    /**
     * @return the cacheTable
     */
    public String getCacheTable() {
        return cacheTable;
    }

    /**
     * @param cacheTable
     *            the cacheTable to set
     */
    public void setCacheTable(String cacheTable) {
        this.cacheTable = cacheTable;
    }

    /**
     * @return the queryChecksum
     */
    public String getQueryChecksum() {
        return queryChecksum;
    }

    /**
     * @param queryChecksum
     *            the queryChecksum to set
     */
    public void setQueryChecksum(String queryChecksum) {
        this.queryChecksum = queryChecksum;
    }
}
