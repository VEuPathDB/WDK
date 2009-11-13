/**
 * 
 */
package org.gusdb.wdk.model.user;

/**
 * @author xingao
 * 
 */
public class Answer {

    private int answerId;
    private String answerChecksum;
    private String projectId;
    private String projectVersion;
    private String questionName;
    private String queryChecksum;

    Answer(int answerId) {
        this.answerId = answerId;
    }
    
    Answer(String projectId, String answerChecksum) {
        this.projectId = projectId.intern();
        this.answerChecksum = answerChecksum.intern();
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
        this.answerChecksum = answerChecksum.intern();
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
        this.projectId = projectId.intern();
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
        this.projectVersion = projectVersion.intern();
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
        this.questionName = questionName.intern();
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
        this.queryChecksum = queryChecksum.intern();
    }

    /**
     * @return the answerId
     */
    public int getAnswerId() {
        return answerId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return projectId.hashCode() ^ answerChecksum.hashCode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Answer) {
            Answer answer = (Answer) obj;
            return answer.projectId.equals(projectId)
                    && answer.answerChecksum.equals(answerChecksum);
        } else return false;
    }
}
