package org.gusdb.wdk.model;

public interface Manageable<T extends Manageable<?>> {

  T getInstance(String projectId, String gusHome) throws WdkModelException;

}
