package org.gusdb.gus.wdk.model.query;

import java.util.Map;

public interface PageableQueryI extends QueryI {
    
    public PageableQueryInstanceI makeInstance();

    /**
     * Resolve any by-name references to queries.
     */
    public void dereference(Map queryMap) throws Exception ;
}
