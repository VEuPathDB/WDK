package org.gusdb.wdk.model.implementation;

import org.gusdb.wdk.model.WdkModelText;


public class SqlParamValue extends WdkModelText {

    private String name;

    
    public String getName() {
        return name;
    }

    
    public void setName(String name) {
        this.name = name;
    }
}
