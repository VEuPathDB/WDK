package org.gusdb.wdk.model.record;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;

public class DefaultResultSizePlugin implements ResultSize {

  private static final Logger LOG = Logger.getLogger(DefaultResultSizePlugin.class);
  
  @Override
  public Integer getResultSize(AnswerValue answerValue)
      throws WdkModelException, WdkUserException {
    return getResultSize(answerValue, answerValue.getIdSql(), answerValue.getQuestion().getFullName() + "__count");
  }

  @Override
  public Integer getResultSize(AnswerValue answerValue, String idSql)
      throws WdkModelException, WdkUserException {
    return getResultSize(answerValue, idSql, answerValue.getIdsQueryInstance().getQuery().getFullName() + "__" + "filter-size");
  }

  private Integer getResultSize(AnswerValue answerValue, String idSql, String countQueryName) throws WdkModelException {
    DataSource dataSource = answerValue.getQuestion().getWdkModel().getAppDb().getDataSource();
    try {
      LOG.debug("Executing filter size count for " + countQueryName + " using idSql:\n" + idSql);
      String countSql = new StringBuilder("SELECT count(*) FROM (").append(idSql).append(")").toString();
      Object count = SqlUtils.executeScalar(dataSource, countSql, countQueryName);
      return Integer.valueOf(count.toString());
    }
    catch (SQLException ex) {
      throw new WdkModelException(ex);
    }
  }
}
