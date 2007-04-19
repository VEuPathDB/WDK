package org.gusdb.wdk.model.user;

import java.util.LinkedHashMap;
import java.util.Map;

public class QueryInfo {

    private String queryFullName;
    private String queryChecksum;
    private boolean isBoolean;
    private Map<String, String> paramValues;

    /**
     * 
     */
    QueryInfo(String queryFullName, String queryChecksum,
            boolean isBoolean) {
        this.queryFullName = queryFullName;
        this.queryChecksum = queryChecksum;
        this.isBoolean = isBoolean;
        paramValues = new LinkedHashMap<String, String>();
    }

    void addParamValue(String param, String value) {
        paramValues.put(param, value);
    }

    public String getQueryFullName() {
        return this.queryFullName;
    }

    public String getQueryChecksum() {
        return this.queryChecksum;
    }

    public boolean isBoolean() {
        return isBoolean;
    }

    public Map<String, String> getParamValues() {
        return paramValues;
    }
}
