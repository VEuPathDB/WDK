package org.gusdb.wdk.model;

/**
 * @author
 * @modified Jan 6, 2006 - Jerric add a property for the name of query history
 * table
 */
public class ModelConfig {

    private String projectId;
    private String connectionUrl;
    private String login;
    private String password;
    private String platformClass;
    private Integer maxIdle;
    private Integer maxWait;
    private Integer maxActive;
    private Integer minIdle;
    private Integer initialSize;
    private String webServiceUrl;

    // the fields for authentication use
    private String authenticationLogin;
    private String authenticationPassword;
    private String authenticationConnectionUrl;
    private String authenticationPlatformClass;

    private String loginSchema;
    private String defaultRole;
    private String smtpServer;

    // the information for registration email
    private String registerEmail;
    private String emailSubject;
    private String emailContent;

    // query logger property
    private boolean enableQueryLogger = false;
    private String queryLoggerFile;

    /**
     * @return the projectId
     */
    public String getProjectId() {
        return this.projectId;
    }

    /**
     * @param projectId
     * the projectId to set
     */
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public void setPlatformClass(String platformClass) {
        this.platformClass = platformClass;
    }

    public String getPlatformClass() {
        return platformClass;
    }

    public void setConnectionUrl(String connectionUrl) {
        this.connectionUrl = connectionUrl;
    }

    public String getConnectionUrl() {
        return connectionUrl;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public Integer getInitialSize() {
        return initialSize;
    }

    public void setInitialSize(Integer initialSize) {
        this.initialSize = initialSize;
    }

    public Integer getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(Integer minIdle) {
        this.minIdle = minIdle;
    }

    public Integer getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(Integer maxIdle) {
        this.maxIdle = maxIdle;
    }

    public Integer getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(Integer maxWait) {
        this.maxWait = maxWait;
    }

    public Integer getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(Integer maxActive) {
        this.maxActive = maxActive;
    }

    public String getWebServiceUrl() {
        return webServiceUrl;
    }

    public void setWebServiceUrl(String urlString) {
        webServiceUrl = urlString;
    }

    /**
     * @return Returns the authenticationConnectionUrl.
     */
    public String getAuthenticationConnectionUrl() {
        return authenticationConnectionUrl;
    }

    /**
     * @param authenticationConnectionUrl
     * The authenticationConnectionUrl to set.
     */
    public void setAuthenticationConnectionUrl(
            String authenticationConnectionUrl) {
        this.authenticationConnectionUrl = authenticationConnectionUrl;
    }

    /**
     * @return Returns the authenticationLogin.
     */
    public String getAuthenticationLogin() {
        return authenticationLogin;
    }

    /**
     * @param authenticationLogin
     * The authenticationLogin to set.
     */
    public void setAuthenticationLogin(String authenticationLogin) {
        this.authenticationLogin = authenticationLogin;
    }

    /**
     * @return Returns the authenticationPassword.
     */
    public String getAuthenticationPassword() {
        return authenticationPassword;
    }

    /**
     * @param authenticationPassword
     * The authenticationPassword to set.
     */
    public void setAuthenticationPassword(String authenticationPassword) {
        this.authenticationPassword = authenticationPassword;
    }

    /**
     * @return Returns the loginSchema.
     */
    public String getLoginSchema() {
        return loginSchema;
    }

    /**
     * @param loginSchema
     * The loginSchema to set.
     */
    public void setLoginSchema(String loginSchema) {
        this.loginSchema = loginSchema;
    }

    /**
     * @return Returns the defaultRole.
     */
    public String getDefaultRole() {
        return defaultRole;
    }

    /**
     * @param defaultRole
     * The defaultRole to set.
     */
    public void setDefaultRole(String defaultRole) {
        this.defaultRole = defaultRole;
    }

    /**
     * @return Returns the authenticationPlatformClass.
     */
    public String getAuthenticationPlatformClass() {
        return authenticationPlatformClass;
    }

    /**
     * @param authenticationPlatformClass
     * The authenticationPlatformClass to set.
     */
    public void setAuthenticationPlatformClass(
            String authenticationPlatformClass) {
        this.authenticationPlatformClass = authenticationPlatformClass;
    }

    /**
     * @return Returns the smtpServer.
     */
    public String getSmtpServer() {
        return smtpServer;
    }

    /**
     * @param smtpServer
     * The smtpServer to set.
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
     * The emailContent to set.
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
     * The emailSubject to set.
     */
    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }

    /**
     * @return Returns the registerEmail.
     */
    public String getRegisterEmail() {
        return registerEmail;
    }

    /**
     * @param registerEmail
     * The registerEmail to set.
     */
    public void setRegisterEmail(String registerEmail) {
        this.registerEmail = registerEmail;
    }

    /**
     * @return the enableQueryLogger
     */
    public boolean isEnableQueryLogger() {
        return enableQueryLogger;
    }

    /**
     * @param enableQueryLogger
     * the enableQueryLogger to set
     */
    public void setEnableQueryLogger(boolean enableQueryLogger) {
        this.enableQueryLogger = enableQueryLogger;
    }

    /**
     * @return the queryLoggerFile
     */
    public String getQueryLoggerFile() {
        return queryLoggerFile;
    }

    /**
     * @param queryLoggerFile
     * the queryLoggerFile to set
     */
    public void setQueryLoggerFile(String queryLoggerFile) {
        this.queryLoggerFile = queryLoggerFile;
    }

}
