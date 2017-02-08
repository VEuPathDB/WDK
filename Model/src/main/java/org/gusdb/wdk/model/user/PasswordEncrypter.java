package org.gusdb.wdk.model.user;

public class PasswordEncrypter {

  public static void main(String[] args) {
    if (args.length != 1) {
      System.err.println("USAGE: fgpJava " + PasswordEncrypter.class.getName() + " <plain_password>");
      System.exit(1);
    }
    System.out.println(UserFactory.encrypt(args[0]));
  }
}
