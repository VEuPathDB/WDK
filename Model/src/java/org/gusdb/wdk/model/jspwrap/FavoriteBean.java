package org.gusdb.wdk.model.jspwrap;

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
    public RecordBean getRecordInstance() {
        return new RecordBean(null, favorite.getRecordInstance());
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.user.Favorite#getRecordClass()
     */
    public RecordClassBean getRecordClass() {
        return new RecordClassBean(favorite.getRecordClass());
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.user.Favorite#getUser()
     */
    public UserBean getUser() {
        return new UserBean(favorite.getUser());
    }
}
