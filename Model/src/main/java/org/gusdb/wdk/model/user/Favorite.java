package org.gusdb.wdk.model.user;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.record.PrimaryKeyValue;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordIdentity;
import org.gusdb.wdk.model.record.StaticRecordInstance;
import org.gusdb.wdk.model.user.FavoriteFactory.NoteAndGroup;

public class Favorite extends RecordIdentity implements NoteAndGroup {

    private static final Logger LOG = Logger.getLogger(Favorite.class);

    private final long _favoriteId;
    private final User _user;
    private final String _display;
    private String _note;
    private String _group;
    private boolean _isDeleted = false;

    public Favorite(User user, RecordClass recordClass, PrimaryKeyValue primaryKey, long favoriteId) {
      super(recordClass, primaryKey);
      _favoriteId = favoriteId;
      _user = user;
      _display = createDisplay(recordClass, primaryKey);
    }

    private static String createDisplay(RecordClass recordClass, PrimaryKeyValue pkValue) {
      try {
        if (!recordClass.idAttributeHasNonPkMacros()) {
          // can use PK values to generate ID display
          StaticRecordInstance record = new StaticRecordInstance(recordClass, recordClass, pkValue.getRawValues(), false);
          // can only do the following cheaply if ID attribute only requires to PK values (not other attribs)
          return record.getIdAttributeValue().getDisplay();
        }
      }
      catch (WdkModelException | WdkUserException e) {
        LOG.warn("Unable to create favorite display value of type " + recordClass.getFullName() +
            " and PK " + pkValue.getValuesAsString(), e);
      }
      // unwilling or unable to generate display from ID attribute
      return FormatUtil.join(pkValue.getValues().values(), ", ");
    }

    /**
     * Return the sequence generated primary key for the Favorite table
     * @return
     */
    public long getFavoriteId() {
      return _favoriteId;
    }

    public User getUser() {
        return _user;
    }

    /**
     * @return the note
     */
    @Override
    public String getNote() {
        return _note;
    }

    /**
     * @param note
     *            the note to set
     */
    public void setNote(String note) {
        this._note = note;
    }

    /**
     * @return the group
     */
    @Override
    public String getGroup() {
        return _group;
    }

    /**
     * @param group
     *            the group to set
     */
    public void setGroup(String group) {
        this._group = group;
    }

    public String getDisplay() {
      return _display;
    }

    public void setDeleted(boolean isDeleted) {
      _isDeleted = isDeleted;
    }

    public boolean isDeleted() {
      return _isDeleted;
    }

}
