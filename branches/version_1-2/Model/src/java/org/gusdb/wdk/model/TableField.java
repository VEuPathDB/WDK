package org.gusdb.wdk.model;


public class TableField implements FieldI {

    private Query query;

    public TableField(Query query) {
	this.query = query;
    }

    public String getName() {
	return query.getName();
    }

    public String getDisplayName() {
	return query.getDisplayName();
    }

    public Boolean getIsInternal() {
	return new Boolean(false);
    }
   
    public String getHelp() {
	return query.getHelp();
    }

    public String getType() {
	return null;
    }

    public String toString() {
	return getDisplayName();
    }

    Query getQuery() {
	return query;
    }

    /**
     * Should never be called, but is necessary because TableField implements FieldI.
     */

    public Integer getTruncate(){
	throw new RuntimeException("getTruncate does not apply to TableField");

    }


}

