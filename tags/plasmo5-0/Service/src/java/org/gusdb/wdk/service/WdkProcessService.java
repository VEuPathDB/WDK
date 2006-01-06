/**
 * 
 */
package org.gusdb.wdk.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Jerric
 * @created Nov 2, 2005
 */
public class WdkProcessService {

    private static final boolean DEBUG = true;

    private static Map<String, String> processors = new HashMap<String, String>();

    static {
        // load configurations
        Properties prop = new Properties();
        String root = System.getProperty("webservice.home");
        File configFile = new File(root,
                "WEB-INF/wdkService-config.xml");
        try {
            // TEST
            if (DEBUG) System.out.println(configFile.getAbsolutePath());
            prop.loadFromXML(new FileInputStream(configFile));

            for (Object key : prop.keySet()) {
                String propName = (String) key;
                if (propName.endsWith("Processor")) {
                    String propValue = prop.getProperty(propName);
                    processors.put(propName, propValue);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
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
    public String[][] invoke(String processName, String[] params,
            String[] values, String[] cols) throws WdkServiceException {
        // use reflection to load the parser
        System.out.println("WdkProcessService.invoke(" + processName + ")");

        // check if the processor valid
        if (!processors.containsKey(processName)) {
            System.out.println("Invalid processor name: " + processName);
            throw new WdkServiceException("Invalid processor name: "
                    + processName);
        }

        try {
            String processClass = processors.get(processName);
            IProcessor processor = loadProcessor(processClass);
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
