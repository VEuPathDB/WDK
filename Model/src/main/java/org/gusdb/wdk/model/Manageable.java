package org.gusdb.wdk.model;

/**
 * @author Jerric
 *
 *         Declare the classes that implement this interface can be managed by singleton manager.
 * 
 *         The class that implements this interface must have a default construct with no argument, so that a
 *         stub instance can be created, in order for the manager to call the
 *         {@link #getInstance(String, String)} method.
 *
 * @param <T>
 */
public interface Manageable<T extends Manageable<?>> {

  /**
   * Create a singleton instance of this class. Ideally this method should be a static method, but since we
   * cannot declare static methods in interface, we have to use an instance method to do it. The singleton
   * manager will create a stub object of the class, then call this method to create the singleton instance it
   * will manage.
   * 
   * @param projectId
   * @param gusHome
   * @return
   * @throws WdkModelException
   */
  T getInstance(String projectId, String gusHome) throws WdkModelException;

}
