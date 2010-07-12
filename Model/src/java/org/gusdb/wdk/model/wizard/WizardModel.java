package org.gusdb.wdk.model.wizard;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.json.JSONException;
import org.xml.sax.SAXException;

public class WizardModel {

    private static final String WIZARD_PATH = "/xml/wdk-wizard/";

    private List<Wizard> wizardList = new ArrayList<Wizard>();
    private Map<String, Wizard> wizardMap;

    public WizardModel(String gusHome) throws SAXException, IOException,
            WdkModelException {
        // load all the wizards
        WizardParser parser = new WizardParser(gusHome);

        File dir = new File(gusHome + WIZARD_PATH);
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : dir.listFiles()) {
                String fileName = file.getName().toLowerCase();
                if (!fileName.endsWith(".xml")) continue;

                Wizard wizard = parser.parseWizard(file.getAbsolutePath());
                wizardList.add(wizard);
            }
        }
    }

    public Wizard[] getWizards() {
        Wizard[] wizards = new Wizard[wizardMap.size()];
        wizardMap.values().toArray(wizards);
        return wizards;
    }

    public Wizard getWizard(String wizardName) {
        return wizardMap.get(wizardName);
    }

    public void excludeResources(String projectId) throws WdkModelException {
        // exclude wizards
        this.wizardMap = new LinkedHashMap<String, Wizard>();
        for (Wizard wizard : wizardList) {
            if (wizard.include(projectId)) {
                String wizardName = wizard.getName();
                if (wizardMap.containsKey(wizardName))
                    throw new WdkModelException("the wizard '" + wizardName
                            + "' has been defined more than once.");

                wizard.excludeResources(projectId);
                wizardMap.put(wizardName, wizard);
            }
        }
        this.wizardList = null;
    }

    public void resolveReferences(WdkModel wdkModel) throws WdkModelException,
            NoSuchAlgorithmException, WdkUserException, SQLException,
            JSONException {
        for (Wizard wizard : wizardMap.values()) {
            wizard.resolveReferences(wdkModel);
        }
    }
}
