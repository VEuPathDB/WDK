package org.gusdb.gus.wdk.model.query;

public interface QueryI {
    
    public void setName(String name);

    public String getName();

    public void setDisplayName(String displayName);

    public String getDisplayName();

    public void setIsCacheable(Boolean isCacheable);

    public Boolean getIsCacheable();

    public void setHelp(String help);

    public String getHelp();

    public void addParam(Param param);

    public Param[] getParams();

}
