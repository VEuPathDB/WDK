package org.gusdb.wdk.model.test;

import org.gusdb.wdk.model.user.UserFactory;

public class PasswordHelper {

	private static final String passwordToConvert = "staff1";
	
	public static void main(String[] args) {
		System.out.println(UserFactory.encrypt(passwordToConvert));
	}
}
