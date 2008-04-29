/**
 * 
 */
package org.gusdb.wdk.model;

import org.gusdb.wdk.model.implementation.SqlQuery;

/**
 * @author xingao
 * 
 */
public class SubType extends WdkModelBase {

    private String filterQueryRef;
    private String transformQueryRef;
    private String termToSkip;

    private SqlQuery filterQuery;
    private SqlQuery transformQuery;

    private StringParam resultParam;
    private AbstractEnumParam subTypeParam;

    /**
     * @return the questionOnly
     */
    public boolean isQuestionOnly() {
        return (transformQuery == null);
    }

    /**
     * @param transformQueryRef
     *            the transformQueryRef to set
     */
    public void setTransformQueryRef(String transformQueryRef) {
        this.transformQueryRef = transformQueryRef;
    }

    public SqlQuery getTransformQuery() throws WdkModelException {
        if (filterQuery == null)
            throw new WdkModelException("The transform query reference '"
                    + transformQueryRef + "' has not been resolved yet");
        return transformQuery;
    }

    public void setFilterQueryRef(String filterQueryRef) {
        this.filterQueryRef = filterQueryRef;
    }

    public SqlQuery getFilterQuery() throws WdkModelException {
        if (filterQuery == null)
            throw new WdkModelException("The filter query reference '"
                    + filterQueryRef + "' has not been resolved yet");
        return filterQuery;
    }

    /**
     * @return the subTypeIgnoreValue
     */
    public String getTermToSkip() {
        return termToSkip;
    }

    /**
     * @param subTypeIgnoreValue
     *            the subTypeIgnoreValue to set
     */
    public void setTermToSkip(String termToSkip) {
        this.termToSkip = termToSkip;
    }

    public AbstractEnumParam getSubTypeParam() {
        return subTypeParam;
    }

    public StringParam getResultParam() {
        return resultParam;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        // nothing to be done here.
    }

    void resolveReferences(WdkModel wdkModel) throws WdkModelException {
        // resolve the filter query reference
        filterQuery = (SqlQuery) wdkModel.resolveReference(filterQueryRef);

        // make sure there's only two params, and one is AbstractEnumParam, and
        // the other is a historyParam
        Param[] params = filterQuery.getParams();
        String errmsg = "The filter query '" + filterQueryRef + "' must have "
                + "two params, and one of them is an enumParam or "
                + "flatVocabParam, and the other is a stringParam";

        if (params.length != 2) throw new WdkModelException(errmsg);
        else if (params[0] instanceof AbstractEnumParam
                && params[1] instanceof StringParam) {
            this.resultParam = (StringParam) params[1];
            this.subTypeParam = (AbstractEnumParam) params[0];
        } else if (params[0] instanceof StringParam
                && params[1] instanceof AbstractEnumParam) {
            this.resultParam = (StringParam) params[0];
            this.subTypeParam = (AbstractEnumParam) params[1];
        } else throw new WdkModelException(errmsg);

        // make sure the ignore value exists
        if (!subTypeParam.getVocabMap().containsKey(termToSkip))
            throw new WdkModelException("The ignoreValue, '" + termToSkip
                    + "', is not a term in param '" + subTypeParam.getName());

        // resolve the transform query reference if exists
        if (transformQueryRef != null) {
            transformQuery = (SqlQuery) wdkModel.resolveReference(transformQueryRef);

            // make sure it has those two params too
            errmsg = "The transform query '" + transformQueryRef + "' must "
                    + "have at least one string param named as '"
                    + resultParam.getName() + "'.The rest of the params "
                    + "should allow empty values.";

            Param param = transformQuery.getParam(resultParam.getName());
            if (param == null || !(param instanceof StringParam))
                throw new WdkModelException(errmsg);
        }
    }
}
