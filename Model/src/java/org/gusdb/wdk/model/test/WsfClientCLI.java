/**
 * 
 */
package org.gusdb.wdk.model.test;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.Option;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wsf.client.WsfService;
import org.gusdb.wsf.client.WsfServiceServiceLocator;
import org.gusdb.wsf.plugin.WsfRequest;
import org.gusdb.wsf.plugin.WsfResponse;
import org.gusdb.wsf.util.BaseCLI;

/**
 * @author xingao
 * 
 */
public class WsfClientCLI extends BaseCLI {

    private static final String ARG_WEB_SERVICE_URL = "url";
    private static final String ARG_PROCESS_NAME = "process";
    private static final String ARG_CONTEXT = "context";
    private static final String ARG_PARAMS = "params";
    private static final String ARG_COLUMNS = "columns";

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
        addMultiValueOption(ARG_CONTEXT, true, Option.UNLIMITED_VALUES, null,
                "The context info for the invocation. It must be key value "
                        + "pairs, for example, key1 value1 key2 value2...");
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

        WsfRequest request = new WsfRequest();

        String[] contextValues = (String[]) getOptionValue(ARG_COLUMNS);
        String[] columns = (String[]) getOptionValue(ARG_COLUMNS);

        // convert the paramErrors into params
        String[] paramValues = (String[]) getOptionValue(ARG_PARAMS);
        HashMap<String, String> params = new HashMap<String, String>();
        for (int i = 0; i < paramValues.length; i += 2) {
            String paramName = paramValues[i];
            String paramValue = (i + 1 < paramValues.length) ? paramValues[i + 1]
                    : "";
            params.put(paramName, paramValue);
        }
        request.setParams(params);

        request.setOrderedColumns(columns);

        // prepare context
        HashMap<String, String> context = new HashMap<String, String>();
        for (int i = 0; i < contextValues.length; i += 2) {
            String key = contextValues[i];
            String value = (i + 1 < contextValues.length) ? contextValues[i + 1]
                    : "";
            context.put(key, value);
        }
        request.setContext(context);

        printParams(serviceUrl, processName, request);
        try {
            System.out.println("Invoking web service...");
            long start = System.currentTimeMillis();

            // invoke web service
            WsfServiceServiceLocator locator = new WsfServiceServiceLocator();
            WsfService client = locator.getWsfService(new URL(serviceUrl));

            // get the response from the web service
            WsfResponse result = client.invoke(request.toString());
            long end = System.currentTimeMillis();

            printResult(result, columns);

            System.out.println("Invocation took: " + ((end - start) / 1000.0)
                    + " seconds.");
        } catch (Exception ex) {
            throw ex;
        }
    }

    private void printParams(String serviceUrl, String processName,
            WsfRequest request) {
        System.out.println("============== Input ==============");
        System.out.println("Service Url:\t" + serviceUrl);
        System.out.println("Process Name:\t" + processName);

        Map<?, ?> params = request.getParams();
        System.out.println("Parameters:\n");
        for (Object param : params.keySet()) {
            System.out.println("\t" + param + "=" + params.get(param));
        }

        String[] columns = request.getOrderedColumns();
        System.out.println("Columns:\t" + Utilities.fromArray(columns));

        Map<String, String> context = request.getContext();
        System.out.println("Context:\n");
        for (String key : context.keySet()) {
            System.out.println("\t" + key + "=" + context.get(key));
        }

        System.out.println("===================================");
    }

    private void printResult(WsfResponse result, String[] columns) {
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
