/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.xml.XmlRecordClass;
import org.gusdb.wdk.model.xml.XmlRecordClassSet;

/**
 * @author Jerric
 * @created Oct 20, 2005
 */
public class XmlRecordClassSetBean {

    private XmlRecordClassSet recordClassSet;

    /**
     * 
     */
    public XmlRecordClassSetBean(XmlRecordClassSet recordClassSet) {
        this.recordClassSet = recordClassSet;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlRecordClassSet#getName()
     */
    public String getName() {
        return this.recordClassSet.getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlRecordClassSet#getRecordClasses()
     */
    public XmlRecordClassBean[] getRecordClasses() {
        XmlRecordClass[] rcs = recordClassSet.getRecordClasses();
        XmlRecordClassBean[] rcBeans = new XmlRecordClassBean[rcs.length];
        for (int i = 0; i < rcs.length; i++) {
            rcBeans[i] = new XmlRecordClassBean(rcs[i]);
        }
        return rcBeans;
    }

}
