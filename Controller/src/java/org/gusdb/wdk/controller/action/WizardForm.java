package org.gusdb.wdk.controller.action;


public class WizardForm extends MapActionForm {

    /**
     * 
     */
    private static final long serialVersionUID = 3608316943911485689L;

    private int strategy;
    private int step;
    private String stage;

    /**
     * @return the strategy
     */
    public int getStrategy() {
        return strategy;
    }

    /**
     * @param strategy
     *            the strategy to set
     */
    public void setStrategy(int strategy) {
        this.strategy = strategy;
    }

    /**
     * @return the step
     */
    public int getStep() {
        return step;
    }

    /**
     * @param step
     *            the step to set
     */
    public void setStep(int step) {
        this.step = step;
    }

    /**
     * @return the stage
     */
    public String getStage() {
        return stage;
    }

    /**
     * @param stage
     *            the stage to set
     */
    public void setStage(String stage) {
        this.stage = stage;
    }
}
