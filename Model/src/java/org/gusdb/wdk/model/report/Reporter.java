/**
 * 
 */
package org.gusdb.wdk.model.report;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author xingao
 * 
 */
public abstract class Reporter implements IReporter {

    protected Map<String, String> config;

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.report.IReporter#setConfiguration(java.util.Map)
     */
    public void config(Map<String, String> config) {
        this.config = (config != null) ? config
                : new LinkedHashMap<String, String>();
    }

}
