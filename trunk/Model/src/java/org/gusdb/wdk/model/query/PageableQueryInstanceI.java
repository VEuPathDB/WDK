package org.gusdb.gus.wdk.model.query;

import java.sql.ResultSet;

public interface PageableQueryInstanceI extends SimpleQueryInstanceI {

    public ResultSet getResult(int startRow, int endRow) throws Exception;

    public int getResultSize() throws Exception;
}
