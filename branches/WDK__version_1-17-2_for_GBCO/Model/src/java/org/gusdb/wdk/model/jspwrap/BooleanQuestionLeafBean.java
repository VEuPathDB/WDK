package org.gusdb.wdk.model.jspwrap;

import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.BooleanQuestionNode;

public class BooleanQuestionLeafBean {

    private static Logger logger = Logger.getLogger(BooleanQuestionLeafBean.class);

    BooleanQuestionNode bqn;
    BooleanQuestionNodeBean parent;

    int leafId;

    public BooleanQuestionLeafBean(BooleanQuestionNode bqn,
            BooleanQuestionNodeBean parent) {
        this.bqn = bqn;
        this.parent = parent;
    }

    public QuestionBean getQuestion() {
        // TEST
        logger.debug("Question Name: " + bqn.getType());

        return new QuestionBean(bqn.getQuestion());
    }

    public Integer getLeafId() {
        return new Integer(leafId);
    }

    public void setLeafId(int id) {
        this.leafId = id;
    }

    public BooleanQuestionNodeBean getParent() {
        return parent;
    }

    public void setValues(Map<String, Object> values) {
        bqn.setValues(values);
    }

    protected void setParent(BooleanQuestionNodeBean parent) {
        this.parent = parent;
    }

    public String toString() {
        return getQuestion().getFullName() + " (id: " + getLeafId() + ")";
    }
}
