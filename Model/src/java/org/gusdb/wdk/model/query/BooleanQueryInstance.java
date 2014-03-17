package org.gusdb.wdk.model.query;

import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerFilterInstance;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.StringParam;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.user.User;

/**
 * BooleanQueryInstance.java
 * 
 * Instance instantiated by a BooleanQuery. Takes Answers as values for its
 * parameters along with a boolean operation, and returns a result.
 * 
 * Created: Wed May 19 15:11:30 2004
 * 
 * @author David Barkan
 * @version $Revision$ $Date: 2005-08-09 22:24:36 -0400 (Tue, 09 Aug
 *          2005) $ $Author$
 */
public class BooleanQueryInstance extends SqlQueryInstance {

  private static final Logger logger = Logger.getLogger(BooleanQueryInstance.class);

  private BooleanQuery booleanQuery;

  /**
   * @param query
   * @param values
   * @throws WdkUserException
   */
  protected BooleanQueryInstance(User user, BooleanQuery query,
      Map<String, String> values, boolean validate, int assignedWeight,
      Map<String, String> context) throws WdkModelException, WdkUserException {
    // boolean query doesn't use assigned weight
    super(user, query, values, validate, assignedWeight, context);
    this.booleanQuery = query;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.query.SqlQueryInstance#getUncachedSql()
   */
  @Override
  public String getUncachedSql() throws WdkModelException {

    // needs to apply the view to each operand before boolean
    // get the use_boolean filter param
    boolean booleanFlag = isUseBooleanFilter();

    logger.info("Boolean expansion flag: " + booleanFlag);

    Map<String, String> InternalValues = getParamInternalValues();

    // parse operator
    String operator = InternalValues.get(booleanQuery.getOperatorParam().getName());
    BooleanOperator op = BooleanOperator.parse(operator);
    DBPlatform platform = wdkModel.getAppDb().getPlatform();
    operator = op.getOperator(platform);

    // construct the filter query for the first child
    AnswerParam leftParam = booleanQuery.getLeftOperandParam();
    String leftSubSql = InternalValues.get(leftParam.getName());
    String leftSql = constructOperandSql(leftSubSql, booleanFlag);

    AnswerParam rightParam = booleanQuery.getRightOperandParam();
    String rightSubSql = InternalValues.get(rightParam.getName());
    String rightSql = constructOperandSql(rightSubSql, booleanFlag);

    String sql;
    if (op == BooleanOperator.UNION) {
      sql = getUnionSql(leftSql, rightSql, operator);
    } else if (op == BooleanOperator.INTERSECT) {
      // the union query is reused, and having count(*) > 1 is appended to
      // last group by to get intersect results. the unioned sql has to
      // have
      // group by as the last clause.
      sql = getIntersectSql(leftSql, rightSql,
          BooleanOperator.UNION.getOperator(platform));
    } else {
      // swap sqls if it is right_minus
      if (op == BooleanOperator.RIGHT_MINUS) {
        String tempSql = leftSql;
        leftSql = rightSql;
        rightSql = tempSql;
      }
      sql = GetOtherOperationSql(leftSql, rightSql, operator);
    }
    logger.debug("boolean sql:\n" + sql);
    return sql;
  }

  private String constructOperandSql(String subSql, boolean booleanFlag)
      throws WdkModelException {
    RecordClass recordClass = booleanQuery.getRecordClass();
    // apply the filter query if needed
    AnswerFilterInstance filter = recordClass.getBooleanExpansionFilter();
    if (booleanFlag && filter != null) {
      // here the assigned weight comes from the boolean query itself,
      // since the filter is the boolean extension filter.
      subSql = filter.applyFilter(user, subSql, assignedWeight);
    }

    // limit the column output
    StringBuffer sql = new StringBuffer();

    // fix the returned columns to be the pk columns, plus weight column
    sql.append("SELECT " + Utilities.COLUMN_WEIGHT);
    RecordClass rc = booleanQuery.getRecordClass();
    String[] pkColumns = rc.getPrimaryKeyAttributeField().getColumnRefs();
    for (String column : pkColumns) {
      sql.append(", " + column);
    }
    sql.append(" FROM (").append(subSql).append(") f");
    return sql.toString();
  }

  public boolean isUseBooleanFilter() {
    StringParam useBooleanFilter = booleanQuery.getUseBooleanFilter();
    String strBooleanFlag = stableValues.get(useBooleanFilter.getName());
    return Boolean.parseBoolean(strBooleanFlag);
  }

  private String getUnionSql(String leftSql, String rightSql, String operator) {
    // just sum the weight from original sql
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT ");

    RecordClass rc = booleanQuery.getRecordClass();
    String[] pkColumns = rc.getPrimaryKeyAttributeField().getColumnRefs();
    for (String column : pkColumns) {
      sql.append(column + ", ");
    }
    // weight has to be the last column to ensure the values are inserted
    // correctly
    String weightColumn = Utilities.COLUMN_WEIGHT;
    sql.append("sum (" + weightColumn + ") AS " + weightColumn);
    sql.append(" FROM (");
    sql.append("(SELECT 1 AS wdk_t, l.* FROM (" + leftSql + ") l) ");
    sql.append(operator);
    sql.append(" (SELECT 2 AS wdk_t, r.* FROM (" + rightSql + ") r)");
    sql.append(") t GROUP BY ");
    for (int i = 0; i < pkColumns.length; i++) {
      sql.append((i == 0) ? "" : ",");
      sql.append(pkColumns[i]);
    }
    return sql.toString();
  }

  private String getIntersectSql(String leftSql, String rightSql,
      String operator) {
    // just sum the weight from original sql
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT ");

    RecordClass rc = booleanQuery.getRecordClass();
    String[] pkColumns = rc.getPrimaryKeyAttributeField().getColumnRefs();
    for (String column : pkColumns) {
      sql.append(column + ", ");
    }
    // weight has to be the last column to ensure the values are inserted
    // correctly
    String weightColumn = Utilities.COLUMN_WEIGHT;
    sql.append("sum (" + weightColumn + ") AS " + weightColumn);
    sql.append(" FROM (");
    sql.append("(SELECT 1 AS wdk_t, l.* FROM (" + leftSql + ") l) ");
    sql.append(operator);
    sql.append(" (SELECT 2 AS wdk_t, r.* FROM (" + rightSql + ") r)");
    sql.append(") t GROUP BY ");
    for (int i = 0; i < pkColumns.length; i++) {
      sql.append((i == 0) ? "" : ",");
      sql.append(pkColumns[i]);
    }
    sql.append(" HAVING count(*) > 1");
    return sql.toString();
  }

  private String GetOtherOperationSql(String leftSql, String rightSql,
      String operator) {
    RecordClass rc = booleanQuery.getRecordClass();
    String[] pkColumns = rc.getPrimaryKeyAttributeField().getColumnRefs();

    // first do boolean operation on primary keys only
    StringBuffer leftPiece = new StringBuffer();
    StringBuffer rightPiece = new StringBuffer();
    StringBuffer sql = new StringBuffer("SELECT ");
    for (String column : pkColumns) {
      leftPiece.append((leftPiece.length() == 0) ? "SELECT " : ", ");
      leftPiece.append(column);
      rightPiece.append((rightPiece.length() == 0) ? "SELECT " : ", ");
      rightPiece.append(column);
      sql.append("o." + column + ", ");
    }
    leftPiece.append(" FROM (" + leftSql + ") ls ");
    rightPiece.append(" FROM (" + rightSql + ") rs ");
    // get extra columns from the left sql; the column weight has to be the
    // last column to ensure the data are correctly inserted.
    sql.append("o." + Utilities.COLUMN_WEIGHT + " FROM ");
    sql.append("(" + leftSql + ") o, (");
    sql.append("(" + leftPiece + ") " + operator + "(" + rightPiece + ")");
    sql.append(") b WHERE ");
    for (int i = 0; i < pkColumns.length; i++) {
      if (i > 0) sql.append(" AND ");
      sql.append("o." + pkColumns[i] + " = b." + pkColumns[i]);
    }
    return sql.toString();
  }
}
