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
import org.gusdb.wdk.model.AbstractEnumParam;
import org.gusdb.wdk.model.Column;
import org.gusdb.wdk.model.DatasetParam;
import org.gusdb.wdk.model.FlatVocabParam;
import org.gusdb.wdk.model.ParamValuesSet;
import org.gusdb.wdk.model.Group;
import org.gusdb.wdk.model.Param;
import org.gusdb.wdk.model.ParamReference;
import org.gusdb.wdk.model.QuerySet;
import org.gusdb.wdk.model.StringParam;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Jerric Gao
 * 
 */
public abstract class Query extends WdkModelBase {

    private static final Logger logger = Logger.getLogger(Query.class);

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
    private List<ParamValuesSet> paramValuesSets
	= new ArrayList<ParamValuesSet>();

    protected WdkModel wdkModel;
    private QuerySet querySet;

    // cache the signature
    private String signature;

    // =========================================================================
    // Abstract methods
    // =========================================================================

    protected abstract void appendJSONContent(JSONObject jsQuery)
            throws JSONException;

    public abstract QueryInstance makeInstance(Map<String, Object> values)
            throws WdkModelException;

    public abstract Query clone();

    // =========================================================================
    // Constructors
    // =========================================================================

    protected Query() {
        paramRefList = new ArrayList<ParamReference>();
        paramMap = new LinkedHashMap<String, Param>();
        columnList = new ArrayList<Column>();
        columnMap = new LinkedHashMap<String, Column>();
    }

    /**
     * clone the query object
     * 
     * @param query
     */
    protected Query(Query query) {
        this.name = query.name;
        this.cached = query.cached;
        this.paramMap = new LinkedHashMap<String, Param>();
        this.columnMap = new LinkedHashMap<String, Column>();
        this.wdkModel = query.wdkModel;
        this.querySet = query.querySet;
        this.doNotTest = query.doNotTest;
        this.signature = query.signature;
        this.paramValuesSets = new ArrayList<ParamValuesSet>(query.paramValuesSets);
        this.wdkModel =query.wdkModel;

        // clone columns
        for (String columnName : query.columnMap.keySet()) {
            Column column = new Column(query.columnMap.get(columnName));
            column.setQuery(this);
            columnMap.put(columnName, column);
        }

        // clone params
        for (String paramName : query.paramMap.keySet()) {
            Param param = query.paramMap.get(paramName).clone();
            paramMap.put(paramName, param);
        }
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
     *                the querySet to set
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
     *                the cached to set
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

    /**
     * Skip the initialization, and set the column directly
     * 
     * @param columns
     */
    public void setColumns(Column[] columns) {
        columnMap.clear();
        for (Column column : columns) {
            Column newColumn = new Column(column);
            newColumn.setQuery(this);
            columnMap.put(column.getName(), newColumn);
        }
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

    public List<ParamValuesSet> getParamValuesSets() throws WdkModelException, NoSuchAlgorithmException, SQLException, JSONException, WdkUserException {
	updateParamValuesSetsWithDefaults();
	return paramValuesSets;
    }

    public WdkModel getWdkModel() {
        return wdkModel;
    }

    public String getChecksum() throws JSONException,
            NoSuchAlgorithmException, WdkModelException {
        if (signature == null) {
            JSONObject jsQuery = getJSONContent();
            signature = Utilities.encrypt(jsQuery.toString());
        }
        return signature;
    }

    private JSONObject getJSONContent() throws JSONException {
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
            jsParams.put(param.getJSONContent());
        }
        jsQuery.put("params", jsParams);

        // construct columns; ordered by columnName
        String[] columnNames = new String[columnMap.size()];
        columnMap.keySet().toArray(columnNames);
        Arrays.sort(columnNames);

        JSONArray jsColumns = new JSONArray();
        for (String columnName : columnNames) {
            Column column = columnMap.get(columnName);
            jsColumns.put(column.getJSONContent());
        }
        jsQuery.put("columns", jsColumns);

        // append child-specific data
        appendJSONContent(jsQuery);

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

    public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
        if (resolved) return;
        
        this.wdkModel = wdkModel;

        // resolve the params
        for (ParamReference paramRef : paramRefList) {
            Param param = resolveParamReference(wdkModel, paramRef);
            String paramName = param.getName();
            if (paramMap.containsKey(paramName)) {
                throw new WdkModelException("The param '" + paramName
                        + "' is duplicated in query " + getFullName());
            } else paramMap.put(paramName, param);
        }
        paramRefList = null;
        resolved = true;
    }

    private Param resolveParamReference(WdkModel wdkModel,
            ParamReference paramRef) throws WdkModelException {
        String twoPartName = paramRef.getTwoPartName();
        Param param = (Param) wdkModel.resolveReference(twoPartName);
        // clone the param to have different default values
        param = param.clone();

        // if the param has customized default value
        String defaultValue = paramRef.getDefault();
        if (defaultValue != null) param.setDefault(defaultValue);

        // if the param has customized allowEmpty
        Boolean allowEmpty = paramRef.isAllowEmpty();
        if (allowEmpty != null) {
            param.setAllowEmpty(allowEmpty);

            // if the param has customized allowEmpty
            String emptyValue = paramRef.getEmptyValue();
            if (emptyValue != null) param.setEmptyValue(emptyValue);
        }

        Boolean quote = paramRef.getQuote();
        Boolean multiPick = paramRef.isMultiPick();
        Boolean useTermOnly = paramRef.getUseTermOnly();
        String queryRef = paramRef.getQueryRef();
        String displayType = paramRef.getDisplayType();
        if (param instanceof AbstractEnumParam) {
            if (param instanceof FlatVocabParam)
                ((FlatVocabParam) param).setServedQueryName(getFullName());

            // if the param has customized multi pick
            if (multiPick != null)
                ((AbstractEnumParam) param).setMultiPick(multiPick);

            // if the useTermOnly is set
            if (useTermOnly != null)
                ((AbstractEnumParam) param).setUseTermOnly(useTermOnly);

            // if the queryRef is set for FlatVocabParam
            if (queryRef != null) {
                if (param instanceof FlatVocabParam) {
                    ((FlatVocabParam) param).setQueryRef(queryRef);
                } else throw new WdkModelException("The paramRef to '"
                        + twoPartName + "' is not a flatVocabParam. The "
                        + "'queryRef' property can only be applied to "
                        + "paramRefs of flatVocabParams.");
            }

            // if quote is set, it overrides the value of the param
            if (quote != null) ((AbstractEnumParam) param).setQuote(quote);

            // if displayType is set, overrides the value in param
            if (displayType != null)
                ((AbstractEnumParam) param).setDisplayType(displayType);
        } else if (multiPick != null || useTermOnly != null
                || displayType != null) {
            throw new WdkModelException("The paramRef to '" + twoPartName
                    + "' is not a flatVocabParam nor enumParam. The "
                    + "'multiPick', 'useTermOnly', 'displayType' property "
                    + "can only be applied to "
                    + "paramRefs of flatVocabParams or enumParams.");
        } else if (param instanceof StringParam) {
            // if quote is set, it overrides the value of the param
            if (quote != null) ((StringParam) param).setQuote(quote);
        }

        // resolve the group reference
        String groupRef = paramRef.getGroupRef();
        if (groupRef != null) {
            Group group = (Group) wdkModel.resolveReference(groupRef);
            param.setGroup(group);
        }
        param.resolveReferences(wdkModel);
        return param;
    }

    private void updateParamValuesSetsWithDefaults() throws WdkModelException, NoSuchAlgorithmException, SQLException, JSONException, WdkUserException {
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

    void validateValues(Map<String, Object> values) throws WdkModelException {
        // fill the empty values
        fillEmptyValues(values);

        Map<Param, String[]> errors = null;

        // then check that all params have supplied values
        for (Param param : paramMap.values()) {
            Object value = values.get(param.getName());
            String errMsg = null;
            try {
                // validate dataset param by getting the dataset id
                if (param instanceof DatasetParam) {
                    param.getInternalValue(value.toString());
                } else {
                    param.validateValue(value);
                }
            } catch (Exception ex) {
                errMsg = ex.getMessage();
                if (errMsg == null) errMsg = ex.getClass().getName();
            }
            if (errMsg != null) {
                if (errors == null)
                    errors = new LinkedHashMap<Param, String[]>();
                String booBoo[] = { value == null ? "" : value.toString(),
                        errMsg };
                errors.put(param, booBoo);
            }
        }
        if (errors != null) {
            WdkModelException ex = new WdkModelException(errors);
            logger.debug(ex.formatErrors());
            throw ex;
        }
    }

    private void fillEmptyValues(Map<String, Object> values)
            throws WdkModelException {
        for (String paramName : values.keySet()) {
            if (!paramMap.containsKey(paramName))
                throw new WdkModelException("Param '" + paramName
                        + "' is not legal for query " + getFullName());

            Param param = paramMap.get(paramName);
            if (!param.isAllowEmpty()) continue;

            Object value = values.get(paramName);
            if (value == null
                    || (paramName instanceof String && "".equals(value)))
                values.put(paramName, param.getEmptyValue());
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer(getFullName());
        buffer.append(": params{");
        boolean firstParam = true;
        for(Param param : paramMap.values()) {
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
    
}
