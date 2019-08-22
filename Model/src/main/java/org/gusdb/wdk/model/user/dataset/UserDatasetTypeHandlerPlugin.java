package org.gusdb.wdk.model.user.dataset;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;

public class UserDatasetTypeHandlerPlugin extends WdkModelBase {

  private String implementationClass;
  private String type;
  private String version;
  private UserDatasetTypeHandler typeHandler;

  public UserDatasetTypeHandler getTypeHandler() { return typeHandler; }

  /**
   * @return the implementation
   */
  public String getImplementation() {
    return implementationClass;
  }

  /**
   * @param implementation
   *   the implementation to set
   */
  public void setImplementation(String implementation) {
    this.implementationClass = implementation;
  }

  public void setType(String type) { this.type = type; }

  public String getType() { return type; }

  public void setVersion(String version) { this.version = version; }

  public String getVersion() { return version; }

  /**
   * @see org.gusdb.wdk.model.WdkModelBase#resolveReferences(WdkModel)
   */
  @Override
  public void resolveReferences(WdkModel wodkModel) throws WdkModelException {

    // try to find implementation class
    String msgStart = "Implementation class for userDatasetTypeHandlerPlugin [" + getImplementation() + "] ";
    try {
      Class<?> implClass = Class.forName(getImplementation());
      if (!UserDatasetTypeHandler.class.isAssignableFrom(implClass))
        throw new WdkModelException(msgStart + "must implement " + UserDatasetTypeHandler.class.getName());
      Constructor<?> constructor = implClass.getConstructor();
      typeHandler = (UserDatasetTypeHandler) constructor.newInstance();
      if (!typeHandler.getUserDatasetType().getName().equals(type) || !typeHandler.getUserDatasetType().getVersion().equals(version))
        throw new WdkModelException(msgStart + " is not compatible with " + type + " " + version);
    }
    catch (ClassNotFoundException e) {
      throw new WdkModelException(msgStart + "cannot be found.", e);
    }
    catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new WdkModelException(msgStart + "cannot be constructed.", e);
    }

  }
}
