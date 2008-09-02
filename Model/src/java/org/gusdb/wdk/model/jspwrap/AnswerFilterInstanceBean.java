/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.AnswerFilterInstance;

/**
 * @author SYSTEM
 * 
 */
public class AnswerFilterInstanceBean {

    private AnswerFilterInstance instance;

    AnswerFilterInstanceBean(AnswerFilterInstance instance) {
        this.instance = instance;
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.AnswerFilterInstance#getDescription()
     */
    public String getDescription() {
        return instance.getDescription();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.AnswerFilterInstance#getDisplayName()
     */
    public String getDisplayName() {
        return instance.getDisplayName();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.AnswerFilterInstance#getName()
     */
    public String getName() {
        return instance.getName();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.AnswerFilterInstance#isBooleanDefault()
     */
    public boolean isBooleanDefault() {
        return instance.isBooleanDefault();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.AnswerFilterInstance#isDefault()
     */
    public boolean isDefault() {
        return instance.isDefault();
    }

}
