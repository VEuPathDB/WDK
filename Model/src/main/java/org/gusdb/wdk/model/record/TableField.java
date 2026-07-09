package org.gusdb.wdk.model.record;

import static org.gusdb.wdk.model.AttributeMetaQueryHandler.getDynamicallyDefinedAttributes;
import static org.gusdb.wdk.model.answer.single.SingleRecordQuestionParam.PRIMARY_KEY_PARAM_NAME;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.gusdb.fgputil.SortDirection;
import org.gusdb.fgputil.SortDirectionSpec;
import org.gusdb.fgputil.functional.Either;
import org.gusdb.wdk.model.AttributeMetaQueryHandler;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.answer.single.SingleRecordQuestion;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.ProcessQuery;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.SqlQuery;
import org.gusdb.wdk.model.query.param.StringParam;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeFieldContainer;
import org.gusdb.wdk.model.record.attribute.DerivedAttributeField;
import org.gusdb.wdk.model.record.attribute.QueryColumnAttributeField;


/**
 * A table field defines a table of data associated with a recordClass. It defines what attributes the table
 * will have. column, link, and text attributes are allowed in the table field.
 *
 * A table field is linked to a table query that provides the values for the table. Please refer to the Query
 * class for how to define table queries.
 *
 * @author jerric
 */
public class TableField extends Field implements AttributeFieldContainer {

  public class QueryPair {
    public SqlQuery getWrappedQuery() { return _wrappedQuery; }
    public SqlQuery getUnwrappedQuery() { return _unwrappedQuery; }
  }

  private String _queryTwoPartName;
  private String _attributeMetaQueryTwoPartName;
  private SqlQuery _unwrappedQuery;
  private SqlQuery _wrappedQuery;
  private ProcessQuery _processQuery;
  private List<AttributeField> _attributeFieldList = new ArrayList<>();
  private Map<String, AttributeField> _attributeFieldMap = new LinkedHashMap<>();

  private List<WdkModelText> _descriptions = new ArrayList<>();
  private String _description;
  private String _categoryName;
  private String _clientSortingOrderString;
  private List<SortDirectionSpec<AttributeField>> _clientSortingOrderList = new ArrayList<>();

  public static final String SORT_ASCENDING = "ASC";
  public static final String SORT_DESCENDING = "DESC";

  public static final String TABLE_NAME_PARAM_NAME = "tableName";

  private RecordClass _recordClass;

  public void setRecordClass(RecordClass recordClass) {
    _recordClass = recordClass;
  }

  public RecordClass getRecordClass() {
    return _recordClass;
  }

  public Either<QueryPair,ProcessQuery> getQuery() {
    return hasSqlQuery() ? Either.left(new QueryPair()) : Either.right(getProcessQuery());
  }

  private ProcessQuery getProcessQuery() {
    // NOTE: the assignment of the context question must be done after the
    //    single record questions are assigned (which is after resolveReferences())
    //    is called.  Rather than add another step to the model parse process,
    //    this is done here on-the-fly.
    if (_processQuery.getContextQuestion() == null) {
      try {
        // process queries in table values are only allowed for single-record questions
        String singleRecordQuestionName = SingleRecordQuestion.getQuestionName(_recordClass);
        Question singleRecordQuestion = _recordClass.getWdkModel()
            .getQuestionByName(singleRecordQuestionName).orElseThrow();
        _processQuery.setContextQuestion(singleRecordQuestion);
      }
      catch (WdkModelException e) {
        throw new WdkRuntimeException("Error while looking up single value question for table field process query", e);
      }
    }
    return _processQuery;
  }

  public void setQueryRef(String queryRef) {
    _queryTwoPartName = queryRef;
  }

  public String getQueryRef() {
    return _queryTwoPartName;
  }

  public boolean isForSingleRecordOnly() {
    return !hasSqlQuery();
  }

  /**
   * an optional comma delimited list of column names to tell client how to sort this table.
   * each element of the list is of the form "column_name ASC|DESC"
   * this is used typically if sorting in SQL is too expensive
   * @param sortingOrderString
   */
  public void setClientSortingOrder(String sortingOrderString) {
    _clientSortingOrderString = sortingOrderString;
  }

  public String getClientSortingOrder() {
    return _clientSortingOrderString;
  }

  public List<SortDirectionSpec<AttributeField>> getClientSortingOrderList() {
    return Collections.unmodifiableList(_clientSortingOrderList);
  }

  public void setAttributeMetaQueryRef(String attributeMetaQueryRef) {
    _attributeMetaQueryTwoPartName = attributeMetaQueryRef;
  }

  public String getAttributeMetaQueryRef() {
    return _attributeMetaQueryTwoPartName;
  }

  public void addAttributeField(AttributeField attributeField) {
    if (attributeField instanceof DerivedAttributeField) {
      attributeField.setContainer(this);
    }
    _attributeFieldList.add(attributeField);
  }

  public void addDescription(WdkModelText description) {
    _descriptions.add(description);
  }

  /**
   * @return the sortingAttributeMap
   */
  @Override
  public Map<String, Boolean> getSortingAttributeMap() {
    return null;
  }

  @Override
  public Optional<AttributeField> getAttributeField(String key) {
    return Optional.ofNullable(_attributeFieldMap.get(key));
  }

  @Override
  public Map<String, AttributeField> getAttributeFieldMap() {
    return _attributeFieldMap;
  }

  public Map<String, AttributeField> getReporterAttributeFieldMap() {
    return FieldScope.REPORT_MAKER.filter(getAttributeFieldMap());
  }

  public String getDescription() {
    return (_description == null) ? "" : _description;
  }

  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    if (_resolved)
      return;
    super.resolveReferences(wdkModel);

    // resolve Query
    Query query;
    try {
      query = (Query)wdkModel.resolveReference(_queryTwoPartName);
    }
    catch (ClassCastException e) {
      throw new WdkModelException("Reference '" + _queryTwoPartName  +
          "' referenced as a table query in table '" + getName() +
          "' in record class '" + getRecordClass().getFullName() +
          "' is not a Query.");
    }

    // special processing for SQL queries
    if (query instanceof SqlQuery) {

      _unwrappedQuery = (SqlQuery)query;

      // validate the table query
      _recordClass.validateBulkQuery(_unwrappedQuery);

      // prepare the query and add primary key params
      String[] paramNames = _recordClass.getPrimaryKeyDefinition().getColumnRefs();
      _wrappedQuery = RecordClass.prepareQuery(wdkModel, _unwrappedQuery, paramNames);

      // match table query's columns to declared attribute fields (ok to have extra columns)
      Map<String,Column> columns = _wrappedQuery.getColumnMap();
      for (AttributeField field : _attributeFieldMap.values()) {
        field.setContainer(this);
        if (field instanceof QueryColumnAttributeField) {
          Column column = columns.get(field.getName());
          if (column == null) {
            throw new WdkModelException("Table " + _name + " in recordclass " +
                _recordClass.getFullName() + " declares a column attribute '" + field.getName() +
                "' that does not appear in the columns of the referenced table query (" +
                _unwrappedQuery.getFullName() + ").");
          }
          ((QueryColumnAttributeField) field).setColumn(column);
        }
        field.resolveReferences(wdkModel);
      }

      // Continue only if a table attribute meta query reference is provided.
      if (_attributeMetaQueryTwoPartName != null) {
        for (Map<String, Object> row : getDynamicallyDefinedAttributes(_attributeMetaQueryTwoPartName,
            wdkModel)) {
          AttributeField attributeField = new QueryColumnAttributeField();

          // Need to call this explicitly since this attribute field originates from the database
          attributeField.excludeResources(wdkModel.getProjectId());

          // Populate the attributeField with the attribute meta data
          AttributeMetaQueryHandler.populate(attributeField, row);

          // Add the new attributeField to the map
          _attributeFieldMap.put(attributeField.getName(), attributeField);
        }
      }
    }

    // special processing for process queries
    else {

      // make copy of dereferenced query for this table field
      _processQuery = (ProcessQuery)query.clone();

      // confirm no params are present; PK and table name params are added below
      if (_processQuery.getParams().length != 0) {
        throw new WdkModelException("Process query " + _processQuery.getFullName() +
            " referenced by table field " + _name + " must have zero parameters. Params '" +
            PRIMARY_KEY_PARAM_NAME + "' and '" + TABLE_NAME_PARAM_NAME + "' will be added.");
      }

      // add parameters to the query
      _processQuery.addParam(createProcessQueryParam(PRIMARY_KEY_PARAM_NAME, wdkModel));
      _processQuery.addParam(createProcessQueryParam(TABLE_NAME_PARAM_NAME, wdkModel));
    }

    unpackAndValidateClientSortingOrder();

    _resolved = true;
  }

  private StringParam createProcessQueryParam(String name, WdkModel wdkModel) throws WdkModelException {
    StringParam param = new StringParam();
    param.setName(name);
    param.setAllowEmpty(false);
    param.setNoTranslation(true);
    param.resolveReferences(wdkModel);
    return param;
  }

  private String getTableNameForErrMsg() {
    return "<table name=\""  + _name + "\"> of recordClass " +  _recordClass.getFullName();
  }

  private void unpackAndValidateClientSortingOrder() throws WdkModelException {

    // comma delimited list of 'column_name ASC|DESC'
    String[] sortSpecStrings = _clientSortingOrderString == null? new String[0] : _clientSortingOrderString.split(",\\s*");

    for (String sortSpecString : sortSpecStrings) {

      String errPrefix = "Invalid clientSortingOrder item '" + sortSpecString + "' in " + getTableNameForErrMsg() + ": ";

      String[] parsedSpec = sortSpecString.split("\\s+");

      if (!(parsedSpec.length == 2 || SortDirection.isValidDirection(parsedSpec[1])))
        throw new WdkModelException(errPrefix + "must be in the form: column_name ASC|DESC, ...");

      if (!_attributeFieldMap.containsKey(parsedSpec[0]))
        throw new WdkModelException(errPrefix + " no attribute field exists with name " + parsedSpec[0]);

      SortDirectionSpec<AttributeField> sortDirection =
          new SortDirectionSpec<AttributeField>(_attributeFieldMap.get(parsedSpec[0]), SortDirection.valueOf(parsedSpec[1]));

      _clientSortingOrderList.add(sortDirection);
    }
  }

  @Override
  public int getTruncateTo() {
    throw new UnsupportedOperationException("getTruncate does not apply to TableField");
  }

  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    super.excludeResources(projectId);

    // exclude descriptions
    boolean hasDescription = false;
    for (WdkModelText description : _descriptions) {
      if (description.include(projectId)) {
        if (hasDescription) {
          throw new WdkModelException(getTableNameForErrMsg() + " has more than one description for project " + projectId);
        }
        else {
          _description = description.getText();
          hasDescription = true;
        }
      }
    }
    _descriptions = null;

    // exclude attributes
    for (AttributeField field : _attributeFieldList) {
      if (field.include(projectId)) {
        field.excludeResources(projectId);
        String fieldName = field.getName();
        if (_attributeFieldMap.containsKey(fieldName))
          throw new WdkModelException("In " + getTableNameForErrMsg() + "the attributeField " + fieldName + " is duplicated");
        _attributeFieldMap.put(fieldName, field);
      }
    }
    _attributeFieldList = null;
  }

  @Override
  protected void printDependencyContent(PrintWriter writer, String indent) throws WdkModelException {
    super.printDependencyContent(writer, indent);

    // print attribute fields
    writer.println(indent + "<attributes count=\"" + _attributeFieldMap.size() + "\">");
    String[] attributeNames = _attributeFieldMap.keySet().toArray(new String[0]);
    Arrays.sort(attributeNames);
    String indent1 = indent + WdkModel.INDENT;
    for (String attributeName : attributeNames) {
      _attributeFieldMap.get(attributeName).printDependency(writer, indent1);
    }

    // print table queries
    _wrappedQuery.printDependency(writer, indent);
  }

  /**
   * @return attribute category name
   */
  public String getAttributeCategory() {
    return _categoryName;
  }

  /**
   * @param categoryName
   *          attribute category name
   */
  public void setAttributeCategory(String categoryName) {
    _categoryName = categoryName;
  }

  @Override
  public String getNameForLogging() {
    return _recordClass.getFullName() + "." + getName();
  }

  public boolean hasSqlQuery() {
    return _processQuery == null;
  }

  public String getQueryFullName() {
    return hasSqlQuery() ? _wrappedQuery.getFullName() : _processQuery.getFullName();
  }

}
