/**
 * 
 */
package org.gusdb.wdk.model.query;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.AbstractEnumParam;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.ParamReference;
import org.gusdb.wdk.model.query.param.ParamValuesSet;
import org.gusdb.wdk.model.user.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Jerric Gao
 * 
 */
public abstract class Query extends WdkModelBase {

    protected static final Logger logger = Logger.getLogger(Query.class);

    private String name;
    protected boolean cached = false;

    // temp list, will be discarded after resolve references
    private List<ParamReference> paramRefList;
    protected Map<String, Param> paramMap;

    // temp list, will be discarded after resolve references
    private List<Column> columnList;
    protected Map<String, Column> columnMap;

    // for sanity testing
    private boolean doNotTest = false;
    private List<ParamValuesSet> paramValuesSets = new ArrayList<ParamValuesSet>();

    protected WdkModel wdkModel;
    private QuerySet querySet;

    private String[] indexColumns;

    private boolean hasWeight;

    // =========================================================================
    // Abstract methods
    // =========================================================================

    protected abstract void appendJSONContent(JSONObject jsQuery, boolean extra)
            throws JSONException;

    public abstract QueryInstance makeInstance(User user,
            Map<String, String> values, boolean validate, int assignedWeight,
            Map<String, String> context) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException;

    public abstract Query clone();

    public abstract void resolveQueryReferences(WdkModel wdkModel)
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException;

    // =========================================================================
    // Constructors
    // =========================================================================

    protected Query() {
        paramRefList = new ArrayList<ParamReference>();
        paramMap = new LinkedHashMap<String, Param>();
        columnList = new ArrayList<Column>();
        columnMap = new LinkedHashMap<String, Column>();
        hasWeight = false;
    }

    /**
     * clone the query object
     * 
     * @param query
     */
    protected Query(Query query) {
        super(query);

        // logger.debug("clone query: " + query.getFullName());
        this.name = query.name;
        this.cached = query.cached;
        this.paramMap = new LinkedHashMap<String, Param>();
        this.columnMap = new LinkedHashMap<String, Column>();
        this.wdkModel = query.wdkModel;
        this.querySet = query.querySet;
        this.doNotTest = query.doNotTest;
        this.paramValuesSets = new ArrayList<ParamValuesSet>(
                query.paramValuesSets);
        this.wdkModel = query.wdkModel;
        this.hasWeight = query.hasWeight;

        // clone columns
        for (String columnName : query.columnMap.keySet()) {
            Column column = new Column(query.columnMap.get(columnName));
            column.setQuery(this);
            columnMap.put(columnName, column);
        }

        // clone params
        for (String paramName : query.paramMap.keySet()) {
            Param param = query.paramMap.get(paramName).clone();
            param.setContextQuery(this);
            paramMap.put(paramName, param);
        }
    }

    public void setIndexColumns(String[] indexColumns) {
        this.indexColumns = indexColumns;
    }

    public String[] getIndexColumns() {
        return indexColumns;
    }

    /**
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the querySet
     */
    public QuerySet getQuerySet() {
        return querySet;
    }

    /**
     * @param querySet
     *            the querySet to set
     */
    public void setQuerySet(QuerySet querySet) {
        this.querySet = querySet;
    }

    /**
     * @return the cached
     */
    public boolean isCached() {
        return this.cached;
    }

    /**
     * @param cached
     *            the cached to set
     */
    public void setIsCacheable(boolean cached) {
        this.cached = cached;
    }

    public String getFullName() {
        return querySet.getName() + "." + name;
    }

    public void addParamRef(ParamReference paramRef) {
        this.paramRefList.add(paramRef);
    }

    /**
     * Add a param into the query
     * 
     * @param param
     */
    public void addParam(Param param) {
        param.setContextQuery(this);
        paramMap.put(param.getName(), param);
    }

    public Map<String, Param> getParamMap() {
        return new LinkedHashMap<String, Param>(paramMap);
    }

    public Param[] getParams() {
        Param[] array = new Param[paramMap.size()];
        paramMap.values().toArray(array);
        return array;
    }

    public void addColumn(Column column) {
        column.setQuery(this);
        if (columnList != null) this.columnList.add(column);
        else columnMap.put(column.getName(), column);
    }

    public Map<String, Column> getColumnMap() {
        return new LinkedHashMap<String, Column>(columnMap);
    }

    public Column[] getColumns() {
        Column[] array = new Column[columnMap.size()];
        columnMap.values().toArray(array);
        return array;
    }

    // exclude this query from sanity testing
    public void setDoNotTest(boolean doNotTest) {
        this.doNotTest = doNotTest;
    }

    public boolean getDoNotTest() {
        return doNotTest;
    }

    public void addParamValuesSet(ParamValuesSet paramValuesSet) {
        paramValuesSets.add(paramValuesSet);
    }

    public List<ParamValuesSet> getParamValuesSets() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        updateParamValuesSetsWithDefaults();
        return paramValuesSets;
    }

    public WdkModel getWdkModel() {
        return wdkModel;
    }

    public String getChecksum(boolean extra) throws JSONException,
            NoSuchAlgorithmException, WdkModelException {
        JSONObject jsQuery = getJSONContent(extra);
        return Utilities.encrypt(jsQuery.toString());
    }

    /**
     * @param extra
     *            , if extra is true, then column names are also includes, plus
     *            the extra info from param.
     * @return
     * @throws JSONException
     */
    private JSONObject getJSONContent(boolean extra) throws JSONException {
        // use JSON to construct the string content
        JSONObject jsQuery = new JSONObject();
        jsQuery.put("name", getFullName());
        jsQuery.put("project", wdkModel.getProjectId());

        // construct params; ordered by paramName
        String[] paramNames = new String[paramMap.size()];
        paramMap.keySet().toArray(paramNames);
        Arrays.sort(paramNames);

        JSONArray jsParams = new JSONArray();
        for (String paramName : paramNames) {
            Param param = paramMap.get(paramName);
            jsParams.put(param.getJSONContent(extra));
        }
        jsQuery.put("params", jsParams);

        // construct columns; ordered by columnName
        if (extra) {
            String[] columnNames = new String[columnMap.size()];
            columnMap.keySet().toArray(columnNames);
            Arrays.sort(columnNames);

            JSONArray jsColumns = new JSONArray();
            for (String columnName : columnNames) {
                Column column = columnMap.get(columnName);
                jsColumns.put(column.getJSONContent());
            }
            jsQuery.put("columns", jsColumns);
        }

        // append child-specific data
        appendJSONContent(jsQuery, extra);

        return jsQuery;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        // exclude paramRefs
        List<ParamReference> paramRefs = new ArrayList<ParamReference>();
        for (ParamReference paramRef : paramRefList) {
            if (paramRef.include(projectId)) {
                paramRef.excludeResources(projectId);
                paramRefs.add(paramRef);
            }
        }
        paramRefList = paramRefs;

        // exclude columns
        for (Column column : columnList) {
            if (column.include(projectId)) {
                column.excludeResources(projectId);
                String columnName = column.getName();
                if (columnMap.containsKey(columnName)) {
                    throw new WdkModelException("The column '" + columnName
                            + "' is duplicated in query " + getFullName());
                } else columnMap.put(columnName, column);
            }
        }
        columnList = null;

        // exclude paramValuesSets
        List<ParamValuesSet> tempList = new ArrayList<ParamValuesSet>();
        for (ParamValuesSet paramValuesSet : paramValuesSets) {
            if (paramValuesSet.include(projectId)) {
                tempList.add(paramValuesSet);
            }
        }
        paramValuesSets = tempList;
    }

    public void resolveReferences(WdkModel wdkModel) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        // logger.debug("Resolving " + getFullName() + " - " + resolved);
        if (resolved) return;

        this.wdkModel = wdkModel;

        // resolve the params
        for (ParamReference paramRef : paramRefList) {
            Param param = ParamReference.resolveReference(wdkModel, paramRef,
                    getFullName());
            String paramName = param.getName();
            if (paramMap.containsKey(paramName)) {
                throw new WdkModelException("The param '" + paramName
                        + "' is duplicated in query " + getFullName());
            } else {
                param.setContextQuery(this);
                paramMap.put(paramName, param);
            }
        }
        paramRefList = null;

        // apply the default values to depended params
        Map<String, String> valueStub = new LinkedHashMap<String, String>();
        for (Param param : paramMap.values()) {
            // FIXME - this cause problems with some params, need to investigate. comment out temporarily
            // resolveDependedValue(valueStub, param);
        }

        // resolve columns
        for (Column column : columnMap.values()) {
            String sortingColumn = column.getSortingColumn();
            if (sortingColumn == null) continue;
            if (!columnMap.containsKey(sortingColumn))
                throw new WdkModelException("Query [" + getFullName()
                        + "] has a column [" + column.getName()
                        + "] with sortingColumn [" + sortingColumn
                        + "], but the sorting column doesn't exist in "
                        + "the same query.");
        }

        // if the query is a transform, it has to return weight column.
        // this applies to both explicit transform and filter queries.
        if (isTransform()) {
            if (!columnMap.containsKey(Utilities.COLUMN_WEIGHT))
                throw new WdkModelException("Transform query [" + getFullName()
                        + "] doesn't define the required "
                        + Utilities.COLUMN_WEIGHT + " column.");
        }

        resolveQueryReferences(wdkModel);
        resolved = true;
    }

    void resolveDependedValue(Map<String, String> values, Param param)
            throws WdkModelException, NoSuchAlgorithmException,
            WdkUserException, SQLException, JSONException {
        if (!(param instanceof AbstractEnumParam)) return;

        AbstractEnumParam enumParam = (AbstractEnumParam) param;
        Param dependedParam = enumParam.getDependedParam();
        if (dependedParam == null) return;

        String dependedValue = values.get(dependedParam.getName());
        if (dependedValue == null) {
            resolveDependedValue(values, dependedParam);
            dependedValue = dependedParam.getDefault();
        }
        enumParam.setDependedValue(dependedValue);
    }

    private void updateParamValuesSetsWithDefaults() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        ParamValuesSet querySetDefaults = querySet.getDefaultParamValuesSet();
        if (paramValuesSets.isEmpty()) {
            paramValuesSets.add(new ParamValuesSet());
        }
        for (ParamValuesSet paramValuesSet : paramValuesSets) {
            paramValuesSet.updateWithDefaults(querySetDefaults);

            for (Param param : getParams()) {
                String paramName = param.getName();
                String defaultValue = param.getDefault();
                paramValuesSet.updateWithDefault(paramName, defaultValue);
            }
        }
    }

    /**
     * @return the combined
     */
    public boolean isCombined() {
        return (getAnswerParamCount() > 0);
    }

    public boolean isBoolean() {
        return (this instanceof BooleanQuery);
    }

    public boolean isTransform() {
        return (getAnswerParamCount() == 1);
    }

    public int getAnswerParamCount() {
        int count = 0;
        for (Param param : paramMap.values()) {
            if (param instanceof AnswerParam) count++;
        }
        return count;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer(getFullName());
        buffer.append(": params{");
        boolean firstParam = true;
        for (Param param : paramMap.values()) {
            if (firstParam) firstParam = false;
            else buffer.append(", ");
            buffer.append(param.getName()).append("[");
            buffer.append(param.getClass().getSimpleName()).append("]");
        }
        buffer.append("} columns{");
        boolean firstColumn = true;
        for (Column column : columnMap.values()) {
            if (firstColumn) firstColumn = false;
            else buffer.append(", ");
            buffer.append(column.getName());
        }
        buffer.append("}");
        return buffer.toString();
    }

    public Map<String, String> rawOrDependentValuesToDependentValues(User user,
            Map<String, String> rawValues) throws NoSuchAlgorithmException,
            WdkModelException, WdkUserException, SQLException, JSONException {
        Map<String, String> dependentValues = new LinkedHashMap<String, String>();
        for (String paramName : rawValues.keySet()) {
            Param param = paramMap.get(paramName);
            if (param == null) {
                // instead of throwing an error, wdk will silently ignore it
                // throw new WdkModelException("Invalid param name '" +
                // paramName
                // + "' in query " + getFullName());
                logger.warn("Param " + paramName + " does not exist in query "
                        + getFullName());
                continue;
            }
            String rawValue = rawValues.get(paramName);
            String dependentValue = param.rawOrDependentValueToDependentValue(
                    user, rawValue);
            dependentValues.put(paramName, dependentValue);
        }
        if (paramMap.containsKey(Utilities.PARAM_USER_ID)) {
            if (!dependentValues.containsKey(Utilities.PARAM_USER_ID))
                dependentValues.put(Utilities.PARAM_USER_ID,
                        Integer.toString(user.getUserId()));
        }
        return dependentValues;
    }

    public Map<String, String> dependentValuesToIndependentValues(User user,
            Map<String, String> dependentValues)
            throws NoSuchAlgorithmException, WdkModelException,
            WdkUserException, SQLException, JSONException {
        Map<String, String> independentValues = new LinkedHashMap<String, String>();
        for (String paramName : dependentValues.keySet()) {
            Param param = paramMap.get(paramName);
            if (param == null) {
                // instead of throwing an error, wdk will silently ignore it
                // throw new WdkModelException("Invalid param name '" +
                // paramName
                // + "' in query " + getFullName());
                logger.warn("Param " + paramName + " does not exist in query "
                        + getFullName());
                continue;
            }
            String dependentValue = dependentValues.get(paramName);
            String independentValue = param.dependentValueToIndependentValue(
                    user, dependentValue);
            independentValues.put(paramName, independentValue);
        }
        return independentValues;
    }

    /**
     * @param hasWeight
     *            the hasWeight to set
     */
    public void setHasWeight(boolean hasWeight) {
        this.hasWeight = hasWeight;
    }

    /**
     * @return the hasWeight
     */
    public boolean isHasWeight() {
        return hasWeight;
    }
}
