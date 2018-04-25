package org.gusdb.wdk.model.report;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

public class AttributeReporterRef extends ReporterRef {

  @Override
  public void resolveReferences(WdkModel wodkModel) throws WdkModelException {

    // try to find implementation class
    String msgStart = "Implementation class for reporter '" + getName() + "' [" + getImplementation() + "] ";
    try {
      Class<?> implClass = Class.forName(getImplementation());
      if (!AbstractAttributeReporter.class.isAssignableFrom(implClass)) {
        throw new WdkModelException(msgStart + "must be a subclass of " + AbstractAttributeReporter.class.getName());
      }
    }
    catch (ClassNotFoundException e) {
      throw new WdkModelException(msgStart + "cannot be found.", e);
    }
  }
}
