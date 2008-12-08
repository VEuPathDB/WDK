package org.gusdb.wdk.model.user;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.json.JSONException;

public class Strategy {

    private StepFactory stepFactory;
    private User user;
    private Step latestStep;
    private int displayId;
    private int internalId;
    private boolean isSaved;
    private String name;
    private String savedName = null;

    Strategy(StepFactory factory, User user, int displayId, int internalId,
            String name) {
        this.stepFactory = factory;
        this.user = user;
        this.displayId = displayId;
        this.internalId = internalId;
        this.name = name;
        isSaved = false;
    }

    public User getUser() {
        return user;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setSavedName(String savedName) {
        this.savedName = savedName;
    }

    public String getSavedName() {
        return savedName;
    }

    public void setIsSaved(boolean saved) {
        this.isSaved = saved;
    }

    public boolean getIsSaved() {
        return isSaved;
    }

    public Step getLatestStep() {
        return latestStep;
    }

    void setDisplayId(int displayId) {
        this.displayId = displayId;
    }

    public int getDisplayId() {
        return displayId;
    }

    public int getInternalId() {
        return internalId;
    }

    void setInternalId(int internalId) {
        this.internalId = internalId;
    }

    public Step getStep(int index) {
        return latestStep.getStep(index);
    }

    public Step[] getAllSteps() {
        return latestStep.getAllSteps();
    }

    public int getLength() {
        return latestStep.getLength();
    }

    public void addStep(Step step) throws WdkUserException {
        if (latestStep != null) {
            latestStep.addStep(step);
        }
        setLatestStep(step);
    }

    public void setLatestStep(Step step) throws WdkUserException {
        this.latestStep = step;
    }

    public Step getStepById(int id) {
        return latestStep.getStepByDisplayId(id);
    }

    public void update(boolean overwrite) throws WdkUserException,
            WdkModelException, SQLException, JSONException {
        stepFactory.updateStrategy(user, this, overwrite);
    }

    public String getType() throws NoSuchAlgorithmException, WdkModelException,
            JSONException, WdkUserException, SQLException {
        return latestStep.getType();
    }
}
