package org.gusdb.wdk.model.record;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.query.SqlQuery;

public class DefaultResultSizePlugin implements ResultSize {

  private static final Logger LOG = Logger.getLogger(DefaultResultSizePlugin.class);
  
  @Override
  public Integer getResultSize(AnswerValue answerValue) throws WdkModelException {
    return getResultSize(answerValue, answerValue.getIdSql(), answerValue.getQuestion().getFullName() + "__count");
  }

  @Override
  public Integer getResultSize(AnswerValue answerValue, String idSql) throws WdkModelException {
    return getResultSize(answerValue, idSql, answerValue.getIdsQueryInstance().getQuery().getFullName() + "__" + "filter-size");
  }

  private Integer getResultSize(AnswerValue answerValue, String idSql, String countQueryName) throws WdkModelException {
    DataSource dataSource = answerValue.getWdkModel().getAppDb().getDataSource();
    try {
      if (idSql.contains(SqlQuery.PARTITION_KEYS_MACRO))
        idSql = idSql.replaceAll(SqlQuery.PARTITION_KEYS_MACRO,
            answerValue.getPartitionKeysString("DefaultResultSizePlugin"));

      LOG.debug("Executing filter size count for " + countQueryName + " using idSql:\n" + idSql);
      String countSql = new StringBuilder("SELECT count(*) FROM (").append(idSql).append(") ids").toString();
      Object count = SqlUtils.executeScalar(dataSource, countSql, countQueryName);
      return Integer.valueOf(count.toString());
    }
    catch (SQLException ex) {
      throw new WdkModelException(ex);
    }
  }
}
