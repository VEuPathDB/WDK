/**
 * 
 */
package org.gusdb.wdk.model;


/**
 * @author xingao
 * 
 */
public class AnswerFilterInstanceReference extends WdkModelBase {

    private String ref;

    private RecordClass recordClass;

    private AnswerFilterInstance instance;

    /**
     * @return the ref
     */
    public String getRef() {
        return ref;
    }

    /**
     * @param ref
     *            the ref to set
     */
    public void setRef(String ref) {
        this.ref = ref;
    }

    /**
     * @return the recordClass
     */
    public RecordClass getRecordClass() {
        return recordClass;
    }

    /**
     * @param recordClass
     *            the recordClass to set
     */
    void setRecordClass(RecordClass recordClass) {
        this.recordClass = recordClass;
    }

    /**
     * @return the instance
     */
    public AnswerFilterInstance getInstance() {
        return instance;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
    // nothing to exclude
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#resolveReferences(org.gusdb.wdk.model.WdkModel)
     */
    @Override
    public void resolveReferences(WdkModel wodkModel) throws WdkModelException {
        // resolve the instance reference
        this.instance = recordClass.getFilter(ref);
        if (instance == null) throw new WdkModelException("Filter doesn't exist: " + ref);
        resolved = true;
    }
}
