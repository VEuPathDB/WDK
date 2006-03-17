/**
 * 
 */
package org.gusdb.wdk.model.test.stress;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.gusdb.wdk.model.FlatVocabParam;
import org.gusdb.wdk.model.Param;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.QuestionSet;
import org.gusdb.wdk.model.Reference;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.test.CommandHelper;
import org.gusdb.wdk.model.test.SanityModel;
import org.gusdb.wdk.model.test.SanityQuestion;
import org.gusdb.wdk.model.test.SanityRecord;
import org.gusdb.wdk.model.test.SanityTestXmlParser;
import org.gusdb.wdk.model.test.SanityXmlQuestion;

/**
 * @author: Jerric
 * @created: Mar 15, 2006
 * @modified by: Jerric
 * @modified at: Mar 15, 2006
 * 
 */
public class StressTester {

    public static final String FIELD_QUESTION_URL = "QuestionUrl";
    public static final String FIELD_XML_QUESTION_URL = "XmlQuestionUrl";
    public static final String FIELD_RECORD_URL = "RecordUrl";
    public static final String FIELD_HOME_URL = "HomeUrl";
    public static final String FIELD_MAX_DELAY_TIME = "MaxDelayTime";

    public static final String TYPE_HOME_URL = "HomeUrl";
    public static final String TYPE_QUESTION_URL = "QuestionUrl";
    public static final String TYPE_XML_QUESTION_URL = "XmlQuestionUrl";
    public static final String TYPE_RECORD_URL = "RecordUrl";

    private static Logger logger = Logger.getLogger(StressTester.class);

    private Map<URL, String> urlPool;

    private String questionUrlPattern;
    private String xmlQuestionUrlPattern;
    private String recordUrlPattern;
    private String homeUrlPattern;
    private int maxDelayTime;

    private Random rand;

    /**
     * @throws IOException
     * @throws InvalidPropertiesFormatException
     * @throws WdkModelException
     * @throws URISyntaxException
     * @throws WdkUserException
     * 
     */
    public StressTester(String modelName)
            throws InvalidPropertiesFormatException, IOException,
            WdkModelException, URISyntaxException, WdkUserException {
        logger.info("Initializing stress test on " + modelName);

        urlPool = new HashMap<URL, String>();
        rand = new Random(System.currentTimeMillis());

        File configDir = new File(System.getProperty("configDir"));
        // load configurations
        loadProperties(configDir, modelName);
        // load sanity model
        SanityModel sanityModel = loadSanityModel(configDir, modelName);
        // compose the testing urls
        composeUrls(modelName, sanityModel);
    }

    private void loadProperties(File configDir, String modelName)
            throws InvalidPropertiesFormatException, IOException {
        logger.debug("Loading stress test configurations...");

        // load stress test configuration file
        File stressConfigFile = new File(configDir, modelName
                + "-stress-config.xml");
        InputStream in = new FileInputStream(stressConfigFile);
        Properties properties = new Properties();
        properties.loadFromXML(in);

        // load properties
        questionUrlPattern = properties.getProperty(FIELD_QUESTION_URL);
        xmlQuestionUrlPattern = properties.getProperty(FIELD_XML_QUESTION_URL);
        recordUrlPattern = properties.getProperty(FIELD_RECORD_URL);
        homeUrlPattern = properties.getProperty(FIELD_HOME_URL);
        String delay = properties.getProperty(FIELD_MAX_DELAY_TIME);
        maxDelayTime = Integer.parseInt(delay);
    }

    private SanityModel loadSanityModel(File configDir, String modelName)
            throws MalformedURLException, WdkModelException {
        logger.debug("Loading sanity model...");

        File sanityXmlFile = new File(configDir, modelName + "-sanity.xml");
        File modelPropFile = new File(configDir, modelName + ".prop");
        File sanitySchemaFile = new File(System.getProperty("sanitySchemaFile"));
        SanityModel sanityModel = SanityTestXmlParser.parseXmlFile(
                sanityXmlFile.toURL(), modelPropFile.toURL(),
                sanitySchemaFile.toURL());
        return sanityModel;
    }

    private void composeUrls(String modelName, SanityModel sanityModel)
            throws URISyntaxException, MalformedURLException, WdkUserException,
            WdkModelException {
        logger.debug("Composing test urls...");

        // load the model
        WdkModel wdkModel = WdkModel.construct(modelName);

        // add home url into the pool
        URL homeUrl = new URL(homeUrlPattern);
        urlPool.put(homeUrl, TYPE_HOME_URL);

        // get sanity questions from the sanity model
        SanityQuestion[] questions = sanityModel.getAllSanityQuestions();
        for (SanityQuestion question : questions) {
            // get question full name
            String fullName = question.getName();
            // get parameters
            Map<String, Object> params = question.getParamHash();//
            // get original parameters
            Reference questionRef = new Reference(question.getRef());
            QuestionSet questionSet = wdkModel.getQuestionSet(questionRef.getSetName());
            Question q = questionSet.getQuestion(questionRef.getElementName());
            Map<String, Param> originalParams = q.getParamMap();

            // compose question url
            StringBuffer sb = new StringBuffer(questionUrlPattern);
            sb.append(fullName);
            Set<String> paramKeys = params.keySet();
            for (String paramName : paramKeys) {
                String value = params.get(paramName).toString();
                value = value.replaceAll(" ", "+");

                // check if the parameter is a flatvocab parameter
                Param param = originalParams.get(paramName);
                if (param instanceof FlatVocabParam) sb.append("&myMultiProp");
                else sb.append("&myProp");
                sb.append("%28" + paramName + "%29=" + value);
            }
            sb.append("&questionSubmit=Get+Answer");
            URI questionUri = new URI(sb.toString());
            urlPool.put(questionUri.toURL(), TYPE_QUESTION_URL);
        }

        // get sanity xml questions from the sanity model
        SanityXmlQuestion[] xmlQuestions = sanityModel.getSanityXmlQuestions();
        for (SanityXmlQuestion xmlQuestion : xmlQuestions) {
            // get question full name
            String fullName = xmlQuestion.getName();
            StringBuffer sb = new StringBuffer(xmlQuestionUrlPattern);
            sb.append(fullName);
            URI xmlQuestionUri = new URI(sb.toString());
            urlPool.put(xmlQuestionUri.toURL(), TYPE_XML_QUESTION_URL);
        }

        // get sanit records from the sanity model
        SanityRecord[] records = sanityModel.getAllSanityRecords();
        for (SanityRecord record : records) {
            // get record full name
            String fullName = record.getName();
            String projectID = record.getProjectID();
            String primaryKey = record.getPrimaryKey();
            // compose url
            StringBuffer sb = new StringBuffer(recordUrlPattern);
            sb.append(fullName);
            if (projectID != null && projectID.length() != 0)
                sb.append("&project_id=" + projectID);
            sb.append("&primary_key=" + primaryKey);
            URI recordUri = new URI(sb.toString());
            urlPool.put(recordUri.toURL(), TYPE_RECORD_URL);
        }
    }

    public StressTestTask[] runTest(int numThreads, int numReuqests) {
        logger.info("Running stress test...");
        // create a scheduler
        StressTestScheduler scheduler = createScheduler(numThreads, numReuqests);
        // start test
        Thread thread = new Thread(scheduler);
        thread.start();

        // wait till all tasks are executed
        boolean stopping = false;
        while (!scheduler.isFinished()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {}
            // print out current progress
            logger.info("Pending: " + scheduler.getPendingTaskCount()
                    + "\t Executing: " + scheduler.getExecutingTaskCount()
                    + "\t Finished: " + scheduler.getFinishedTaskCount());

            if (!scheduler.hasPendingTask()
                    && scheduler.getExecutingTaskCount() == 0 && !stopping) {
                scheduler.stop();
                stopping = true;
            }
        }
        return scheduler.getFinishedTasks();
    }

    private StressTestScheduler createScheduler(int numThreads, int numRequests) {
        logger.debug("Creating stress test scheduler...");

        StressTestScheduler scheduler = new StressTestScheduler(numThreads,
                maxDelayTime);
        // create test requests
        for (int i = 0; i < numRequests; i++) {
            int index = rand.nextInt(urlPool.size());
            URL url = null;
            String urlType = null;
            Iterator<URL> urls = urlPool.keySet().iterator();
            int idx = 0;
            while (urls.hasNext()) {
                url = urls.next();
                if (idx == index) {
                    urlType = urlPool.get(url);
                    break;
                }
                idx++;
            }
            StressTestTask task = new StressTestTask(url, urlType);
            scheduler.addTestTask(task);
        }
        return scheduler;
    }

    private static Options declareOptions() {
        String[] names = { "model", "threads", "tasks" };
        String[] descs = {
                "the name of the model.  This is used to find the Model XML file ($GUS_HOME/config/model_name.xml) the Model property file ($GUS_HOME/config/model_name.prop) and the Model config file ($GUS_HOME/config/model_name-config.xml)",
                "The number of threads used to run the tasks.",
                "The number of tasks to be executed" };
        boolean[] required = { true, true, true };
        int[] args = { 0, 0, 0 };

        return CommandHelper.declareOptions(names, descs, required, args);
    }

    /**
     * @param args
     * @throws WdkModelException
     * @throws IOException
     * @throws InvalidPropertiesFormatException
     * @throws URISyntaxException
     * @throws WdkUserException
     */
    public static void main(String[] args)
            throws InvalidPropertiesFormatException, IOException,
            WdkModelException, URISyntaxException, WdkUserException {

        String cmdName = System.getProperties().getProperty("cmdName");

        // process args
        Options options = declareOptions();
        CommandLine cmdLine = CommandHelper.parseOptions(cmdName, options, args);

        String modelName = cmdLine.getOptionValue("model");
        String strThreads = cmdLine.getOptionValue("threads");
        int numThreads = Integer.parseInt(strThreads);
        String strTasks = cmdLine.getOptionValue("tasks");
        int numTasks = Integer.parseInt(strTasks);

        // create tester
        StressTester tester = new StressTester(modelName);
        // run tester
        StressTestTask[] tasks = tester.runTest(numThreads, numTasks);
        // analyze the result
        StressTestAnalyzer analyzer = new StressTestAnalyzer(tasks);
        analyzer.print();
    }
}
