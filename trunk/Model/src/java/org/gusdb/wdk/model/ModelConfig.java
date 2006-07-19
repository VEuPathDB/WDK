package org.gusdb.wdk.model;

/**
 * @author
 * @modified Jan 6, 2006 - Jerric add a property for the name of query history
 *           table
 */
public class ModelConfig {

    String connectionUrl;
    String login;
    String password;
    String instanceTable;
    String platformClass;
    Integer maxQueryParams;
    Integer maxIdle;
    Integer maxWait;
    Integer maxActive;
    Integer minIdle;
    Integer initialSize;
    String webServiceUrl;

    // the fields for authentication use
    private String authenticationLogin;
    private String authenticationPassword;
    private String authenticationConnectionUrl;
    private String authenticationPlatformClass;
    private String userTable;
    private String roleTable;
    private String historyTable;
    private String preferenceTable;
    private String defaultRole;
    private String smtpServer;
    
    public ModelConfig() {}

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

    public void setQueryInstanceTable(String instanceTable) {
        this.instanceTable = instanceTable;
    }

    public String getQueryInstanceTable() {
        return instanceTable;
    }

    public void setMaxQueryParams(Integer maxQueryParams) {
        this.maxQueryParams = maxQueryParams;
    }

    public Integer getMaxQueryParams() {
        return maxQueryParams;
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
     *            The authenticationConnectionUrl to set.
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
     *            The authenticationLogin to set.
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
     *            The authenticationPassword to set.
     */
    public void setAuthenticationPassword(String authenticationPassword) {
        this.authenticationPassword = authenticationPassword;
    }

    /**
     * @return Returns the historyTable.
     */
    public String getHistoryTable() {
        return historyTable;
    }

    /**
     * @param historyTable
     *            The historyTable to set.
     */
    public void setHistoryTable(String historyTable) {
        this.historyTable = historyTable;
    }

    /**
     * @return Returns the instanceTable.
     */
    public String getInstanceTable() {
        return instanceTable;
    }

    /**
     * @param instanceTable
     *            The instanceTable to set.
     */
    public void setInstanceTable(String instanceTable) {
        this.instanceTable = instanceTable;
    }

    /**
     * @return Returns the preferenceTable.
     */
    public String getPreferenceTable() {
        return preferenceTable;
    }

    /**
     * @param preferenceTable
     *            The preferenceTable to set.
     */
    public void setPreferenceTable(String preferenceTable) {
        this.preferenceTable = preferenceTable;
    }

    /**
     * @return Returns the defaultRole.
     */
    public String getDefaultRole() {
        return defaultRole;
    }

    /**
     * @param defaultRole
     *            The defaultRole to set.
     */
    public void setDefaultRole(String defaultRole) {
        this.defaultRole = defaultRole;
    }

    /**
     * @return Returns the userTable.
     */
    public String getUserTable() {
        return userTable;
    }

    /**
     * @param userTable
     *            The userTable to set.
     */
    public void setUserTable(String userTable) {
        this.userTable = userTable;
    }

    /**
     * @return Returns the authenticationPlatformClass.
     */
    public String getAuthenticationPlatformClass() {
        return authenticationPlatformClass;
    }

    /**
     * @param authenticationPlatformClass
     *            The authenticationPlatformClass to set.
     */
    public void setAuthenticationPlatformClass(
            String authenticationPlatformClass) {
        this.authenticationPlatformClass = authenticationPlatformClass;
    }

    /**
     * @return Returns the roleTable.
     */
    public String getRoleTable() {
        return roleTable;
    }

    /**
     * @param roleTable
     *            The roleTable to set.
     */
    public void setRoleTable(String roleTable) {
        this.roleTable = roleTable;
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
}
