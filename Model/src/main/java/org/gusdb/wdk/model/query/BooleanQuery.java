package org.gusdb.wdk.model.query;

import java.util.List;
import java.util.Optional;

import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.query.param.AbstractEnumParam;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.EnumItem;
import org.gusdb.wdk.model.query.param.EnumItemList;
import org.gusdb.wdk.model.query.param.EnumParam;
import org.gusdb.wdk.model.query.param.ParamSet;
import org.gusdb.wdk.model.query.param.RecordClassReference;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpecBuilder;
import org.gusdb.wdk.model.record.RecordClass;
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
 * intersect, union, left minus right, and right minus left.
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

  public static final String OPERATOR_PARAM = "bq_operator";

  public static String getQueryName(RecordClass recordClass) {
    String rcName = recordClass.getFullName().replace('.', '_');
    return QUERY_NAME_PREFIX + rcName;
  }

  private AnswerParam _leftOperand;
  private AnswerParam _rightOperand;
  private EnumParam _operator;
  private RecordClass _recordClass;

  public BooleanQuery(RecordClass recordClass) throws WdkModelException {
    setRecordClass(recordClass);
  }

 /**
  * Need a no-arg constructor for easy construction using newInstance()
  * FIXME: why? where are we using newInstance()?
  * @throws WdkModelException
  */
  public BooleanQuery() throws WdkModelException {
  }

  public void setRecordClass(RecordClass recordClass) throws WdkModelException {
    this._recordClass = recordClass;
    this._wdkModel = recordClass.getWdkModel();
    String rcName = recordClass.getFullName().replace('.', '_');

    // create or get the historyParam for the query
    ParamSet internalParamSet = _wdkModel.getParamSet(Utilities.INTERNAL_PARAM_SET);
    _leftOperand = prepareOperand(internalParamSet, recordClass,
        LEFT_OPERAND_PARAM_PREFIX + rcName, "Left Operand");
    _rightOperand = prepareOperand(internalParamSet, recordClass,
        RIGHT_OPERAND_PARAM_PREFIX + rcName, "Right Operand");

    // create the stringParam for the others
    _operator = prepareOperatorParam(internalParamSet, OPERATOR_PARAM);
    _operator.setPrompt("Operator");

    // create the query
    this.setName(getQueryName(recordClass));
    this.addParam(_leftOperand);
    this.addParam(_rightOperand);
    this.addParam(_operator);

    prepareColumns(recordClass);

    this.setSql("don't care");  // the boolean query instance will not use sql set at the query level
  }

  protected BooleanQuery(BooleanQuery query) {
    super(query);

    this._recordClass = query._recordClass;
    this._leftOperand = (AnswerParam) paramMap.get(query._leftOperand.getName());
    this._operator = (EnumParam) paramMap.get(query._operator.getName());
    this._rightOperand = (AnswerParam) paramMap.get(query._rightOperand.getName());
  }

  /**
   * @return the recordClass
   */
  public RecordClass getRecordClass() {
    return _recordClass;
  }

  /**
   * @return the leftOperand
   */
  public AnswerParam getLeftOperandParam() {
    return _leftOperand;
  }

  /**
   * @return the rightOperand
   */
  public AnswerParam getRightOperandParam() {
    return _rightOperand;
  }

  /**
   * @return the operator
   */
  public EnumParam getOperatorParam() {
    return _operator;
  }

  private AnswerParam prepareOperand(ParamSet paramSet,
      RecordClass recordClass, String paramName, String prompt) throws WdkModelException {
    AnswerParam operand;
    if (paramSet.contains(paramName)) {
      operand = (AnswerParam) paramSet.getParam(paramName);
    } else {
      operand = new AnswerParam();
      operand.setName(paramName);
      String rcName = recordClass.getFullName();
      // False is the default so this is unneeded BUT a good reminder that you, future developer, cannot
      //   change the value to true without altering the name of the answer params since the generated names
      //   (with recordclass full name suffix) is too long to be an Oracle column.  If we want this feature
      //   for WDK set booleans, we have to shorten the param names, which costs us a DB migration.
      operand.setExposeAsAttribute(false);
      operand.addRecordClassRef(new RecordClassReference(rcName));
      paramSet.addParam(operand);
      operand.excludeResources(_wdkModel.getProjectId());
      operand.resolveReferences(_wdkModel);
      operand.setPrompt(prompt);
    }
    return operand;
  }

  private EnumParam prepareOperatorParam(ParamSet paramSet, String paramName)
      throws WdkModelException {
    EnumParam param;
    if (paramSet.contains(paramName)) {
      param = (EnumParam) paramSet.getParam(paramName);
    }
    else {
      param = new EnumParam();
      param.setName(paramName);
      param.setNoTranslation(true);
      param.setDefault(BooleanOperator.INTERSECT.name());
      EnumItemList items = new EnumItemList();
      for (BooleanOperator value : BooleanOperator.values()) {
        EnumItem item = new EnumItem();
        item.setDisplay(value.name());
        item.setTerm(value.getBaseOperator());
        item.setInternal(value.getBaseOperator());
        items.addEnumItem(item);
      }
      param.addEnumItemList(items);
      param.excludeResources(_wdkModel.getProjectId());
      param.resolveReferences(_wdkModel);
      paramSet.addParam(param);
    }
    return param;
  }

  protected void prepareColumns(RecordClass recordClass) {
    for (String columnName : recordClass.getPrimaryKeyDefinition().getColumnRefs()) {
      Column column = new Column();
      column.setName(columnName);
      column.setQuery(this);
      _columnMap.put(columnName, column);
    }
  }

  @Override
  protected void appendChecksumJSON(JSONObject jsQuery, boolean extra)
      throws JSONException {
    super.appendChecksumJSON(jsQuery, extra);
    jsQuery.append("recordClass", _recordClass.getFullName());
  }

  @Override
  public BooleanQuery clone() {
    return new BooleanQuery(this);
  }

  @Override
  protected BooleanQueryInstance makeInstance(RunnableObj<QueryInstanceSpec> spec) {
    return new BooleanQueryInstance(spec);
  }

  @Override
  public boolean isBoolean() {
    return true;
  }

  /**
   * Pulls the current stable value of a boolean query operator param out of the
   * passed query instance spec builder and converts it to an operator object
   * 
   * @param specBuilder
   * @param stepId optional step ID used to add information when logging a problem
   * @return
   * @throws WdkModelException
   */
  public static BooleanOperator getOperator(QueryInstanceSpecBuilder specBuilder, Optional<Long> stepId) {
    return getOperator(specBuilder.get(BooleanQuery.OPERATOR_PARAM), stepId);
  }

  /**
   * Pulls the current stable value of a boolean query operator param out of the
   * passed query instance spec and converts it to an operator object
   * 
   * @param spec
   * @param stepId optional step ID used to add information when logging a problem
   * @return
   */
  public static BooleanOperator getOperator(QueryInstanceSpec spec, Optional<Long> stepId) {
    return getOperator(spec.get(BooleanQuery.OPERATOR_PARAM), stepId);
  }

  private static BooleanOperator getOperator(String stableValue, Optional<Long> stepId) {
    String errorPrefix = "Found transcript boolen step " + stepId.map(id -> id + " ").orElse("");
    if (stableValue == null) {
      throw new WdkRuntimeException(errorPrefix + "without " + BooleanQuery.OPERATOR_PARAM + " parameter value.");
    }
    List<String> values = AbstractEnumParam.convertToTerms(stableValue);
    if (values.isEmpty()|| values.size() > 1) {
      throw new WdkRuntimeException(errorPrefix + "with zero or >1 values: " + stableValue);
    }
    return BooleanOperator.parse(values.get(0));
  }
}
