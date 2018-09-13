package org.gusdb.wdk.model.config;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.user.dataset.UserDatasetStore;
import org.gusdb.wdk.model.user.dataset.UserDatasetType;
import org.gusdb.wdk.model.user.dataset.UserDatasetTypeHandler;


public class ModelConfigUserDatasetStore extends WdkModelBase {
  private static final Logger logger = Logger.getLogger(ModelConfigUserDatasetStore.class);

  private String implementationClass;
  private List<WdkModelText> propertyList = new ArrayList<>();
  private Set<ModelConfigUserDatasetTypeHandler> typeHandlerConfigs = new HashSet<ModelConfigUserDatasetTypeHandler>();
  private UserDatasetStore userDatasetStore;
  private Map<UserDatasetType, UserDatasetTypeHandler> typeHandlers;
  
  /**
   * @return the implementation
   */
  public String getImplementation() {
    return implementationClass;
  }

  /**
   * @param implementation
   *          the implementation to set
   */
  public void setImplementation(String implementation) {
    this.implementationClass = implementation;
  }

  public void addProperty(WdkModelText property) {
    this.propertyList.add(property);
  }

  public void addTypeHandler(ModelConfigUserDatasetTypeHandler handlerConfig) {
    typeHandlerConfigs.add(handlerConfig);
  }
 
  /*
   * Returns an initialized user dataset store.
   */
  public UserDatasetStore getUserDatasetStore(Path wdkTempDir) throws WdkModelException {

    // if a store has already been created and configured, return that one
    if (userDatasetStore != null) {
      return userDatasetStore;
    }

    // try to find implementation class
    String msgStart = "Implementation class for userDatasetStorePlugin [" + getImplementation() + "] ";
    try {
      Class<?> implClass = Class.forName(getImplementation());
      if (!UserDatasetStore.class.isAssignableFrom(implClass)) 
        throw new WdkModelException(msgStart + "must implement " + UserDatasetStore.class.getName());
      Constructor<?> constructor = implClass.getConstructor();
      userDatasetStore = (UserDatasetStore) constructor.newInstance();
    }
    catch (ClassNotFoundException e) {
      throw new WdkModelException(msgStart + "cannot be found.", e);
    }
    catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new WdkModelException(msgStart + "cannot be constructed.", e);
    }
    
    // stuff properties into a useable map
    Map<String, String> properties = new LinkedHashMap<>();
    for (WdkModelText property : propertyList) {
        String propName = property.getName();
        String propValue = property.getText();
        if (properties.containsKey(propName))
          throw new WdkModelException("The property " + propName
              + " is duplicated in userDatasetStorePlugin");
        properties.put(propName, propValue);
        logger.trace("userDatasetStore property: [" + propName + "]='" + propValue
            + "'");
    }
    propertyList = null;
    
    userDatasetStore.initialize(properties, getTypeHandlers(), wdkTempDir);

    return userDatasetStore;
  }

  public Map<UserDatasetType, UserDatasetTypeHandler> getTypeHandlers() throws WdkModelException {
    if (typeHandlers == null) {
      // call resolve references on handlers; add the resolved typeHandlers to our list
      typeHandlers = new HashMap<UserDatasetType, UserDatasetTypeHandler>();
      for (ModelConfigUserDatasetTypeHandler typeHandlerConfig : typeHandlerConfigs) {
        typeHandlers.put(typeHandlerConfig.getTypeHandler().getUserDatasetType(), typeHandlerConfig.getTypeHandler());
      }
    }
    return typeHandlers;
  }
}
