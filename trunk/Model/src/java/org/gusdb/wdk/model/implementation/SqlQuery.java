package org.gusdb.wdk.model.implementation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gusdb.wdk.model.Query;
import org.gusdb.wdk.model.QueryInstance;
import org.gusdb.wdk.model.RDBMSPlatformI;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;

public class SqlQuery extends Query implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = -3356832112831465261L;
    
    static final String RESULT_TABLE_MACRO = "%%RESULT_TABLE%%";
    
    private List< WdkModelText > sqls;
    private String sql;
    private RDBMSPlatformI platform;
    
    public SqlQuery( ) {
        sqls = new ArrayList< WdkModelText >();
    }
    
    // ///////////////////////////////////////////////////////////////////
    // /////////// Public properties ////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////
    
    public QueryInstance makeInstance() {
        return new SqlQueryInstance( this );
    }
    
    public void setSql( String sql ) {
        this.sql = sql;
        signature = null;
    }
    
    public void addSql( WdkModelText sql ) {
        this.sqls.add( sql );
    }
    
    // ///////////////////////////////////////////////////////////////////
    // /////////// Protected ////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////
    
    protected void setResources( WdkModel model ) throws WdkModelException {
        this.platform = model.getRDBMSPlatform();
        super.setResources( model );
    }
    
    /**
     * @param values
     *            These values are assumed to be pre-validated
     */
    protected String instantiateSql( Map< String, String > values ) {
        return instantiateSql( values, sql );
    }
    
    /**
     * @param values
     *            These values are assumed to be pre-validated
     * @param inputSql
     *            Sql to use (may be modified from this.sql)
     */
    protected String instantiateSql( Map< String, String > values,
            String inputSql ) {
        Iterator< String > keySet = values.keySet().iterator();
        String s = inputSql;
        while ( keySet.hasNext() ) {
            String key = keySet.next();
            String regex = "\\$\\$" + key + "\\$\\$";
            // also escape all single quotes in the value
            s = s.replaceAll( regex, values.get( key ) );
        }
        
        return s;
    }
    
    protected StringBuffer formatHeader() {
        String newline = System.getProperty( "line.separator" );
        StringBuffer buf = super.formatHeader();
        buf.append( "  sql='" + sql + "'" + newline );
        return buf;
    }
    
    public String getSql() {
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
    public Query getBaseQuery( Set< String > excludedColumns ) {
        SqlQuery query = new SqlQuery();
        // clone the base query
        clone( query, excludedColumns );
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
        return sql.replaceAll( "\\s+", " " );
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Query#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources( String projectId ) throws WdkModelException {
        super.excludeResources( projectId );
        
        // exclude sql
        for ( WdkModelText sql : sqls ) {
            if ( sql.include( projectId ) ) {
                this.sql = sql.getText();
                break;
            }
        }
        sqls = null;
        // check if the sql is assigned
        if ( sql == null )
            throw new WdkModelException( "The required SQL in query "
                    + getFullName() + " is missing for project " + projectId );
    }
}
