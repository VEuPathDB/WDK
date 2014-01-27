package org.gusdb.wdk.model.query.param;

import org.gusdb.wdk.model.WdkModelException;

public interface RequestParams {

  String getParam(String name);
  
  String[] getArray(String name);
  
  String getUploadFileContent(String name) throws WdkModelException;
}
