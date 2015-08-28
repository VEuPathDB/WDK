package org.gusdb.wdk.model.record;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;

public interface ResultSize {

  /**
   * Returns a custom display result size for this answer value.  Uses native
   * ID SQL from AnswerValue.getIdSql() to get count.
   * 
   * @param answerValue answer value from which to get a count
   * @return custom record count
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public Integer getResultSize(AnswerValue answerValue) throws WdkModelException, WdkUserException;

  /**
   * Returns a custom display result size for this answer value, but uses the
   * ID SQL passed in instead of getting it directly from the AnswerValue.
   * 
   * @param answerValue answer value from which to get a count
   * @param idSql customized ID SQL
   * @return custom record count
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public Integer getResultSize(AnswerValue answerValue, String idSql) throws WdkModelException, WdkUserException;
}
