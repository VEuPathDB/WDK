package org.gusdb.wdk.model.wizard;

import org.gusdb.wdk.model.WdkModelBase;

public class StageReference extends WdkModelBase {

    private String label;
    private String stage;

    /**
     * @return the name
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return the stage
     */
    public String getStage() {
        return stage;
    }

    /**
     * @param stage the stage to set
     */
    public void setStage(String stage) {
        this.stage = stage;
    }
}
