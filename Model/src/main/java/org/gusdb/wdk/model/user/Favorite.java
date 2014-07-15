package org.gusdb.wdk.model.user;

import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordInstance;

public class Favorite {

    private User user;
    private RecordClass recordClass;
    private RecordInstance recordInstance;
    private String note;
    private String group;

    public Favorite(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public RecordClass getRecordClass() {
        return recordClass;
    }

    public RecordInstance getRecordInstance() {
        return recordInstance;
    }

    void setRecordInstance(RecordInstance recordInstance) {
        this.recordInstance = recordInstance;
        this.recordClass = recordInstance.getRecordClass();
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

}
