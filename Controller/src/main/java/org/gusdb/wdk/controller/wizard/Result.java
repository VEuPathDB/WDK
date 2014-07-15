package org.gusdb.wdk.controller.wizard;

import org.gusdb.wdk.model.WdkModelText;

public class Result extends WdkModelText {
    
    public static final String TYPE_VIEW = "view";
    public static final String TYPE_ACTION = "action";

    private String type;

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }
}
