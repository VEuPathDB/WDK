package org.gusdb.wdk.controller.wizard;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.digester.Digester;
import org.apache.log4j.Logger;
import org.gusdb.fgputil.xml.XmlParser;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.xml.sax.SAXException;

public class WizardParser extends XmlParser {

    private static final Logger logger = Logger.getLogger(WizardParser.class);

    private final String _gusHome;

    public WizardParser(String gusHome) {
      _gusHome = gusHome;
    }

    @Override
    protected Digester configureDigester() {
        Digester digester = new Digester();
        digester.setValidating(false);

        digester.addObjectCreate("wdkWizard", Wizard.class);
        digester.addSetProperties("wdkWizard");

        // load default stage reference
        configureNode(digester, "wdkWizard/defaultStageRef",
                StageReference.class, "addDefaultStageReference");

        // load stages
        configureNode(digester, "wdkWizard/stage", Stage.class, "addStage");
        configureNode(digester, "wdkWizard/stage/description",
                WdkModelText.class, "addDescription");
        digester.addCallMethod("wdkWizard/stage/description", "setText", 0);

        // load result
        configureNode(digester, "wdkWizard/stage/result",
                Result.class, "addResult");
        digester.addCallMethod("wdkWizard/stage/result", "setText", 0);

        return digester;
    }

    public Wizard parseWizard(String resource) throws WdkModelException,
            SAXException, IOException {
        File file = new File(resource);
        if (!file.exists()) {
            logger.debug("wdk step wizard '" + resource + "' doesn't exist");
            return null;
        }

        // validate the process model file
        configureValidator(_gusHome + "/lib/rng/wdkWizard.rng");
        URL configFileUrl = file.toURI().toURL();
        if (!validate(configFileUrl)) {
          throw new WdkModelException("Validation failed: " + configFileUrl.toExternalForm());
        }

        Wizard wizard = (Wizard) getDigester().parse(new FileInputStream(file));
        wizard.setFile(resource);
        return wizard;

    }
}
