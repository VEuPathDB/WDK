package org.gusdb.wdk.model.query;

import java.util.Map;

import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.ParamSet;
import org.gusdb.wdk.model.query.param.RecordClassReference;
import org.gusdb.wdk.model.query.param.StringParam;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.attribute.PrimaryKeyAttributeField;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A boolean query is a SqlQuery created internally by WDK, and one boolean
 * query will be created for each recordClass. The boolean query represents the
 * operation of combining two steps by booleaning the results in the strategy
 * system.
 * 
 * A boolean query has three important params, left operand, right operand, and
 * operator. The left and right operands are AnswerParams, which take a step as
 * input, and the operator is a StringParam which represents the boolean
 * operation to be performed on the input operands. The left and right operands
 * have to be of the same recordClass type, and the result of the boolean will
 * be that same type as well. Currently, we support four operations on boolean:
 * interset, union, left minus right, and right minus left.
 * 
 * The weight for different operators are computed differently. In intersect and
 * Union, the weight is the sum of the same primary keys; in the case of minus,
 * the weight of the remaining records stay unchanged.
 * 
 * If the useBooleanFilter is set to true, a boolean filter defined in the
 * RecordClass will be used to filter against both operands, and then the
 * results will be used in the boolean operation. This feature is used to allow
 * the boolean two records that represent the same conceptual entity, but might
 * have different primary keys. The result of the boolean will have the shared
 * the identities of those records.
 * 
 * Created: Fri May 21 1821:30 EDT 2004
 * 
 * @author David Barkan
 * @version $Revision$ $Date: 2007-05-30 14:07:48 -0400 (Wed, 30 May
 *          2007) $ $Author$
 */

public class BooleanQuery extends SqlQuery {

  public static final String QUERY_NAME_PREFIX = "bq_";
  public static final String LEFT_OPERAND_PARAM_PREFIX = "bq_left_op_";
  public static final String RIGHT_OPERAND_PARAM_PREFIX = "bq_right_op_";
  public static final String USE_BOOLEAN_FILTER_PARAM = "use_boolean_filter";

  public static final String OPERATOR_PARAM = "bq_operator";

  public static String getQueryName(RecordClass recordClass) {
    String rcName = recordClass.getFullName().replace('.', '_');
    return QUERY_NAME_PREFIX + rcName;
  }

  private AnswerParam leftOperand;
  private AnswerParam rightOperand;
  private StringParam operator;
  private StringParam useBooleanFilter;
  private RecordClass recordClass;

  public BooleanQuery(RecordClass recordClass) throws WdkModelException {
    this.recordClass = recordClass;
    this.wdkModel = recordClass.getWdkModel();
    String rcName = recordClass.getFullName().replace('.', '_');

    // create or get the historyParam for the query
    ParamSet internalParamSet = wdkModel.getParamSet(Utilities.INTERNAL_PARAM_SET);
    leftOperand = prepareOperand(internalParamSet, recordClass,
        LEFT_OPERAND_PARAM_PREFIX + rcName);
    leftOperand.setPrompt("Left Operand");
    rightOperand = prepareOperand(internalParamSet, recordClass,
        RIGHT_OPERAND_PARAM_PREFIX + rcName);
    rightOperand.setPrompt("Right Operand");

    // create the stringParam for the others
    operator = prepareStringParam(internalParamSet, OPERATOR_PARAM);
    operator.setPrompt("Operator");
    useBooleanFilter = prepareStringParam(internalParamSet,
        USE_BOOLEAN_FILTER_PARAM);
    useBooleanFilter.setPrompt("Use Expand Filter");

    // create the query
    this.setName(BooleanQuery.getQueryName(recordClass));
    this.addParam(leftOperand);
    this.addParam(rightOperand);
    this.addParam(operator);
    this.addParam(useBooleanFilter);

    prepareColumns(recordClass);

    this.setSql(constructSql());
  }

  private BooleanQuery(BooleanQuery query) {
    super(query);

    this.recordClass = query.recordClass;
    this.leftOperand = (AnswerParam) paramMap.get(query.leftOperand.getName());
    this.operator = (StringParam) paramMap.get(query.operator.getName());
    this.rightOperand = (AnswerParam) paramMap.get(query.rightOperand.getName());
    this.useBooleanFilter = (StringParam) paramMap.get(query.useBooleanFilter.getName());
  }

  /**
   * @return the recordClass
   */
  public RecordClass getRecordClass() {
    return recordClass;
  }

  /**
   * @return the leftOperand
   */
  public AnswerParam getLeftOperandParam() {
    return leftOperand;
  }

  /**
   * @return the rightOperand
   */
  public AnswerParam getRightOperandParam() {
    return rightOperand;
  }

  /**
   * @return the operator
   */
  public StringParam getOperatorParam() {
    return operator;
  }

  /**
   * @return the useBooleanFilter
   */
  public StringParam getUseBooleanFilter() {
    return useBooleanFilter;
  }

  private AnswerParam prepareOperand(ParamSet paramSet,
      RecordClass recordClass, String paramName) throws WdkModelException {
    AnswerParam operand;
    if (paramSet.contains(paramName)) {
      operand = (AnswerParam) paramSet.getParam(paramName);
    } else {
      operand = new AnswerParam();
      operand.setName(paramName);
      String rcName = recordClass.getFullName();
      operand.addRecordClassRef(new RecordClassReference(rcName));
      paramSet.addParam(operand);
      operand.resolveReferences(wdkModel);
      operand.setResources(wdkModel);
    }
    return operand;
  }

  private StringParam prepareStringParam(ParamSet paramSet, String paramName)
      throws WdkModelException {
    StringParam param;
    if (paramSet.contains(paramName)) {
      param = (StringParam) paramSet.getParam(paramName);
    } else {
      param = new StringParam();
      param.setName(paramName);
      param.setNumber(false);
      param.setNoTranslation(true);
      param.resolveReferences(wdkModel);
      param.setResources(wdkModel);
      paramSet.addParam(param);
    }
    return param;
  }

  private void prepareColumns(RecordClass recordClass) {
    PrimaryKeyAttributeField primaryKey = recordClass.getPrimaryKeyAttributeField();

    for (String columnName : primaryKey.getColumnRefs()) {
      Column column = new Column();
      column.setName(columnName);
      column.setQuery(this);
      columnMap.put(columnName, column);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.query.Query#appendJSONContent(org.json.JSONObject)
   */
  @Override
  protected void appendJSONContent(JSONObject jsQuery, boolean extra)
      throws JSONException {
    super.appendJSONContent(jsQuery, extra);
    jsQuery.append("recordClass", recordClass.getFullName());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.query.Query#clone()
   */
  @Override
  public Query clone() {
    return new BooleanQuery(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.query.Query#makeInstance()
   */
  @Override
  public QueryInstance makeInstance(User user, Map<String, String> values,
      boolean validate, int assignedWeight, Map<String, String> context)
      throws WdkModelException, WdkUserException {
    return new BooleanQueryInstance(user, this, values, validate,
        assignedWeight, context);
  }

  private String constructSql() {
    StringBuffer sql = new StringBuffer();
    constructOperandSql(sql, leftOperand.getName());
    sql.append(" $$").append(operator.getName()).append("$$ ");
    constructOperandSql(sql, leftOperand.getName());
    return sql.toString();
  }

  private void constructOperandSql(StringBuffer sql, String operand) {
    sql.append("SELECT ");
    boolean first = true;
    for (String column : columnMap.keySet()) {
      if (first) first = false;
      else sql.append(", ");
      sql.append(column);
    }
    sql.append(" FROM $$").append(operand).append("$$");
    sql.append(" WHERE $$").append(operand).append(".condition$$");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.query.Query#isBoolean()
   */
  @Override
  public boolean isBoolean() {
    return true;
  }

}
