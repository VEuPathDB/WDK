package org.gusdb.wdk.model;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * This class holds common methods and data related to WDK Queries.
 * Classes implementing this class are basically QueryTemplates for
 * <code>QueryInstance</code>s.
 * 
 * Instances of this class are not created in code, in most cases. They
 * are typically defined in the WDK Model XML file.
 */
public abstract class Query implements Serializable {

    private static Logger logger = Logger.getLogger(Query.class);

    protected String name;
    protected String fullName;
    protected String displayName;
    protected String description;
    protected String help;
    protected Boolean isCacheable = new Boolean(true);
    protected LinkedHashSet<ParamReference> paramRefs;
    protected Map<String, Param> paramsH;
    protected Vector<Param> paramsV;
    protected Map<String, Column> columnsH;
    protected Vector<Column> columnsV;
    protected ResultFactory resultFactory;

    protected String signature = null;
    protected String projectId;

    public Query() {
        paramRefs = new LinkedHashSet<ParamReference>();
        paramsH = new LinkedHashMap<String, Param>();
        paramsV = new Vector<Param>();
        columnsH = new LinkedHashMap<String, Column>();
        columnsV = new Vector<Column>();
    }

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
        paramRefs.add(paramRef);
    }

    public void setIsCacheable(Boolean isCacheable) {
        this.isCacheable = isCacheable;
    }

    public void setHelp(String help) {
        this.help = help;
    }

    public void addColumn(Column column) {
        column.setQuery(this);
        columnsV.add(column);
        columnsH.put(column.getName(), column);
        signature = null;
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

    /**
     * @return An array of parameters that this query has.
     */
    public Param[] getParams() {
        Param[] paramA = new Param[paramsV.size()];
        paramsV.copyInto(paramA);
        return paramA;
    }

    /**
     * @return A Map of parmeter name to Params.
     */
    public Map<String, Param> getParamMap() {
        return new LinkedHashMap<String, Param>(paramsH);
    }

    /**
     * This method will determine whether a QueryInstance can
     * actually be written to the database or not.
     * @return True if the query can be written or cached.
     */
    public Boolean getIsCacheable() {
        return isCacheable;
    }

    /**
     * @return The human understandable description of the Query.
     */
    public String getDescription() {
        return description;
    }

    public String getHelp() {
        return help;
    }

    /**
     * @return An array of all the Columns.
     */
    public Column[] getColumns() {
        Column[] columnA = new Column[columnsV.size()];
        columnsV.copyInto(columnA);
        return columnA;
    }

    /**
     * @return A map of column names to <code>Column</code>s.
     */
    public Map<String, Column> getColumnMap() {
        return columnsH;
    }

    /**
     * Get a Column given a column name.
     * @param columnName The name of the Column.
     * @return A Column.
     * @throws WdkModelException If the name is not a valid Column in this Query.
     */
    public Column getColumn(String columnName) throws WdkModelException {
        if (columnsH.get(columnName) == null)
            throw new WdkModelException("Query " + name
                    + " does not have a column '" + columnName + "'");
        return (Column) columnsH.get(columnName);
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
            Param param = paramsH.get(paramName);
            internalValues.put(paramName, param.getInternalValue(values.get(
                    paramName).toString()));
        }
        return internalValues;
    }

    public String toString() {
        String newline = System.getProperty("line.separator");
        StringBuffer buf = formatHeader();

        buf.append("--- Columns ---").append(newline);
        for (int i = 0; i < columnsV.size(); i++) {
            buf.append(columnsV.elementAt(i)).append(newline);
        }

        buf.append("--- Params ---").append(newline);
        for (int i = 0; i < paramsV.size(); i++) {
            buf.append(paramsV.elementAt(i)).append(newline);
        }

        return buf.toString();
    }

    public ResultFactory getResultFactory() {
        return resultFactory;
    }

    /**
     * Gets a unique checksum of this query.
     * @return The checksum of this Query.
     * @throws WdkModelException If the checksum algorithm is not found.
     * @see org.gusdb.wdk.model.QueryInstance.getCheckSum
     */
    public String getSignature() throws WdkModelException {
        if (signature == null) {
            StringBuffer content = new StringBuffer();
            content.append(fullName);

            // get parameter name list, and sort it
            String[] paramNames = new String[paramsH.size()];
            paramsH.keySet().toArray(paramNames);
            Arrays.sort(paramNames);

            // get a combination of parameters and types
            for (String paramName : paramNames) {
                Param param = paramsH.get(paramName);
                content.append(Utilities.DATA_DIVIDER);
                content.append(paramName);
                content.append('|');
                content.append(param.getClass().getName());
            }
            
            // get the columns
            String[] columnNames = new String[columnsH.size()];
            columnsH.keySet().toArray(columnNames);
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
      <sanityQuery ref="GeneFeatureIds.GenesByGeneType"
                   minOutputLength="30" maxOutputLength="100">
          <sanityParam name="geneType" value="tRNA"/>
          <sanityParam name="organism" value="Plasmodium falciparum"/>
      </sanityQuery>
    */
    public String getSanityTestSuggestion () throws WdkModelException {
	String indent = "    ";
        String newline = System.getProperty("line.separator");
	StringBuffer buf = new StringBuffer(
	      newline + newline
	    + indent + "<sanityQuery ref=\"" + getFullName() + "\"" 
	    + newline
	    + indent + indent + indent
	    + "minOutputLength=\"FIX_min_len\" maxOutputLength=\"FIX_max_len\">"
	    + newline);
	for (Param param : getParams()) {
	    String paramName = param.getName();
	    String value = param.getDefault();
	    if (value == null) value = "FIX_null_dflt";
	    buf.append(indent + indent
		       + "<sanityParam name=\"" + paramName 
		       + "\" value=\"" + value + "\"/>"
		       + newline);
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

    protected void addParam(Param param) {
        paramsV.add(param);
        paramsH.put(param.getName(), param);
        signature = null;
    }

    Param getParam(String paramName) {
        return paramsH.get(paramName);
    }

    /**
     * Take all the references in this Query and turn them into the
     * actual Parameters.
     * 
     * @param model The WdkModel that holds the actual Parameters.
     * @throws WdkModelException Cannot find a Parameter.
     */
    protected void resolveReferences(WdkModel model) throws WdkModelException {
        Iterator<ParamReference> paramRefsIter = paramRefs.iterator();
        while (paramRefsIter.hasNext()) {
            ParamReference paramRef = paramRefsIter.next();
            String twoPartName = paramRef.getTwoPartName();
            Param param = (Param) model.resolveReference(twoPartName,
                    this.name, "Query", "paramRef");
            // clone the param to have different default values
            param = param.clone();
            param.setDefault(paramRef.getDefault());
            
            // resolve the group reference
            String groupRef = paramRef.getGroupRef();
            if (groupRef != null) {
                Group group = (Group) model.resolveReference( groupRef, this.name, "Param", "groupRef" );
                param.setGroup( group );
            }
            addParam(param);
            param.resolveReferences(model);
        }
    }

    protected void setResources(WdkModel model) throws WdkModelException {

        this.resultFactory = model.getResultFactory();

        Iterator paramIterator = paramsH.values().iterator();
        while (paramIterator.hasNext()) {
            Param param = (Param) paramIterator.next();
            param.setResources(model);
        }
        this.projectId = model.getProjectId();
    }

    /**
     * Checks all parameters that are based from a <code>QueryInstance</code>
     * to see if they meet the requirements of the parameter types of the Query.
     * 
     * @param values A map of Param=Value pairs.
     * @throws WdkModelException If there are problems with the parameters.
     */
    protected void validateParamValues(Map<String, Object> values)
            throws WdkModelException {
        LinkedHashMap<Param, String[]> errors = null;

        // first confirm that all supplied values have legal names
        Iterator<String> valueNames = values.keySet().iterator();
        while (valueNames.hasNext()) {
            String valueName = valueNames.next();
            if (paramsH.get(valueName) == null) {
                throw new WdkModelException("'" + valueName
                        + "' is not a legal parameter name for query '"
                        + getFullName() + "'");
            }
        }

        // then check that all params have supplied values
        int size = paramsV.size();
        for (int i = 0; i < size; i++) {
            Param p = paramsV.elementAt(i);
            Object value = values.get(p.getName());
            String errMsg;
            if (value == null) {
                errMsg = "No value supplied for param " + p.getName();
            } else {
                errMsg = p.validateValue(value);
            }
            if (errMsg != null) {
                if (errors == null)
                    errors = new LinkedHashMap<Param, String[]>();
                String booBoo[] = { value == null ? "" : value.toString(),
                        errMsg };
                errors.put(p, booBoo);
            }
        }
        if (errors != null) {
            WdkModelException ex = new WdkModelException(errors);
            logger.debug(ex.formatErrors());
            throw ex;
        }
    }

    /**
     * Applies the default values for Parmaeters such that they are
     * stored in the Map.
     * 
     * @param values The place to store the default values.
     * @throws WdkModelException never thrown.
     */
    protected void applyDefaults(Map<String, Object> values) throws WdkModelException {
        int size = paramsV.size();
        for (int i = 0; i < size; i++) {
            Param p = paramsV.elementAt(i);
            if (values.get(p.getName()) == null && p.getDefault() != null)
                values.put(p.getName(), p.getDefault());
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
     * @param allowedColumns The column names that are allowed in the base query
     * @return returns a cloned base query.
     */
    public abstract Query getBaseQuery(Set<String> excludedColumns);

    /**
     * The query clones its members, and only contains the allowed columns
     * 
     * @param query
     * @param excludedColumns
     */
    protected void clone(Query query, Set<String> excludedColumns) {
        // copy allowed columns
        for (Column column : this.columnsV) {
            if (!excludedColumns.contains(column.getName())) {
                Column newColumn = column.clone();
                query.addColumn(newColumn);
            }
        }
        // clone other attributes
        query.description = this.description;
        query.displayName = this.displayName;
        query.fullName = this.fullName;
        query.help = this.help;
        query.isCacheable = this.isCacheable;
        query.name = this.name;
        query.paramRefs.addAll(this.paramRefs);
        query.paramsH.putAll(this.paramsH);
        query.paramsV.addAll(this.paramsV);
        query.resultFactory = this.resultFactory;
    }
    
    /**
     * The data for which the signature is to be generated for this
     * Query.
     * 
     * @return A String representing the data to checksum.
     */
    protected abstract String getSignatureData();
}
