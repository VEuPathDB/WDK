package org.gusdb.wdk.controller.wizard;

public interface WizardFormIfc extends MapActionFormIfc {

  String getAction();

  void setStrategy(String strategyKey);

  void copyFrom(MapActionFormIfc otherForm);

  // returns strategy key
  String getStrategy();

}
