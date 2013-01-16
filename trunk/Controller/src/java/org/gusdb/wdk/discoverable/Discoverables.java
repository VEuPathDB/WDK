package org.gusdb.wdk.discoverable;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.reflections.Reflections;

public class Discoverables {

  private static Logger LOG = Logger.getLogger(Discoverables.class.getName());
  
  private static final String IMPLEMENTATIONS_PACKAGE = "org.gusdb.wdk.discoverables";
  
  public static AdditionalLogoutCookies getAdditionalLogoutCookies() throws WdkModelException {
    return getFirstInstance(AdditionalLogoutCookies.class);
  }
  
  private static <T> T getFirstInstance(Class<T> parentClass) throws WdkModelException {
    Set<Class<? extends T>> subClasses = getImplementations(parentClass);
    if (subClasses.isEmpty()) return null;
    Class<? extends T> instantiationClass = subClasses.iterator().next();
    return getSingleInstance(instantiationClass);
  }
  
  private static <T> T getSingleInstance(Class<T> instantiationClass) throws WdkModelException {
    try {
      return instantiationClass.newInstance();
    }
    catch (InstantiationException | IllegalAccessException e) {
      String msg = "Could not create instance of " + instantiationClass +
          " with no-arg constructor.  Please check the implementation.";
      LOG.info(msg);
      throw new WdkModelException(msg, e);
    }
  }
  
  @SuppressWarnings("unchecked")
  private static <T> Set<Class<? extends T>> getImplementations(Class<T> parentClass) {
    Reflections reflections = new Reflections(IMPLEMENTATIONS_PACKAGE);
    Set<Class<? extends Object>> allClasses = 
        reflections.getSubTypesOf(Object.class);
    Set<Class<? extends T>> subClasses = new HashSet<>();
    for (Class<? extends Object> clazz : allClasses) {
      if (parentClass.isAssignableFrom(clazz)) {
        // found a subclass or implementation of the passed superclass/superinterface
        subClasses.add((Class<? extends T>)clazz);
      }
    }
    return subClasses;
  }
}
