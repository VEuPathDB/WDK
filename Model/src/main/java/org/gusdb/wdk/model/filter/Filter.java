package org.gusdb.wdk.model.filter;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;

public interface Filter {

  FilterSummary getSummary(AnswerValue answer) throws WdkModelException, WdkUserException;
  
  String getSql(AnswerValue answer, String idSql, String options) throws WdkModelException, WdkUserException;
}
