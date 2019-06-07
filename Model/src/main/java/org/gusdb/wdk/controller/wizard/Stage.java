package org.gusdb.wdk.controller.wizard;

import java.util.ArrayList;
import java.util.List;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;

public class Stage extends WdkModelBase {

    private Wizard _wizard;
    private String _name;
    private String _display;

    private String _handlerClass;
    private StageHandler _handler;

    private List<WdkModelText> _descriptionList = new ArrayList<WdkModelText>();
    private String _description;

    private List<Result> _resultList = new ArrayList<Result>();
    private Result _result;

    /**
     * @return the wizard
     */
    public Wizard getWizard() {
        return _wizard;
    }

    /**
     * @param wizard
     *            the wizard to set
     */
    public void setWizard(Wizard wizard) {
        _wizard = wizard;
    }

    /**
     * @return the name
     */
    public String getName() {
        return _name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        _name = name;
    }

    /**
     * @return the display
     */
    public String getDisplay() {
        return (_display == null) ? _name : _display;
    }

    /**
     * @param display
     *            the display to set
     */
    public void setDisplay(String display) {
        _display = display;
    }

    /**
     * @param handlerClass
     *            the handlerClass to set
     */
    public void setHandlerClass(String handlerClass) {
        _handlerClass = handlerClass;
    }

    /**
     * @return the handler
     */
    public StageHandler getHandler() {
        return _handler;
    }

    public void addDescription(WdkModelText description) {
        _descriptionList.add(description);
    }

    public String getDescription() {
        return _description;
    }

    public void addResult(Result result) {
        _resultList.add(result);
    }

    public Result getResult() {
        return _result;
    }

    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        // exclude the description
        for (WdkModelText desc : _descriptionList) {
            if (desc.include(projectId)) {
                if (_description != null)
                    throw new WdkModelException("There are more than one "
                            + "description defined in the stage '" + _name
                            + "' for " + projectId);
                desc.excludeResources(projectId);
                _description = desc.getText();
            }
        }
        _descriptionList = null;

        // exclude result
        for (Result result : _resultList) {
            if (result.include(projectId)) {
                if (_result != null)
                    throw new WdkModelException("Only one result is allowed in"
                            + " stage [" + _name + "]");
                result.excludeResources(projectId);
                _result = result;
            }
        }
        _resultList = null;

        if (_result == null)
            throw new WdkModelException("No result is defined in stage ["
                    + _name + "]");

        super.excludeResources(projectId);
    }

    @Override
    public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
        // resolve the handler
        if (_handlerClass != null) {
            try {
                Class<?> hClass = Class.forName(_handlerClass);
                _handler = (StageHandler) hClass.getDeclaredConstructor().newInstance();
            } catch (Exception ex) {
                throw new WdkModelException("The flow stage handler is not of type: "
                        + StageHandler.class + ". stage: " + _name, ex);
            }
        }

        // resolve the reference in the result;
        _result.resolveReferences(wdkModel);

        super.resolveReferences(wdkModel);
    }
}
