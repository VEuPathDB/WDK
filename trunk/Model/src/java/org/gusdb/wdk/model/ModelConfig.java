package org.gusdb.gus.wdk.model;

public class ModelConfig {
    
    String connectionUrl;
    String login;
    String password;
    String instanceTable;
    String platformClass;
    Integer maxQueryParams;

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
    
}
