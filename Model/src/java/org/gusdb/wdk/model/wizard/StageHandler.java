package org.gusdb.wdk.model.wizard;

import java.util.Map;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.user.User;

public interface StageHandler {

    public Map<String, Object> execute(WdkModel wdkModel, User user,
            Map<String, String> params) throws Exception;
}
