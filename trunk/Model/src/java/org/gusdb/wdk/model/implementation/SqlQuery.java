package org.gusdb.wdk.model.implementation;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.gusdb.wdk.model.Query;
import org.gusdb.wdk.model.QueryInstance;
import org.gusdb.wdk.model.RDBMSPlatformI;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

public class SqlQuery extends Query implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -3356832112831465261L;

    static final String RESULT_TABLE_MACRO = "%%RESULT_TABLE%%";

    private String sql;
    private RDBMSPlatformI platform;

    public SqlQuery() {
        super();
    }

    // ///////////////////////////////////////////////////////////////////
    // /////////// Public properties ////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////

    public QueryInstance makeInstance() {
        return new SqlQueryInstance(this);
    }

    public void setSql(String sql) {
        this.sql = sql;
        signature = null;
    }

    // ///////////////////////////////////////////////////////////////////
    // /////////// Protected ////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////

    protected void setResources(WdkModel model) throws WdkModelException {
        this.platform = model.getRDBMSPlatform();
        super.setResources(model);
    }

    /**
     * @param values These values are assumed to be pre-validated
     */
    protected String instantiateSql(Map<String, String> values) {
        return instantiateSql(values, sql);
    }

    /**
     * @param values These values are assumed to be pre-validated
     * @param inputSql Sql to use (may be modified from this.sql)
     */
    protected String instantiateSql(Map<String, String> values, String inputSql) {
        Iterator<String> keySet = values.keySet().iterator();
        String s = inputSql;
        while (keySet.hasNext()) {
            String key = keySet.next();
            String regex = "\\$\\$" + key + "\\$\\$";
            // also escape all single quotes in the value
            s = s.replaceAll(regex, values.get(key));
        }

        return s;
    }

    protected StringBuffer formatHeader() {
        String newline = System.getProperty("line.separator");
        StringBuffer buf = super.formatHeader();
        buf.append("  sql='" + sql + "'" + newline);
        return buf;
    }

    String getSql() {
        return sql;
    }

    RDBMSPlatformI getRDBMSPlatform() {
        return platform;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Query#getBaseQuery(java.util.Set)
     */
    @Override
    public Query getBaseQuery(Set<String> excludedColumns) {
        SqlQuery query = new SqlQuery();
        // clone the base query
        clone(query, excludedColumns);
        // clone the members belongs to the child
        query.platform = this.platform;
        query.sql = this.sql;
        return query;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Query#getSignatureData()
     */
    @Override
    protected String getSignatureData() {
        return sql.replaceAll("\\s+", " ");
    }
}
