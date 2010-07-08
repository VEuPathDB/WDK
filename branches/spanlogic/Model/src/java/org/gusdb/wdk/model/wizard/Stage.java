package org.gusdb.wdk.model.wizard;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.WdkUserException;
import org.json.JSONException;

public class Stage extends WdkModelBase {

    private String name;
    private String display;
    private String view;

    private String handler;
    private StageHandler stageHandler;

    private List<WdkModelText> descriptionList = new ArrayList<WdkModelText>();
    private String description;

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
        return (display == null) ? name : display;
    }

    /**
     * @param display
     *            the display to set
     */
    public void setDisplay(String display) {
        this.display = display;
    }

    /**
     * @return the view
     */
    public String getView() {
        return view;
    }

    /**
     * @param view
     *            the view to set
     */
    public void setView(String view) {
        this.view = view;
    }

    /**
     * @param handlerClass
     *            the handlerClass to set
     */
    public void setHandler(String handler) {
        this.handler = handler;
    }

    /**
     * @return the handler
     */
    public StageHandler getHandler() {
        return stageHandler;
    }

    public void addDescription(WdkModelText description) {
        this.descriptionList.add(description);
    }

    public String getDescription() {
        return this.description;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        // exclude the description
        for (WdkModelText desc : descriptionList) {
            if (desc.include(projectId)) {
                if (this.description != null)
                    throw new WdkModelException("There are more than one "
                            + "description defined in the stage '" + this.name
                            + "' for " + projectId);
                desc.excludeResources(projectId);
                this.description = desc.getText();
            }
        }
        this.descriptionList = null;

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
        // resolve the handler
        if (handler != null) {
            try {
                Class<?> handlerClass = Class.forName(handler);
                this.stageHandler = (StageHandler) handlerClass.newInstance();
            } catch (Exception ex) {
                new WdkModelException("The flow stage handler is not of type: "
                        + StageHandler.class + ". stage: " + name, ex);
            }
        }

        super.resolveReferences(wdkModel);
    }
}
