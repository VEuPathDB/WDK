package org.gusdb.wdk.model.user;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.record.PrimaryKeyValue;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.StaticRecordInstance;

public class Favorite {

    private static final Logger LOG = Logger.getLogger(Favorite.class);

    private final User user;
    private final RecordClass recordClass;
    private final PrimaryKeyValue id;
    private final String display;
    private String note;
    private String group;

    public Favorite(User user, RecordClass recordClass, PrimaryKeyValue primaryKey) {
        this.user = user;
        this.recordClass = recordClass;
        this.id = primaryKey;
        this.display = createDisplay(user, recordClass, id);
    }

    private static String createDisplay(User user, RecordClass recordClass, PrimaryKeyValue pkValue) {
      try {
        if (!recordClass.idAttributeHasNonPkMacros()) {
          // can use PK values to generate ID display
          StaticRecordInstance record = new StaticRecordInstance(user, recordClass, recordClass, pkValue.getRawValues(), true);
          // can only do the following cheaply if ID attribute only requires to PK values (not other attribs)
          return record.getIdAttributeValue().getDisplay();
        }
      }
      catch (WdkModelException | WdkUserException e) {
        LOG.warn("Unable to create favorite display value of type " + recordClass.getFullName() +
            " and PK " + pkValue.getValuesAsString(), e);
      }
      // unwilling or unable to generate display from ID attribute
      return pkValue.getValuesAsString();
    }

    public User getUser() {
        return user;
    }

    public RecordClass getRecordClass() {
        return recordClass;
    }

    public PrimaryKeyValue getPrimaryKey() {
        return id;
    }

    /**
     * @return the note
     */
    public String getNote() {
        return note;
    }

    /**
     * @param note
     *            the note to set
     */
    void setNote(String note) {
        this.note = note;
    }

    /**
     * @return the group
     */
    public String getGroup() {
        return group;
    }

    /**
     * @param group
     *            the group to set
     */
    void setGroup(String group) {
        this.group = group;
    }

    public String getDisplay() {
      return display;
    }

}
