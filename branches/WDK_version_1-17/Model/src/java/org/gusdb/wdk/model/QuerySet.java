package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class QuerySet extends WdkModelBase implements ModelSetI {

    private List<Query> queryList = new ArrayList<Query>();
    private Map<String, Query> queries = new LinkedHashMap<String, Query>();
    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Query getQuery(String name) throws WdkModelException {
        Query q = queries.get(name);
        if (q == null)
            throw new WdkModelException("Query Set " + getName()
                    + " does not include query " + name);
        return q;
    }

    public Object getElement(String name) {
        return queries.get(name);
    }

    public Query[] getQueries() {
        Query[] array = new Query[queries.size()];
        queries.values().toArray(array);
        return array;
    }

    public void addQuery(Query query) throws WdkModelException {
        query.setSetName(this.getName());
        queryList.add(query);
    }

    public void resolveReferences(WdkModel model) throws WdkModelException {
        for (Query query : queries.values()) {
            query.resolveReferences(model);
        }
    }

    public void setResources(WdkModel model) throws WdkModelException {
        for (Query query : queries.values()) {
            query.setResources(model);
        }
    }

    public String toString() {
        String newline = System.getProperty("line.separator");
        StringBuffer buf = new StringBuffer("QuerySet: name='" + name + "'");
        buf.append(newline);
        for (Query query : queries.values()) {
            buf.append(newline);
            buf.append(":::::::::::::::::::::::::::::::::::::::::::::");
            buf.append(newline);
            buf.append(query);
            buf.append("----------------");
            buf.append(newline);
        }
        return buf.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        // exclude queries
        for (Query query : queryList) {
            if (query.include(projectId)) {
                query.excludeResources(projectId);
                String queryName = query.getName();
                if (queries.containsKey(queryName))
                    throw new WdkModelException("Query named " + queryName
                            + " already exists in query set " + getName());
                queries.put(queryName, query);
            }
        }
        queryList = null;
    }
    // ///////////////////////////////////////////////////////////////
    // ///// protected
    // ///////////////////////////////////////////////////////////////

}
