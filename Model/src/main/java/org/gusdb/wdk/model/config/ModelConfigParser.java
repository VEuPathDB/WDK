package org.gusdb.wdk.model.config;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.digester3.Digester;
import org.gusdb.fgputil.xml.XmlParser;
import org.gusdb.fgputil.xml.XmlValidator;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.xml.sax.SAXException;

/**
 * This class parses the {@code model-config.xml}, and creates
 * {@link ModelConfig} object to hold the configuration information.
 * 
 * @author jerric
 * 
 */
public class ModelConfigParser extends XmlParser {

  private final String _gusHome;
  private final Digester _digester;

  public ModelConfigParser(String gusHome) {
    _gusHome = gusHome;
    _digester = configureDigester();
  }

  public ModelConfigBuilder parseConfig(String projectId) throws SAXException, IOException, WdkModelException {

    // get URL to the config file
    URL configURL = makeURL(_gusHome + "/config/" + projectId + "/model-config.xml");

    // validate the configuration file
    XmlValidator validator = new XmlValidator(_gusHome + "/lib/rng/wdkModel-config.rng");
    if (!validator.validate(configURL)) {
      throw new WdkModelException("Validation failed: " + configURL.toExternalForm());
    }

    // RNG validation passed; create a model config object
    ModelConfigBuilder modelConfig = (ModelConfigBuilder) _digester.parse(configURL.openStream());
    modelConfig.setGusHome(_gusHome);
    modelConfig.setProjectId(projectId);
    return modelConfig;
  }

  private static Digester configureDigester() {

    Digester digester = new Digester();
    digester.setValidating(false);

    digester.addObjectCreate("modelConfig", ModelConfigBuilder.class);
    digester.addSetProperties("modelConfig");
    digester.addBeanPropertySetter("modelConfig/paramRegex");
    digester.addBeanPropertySetter("modelConfig/emailContent");
    digester.addBeanPropertySetter("modelConfig/emailSubject");

    // load application db
    configureNode(digester, "modelConfig/appDb", ModelConfigAppDB.class, "setAppDB");

    // load user db
    configureNode(digester, "modelConfig/userDb", ModelConfigUserDB.class, "setUserDB");

    // load user db
    configureNode(digester, "modelConfig/accountDb", ModelConfigAccountDB.class, "setAccountDB");

    // userdatasetstore
    configureNode(digester, "modelConfig/userDatasetStore", ModelConfigUserDatasetStore.class, "setUserDatasetStore");
    configureNode(digester, "modelConfig/userDatasetStore/property", WdkModelText.class, "addProperty");
    digester.addCallMethod("modelConfig/userDatasetStore/property", "setText", 0);
    configureNode(digester, "modelConfig/userDatasetStore/typeHandler", ModelConfigUserDatasetTypeHandler.class, "addTypeHandler");

    // query monitor
    configureNode(digester, "modelConfig/queryMonitor", QueryMonitor.class, "setQueryMonitor");
    digester.addCallMethod("modelConfig/queryMonitor/ignoreSlowQueryRegex", "addIgnoreSlowQueryRegex", 0);
    digester.addCallMethod("modelConfig/queryMonitor/ignoreBrokenQueryRegex", "addIgnoreBrokenQueryRegex", 0);

    return digester;
  }
}
