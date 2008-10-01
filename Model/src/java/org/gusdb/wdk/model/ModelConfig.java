package org.gusdb.wdk.model;

/**
 * @author
 * @modified Jan 6, 2006 - Jerric add a property for the name of query history
 *           table
 */
public class ModelConfig {

    private String modelName;
    private String webServiceUrl;

    private String defaultRole = "wdk_user";

    // the information for registration email
    private String smtpServer;
    private String supportEmail;

    private String emailSubject;
    private String emailContent;

    private ModelConfigUserDB userDB;
    private ModelConfigApplicationDB appDB;

    /**
     * The projectId is not part of the config file content, it is input by the
     * user
     */
    private String projectId;
    private String gusHome;

    /**
     * @return the projectId
     */
    public String getProjectId() {
        return this.projectId;
    }

    /**
     * @param projectId
     *            the projectId to set
     */
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    /**
     * @return the gusHome
     */
    public String getGusHome() {
        return this.gusHome;
    }

    /**
     * @param gusHome
     *            the gusHome to set
     */
    public void setGusHome(String gusHome) {
        this.gusHome = gusHome;
    }

    /**
     * @return the modelName
     */
    public String getModelName() {
        return this.modelName;
    }

    /**
     * @param modelName
     *            the modelName to set
     */
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getWebServiceUrl() {
        return webServiceUrl;
    }

    public void setWebServiceUrl(String urlString) {
        webServiceUrl = urlString;
    }

    /**
     * @return Returns the smtpServer.
     */
    public String getSmtpServer() {
        return smtpServer;
    }

    /**
     * @param smtpServer
     *            The smtpServer to set.
     */
    public void setSmtpServer(String smtpServer) {
        this.smtpServer = smtpServer;
    }

    /**
     * @return Returns the emailContent.
     */
    public String getEmailContent() {
        return emailContent;
    }

    /**
     * @param emailContent
     *            The emailContent to set.
     */
    public void setEmailContent(String emailContent) {
        this.emailContent = emailContent;
    }

    /**
     * @return Returns the emailSubject.
     */
    public String getEmailSubject() {
        return emailSubject;
    }

    /**
     * @param emailSubject
     *            The emailSubject to set.
     */
    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }

    public String getSupportEmail() {
        return supportEmail;
    }

    public void setSupportEmail(String supportEmail) {
        this.supportEmail = supportEmail;
    }

    /**
     * @return the userDB
     */
    public ModelConfigUserDB getUserDB() {
        return userDB;
    }

    /**
     * @param userDB
     *            the userDB to set
     */
    public void setUserDB(ModelConfigUserDB userDB) {
        this.userDB = userDB;
    }

    /**
     * @return the appDB
     */
    public ModelConfigApplicationDB getApplicationDB() {
        return appDB;
    }

    /**
     * @param appDB
     *            the appDB to set
     */
    public void setApplicationDB(ModelConfigApplicationDB appDB) {
        this.appDB = appDB;
    }

    /**
     * @return the defaultRole
     */
    public String getDefaultRole() {
        return defaultRole;
    }

    /**
     * @param defaultRole
     *            the defaultRole to set
     */
    public void setDefaultRole(String defaultRole) {
        this.defaultRole = defaultRole;
    }
}
