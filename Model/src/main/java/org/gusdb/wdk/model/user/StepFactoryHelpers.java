package org.gusdb.wdk.model.user;

import java.util.HashMap;

import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;

public class StepFactoryHelpers {

  // columns shared between steps and strategies tables
  static final String COLUMN_USER_ID = Utilities.COLUMN_USER_ID;
  static final String COLUMN_STRATEGY_ID = "strategy_id";
  static final String COLUMN_PROJECT_ID = "project_id";
  static final String COLUMN_CREATE_TIME = "create_time";
  static final String COLUMN_IS_DELETED = "is_deleted";

  // steps table and columns
  static final String TABLE_STEP = "steps";
  static final String COLUMN_STEP_ID = "step_id";
  static final String COLUMN_PREVIOUS_STEP_ID = "left_child_id";
  static final String COLUMN_CHILD_STEP_ID = "right_child_id";
  static final String COLUMN_LAST_RUN_TIME = "last_run_time";
  static final String COLUMN_ESTIMATE_SIZE = "estimate_size";
  static final String COLUMN_ANSWER_FILTER = "answer_filter";
  static final String COLUMN_CUSTOM_NAME = "custom_name";
  static final String COLUMN_IS_VALID = "is_valid";
  static final String COLUMN_COLLAPSED_NAME = "collapsed_name";
  static final String COLUMN_IS_COLLAPSIBLE = "is_collapsible";
  static final String COLUMN_ASSIGNED_WEIGHT = "assigned_weight";
  static final String COLUMN_PROJECT_VERSION = "project_version";
  static final String COLUMN_QUESTION_NAME = "question_name";
  static final String COLUMN_DISPLAY_PARAMS = "display_params";
  static final String COLUMN_DISPLAY_PREFS  = "display_prefs";

  static final String[] STEP_TABLE_COLUMNS = {
      COLUMN_USER_ID, COLUMN_STRATEGY_ID, COLUMN_PROJECT_ID, COLUMN_CREATE_TIME, COLUMN_IS_DELETED,
      COLUMN_STEP_ID, COLUMN_PREVIOUS_STEP_ID, COLUMN_CHILD_STEP_ID, COLUMN_LAST_RUN_TIME, COLUMN_ESTIMATE_SIZE,
      COLUMN_ANSWER_FILTER, COLUMN_CUSTOM_NAME, COLUMN_IS_VALID, COLUMN_COLLAPSED_NAME, COLUMN_IS_COLLAPSIBLE,
      COLUMN_ASSIGNED_WEIGHT, COLUMN_PROJECT_VERSION, COLUMN_QUESTION_NAME, COLUMN_DISPLAY_PARAMS
  };

  // strategies table and columns
  static final String TABLE_STRATEGY = "strategies";
  static final String COLUMN_ROOT_STEP_ID = "root_step_id";
  static final String COLUMN_VERSION = "version";
  static final String COLUMN_IS_SAVED = "is_saved";
  static final String COLUMN_LAST_VIEWED_TIME = "last_view_time";
  static final String COLUMN_LAST_MODIFIED_TIME = "last_modify_time";
  static final String COLUMN_DESCRIPTION = "description";
  static final String COLUMN_SIGNATURE = "signature";
  static final String COLUMN_NAME = "name";
  static final String COLUMN_SAVED_NAME = "saved_name";
  static final String COLUMN_IS_PUBLIC = "is_public";

  static final String[] STRATEGY_TABLE_COLUMNS = {
      COLUMN_USER_ID, COLUMN_STRATEGY_ID, COLUMN_PROJECT_ID, COLUMN_CREATE_TIME, COLUMN_IS_DELETED,
      COLUMN_ROOT_STEP_ID, COLUMN_VERSION, COLUMN_IS_SAVED, COLUMN_LAST_VIEWED_TIME, COLUMN_LAST_MODIFIED_TIME,
      COLUMN_DESCRIPTION, COLUMN_SIGNATURE, COLUMN_NAME, COLUMN_SAVED_NAME, COLUMN_IS_PUBLIC
  };

  public static class UserCache extends HashMap<Long,User> {

    private static final long serialVersionUID = 1L;

    private final UserFactory _userFactory;

    public UserCache(UserFactory userFactory) {
      _userFactory = userFactory;
    }

    /**
     * Creates a user cache with an initial value.
     *
     * @param user a user to place in the cache.
     */
    public UserCache(User user) {
      put(user.getUserId(), user);
      _userFactory = null;
    }

    @Override
    public User get(Object id) {
      try {
        Long userId = (Long)id;
        if (userId == null) {
          throw new WdkRuntimeException("User ID cannot be null.");
        }
        if (!containsKey(userId)) {
          if (_userFactory != null) {
            put(userId, _userFactory.getUserById(userId)
                .orElseThrow(() -> new WdkRuntimeException("User with ID " + id + " does not exist.")));
          }
          else {
            throw new WdkRuntimeException("No-lookup cache does not contain the requested user (" + id + ").");
          }
        }
        return super.get(userId);
      }
      catch (WdkModelException e) {
        throw new WdkRuntimeException("Unable to execute user lookup query.", e);
      }
    }
  }

  public static class NameCheckInfo {

    boolean _nameExists;
    boolean _isPublic;
    String _description;

    public NameCheckInfo(boolean nameExists, boolean isPublic, String description) {
      _nameExists = nameExists;
      _isPublic = isPublic;
      _description = description;
    }

    public boolean nameExists() {
      return _nameExists;
    }

    public boolean isPublic() {
      return _isPublic;
    }

    public String getDescription() {
      return _description;
    }
  }
}
