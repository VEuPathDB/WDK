package org.gusdb.wdk.model;

import org.gusdb.wdk.model.RecordPage;

public class RecordPageParam extends Param {

    // ///////////////////////////////////////////////////////////////////
    // /////////// Public properties ////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////

    public RecordPageParam() {}

    public RecordPageParam(RecordPageParam param) {
        super(param);
    }

    public String validateValue(Object value) throws WdkModelException {
        if (!(value instanceof RecordPage)) {
            throw new WdkModelException("Value must be a RecordPage " + value);
        }
        return null;
    }
    

    // ///////////////////////////////////////////////////////////////
    // protected methods
    // ///////////////////////////////////////////////////////////////

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.Param#clone()
     */
    @Override
    public Param clone() {
        return new RecordPageParam(this);
    }

    protected void resolveReferences(WdkModel model) throws WdkModelException {}
}
