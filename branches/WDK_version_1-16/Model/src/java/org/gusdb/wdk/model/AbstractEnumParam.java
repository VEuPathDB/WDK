package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractEnumParam extends Param {

    protected boolean multiPick = false;
    protected Map<String, String> vocabMap;
    protected boolean quote = true;

    private List<ParamConfiguration> useTermOnlies =
            new ArrayList<ParamConfiguration>();
    protected boolean useTermOnly = false;
    
    private String displayType;

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

    public String validateValue(Object value) throws WdkModelException {
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
    public String getInternalValue(String termList) throws WdkModelException {
        // check if null value is allowed
        if (allowEmpty && termList == null) return getEmptyValue();

        // the input is a list of terms
        String[] terms = (String[]) decompressValue(termList);
        initVocabMap();
        StringBuffer buf = new StringBuffer();
        for (String term : terms) {
            // verify the term
            if (!vocabMap.containsKey(term))
                throw new WdkModelException("The term " + term
                        + " does not exist in param " + getFullName());

            String internal = useTermOnly ? term : vocabMap.get(term);
            if (quote) internal = "'" + internal + "'";
            if (buf.length() != 0) buf.append(", ");
            buf.append(internal);
        }
        return buf.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#resolveReferences(org.gusdb.wdk.model.WdkModel)
     */
    @Override
    protected void resolveReferences(WdkModel model) throws WdkModelException {
    // TODO Auto-generated method stub

    }

    public String[] getVocab() throws WdkModelException {
        initVocabMap();
        String[] array = new String[vocabMap.size()];
        vocabMap.keySet().toArray(array);
        return array;
    }

    public String[] getVocabInternal() throws WdkModelException {
        initVocabMap();
        String[] array = new String[vocabMap.size()];
        if (useTermOnly) vocabMap.keySet().toArray(array);
        else vocabMap.values().toArray(array);
        return array;
    }

    public Map<String, String> getVocabMap() throws WdkModelException {
        initVocabMap();
        Map<String, String> newVocabMap = new LinkedHashMap<String, String>();
        for (String term : vocabMap.keySet()) {
            newVocabMap.put(term, useTermOnly ? term : vocabMap.get(term));
        }
        return newVocabMap;
    }

    public String getDefault() throws WdkModelException {
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
     * @param displayType the displayType to set
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
    public String compressValue(Object value) throws WdkModelException {
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
            String checksum =
                    value.substring(Utilities.COMPRESSED_VALUE_PREFIX.length()).trim();
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

    protected abstract void initVocabMap() throws WdkModelException;

    protected String validateSingleValue(Object value) throws WdkModelException {
        initVocabMap();

        if (vocabMap.containsKey(value)) {
            return null;
        }
        if (value == null || value.toString().trim().length() == 0) {
            return " - Please choose value(s) for parameter '" + name + "'";
        } else {
            return " - Invalid value '" + value + "' for parameter '" + name
                    + "'";
        }
    }

    protected void clone(AbstractEnumParam param) {
        super.clone(param);
        param.multiPick = multiPick;
        if (vocabMap != null) {
            if (param.vocabMap == null)
                param.vocabMap = new LinkedHashMap<String, String>();
            param.vocabMap.putAll(vocabMap);
        }
        param.quote = quote;
        param.useTermOnly = useTermOnly;
        param.displayType = displayType;
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
}
