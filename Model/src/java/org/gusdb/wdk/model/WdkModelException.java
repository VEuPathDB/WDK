/*
 * Created on Jun 22, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.gusdb.gus.wdk.model;


/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WdkModelException extends Exception {

    /**
     * @param string
     */
    public WdkModelException(String string) {
        super(string);
    }

    /**
     * @param e
     */
    public WdkModelException(Exception e) {
        super(e);
    }

    /**
     * @param msg
     * @param e
     */
    public WdkModelException(String msg, Exception e) {
        super(msg, e);
    }

    
    
}
