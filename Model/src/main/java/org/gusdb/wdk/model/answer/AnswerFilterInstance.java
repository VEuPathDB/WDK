package org.gusdb.wdk.model.answer;

import static org.gusdb.fgputil.FormatUtil.NL;
import static org.gusdb.fgputil.FormatUtil.join;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.gusdb.fgputil.Named.NamedObject;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.dbms.SqlResultList;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.query.SqlQuery;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.spec.ParameterContainerInstanceSpecBuilder.FillStrategy;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpecBuilder;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.StepContainer;
import org.gusdb.wdk.model.user.User;

/**
 * An object representation of {@code <answerFilter>/<instance>}; object. This
 * object hold the values to the {@link Param}s of the {@link Query} in its
 * parent {@link AnswerFilter} (all param values except the {@link AnswerParam},
 * which represents the current {@link Step} the filter is applied on).
 * 
 * @author xingao
 * 
 */
public class AnswerFilterInstance extends WdkModelBase implements NamedObject {

  private String _name;
  private boolean _isDefault;

  /**
   * A record class can have at most one booleanExpansion filter, and this
   * filter will be used in the boolean query to convert input steps to make the
   * operand results to be able to do boolean.
   */
  private boolean _isBooleanExpansion;

  private List<WdkModelText> _displayNameList = new ArrayList<WdkModelText>();
  private String _displayName;

  private List<WdkModelText> _descriptionList = new ArrayList<WdkModelText>();
  private String _description;

  private List<WdkModelText> _paramValueList = new ArrayList<WdkModelText>();
  private QueryInstanceSpecBuilder _partialParamStableValuesBuilder; // omits answer param for step input to this filter

  private RecordClass _recordClass;
  private SqlQuery _filterQuery;
  private AnswerParam _answerParam;

  /**
   * @return the name
   */
  @Override
  public String getName() {
    return _name;
  }

  /**
   * @param name
   *          the name of the instance has be to unique in the recordClass.
   */
  public void setName(String name) {
    _name = name;
  }

  /**
   * @return the displayName
   */
  public String getDisplayName() {
    return _displayName;
  }

  /**
   * @param displayName
   *          the displayName to set
   */
  public void setDisplayName(String displayName) {
    _displayName = displayName;
  }

  public void addDisplayName(WdkModelText displayName) {
    _displayNameList.add(displayName);
  }

  /**
   * @return the isDefault
   */
  public boolean isDefault() {
    return _isDefault;
  }

  /**
   * @param isDefault
   *          the isDefault to set
   */
  public void setDefault(boolean isDefault) {
    _isDefault = isDefault;
  }

  /**
   * @return the isBooleanExpansion
   */
  public boolean isBooleanExpansion() {
    return _isBooleanExpansion;
  }

  /**
   * @param isBooleanExpansion
   *          the isBooleanExpansion to set
   */
  public void setBooleanExpansion(boolean isBooleanExpansion) {
    _isBooleanExpansion = isBooleanExpansion;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return _description;
  }

  public void addDescription(WdkModelText description) {
    _descriptionList.add(description);
  }

  public void addParamValue(WdkModelText param) {
    _paramValueList.add(param);
  }

  /**
   * @return the recordClass
   */
  public RecordClass getRecordClass() {
    return _recordClass;
  }

  /**
   * @param recordClass
   *          the recordClass to set
   */
  void setRecordClass(RecordClass recordClass) {
    _recordClass = recordClass;
  }

  /**
   * @return the filterQuery
   */
  public SqlQuery getFilterQuery() {
    return _filterQuery;
  }

  /**
   * @param filterQuery
   *          the filterQuery to set
   */
  void setFilterQuery(SqlQuery filterQuery) {
    _filterQuery = filterQuery;
  }

  /**
   * @return the answerParam
   */
  public AnswerParam getAnswerParam() {
    return _answerParam;
  }

  /**
   * @param answerParam
   *          the answerParam to set
   */
  void setAnswerParam(AnswerParam answerParam) {
    _answerParam = answerParam;
  }

  public Map<String, String> getParamValueMap() {
    return new LinkedHashMap<>(_partialParamStableValuesBuilder.toMap());
  }

  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    // exclude the display names
    for (WdkModelText text : _displayNameList) {
      if (text.include(projectId)) {
        text.excludeResources(projectId);
        if (_displayName != null)
          throw new WdkModelException("Display Name of "
              + "answerFilterInstance '" + _name + "' in "
              + _recordClass.getFullName() + " is included more than once.");
        _displayName = text.getText();
      }
    }
    _displayNameList = null;

    // exclude the descriptions
    for (WdkModelText text : _descriptionList) {
      if (text.include(projectId)) {
        text.excludeResources(projectId);
        if (_description != null)
          throw new WdkModelException("Description of "
              + "answerFilterInstance '" + _name + "' in "
              + _recordClass.getFullName() + " is included more than once.");
        _description = text.getText();
      }
    }
    _descriptionList = null;

    // exclude the param values
    _partialParamStableValuesBuilder = QueryInstanceSpec.builder();
    for (WdkModelText param : _paramValueList) {
      if (param.include(projectId)) {
        param.excludeResources(projectId);
        String paramName = param.getName();
        String paramValue = param.getText().trim();
 
        if (_partialParamStableValuesBuilder.containsKey(paramName))
          throw new WdkModelException("The param [" + paramName
              + "] for answerFilterInstance [" + _name + "] of type "
              + _recordClass.getFullName() + "  is included more than once.");
        _partialParamStableValuesBuilder.put(paramName, paramValue);
      }
    }
    _paramValueList = null;
  }

  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    if (_resolved)
      return;

    _wdkModel = wdkModel;

    // make sure the params provides match with those in the filter query
    Map<String, Param> params = _filterQuery.getParamMap();
    for (String paramName : _partialParamStableValuesBuilder.toMap().keySet()) {
      if (!params.containsKey(paramName))
        throw new WdkModelException("The param [" + paramName
            + "] declared in answerFilterInstance [" + _name + "] of type "
            + _recordClass.getFullName()
            + " does not exist in the filter query ["
            + _filterQuery.getFullName() + "]");
    }
    // User user = wdkModel.getSystemUser();
    // make sure the required param is defined
    for (String paramName : params.keySet()) {
      if (_answerParam.getName().equals(paramName))
        continue;
      if (!_partialParamStableValuesBuilder.containsKey(paramName))
        throw new WdkModelException("The required param value of [" + paramName
            + "] is not assigned to filter [" + getName() + "]");
    }

    _resolved = true;
  }

  public ResultList getResults(AnswerValue answerValue) throws WdkModelException {
    // use only the id query sql as input
    QueryInstance<?> idInstance = answerValue.getIdsQueryInstance();
    String sql = idInstance.getSql();
    int assignedWeight = idInstance.getAssignedWeight();
    sql = applyFilter(answerValue.getRequestingUser(), sql, assignedWeight);
    DataSource dataSource = _wdkModel.getAppDb().getDataSource();
    try {
      ResultSet resultSet = SqlUtils.executeQuery(dataSource, sql,
          idInstance.getQuery().getFullName() + "__" + _name + "-filtered");
      return new SqlResultList(resultSet);
    } catch (SQLException e) {
      throw new WdkModelException("Could not get answer results.", e);
    }
  }

  public String applyFilter(User user, String sql, int assignedWeight)
      throws WdkModelException {
    // validate params
    RunnableObj<QueryInstanceSpec> validSpec = QueryInstanceSpec
        .builder(_partialParamStableValuesBuilder.buildInvalid(user))
        .buildValidated(user, _filterQuery, StepContainer.emptyContainer(),
            ValidationLevel.RUNNABLE, FillStrategy.NO_FILL)
        .getRunnable()
        .getOrThrow(spec -> new WdkModelException(
            "AnswerFilterInstance params failed semantic validation: " + NL +
            join(spec.getValidationBundle().getAllErrors(), NL)));

    // don't use QueryInstance here because of the way we are injecting the AnswerParam's SQL
    String filterSql = _filterQuery.getSql();

    // populate filter SQL with param values
    for (Param param : _filterQuery.getParams()) {
      String internal = param.getFullName().equals(_answerParam.getFullName()) ?
          "(" + sql + ")" : param.getInternalValue(validSpec);
      filterSql = param.replaceSql(filterSql, validSpec.get().get(param.getName()), internal);
    }

    // if the filter doesn't return weight, assigned weight will be used
    if (!_filterQuery.getColumnMap().containsKey(Utilities.COLUMN_WEIGHT)) {
      filterSql = "SELECT f.*, " + assignedWeight + " AS "
          + Utilities.COLUMN_WEIGHT + " FROM (" + filterSql + ") f";
    }

    return filterSql;
  }
}
