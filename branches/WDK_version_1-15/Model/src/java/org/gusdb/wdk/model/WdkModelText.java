/**
 * 
 */
package org.gusdb.wdk.model;

/**
 * @author Jerric
 *
 */
public class WdkModelText extends WdkModelBase {

    private String text;

    /**
     * @return the text
     */
    public String getText() {
        return this.text;
    }

    /**
     * @param text the text to set
     */
    public void setText(String text) {
        this.text = text;
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) {
    // no resource held by it, do nothing
    }
}
