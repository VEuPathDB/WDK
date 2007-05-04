package org.gusdb.wdk.model.user;

import java.util.LinkedHashMap;
import java.util.Map;

public class QueryInfo {

    private String queryFullName;
    private String queryChecksum;
    private String querySignature;
    private boolean isBoolean;
    private Map<String, Object> paramValues;

    /**
     * 
     */
    QueryInfo(String queryFullName, String queryChecksum, String querySignature,
            boolean isBoolean) {
        this.queryFullName = queryFullName;
        this.queryChecksum = queryChecksum;
        this.querySignature = querySignature;
        this.isBoolean = isBoolean;
        paramValues = new LinkedHashMap<String, Object>();
    }

    void addParamValue(String param, Object value) {
        paramValues.put(param, value);
    }

    public String getQueryFullName() {
        return this.queryFullName;
    }

    /**
     * @return The checksum uniquely identifies this query instance, including the info of project id, query full name, and parameter-value pairs (in ascending order)
     */
    public String getQueryChecksum() {
        return this.queryChecksum;
    }
    
    /**
     * @return The signature uniquely identifies the query, including the info of project id, query full name, and parameter list (in ascending order)
     */
    public String getQuerySignature() {
        return this.querySignature;
    }

    public boolean isBoolean() {
        return isBoolean;
    }

    public Map<String, Object> getParamValues() {
        return paramValues;
    }
}
