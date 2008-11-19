/**
 * 
 */
package org.gusdb.wdk.model.query.param;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;

/**
 * @author Jerric
 * 
 */
public class ParamSuggestion extends WdkModelBase {

    private String sample;
    private String defaultValue;
    private boolean allowEmpty;

    /**
     * the default constructor is used by the digester
     */
    public ParamSuggestion() {}

    /**
     * the copy constructor is used by the clone methods
     */
    public ParamSuggestion(ParamSuggestion suggestion) {
        this.sample = suggestion.sample;
        this.defaultValue = suggestion.defaultValue;
        this.allowEmpty = suggestion.allowEmpty;
    }

    /**
     * @return the allowEmpty
     */
    public boolean isAllowEmpty() {
        return this.allowEmpty;
    }

    /**
     * @param allowEmpty
     *        the allowEmpty to set
     */
    public void setAllowEmpty(boolean allowEmpty) {
        this.allowEmpty = allowEmpty;
    }

    /**
     * @return the defaultValue
     */
    public String getDefault() {
        return this.defaultValue;
    }

    /**
     * @param defaultValue
     *        the defaultValue to set
     */
    public void setDefault(String defaultValue) {
        this.defaultValue = (defaultValue.trim().length() == 0) ? null
                : defaultValue;
    }

    /**
     * @return the sample
     */
    public String getSample() {
        return this.sample;
    }

    /**
     * @param sample
     *        the sample to set
     */
    public void setSample(String sample) {
        this.sample = sample;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) {
    // do nothing
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.WdkModelBase#resolveReferences(org.gusdb.wdk.model.WdkModel)
     */
    @Override
    public void resolveReferences(WdkModel wodkModel) throws WdkModelException {
        // do nothing
    }
}
