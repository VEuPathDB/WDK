package org.gusdb.wdk.controller;

public class OpenIdUser {

	private String _openId;
	private String _name;
	private String _email;
	
	public OpenIdUser(String openId) {
		_openId = openId;
	}
	
	public String getOpenId() {
		return _openId;
	}
    public void setOpenId(String openId) {
		_openId = openId;
	}
    
	public String getName() {
		return _name;
	}
	public void setName(String name) {
		_name = name;
	}
	
	public String getEmail() {
		return _email;
	}
	public void setEmail(String email) {
		_email = email;
	}
}
