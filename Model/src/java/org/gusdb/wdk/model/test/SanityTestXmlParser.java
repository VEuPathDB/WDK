package org.gusdb.wdk.model.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

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
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.XmlParser;
import org.gusdb.wdk.model.implementation.ModelXmlParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SanityTestXmlParser extends XmlParser {

    private static final Logger logger = Logger.getLogger(SanityTestXmlParser.class);

    public SanityTestXmlParser(String gusHome) throws SAXException, IOException {
        super(gusHome, "lib/rng/sanityModel.rng");
    }

    public SanityModel parseModel(String modelName)
            throws SAXException, IOException, ParserConfigurationException,
            TransformerFactoryConfigurationError, TransformerException,
            WdkModelException {
        // load model
        ModelXmlParser parser = new ModelXmlParser(gusHome);
        WdkModel wdkModel = parser.parseModel(modelName);
        return parseModel(modelName, wdkModel);
    }

    public SanityModel parseModel(String modelName, WdkModel wdkModel)
            throws WdkModelException, SAXException, IOException,
            ParserConfigurationException, TransformerFactoryConfigurationError,
            TransformerException {
        // construct urls to model file, prop file, and config file
        URL sanityModelURL = makeURL(gusHome, "lib/wdk/" + modelName
                + "-sanity.xml");
        URL modelPropURL = makeURL(gusHome, "config/" + modelName + ".prop");

        // validate the master model file
        if (!validate(sanityModelURL))
            throw new WdkModelException(
                    "Master sanity model validation failed.");

        // replace any <import> tag with content from sub-models in the
        // master model, and build the master document
        Document masterDoc = buildMasterDocument(sanityModelURL);

        // load property map
        Map<String, String> properties = getPropMap(modelPropURL);
        InputStream inStream = substituteProps(masterDoc, properties);

        SanityModel sanityModel = (SanityModel) digester.parse(inStream);

        // resolve the reference of the sanity model
        sanityModel.resolveReferences(wdkModel);

        return sanityModel;
    }

    private Document buildMasterDocument(URL wdkModelURL)
            throws SAXException, IOException, ParserConfigurationException,
            WdkModelException {
        // get the xml document of the model
        Document masterDoc = buildDocument(wdkModelURL);
        Node rootNode = masterDoc.getElementsByTagName("sanityModel").item(0);

        // get all imports, and replace each of them with the sub-model
        NodeList importNodes = masterDoc.getElementsByTagName("import");
        for (int i = 0; i < importNodes.getLength(); i++) {
            // get url to the first import
            Node importNode = importNodes.item(i);
            String href = importNode.getAttributes().getNamedItem("file").getNodeValue();
            URL importURL = makeURL(gusHome, "lib/wdk/" + href);

            // validate the sub-model
            if (!validate(importURL))
                throw new WdkModelException("sub sanity model "
                        + importURL.toExternalForm() + " validation failed.");

            logger.debug("Importing: " + importURL.toExternalForm());

            Document importDoc = buildDocument(importURL);

            // get the children nodes from imported sub-model, and add them
            // into master document
            Node subRoot = importDoc.getElementsByTagName("sanityModel").item(0);
            NodeList childrenNodes = subRoot.getChildNodes();
            for (int j = 0; j < childrenNodes.getLength(); j++) {
                Node childNode = childrenNodes.item(j);
                if (childNode instanceof Element) {
                    Node imported = masterDoc.importNode(childNode, true);
                    rootNode.appendChild(imported);
                }
            }
        }
        return masterDoc;
    }

    private Map<String, String> getPropMap(URL modelPropURL) throws IOException {
        Map<String, String> propMap = new LinkedHashMap<String, String>();
        Properties properties = new Properties();
        properties.load(modelPropURL.openStream());
        Iterator<Object> it = properties.keySet().iterator();
        while (it.hasNext()) {
            String propName = (String) it.next();
            String value = properties.getProperty(propName);
            propMap.put(propName, value);
        }
        return propMap;
    }

    private InputStream substituteProps(Document masterDoc,
            Map<String, String> properties)
            throws TransformerFactoryConfigurationError, TransformerException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // transform the DOM doc to a string
        Source source = new DOMSource(masterDoc);
        Result result = new StreamResult(out);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(source, result);
        String content = new String(out.toByteArray());

        // substitute prop macros
        for (String propName : properties.keySet()) {
            String propValue = properties.get(propName);
            content = content.replaceAll("\\@" + propName + "\\@", propValue);
        }

        // construct input stream
        return new ByteArrayInputStream(content.getBytes());
    }

    protected Digester configureDigester() {
        Digester digester = new Digester();
        digester.setValidating(false);

        // Root -- Sanity Model

        digester.addObjectCreate("sanityModel", SanityModel.class);
        digester.addSetProperties("sanityModel");

        // SanityRecord
        configureNode(digester, "sanityModel/sanityRecord", SanityRecord.class,
                "addSanityRecord");

        configureNode(digester, "sanityModel/sanityQuery", SanityQuery.class,
                "addSanityQuery");

        configureNode(digester, "sanityModel/sanityQuery/sanityParam",
                SanityParam.class, "addParam");

        configureNode(digester, "sanityModel/sanityQuestion",
                SanityQuestion.class, "addSanityQuestion");

        configureNode(digester, "sanityModel/sanityQuestion/sanityParam",
                SanityParam.class, "addParam");

        configureNode(digester, "sanityModel/sanityXmlQuestion",
                SanityXmlQuestion.class, "addSanityXmlQuestion");

        return digester;
    }

    public static void main(String[] args)
            throws SAXException, IOException, WdkModelException,
            ParserConfigurationException, TransformerFactoryConfigurationError,
            TransformerException {
        String cmdName = System.getProperty("cmdName");
        String gusHome = System.getProperty(Utilities.SYS_PROP_GUS_HOME);

        // process args
        Options options = declareOptions();
        CommandLine cmdLine = parseOptions(cmdName, options, args);
        String modelName = cmdLine.getOptionValue(Utilities.ARGUMENT_MODEL);

        // create a parser, and parse the model file
        SanityTestXmlParser parser = new SanityTestXmlParser(gusHome);
        SanityModel sanityModel = parser.parseModel(modelName);

        System.out.println(sanityModel.toString());
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
                + "find the Sanity Model XML file "
                + "($GUS_HOME/lib/wdk/<model>-sanity.xml) and the Model "
                + "property file ($GUS_HOME/config/<model>.prop) ");

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
