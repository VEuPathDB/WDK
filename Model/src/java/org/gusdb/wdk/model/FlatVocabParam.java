package org.gusdb.wdk.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class FlatVocabParam extends AbstractEnumParam {

    public static final String PARAM_SERVED_QUERY = "ServedQuery";
    private Query query;
    private String queryTwoPartName;
    private String servedQueryName;
    
    public FlatVocabParam() {}
    
    public FlatVocabParam(FlatVocabParam param) {
        super(param);
        this.query = param.query;
        this.queryTwoPartName = param.queryTwoPartName;
    }

    // ///////////////////////////////////////////////////////////////////
    // /////////// Public properties ////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////

    public void setQueryRef(String queryTwoPartName) {

        this.queryTwoPartName = queryTwoPartName;
    }

    public Query getQuery() {
        return query;
    }

    /**
     * @param servedQueryName the servedQueryName to set
     */
    public void setServedQueryName(String servedQueryName) {
        this.servedQueryName = servedQueryName;
    }

    // ///////////////////////////////////////////////////////////////////
    // /////////// Protected properties ////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////

    protected void resolveReferences(WdkModel model) throws WdkModelException {
        query = (Query) model.resolveReference(queryTwoPartName);
        
        // add a served query param into flatVocabQuery, if it doesn't exist
        if (null == query.getParam(PARAM_SERVED_QUERY)) {
            StringParam param = new StringParam();
            param.setName(PARAM_SERVED_QUERY);
            param.setDefault(servedQueryName);
            param.setAllowEmpty(true);
            query.addParam(param);
        }
    }

    public void setResources(WdkModel model) throws WdkModelException {
        super.setResources(model);
        query.setResources(model);
    }

    protected void initVocabMap() throws WdkModelException {

        if (termInternalMap == null) {
            termInternalMap = new LinkedHashMap<String, String>();
            termDisplayMap = new LinkedHashMap<String, String>();

            // check if the query has "display" column
            boolean hasDisplay = query.getColumnMap().containsKey("display");

            QueryInstance instance = query.makeInstance();
            
            // assign param value
            Map<String, Object> values = new LinkedHashMap<String, Object>();
            values.put(PARAM_SERVED_QUERY, servedQueryName);
            instance.setValues(values);
            ResultList result = instance.getResult();
            while (result.next()) {
                String term = result.getValue("term").toString();
                String value = result.getValue("internal").toString();
                String display = hasDisplay ? result.getValue("display").toString()
                        : term;
                termInternalMap.put(term, value);
                termDisplayMap.put(term, display);
            }
            result.close();
        }
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.Param#clone()
     */
    @Override
    public Param clone() {
        return new FlatVocabParam(this);
    }
}
