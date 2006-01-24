/**
 * 
 */
package org.gusdb.wdk.model.xml;

import java.util.HashMap;
import java.util.Map;

import org.gusdb.wdk.model.ModelSetI;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

/**
 * @author Jerric
 * @created Oct 14, 2005
 */
public class XmlRecordClassSet implements ModelSetI {

    private String name;
    private Map<String, XmlRecordClass> recordClasses;

    /**
     * 
     */
    public XmlRecordClassSet() {
        recordClasses = new HashMap<String, XmlRecordClass>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.ModelSetI#getName()
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public XmlRecordClass getRecordClass(String name) throws WdkModelException {
        XmlRecordClass recordClass = recordClasses.get(name);
        if (recordClass == null)
            throw new WdkModelException("RecordClass \"" + name
                    + "\" not found in set " + getName());
        return recordClass;
    }

    public XmlRecordClass[] getRecordClasses() {
        XmlRecordClass[] rcArray = new XmlRecordClass[recordClasses.size()];
        recordClasses.values().toArray(rcArray);
        return rcArray;
    }

    public void addRecordClass(XmlRecordClass recordClass) {
        recordClasses.put(recordClass.getName(), recordClass);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.ModelSetI#getElement(java.lang.String)
     */
    public Object getElement(String elementName) {
        return recordClasses.get(elementName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.ModelSetI#setResources(org.gusdb.wdk.model.WdkModel)
     */
    public void setResources(WdkModel model) throws WdkModelException {
        for (XmlRecordClass recordClass : recordClasses.values()) {
            recordClass.setRecordClassSet(this);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.ModelSetI#resolveReferences(org.gusdb.wdk.model.WdkModel)
     */
    public void resolveReferences(WdkModel model) throws WdkModelException {
        for (XmlRecordClass recordClass : recordClasses.values()) {
            recordClass.resolveReferences(model);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer("XmlRecordClassSet: name='");
        buf.append(name);
        for (XmlRecordClass rc : recordClasses.values()) {
            buf.append("\r\n:::::::::::::::::::::::::::::::::::::::::::::\r\n");
            buf.append(rc);
            buf.append("\r\n");
        }
        return buf.toString();
    }
}
