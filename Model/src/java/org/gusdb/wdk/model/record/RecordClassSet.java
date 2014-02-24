package org.gusdb.wdk.model.record;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.ModelSetI;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;

/**
 * A set used to organize recordClasses in the model.
 * 
 * @author jerric
 * 
 */
public class RecordClassSet extends WdkModelBase implements ModelSetI {

  private List<RecordClass> recordClassList = new ArrayList<RecordClass>();
  private Map<String, RecordClass> recordClassMap = new LinkedHashMap<String, RecordClass>();
  private String name;

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  public RecordClass getRecordClass(String name) throws WdkModelException {
    RecordClass s = recordClassMap.get(name);
    if (s == null)
      throw new WdkModelException("RecordClass Set " + getName()
          + " does not include recordClass " + name);
    return s;
  }

  @Override
  public Object getElement(String name) {
    return recordClassMap.get(name);
  }

  public RecordClass[] getRecordClasses() {
    RecordClass[] array = new RecordClass[recordClassMap.size()];
    recordClassMap.values().toArray(array);
    return array;
  }
  
  public Map<String, RecordClass> getRecordClassMap() {
    return new LinkedHashMap<>(recordClassMap);
  }

  boolean hasRecordClass(RecordClass recordClass) {
    return recordClassMap.containsKey(recordClass.getName());
  }

  public void addRecordClass(RecordClass recordClass) {
    recordClass.setRecordClassSet(this);
    recordClassList.add(recordClass);
  }

  @Override
  public String toString() {
    String newline = System.getProperty("line.separator");
    StringBuffer buf = new StringBuffer("RecordClassSet: name='" + name + "'");
    buf.append(newline);
    Iterator<RecordClass> recordClassIterator = recordClassMap.values().iterator();
    while (recordClassIterator.hasNext()) {
      buf.append(newline);
      buf.append(":::::::::::::::::::::::::::::::::::::::::::::");
      buf.append(newline);
      buf.append(recordClassIterator.next()).append(newline);
    }

    return buf.toString();
  }

  @Override
  public void resolveReferences(WdkModel model) throws WdkModelException {
    if (name.length() == 0 || name.indexOf('\'') >= 0)
      throw new WdkModelException("recordClassSet name cannot be empty "
          + "or having single quotes: " + name);

    Iterator<RecordClass> recordClassIterator = recordClassMap.values().iterator();
    while (recordClassIterator.hasNext()) {
      RecordClass recordClass = recordClassIterator.next();
      recordClass.resolveReferences(model);
    }
  }

  @Override
  public void setResources(WdkModel model) throws WdkModelException {
    for (RecordClass recordClass : recordClassMap.values()) {
      recordClass.setResources(model);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
   */
  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    // exclude record classes
    for (RecordClass recordClass : recordClassList) {
      if (recordClass.include(projectId)) {
        recordClass.excludeResources(projectId);
        String rcName = recordClass.getName();
        if (recordClassMap.containsKey(rcName))
          throw new WdkModelException("RecordClass " + rcName
              + " already exists in recordClass set " + getName());
        recordClassMap.put(rcName, recordClass);
      }
    }
    recordClassList = null;
  }
}
