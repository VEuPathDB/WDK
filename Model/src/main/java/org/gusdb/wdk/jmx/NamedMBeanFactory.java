package org.gusdb.wdk.jmx;

import java.util.Map;

public interface NamedMBeanFactory {

  public Map<String, Object> getNamedMbeans(String parameterizedMbeanName);

}
