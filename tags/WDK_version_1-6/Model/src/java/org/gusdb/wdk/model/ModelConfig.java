package org.gusdb.wdk.model;

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
    
    public Integer getInitialSize(){
	return initialSize;
    }
    public void setInitialSize(Integer initialSize){
	this.initialSize = initialSize;
    }

    public Integer getMinIdle(){
	return minIdle;
    }
    public void setMinIdle(Integer minIdle){
	this.minIdle = minIdle;
    }

    public Integer getMaxIdle(){
	return maxIdle;
    }
    public void setMaxIdle(Integer maxIdle){
	this.maxIdle = maxIdle;
    }

    public Integer getMaxWait(){
     return maxWait;
    }
    public void setMaxWait(Integer maxWait){
	this.maxWait = maxWait;
    }

    public Integer getMaxActive(){
	return maxActive;
    }
    public void setMaxActive(Integer maxActive){
	this.maxActive = maxActive;
    }
}
