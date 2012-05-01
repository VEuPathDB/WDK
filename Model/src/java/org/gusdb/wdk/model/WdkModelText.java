/**
 * 
 */
package org.gusdb.wdk.model;


/**
 * @author Jerric
 * 
 */
public class WdkModelText extends WdkModelBase {

    protected String name;
    private String text;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the text
     */
    public String getText() {
        return this.text;
    }

    /**
     * @param text
     *            the text to set
     */
    public void setText(String text) {
        this.text = text;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) {
    // no resource held by it, do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.WdkModelBase#resolveReferences(org.gusdb.wdk.model
     * .WdkModel)
     */
    @Override
    public void resolveReferences(WdkModel wodkModel) {
    // have nothing to do
    }
}
