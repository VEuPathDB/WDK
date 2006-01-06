package org.gusdb.wdk.service;

/**
 * 
 */

/**
 * @author Jerric
 * @created Nov 2, 2005
 */
public interface IProcessor {

    public String[][] invoke(String[] params, String[] values, String[] cols)
            throws WdkServiceException;
}
