package org.gusdb.wdk.model;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

public class ModelConfigParser extends XmlParser {

    // private static final Logger logger =
    // Logger.getLogger(ModelConfigParser.class);

    public ModelConfigParser(String gusHome) throws SAXException, IOException {
        super(gusHome, "lib/rng/wdkModel-config.rng");
    }

    public ModelConfig parseConfig(String projectId) throws SAXException,
            IOException, WdkModelException {
        // validate the configuration file
        URL configURL = makeURL(gusHome, "config/" + projectId
                + "/model-config.xml");
        if (!validate(configURL))
            throw new WdkModelException("Relax-NG validation failed on "
                    + configURL.toExternalForm());

        ModelConfig modelConfig = (ModelConfig) digester.parse(configURL.openStream());
        modelConfig.setGusHome(gusHome);
        modelConfig.setProjectId(projectId);
        return modelConfig;
    }

    protected Digester configureDigester() {

        Digester digester = new Digester();
        digester.setValidating(false);

        digester.addObjectCreate("modelConfig", ModelConfig.class);
        digester.addSetProperties("modelConfig");
        digester.addBeanPropertySetter("modelConfig/paramRegex");
        digester.addBeanPropertySetter("modelConfig/emailContent");
        digester.addBeanPropertySetter("modelConfig/emailSubject");

        // load application db
        configureNode(digester, "modelConfig/appDb", ModelConfigAppDB.class,
                "setAppDB");

        // load user db
        configureNode(digester, "modelConfig/userDb", ModelConfigUserDB.class,
                "setUserDB");

        configureNode(digester, "modelConfig/queryMonitor", QueryMonitor.class,
                "setQueryMonitor");
        digester.addCallMethod("modelConfig/queryMonitor/ignoreSlowQueryRegex",
                "addIgnoreSlowQueryRegex", 0);
        digester.addCallMethod(
                "modelConfig/queryMonitor/ignoreBrokenQueryRegex",
                "addIgnoreBrokenQueryRegex", 0);

        return digester;
    }
}
