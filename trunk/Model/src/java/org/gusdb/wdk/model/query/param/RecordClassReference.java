package org.gusdb.wdk.model.query.param;

import org.gusdb.wdk.model.WdkModelBase;

public class RecordClassReference extends WdkModelBase {

    String ref;

    public RecordClassReference() {}

    public RecordClassReference(String ref) {
        this.ref = ref;
    }

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
}
