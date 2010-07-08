package org.gusdb.wdk.model.wizard;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.WdkUserException;
import org.json.JSONException;

public class Wizard extends WdkModelBase {

    private String name;
    private String display;

    private List<WdkModelText> descriptionList = new ArrayList<WdkModelText>();
    private String description;

    private List<Stage> stageList = new ArrayList<Stage>();
    private Map<String, Stage> stageMap;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the display
     */
    public String getDisplay() {
        return display;
    }

    /**
     * @param display
     *            the display to set
     */
    public void setDisplay(String display) {
        this.display = display;
    }

    public void addDescription(WdkModelText description) {
        this.descriptionList.add(description);
    }

    public String getDescription() {
        return this.description;
    }

    public void addStage(Stage stage) {
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
                            + "' exist in wizard " + this.name);
                stageMap.put(name, stage);
            }
        }
        stageList = null;
        
        for (WdkModelText desc : descriptionList) {
            if (desc.include(projectId)) {
                if (this.description != null)
                    throw new WdkModelException("More than one desc");
                desc.excludeResources(projectId);
                this.description = desc.getText();
            }
        }
        
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

        super.resolveReferences(wdkModel);
    }

    public Stage getStage(String stageName) {
        return stageMap.get(stageName);
    }

    public Stage getFirstStage() {
        return stageMap.values().iterator().next();
    }
}
