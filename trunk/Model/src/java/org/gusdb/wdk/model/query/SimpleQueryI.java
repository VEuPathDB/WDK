package org.gusdb.gus.wdk.model.query;

public interface SimpleQueryI extends QueryI {
    
    public SimpleQueryInstanceI makeInstance();

    public void setResultFactory(ResultFactory resultFactory);
}
