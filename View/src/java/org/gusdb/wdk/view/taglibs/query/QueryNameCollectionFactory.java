package org.gusdb.gus.wdk.view.taglibs.query;

import java.util.HashMap;
import java.util.Iterator;

import org.gusdb.gus.wdk.model.Query;
import org.gusdb.gus.wdk.model.QueryName;
import org.gusdb.gus.wdk.model.QueryNameList;
import org.gusdb.gus.wdk.model.QuerySet;
import org.gusdb.gus.wdk.model.WdkModel;

public class QueryNameCollectionFactory {

    HashMap querySets = new HashMap();
    HashMap recordSets = new HashMap();
    HashMap queryNameLists = new HashMap();
    String name;

    
    public QueryNameCollection getQueryNameCollection(String name, WdkModel model) {
        if (model.hasQuerySet(name)) {
            return new QuerySetFacade(model.getQuerySet(name)); 
        }
        QueryNameList qnl = model.getQueryNameList(name);
        return new ExtendedQueryNameList(qnl);
    }

    
    public QueryNameList getQueryNameList(String queryNameListName){
	
        if (!queryNameLists.containsKey(queryNameListName)){
            String err = "WDK Model " + name +
            " does not contain a  query set with name " + queryNameListName;
            throw new IllegalArgumentException(err);
        }
        return (QueryNameList)queryNameLists.get(queryNameListName);
    }

    public QueryNameList[] getAllQueryNameLists(){

        QueryNameList lists[] = new QueryNameList[queryNameLists.size()];
        Iterator keys = queryNameLists.keySet().iterator();
        int counter = 0;
        while (keys.hasNext()){
            String name = (String)keys.next();
            QueryNameList nextQueryNameList = (QueryNameList)queryNameLists.get(name);
            lists[counter] = nextQueryNameList;
            counter++;
        }
        return lists;
    }

    
    private class ExtendedQueryNameList extends QueryNameList implements QueryNameCollection {
        
        ExtendedQueryNameList(QueryNameList qnl) {
            setName(qnl.getName());
            QueryName[] names = qnl.getQueryNames();
            for (int i = 0; i < names.length; i++) {
                QueryName name = names[i];
                addQueryName(name);
            }
        }  
        
    }

    
    private class QuerySetFacade implements QueryNameCollection {

        private QueryName[] queryNames;
        
        public QueryName[] getQueryNames() {
            return queryNames;
        }
        
    
        QuerySetFacade(QuerySet qs) {
            Query[] queries = qs.getQueries();
            queryNames = new QueryName[queries.length];
            for (int i = 0; i < queries.length; i++) {
                try {
                    queryNames[i] = new QueryName(qs.getName() + "." + queries[i].getName());
                } catch (Exception exp) {
                    // Ignore - shouldn't happen
                    // FIXME Exception handling
                }
            }
        }
    }

}

