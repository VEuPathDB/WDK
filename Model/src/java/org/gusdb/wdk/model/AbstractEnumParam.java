package org.gusdb.wdk.model;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

public abstract class AbstractEnumParam extends Param {

    protected abstract void initVocabMap() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException;

    protected boolean multiPick = false;
    protected Map<String, String> termInternalMap;
    protected Map<String, String> termDisplayMap;
    protected List<EnumParamTermNode> termTreeList;

    protected boolean quote = true;

    private List<ParamConfiguration> useTermOnlies = new ArrayList<ParamConfiguration>();
    protected boolean useTermOnly = false;

    private String displayType;

    public AbstractEnumParam() {}

    public AbstractEnumParam(AbstractEnumParam param) {
        super(param);
        this.multiPick = param.multiPick;
        if (param.termDisplayMap != null)
            this.termDisplayMap = new LinkedHashMap<String, String>(
                    param.termDisplayMap);
        if (param.termInternalMap != null)
            this.termInternalMap = new LinkedHashMap<String, String>(
                    param.termInternalMap);
        if (param.termTreeList != null) {
            this.termTreeList = new ArrayList<EnumParamTermNode>(
                    param.termTreeList);
        }
        this.quote = param.quote;
        this.useTermOnly = param.useTermOnly;
        this.displayType = param.displayType;
    }

    // ///////////////////////////////////////////////////////////////////
    // /////////// Public properties ////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////

    public void setMultiPick(Boolean multiPick) {
        this.multiPick = multiPick.booleanValue();
    }

    public Boolean getMultiPick() {
        return new Boolean(multiPick);
    }

    public void setQuote(boolean quote) {
        this.quote = quote;
    }

    public boolean getQuote() {
        return quote;
    }

    public void addUseTermOnly(ParamConfiguration paramConfig) {
        this.useTermOnlies.add(paramConfig);
    }

    @Override
    public String validateValue(Object value) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        // check if null value is allowed; if so, pass
        if (allowEmpty && value == null) return null;

        if (value == null || value.toString().length() == 0)
            return "Missing the value";

        // check if the value is string, if yes, try decompres it
        if (value instanceof String) {
            value = decompressValue((String) value);
        }
        String err = null;
        String[] values = (String[]) value;
        for (String val : values) {
            err = validateSingleValue(val);
            if (err != null) break;
        }
        return err;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#getInternalValue(java.lang.String)
     */
    @Override
    public String getInternalValue(Object termList) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        // check if null value is allowed
        if (allowEmpty && termList == null) return getEmptyValue();

        // the input is a list of terms
        String[] terms = (String[]) decompressValue((String) termList);
        initVocabMap();
        StringBuffer buf = new StringBuffer();
        for (String term : terms) {
            // verify the term
            if (!termInternalMap.containsKey(term))
                throw new WdkModelException("The term '" + term
                        + "' does not exist in param " + getFullName());

            String internal = useTermOnly ? term : termInternalMap.get(term);
            if (quote) internal = "'" + internal + "'";
            if (buf.length() != 0) buf.append(", ");
            buf.append(internal);
        }
        return buf.toString();
    }

    public String[] getVocab() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        initVocabMap();
        String[] array = new String[termInternalMap.size()];
        termInternalMap.keySet().toArray(array);
        return array;
    }

    public EnumParamTermNode[] getVocabTreeRoots()
            throws NoSuchAlgorithmException, WdkModelException, SQLException,
            JSONException, WdkUserException {
        initVocabMap();
        EnumParamTermNode[] array = new EnumParamTermNode[termTreeList.size()];
        termTreeList.toArray(array);
        return array;
    }

    public String[] getVocabInternal() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        initVocabMap();
        String[] array = new String[termInternalMap.size()];
        if (useTermOnly) termInternalMap.keySet().toArray(array);
        else termInternalMap.values().toArray(array);
        return array;
    }

    public String[] getDisplays() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        Map<String, String> displayMap = getDisplayMap();
        String[] displays = new String[displayMap.size()];
        displayMap.values().toArray(displays);
        return displays;
    }

    public Map<String, String> getVocabMap() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        initVocabMap();
        Map<String, String> newVocabMap = new LinkedHashMap<String, String>();
        for (String term : termInternalMap.keySet()) {
            newVocabMap.put(term, useTermOnly ? term
                    : termInternalMap.get(term));
        }
        return newVocabMap;
    }

    public Map<String, String> getDisplayMap() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        initVocabMap();
        Map<String, String> newDisplayMap = new LinkedHashMap<String, String>();
        for (String term : termDisplayMap.keySet()) {
            newDisplayMap.put(term, termDisplayMap.get(term));
        }
        return newDisplayMap;
    }

    @Override
    public String getDefault() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        if (defaultValue != null) return defaultValue;
        String[] vocab = getVocab();
        if (vocab.length == 0) return null;
        return vocab[0];
    }

    /**
     * @return the useTermOnly
     */
    public boolean isUseTermOnly() {
        return this.useTermOnly;
    }

    /**
     * @param useTermOnly
     */
    public void setUseTermOnly(boolean useTermOnly) {
        this.useTermOnly = useTermOnly;
    }

    /**
     * @return the displayType
     */
    public String getDisplayType() {
        return displayType;
    }

    /**
     * @param displayType
     *            the displayType to set
     */
    public void setDisplayType(String displayType) {
        this.displayType = displayType;
    }

    // ///////////////////////////////////////////////////////////////////
    // /////////// Protected properties ////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#compressValue(java.lang.Object)
     */
    @Override
    public String compressValue(Object value) throws WdkModelException,
            NoSuchAlgorithmException {
        if (value instanceof String[]) {
            String[] values = (String[]) value;
            value = Utilities.fromArray(values);
        }
        return super.compressValue(value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#decompressValue(java.lang.String)
     */
    @Override
    public Object decompressValue(String value) throws WdkModelException {
        // logger.info( "decompressing: '" + value + "'" );

        // check if the value is compressed; that is, if it has a compression
        // prefix
        if (value.startsWith(Utilities.COMPRESSED_VALUE_PREFIX)) {

            // decompress the value
            String checksum = value.substring(
                    Utilities.COMPRESSED_VALUE_PREFIX.length()).trim();
            value = queryFactory.getClobValue(checksum);
        }
        String[] values;
        if (multiPick) {
            values = value.split(",");
            for (int i = 0; i < values.length; i++)
                values[i] = values[i].trim();
        } else values = new String[] { value.trim() };
        return values;
    }

    protected String validateSingleValue(Object value)
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
        initVocabMap();

        if (termInternalMap.containsKey(value)) {
            return null;
        }
        if (value == null || value.toString().trim().length() == 0) {
            return " - Please choose value(s) for parameter '" + name + "'";
        } else {
            return " - Invalid value '" + value + "' for parameter '" + name
                    + "'";
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        super.excludeResources(projectId);

        // exclude userTermOnly
        boolean hasUseTermOnly = false;
        for (ParamConfiguration paramConfig : useTermOnlies) {
            if (paramConfig.include(projectId)) {
                if (hasUseTermOnly) {
                    throw new WdkModelException("More than one <useTermOnly> "
                            + "are included in param " + getFullName());
                } else {
                    this.useTermOnly = paramConfig.isValue();
                    hasUseTermOnly = true;
                }
            }
        }
        // if no useTermOnly setting, use parent's
        if (!hasUseTermOnly) useTermOnly = paramSet.isUseTermOnly();
        useTermOnlies = null;
    }

    protected void initTreeMap(Map<String, String> termParentMap)
            throws WdkModelException {
        termTreeList = new ArrayList<EnumParamTermNode>();

        // construct index
        Map<String, EnumParamTermNode> indexMap = new LinkedHashMap<String, EnumParamTermNode>();
        for (String term : termParentMap.keySet()) {
            EnumParamTermNode node = new EnumParamTermNode(term);
            node.setDisplay(termDisplayMap.get(term));
            indexMap.put(term, node);

            // check if the node is root
            String parentTerm = termParentMap.get(term);
            if (parentTerm != null && !termInternalMap.containsKey(parentTerm))
                parentTerm = null;
            if (parentTerm == null) {
                termTreeList.add(node);
                termParentMap.put(term, parentTerm);
            }
        }
        // construct the relationships
        for (String term : termParentMap.keySet()) {
            String parentTerm = termParentMap.get(term);
            // skip if parent doesn't exist
            if (parentTerm == null) continue;

            EnumParamTermNode node = indexMap.get(term);
            EnumParamTermNode parent = indexMap.get(parentTerm);
            parent.addChild(node);
        }
    }
}
