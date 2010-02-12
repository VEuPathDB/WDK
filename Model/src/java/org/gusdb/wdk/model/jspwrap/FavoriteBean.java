package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.RecordInstance;
import org.gusdb.wdk.model.user.Favorite;

public class FavoriteBean {

    private Favorite favorite;

    public FavoriteBean(Favorite favorite) {
        this.favorite = favorite;
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
     * @see org.gusdb.wdk.model.user.Favorite#getRecordInstances()
     */
    public RecordBean[] getRecordInstances() {
        RecordInstance[] instances = favorite.getRecordInstances();
        RecordBean[] beans = new RecordBean[instances.length];
        for (int i = 0; i < instances.length; i++) {
            beans[i] = new RecordBean(favorite.getUser(), instances[i]);
        }
        return beans;
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
     * @see org.gusdb.wdk.model.user.Favorite#getCount()
     */
    public int getCount() {
        return favorite.getCount();
    }

}
