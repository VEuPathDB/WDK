package org.gusdb.wdk.model.jspwrap;

import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.RemoteListParam;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;

public class RemoteListParamBean extends ParamBean {

    private RemoteListParam param;
    
    public RemoteListParamBean(UserBean user, RemoteListParam param) {
        super(user, param);
        
        this.param = param;
    }

    /**
     * @param user
     * @return
     * @throws JSONException
     * @throws WdkModelException
     * @see org.gusdb.wdk.model.query.param.RemoteListParam#getDisplayMap(org.gusdb.wdk.model.user.User)
     */
    public Map<String, String> getDisplayMap() throws JSONException,
            WdkModelException {
        return param.getDisplayMap(user.getUser());
    }
}
