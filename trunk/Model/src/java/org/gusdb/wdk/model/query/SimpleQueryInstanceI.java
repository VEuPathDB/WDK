package org.gusdb.gus.wdk.model.query;

import java.sql.ResultSet;
import java.util.Collection;
import java.util.Map;

public interface SimpleQueryInstanceI {

    public Collection getValues();

    public boolean getIsCacheable();

    public void setIsCacheable(boolean isCacheable);

    public QueryI getQuery();

    public ResultSet getResult() throws Exception;

    public void setValues(Map values) throws QueryParamsException;
}
