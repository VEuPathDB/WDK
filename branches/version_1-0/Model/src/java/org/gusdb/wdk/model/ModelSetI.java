/*
 * Created on Jun 24, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.gusdb.wdk.model;


/**
 * @author art
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface ModelSetI {

    /**
     * @return
     */
    String getName();

    /**
     * @param elementName
     * @return
     */
    Object getElement(String elementName);

    /**
     * @param model
     * @throws WdkModelException
     */
    void setResources(WdkModel model) throws WdkModelException;

    /**
     * @param model
     * @throws WdkModelException
     */
    void resolveReferences(WdkModel model) throws WdkModelException;

}
