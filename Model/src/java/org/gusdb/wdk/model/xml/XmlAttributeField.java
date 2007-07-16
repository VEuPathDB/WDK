/**
 * 
 */
package org.gusdb.wdk.model.xml;

import org.gusdb.wdk.model.AttributeField;

/**
 * @author Jerric
 * @created Oct 11, 2005
 */
public class XmlAttributeField extends AttributeField {

    public XmlAttributeField() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String classnm = this.getClass().getName();
        StringBuffer buf = new StringBuffer(classnm);
        buf.append(": name='");
        buf.append(getName());
        buf.append("'\r\n  displayName='");
        buf.append(getDisplayName());
        buf.append("'\r\n  help='");
        buf.append(getHelp());
        buf.append("'\r\n");
        return buf.toString();
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) {
        // do nothing
    }
}
