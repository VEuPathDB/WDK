package org.gusdb.wdk.model.user;

import org.gusdb.fgputil.EncryptionUtil;

public class PasswordEncrypter {

  public static void main(String[] args) {
    if (args.length != 1) {
      System.err.println("USAGE: fgpJava " + PasswordEncrypter.class.getName() + " <plain_password>");
      System.exit(1);
    }
    System.out.println(EncryptionUtil.encryptPassword(args[0]));
  }
}
