package org.gusdb.wdk.model;


public interface FieldI {

    public String getName();
    public String getDisplayName();
    public String getHelp();
    public String getType();
    public Boolean getIsInternal();
    public Integer getTruncate();
}

