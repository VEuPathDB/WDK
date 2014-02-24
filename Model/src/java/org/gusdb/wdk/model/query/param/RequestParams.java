package org.gusdb.wdk.model.query.param;

import org.gusdb.wdk.model.WdkModelException;

public interface RequestParams {

  String getParam(String name);
  
  String[] getArray(String name);
  
  Object getAttribute(String name);
  
  String getUploadFileContent(String name) throws WdkModelException;
  
  void setParam(String name, String value);
  
  void setArray(String name, String[] array);
  
  void setAttribute(String name, Object value);
}
