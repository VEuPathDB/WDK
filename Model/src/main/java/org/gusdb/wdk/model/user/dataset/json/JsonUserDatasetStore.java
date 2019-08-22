package org.gusdb.wdk.model.user.dataset.json;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UnsupportedTypeHandler;
import org.gusdb.wdk.model.user.dataset.UserDatasetStore;
import org.gusdb.wdk.model.user.dataset.UserDatasetStoreAdaptor;
import org.gusdb.wdk.model.user.dataset.UserDatasetType;
import org.gusdb.wdk.model.user.dataset.UserDatasetTypeHandler;

/**
 * An abstract implementation of UserDatasetStore that uses the JSON based
 * objects in this package, as well as java.nio.file.Path (but no other nio
 * classes). Since Path is just a file path, should be compatible with other
 * file systems (such as iRODS).
 * <p>
 * This is the directory structure:
 * <pre>
 *  rootPath/
 *   default_quota
 *   u12345/              # files in this directory are user readable, but not writeable
 *     quota              # can put this here, if increased from default
 *     datasets/
 *       d34541/
 *       d67690/
 *         datafiles/
 *            Blah.bigwig
 *            blah.profile
 *         dataset.json
 *         meta.json      # we have this as a separate file to avoid race conditions with sharing updates
 *         /sharedWith
 *           54145        # a an empty file, named for the user that is shared with
 *           90912
 *     externalDatasets/
 *       43425.12592    # reference to dataset in another user.  empty file, name: user_id.dataset_id
 *       691056.53165
 *     removedExternalDatasets/   # holds shares this user no longer wants to see.
 *       502401.90112
 * </pre>
 *
 * @author steve
 */
public abstract class JsonUserDatasetStore implements UserDatasetStore {

  protected static final String DATASET_JSON_FILE = "dataset.json";
  protected static final String META_JSON_FILE = "meta.json";
  protected static final String EXTERNAL_DATASETS_DIR = "externalDatasets";
  protected static final String SHARED_WITH_DIR = "sharedWith";
  protected static final String REMOVED_EXTERNAL_DATASETS_DIR = "removedExternalDatasets";
  protected Map<UserDatasetType, UserDatasetTypeHandler> typeHandlersMap = new HashMap<UserDatasetType, UserDatasetTypeHandler>();

  protected Path _usersRootDir;
  protected UserDatasetStoreAdaptor _adaptor;
  protected String _id;
  protected Path _wdkTempDir;
  protected UserDatasetTypeHandler _unsupportedTypeHandler;

  @Override
  public Path getUsersRootDir() {
    return _usersRootDir;
  }

  @Override
  public void initialize(Map<String, String> configuration, Map<UserDatasetType, UserDatasetTypeHandler> typeHandlers, Path wdkTempDir) throws WdkModelException {
    String pathName = configuration.get("rootPath");
    if (pathName == null)
      throw new WdkModelException("Required configuration 'rootPath' not found.");
    _usersRootDir = Paths.get(pathName);
    _wdkTempDir = wdkTempDir;
    typeHandlersMap = typeHandlers;
    createUnsupportedTypeHandler();
  }

  /**
   * This creates a user dataset type handler to substitute for type handlers
   * that are no longer supported by the website.
   */
  protected void createUnsupportedTypeHandler() throws WdkModelException {
    Class<?> implClass = UnsupportedTypeHandler.class;
    try {
      Constructor<?> constructor = implClass.getConstructor();
      _unsupportedTypeHandler = (UserDatasetTypeHandler) constructor.newInstance();
    }
    catch(NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
      throw new WdkModelException("No proper unsupported type handler implementation exists.", e);
    }
  }

  /**
   * Return the user dataset type handler that matches the type and version of
   * the given user dataset type. Otherwise return a very generic type handler
   * (intended for types/versions that are no longer supported).
   */
  @Override
  public UserDatasetTypeHandler getTypeHandler(UserDatasetType type) {
    UserDatasetTypeHandler handler = typeHandlersMap.get(type);
    return handler == null ? _unsupportedTypeHandler : handler;
  }

  @Override
  public String getId() {
    return _id;
  }

  @Override
  public Path getWdkTempDir() {
    return _wdkTempDir;
  }
}
