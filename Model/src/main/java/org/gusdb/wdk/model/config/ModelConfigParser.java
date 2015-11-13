package org.gusdb.wdk.model.config;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.digester.Digester;
import org.gusdb.fgputil.xml.XmlParser;
import org.gusdb.wdk.model.WdkModelException;
import org.xml.sax.SAXException;

/**
 * This class parses the {@code model-config.xml}, and creates
 * {@link ModelConfig} object to hold the configuration information.
 * 
 * @author jerric
 * 
 */
public class ModelConfigParser extends XmlParser {

  private final String gusHome;

  public ModelConfigParser(String gusHome) {
    this.gusHome = gusHome;
  }

  public ModelConfig parseConfig(String projectId) throws SAXException,
      IOException, WdkModelException {
    // validate the configuration file
    URL configURL = makeURL(gusHome + "/config/" + projectId + "/model-config.xml");

    configureValidator(gusHome + "/lib/rng/wdkModel-config.rng");
    if (!validate(configURL)) {
      throw new WdkModelException("Validation failed: " + configURL.toExternalForm());
    }
    ModelConfig modelConfig = (ModelConfig) getDigester().parse(configURL.openStream());
    modelConfig.setGusHome(gusHome);
    modelConfig.setProjectId(projectId);
    return modelConfig;
  }

  @Override
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
    digester.addCallMethod("modelConfig/queryMonitor/ignoreBrokenQueryRegex",
        "addIgnoreBrokenQueryRegex", 0);

    return digester;
  }
}
