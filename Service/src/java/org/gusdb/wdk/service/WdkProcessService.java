/**
 * 
 */
package org.gusdb.wdk.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Jerric
 * @created Nov 2, 2005
 */
public class WdkProcessService implements IProcessor {

    private static final boolean DEBUG = true;
    
    private static String processorName;

    static {
        // load configurations
        Properties prop = new Properties();
        String root = System.getProperty("catalina.home");
        File configFile = new File(root,
                "webapps/axis/WEB-INF/wdkService-config.xml");
        try {
            // TEST
            if (DEBUG) System.out.println(configFile.getAbsolutePath());

            prop.loadFromXML(new FileInputStream(configFile));
            processorName = prop.getProperty("Processor");
        } catch (IOException ex) {
            processorName = "blast.ncbi.NcbiProcessor";
        }

    }

    /**
     * 
     */
    public WdkProcessService() {}

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.service.IProcessor#invoke(java.lang.String[],
     *      java.lang.String[], java.lang.String[])
     */
    public String[][] invoke(String[] params, String[] values, String[] cols)
            throws WdkServiceException {
        // use reflection to load the parser
        System.out.println("WdkProcessService.invoke()");
        
        try {
            IProcessor processor = loadProcessor(processorName);
            // invoke process and obtain result
            return processor.invoke(params, values, cols);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            System.out.println(ex);
            throw new WdkServiceException(ex);
        } catch (InstantiationException ex) {
            ex.printStackTrace();
            System.out.println(ex);
            throw new WdkServiceException(ex);
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
            System.out.println(ex);
            throw new WdkServiceException(ex);
        }
    }

    private IProcessor loadProcessor(String className)
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException {
        Class processorClass = Class.forName(className);
        IProcessor processor = (IProcessor) processorClass.newInstance();
        return processor;
    }
}
