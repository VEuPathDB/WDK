package org.gusdb.wdk.model;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

public abstract class Query extends WdkModelBase implements Serializable {

    public static final String PROJECT_ID_COLUMN = "project_id";

    private static Logger logger = Logger.getLogger(Query.class);

    protected String name;
    protected String fullName;
    protected String displayName;
    protected String description;
    protected String help;
    protected Boolean isCacheable = new Boolean(true);
    protected List<ParamReference> paramRefList = new ArrayList<ParamReference>();
    protected Map<String, ParamReference> paramRefs = new LinkedHashMap<String, ParamReference>();

    protected Map<String, Param> params = new LinkedHashMap<String, Param>();
    private List<Column> columnList = new ArrayList<Column>();
    protected Map<String, Column> columns = new LinkedHashMap<String, Column>();

    protected ResultFactory resultFactory;

    protected String signature = null;
    protected String projectId;

    // ///////////////////////////////////////////////////////////////////
    // /////////// Setters for initialization ///////////////////////////
    // ///////////////////////////////////////////////////////////////////

    public void setName(String name) {
        this.name = name;
        signature = null;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addParamRef(ParamReference paramRef) {
        paramRefList.add(paramRef);
    }

    public void setIsCacheable(Boolean isCacheable) {
        this.isCacheable = isCacheable;
    }

    public void setHelp(String help) {
        this.help = help;
    }

    public void addColumn(Column column) {
        columnList.add(column);
        signature = null;
    }

    void addColumnToMap(Column column) throws WdkModelException {
        String columnName = column.getName();
        if (columns.containsKey(columnName))
            throw new WdkModelException("The column " + columnName
                    + " is duplicated in query " + getFullName());
        columns.put(column.getName(), column);
    }

    // ///////////////////////////////////////////////////////////////////////
    // public getters
    // ///////////////////////////////////////////////////////////////////////

    public String getName() {
        return name;
    }

    public String getFullName() {
        return fullName == null ? name : fullName;
    }

    public String getDisplayName() {
        return (displayName != null) ? displayName : name;
    }

    public Param[] getParams() {
        Param[] paramA = new Param[params.size()];
        params.values().toArray(paramA);
        return paramA;
    }

    public Map<String, Param> getParamMap() {
        return new LinkedHashMap<String, Param>(params);
    }

    public Boolean getIsCacheable() {
        return isCacheable;
    }

    public String getDescription() {
        return description;
    }

    public String getHelp() {
        return help;
    }

    public Column[] getColumns() {
        Column[] columnA = new Column[columns.size()];
        columns.values().toArray(columnA);
        return columnA;
    }

    public Map<String, Column> getColumnMap() {
        return new LinkedHashMap<String, Column>(columns);
    }

    public Column getColumn(String columnName) throws WdkModelException {
        Column column = columns.get(columnName);
        if (column == null)
            throw new WdkModelException("Query " + name
                    + " does not have a column '" + columnName + "'");
        return column;
    }

    public abstract QueryInstance makeInstance();

    /**
     * transform a set of param values to internal param values
     */
    public Map<String, String> getInternalParamValues(Map<String, Object> values)
            throws WdkModelException {

        Map<String, String> internalValues = new LinkedHashMap<String, String>();
        Iterator<String> paramNames = values.keySet().iterator();
        while (paramNames.hasNext()) {
            String paramName = paramNames.next();
            Param param = params.get(paramName);
            internalValues.put(paramName, param.getInternalValue(values.get(
                    paramName).toString()));
        }
        return internalValues;
    }

    public String toString() {
        String newline = System.getProperty("line.separator");
        StringBuffer buf = formatHeader();

        buf.append("--- Columns ---").append(newline);
        for (Column column : columns.values()) {
            buf.append(column);
            buf.append(newline);
        }

        buf.append("--- Params ---").append(newline);
        for (Param param : params.values()) {
            buf.append(param);
            buf.append(newline);
        }

        return buf.toString();
    }

    public ResultFactory getResultFactory() {
        return resultFactory;
    }

    public String getSignature() throws WdkModelException {
        if (signature == null) {
            StringBuffer content = new StringBuffer();
            content.append(fullName);

            // get parameter name list, and sort it
            String[] paramNames = new String[params.size()];
            params.keySet().toArray(paramNames);
            Arrays.sort(paramNames);

            // get a combination of parameters and types
            for (String paramName : paramNames) {
                Param param = params.get(paramName);
                content.append(Utilities.DATA_DIVIDER);
                content.append(paramName);
                content.append('|');
                content.append(param.getClass().getName());
            }

            // get the columns
            String[] columnNames = new String[columns.size()];
            columns.keySet().toArray(columnNames);
            Arrays.sort(columnNames);

            for (String columnName : columnNames) {
                content.append(Utilities.DATA_DIVIDER);
                content.append(columnName);
            }

            // get extra data for making signature
            String extra = getSignatureData();
            if (extra != null) {
                content.append(Utilities.DATA_DIVIDER);
                content.append(extra);
            }

            try {
                MessageDigest digest = MessageDigest.getInstance("MD5");
                byte[] byteBuffer = digest.digest(content.toString().getBytes());
                // convert each byte into hex format
                StringBuffer buffer = new StringBuffer();
                for (int i = 0; i < byteBuffer.length; i++) {
                    int code = (byteBuffer[i] & 0xFF);
                    if (code < 0x10) buffer.append('0');
                    buffer.append(Integer.toHexString(code));
                }
                signature = buffer.toString();
            } catch (NoSuchAlgorithmException ex) {
                throw new WdkModelException(ex);
            }
        }
        return signature;
    }

    /*
     * <sanityQuery ref="GeneFeatureIds.GenesByGeneType" minOutputLength="30"
     * maxOutputLength="100"> <sanityParam name="geneType" value="tRNA"/>
     * <sanityParam name="organism" value="Plasmodium falciparum"/>
     * </sanityQuery>
     */
    public String getSanityTestSuggestion() throws WdkModelException {
        String indent = "    ";
        String newline = System.getProperty("line.separator");
        StringBuffer buf = new StringBuffer(newline + newline + indent
                + "<sanityQuery ref=\"" + getFullName() + "\"" + newline
                + indent + indent + indent + "minOutputLength=\"FIX_min_len\" "
                + "maxOutputLength=\"FIX_max_len\">" + newline);
        for (Param param : getParams()) {
            String paramName = param.getName();
            String value = param.getDefault();
            if (value == null) value = "FIX_null_dflt";
            buf.append(indent + indent + "<sanityParam name=\"" + paramName
                    + "\" value=\"" + value + "\"/>" + newline);
        }
        buf.append(indent + "</sanityQuery>");
        return buf.toString();
    }

    public String getProjectId() {
        return projectId;
    }

    // ///////////////////////////////////////////////////////////////////
    // /////////// Protected methods ////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////

    void setSetName(String querySetName) {
        this.fullName = querySetName + "." + name;
        signature = null;
    }

    protected void addParam(Param param) throws WdkModelException {
        String paramName = param.getName();
        if (params.containsKey(paramName))
            throw new WdkModelException("The param " + paramName
                    + " is duplicated in query " + getFullName());
        params.put(paramName, param);
        signature = null;
    }

    public Param getParam(String paramName) {
        return params.get(paramName);
    }

    protected void resolveReferences(WdkModel model) throws WdkModelException {
        for (ParamReference paramRef : paramRefs.values()) {
            String twoPartName = paramRef.getTwoPartName();
            Param param = (Param) model.resolveReference(twoPartName);
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
            if (param instanceof FlatVocabParam || param instanceof EnumParam) {
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
                Group group = (Group) model.resolveReference(groupRef);
                param.setGroup(group);
            }
            addParam(param);
            param.resolveReferences(model);
        }
    }

    protected void setResources(WdkModel model) throws WdkModelException {

        this.resultFactory = model.getResultFactory();

        for (Param param : params.values()) {
            param.setResources(model);
        }
        this.projectId = model.getProjectId();
    }

    protected void validateParamValues(Map<String, Object> values)
            throws WdkModelException {
        Map<Param, String[]> errors = null;

        // first confirm that all supplied values have legal names
        for (String paramName : values.keySet()) {
            if (params.get(paramName) == null) {
                throw new WdkModelException("'" + paramName
                        + "' is not a legal parameter name for query '"
                        + getFullName() + "'");
            }
        }

        // then check that all params have supplied values
        for (Param param : params.values()) {
            Object value = values.get(param.getName());
            String errMsg = null;
            if (param instanceof DatasetParam) {
                // validate dataset param by getting the dataset id
                try {
                    param.getInternalValue(value.toString());
                } catch (WdkModelException ex) {
                    errMsg = ex.toString();
                }
            } else {
                errMsg = param.validateValue(value);
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

    protected void applyDefaults(Map<String, Object> values)
            throws WdkModelException {
        for (Param param : params.values()) {
            String paramName = param.getName();
            Object paramValue = values.get(paramName);
            if (paramValue == null || paramValue.toString().length() == 0) {
                if (param.isAllowEmpty()) {
                    values.put(paramName, param.getEmptyValue());
                } else if (param.getDefault() != null) {
                    values.put(paramName, param.getDefault());
                }
            }
        }
    }

    protected StringBuffer formatHeader() {
        String newline = System.getProperty("line.separator");
        StringBuffer buf = new StringBuffer("Query: name='" + getName() + "'"
                + newline + "  displayName='" + getDisplayName() + "'"
                + newline + "  description='" + getDescription() + "'"
                + newline + "  help='" + getHelp() + "'" + newline);
        return buf;
    }

    /**
     * This method is used when we clone a question into its base form, which
     * doesn't contain any dynamic attribute. The base query shouldn't contain
     * any columns associated with dynamic attributes either.
     * 
     * @param allowedColumns
     *          The column names that are allowed in the base query
     * @return returns a cloned base query.
     */
    public abstract Query getBaseQuery(Set<String> excludedColumns)
            throws WdkModelException;

    /**
     * The query clones its members, and only contains the allowed columns
     * 
     * @param query
     * @param allowedColumns
     * @throws WdkModelException
     */
    protected void clone(Query query, Set<String> excludedColumns)
            throws WdkModelException {
        // TEST print out excluded columns
        StringBuffer sb = new StringBuffer();
        for (String exCol : excludedColumns) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(exCol);
        }
        logger.debug("Excluded Columns[" + excludedColumns.size() + "]: "
                + sb.toString());

        // copy allowed columns
        for (Column column : this.columns.values()) {
            logger.debug("Source Column: " + column.getName());
            if (!excludedColumns.contains(column.getName())) {
                query.addColumnToMap(column.clone());
            }
        }
        // clone other attributes
        query.description = this.description;
        query.displayName = this.displayName;
        query.fullName = this.fullName;
        query.help = this.help;
        query.isCacheable = this.isCacheable;
        query.name = this.name;
        query.paramRefs = new LinkedHashMap<String, ParamReference>(
                this.paramRefs);
        query.params = new LinkedHashMap<String, Param>(this.params);

        query.resultFactory = this.resultFactory;
        query.signature = signature;
        query.projectId = projectId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        // exclude paramRefs
        for (ParamReference paramRef : paramRefList) {
            if (paramRef.include(projectId)) {
                String refName = paramRef.getTwoPartName();
                if (paramRefs.containsKey(refName)) {
                    throw new WdkModelException("query " + getFullName()
                            + " has more than one paramRef " + refName);
                } else {
                    paramRef.excludeResources(projectId);
                    paramRefs.put(refName, paramRef);
                }
            }
        }
        paramRefList = null;

        // exclude columns
        for (Column column : columnList) {
            if (column.include(projectId)) {
                column.setQuery(this);
                column.excludeResources(projectId);
                addColumnToMap(column);
            }
        }
        columnList = null;
    }

    protected abstract String getSignatureData();
}
