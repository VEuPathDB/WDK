package org.gusdb.gus.wdk.model;

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

    public void addColumn(Column column);

    public Column[] getColumns();

    public Column getColumn(String columnName);
}
