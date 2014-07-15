/**
 * 
 */
package org.gusdb.wdk.model.test;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.question.QuestionSet;
import org.gusdb.wdk.model.user.UserFactory;

/**
 * @author xingao
 * 
 */
@Deprecated
public class HistoryRecycler implements Runnable {

    private static final String STOP_SIGNAL_FILE = "recycle-history.stop";
    private static final int USER_EXPIRE_TIME = 24; // in hours
    private String modelName;
    private int interval;

    public HistoryRecycler(String modelName, int interval) {
        this.modelName = modelName;
        this.interval = interval;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        while (!isStopping()) {
            try {
                // recycle the histories periodically
                long start = System.currentTimeMillis();
                recycle();
                long end = System.currentTimeMillis();
                System.out.println("spent " + ((end - start) / 1000.0)
                        + " seconds.");

                // then sleep for a total interval time, but quit if necessary
                for (int i = 0; i < interval * 12; i++) {
                    // check every 5 seconds
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {}
                    if (isStopping()) break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(-1);
            }
        }
    }

    private boolean isStopping() {
        String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);
        File stopFile = new File(gusHome, "/config/" + STOP_SIGNAL_FILE);
        return stopFile.exists();
    }

    private void recycle() throws WdkModelException, WdkUserException {
        System.out.println("========== Start recycling histories on "
                + modelName + " ==========");
        // construct model
        String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);
        WdkModel wdkModel = WdkModel.construct(modelName, gusHome);
        UserFactory factory = wdkModel.getUserFactory();

        // construct query signature
        Map<String, String> signatures = new LinkedHashMap<String, String>();
        QuestionSet[] qsets = wdkModel.getAllQuestionSets();
        for (QuestionSet qset : qsets) {
            Question[] questions = qset.getQuestions();
            for (Question question : questions) {
                signatures.put(question.getFullName(),
                        question.getQuery().getChecksum(false));
            }
        }

        // in this version, do not delete invalid histories
        // remove invalid histories
        // factory.deleteInvalidHistories(signatures);

        // remove expired users
        System.out.println("Deleting expired guest users...");
        factory.deleteExpiredUsers(USER_EXPIRE_TIME);

        System.out.println("========== Finished recycling histories on "
                + modelName + " ==========");
    }

    public static void printUsage() {
        String command = System.getProperty("cmdName");

        System.err.println("Usage: " + command + " -model <model_name> "
                + "[-interval <minutes>]");
        System.err.println("\t\t<model_name>\t\tthe name of the WDK model "
                + "that this command works on;");
        System.err.println("\t\t<interval>\t\tthe interval between each "
                + "recycling of the histories, in minute.");
        System.err.println();
        System.exit(-1);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        String modelName = null;
        int interval = 0;

        // validate input parameters
        if (args.length == 2) {
            if (!args[0].equalsIgnoreCase("-model")) printUsage();
            modelName = args[1];
            interval = 60;
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("-model")
                    && args[2].equalsIgnoreCase("-interval")) {
                modelName = args[1];
                interval = Integer.parseInt(args[3]);
            } else if (args[2].equalsIgnoreCase("-model")
                    && args[0].equalsIgnoreCase("-interval")) {
                modelName = args[3];
                interval = Integer.parseInt(args[1]);
            } else {
                printUsage();
            }
        } else {
            printUsage();
        }

        HistoryRecycler recycler = new HistoryRecycler(modelName, interval);
        recycler.run();
    }
}
