package org.gusdb.wdk.model.record.attribute.plugin;

import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.user.Step;

/**
 * @author jerric
 * 
 *         The plugin should not hold per-request data, since it will be reused
 *         between requests.
 */
public interface AttributePlugin {
  
  String getName();

  void setName(String name);

  String getDisplay();

  void setDisplay(String display);

  String getView();

  void setView(String view);

  String getDescription();

  void setDescription(String description);

  void setProperties(Map<String, String> properties);

  void setAttributeField(AttributeField attribute);

  Map<String, Object> process(Step step) throws WdkModelException;

}
