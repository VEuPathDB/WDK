package org.gusdb.wdk.model;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

public class ModelConfigParser extends XmlParser {

    //private static final Logger logger = Logger.getLogger(ModelConfigParser.class);

    public ModelConfigParser(String gusHome) throws SAXException, IOException {
        super(gusHome, "lib/rng/wdkModel-config.rng");
    }

    public ModelConfig parseConfig(String modelName)
            throws SAXException, IOException, WdkModelException  {
        // validate the configuration file
        URL configURL = makeURL(gusHome, "config/" + modelName + "-config.xml");
        if (!validate(configURL))
            throw new WdkModelException("Relax-NG validation failed on "
                    + configURL.toExternalForm());

        return (ModelConfig) digester.parse(configURL.openStream());
    }

    protected Digester configureDigester() {

        Digester digester = new Digester();
        digester.setValidating(false);

        digester.addObjectCreate("modelConfig", ModelConfig.class);
        digester.addSetProperties("modelConfig");
        digester.addBeanPropertySetter("modelConfig/emailSubject");
        digester.addBeanPropertySetter("modelConfig/emailContent");

        return digester;
    }
}
