package org.gusdb.wdk.model.query.param;

import java.util.List;

public interface DatasetParser {

  /**
   * A unique name of a parser. The value is mapped to the value of type
   * sub-param.
   * 
   * @return
   */
  String getName();

  /**
   * The display of the parser, the value is mapped to display of the type
   * sub-param.
   * 
   * @return
   */
  String getDisplay();
  
  void addProperty(String propName, String propValue);

  List<String[]> parse(String content);
}
