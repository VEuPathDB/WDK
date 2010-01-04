/**
 * 
 */
package org.gusdb.wdk.model.query.param;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xingao
 * 
 */
public class EnumParamTermNode {

    private String term;
    private String display;
    private List<EnumParamTermNode> children;

    /**
     * 
     */
    public EnumParamTermNode(String term) {
        this.term = term;
        children = new ArrayList<EnumParamTermNode>();
    }

    /**
     * @return
     */
    public String getTerm() {
        return term;
    }

    /**
     * @param child
     */
    void addChild(EnumParamTermNode child) {
        children.add(child);
    }

    /**
     * @return
     */
    public EnumParamTermNode[] getChildren() {
        EnumParamTermNode[] array = new EnumParamTermNode[children.size()];
        children.toArray(array);
        return array;
    }

    /**
     * @return the display
     */
    public String getDisplay() {
        return display;
    }

    /**
     * @param display
     *            the display to set
     */
    public void setDisplay(String display) {
        this.display = display;
    }
}
