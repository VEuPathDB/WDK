package org.gusdb.wdk.model.dataset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;

public class DatasetParserReference extends WdkModelBase {

  private String name;
  private String display;
  private String implementation;
  private List<WdkModelText> propertyList = new ArrayList<>();
  private List<WdkModelText> descriptionList = new ArrayList<>();

  private DatasetParser parser;
  private Map<String, String> properties = new HashMap<>();
  private String description;

  public void setName(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }

  public void setImplementation(String implementation) {
    this.implementation = implementation;
  }

  public void addProperty(WdkModelText property) {
    propertyList.add(property);
  }

  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    super.excludeResources(projectId);

    // exclude property resource
    for (WdkModelText property : propertyList) {
      if (property.include(projectId)) {
        property.excludeResources(projectId);
        String name = property.getName();
        if (properties.containsKey(name))
          throw new WdkModelException("Property name \"" + name
              + "\" duplicated.");
        properties.put(name, property.getText());
      }
    }
    propertyList = null;

    // exclude description resource
    for (WdkModelText description : descriptionList) {
      if (description.include(projectId)) {
        if (this.description != null)
          throw new WdkModelException("Description of the dataset param "
              + "parser is duplicated in " + this.name);
        
        description.excludeResources(projectId);
        this.description = description.getText();
      }
    }
    this.descriptionList = null;
  }

  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    try {
      Class<? extends DatasetParser> parserClass = Class.forName(implementation).asSubclass(DatasetParser.class);
      parser = parserClass.newInstance();
      parser.setName(name);
      if (display != null) parser.setDisplay(display);
      if (description != null) parser.setDescription(description);
      parser.setProperties(properties);
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      throw new WdkModelException(ex);
    }
  }

  public DatasetParser getParser() {
    return parser;
  }
}
