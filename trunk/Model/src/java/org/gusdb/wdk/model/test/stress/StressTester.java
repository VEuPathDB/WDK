/**
 * 
 */
package org.gusdb.wdk.model.test.stress;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import javax.sql.DataSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.gusdb.wdk.model.FlatVocabParam;
import org.gusdb.wdk.model.Param;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.QuestionSet;
import org.gusdb.wdk.model.RDBMSPlatformI;
import org.gusdb.wdk.model.Reference;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.implementation.SqlUtils;
import org.gusdb.wdk.model.test.CommandHelper;
import org.gusdb.wdk.model.test.SanityModel;
import org.gusdb.wdk.model.test.SanityQuestion;
import org.gusdb.wdk.model.test.SanityRecord;
import org.gusdb.wdk.model.test.SanityTestXmlParser;
import org.gusdb.wdk.model.test.SanityXmlQuestion;
import org.gusdb.wdk.model.test.stress.StressTestRunner.RunnerState;
import org.gusdb.wdk.model.test.stress.StressTestTask.ResultType;

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
    public static final String FIELD_MIN_DELAY_TIME = "MinDelayTime";
    public static final String FIELD_URL_PREFIX = "url";

    public static final String TYPE_HOME_URL = "HomeUrl";
    public static final String TYPE_QUESTION_URL = "QuestionUrl";
    public static final String TYPE_XML_QUESTION_URL = "XmlQuestionUrl";
    public static final String TYPE_RECORD_URL = "RecordUrl";

    public static final String TABLE_STRESS_RESULT = "stress_result";

    private static Logger logger = Logger.getLogger(StressTester.class);

    private List<UrlItem> urlPool;
    private Map<String, Map<String, Set<String>>> questionCache;

    private String questionUrlPattern;
    private String xmlQuestionUrlPattern;
    private String recordUrlPattern;
    private String homeUrlPattern;
    private Map<String, String> otherUrls;

    private List<StressTestRunner> runners;

    private String modelName;
    private File configDir;

    private int maxDelayTime;
    private int minDelayTime;

    private Random rand;

    private long testTag;
    private DataSource dataSource;
    private PreparedStatement preparedStatement;

    private long finishedCount;
    private long succeededCount;

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

        urlPool = new ArrayList<UrlItem>();
        otherUrls = new LinkedHashMap<String, String>();
        questionCache = new LinkedHashMap<String, Map<String, Set<String>>>();
        runners = new ArrayList<StressTestRunner>();
        rand = new Random(System.currentTimeMillis());
        finishedCount = succeededCount = 0;

        this.modelName = modelName;
        configDir = new File(System.getProperty("configDir"));

        // load the model
        WdkModel wdkModel = WdkModel.construct(modelName);
        dataSource = wdkModel.getPlatform().getDataSource();

        // initialize the stress-test result table
        try {
            initializeResultTable(wdkModel);
            // get a new test_tag
            testTag = getNewTestTag();
            System.out.println("The curent test tag is: " + testTag);
        } catch (SQLException ex) {
            throw new WdkModelException(ex);
        }

        // load configurations
        loadProperties(wdkModel);
        // compose the testing urls
        composeUrls(wdkModel);
    }

    private void initializeResultTable(WdkModel wdkModel) throws SQLException {
        // check if result table exists
        try {
            ResultSet rs = SqlUtils.getResultSet(dataSource, "SELECT * FROM "
                    + TABLE_STRESS_RESULT);
            SqlUtils.closeResultSet(rs);
        } catch (SQLException e) {
            // table doesn't exist, create it
            RDBMSPlatformI platform = wdkModel.getPlatform();
            String numericType = platform.getNumberDataType();
            String textType = platform.getClobDataType();

            StringBuffer sb = new StringBuffer();
            sb.append("CREATE TABLE " + TABLE_STRESS_RESULT + " (");
            sb.append("test_tag " + numericType + "(20) not null, ");
            sb.append("task_id " + numericType + "(20) not null, ");
            sb.append("runner_id " + numericType + "(20) not null, ");
            sb.append("task_type varchar(100) not null, ");
            sb.append("start_time " + numericType + "(20) not null, ");
            sb.append("end_time " + numericType + "(20) not null, ");
            sb.append("result_type varchar(100) not null, ");
            sb.append("result_message " + textType + ", ");
            sb.append(" PRIMARY KEY(test_tag, task_id))");

            // create the result table
            SqlUtils.executeUpdate(dataSource, sb.toString());
        }
        // initialize update prepared statement
        StringBuffer sb = new StringBuffer();
        sb.append("INSERT INTO " + TABLE_STRESS_RESULT);
        sb
                .append(" (test_tag, task_id, runner_id, task_type, start_time, end_time, result_type, result_message)");
        sb.append(" VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
        preparedStatement = SqlUtils.getPreparedStatement(dataSource, sb
                .toString());
    }

    private long getNewTestTag() throws SQLException {
        ResultSet rs = SqlUtils.getResultSet(dataSource,
                "SELECT count(0), max(test_tag) FROM " + TABLE_STRESS_RESULT);
        long testTag = 0;
        rs.next();
        int count = rs.getInt(1);
        if (count > 0) testTag = rs.getLong(2);
        SqlUtils.closeResultSet(rs);
        return (testTag + 1);
    }

    private void loadProperties(WdkModel wdkModel)
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
        String maxDelay = properties.getProperty(FIELD_MAX_DELAY_TIME);
        maxDelayTime = Integer.parseInt(maxDelay);
        String minDelay = properties.getProperty(FIELD_MIN_DELAY_TIME);
        minDelayTime = Integer.parseInt(minDelay);

        // load other urls
        for (Object key : properties.keySet()) {
            String urlKey = (String) key;
            if (!urlKey.startsWith(FIELD_URL_PREFIX)) continue;
            String urlValue = properties.getProperty(urlKey);
            otherUrls.put(urlValue, urlKey);
        }
    }

    private void composeUrls(WdkModel wdkModel) throws WdkUserException,
            WdkModelException, URISyntaxException, IOException {
        logger.debug("Composing test urls...");
        // add home url into the pool
        if (homeUrlPattern != null) {
            UrlItem homeUrl = new UrlItem(homeUrlPattern, TYPE_HOME_URL);
            urlPool.add(homeUrl);
        }

        // get other urls
        for (String urlKey : otherUrls.keySet()) {
            String urlValue = otherUrls.get(urlKey);
            UrlItem urlItem = new UrlItem(urlKey, urlValue);
            urlPool.add(urlItem);
        }

        // compose urls from sanity model
        composeFromSanityModel(wdkModel);

        // compose urls from stress test template
        composeFromTemplate(wdkModel);
    }

    private void composeFromSanityModel(WdkModel wdkModel)
            throws MalformedURLException, WdkModelException, WdkUserException,
            UnsupportedEncodingException {
        logger.debug("Loading sanity model...");

        File sanityXmlFile = new File(configDir, modelName + "-sanity.xml");
        File modelPropFile = new File(configDir, modelName + ".prop");
        File sanitySchemaFile = new File(System.getProperty("sanitySchemaFile"));
        SanityModel sanityModel = SanityTestXmlParser.parseXmlFile(
                sanityXmlFile.toURL(), modelPropFile.toURL(), sanitySchemaFile
                        .toURL());

        // get sanity questions from the sanity model
        SanityQuestion[] questions = sanityModel.getAllSanityQuestions();
        for (SanityQuestion question : questions) {
            // get question full name
            String fullName = question.getName();

            // get parameters
            Map<String, Object> params = question.getParamHash();//
            // get original parameters
            Reference questionRef = new Reference(question.getRef());
            QuestionSet questionSet = wdkModel.getQuestionSet(questionRef
                    .getSetName());
            Question q = questionSet.getQuestion(questionRef.getElementName());
            Map<String, Param> originalParams = q.getParamMap();

            // cache the question with all possible param combinations
            Map<String, Set<String>> paramMap = questionCache.get(fullName);
            if (paramMap == null) {
                paramMap = new LinkedHashMap<String, Set<String>>();
                questionCache.put(fullName, paramMap);

                // add questionFullName parameter
                Set<String> paramValues = new LinkedHashSet<String>();
                paramValues.add(fullName);
                paramMap.put("questionFullName", paramValues);

                // add questionSubmit parameter
                paramValues = new LinkedHashSet<String>();
                paramValues.add("Get Answer");
                paramMap.put("questionSubmit", paramValues);
            }

            // iterate all other parameters
            Set<String> paramKeys = params.keySet();
            for (String paramName : paramKeys) {
                String value = params.get(paramName).toString();
                // check if the parameter is a flatvocab parameter
                Param param = originalParams.get(paramName);
                if (param instanceof FlatVocabParam) {
                    paramName = "myMultiProp(" + paramName + ")";
                } else {
                    paramName = "myProp(" + paramName + ")";
                }
                // get the parameter cache
                Set<String> paramValues = paramMap.get(paramName);
                if (paramValues == null) {
                    paramValues = new LinkedHashSet<String>();
                    paramMap.put(paramName, paramValues);
                }
                paramValues.add(value);
            }
        }

        // get sanity xml questions from the sanity model
        SanityXmlQuestion[] xmlQuestions = sanityModel.getSanityXmlQuestions();
        for (SanityXmlQuestion xmlQuestion : xmlQuestions) {
            // get question full name
            String fullName = xmlQuestion.getName();
            StringBuffer sb = new StringBuffer(xmlQuestionUrlPattern);
            sb.append("?name=" + fullName);
            UrlItem xmlQuestionUrl = new UrlItem(sb.toString(),
                    TYPE_XML_QUESTION_URL);
            urlPool.add(xmlQuestionUrl);
        }

        // get sanity records from the sanity model
        SanityRecord[] records = sanityModel.getAllSanityRecords();
        for (SanityRecord record : records) {
            // get record full name
            String fullName = record.getName();

            String projectID = record.getProjectID();
            String primaryKey = record.getPrimaryKey();
            // compose url
            StringBuffer sb = new StringBuffer(recordUrlPattern);
            sb.append("?name=" + fullName);
            if (projectID != null && projectID.length() != 0)
                sb.append("&project_id="
                        + URLEncoder.encode(projectID, "UTF-8"));
            sb.append("&primary_key=" + URLEncoder.encode(primaryKey, "UTF-8"));
            UrlItem recordUrl = new UrlItem(sb.toString(), TYPE_RECORD_URL);
            urlPool.add(recordUrl);
        }
    }

    private void composeFromTemplate(WdkModel wdkModel) throws IOException,
            WdkUserException {
        logger.debug("Loading test cases from template files");
        File templateFile = new File(configDir, modelName + "-stress.template");
        BufferedReader in = new BufferedReader(new FileReader(templateFile));
        String line;
        while ((line = in.readLine()) != null) {
            line = line.trim();
            if (line.length() == 0 || line.equals("//") || line.startsWith("#"))
                continue;

            // a start of an item
            if (line.toLowerCase().startsWith("question")) {
                // read question name
                int pos = line.indexOf(":");
                String fullName = line.substring(pos + 1).trim();

                // get question and the parameter map
                pos = fullName.indexOf(".");
                String qsetName = fullName.substring(0, pos);
                String qName = fullName.substring(pos + 1);
                QuestionSet qset = wdkModel.getQuestionSet(qsetName);
                Question question = qset.getQuestion(qName);
                Map<String, Param> originalParams = question.getParamMap();

                // cache the question with all possible param combinations
                Map<String, Set<String>> paramMap = questionCache.get(fullName);
                if (paramMap == null) {
                    paramMap = new LinkedHashMap<String, Set<String>>();
                    questionCache.put(fullName, paramMap);

                    // add questionFullName parameter
                    Set<String> paramValues = new LinkedHashSet<String>();
                    paramValues.add(fullName);
                    paramMap.put("questionFullName", paramValues);

                    // add questionSubmit parameter
                    paramValues = new LinkedHashSet<String>();
                    paramValues.add("Get Answer");
                    paramMap.put("questionSubmit", paramValues);
                }

                // read parameters
                while ((line = in.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("#")) continue;
                    if (line.equalsIgnoreCase("//")) break;
                    if (line.toLowerCase().startsWith("param")) {
                        pos = line.indexOf(":");
                        String parts = line.substring(pos + 1);
                        pos = parts.indexOf("=");

                        // prepare parameter name
                        String paramName = parts.substring(0, pos).trim();
                        Param param = originalParams.get(paramName);
                        if (param instanceof FlatVocabParam) {
                            paramName = "myMultiProp(" + paramName + ")";
                        } else {
                            paramName = "myProp(" + paramName + ")";
                        }

                        // get the parameter cache
                        Set<String> paramValues = paramMap.get(paramName);
                        if (paramValues == null) {
                            paramValues = new LinkedHashSet<String>();
                            paramMap.put(paramName, paramValues);
                        }

                        String[] values = parts.substring(pos + 1).split(",");
                        for (String value : values) {
                            paramValues.add(value.trim());
                        }
                    }
                }
            } else if (line.toLowerCase().startsWith("record")) {
                // get record class name
                int pos = line.indexOf(":");
                String parts = line.substring(pos + 1).trim();
                pos = parts.indexOf("=");
                String rcName = parts.substring(0, pos).trim();
                String[] primaryKeys = parts.substring(pos + 1).split(",");
                // compose UrlItem
                for (String primaryKey : primaryKeys) {
                    primaryKey = primaryKey.trim();
                    if (primaryKey.length() == 0) continue;
                    StringBuffer sb = new StringBuffer(recordUrlPattern);
                    sb.append("?name=" + rcName);
                    sb.append("&primary_key="
                            + URLEncoder.encode(primaryKey, "UTF-8"));
                    UrlItem recordUrl = new UrlItem(sb.toString(),
                            TYPE_RECORD_URL);
                    urlPool.add(recordUrl);
                }
            } else if (line.toLowerCase().startsWith("xmlquestion")) {
                // get xml question class name
                int pos = line.indexOf(":");
                String xmlqname = line.substring(pos + 1).trim();
                // compose UrlItem
                StringBuffer sb = new StringBuffer(xmlQuestionUrlPattern);
                sb.append("?name=" + xmlqname);
                UrlItem xmlQuestionUrl = new UrlItem(sb.toString(),
                        TYPE_XML_QUESTION_URL);
                urlPool.add(xmlQuestionUrl);
            }
        }
    }

    public void runTest(int numThreads) throws SQLException {
        logger.info("Running stress test...");

        // create stress test runners
        for (int i = 0; i < numThreads; i++) {
            StressTestRunner runner = new StressTestRunner();
            Thread thread = new Thread(runner);
            thread.start();
            runners.add(runner);
        }

        // wait till all tasks are executed
        while (!isStopping()) {
            // get the finished tasks and release runner
            for (StressTestRunner runner : runners) {
                if (runner.getState() == RunnerState.Finished) {
                    try {
                        saveFinishedTask(runner.popFinishedTask());
                    } catch (SQLException ex) {
                        logger.error(ex);
                        ex.printStackTrace();
                    }
                }
                // randomly pick a task to the idling runners;
                if (runner.getState() == RunnerState.Idle) {
                    StressTestTask task = createTask();
                    int delay = rand.nextInt(maxDelayTime - minDelayTime)
                            + minDelayTime;
                    try {
                        runner.assignTask(task, delay);
                    } catch (InvalidStatusException ex) {
                        logger.error(ex);
                        ex.printStackTrace();
                    }
                }
            }

            // print out the current statistics
            logger.info("Idle: " + getIdleCount() + "\tBusy: " + getBusyCount()
                    + "\tFinished: " + finishedCount + "/" + succeededCount);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {}
            // print out current progress
        }
        // stop runners
        for (StressTestRunner runner : runners) {
            runner.stop();
        }
        // wait until all runners are stopped
        boolean stopped = false;
        while (!stopped) {
            stopped = true;
            for (StressTestRunner runner : runners) {
                if (!runner.isStopped()) {
                    stopped = false;
                    break;
                }
            }
            if (!stopped) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {}
            }
        }
        SqlUtils.closeStatement(preparedStatement);
        logger.info("Stress Test is finished.");
        System.out.println("Stress Test is finished. The test tag is: "
                + testTag);
    }

    private boolean isStopping() {
        // check if the exit condition is met; that is, if a "model-stress.stop"
        // file is present
        File stopFile = new File(configDir, modelName + "-stress.stop");
        return (stopFile.exists());
    }

    private StressTestTask createTask() {
        // choose from urlPool or question cache
        UrlItem urlItem;
        if (rand.nextBoolean()) { // get from question cache
            // choose question
            int index = rand.nextInt(questionCache.size());
            int i = 0;
            Iterator<Map<String, Set<String>>> itQuestion = questionCache
                    .values().iterator();
            while (i < index) {
                i++;
                itQuestion.next();
            }
            Map<String, Set<String>> paramMap = itQuestion.next();

            // create UrlItem. and pick parameters
            urlItem = new UrlItem(questionUrlPattern, TYPE_QUESTION_URL);
            for (String paramName : paramMap.keySet()) {
                Set<String> values = paramMap.get(paramName);
                // choose value
                index = rand.nextInt(values.size());
                i = 0;
                Iterator<String> itValue = values.iterator();
                while (i < index) {
                    i++;
                    itValue.next();
                }
                if (itValue.hasNext())
                    urlItem.addParameter(paramName, itValue.next());
            }
        } else { // get from url pool
            urlItem = urlPool.get(rand.nextInt(urlPool.size()));
        }
        return new StressTestTask(urlItem);
    }

    private void saveFinishedTask(StressTestTask task) throws SQLException {
        preparedStatement.setLong(1, testTag);
        preparedStatement.setLong(2, task.getTaskId());
        preparedStatement.setInt(3, task.getRunnerId());
        preparedStatement.setString(4, task.getUrlItem().getUrlType());
        preparedStatement.setLong(5, task.getStartTime());
        preparedStatement.setLong(6, task.getFinishTime());
        preparedStatement.setString(7, task.getResultType().name());
        preparedStatement.setString(8, task.getResultMessage());

        preparedStatement.execute();
        finishedCount++;
        if (task.getResultType() == ResultType.Succeeded) succeededCount++;
    }

    private int getIdleCount() {
        int idle = 0;
        for (StressTestRunner runner : runners) {
            if (runner.getState() != RunnerState.Executing) idle++;
        }
        return idle;
    }

    private int getBusyCount() {
        int busy = 0;
        for (StressTestRunner runner : runners) {
            if (runner.getState() == RunnerState.Executing) busy++;
        }
        return busy;
    }

    private static Options declareOptions() {
        String[] names = { "model", "threads" };
        String[] descs = {
                "the name of the model.  This is used to find the Model XML "
                        + "file ($GUS_HOME/config/model_name.xml) the Model "
                        + "property file ($GUS_HOME/config/model_name.prop) "
                        + "and the Model config file "
                        + "($GUS_HOME/config/model_name-config.xml)",
                "The number of threads used to run the tasks." };
        boolean[] required = { true, true };
        int[] args = { 0, 0 };

        return CommandHelper.declareOptions(names, descs, required, args);
    }

    /**
     * @param args
     * @throws WdkModelException
     * @throws IOException
     * @throws InvalidPropertiesFormatException
     * @throws URISyntaxException
     * @throws WdkUserException
     * @throws SQLException
     */
    public static void main(String[] args)
            throws InvalidPropertiesFormatException, IOException,
            WdkModelException, URISyntaxException, WdkUserException,
            SQLException {

        String cmdName = System.getProperties().getProperty("cmdName");

        // process args
        Options options = declareOptions();
        CommandLine cmdLine = CommandHelper
                .parseOptions(cmdName, options, args);

        String modelName = cmdLine.getOptionValue("model");
        String strThreads = cmdLine.getOptionValue("threads");
        int numThreads = Integer.parseInt(strThreads);

        // create tester
        StressTester tester = new StressTester(modelName);
        // run tester
        tester.runTest(numThreads);
        System.exit(0);
    }
}
