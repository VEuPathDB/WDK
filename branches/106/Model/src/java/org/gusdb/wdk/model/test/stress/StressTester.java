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
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.QuestionSet;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.DBPlatform;
import org.gusdb.wdk.model.dbms.SqlUtils;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.DatasetParam;
import org.gusdb.wdk.model.query.param.FlatVocabParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.test.CommandHelper;
import org.gusdb.wdk.model.test.stress.StressTestRunner.RunnerState;
import org.gusdb.wdk.model.test.stress.StressTestTask.ResultType;
import org.gusdb.wdk.model.xml.XmlQuestion;
import org.gusdb.wdk.model.xml.XmlQuestionSet;
import org.json.JSONException;
import org.xml.sax.SAXException;

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
    private String gusHome;

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
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws JSONException
     * @throws SQLException
     * @throws SAXException
     * @throws TransformerException
     * @throws TransformerFactoryConfigurationError
     * @throws ParserConfigurationException
     * @throws NoSuchAlgorithmException
     * 
     */
    public StressTester(String modelName)
            throws InvalidPropertiesFormatException, IOException,
            WdkModelException, URISyntaxException, WdkUserException,
            NoSuchAlgorithmException, ParserConfigurationException,
            TransformerFactoryConfigurationError, TransformerException,
            SAXException, SQLException, JSONException, InstantiationException,
            IllegalAccessException, ClassNotFoundException {
        logger.info("Initializing stress test on " + modelName);

        urlPool = new ArrayList<UrlItem>();
        otherUrls = new LinkedHashMap<String, String>();
        questionCache = new LinkedHashMap<String, Map<String, Set<String>>>();
        runners = new ArrayList<StressTestRunner>();
        rand = new Random(System.currentTimeMillis());
        finishedCount = succeededCount = 0;

        this.modelName = modelName;
        gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);

        // load the model
        WdkModel wdkModel = WdkModel.construct(modelName, gusHome);
        dataSource = wdkModel.getQueryPlatform().getDataSource();

        // initialize the stress-test result table
        try {
            initializeResultTable(wdkModel);
            // get a new test_tag
            testTag = getNewTestTag(wdkModel);
            System.out.println("The curent test tag is: " + testTag);
        } catch (SQLException ex) {
            throw new WdkModelException(ex);
        }

        // load configurations
        loadProperties(wdkModel);
        // compose the testing urls
        composeUrls(wdkModel);
    }

    private void initializeResultTable(WdkModel wdkModel) throws SQLException,
            WdkUserException, WdkModelException {
        // check if result table exists
        try {
            ResultSet rs = SqlUtils.executeQuery(wdkModel, dataSource,
                    "SELECT * FROM " + TABLE_STRESS_RESULT, "wdk-stress-result");
            SqlUtils.closeResultSet(rs);
        } catch (WdkModelException e) {
            // table doesn't exist, create it
            DBPlatform platform = wdkModel.getQueryPlatform();
            String numericType = platform.getNumberDataType(20);
            String textType = platform.getClobDataType();

            StringBuffer sb = new StringBuffer();
            sb.append("CREATE TABLE " + TABLE_STRESS_RESULT + " (");
            sb.append("test_tag " + numericType + " not null, ");
            sb.append("task_id " + numericType + " not null, ");
            sb.append("runner_id " + numericType + " not null, ");
            sb.append("task_type varchar(100) not null, ");
            sb.append("start_time " + numericType + " not null, ");
            sb.append("end_time " + numericType + " not null, ");
            sb.append("result_type varchar(100) not null, ");
            sb.append("result_message " + textType + ", ");
            sb.append(" PRIMARY KEY(test_tag, task_id))");

            // create the result table
            SqlUtils.executeUpdate(wdkModel, dataSource, sb.toString(),
                    "wdk-create-table");
        }
        // initialize update prepared statement
        StringBuffer sb = new StringBuffer();
        sb.append("INSERT INTO " + TABLE_STRESS_RESULT);
        sb.append(" (test_tag, task_id, runner_id, task_type, start_time, ");
        sb.append("end_time, result_type, result_message)");
        sb.append(" VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
        preparedStatement = SqlUtils.getPreparedStatement(dataSource,
                sb.toString());
    }

    private long getNewTestTag(WdkModel wdkModel) throws SQLException,
            WdkUserException, WdkModelException {
        ResultSet rs = SqlUtils.executeQuery(wdkModel, dataSource,
                "SELECT count(0), max(test_tag) FROM " + TABLE_STRESS_RESULT,
                "wdk-stress-next-tag");
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
        File stressConfigFile = new File(gusHome, "/config/" + modelName
                + "/stress-config.xml");
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
            WdkModelException, URISyntaxException, IOException,
            NoSuchAlgorithmException, SQLException, JSONException {
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

        // compose urls from the model
        composeFromModel(wdkModel);

        // compose urls from stress test template
        composeFromTemplate();
    }

    private void composeFromModel(WdkModel wdkModel) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        // compose urls for all xml questions
        XmlQuestionSet[] xmlqsets = wdkModel.getXmlQuestionSets();
        for (XmlQuestionSet xmlqset : xmlqsets) {
            XmlQuestion[] xmlqs = xmlqset.getQuestions();
            for (XmlQuestion xmlq : xmlqs) {
                String xmlqName = xmlq.getFullName();
                UrlItem xmlQuestionUrl = new UrlItem(xmlQuestionUrlPattern
                        + "?name=" + xmlqName, TYPE_XML_QUESTION_URL);
                urlPool.add(xmlQuestionUrl);
            }
        }

        // load questions from the model
        questionCache.clear();
        QuestionSet[] qsets = wdkModel.getAllQuestionSets();
        for (QuestionSet qset : qsets) {
            // skip the internal questions
            if (qset.isInternal()) continue;

            Question[] questions = qset.getQuestions();
            for (Question question : questions) {
                // create question cache, and create param stub, and load the
                // vocab params
                String qName = question.getFullName();
                Param[] params = question.getParams();
                Map<String, Set<String>> paramMap = new LinkedHashMap<String, Set<String>>();
                boolean unusable = false;
                for (Param param : params) {
                    Set<String> values = new LinkedHashSet<String>();
                    if (param instanceof FlatVocabParam) {
                        FlatVocabParam fvParam = (FlatVocabParam) param;
                        String[] terms = fvParam.getVocab(null); // assume independent param
                        for (String term : terms) {
                            values.add(term);
                        }
                    } else if (param instanceof AnswerParam
                            || param instanceof DatasetParam) {
                        unusable = true;
                        break;
                    }
                    paramMap.put(param.getName(), values);
                }
                if (!unusable) questionCache.put(qName, paramMap);
            }
        }
    }

    private void composeFromTemplate() throws WdkModelException {
        logger.info("Loading test cases from template files");
        File templateFile = new File(gusHome, "/config/" + modelName
                + "/stress.template");

        try {
            BufferedReader reader = new BufferedReader(new FileReader(
                    templateFile));

            String line = null;
            String questionName = null;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                String lowerCase = line.toLowerCase();
                if (lowerCase.startsWith("question")) {
                    questionName = null; // clear the old question
                    int pos = line.indexOf(":");
                    if (pos > 0) {
                        questionName = line.substring(pos + 1).trim();
                    }
                } else if (lowerCase.startsWith("param")) {
                    if (questionName != null) {
                        Map<String, Set<String>> paramMap = questionCache.get(questionName);
                        int pos = line.indexOf(":");
                        if (paramMap != null && pos > 0) {
                            line = line.substring(pos + 1).trim();
                            pos = line.indexOf("=");
                            if (pos > 0) {
                                String paramName = line.substring(0, pos).trim();
                                Set<String> paramValues = paramMap.get(paramName);
                                if (paramValues != null) {
                                    String valueString = line.substring(pos + 1).trim();
                                    String[] values = valueString.split(",");
                                    for (String value : values) {
                                        paramValues.add(value.trim());
                                    }
                                }
                            }
                        }
                    }
                } else if (lowerCase.startsWith("record")) {
                    int pos = line.indexOf(":");
                    if (pos > 0) {
                        line = line.substring(pos + 1).trim();
                        pos = line.indexOf("=");
                        if (pos > 0) {
                            String recordName = line.substring(0, pos).trim();

                            String idString = line.substring(pos + 1).trim();
                            String[] ids = idString.split(",");
                            for (String id : ids) {
                                id = id.trim();
                                if (id.length() == 0) continue;

                                StringBuffer sb = new StringBuffer(
                                        recordUrlPattern);
                                sb.append("?name=" + recordName);
                                sb.append("&primary_key="
                                        + URLEncoder.encode(id, "UTF-8"));
                                UrlItem recordUrl = new UrlItem(sb.toString(),
                                        TYPE_RECORD_URL);
                                urlPool.add(recordUrl);
                            }
                        }
                    }
                } // other line types are ignored
            }

            reader.close();
        } catch (IOException ex) {
            throw new WdkModelException(ex);
        }
    }

    public void runTest(int numThreads) throws WdkModelException {
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
                Thread.sleep(5 * 1000);
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
        File stopFile = new File(gusHome, "/config/" + modelName
                + "/stress.stop");
        return (stopFile.exists());
    }

    private StressTestTask createTask() throws WdkModelException {
        // choose from urlPool or question cache
        UrlItem urlItem;
        if (rand.nextInt(10) >= 3) { // get from question cache
            StringBuffer url = new StringBuffer(questionUrlPattern);

            // choose question
            String[] questions = new String[questionCache.size()];
            questionCache.keySet().toArray(questions);
            String questionName = questions[rand.nextInt(questions.length)];
            url.append("?questionFullName=" + questionName);

            // choose parameters
            Map<String, Set<String>> params = questionCache.get(questionName);
            for (String param : params.keySet()) {
                // choose value
                Set<String> valueSet = params.get(param);
                String[] values = new String[valueSet.size()];
                valueSet.toArray(values);
                String value = values[rand.nextInt(values.length)];
                try {
                    url.append("&"
                            + URLEncoder.encode("value(" + param + ")",
                                    "utf-8"));
                    url.append("=" + URLEncoder.encode(value, "utf-8"));
                } catch (UnsupportedEncodingException ex) {
                    throw new WdkModelException(ex);
                }
            }

            // create UrlItem. and pick parameters
            urlItem = new UrlItem(url.toString(), TYPE_QUESTION_URL);
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
        String[] descs = { "The project id. For example, ToyDB, PlasmoDB",
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
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws JSONException
     * @throws SAXException
     * @throws TransformerException
     * @throws TransformerFactoryConfigurationError
     * @throws ParserConfigurationException
     * @throws NoSuchAlgorithmException
     */
    public static void main(String[] args)
            throws InvalidPropertiesFormatException, IOException,
            WdkModelException, URISyntaxException, WdkUserException,
            SQLException, NoSuchAlgorithmException,
            ParserConfigurationException, TransformerFactoryConfigurationError,
            TransformerException, SAXException, JSONException,
            InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        String cmdName = System.getProperty("cmdName");

        // process args
        Options options = declareOptions();
        CommandLine cmdLine = CommandHelper.parseOptions(cmdName, options, args);

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
