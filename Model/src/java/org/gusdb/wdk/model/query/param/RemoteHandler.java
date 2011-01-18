package org.gusdb.wdk.model.query.param;

import java.util.Map;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;

public interface RemoteHandler {

    void setProperties(Map<String, String> properties) throws WdkModelException;

    void setModel(WdkModel wdkModel);

    String getResource(User user, Map<String, String> params) throws JSONException, WdkModelException;
}
