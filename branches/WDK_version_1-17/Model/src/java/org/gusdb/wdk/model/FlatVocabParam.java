package org.gusdb.wdk.model;

import java.util.LinkedHashMap;

public class FlatVocabParam extends AbstractEnumParam {

    protected Query query;
    protected String queryTwoPartName;
    
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

    // ///////////////////////////////////////////////////////////////////
    // /////////// Protected properties ////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////

    protected void resolveReferences(WdkModel model) throws WdkModelException {
        query = (Query) model.resolveReference(queryTwoPartName);
        query.resolveReferences(model);
        // here check query's columns
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
