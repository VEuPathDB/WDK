package org.gusdb.wdk.model.dataset;

import java.util.List;
import java.util.Map;

public interface DatasetParser {

  String getName();
  
  void setName(String name);
  
  String getDisplay();
  
  void setDisplay(String display);
  
  String getDescription();
  
  void setDescription(String description);
  
  void setProperties(Map<String, String> properties);
  
  List<String[]> parse(String content) throws WdkDatasetException;
}
