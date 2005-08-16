/*
 * Created on Feb 16, 2005
 * 
 */
package org.gusdb.wdk.model.implementation;

import java.util.Iterator;
import java.util.Map;

import org.gusdb.wdk.model.Query;
import org.gusdb.wdk.model.QueryInstance;
import org.gusdb.wdk.model.WdkModelException;

/**
 * Class representing the oql part in the xml configuration
 * @author Daud @University of Pennsylvania (daud@seas.upenn.edu)
 *
 */
public class K2Query extends Query {
    String description;
    String oql;
    String rmiNameBinding;
    
    public QueryInstance makeInstance() {
        return new K2QueryInstance(this);
    }
    
    
    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }
    /**
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }
    /**
     * @return Returns the oql.
     */
    public String getOql() {
        return oql;
    }
    /**
     * @param oql The oql to set.
     */
    public void setOql(String oql) {
        this.oql = oql;
    }
      
    /**
     * @return Returns the rmiNameBinding.
     */
    public String getRmiNameBinding() {
        return rmiNameBinding;
    }
    
    /**
     * STEVE: I leave it up to you to decide whether this
     * should be here or in configuration.
     * the only reason that it is here because each
     * K2Query can be configured with different server
     * @param rmiNameBinding The rmiNameBinding to set.
     */
    public void setRmiNameBinding(String rmiNameBinding) {
        this.rmiNameBinding = rmiNameBinding;
    }
    /**
     * Replace all the dollars with real values
     * @param values These values are assumed to be pre-validated
     */
    protected String instantiateOql(Map values)   throws WdkModelException {
        String s = this.oql;
        Iterator keySet = values.keySet().iterator();
        while (keySet.hasNext()) {
            String key = (String)keySet.next();
            String regex = "\\$\\$" + key  + "\\$\\$";
            s = s.replaceAll(regex, (String)values.get(key));
        }
        return s;
    }

}
