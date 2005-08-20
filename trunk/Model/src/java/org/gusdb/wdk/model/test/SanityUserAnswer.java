package org.gusdb.wdk.model.test;

public class SanityUserAnswer {

    protected String name;
    protected int answerID;
    protected SanityQuestion sanityQuestion;

    public SanityUserAnswer() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SanityQuestion getSanityQuestion() {
        return sanityQuestion;
    }

    public void setSanityQuestion(SanityQuestion sanityQuestion) {
        this.sanityQuestion = sanityQuestion;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("name=" + name + "; question=" + sanityQuestion.toString());
        return sb.toString();
    }
}
