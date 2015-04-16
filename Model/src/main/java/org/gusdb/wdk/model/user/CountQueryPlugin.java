package org.gusdb.wdk.model.user;

import java.util.HashMap;
import java.util.Map;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.Param;

public class CountQueryPlugin implements CountPlugin {

  private final Query _query;
  
  @SuppressWarnings("unused")
  private WdkModel _wdkModel;

  /**
   * @param query
   *          the query should have only one param, and it must be AnswerParam. the query should return only
   *          one column of int type.
   * @throws WdkModelException
   */
  public CountQueryPlugin(Query query) throws WdkModelException {
    // need to validate the query
    Param[] params = query.getParams();
    if (params.length != 1 || !(params[0] instanceof AnswerParam))
      throw new WdkModelException("Invalid count query: " + query.getFullName() +
          ". The query should have only one param, and it must be an AnswerParam.");

    // can only validate column count, but cant validate column type.
    Column[] columns = query.getColumns();
    if (columns.length != 1)
      throw new WdkModelException("Invalid count query: " + query.getFullName() +
          ". The query should have only one return column, and it must be a number.");

    this._query = query;
  }
  
  

  @Override
  public void setModel(WdkModel wdkModel) {
    this._wdkModel = wdkModel;
  }



  @Override
  public int count(Step step) throws WdkModelException, WdkUserException {
    // prepare params, which has only one answerParam
    Param[] params = _query.getParams();
    Map<String, String> paramValues = new HashMap<>();
    paramValues.put(params[0].getName(), Integer.toString(step.getStepId()));

    // create a queryInstance, and get count;
    QueryInstance<?> queryInstance = _query.makeInstance(step.getUser(), paramValues, false, 0, paramValues);
    try (ResultList resultList = queryInstance.getResults()) {

      // verify the result
      int count;
      if (resultList.next()) {
        Column[] columns = _query.getColumns();
        count = Integer.valueOf(resultList.get(columns[0].getName()).toString());
      }
      else
        throw new WdkModelException("The count query " + _query.getFullName() + " didn't return any row.");

      if (resultList.next())
        throw new WdkModelException("The count query " + _query.getFullName() + " returns more than one row.");

      return count;
    }
  }

}
