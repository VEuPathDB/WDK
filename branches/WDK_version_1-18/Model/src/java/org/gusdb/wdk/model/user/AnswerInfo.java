/**
 * 
 */
package org.gusdb.wdk.model.user;

/**
 * @author xingao
 * 
 */
public class AnswerInfo {

    private int answerId;
    private String answerChecksum;
    private String projectId;
    private String projectVersion;
    private String questionName;
    private String queryChecksum;
    private String resultMessage;

    public AnswerInfo(int answerId) {
        this.answerId = answerId;
    }

    /**
     * @return the answerChecksum
     */
    public String getAnswerChecksum() {
        return answerChecksum;
    }

    /**
     * @param answerChecksum
     *            the answerChecksum to set
     */
    public void setAnswerChecksum(String answerChecksum) {
        this.answerChecksum = answerChecksum;
    }

    /**
     * @return the projectId
     */
    public String getProjectId() {
        return projectId;
    }

    /**
     * @param projectId
     *            the projectId to set
     */
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    /**
     * @return the projectVersion
     */
    public String getProjectVersion() {
        return projectVersion;
    }

    /**
     * @param projectVersion
     *            the projectVersion to set
     */
    public void setProjectVersion(String projectVersion) {
        this.projectVersion = projectVersion;
    }

    /**
     * @return the questionName
     */
    public String getQuestionName() {
        return questionName;
    }

    /**
     * @param questionName
     *            the questionName to set
     */
    public void setQuestionName(String questionName) {
        this.questionName = questionName;
    }

    /**
     * @return the queryChecksum
     */
    public String getQueryChecksum() {
        return queryChecksum;
    }

    /**
     * @param queryChecksum
     *            the queryChecksum to set
     */
    public void setQueryChecksum(String queryChecksum) {
        this.queryChecksum = queryChecksum;
    }

    /**
     * @return the answerId
     */
    public int getAnswerId() {
        return answerId;
    }

    /**
     * @return the resultMessage
     */
    public String getResultMessage() {
        return resultMessage;
    }

    /**
     * @param resultMessage
     *            the resultMessage to set
     */
    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }
}
