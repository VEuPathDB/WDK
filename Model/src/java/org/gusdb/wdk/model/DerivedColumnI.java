package org.gusdb.wdk.model;

public interface DerivedColumnI {

    public Object getDerivedValue(ResultList resultList) throws WdkModelException;
}

