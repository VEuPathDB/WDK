package org.gusdb.wdk.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
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
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.digester3.Digester;
import org.apache.log4j.Logger;
import org.gusdb.fgputil.runtime.GusHome;
import org.gusdb.fgputil.web.CookieBuilder;
import org.gusdb.fgputil.xml.XmlParser;
import org.gusdb.fgputil.xml.XmlValidator;
import org.gusdb.wdk.model.UIConfig.ExtraLogoutCookies;
import org.gusdb.wdk.model.analysis.StepAnalysisPlugins;
import org.gusdb.wdk.model.analysis.StepAnalysisXml;
import org.gusdb.wdk.model.answer.SummaryView;
import org.gusdb.wdk.model.columntool.ColumnTool;
import org.gusdb.wdk.model.columntool.ColumnToolBundle;
import org.gusdb.wdk.model.columntool.ColumnToolBundleMap;
import org.gusdb.wdk.model.columntool.DefaultColumnToolBundleRef;
import org.gusdb.wdk.model.columntool.ColumnToolElementRef;
import org.gusdb.wdk.model.columntool.ColumnToolElementRefPair;
import org.gusdb.wdk.model.config.ModelConfig;
import org.gusdb.wdk.model.config.ModelConfigBuilder;
import org.gusdb.wdk.model.config.ModelConfigParser;
import org.gusdb.wdk.model.dataset.DatasetParserReference;
import org.gusdb.wdk.model.filter.FilterReference;
import org.gusdb.wdk.model.filter.FilterSet;
import org.gusdb.wdk.model.filter.StepFilterDefinition;
import org.gusdb.wdk.model.ontology.OntologyFactoryImpl;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.PostCacheUpdateSql;
import org.gusdb.wdk.model.query.ProcessQuery;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QuerySet;
import org.gusdb.wdk.model.query.SqlQuery;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.DatasetParam;
import org.gusdb.wdk.model.query.param.DatasetParamSuggestion;
import org.gusdb.wdk.model.query.param.DateParam;
import org.gusdb.wdk.model.query.param.DateRangeParam;
import org.gusdb.wdk.model.query.param.EnumItem;
import org.gusdb.wdk.model.query.param.EnumItemList;
import org.gusdb.wdk.model.query.param.EnumParam;
import org.gusdb.wdk.model.query.param.EnumParamSuggestion;
import org.gusdb.wdk.model.query.param.FilterParamNew;
import org.gusdb.wdk.model.query.param.FlatVocabParam;
import org.gusdb.wdk.model.query.param.NumberParam;
import org.gusdb.wdk.model.query.param.NumberRangeParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.ParamConfiguration;
import org.gusdb.wdk.model.query.param.ParamHandlerReference;
import org.gusdb.wdk.model.query.param.ParamReference;
import org.gusdb.wdk.model.query.param.ParamSet;
import org.gusdb.wdk.model.query.param.ParamSuggestion;
import org.gusdb.wdk.model.query.param.ParamValue;
import org.gusdb.wdk.model.query.param.ParamValuesSet;
import org.gusdb.wdk.model.query.param.RecordClassReference;
import org.gusdb.wdk.model.query.param.StringParam;
import org.gusdb.wdk.model.query.param.TimestampParam;
import org.gusdb.wdk.model.question.AttributeList;
import org.gusdb.wdk.model.question.CategoryList;
import org.gusdb.wdk.model.question.CategoryQuestionRef;
import org.gusdb.wdk.model.question.DynamicAttributeSet;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.question.QuestionSet;
import org.gusdb.wdk.model.question.QuestionSuggestion;
import org.gusdb.wdk.model.question.SearchCategory;
import org.gusdb.wdk.model.record.AttributeQueryReference;
import org.gusdb.wdk.model.record.BooleanReference;
import org.gusdb.wdk.model.record.CountReference;
import org.gusdb.wdk.model.record.PrimaryKeyDefinition;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordClassSet;
import org.gusdb.wdk.model.record.RecordView;
import org.gusdb.wdk.model.record.ResultPropertyQueryReference;
import org.gusdb.wdk.model.record.ResultSizeQueryReference;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.attribute.AttributeCategory;
import org.gusdb.wdk.model.record.attribute.AttributeCategoryTree;
import org.gusdb.wdk.model.record.attribute.IdAttributeField;
import org.gusdb.wdk.model.record.attribute.LinkAttributeField;
import org.gusdb.wdk.model.record.attribute.PkColumnAttributeField;
import org.gusdb.wdk.model.record.attribute.QueryColumnAttributeField;
import org.gusdb.wdk.model.record.attribute.TextAttributeField;
import org.gusdb.wdk.model.report.ReporterRef;
import org.gusdb.wdk.model.user.FavoriteReference;
import org.gusdb.wdk.model.xml.XmlAttributeField;
import org.gusdb.wdk.model.xml.XmlQuestion;
import org.gusdb.wdk.model.xml.XmlQuestionSet;
import org.gusdb.wdk.model.xml.XmlRecordClass;
import org.gusdb.wdk.model.xml.XmlRecordClassSet;
import org.gusdb.wdk.model.xml.XmlTableField;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * <p>
 * The XML parser is used to parse the WDK model file and all its sub-model files.
 * </p>
 *
 * <p>
 * First, the parser will start from the top level model file, and get sub-model files from the
 * {@code &lt;import>} tag. currently, we only support {@code &lt;import>} in the top level model file, and
 * sub-model file cannot have {@code &lt;import>} to other sub-models.
 * </p>
 *
 * <p>
 * Then the top level model and each such-model's XML file will be validated against {@code wdkModel.rng}, and
 * if validation fails, an exception will be thrown out with the line number where it failed.
 * </p>
 *
 * <p>
 * Nest, the parser will first get global constants defined in the top level model, and substitute them into
 * the text of the model; then it will read constants from each sub-model, and those constants can override
 * the global constants in the scope of the sub-model; and the constants are substituted into the text of the
 * sub-models.
 * </p>
 *
 * <p>
 * In the next step, the parser will substitute the macros defined in model.prop into the text of the top
 * level model and each sub-model.
 * </p>
 *
 * <p>
 * Next, a DOM will be created for each top level and sub-model, and DOM of sub-model will be injected into
 * top level model. the {@code &lt;wdkModel>} root tag in each sub-model will be ignored, but all its child
 * elements will be inserted into the {@code &lt;wdkModel>} in the top level DOM.
 * </p>
 *
 * <p>
 * Next, the DOM will be streamed into Apache {@link org.apache.commons.digester3.Digester}, and a
 * {@link WdkModel} object will be created.
 * </p>
 *
 * <p>
 * Next, {@link WdkModel#excludeResources} will be called, and the resources that don't belong to the current
 * project will be removed, and the model objects will be cleaned up to remove excluded resources, and some of
 * the unique constraints will be applied here. The include/exclude projects flag are used to declared if a
 * resource belongs, or doesn't belong, to a list of projects.
 * </p>
 *
 * <p>
 * Lastly, {@link WdkModel#resolveReferences} will be called and object references will be resolved, for
 * example, a {@link Question} object will have a reference to {@link RecordClass} object. In certain cases, a
 * clone will be created, such as a {@link Question} will have a copy of the ID {@link Query}, and a
 * {@link Query} will have a local copy of all the {@link Param}s because the properties of a {@link Param}
 * can be overriden in the {@code &lt;paramRef>} inside of {@code &lt;question>, &lt;sqlQuery>, and &lt;processQuery>}.
 * </p>
 *
 * @author jerric
 *
 */
public class ModelXmlParser extends XmlParser {

  public static final String ARG_DEPENDENCY = "dependency";

  private static final Logger logger = Logger.getLogger(ModelXmlParser.class);

  private static final Pattern PROPERTY_PATTERN = Pattern.compile("\\@([\\w\\.\\-]+)\\@", Pattern.MULTILINE);
  private static final Pattern CONSTANT_PATTERN = Pattern.compile("\\%\\%([\\w\\.\\-]+)\\%\\%", Pattern.MULTILINE);

  private final String _gusHome;
  private final URL _xmlSchemaURL;
  private final String _xmlDataDir;
  private final XmlValidator _validator;
  private final Digester _digester;

  public ModelXmlParser(String gusHome) throws WdkModelException {
    try {
      // get model schema file and xml schema file
      _gusHome = gusHome;
      _xmlSchemaURL = makeURL(gusHome + "/lib/rng/xmlAnswer.rng");
      _xmlDataDir = gusHome + "/lib/xml/";
      _validator = new XmlValidator(gusHome + "/lib/rng/wdkModel.rng");
      _digester = configureDigester();
    }
    catch (SAXException | IOException ex) {
      throw new WdkModelException(ex);
    }
  }

  public WdkModel parseModel(String projectId) throws ParserConfigurationException, TransformerException,
      IOException, SAXException, WdkModelException, URISyntaxException {
    logger.debug("Loading configuration...");

    // get model config
    ModelConfig config = getModelConfig(projectId).build();
    String modelName = config.getModelName();

    // construct urls to model file, prop file, and config file
    URL modelURL = makeURL(_gusHome + "/lib/wdk/" + modelName + ".xml");
    URL modelPropURL = makeURL(_gusHome + "/config/" + projectId + "/model.prop");

    // load property map
    Map<String, String> properties = loadProperties(projectId, modelPropURL, config);

    // load master model
    logger.debug("Resolving WDK model...");
    Set<String> replacedMacros = new LinkedHashSet<String>();
    Document masterDoc = buildMasterDocument(projectId, modelURL, properties, replacedMacros);

    // write document into an input source
    logger.debug("Parsing WDK model...");
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    Source source = new DOMSource(masterDoc);
    Result result = new StreamResult(output);
    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    transformer.transform(source, result);
    InputStream input = new ByteArrayInputStream(output.toByteArray());
    WdkModel model = (WdkModel) _digester.parse(input);

    model.setGusHome(_gusHome);
    model.setXmlSchema(_xmlSchemaURL); // set schema for xml data
    model.setXmlDataDir(new File(_xmlDataDir)); // consider refactoring
    model.configure(config);
    model.setResources();
    model.setProperties(properties, replacedMacros);

    return model;
  }

  public ModelConfigBuilder getModelConfig(String projectId) throws SAXException, IOException, WdkModelException {
    ModelConfigParser parser = new ModelConfigParser(_gusHome);
    return parser.parseConfig(projectId);
  }

  /**
   * Before building the master model DOM, the constants will be loaded first, and then substituted into each
   * sub model dom. Then each sub model dom will be combined into the master model. At last, the properties
   * from model.prop will be substituded into the master model.
   *
   * @param projectId
   * @param wdkModelURL
   * @param properties
   * @param replacedMacros
   * @return
   */
  private Document buildMasterDocument(String projectId, URL wdkModelURL, Map<String, String> properties,
      Set<String> replacedMacros) throws SAXException, IOException, ParserConfigurationException,
      WdkModelException, URISyntaxException {
    // load constants from master file
    Map<String, String> constants = loadConstants(projectId, wdkModelURL);

    // get the xml document of the model
    Document masterDoc = loadDocument(wdkModelURL, properties, replacedMacros, constants);
    Node root = masterDoc.getElementsByTagName("wdkModel").item(0);

    // get all imports, and replace each of them with the sub-model
    NodeList children = root.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if (!(child instanceof Element))
        continue;
      Element importNode = (Element) child;
      if (!importNode.getTagName().equals("import"))
        continue;

      // get url to the first import
      String href = importNode.getAttribute("file");
      URL importURL = makeURL(_gusHome + "/lib/wdk/" + href);

      // load constants from import doc, and merge it with master ones
      Map<String, String> subConsts = loadConstants(projectId, importURL);
      for (String key : constants.keySet()) {
        if (!subConsts.containsKey(key)) // keep all sub-constants
          subConsts.put(key, constants.get(key));
      }

      Document importDoc = loadDocument(importURL, properties, replacedMacros, subConsts);

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

  /**
   * Load constants recursively from all the model files.
   *
   * @param projectId
   * @param modelXmlURL
   * @return
   */
  private Map<String, String> loadConstants(String projectId, URL modelXmlURL) throws IOException,
      SAXException, WdkModelException, ParserConfigurationException, URISyntaxException {
    logger.trace("Test parsing " + modelXmlURL.toString());

    if (!_validator.validate(modelXmlURL)) {
      throw new WdkModelException("Validation failed: " + modelXmlURL.toExternalForm());
    }

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
          }
          else if (excludes.length() > 0) {
            String[] array = excludes.trim().split("\\s*,\\s*");
            if (!arrayContains(array, projectId))
              constants.put(name, value);
          }
          else { // no in/excludes, include by default
            constants.put(name, value);
          }
        }
      }
    }
    return constants;
  }

  private boolean arrayContains(String[] array, String key) {
    for (String value : array) {
      if (value.equals(key))
        return true;
    }
    return false;
  }

  /**
   * Valid the xml first, and then subsitute properties and constants, and at last return the parsed XML
   * Document.
   *
   * @param modelXmlURL
   * @param properties
   * @param replacedMacros
   * @param constants
   * @return
   */
  private Document loadDocument(URL modelXmlURL, Map<String, String> properties, Set<String> replacedMacros,
      Map<String, String> constants) throws SAXException, IOException, ParserConfigurationException,
      WdkModelException, URISyntaxException {
    // validate the sub-model
    if (!_validator.validate(modelXmlURL)) {
      throw new WdkModelException("Validation failed: " + modelXmlURL.toExternalForm());
    }

    // load file into string
    File file = new File(modelXmlURL.toURI());
    String content = new String(Utilities.readFile(file));

    // substitute the constants & properties. Constants first, since
    // properties are more specific.
    content = substituteConstants(content, constants);
    content = substituteProps(content, properties, replacedMacros);

    return loadDocument(content);
  }

  private Map<String, String> loadProperties(String projectId, URL modelPropURL, ModelConfig config)
      throws IOException {
    Map<String, String> propMap = new LinkedHashMap<String, String>();
    Properties properties = new Properties();
    properties.load(modelPropURL.openStream());
    Iterator<Object> it = properties.keySet().iterator();
    while (it.hasNext()) {
      String propName = (String) it.next();
      String value = properties.getProperty(propName).trim();
      propMap.put(propName, value);
    }

    // add several config into the prop map automatically
    if (!propMap.containsKey("PROJECT_ID")) {
      propMap.put("PROJECT_ID", projectId);
    }
    if (!propMap.containsKey("USER_DBLINK")) {
      String userDbLink = config.getAppDB().getUserDbLink();
      propMap.put("USER_DBLINK", userDbLink);
    }
    if (!propMap.containsKey("ACCT_DBLINK")) {
      String acctDbLink = config.getAppDB().getAcctDbLink();
      propMap.put("ACCT_DBLINK", acctDbLink);
    }
    if (!propMap.containsKey("USER_SCHEMA")) {
      propMap.put("USER_SCHEMA", config.getUserDB().getUserSchema());
    }

    return propMap;
  }

  private String substituteConstants(String content, Map<String, String> constants) {
    Matcher matcher = CONSTANT_PATTERN.matcher(content);

    // search and substitute the property macros
    StringBuilder buffer = new StringBuilder();
    int prevPos = 0;
    while (matcher.find()) {
      String propName = matcher.group(1);

      // check if the property macro is defined
      if (!constants.containsKey(propName))
        continue;

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

  private String substituteProps(String content, Map<String, String> properties, Set<String> replacedMacros) {
    Matcher matcher = PROPERTY_PATTERN.matcher(content);

    // search and substitute the property macros
    StringBuilder buffer = new StringBuilder();
    int prevPos = 0;
    while (matcher.find()) {
      String propName = matcher.group(1);

      // check if the property macro is defined
      if (!properties.containsKey(propName))
        continue;

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

  private static Digester configureDigester() {
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

    configureUiConfig(digester);

    configureStepAnalysis(digester);

    // configureF all sub nodes of filters
    configureFilterSet(digester);

    return digester;
  }

  private static void configureModel(Digester digester) {
    // Root -- WDK Model
    digester.addObjectCreate("wdkModel", WdkModel.class);
    digester.addSetProperties("wdkModel");

    configureNode(digester, "wdkModel/modelName", WdkModelName.class, "addWdkModelName");

    configureNode(digester, "wdkModel/exampleStratsAuthor", ExampleStratsAuthor.class,
        "setExampleStratsAuthor");

    configureNode(digester, "wdkModel/introduction", WdkModelText.class, "addIntroduction");
    digester.addCallMethod("wdkModel/introduction", "setText", 0);

    // default property list
    configureNode(digester, "wdkModel/defaultPropertyList", PropertyList.class, "addDefaultPropertyList");

    // ontologies
    configureNode(digester, "wdkModel/ontology", OntologyFactoryImpl.class, "addOntology");
    configureNode(digester, "wdkModel/ontology/property", WdkModelText.class, "addProperty");
    digester.addCallMethod("wdkModel/ontology/property", "setText", 0);

    // categories
    configureNode(digester, "wdkModel/searchCategory", SearchCategory.class, "addCategory");

    configureNode(digester, "wdkModel/searchCategory/questionRef", CategoryQuestionRef.class,
        "addQuestionRef");
    digester.addCallMethod("wdkModel/searchCategory/questionRef", "setText", 0);

    configureNode(digester, "wdkModel/searchCategory/description", WdkModelText.class, "addDescription");
    digester.addCallMethod("wdkModel/searchCategory/description", "setText", 0);

    // configure property macros
    configureNode(digester, "wdkModel/declaredMacro", MacroDeclaration.class, "addMacroDeclaration");

    // configure general properties
    configureNode(digester, "*/property", WdkModelText.class, "addProperty");
    digester.addCallMethod("*/property", "setText", 0);

    // Configure attribute tool bundles
    configureToolBundlesNode(digester);
    configureNode(digester, "wdkModel/defaultColumnToolBundle",
      DefaultColumnToolBundleRef.class, "setDefaultColumnToolBundleRef");
  }

  private static void configureToolBundlesNode(final Digester digester) {
    final String root = "wdkModel/columnToolBundles";
    final String bundle = root + "/toolBundle";
    final String tool = bundle + "/tool";

    configureNode(digester, root, ColumnToolBundleMap.class, "setColumnToolLibrary");
    configureNode(digester, bundle, ColumnToolBundle.class, "addToolBundle");
    configureNode(digester, tool, ColumnTool.class, "addTool");

    String[] dataTypes = new String[] { "String", "Date", "Number", "Other" };
    for (String dataType : dataTypes) {
      String typePath = tool + "/" + dataType.toLowerCase();
      configureNode(digester, typePath, ColumnToolElementRefPair.class, "set" + dataType + "Pair");
      configureToolImplementation(digester, typePath + "/reporter", "setReporter");
      configureToolImplementation(digester, typePath + "/filter", "setFilter");
    }
  }

  private static void configureToolImplementation(Digester digester, String path, String setter) {
    configureNode(digester, path, ColumnToolElementRef.class, setter);
    configureNode(digester, path + "/property", WdkModelText.class, "addProperty");
    digester.addCallMethod(path + "/property", "setText", 0);
  }

  private static void configureRecordClassSet(Digester digester) {
    // record class set
    configureNode(digester, "wdkModel/recordClassSet", RecordClassSet.class, "addRecordClassSet");

    // record class
    configureNode(digester, "wdkModel/recordClassSet/recordClass", RecordClass.class, "addRecordClass");

    // attribute categories
    configureNode(digester, "wdkModel/recordClassSet/recordClass/attributeCategories",
        AttributeCategoryTree.class, "setAttributeCategoryTree");
    configureNode(digester, "*/attributeCategory", AttributeCategory.class, "addAttributeCategory");
    configureNode(digester, "*/attributeCategory/description", WdkModelText.class, "setDescription");
    digester.addCallMethod("*/attributeCategory/description", "setText", 0);

    // favorite references
    configureNode(digester, "wdkModel/recordClassSet/recordClass/favorite", FavoriteReference.class,
        "addFavorite");

    configureNode(digester, "wdkModel/recordClassSet/recordClass/attributesList", AttributeList.class,
        "addAttributeList");

    configureNode(digester, "wdkModel/recordClassSet/recordClass/categoriesList", CategoryList.class,
        "addCategoryList");

    // defaultTestParamValues
    configureParamValuesSet(digester, "wdkModel/recordClassSet/recordClass/testParamValues",
        "addParamValuesSet");

    configureNode(digester, "wdkModel/recordClassSet/recordClass/propertyList", PropertyList.class,
        "addPropertyList");

    // reporter
    configureNode(digester, "wdkModel/recordClassSet/recordClass/reporter", ReporterRef.class,
        "addReporterRef");
    configureNode(digester, "wdkModel/recordClassSet/recordClass/reporter/description", WdkModelText.class, "setDescription");
    digester.addCallMethod("wdkModel/recordClassSet/recordClass/reporter/description", "setText", 0);
    configureNode(digester, "wdkModel/recordClassSet/recordClass/reporter/property", WdkModelText.class, "addProperty");
    digester.addCallMethod("wdkModel/recordClassSet/recordClass/reporter/property", "setText", 0);

    // Default attribute tool bundle
    configureNode(digester, "wdkModel/recordClassSet/recordClass/defaultColumnToolBundle",
        DefaultColumnToolBundleRef.class, "setDefaultColumnToolBundleRef");

    // attribute query ref
    configureNode(digester, "wdkModel/recordClassSet/recordClass/attributeQueryRef",
        AttributeQueryReference.class, "addAttributesQueryRef");

    // tables
    configureNode(digester, "wdkModel/recordClassSet/recordClass/table", TableField.class, "addTableField");

    configureNode(digester, "wdkModel/recordClassSet/recordClass/table/description", WdkModelText.class,
        "addDescription");
    digester.addCallMethod("wdkModel/recordClassSet/recordClass/table/description", "setText", 0);

    // tableField's property list
    configureNode(digester, "wdkModel/recordClassSet/recordClass/table/propertyList", PropertyList.class,
        "addPropertyList");

    configureNode(digester, "wdkModel/recordClassSet/recordClass/table/columnAttribute",
        QueryColumnAttributeField.class, "addAttributeField");

    // attributeField's property list
    configureNode(digester, "wdkModel/recordClassSet/recordClass/table/columnAttribute/propertyList", PropertyList.class,
        "addPropertyList");

    // result size query ref
    configureNode(digester, "wdkModel/recordClassSet/recordClass/resultSizeQueryRef",
        ResultSizeQueryReference.class, "setResultSizeQueryRef");

    // result properties plugin ref
    configureNode(digester, "wdkModel/recordClassSet/recordClass/resultPropertyQueryRef",
        ResultPropertyQueryReference.class, "setResultPropertyQueryRef");

    configureNode(digester, "wdkModel/recordClassSet/recordClass/summaryView", SummaryView.class,
        "addSummaryView");

    configureStepAnalysisNode(digester, "wdkModel/recordClassSet/recordClass/stepAnalysisRef");

    configureNode(digester, "wdkModel/recordClassSet/recordClass/summaryView/description",
        WdkModelText.class, "addDescription");
    digester.addCallMethod("wdkModel/recordClassSet/recordClass/summaryView/description", "setText", 0);

    configureNode(digester, "wdkModel/recordClassSet/recordClass/recordView", RecordView.class,
        "addRecordView");

    configureNode(digester, "wdkModel/recordClassSet/recordClass/recordView/description", WdkModelText.class,
        "addDescription");
    digester.addCallMethod("wdkModel/recordClassSet/recordClass/recordView/description", "setText", 0);

    configureNode(digester, "wdkModel/recordClassSet/recordClass/filterRef", FilterReference.class,
        "addFilterReference");

    configureNode(digester, "wdkModel/recordClassSet/recordClass/count", CountReference.class,
        "setCountReference");

    configureNode(digester, "wdkModel/recordClassSet/recordClass/boolean", BooleanReference.class,
        "setBooleanReference");

}

  private static void configureQuerySet(Digester digester) {
    // QuerySet
    configureNode(digester, "wdkModel/querySet", QuerySet.class, "addQuerySet");

    configureNode(digester, "wdkModel/querySet/postCacheUpdateSql",
            PostCacheUpdateSql.class, "addPostCacheUpdateSql");
    configureNode(digester, "wdkModel/querySet/postCacheUpdateSql/sql",
            WdkModelText.class, "setSql");
    digester.addCallMethod("wdkModel/querySet/postCacheUpdateSql/sql", "setText", 0);

    // defaultTestParamValues
    configureParamValuesSet(digester, "wdkModel/querySet/defaultTestParamValues", "addDefaultParamValuesSet");

    // cardinalitySql
    configureNode(digester, "wdkModel/querySet/testRowCountSql", WdkModelText.class, "addTestRowCountSql");
    digester.addCallMethod("wdkModel/querySet/testRowCountSql", "setText", 0);

    // sqlQuery
    configureNode(digester, "wdkModel/querySet/sqlQuery", SqlQuery.class, "addQuery");

    // testParamValues
    configureParamValuesSet(digester, "wdkModel/querySet/sqlQuery/testParamValues", "addParamValuesSet");

    configureNode(digester, "wdkModel/querySet/sqlQuery/sql", WdkModelText.class, "addSql");
    digester.addCallMethod("wdkModel/querySet/sqlQuery/sql", "setText", 0);

    configureNode(digester, "wdkModel/querySet/sqlQuery/paramRef", ParamReference.class, "addParamRef");

    configureNode(digester, "wdkModel/querySet/sqlQuery/column", Column.class, "addColumn");

    configureNode(digester, "wdkModel/querySet/sqlQuery/sqlParamValue", WdkModelText.class,
        "addSqlParamValue");
    digester.addCallMethod("wdkModel/querySet/sqlQuery/sqlParamValue", "setText", 0);

    configureNode(digester, "wdkModel/querySet/sqlQuery/postCacheUpdateSql",
        PostCacheUpdateSql.class, "addPostCacheUpdateSql");
    configureNode(digester, "wdkModel/querySet/sqlQuery/postCacheUpdateSql/sql", WdkModelText.class, "setSql");
    digester.addCallMethod("wdkModel/querySet/sqlQuery/postCacheUpdateSql/sql", "setText", 0);


    // processQuery
    configureNode(digester, "wdkModel/querySet/processQuery", ProcessQuery.class, "addQuery");

    // testParamValues
    configureParamValuesSet(digester, "wdkModel/querySet/processQuery/testParamValues", "addParamValuesSet");

    configureNode(digester, "wdkModel/querySet/processQuery/paramRef", ParamReference.class, "addParamRef");

    configureNode(digester, "wdkModel/querySet/processQuery/wsColumn", Column.class, "addColumn");

    configureNode(digester, "wdkModel/querySet/processQuery/postCacheUpdateSql",
        PostCacheUpdateSql.class, "addPostCacheUpdateSql");
    configureNode(digester, "wdkModel/querySet/processQuery/postCacheUpdateSql/sql", WdkModelText.class, "setSql");
    digester.addCallMethod("wdkModel/querySet/processQuery/postCacheUpdateSql/sql", "setText", 0);

  }

  private static void configureParamSet(Digester digester) {
    // ParamSet
    configureNode(digester, "wdkModel/paramSet", ParamSet.class, "addParamSet");

    // string param
    String path = "wdkModel/paramSet/stringParam";
    configureNode(digester, path, StringParam.class, "addParam");
    configureParamContent(digester, path, ParamSuggestion.class);
    configureNode(digester, path + "/regex", WdkModelText.class, "addRegex");
    digester.addCallMethod(path + "/regex", "setText", 0);

    // number param
    path = "wdkModel/paramSet/numberParam";
    configureNode(digester, path, NumberParam.class, "addParam");
    configureParamContent(digester, path, ParamSuggestion.class);
    configureNode(digester, path + "/regex", WdkModelText.class, "addRegex");
    digester.addCallMethod(path + "/regex", "setText", 0);

    // number range param
    path = "wdkModel/paramSet/numberRangeParam";
    configureNode(digester, path, NumberRangeParam.class, "addParam");
    configureParamContent(digester, path, ParamSuggestion.class);
    configureNode(digester, path + "/regex", WdkModelText.class, "addRegex");
    digester.addCallMethod(path + "/regex", "setText", 0);

    // date param
    path = "wdkModel/paramSet/dateParam";
    configureNode(digester, path, DateParam.class, "addParam");
    configureParamContent(digester, path, ParamSuggestion.class);
    configureNode(digester, path + "/regex", WdkModelText.class, "addRegex");
    digester.addCallMethod(path + "/regex", "setText", 0);

    // date range param
    path = "wdkModel/paramSet/dateRangeParam";
    configureNode(digester, path, DateRangeParam.class, "addParam");
    configureParamContent(digester, path, ParamSuggestion.class);
    configureNode(digester, path + "/regex", WdkModelText.class, "addRegex");
    digester.addCallMethod(path + "/regex", "setText", 0);

    // flatVocabParam
    path = "wdkModel/paramSet/flatVocabParam";
    configureNode(digester, path, FlatVocabParam.class, "addParam");
    configureParamContent(digester, path, EnumParamSuggestion.class);

    // filterParamNew
    path = "wdkModel/paramSet/filterParam";
    configureNode(digester, path, FilterParamNew.class, "addParam");
    configureParamContent(digester, path, ParamSuggestion.class);


    // answer param
    path = "wdkModel/paramSet/answerParam";
    configureNode(digester, path, AnswerParam.class, "addParam");
    configureParamContent(digester, path, ParamSuggestion.class);
    configureNode(digester, path + "/recordClass", RecordClassReference.class, "addRecordClassRef");

    // dataset param
    path = "wdkModel/paramSet/datasetParam";
    configureNode(digester, path, DatasetParam.class, "addParam");
    configureParamContent(digester, path, DatasetParamSuggestion.class);
    configureNode(digester, path + "/parser", DatasetParserReference.class, "addParserReference");
    configureNode(digester, path + "/parser/property", WdkModelText.class, "addProperty");
    digester.addCallMethod(path + "/parser/property", "setText", 0);

    // enum param
    path = "wdkModel/paramSet/enumParam";
    configureNode(digester, path, EnumParam.class, "addParam");
    configureParamContent(digester, path, EnumParamSuggestion.class);

    path = path + "/enumList";
    configureNode(digester, path, EnumItemList.class, "addEnumItemList");

    configureNode(digester, path + "/enumValue", EnumItem.class, "addEnumItem");
    digester.addBeanPropertySetter(path + "/enumValue/display");
    digester.addBeanPropertySetter(path + "/enumValue/term");
    digester.addBeanPropertySetter(path + "/enumValue/internal");
    digester.addBeanPropertySetter(path + "/enumValue/parentTerm");

    configureNode(digester, path + "/enumValue/dependedValue", WdkModelText.class, "addDependedValue");
    digester.addCallMethod(path + "/enumValue/dependedValue", "setText", 0);

    // timestamp param
    path = "wdkModel/paramSet/timestampParam";
    configureNode(digester, path, TimestampParam.class, "addParam");
    configureParamContent(digester, path, ParamSuggestion.class);
  }

  private static void configureParamContent(Digester digester, String paramPath, Class<?> suggestionClass) {
    configureNode(digester, paramPath + "/suggest", suggestionClass, "addSuggest");
    configureNode(digester, paramPath + "/noTranslation", ParamConfiguration.class, "addNoTranslation");
    configureNode(digester, paramPath + "/handler", ParamHandlerReference.class, "addHandler");
    configureNode(digester, paramPath + "/handler/property", WdkModelText.class, "addProperty");
    digester.addCallMethod(paramPath + "/handler/property", "setText", 0);
    configureNode(digester, paramPath + "/propertyList", PropertyList.class, "addPropertyList");
  }

  private static void configureQuestionSet(Digester digester) {
    // QuestionSet
    configureNode(digester, "wdkModel/questionSet", QuestionSet.class, "addQuestionSet");

    configureNode(digester, "wdkModel/questionSet/description", WdkModelText.class, "addDescription");
    digester.addCallMethod("wdkModel/questionSet/description", "setText", 0);

    // question
    configureNode(digester, "wdkModel/questionSet/question", Question.class, "addQuestion");

    configureNode(digester, "wdkModel/questionSet/question/description", WdkModelText.class, "addDescription");
    digester.addCallMethod("wdkModel/questionSet/question/description", "setText", 0);

    configureNode(digester, "wdkModel/questionSet/question/summary", WdkModelText.class, "addSummary");
    digester.addCallMethod("wdkModel/questionSet/question/summary", "setText", 0);

    configureNode(digester, "wdkModel/questionSet/question/searchVisibleHelp", WdkModelText.class, "addSearchVisibleHelp");
    digester.addCallMethod("wdkModel/questionSet/question/searchVisibleHelp", "setText", 0);

    // question's property list
    configureNode(digester, "wdkModel/questionSet/question/propertyList", PropertyList.class,
        "addPropertyList");

    configureNode(digester, "wdkModel/questionSet/question/attributesList", AttributeList.class,
        "addAttributeList");

    // dynamic attribute set
    configureNode(digester, "wdkModel/questionSet/question/dynamicAttributes", DynamicAttributeSet.class,
        "addDynamicAttributeSet");

    configureNode(digester, "wdkModel/questionSet/question/dynamicAttributes/columnAttribute",
        QueryColumnAttributeField.class, "addAttributeField");

    // attributeField's property list
    configureNode(digester, "wdkModel/questionSet/question/dynamicAttributes/columnAttribute/propertyList",
        PropertyList.class, "addPropertyList");

    configureNode(digester, "wdkModel/questionSet/question/paramRef", ParamReference.class, "addParamRef");

    configureNode(digester, "wdkModel/questionSet/question/sqlParamValue", WdkModelText.class,
        "addSqlParamValue");
    digester.addCallMethod("wdkModel/questionSet/question/sqlParamValue", "setText", 0);

    configureNode(digester, "wdkModel/questionSet/question/summaryView", SummaryView.class, "addSummaryView");

    configureStepAnalysisNode(digester, "wdkModel/questionSet/question/stepAnalysisRef");

    configureNode(digester, "wdkModel/questionSet/question/summaryView/description", WdkModelText.class,
        "addDescription");
    digester.addCallMethod("wdkModel/questionSet/question/summaryView/description", "setText", 0);

    configureNode(digester, "wdkModel/questionSet/question/suggestion", QuestionSuggestion.class,
        "addSuggestion");
  }

  private static void configureXmlQuestionSet(Digester digester) {
    // load XmlQuestionSet
    configureNode(digester, "wdkModel/xmlQuestionSet", XmlQuestionSet.class, "addXmlQuestionSet");

    configureNode(digester, "wdkModel/xmlQuestionSet/description", WdkModelText.class, "addDescription");
    digester.addCallMethod("wdkModel/xmlQuestionSet/description", "setText", 0);

    // load XmlQuestion
    configureNode(digester, "wdkModel/xmlQuestionSet/xmlQuestion", XmlQuestion.class, "addQuestion");

    configureNode(digester, "wdkModel/xmlQuestionSet/xmlQuestion/description", WdkModelText.class,
        "addDescription");
    digester.addCallMethod("wdkModel/xmlQuestionSet/xmlQuestion/description", "setText", 0);
  }

  private static void configureParamValuesSet(Digester digester, String path, String addMethodName) {
    configureNode(digester, path, ParamValuesSet.class, addMethodName);
    configureNode(digester, path + "/paramValue", ParamValue.class, "addParamValue");
    digester.addCallMethod(path + "/paramValue", "setValue", 0);
    /*
     * digester.addObjectCreate(path, ParamValuesSet.class); digester.addSetProperties(path);
     * digester.addCallMethod(path + "/paramValue", "put", 2); digester.addCallParam(path + "/paramValue", 0,
     * "name"); digester.addCallParam(path + "/paramValue", 1); digester.addSetNext(path, addMethodName);
     */
  }

  private static void configureXmlRecordClassSet(Digester digester) {
    // load XmlRecordClassSet
    configureNode(digester, "wdkModel/xmlRecordClassSet", XmlRecordClassSet.class, "addXmlRecordClassSet");

    // load XmlRecordClass
    configureNode(digester, "wdkModel/xmlRecordClassSet/xmlRecordClass", XmlRecordClass.class,
        "addRecordClass");

    // load XmlAttributeField
    configureNode(digester, "wdkModel/xmlRecordClassSet/xmlRecordClass/xmlAttribute",
        XmlAttributeField.class, "addAttributeField");

    // load XmlTableField
    configureNode(digester, "wdkModel/xmlRecordClassSet/xmlRecordClass/xmlTable", XmlTableField.class,
        "addTableField");

    // load XmlAttributeField within table
    configureNode(digester, "wdkModel/xmlRecordClassSet/xmlRecordClass/xmlTable/xmlAttribute",
        XmlAttributeField.class, "addAttributeField");
  }

  private static void configureGroupSet(Digester digester) {
    // load GroupSet
    configureNode(digester, "wdkModel/groupSet", GroupSet.class, "addGroupSet");

    // load group
    configureNode(digester, "wdkModel/groupSet/group", Group.class, "addGroup");

    configureNode(digester, "wdkModel/groupSet/group/description", WdkModelText.class, "addDescription");
    digester.addCallMethod("wdkModel/groupSet/group/description", "setText", 0);
  }

  private static void configureAttributeFields(Digester digester) {
    // primary key
    String prefixPk = "wdkModel/recordClassSet/recordClass/primaryKey";
    configureNode(digester, prefixPk, PrimaryKeyDefinition.class, "setPrimaryKeyDefinition");
    configureNode(digester, prefixPk + "/columnRef", WdkModelText.class, "addColumnRef");
    digester.addCallMethod(prefixPk + "/columnRef", "setText", 0);

    // id attribute
    String prefixIdAttr = "wdkModel/recordClassSet/recordClass/idAttribute";
    configureNode(digester, prefixIdAttr, IdAttributeField.class, "addAttributeField");
    configureNode(digester, prefixIdAttr + "/propertyList", PropertyList.class, "addPropertyList");
    configureNode(digester, prefixIdAttr + "/text", WdkModelText.class, "addText");
    digester.addCallMethod(prefixIdAttr + "/text", "setText", 0);
    configureNode(digester, prefixIdAttr + "/display", WdkModelText.class, "addDisplay");
    digester.addCallMethod(prefixIdAttr + "/display", "setText", 0);
    configureAttributeReporters(digester, "primaryKeyAttribute");

    // pk column attributes
    configureNode(digester, "*/pkColumnAttribute", PkColumnAttributeField.class, "addAttributeField");
    configureNode(digester, "*/pkColumnAttribute/propertyList", PropertyList.class, "addPropertyList");
    configureAttributeReporters(digester, "pkColumnAttribute");

    // column attributes
    configureNode(digester, "*/columnAttribute", QueryColumnAttributeField.class, "addAttributeField");
    configureNode(digester, "*/columnAttribute/propertyList", PropertyList.class, "addPropertyList");
    configureNode(digester, "*/columnAttribute/filterRef", FilterReference.class, "addFilterReference");
    configureNode(digester, "*/columnAttribute/htmlHelp", WdkModelText.class, "addHtmlHelp");
    digester.addCallMethod("*/columnAttribute/htmlHelp", "setText", 0);
    configureAttributeReporters(digester, "columnAttribute");

    // link attribute
    configureNode(digester, "*/linkAttribute", LinkAttributeField.class, "addAttributeField");
    configureNode(digester, "*/linkAttribute/propertyList", PropertyList.class, "addPropertyList");
    configureNode(digester, "*/linkAttribute/url", WdkModelText.class, "addUrl");
    digester.addCallMethod("*/linkAttribute/url", "setText", 0);
    configureNode(digester, "*/linkAttribute/displayText", WdkModelText.class, "addDisplayText");
    digester.addCallMethod("*/linkAttribute/displayText", "setText", 0);
    configureNode(digester, "*/linkAttribute/tooltip", WdkModelText.class, "addTooltip");
    digester.addCallMethod("*/linkAttribute/tooltip", "setText", 0);
    configureAttributeReporters(digester, "linkAttribute");

    // text attribute
    configureNode(digester, "*/textAttribute", TextAttributeField.class, "addAttributeField");
    configureNode(digester, "*/textAttribute/propertyList", PropertyList.class, "addPropertyList");
    configureNode(digester, "*/textAttribute/text", WdkModelText.class, "addText");
    digester.addCallMethod("*/textAttribute/text", "setText", 0);
    configureNode(digester, "*/textAttribute/display", WdkModelText.class, "addDisplay");
    digester.addCallMethod("*/textAttribute/display", "setText", 0);
    configureNode(digester, "*/linkAttribute/tooltip", WdkModelText.class, "addTooltip");
    digester.addCallMethod("*/linkAttribute/tooltip", "setText", 0);
    configureAttributeReporters(digester, "textAttribute");
  }

  private static void configureAttributeReporters(Digester digester, String attribute) {
    String prefix = "*/" + attribute + "/reporter";
    // configure plugins for
    configureNode(digester, prefix, org.gusdb.wdk.model.report.AttributeReporterRef.class, "addReporterReference");
    configureNode(digester, prefix + "/property", WdkModelText.class, "addProperty");
    digester.addCallMethod(prefix + "/property", "setText", 0);

  }

  private static void configureCommonNodes(Digester digester) {
    configureNode(digester, "*/help", WdkModelText.class, "addHelp");
    digester.addCallMethod("*/help", "setText", 0);

		configureNode(digester, "*/visibleHelp", WdkModelText.class, "addVisibleHelp");
    digester.addCallMethod("*/visibleHelp", "setText", 0);

    configureNode(digester, "*/value", WdkModelText.class, "addValue");
    digester.addCallMethod("*/value", "setText", 0);
  }

  private static void configureUiConfig(Digester digester) {
    configureNode(digester, "wdkModel/uiConfig", UIConfig.class, "setUiConfig");
    configureNode(digester, "wdkModel/uiConfig/extraLogoutCookies", ExtraLogoutCookies.class,
        "setExtraLogoutCookies");
    configureNode(digester, "wdkModel/uiConfig/extraLogoutCookies/cookie", CookieBuilder.class, "add");
  }

  private static void configureStepAnalysis(Digester digester) {
    configureNode(digester, "wdkModel/stepAnalysisPlugins", StepAnalysisPlugins.class,
        "setStepAnalysisPlugins");
    configureNode(digester, "wdkModel/stepAnalysisPlugins/executionConfig",
        StepAnalysisPlugins.ExecutionConfig.class, "setExecutionConfig");
    configureStepAnalysisNode(digester, "wdkModel/stepAnalysisPlugins/stepAnalysisPlugin");
  }

  private static void configureStepAnalysisNode(Digester digester, String nodeLocation) {
    configureNode(digester, nodeLocation, StepAnalysisXml.class, "addStepAnalysis");
    configureNode(digester, nodeLocation + "/paramRef", ParamReference.class, "addParamRef");
    configureNode(digester, nodeLocation + "/shortDescription", WdkModelText.class, "setShortDescription");
    digester.addCallMethod(nodeLocation + "/shortDescription", "setText", 0);
    configureNode(digester, nodeLocation + "/description", WdkModelText.class, "setDescription");
    digester.addCallMethod(nodeLocation + "/description", "setText", 0);
  }

  private static void configureFilterSet(Digester digester) {
    // load filter set
    configureNode(digester, "wdkModel/filterSet", FilterSet.class, "addFilterSet");

    // load filter
    configureNode(digester, "wdkModel/filterSet/stepFilter", StepFilterDefinition.class, "addStepFilter");
    configureNode(digester, "wdkModel/filterSet/stepFilter/display", WdkModelText.class, "addDisplay");
    digester.addCallMethod("wdkModel/filterSet/stepFilter/display", "setText", 0);
    configureNode(digester, "wdkModel/filterSet/stepFilter/description", WdkModelText.class, "addDescription");
    digester.addCallMethod("wdkModel/filterSet/stepFilter/description", "setText", 0);
    configureNode(digester, "wdkModel/filterSet/stepFilter/property", WdkModelText.class, "addProperty");
    digester.addCallMethod("wdkModel/filterSet/stepFilter/property", "setText", 0);
  }

  public static void main(String[] args) {
    try {
      String cmdName = System.getProperty("cmdName");
      Options options = declareOptions();
      CommandLine cmdLine = parseOptions(cmdName, options, args);
      String projectId = cmdLine.getOptionValue(Utilities.ARGUMENT_PROJECT_ID);

      // process args
      try (WdkModel wdkModel = WdkModel.construct(projectId, GusHome.getGusHome())) {
        if (cmdLine.hasOption(ARG_DEPENDENCY)) { // print out only the dependency
          System.out.println(wdkModel.getDependencyTree());
        }
        else { // print out the model content
          System.out.println(wdkModel.toString());
        }
      }
    }
    catch (Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    }
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
    addOption(options, "model", "The ProjectId, which"
          + " should match the directory name under $GUS_HOME where "
          + "model-config.xml is stored.");

    Option optDependency = new Option(ARG_DEPENDENCY, false, "Print out the dependency tree of the model");
    optDependency.setRequired(false);
    optDependency.setArgName(ARG_DEPENDENCY);
    options.addOption(optDependency);

    return options;
  }

  private static CommandLine parseOptions(String cmdName, Options options, String[] args) {

    CommandLineParser parser = new DefaultParser();
    CommandLine cmdLine = null;
    try {
      // parse the command line arguments
      cmdLine = parser.parse(options, args);
    }
    catch (ParseException exp) {
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

    String header = newline + "Parse and print out a WDK Model xml file." + newline + newline + "Options:";

    String footer = "";

    // PrintWriter stderr = new PrintWriter(System.err);
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(75, cmdlineSyntax, header, options, footer);
    System.exit(1);
  }
}
