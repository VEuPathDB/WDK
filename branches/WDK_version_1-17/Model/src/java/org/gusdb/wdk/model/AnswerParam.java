package org.gusdb.wdk.model;

import org.gusdb.wdk.model.Answer;

public class AnswerParam extends Param {

    // ///////////////////////////////////////////////////////////////////
    // /////////// Public properties ////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////

    public AnswerParam() {}

    public AnswerParam(AnswerParam param) {
        super(param);
    }

    public String validateValue(Object value) throws WdkModelException {
        if (!(value instanceof Answer)) {
            throw new WdkModelException("Value must be a Answer " + value);
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
        return new AnswerParam(this);
    }

    protected void resolveReferences(WdkModel model) throws WdkModelException {}
}
