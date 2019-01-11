package org.gusdb.wdk.controller.wizard;

import java.util.Map;

import javax.servlet.ServletContext;

public interface MapActionFormIfc {

  Map<String, Object> getValues();

  Map<String, String[]> getArrays();

  Object getValue(String string);

  void setValue(String key, Object value);

  Object getValueOrArray(String key);

  ServletContext getServletContext();

}
