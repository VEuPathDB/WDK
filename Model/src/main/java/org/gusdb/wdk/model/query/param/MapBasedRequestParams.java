package org.gusdb.wdk.model.query.param;

import java.util.HashMap;

import org.gusdb.wdk.model.WdkModelException;

public class MapBasedRequestParams extends HashMap<String,String> implements RequestParams {

  private static final long serialVersionUID = 1L;

  @Override
  public RequestParams setParam(String name, String value) {
    put(name, value);
    return this;
  }

  @Override
  public String getParam(String name) {
    return get(name);
  }

  @Override
  public String[] getArray(String name) {
    throw new UnsupportedOperationException("Not supported by " + MapBasedRequestParams.class.getName());
  }

  @Override
  public Object getAttribute(String name) {
    throw new UnsupportedOperationException("Not supported by " + MapBasedRequestParams.class.getName());
  }

  @Override
  public String getUploadFileContent(String name) throws WdkModelException {
    throw new UnsupportedOperationException("Not supported by " + MapBasedRequestParams.class.getName());
  }

  @Override
  public void setArray(String name, String[] array) {
    throw new UnsupportedOperationException("Not supported by " + MapBasedRequestParams.class.getName());
  }

  @Override
  public void setAttribute(String name, Object value) {
    throw new UnsupportedOperationException("Not supported by " + MapBasedRequestParams.class.getName());
  }

}
