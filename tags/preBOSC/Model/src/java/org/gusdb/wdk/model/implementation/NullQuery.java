package org.gusdb.gus.wdk.model.implementation;

import org.gusdb.gus.wdk.model.Column;
import org.gusdb.gus.wdk.model.Param;
import org.gusdb.gus.wdk.model.Query;
import org.gusdb.gus.wdk.model.QueryInstance;

public class NullQuery extends Query {
    
	public static final NullQuery INSTANCE = new NullQuery();
	
	private NullQuery() {
		// Deliberately empty
	}
	
    public void setName(String name) {
    	// Deliberately empty
    }

    public String getName() {
    	return "null";
    }

    public void setDisplayName(String displayName) {
    	// Deliberately empty
    }

    public String getDisplayName() {
    	return "NullQuery";
    }

    public void setIsCacheable(Boolean isCacheable) {
    	// Deliberately empty
    }

    public Boolean getIsCacheable() {
    	return Boolean.TRUE;
    }

    public void setHelp(String help) {
    	// Deliberately empty
    }

    public String getHelp() {
    	return "NullQuery";
    }

    public void addParam(Param param) {
    	// Deliberately empty
    }

    public Param[] getParams() {
    	return new Param[0];
    }
    
	public void addColumn(Column column) {
    	// Deliberately empty		
	}

	public Column[] getColumns() {
		return new Column[0];
	}

	public Column getColumn(String columnName) {
		return null;
	}

    /* (non-Javadoc)
     * @see org.gusdb.gus.wdk.model.Query#makeInstance()
     */
    public QueryInstance makeInstance() {
        // TODO Auto-generated method stub
        return NullQueryInstance.INSTANCE;
    }

}
