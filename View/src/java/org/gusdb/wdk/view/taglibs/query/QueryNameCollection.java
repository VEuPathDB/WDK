package org.gusdb.gus.wdk.view.taglibs.query;

import org.gusdb.gus.wdk.model.QueryName;

/**
 * QueryNameList.java
 *
 * A simple list where each entry is of type QueryName, representing the fully 
 * qualified name of a Query.  Queries in the list can come
 * from different QuerySets and are not restricted on the different types of 
 * QuerySets they can reference (A QueryNameList could include Queries from 
 * SimpleQuerySets and PageableQuerySets, for example.)
 *
 * Created: Mon May 10 12:34:30 2004
 *
 * @author David Barkan
 * @version $Revision$ $Date$ $Author$
 */

public interface QueryNameCollection {


    /**
     * @return  an array of QueryNames, where each entry in the array represents one fully qualified Query.
     */

    public QueryName[] getQueryNames();


} 
