package org.gusdb.wdk.model;

import java.util.LinkedHashMap;

public class FlatVocabParam extends AbstractEnumParam {
    
    Query query;
    String queryTwoPartName;

    public FlatVocabParam () {
	super();
    }

    /////////////////////////////////////////////////////////////////////
    /////////////  Public properties ////////////////////////////////////
    /////////////////////////////////////////////////////////////////////

    public void setQueryRef(String queryTwoPartName){

	this.queryTwoPartName = queryTwoPartName;
    }

    public Query getQuery() {
	return query;
    }


    /////////////////////////////////////////////////////////////////////
    /////////////  Protected properties ////////////////////////////////////
    /////////////////////////////////////////////////////////////////////

    protected void resolveReferences(WdkModel model) throws WdkModelException {
	query = (Query)model.resolveReference(queryTwoPartName, name, "flatVocabParam", "queryRef");
	query.resolveReferences(model);
	// here check query's columns
    }

    public void setResources(WdkModel model)throws WdkModelException {
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

}
