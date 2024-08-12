package org.gusdb.wdk.model.config;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.fgputil.IoUtil;

public class SecretKeyReader {

  public static void main(String[] args) {
    if (args.length != 1) {
      System.err.println("USAGE: readSecretKey <path-to-secret-key-file>");
      System.exit(1);
    }
    Path path = Paths.get(args[0]);
    try {
      System.out.print(readSecretKey(Optional.of(path)));
    }
    catch (Exception e) {
      e.printStackTrace(System.err);
      System.exit(2);
    }
  }

  public static String readSecretKey(Optional<Path> secretKeyFile) throws IOException {
    try (FileReader in = new FileReader(secretKeyFile.get().toFile())) {
      return EncryptionUtil.md5(IoUtil.readAllChars(in).strip());
    }
  }

}
