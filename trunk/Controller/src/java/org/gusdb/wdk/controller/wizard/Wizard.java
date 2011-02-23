package org.gusdb.wdk.controller.wizard;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.json.JSONException;
import org.xml.sax.SAXException;

public class Wizard extends WdkModelBase {

    private static final String WIZARD_PATH = "/lib/wdk/wizard/";
    private static final Logger logger = Logger.getLogger(Wizard.class);

    public static Wizard loadWizard(String gusHome) throws SAXException,
            IOException, WdkModelException {
        // load all the wizards
        WizardParser parser = new WizardParser(gusHome);
        Wizard wizard = null;

        File dir = new File(gusHome + WIZARD_PATH);
        logger.debug("wizard-dir: " + dir.getAbsolutePath());
        File[] files = dir.listFiles();
        if (files != null) {

            for (File file : dir.listFiles()) {
                logger.debug("wizard-file: " + file.getAbsolutePath());
                String fileName = file.getName().toLowerCase();
                if (!fileName.endsWith(".xml")) continue;

                Wizard w = parser.parseWizard(file.getAbsolutePath());
                if (wizard == null) wizard = w;
                else wizard.merge(w);
            }
        }
        return wizard;
    }

    private List<StageReference> defaultStageReferenceList = new ArrayList<StageReference>();
    private String defaultStageName;
    private Stage defaultStage;

    private List<Stage> stageList = new ArrayList<Stage>();
    private Map<String, Stage> stageMap;

    /**
     * The merge has to occur before the exclusion.
     * 
     * @param wizard
     * @throws WdkModelException
     */
    void merge(Wizard wizard) throws WdkModelException {
        if (defaultStage != null || stageMap != null)
            throw new WdkModelException("Merging of wizards has to occur "
                    + "before the wizard being excluded or resolved.");

        for (StageReference reference : wizard.defaultStageReferenceList) {
            defaultStageReferenceList.add(reference);
        }
        for (Stage stage : wizard.stageList) {
            stageList.add(stage);
        }
    }

    public Stage[] getStages() {
        Stage[] stages = new Stage[stageMap.size()];
        stageMap.values().toArray(stages);
        return stages;
    }

    public void addDefaultStageReference(StageReference reference) {
        defaultStageReferenceList.add(reference);
    }

    public void addStage(Stage stage) {
        stage.setWizard(this);
        stageList.add(stage);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        stageMap = new LinkedHashMap<String, Stage>();
        for (Stage stage : stageList) {
            if (stage.include(projectId)) {
                stage.excludeResources(projectId);
                String name = stage.getName();
                if (stageMap.containsKey(name))
                    throw new WdkModelException("More than one stage '" + name
                            + "' exist in the wizard.");
                stageMap.put(name, stage);
            }
        }
        stageList = null;

        if (stageMap.size() == 0)
            throw new WdkModelException("The wizard does not contain any "
                    + "stage.");

        for (StageReference reference : defaultStageReferenceList) {
            if (reference.include(projectId)) {
                String stageName = reference.getStage();
                if (defaultStageName != null)
                    throw new WdkModelException("More than one  "
                            + "defaultStageReferences exist: ["
                            + defaultStageName + "] and [" + stageName + "]");
                reference.excludeResources(projectId);
                defaultStageName = stageName;
            }
        }
        defaultStageReferenceList = null;

        if (defaultStageName == null)
            throw new WdkModelException("Required defaultStageReference "
                    + " is not defined");

        super.excludeResources(projectId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.WdkModelBase#resolveReferences(org.gusdb.wdk.model
     * .WdkModel)
     */
    @Override
    public void resolveReferences(WdkModel wdkModel) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        for (Stage stage : stageMap.values()) {
            stage.resolveReferences(wdkModel);
        }

        defaultStage = stageMap.get(defaultStageName);
        if (defaultStage == null)
            throw new WdkModelException("defaultStageReference ["
                    + defaultStageName + "] references to an invalid stage.");

        super.resolveReferences(wdkModel);
    }

    public Stage queryStage(String stageName) throws WdkUserException {
        Stage stage = stageMap.get(stageName);
        if (stage == null)
            throw new WdkUserException("The stage name [" + stageName
                    + "] is invalid.");
        return stage;
    }

    public Stage getDefaultStage() {
        return defaultStage;
    }
}
