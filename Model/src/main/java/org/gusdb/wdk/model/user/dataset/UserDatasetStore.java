package org.gusdb.wdk.model.user.dataset;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import org.gusdb.wdk.model.WdkModelException;
import org.json.JSONObject;

/**
 * Provides access to collections of user datasets.  Only provides information about
 * one user at a time.
 * @author steve
 *
 */
public interface UserDatasetStore {

  public static class Status {

    public static final Status UNCONFIGURED = new Status();

    public boolean _isConfigured = false;
    public Optional<Exception> _creationError = Optional.empty();
    public Optional<UserDatasetStore> _store = Optional.empty();

    private Status() { /* for unconfigured UDS */ }

    public Status(UserDatasetStore store) {
      _isConfigured = true;
      _store = Optional.of(store);
    }

    public Status(Exception creationError) {
      _isConfigured = true;
      _creationError = Optional.of(creationError);
    }

    public boolean isConfigured() {
      return _isConfigured;
    }

    public Optional<UserDatasetStore> getStore() {
      return _store;
    }

    public Optional<String> getCreationErrorMessage() {
      return _creationError.map(e ->
        e instanceof WdkModelException && e.getCause() != null ?
            e.getCause().getMessage() : e.getMessage());
    }

    public JSONObject toJson() {
      return new JSONObject()
        .put("isConfigured", _isConfigured)
        .put("isAvailable", _store.isPresent())
        .put("creationError", getCreationErrorMessage().orElse(null));
    }

    public boolean hasStore() {
      return _store.isPresent();
    }

  }

  /**
   * Called at start up by the WDK.  The configuration comes from properties in
   * model XML.
   */
  void initialize(Map<String, String> configuration, Map<UserDatasetType, UserDatasetTypeHandler> typeHandlers, Path wdkTempDir) throws WdkModelException;

  UserDatasetSession getSession() throws WdkModelException;
  UserDatasetSession getSession(Path usersRootDir) throws WdkModelException;

  Path getUsersRootDir();

  /**
   * Return the type handler registered for the specified type.
   *
   * @return null if not found.
   */
  UserDatasetTypeHandler getTypeHandler(UserDatasetType type);

  String getId();

  Path getWdkTempDir();

}
