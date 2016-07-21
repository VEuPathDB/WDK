package org.gusdb.wdk.model.user.dataset;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;

public class UserDatasetStorePlugin extends WdkModelBase {
  private static final Logger logger = Logger.getLogger(UserDatasetStorePlugin.class);

  private String implementationClass;
  private List<WdkModelText> propertyList = new ArrayList<>();
  private Set<UserDatasetTypeHandlerPlugin> typeHandlerPlugins = new HashSet<UserDatasetTypeHandlerPlugin>();
  private UserDatasetStore userDatasetStore;

  public UserDatasetStore getUserDatasetStore() {
    return userDatasetStore;
  }
  
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

  public void addTypeHandler(UserDatasetTypeHandlerPlugin plugin) {
    typeHandlerPlugins.add(plugin);
  }
 
  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.WdkModelBase#resolveReferences(org.gusdb.wdk.model
   * .WdkModel)
   */
  @Override
  public void resolveReferences(WdkModel wodkModel) throws WdkModelException {

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
    
    // call resolve references on handlers; add the resolved typeHandlers to our list
    Set<UserDatasetTypeHandler> typeHandlers = new HashSet<UserDatasetTypeHandler>();
    for (UserDatasetTypeHandlerPlugin plugin : typeHandlerPlugins) {
      plugin.resolveReferences(getWdkModel());
      typeHandlers.add(plugin.getTypeHandler());
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

    // initialize the store
    userDatasetStore.initialize(properties, typeHandlers);
  }
}
