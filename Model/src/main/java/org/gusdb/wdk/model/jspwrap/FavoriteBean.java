package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.record.PrimaryKeyValue;
import org.gusdb.wdk.model.user.Favorite;

public class FavoriteBean {

    private Favorite favorite;

    public FavoriteBean(Favorite favorite) {
        this.favorite = favorite;
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.user.Favorite#getRecordInstances()
     */
    public PrimaryKeyValue getPrimaryKey() {
        return favorite.getPrimaryKey();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.user.Favorite#getRecordClass()
     */
    public RecordClassBean getRecordClass() {
        return new RecordClassBean(favorite.getRecordClass());
    }

    public String getDisplay() {
        return favorite.getDisplay();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.user.Favorite#getUser()
     */
    public UserBean getUser() {
        return new UserBean(favorite.getUser());
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.user.Favorite#getGroup()
     */
    public String getGroup() {
        return favorite.getGroup();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.user.Favorite#getNote()
     */
    public String getNote() {
        return favorite.getNote();
    }
}
