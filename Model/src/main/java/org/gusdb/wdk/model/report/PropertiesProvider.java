package org.gusdb.wdk.model.report;

import java.util.Map;

@FunctionalInterface
public interface PropertiesProvider {

  Map<String,String> getProperties();

}
