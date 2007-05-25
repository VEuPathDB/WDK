package org.gusdb.wdk.model;

import java.util.LinkedHashMap;

public class FlatVocabParam extends AbstractEnumParam {

    protected Query query;
    protected String queryTwoPartName;

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
        query = (Query) model.resolveReference(queryTwoPartName, name,
                "flatVocabParam", "queryRef");
        query.resolveReferences(model);
        // here check query's columns
    }

    public void setResources(WdkModel model) throws WdkModelException {
        super.setResources( model );
        query.setResources(model);
    }

    protected void initVocabMap() throws WdkModelException {
        if (vocabMap == null) {
            vocabMap = new LinkedHashMap();
            QueryInstance instance = query.makeInstance();
            ResultList result = instance.getResult();
            while (result.next()) {
                Object value = result.getValue("term");
                orderedKeySet.add(value);
                vocabMap.put(value, result.getValue("internal"));
            }
            result.close();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#clone()
     */
    @Override
    public Param clone() {
        FlatVocabParam param = new FlatVocabParam();
        super.clone(param);
        param.query = query;
        param.queryTwoPartName = queryTwoPartName;
        return param;
    }
}
