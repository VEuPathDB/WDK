package org.gusdb.gus.wdk.model;

import java.util.Map;

public interface PageableQueryI extends QueryI {
    
    public PageableQueryInstanceI makeInstance();

    /**
     * Resolve any by-name references to queries.
     */
    public void resolveReferences(Map queryMap) throws Exception ;
}
