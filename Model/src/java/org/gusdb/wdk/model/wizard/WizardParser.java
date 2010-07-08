package org.gusdb.wdk.model.wizard;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.digester.Digester;
import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.XmlParser;
import org.xml.sax.SAXException;

public class WizardParser extends XmlParser {

    private static final Logger logger = Logger.getLogger(WizardParser.class);

    public WizardParser(String gusHome) throws SAXException, IOException {
        super(gusHome, "lib/rng/wdkWizard.rng");
    }

    @Override
    protected Digester configureDigester() {
        Digester digester = new Digester();
        digester.setValidating(false);

        digester.addObjectCreate("wdkWizard", WizardModel.class);
        digester.addSetProperties("wdkWizard");
        configureNode(digester, "wdkWizard/description", WdkModelText.class,
                "addDescription");
        digester.addCallMethod("wdkWizard/description", "setText", 0);

        // load stages
        configureNode(digester, "wdkWizard/stage", Stage.class, "addStage");
        configureNode(digester, "wdkWizard/stage/description",
                WdkModelText.class, "addDescription");
        digester.addCallMethod("wdkWizard/stage/description", "setText", 0);

        return digester;
    }

    public Wizard parseWizard(String resource) throws WdkModelException,
            SAXException, IOException {
        URL wizardUrl = WizardParser.class.getResource(resource);
        if (wizardUrl == null) {
            logger.debug("wdk step wizard '" + resource + "' doesn't exist");
            return null;
        }

        // validate the process model file
        if (!validate(wizardUrl))
            throw new WdkModelException("XML syntax validation failed on "
                    + wizardUrl.toExternalForm());

        Wizard wizard = (Wizard) digester.parse(wizardUrl.openStream());
        return wizard;

    }
}
