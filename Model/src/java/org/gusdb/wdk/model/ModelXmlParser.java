package org.gusdb.wdk.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.digester.Digester;
import org.apache.log4j.Logger;
import org.gusdb.wdk.model.attribute.plugin.AttributePluginReference;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.ProcessQuery;
import org.gusdb.wdk.model.query.QuerySet;
import org.gusdb.wdk.model.query.SqlQuery;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.DatasetParam;
import org.gusdb.wdk.model.query.param.EnumItem;
import org.gusdb.wdk.model.query.param.EnumItemList;
import org.gusdb.wdk.model.query.param.EnumParam;
import org.gusdb.wdk.model.query.param.FlatVocabParam;
import org.gusdb.wdk.model.query.param.ParamConfiguration;
import org.gusdb.wdk.model.query.param.ParamReference;
import org.gusdb.wdk.model.query.param.ParamSet;
import org.gusdb.wdk.model.query.param.ParamSuggestion;
import org.gusdb.wdk.model.query.param.ParamValuesSet;
import org.gusdb.wdk.model.query.param.RecordClassReference;
import org.gusdb.wdk.model.query.param.StringParam;
import org.gusdb.wdk.model.query.param.TimestampParam;
import org.gusdb.wdk.model.view.RecordView;
import org.gusdb.wdk.model.view.SummaryView;
import org.gusdb.wdk.model.xml.XmlAttributeField;
import org.gusdb.wdk.model.xml.XmlQuestion;
import org.gusdb.wdk.model.xml.XmlQuestionSet;
import org.gusdb.wdk.model.xml.XmlRecordClass;
import org.gusdb.wdk.model.xml.XmlRecordClassSet;
import org.gusdb.wdk.model.xml.XmlTableField;
import org.json.JSONException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author jerric
 *
 */
/**
 * @author jerric
 * 
 */
public class ModelXmlParser extends XmlParser {

    private static final Logger logger = Logger.getLogger(ModelXmlParser.class);

    private static final Pattern PROPERTY_PATTERN = Pattern.compile(
            "\\@([\\w\\.\\-]+)\\@", Pattern.MULTILINE);
    private static final Pattern CONSTANT_PATTERN = Pattern.compile(
            "\\%\\%([\\w\\.\\-]+)\\%\\%", Pattern.MULTILINE);

    private URL xmlSchemaURL;
    private String xmlDataDir;

    public ModelXmlParser(String gusHome) throws SAXException, IOException {
        super(gusHome, "lib/rng/wdkModel.rng");

        // get model schema file and xml schema file
        xmlSchemaURL = makeURL(gusHome, "lib/rng/xmlAnswer.rng");
        xmlDataDir = gusHome + "/lib/xml/";
    }

    public WdkModel parseModel(String projectId)
            throws ParserConfigurationException,
            TransformerFactoryConfigurationError, TransformerException,
            IOException, SAXException, WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException, InstantiationException, IllegalAccessException,
            ClassNotFoundException, URISyntaxException {
        logger.debug("Loading configuration...");

        // get model config
        ModelConfig config = getModelConfig(projectId);
        String modelName = config.getModelName();

        // construct urls to model file, prop file, and config file
        URL modelURL = makeURL(gusHome, "lib/wdk/" + modelName + ".xml");
        URL modelPropURL = makeURL(gusHome, "config/" + projectId
                + "/model.prop");

        // load property map
        Map<String, String> properties = loadProperties(projectId,
                modelPropURL, config);

        // load master model
        logger.debug("Resolving WDK model...");
        Set<String> replacedMacros = new LinkedHashSet<String>();
        Document masterDoc = buildMasterDocument(projectId, modelURL,
                properties, replacedMacros);

        // write document into an input source
        logger.debug("Parsing WDK model...");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Source source = new DOMSource(masterDoc);
        Result result = new StreamResult(output);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(source, result);
        InputStream input = new ByteArrayInputStream(output.toByteArray());
        WdkModel model = (WdkModel) digester.parse(input);

        model.setXmlSchema(xmlSchemaURL); // set schema for xml data
        model.setXmlDataDir(new File(xmlDataDir)); // consider refactoring
        model.configure(config);
        model.setResources();
        model.setProperties(properties, replacedMacros);

        return model;
    }

    private ModelConfig getModelConfig(String projectId) throws SAXException,
            IOException, WdkModelException {
        ModelConfigParser parser = new ModelConfigParser(gusHome);
        return parser.parseConfig(projectId);
    }

    private Document buildMasterDocument(String projectId, URL wdkModelURL,
            Map<String, String> properties, Set<String> replacedMacros)
            throws SAXException, IOException, ParserConfigurationException,
            WdkModelException, TransformerFactoryConfigurationError,
            TransformerException, URISyntaxException {
        // load constants from master file
        Map<String, String> constants = loadConstants(projectId, wdkModelURL);

        // get the xml document of the model
        Document masterDoc = loadDocument(wdkModelURL, properties,
                replacedMacros, constants);
        Node root = masterDoc.getElementsByTagName("wdkModel").item(0);

        // get all imports, and replace each of them with the sub-model
        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (!(child instanceof Element)) continue;
            Element importNode = (Element) child;
            if (!importNode.getTagName().equals("import")) continue;

            // get url to the first import
            String href = importNode.getAttribute("file");
            URL importURL = makeURL(gusHome, "lib/wdk/" + href);

            // load constants from import doc, and merge it with master ones
            Map<String, String> subConsts = loadConstants(projectId, importURL);
            for (String key : constants.keySet()) {
                if (!subConsts.containsKey(key)) // keep all sub-constants
                    subConsts.put(key, constants.get(key));
            }

            Document importDoc = loadDocument(importURL, properties,
                    replacedMacros, subConsts);

            // get the children nodes from imported sub-model, and add them
            // into master document
            Node subRoot = importDoc.getElementsByTagName("wdkModel").item(0);
            NodeList childrenNodes = subRoot.getChildNodes();
            for (int j = 0; j < childrenNodes.getLength(); j++) {
                Node childNode = childrenNodes.item(j);
                if (childNode instanceof Element) {
                    Node imported = masterDoc.importNode(childNode, true);
                    root.appendChild(imported);
                }
            }
        }

        return masterDoc;
    }

    private Map<String, String> loadConstants(String projectId, URL modelXmlURL)
            throws IOException, SAXException,
            ParserConfigurationException, URISyntaxException {
        Map<String, String> constants = new LinkedHashMap<String, String>();
        // load xml document without validation
        File file = new File(modelXmlURL.toURI());
        String content = new String(Utilities.readFile(file));
        Document document = loadDocument(content);
        Node root = document.getElementsByTagName("wdkModel").item(0);
        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element) {
                Element element = (Element) child;
                if (element.getTagName().equals("constant")) {
                    String name = element.getAttribute("name");
                    String includes = element.getAttribute("includeProjects");
                    String excludes = element.getAttribute("excludeProjects");
                    String value = element.getTextContent();
                    if (includes.length() > 0) {
                        // if includes is set, ignore excludes
                        String[] array = includes.trim().split("\\s*,\\s*");
                        if (arrayContains(array, projectId))
                            constants.put(name, value);
                    } else if (excludes.length() > 0) {
                        String[] array = excludes.trim().split("\\s*,\\s*");
                        if (!arrayContains(array, projectId))
                            constants.put(name, value);
                    } else { // no in/excludes, include by default
                        constants.put(name, value);
                    }
                }
            }
        }
        return constants;
    }

    private boolean arrayContains(String[] array, String key) {
        for (String value : array) {
            if (value.equals(key)) return true;
        }
        return false;
    }

    /**
     * Valid the xml first, and then subsitute properties and constants, and at
     * last return the parsed XML Document.
     * 
     * @param modelXmlURL
     * @param properties
     * @param replacedMacros
     * @param constants
     * @return
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws WdkModelException
     * @throws TransformerFactoryConfigurationError
     * @throws TransformerException
     * @throws URISyntaxException
     */
    private Document loadDocument(URL modelXmlURL,
            Map<String, String> properties, Set<String> replacedMacros,
            Map<String, String> constants) throws SAXException, IOException,
            ParserConfigurationException, WdkModelException,
            TransformerFactoryConfigurationError, TransformerException,
            URISyntaxException {
        // validate the sub-model
        validate(modelXmlURL);

        // load file into string
        File file = new File(modelXmlURL.toURI());
        String content = new String(Utilities.readFile(file));

        // substitute the constants & properties. Constants first, since
        // properties are more specific.
        content = substituteConstants(content, constants);
        content = substituteProps(content, properties, replacedMacros);

        return loadDocument(content);
    }

    private Map<String, String> loadProperties(String projectId,
            URL modelPropURL, ModelConfig config) throws IOException {
        Map<String, String> propMap = new LinkedHashMap<String, String>();
        Properties properties = new Properties();
        properties.load(modelPropURL.openStream());
        Iterator<Object> it = properties.keySet().iterator();
        while (it.hasNext()) {
            String propName = (String) it.next();
            String value = properties.getProperty(propName);
            propMap.put(propName, value);
        }

        // add several config into the prop map automatically
        if (!properties.containsKey("PROJECT_ID")) {
            properties.put("PROJECT_ID", projectId);
        }
        if (!properties.containsKey("USER_DBLINK")) {
            String userDbLink = config.getAppDB().getUserDbLink();
            properties.put("USER_DBLINK", userDbLink);
        }
        if (!properties.containsKey("USER_SCHEMA")) {
            properties.put("USER_SCHEMA", config.getUserDB().getUserSchema());
        }
        if (!properties.containsKey("WDK_ENGINE_SCHEMA")) {
            String engineSchema = config.getUserDB().getWdkEngineSchema();
            properties.put("WDK_ENGINE_SCHEMA", engineSchema);
        }

        return propMap;
    }

    private String substituteConstants(String content,
            Map<String, String> constants)
            throws TransformerFactoryConfigurationError, TransformerException,
            WdkModelException, URISyntaxException {
        Matcher matcher = CONSTANT_PATTERN.matcher(content);

        // search and substitute the property macros
        StringBuilder buffer = new StringBuilder();
        int prevPos = 0;
        while (matcher.find()) {
            String propName = matcher.group(1);

            // check if the property macro is defined
            if (!constants.containsKey(propName)) continue;

            String propValue = constants.get(propName);
            buffer.append(content.subSequence(prevPos, matcher.start()));
            buffer.append(propValue);
            prevPos = matcher.end();
        }
        if (prevPos < content.length())
            buffer.append(content.substring(prevPos));

        // construct input stream
        return buffer.toString();
    }

    private String substituteProps(String content,
            Map<String, String> properties, Set<String> replacedMacros)
            throws TransformerFactoryConfigurationError, TransformerException,
            WdkModelException, URISyntaxException {
        Matcher matcher = PROPERTY_PATTERN.matcher(content);

        // search and substitute the property macros
        StringBuilder buffer = new StringBuilder();
        int prevPos = 0;
        while (matcher.find()) {
            String propName = matcher.group(1);

            // check if the property macro is defined
            if (!properties.containsKey(propName)) continue;

            String propValue = properties.get(propName);
            buffer.append(content.subSequence(prevPos, matcher.start()));
            buffer.append(propValue);
            prevPos = matcher.end();

            replacedMacros.add(propName);
        }
        if (prevPos < content.length())
            buffer.append(content.substring(prevPos));

        // construct input stream
        return buffer.toString();
    }

    protected Digester configureDigester() {
        Digester digester = new Digester();
        digester.setValidating(false);

        configureModel(digester);

        // configure all sub nodes of recordClassSet
        configureRecordClassSet(digester);

        // configure all sub nodes of querySet
        configureQuerySet(digester);

        // configure all sub nodes of paramSet
        configureParamSet(digester);

        // configure all sub nodes of questionSet
        configureQuestionSet(digester);

        // configure all sub nodes of xmlQuestionSet
        configureXmlQuestionSet(digester);

        // configure all sub nodes of xmlRecordSet
        configureXmlRecordClassSet(digester);

        // configure all sub nodes of xmlRecordSet
        configureGroupSet(digester);

        // configure the attributes
        configureAttributeFields(digester);

        configureCommonNodes(digester);

        return digester;
    }

    private void configureModel(Digester digester) {
        // Root -- WDK Model
        digester.addObjectCreate("wdkModel", WdkModel.class);
        digester.addSetProperties("wdkModel");

        configureNode(digester, "wdkModel/modelName", WdkModelName.class,
                "addWdkModelName");

        configureNode(digester, "wdkModel/introduction", WdkModelText.class,
                "addIntroduction");
        digester.addCallMethod("wdkModel/introduction", "setText", 0);

        // default property list
        configureNode(digester, "wdkModel/defaultPropertyList",
                PropertyList.class, "addDefaultPropertyList");

        // categories
        configureNode(digester, "wdkModel/searchCategory",
                SearchCategory.class, "addCategory");

        configureNode(digester, "wdkModel/searchCategory/questionRef",
                CategoryQuestionRef.class, "addQuestionRef");
        digester.addCallMethod("wdkModel/searchCategory/questionRef",
                "setText", 0);

        // configure property macros
        configureNode(digester, "wdkModel/declaredMacro",
                MacroDeclaration.class, "addMacroDeclaration");
    }

    private void configureRecordClassSet(Digester digester) {
        // record class set
        configureNode(digester, "wdkModel/recordClassSet",
                RecordClassSet.class, "addRecordClassSet");

        // record class
        configureNode(digester, "wdkModel/recordClassSet/recordClass",
                RecordClass.class, "addRecordClass");

        // attribute categories
        configureNode(digester,
                "wdkModel/recordClassSet/recordClass/attributeCategories",
                AttributeCategoryTree.class, "setAttributeCategoryTree");
        configureNode(digester, "*/attributeCategory", AttributeCategory.class,
                "addAttributeCategory");

        // favorite references
        configureNode(digester, "wdkModel/recordClassSet/recordClass/favorite",
                FavoriteReference.class, "addFavorite");

        configureNode(digester,
                "wdkModel/recordClassSet/recordClass/attributesList",
                AttributeList.class, "addAttributeList");

        // defaultTestParamValues
        configureParamValuesSet(digester,
                "wdkModel/recordClassSet/recordClass/testParamValues",
                "addParamValuesSet");

        // reporter
        configureNode(digester, "wdkModel/recordClassSet/recordClass/reporter",
                ReporterRef.class, "addReporterRef");
        configureNode(digester,
                "wdkModel/recordClassSet/recordClass/reporter/property",
                ReporterProperty.class, "addProperty");
        digester.addCallMethod(
                "wdkModel/recordClassSet/recordClass/reporter/property",
                "setValue", 0);

        // filter layouts
        configureNode(digester,
                "wdkModel/recordClassSet/recordClass/answerFilterLayout",
                AnswerFilterLayout.class, "addFilterLayout");
        configureNode(
                digester,
                "wdkModel/recordClassSet/recordClass/answerFilterLayout/description",
                WdkModelText.class, "addDescription");
        digester.addCallMethod(
                "wdkModel/recordClassSet/recordClass/answerFilterLayout/description",
                "setText", 0);
        configureNode(
                digester,
                "wdkModel/recordClassSet/recordClass/answerFilterLayout/instanceRef",
                AnswerFilterInstanceReference.class, "addReference");
        configureNode(
                digester,
                "wdkModel/recordClassSet/recordClass/answerFilterLayout/layout",
                WdkModelText.class, "addLayout");
        digester.addCallMethod(
                "wdkModel/recordClassSet/recordClass/answerFilterLayout/layout",
                "setText", 0);

        // filter instances
        configureNode(digester,
                "wdkModel/recordClassSet/recordClass/answerFilter",
                AnswerFilter.class, "addFilter");
        configureNode(digester,
                "wdkModel/recordClassSet/recordClass/answerFilter/instance",
                AnswerFilterInstance.class, "addInstance");

        configureNode(
                digester,
                "wdkModel/recordClassSet/recordClass/answerFilter/instance/displayName",
                WdkModelText.class, "addDisplayName");
        digester.addCallMethod(
                "wdkModel/recordClassSet/recordClass/answerFilter/instance/displayName",
                "setText", 0);

        configureNode(
                digester,
                "wdkModel/recordClassSet/recordClass/answerFilter/instance/description",
                WdkModelText.class, "addDescription");
        digester.addCallMethod(
                "wdkModel/recordClassSet/recordClass/answerFilter/instance/description",
                "setText", 0);

        configureNode(
                digester,
                "wdkModel/recordClassSet/recordClass/answerFilter/instance/paramValue",
                WdkModelText.class, "addParamValue");
        digester.addCallMethod(
                "wdkModel/recordClassSet/recordClass/answerFilter/instance/paramValue",
                "setText", 0);

        // attribute query ref
        configureNode(digester,
                "wdkModel/recordClassSet/recordClass/attributeQueryRef",
                AttributeQueryReference.class, "addAttributesQueryRef");

        // tables
        configureNode(digester, "wdkModel/recordClassSet/recordClass/table",
                TableField.class, "addTableField");

        configureNode(digester,
                "wdkModel/recordClassSet/recordClass/table/description",
                WdkModelText.class, "addDescription");
        digester.addCallMethod(
                "wdkModel/recordClassSet/recordClass/table/description",
                "setText", 0);

        // tableField's property list
        configureNode(digester,
                "wdkModel/recordClassSet/recordClass/table/propertyList",
                PropertyList.class, "addPropertyList");

        configureNode(digester,
                "wdkModel/recordClassSet/recordClass/table/columnAttribute",
                ColumnAttributeField.class, "addAttributeField");

        // nested record and record list
        configureNode(digester,
                "wdkModel/recordClassSet/recordClass/nestedRecord",
                NestedRecord.class, "addNestedRecordQuestionRef");

        configureNode(digester,
                "wdkModel/recordClassSet/recordClass/nestedRecordList",
                NestedRecordList.class, "addNestedRecordListQuestionRef");

        configureNode(digester,
                "wdkModel/recordClassSet/recordClass/summaryView",
                SummaryView.class, "addSummaryView");

        configureNode(digester,
                "wdkModel/recordClassSet/recordClass/recordView",
                RecordView.class, "addRecordView");
    }

    private void configureQuerySet(Digester digester) {
        // QuerySet
        configureNode(digester, "wdkModel/querySet", QuerySet.class,
                "addQuerySet");

        // defaultTestParamValues
        configureParamValuesSet(digester,
                "wdkModel/querySet/defaultTestParamValues",
                "addDefaultParamValuesSet");

        // cardinalitySql
        configureNode(digester, "wdkModel/querySet/testRowCountSql",
                WdkModelText.class, "addTestRowCountSql");
        digester.addCallMethod("wdkModel/querySet/testRowCountSql", "setText",
                0);

        // sqlQuery
        configureNode(digester, "wdkModel/querySet/sqlQuery", SqlQuery.class,
                "addQuery");

        // testParamValues
        configureParamValuesSet(digester,
                "wdkModel/querySet/sqlQuery/testParamValues",
                "addParamValuesSet");

        configureNode(digester, "wdkModel/querySet/sqlQuery/sql",
                WdkModelText.class, "addSql");
        digester.addCallMethod("wdkModel/querySet/sqlQuery/sql", "setText", 0);

        configureNode(digester, "wdkModel/querySet/sqlQuery/paramRef",
                ParamReference.class, "addParamRef");

        configureNode(digester, "wdkModel/querySet/sqlQuery/column",
                Column.class, "addColumn");

        configureNode(digester, "wdkModel/querySet/sqlQuery/sqlParamValue",
                WdkModelText.class, "addSqlParamValue");
        digester.addCallMethod("wdkModel/querySet/sqlQuery/sqlParamValue",
                "setText", 0);

        // processQuery
        configureNode(digester, "wdkModel/querySet/processQuery",
                ProcessQuery.class, "addQuery");

        // testParamValues
        configureParamValuesSet(digester,
                "wdkModel/querySet/processQuery/testParamValues",
                "addParamValuesSet");

        configureNode(digester, "wdkModel/querySet/processQuery/paramRef",
                ParamReference.class, "addParamRef");

        configureNode(digester, "wdkModel/querySet/processQuery/wsColumn",
                Column.class, "addColumn");
    }

    private void configureParamSet(Digester digester) {
        // ParamSet
        configureNode(digester, "wdkModel/paramSet", ParamSet.class,
                "addParamSet");

        // string param
        String path = "wdkModel/paramSet/stringParam";
        configureNode(digester, path, StringParam.class, "addParam");
        configureParamContent(digester, path);
        configureNode(digester, path + "/regex", WdkModelText.class, "addRegex");
        digester.addCallMethod(path + "/regex", "setText", 0);

        // flatVocabParam
        path = "wdkModel/paramSet/flatVocabParam";
        configureNode(digester, path, FlatVocabParam.class, "addParam");
        configureParamContent(digester, path);

        // answer param
        configureNode(digester, "wdkModel/paramSet/answerParam",
                AnswerParam.class, "addParam");
        configureParamContent(digester, "wdkModel/paramSet/answerParam");
        configureNode(digester, "wdkModel/paramSet/answerParam/recordClass",
                RecordClassReference.class, "addRecordClassRef");

        // dataset param
        path = "wdkModel/paramSet/datasetParam";
        configureNode(digester, path, DatasetParam.class, "addParam");
        configureParamContent(digester, path);

        // enum param
        path = "wdkModel/paramSet/enumParam";
        configureNode(digester, path, EnumParam.class, "addParam");
        configureParamContent(digester, path);

        path = path + "/enumList";
        configureNode(digester, path, EnumItemList.class, "addEnumItemList");

        configureNode(digester, path + "/enumValue", EnumItem.class,
                "addEnumItem");
        digester.addBeanPropertySetter(path + "/enumValue/display");
        digester.addBeanPropertySetter(path + "/enumValue/term");
        digester.addBeanPropertySetter(path + "/enumValue/internal");
        digester.addBeanPropertySetter(path + "/enumValue/parentTerm");

        configureNode(digester, path + "/enumValue/dependedValue",
                WdkModelText.class, "addDependedValue");
        digester.addCallMethod(path + "/enumValue/dependedValue", "setText", 0);

        // timestamp param
        path = "wdkModel/paramSet/timestampParam";
        configureNode(digester, path, TimestampParam.class, "addParam");
        configureParamContent(digester, path);
    }

    private void configureParamContent(Digester digester, String paramPath) {
        configureNode(digester, paramPath + "/suggest", ParamSuggestion.class,
                "addSuggest");
        configureNode(digester, paramPath + "/noTranslation",
                ParamConfiguration.class, "addNoTranslation");
    }

    private void configureQuestionSet(Digester digester) {
        // QuestionSet
        configureNode(digester, "wdkModel/questionSet", QuestionSet.class,
                "addQuestionSet");

        configureNode(digester, "wdkModel/questionSet/description",
                WdkModelText.class, "addDescription");
        digester.addCallMethod("wdkModel/questionSet/description", "setText", 0);

        // question
        configureNode(digester, "wdkModel/questionSet/question",
                Question.class, "addQuestion");

        configureNode(digester, "wdkModel/questionSet/question/description",
                WdkModelText.class, "addDescription");
        digester.addCallMethod("wdkModel/questionSet/question/description",
                "setText", 0);

        configureNode(digester, "wdkModel/questionSet/question/summary",
                WdkModelText.class, "addSummary");
        digester.addCallMethod("wdkModel/questionSet/question/summary",
                "setText", 0);

        // question's property list
        configureNode(digester, "wdkModel/questionSet/question/propertyList",
                PropertyList.class, "addPropertyList");

        configureNode(digester, "wdkModel/questionSet/question/attributesList",
                AttributeList.class, "addAttributeList");

        // dynamic attribute set
        configureNode(digester,
                "wdkModel/questionSet/question/dynamicAttributes",
                DynamicAttributeSet.class, "addDynamicAttributeSet");

        configureNode(
                digester,
                "wdkModel/questionSet/question/dynamicAttributes/columnAttribute",
                ColumnAttributeField.class, "addAttributeField");

        configureNode(digester, "wdkModel/questionSet/question/paramRef",
                ParamReference.class, "addParamRef");

        configureNode(digester, "wdkModel/questionSet/question/sqlParamValue",
                WdkModelText.class, "addSqlParamValue");
        digester.addCallMethod("wdkModel/questionSet/question/sqlParamValue",
                "setText", 0);

        configureNode(digester, "wdkModel/questionSet/question/summaryView",
                SummaryView.class, "addSummaryView");
    }

    private void configureXmlQuestionSet(Digester digester) {
        // load XmlQuestionSet
        configureNode(digester, "wdkModel/xmlQuestionSet",
                XmlQuestionSet.class, "addXmlQuestionSet");

        configureNode(digester, "wdkModel/xmlQuestionSet/description",
                WdkModelText.class, "addDescription");
        digester.addCallMethod("wdkModel/xmlQuestionSet/description",
                "setText", 0);

        // load XmlQuestion
        configureNode(digester, "wdkModel/xmlQuestionSet/xmlQuestion",
                XmlQuestion.class, "addQuestion");

        configureNode(digester,
                "wdkModel/xmlQuestionSet/xmlQuestion/description",
                WdkModelText.class, "addDescription");
        digester.addCallMethod(
                "wdkModel/xmlQuestionSet/xmlQuestion/description", "setText", 0);
    }

    private void configureParamValuesSet(Digester digester, String path,
            String addMethodName) {
        digester.addObjectCreate(path, ParamValuesSet.class);
        digester.addSetProperties(path);
        digester.addCallMethod(path + "/paramValue", "put", 2);
        digester.addCallParam(path + "/paramValue", 0, "name");
        digester.addCallParam(path + "/paramValue", 1);
        digester.addSetNext(path, addMethodName);
    }

    private void configureXmlRecordClassSet(Digester digester) {
        // load XmlRecordClassSet
        configureNode(digester, "wdkModel/xmlRecordClassSet",
                XmlRecordClassSet.class, "addXmlRecordClassSet");

        // load XmlRecordClass
        configureNode(digester, "wdkModel/xmlRecordClassSet/xmlRecordClass",
                XmlRecordClass.class, "addRecordClass");

        // load XmlAttributeField
        configureNode(digester,
                "wdkModel/xmlRecordClassSet/xmlRecordClass/xmlAttribute",
                XmlAttributeField.class, "addAttributeField");

        // load XmlTableField
        configureNode(digester,
                "wdkModel/xmlRecordClassSet/xmlRecordClass/xmlTable",
                XmlTableField.class, "addTableField");

        // load XmlAttributeField within table
        configureNode(
                digester,
                "wdkModel/xmlRecordClassSet/xmlRecordClass/xmlTable/xmlAttribute",
                XmlAttributeField.class, "addAttributeField");
    }

    private void configureGroupSet(Digester digester) {
        // load GroupSet
        configureNode(digester, "wdkModel/groupSet", GroupSet.class,
                "addGroupSet");

        // load group
        configureNode(digester, "wdkModel/groupSet/group", Group.class,
                "addGroup");

        configureNode(digester, "wdkModel/groupSet/group/description",
                WdkModelText.class, "addDescription");
        digester.addCallMethod("wdkModel/groupSet/group/description",
                "setText", 0);
    }

    private void configureAttributeFields(Digester digester) {
        // primary key attribute
        String prefixPK = "wdkModel/recordClassSet/recordClass/primaryKeyAttribute";
        configureNode(digester, prefixPK, PrimaryKeyAttributeField.class,
                "addAttributeField");
        configureNode(digester, prefixPK + "/columnRef", WdkModelText.class,
                "addColumnRef");
        digester.addCallMethod(prefixPK + "/columnRef", "setText", 0);
        configureNode(digester, prefixPK + "/text", WdkModelText.class,
                "addText");
        digester.addCallMethod(prefixPK + "/text", "setText", 0);
        configureAttributePlugins(digester, "primaryKeyAttribute");

        configureNode(digester, "*/columnAttribute",
                ColumnAttributeField.class, "addAttributeField");
        configureAttributePlugins(digester, "columnAttribute");

        // link attribute
        configureNode(digester, "*/linkAttribute", LinkAttributeField.class,
                "addAttributeField");
        configureNode(digester, "*/linkAttribute/url", WdkModelText.class,
                "addUrl");
        digester.addCallMethod("*/linkAttribute/url", "setText", 0);
        configureNode(digester, "*/linkAttribute/displayText",
                WdkModelText.class, "addDisplayText");
        digester.addCallMethod("*/linkAttribute/displayText", "setText", 0);
        configureAttributePlugins(digester, "linkAttribute");

        // text attribute
        configureNode(digester, "*/textAttribute", TextAttributeField.class,
                "addAttributeField");

        configureNode(digester, "*/textAttribute/text", WdkModelText.class,
                "addText");
        digester.addCallMethod("*/textAttribute/text", "setText", 0);

        configureNode(digester, "*/textAttribute/display", WdkModelText.class,
                "addDisplay");
        digester.addCallMethod("*/textAttribute/display", "setText", 0);
        configureAttributePlugins(digester, "textAttribute");
    }

    private void configureAttributePlugins(Digester digester, String attribute) {
        String prefix = "*/" + attribute + "/plugin";
        // configure plugins for
        configureNode(digester, prefix, AttributePluginReference.class,
                "addAttributePluginReference");
        configureNode(digester, prefix + "/property", WdkModelText.class,
                "addProperty");
        digester.addCallMethod(prefix + "/property", "setText", 0);

    }

    private void configureCommonNodes(Digester digester) {
        configureNode(digester, "*/help", WdkModelText.class, "addHelp");
        digester.addCallMethod("*/help", "setText", 0);

        configureNode(digester, "*/value", WdkModelText.class, "addValue");
        digester.addCallMethod("*/value", "setText", 0);
    }

    public static void main(String[] args) throws SAXException, IOException,
            ParserConfigurationException, TransformerFactoryConfigurationError,
            TransformerException, WdkModelException, NoSuchAlgorithmException,
            SQLException, JSONException, WdkUserException,
            InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        String cmdName = System.getProperty("cmdName");

        // process args
        Options options = declareOptions();
        CommandLine cmdLine = parseOptions(cmdName, options, args);
        String projectId = cmdLine.getOptionValue(Utilities.ARGUMENT_PROJECT_ID);
        String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);

        // create a parser, and parse the model file
        WdkModel wdkModel = WdkModel.construct(projectId, gusHome);

        // print out the model content
        System.out.println(wdkModel.toString());
        System.exit(0);
    }

    private static void addOption(Options options, String argName, String desc) {

        Option option = new Option(argName, true, desc);
        option.setRequired(true);
        option.setArgName(argName);

        options.addOption(option);
    }

    private static Options declareOptions() {
        Options options = new Options();

        // config file
        addOption(options, "model", "the name of the model.  This is used to "
                + "find the Model XML file ($GUS_HOME/lib/wdk/model_name.xml) "
                + "the Model property file ($GUS_HOME/config/model_name.prop) "
                + "and the Model config file "
                + "($GUS_HOME/config/model_name-config.xml)");

        return options;
    }

    private static CommandLine parseOptions(String cmdName, Options options,
            String[] args) {

        CommandLineParser parser = new BasicParser();
        CommandLine cmdLine = null;
        try {
            // parse the command line arguments
            cmdLine = parser.parse(options, args);
        } catch (ParseException exp) {
            // oops, something went wrong
            System.err.println("");
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
            System.err.println("");
            usage(cmdName, options);
        }

        return cmdLine;
    }

    private static void usage(String cmdName, Options options) {

        String newline = System.getProperty("line.separator");
        String cmdlineSyntax = cmdName + " -model model_name";

        String header = newline + "Parse and print out a WDK Model xml file."
                + newline + newline + "Options:";

        String footer = "";

        // PrintWriter stderr = new PrintWriter(System.err);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(75, cmdlineSyntax, header, options, footer);
        System.exit(1);
    }
}
