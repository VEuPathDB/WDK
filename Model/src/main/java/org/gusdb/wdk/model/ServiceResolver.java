package org.gusdb.wdk.model;

import java.util.Map;

import org.gusdb.fgputil.MapBuilder;
import org.gusdb.wsf.client.WsfClientFactory;

/**
 * This is a temporary measure until we really start adapting Spring IoC.  In
 * the future, Spring should inject the correct implementation instances into
 * an interface fields of any desiring classes.
 * 
 * @author rdoherty
 */
public class ServiceResolver {

  private static Map<String, Service> SERVICE_REGISTRY = new RegistryBuilder()

  /****************** List of Supported Service Resolutions *******************/
  /**** Arguments: interface class, implementation class name, isSingleton ****/

  .register(WsfClientFactory.class, "org.gusdb.wsf.client.WsfClientFactoryImpl", true)

  /****************************************************************************/
  
  .toMap();

  private static class RegistryBuilder extends MapBuilder<String, Service> {
    public RegistryBuilder register(Class<?> interfaceClass, String implName, boolean isSingleton) {
      return (RegistryBuilder) put(interfaceClass.getName(),
          new Service(interfaceClass.getName(), implName, isSingleton));
    }
  }

  private static class Service {
    public final String _interfaceName;
    public final String _implementationName;
    public final boolean _isSingleton;
    public Object _singletonInstance;
    public Service(String interfaceName, String implementationName, boolean isSingleton) {
      _interfaceName = interfaceName;
      _implementationName = implementationName;
      _isSingleton = isSingleton;
    }
  }

  private ServiceResolver() { }

  @SuppressWarnings("unchecked")
  public static <T> T resolve(Class<T> desiredInterface) {
    String interfaceName = desiredInterface.getName();
    Service service = SERVICE_REGISTRY.get(interfaceName);
    if (service == null) {
      throw new UnsupportedOperationException("Cannot resolve service " +
          "implementation for interface: " + interfaceName);
    }
    if (service._isSingleton) {
      synchronized(service) {
        if (service._singletonInstance == null) {
          service._singletonInstance = getImplemenation(service);
        }
        return (T) service._singletonInstance;
      }
    }
    // otherwise, create instance for each request
    return (T) getImplemenation(service);
  }

  private static Object getImplemenation(Service service) {
    try {
      return Class.forName(service._implementationName).newInstance();
    }
    catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
      throw new WdkRuntimeException("Unable to instantiate implemenation instance of " +
          service._implementationName + " for interface "  + service._interfaceName +
          ".  Is the necessary JAR included in the runtime?");
    }
  }

}
