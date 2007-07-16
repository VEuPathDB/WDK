package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class FlatVocabParam extends AbstractEnumParam {

    protected Query query;
    protected String queryTwoPartName;

    private List<ParamConfiguration> useTermOnlies;

    public FlatVocabParam() {
        useTermOnlies = new ArrayList<ParamConfiguration>();
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

    public void addUseTermOnly(ParamConfiguration paramConfig) {
        this.useTermOnlies.add(paramConfig);
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
        if (vocabMap == null) {
            vocabMap = new LinkedHashMap<String, String>();
            QueryInstance instance = query.makeInstance();
            ResultList result = instance.getResult();
            while (result.next()) {
                String term = result.getValue("term").toString();
                String value = result.getValue("internal").toString();
                vocabMap.put(term, value);
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

    /**
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) {
        super.excludeResources(projectId);

        // exclude userTermOnly
        boolean hasUseTermOnly = false;
        for (ParamConfiguration paramConfig : useTermOnlies) {
            if (paramConfig.include(projectId)) {
                this.useTermOnly = paramConfig.isValue();
                hasUseTermOnly = true;
                break;
            }
        }
        // if no useTermOnly setting, use parent's
        if (!hasUseTermOnly) useTermOnly = paramSet.isUseTermOnly();
        useTermOnlies = null;
    }
}
