/**
 * 
 */
package org.gusdb.wdk.model.xml;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.ModelSetI;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;

/**
 * @author Jerric
 * @created Oct 14, 2005
 */
public class XmlRecordClassSet extends WdkModelBase implements ModelSetI {

    private String name;

    private List<XmlRecordClass> recordClassList = new ArrayList<XmlRecordClass>();
    private Map<String, XmlRecordClass> recordClasses = new LinkedHashMap<String, XmlRecordClass>();

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
        recordClassList.add(recordClass);
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

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        // exclude recordClasses
        for (XmlRecordClass recordClass : recordClassList) {
            if (recordClass.include(projectId)) {
                recordClass.setRecordClassSet(this);
                recordClass.excludeResources(projectId);
                String rcName = recordClass.getName();
                if (recordClasses.containsKey(rcName))
                    throw new WdkModelException("The xmlRecordClass " + rcName
                            + " is duplicated in xmlRecordClassSet "
                            + this.name);
                recordClasses.put(rcName, recordClass);
            }
        }
        recordClassList = null;
    }
}
