package org.gusdb.wdk.controller.wizard;

import java.util.ArrayList;
import java.util.List;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;

public class Stage extends WdkModelBase {

    private Wizard wizard;
    private String name;
    private String display;

    private String handlerClass;
    private StageHandler handler;

    private List<WdkModelText> descriptionList = new ArrayList<WdkModelText>();
    private String description;

    private List<Result> resultList = new ArrayList<Result>();
    private Result result;

    /**
     * @return the wizard
     */
    public Wizard getWizard() {
        return wizard;
    }

    /**
     * @param wizard
     *            the wizard to set
     */
    public void setWizard(Wizard wizard) {
        this.wizard = wizard;
    }

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
     * @param handlerClass
     *            the handlerClass to set
     */
    public void setHandlerClass(String handlerClass) {
        this.handlerClass = handlerClass;
    }

    /**
     * @return the handler
     */
    public StageHandler getHandler() {
        return handler;
    }

    public void addDescription(WdkModelText description) {
        this.descriptionList.add(description);
    }

    public String getDescription() {
        return this.description;
    }

    public void addResult(Result result) {
        this.resultList.add(result);
    }

    public Result getResult() {
        return result;
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

        // exclude result
        for (Result result : resultList) {
            if (result.include(projectId)) {
                if (this.result != null)
                    throw new WdkModelException("Only one result is allowed in"
                            + " stage [" + name + "]");
                result.excludeResources(projectId);
                this.result = result;
            }
        }
        this.resultList = null;

        if (result == null)
            throw new WdkModelException("No result is defined in stage ["
                    + name + "]");

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
    public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
        // resolve the handler
        if (handlerClass != null) {
            try {
                Class<?> hClass = Class.forName(handlerClass);
                this.handler = (StageHandler) hClass.newInstance();
            } catch (Exception ex) {
                new WdkModelException("The flow stage handler is not of type: "
                        + StageHandler.class + ". stage: " + name, ex);
            }
        }

        // resolve the reference in the result;
        result.resolveReferences(wdkModel);

        super.resolveReferences(wdkModel);
    }
}
