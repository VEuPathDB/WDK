package org.gusdb.gus.wdk.model.implementation;

import java.util.HashMap;
import java.sql.SQLException;
import java.sql.ResultSet;

import org.gusdb.gus.wdk.model.PageableQueryInstanceI;

public class PageableSqlQueryInstance extends SimpleSqlQueryInstance implements PageableQueryInstanceI {

    SimpleSqlQuery pageQuery;

    protected PageableSqlQueryInstance(SimpleSqlQuery query, 
				       SimpleSqlQuery pageQuery) {
	super(query);
	this.pageQuery = pageQuery;
    }

    public ResultSet getResult(int startRow, int endRow) throws Exception {
	String initialResultTable = getResultAsTable();
	HashMap values = new HashMap(3);
	values.put(PageableSqlQuery.RESULT_TABLE_SYMBOL, initialResultTable);
	values.put(PageableSqlQuery.START_ROW_SYMBOL, 
		   Integer.toString(startRow));
	values.put(PageableSqlQuery.END_ROW_SYMBOL, 
		   Integer.toString(endRow));
	SimpleSqlQueryInstance pageInstance = 
	    (SimpleSqlQueryInstance)pageQuery.makeInstance();
	pageInstance.setIsCacheable(getIsCacheable());
	pageInstance.setValues(values);
	return pageInstance.getResult();
    }

    public int getResultSize() throws Exception {
	return 0;
    }

}
