/*
 * Created on Jun 24, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


/**
 * @author art
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class WdkLogManager {

    public static final WdkLogManager INSTANCE = new WdkLogManager();
    
    private List loggers = new ArrayList();
    
    private String logFilename;
    
    /**
     * @return Returns the logFilename.
     */
    public String getLogFilename() {
        return logFilename;
    }
    
    /**
     * @param logFilename The logFilename to set.
     */
    public void setLogFilename(String logFilename) {
        this.logFilename = logFilename;
    }
    
    private WdkLogManager() {
        // Hide constructor
    }
    
    /**
     * @param string
     * @return
     */
    public static Logger getLogger(String name) {
        INSTANCE.addLoggerName(name);
        return Logger.getLogger(name);
    }

    /**
     * @param name
     */
    private void addLoggerName(String name) {
        loggers.add(name);
    }

    
    
}
