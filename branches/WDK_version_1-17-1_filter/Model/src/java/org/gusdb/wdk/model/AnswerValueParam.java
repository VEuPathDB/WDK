package org.gusdb.wdk.model;

import org.gusdb.wdk.model.AnswerValue;

public class AnswerValueParam extends Param {

    // ///////////////////////////////////////////////////////////////////
    // /////////// Public properties ////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////

    public AnswerValueParam() {}

    public AnswerValueParam(AnswerValueParam param) {
        super(param);
    }

    public String validateValue(Object value) throws WdkModelException {
        if (!(value instanceof AnswerValue)) {
            throw new WdkModelException("Value must be a AnswerValue " + value);
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
        return new AnswerValueParam(this);
    }

    protected void resolveReferences(WdkModel model) throws WdkModelException {}
}
