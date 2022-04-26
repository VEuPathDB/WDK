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
 */
public class XmlRecordClassSet extends WdkModelBase implements ModelSetI<XmlRecordClass> {

    private String _name;

    private List<XmlRecordClass> _recordClassList = new ArrayList<XmlRecordClass>();
    private Map<String, XmlRecordClass> _recordClasses = new LinkedHashMap<String, XmlRecordClass>();

    @Override
    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public XmlRecordClass getRecordClass(String name) throws WdkModelException {
        XmlRecordClass recordClass = _recordClasses.get(name);
        if (recordClass == null)
            throw new WdkModelException("RecordClass \"" + name
                    + "\" not found in set " + getName());
        return recordClass;
    }

    public XmlRecordClass[] getRecordClasses() {
        XmlRecordClass[] rcArray = new XmlRecordClass[_recordClasses.size()];
        _recordClasses.values().toArray(rcArray);
        return rcArray;
    }

    public void addRecordClass(XmlRecordClass recordClass) {
        _recordClassList.add(recordClass);
    }

    @Override
    public XmlRecordClass getElement(String elementName) {
        return _recordClasses.get(elementName);
    }

    @Override
    public void setResources(WdkModel model) throws WdkModelException {
        for (XmlRecordClass recordClass : _recordClasses.values()) {
            recordClass.setRecordClassSet(this);
        }
    }

    @Override
    public void resolveReferences(WdkModel model) throws WdkModelException {
        for (XmlRecordClass recordClass : _recordClasses.values()) {
            recordClass.resolveReferences(model);
        }
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer("XmlRecordClassSet: name='");
        buf.append(_name);
        for (XmlRecordClass rc : _recordClasses.values()) {
            buf.append("\r\n:::::::::::::::::::::::::::::::::::::::::::::\r\n");
            buf.append(rc);
            buf.append("\r\n");
        }
        return buf.toString();
    }

    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        // exclude recordClasses
        for (XmlRecordClass recordClass : _recordClassList) {
            if (recordClass.include(projectId)) {
                recordClass.setRecordClassSet(this);
                recordClass.excludeResources(projectId);
                String rcName = recordClass.getName();
                if (_recordClasses.containsKey(rcName))
                    throw new WdkModelException("The xmlRecordClass " + rcName
                            + " is duplicated in xmlRecordClassSet "
                            + _name);
                _recordClasses.put(rcName, recordClass);
            }
        }
        _recordClassList = null;
    }
}
