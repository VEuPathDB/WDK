/**
 * 
 */
package org.gusdb.wdk.model.test;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.Option;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wsf.client.WsfService;
import org.gusdb.wsf.client.WsfServiceServiceLocator;
import org.gusdb.wsf.plugin.WsfResult;
import org.gusdb.wsf.util.BaseCLI;

/**
 * @author xingao
 * 
 */
public class WsfClientCLI extends BaseCLI {

    private static final String ARG_WEB_SERVICE_URL = "url";
    private static final String ARG_PROCESS_NAME = "process";
    private static final String ARG_INVOKE_KEY = "invokeKey";
    private static final String ARG_PARAMS = "params";
    private static final String ARG_COLUMNS = "columns";

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String cmdName = System.getProperty("cmdName");
        WsfClientCLI client = new WsfClientCLI(cmdName,
                "Invoke a WSF web service directly");
        try {
            client.invoke(args);
        } finally {
            System.exit(0);
        }
    }

    /**
     * @param command
     * @param description
     */
    protected WsfClientCLI(String command, String description) {
        super((command == null) ? "wdkCache" : command, description);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wsf.util.BaseCLI#declareOptions()
     */
    @Override
    protected void declareOptions() {
        addSingleValueOption(ARG_WEB_SERVICE_URL, true, null, "Url to the Web"
                + "Service, for example, "
                + "http://localhost/webapp/services/WsfService.");
        addSingleValueOption(ARG_PROCESS_NAME, true, null, "The process, or "
                + "the WSF plugin full name.");
        addSingleValueOption(ARG_INVOKE_KEY, true, null, "The invoke key, "
                + "which is usually the full name of the calling query.");
        addMultiValueOption(ARG_PARAMS, true, Option.UNLIMITED_VALUES, null,
                "The parameter and value pairs, for example: "
                        + "param1 value1 param2 value2...");
        addMultiValueOption(ARG_COLUMNS, true, Option.UNLIMITED_VALUES, null,
                "The columns definition in the results. The order in this "
                        + "column list will be applied into the results, for "
                        + "example: column1 column2 column3...");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wsf.util.BaseCLI#invoke()
     */
    @Override
    public void execute() throws Exception {
        String serviceUrl = (String) getOptionValue(ARG_WEB_SERVICE_URL);
        String processName = (String) getOptionValue(ARG_PROCESS_NAME);
        String invokeKey = (String) getOptionValue(ARG_INVOKE_KEY);
        String[] paramValues = (String[]) getOptionValue(ARG_PARAMS);
        String[] columns = (String[]) getOptionValue(ARG_COLUMNS);

        // convert the paramErrors into params
        List<String> paramLists = new ArrayList<String>();
        for (int i = 0; i < paramValues.length; i += 2) {
            String paramName = paramValues[i];
            String paramValue = (i + 1 < paramValues.length)
                    ? paramValues[i + 1] : "";
            paramLists.add(paramName + "=" + paramValue);
        }
        String[] params = new String[paramLists.size()];
        paramLists.toArray(params);

        printParams(serviceUrl, processName, invokeKey, params, columns);
        try {
            System.out.println("Invoking web service...");
            long start = System.currentTimeMillis();

            // invoke web service
            WsfServiceServiceLocator locator = new WsfServiceServiceLocator();
            WsfService client = locator.getWsfService(new URL(serviceUrl));

            // get the response from the web service
            WsfResult result = client.invokeEx(processName, invokeKey, params,
                    columns);
            long end = System.currentTimeMillis();

            printResult(result, columns);

            System.out.println("Invocation took: " + ((end - start) / 1000.0)
                    + " seconds.");
        } catch (Exception ex) {
            throw ex;
        }
    }

    private void printParams(String serviceUrl, String processName,
            String invokeKey, String[] params, String[] columns) {
        System.out.println("============== Input ==============");
        System.out.println("Service Url:\t" + serviceUrl);
        System.out.println("Process Name:\t" + processName);
        System.out.println("Invoke Key:\t" + invokeKey);
        System.out.println("Parameters:");
        for (String param : params) {
            System.out.println("\t" + param);
        }
        System.out.println("Invoke Key:\t" + Utilities.fromArray(columns));
        System.out.println("===================================");
    }

    private void printResult(WsfResult result, String[] columns) {
        System.out.println("============== Results ==============");
        System.out.println("Signal: " + result.getSignal());
        System.out.println("Message: " + result.getMessage());
        String[][] data = result.getResult();
        for (String column : columns) {
            System.out.print(column + "\t");
        }
        System.out.println();
        for (int row = 0; row < data.length; row++) {
            for (int col = 0; col < data[0].length; col++) {
                System.out.print(data[row][col] + "\t");
            }
            System.out.println();
        }
        System.out.println("=====================================");
    }
}
