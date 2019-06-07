package org.gusdb.wdk.model.dataset;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;

public class DatasetParserReference extends WdkModelBase {

  private String _name;
  private String _display;
  private String _implementation;
  private List<WdkModelText> _propertyList = new ArrayList<>();
  private List<WdkModelText> _descriptionList = new ArrayList<>();

  private DatasetParser _parser;
  private Map<String, String> _properties = new HashMap<>();
  private String _description;

  public void setName(String name) {
    _name = name;
  }
  
  public String getName() {
    return _name;
  }

  public void setImplementation(String implementation) {
    _implementation = implementation;
  }

  public void addProperty(WdkModelText property) {
    _propertyList.add(property);
  }

  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    super.excludeResources(projectId);

    // exclude property resource
    for (WdkModelText property : _propertyList) {
      if (property.include(projectId)) {
        property.excludeResources(projectId);
        String name = property.getName();
        if (_properties.containsKey(name))
          throw new WdkModelException("Property name \"" + name
              + "\" duplicated.");
        _properties.put(name, property.getText());
      }
    }
    _propertyList = null;

    // exclude description resource
    for (WdkModelText description : _descriptionList) {
      if (description.include(projectId)) {
        if (_description != null)
          throw new WdkModelException("Description of the dataset param "
              + "parser is duplicated in " + _name);
        
        description.excludeResources(projectId);
        _description = description.getText();
      }
    }
    _descriptionList = null;
  }

  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    try {
      Class<? extends DatasetParser> parserClass = Class.forName(_implementation).asSubclass(DatasetParser.class);
      _parser = parserClass.getDeclaredConstructor().newInstance();
      _parser.setName(_name);
      if (_display != null) _parser.setDisplay(_display);
      if (_description != null) _parser.setDescription(_description);
      _parser.setProperties(_properties);
    }
    catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
        IllegalArgumentException | InvocationTargetException |
        NoSuchMethodException | SecurityException ex) {
      throw new WdkModelException(ex);
    }
  }

  public DatasetParser getParser() {
    return _parser;
  }
}
