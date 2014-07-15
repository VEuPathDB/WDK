package org.gusdb.wdk.model.migrate;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.json.JSONException;

public interface Migrator {

  void declareOptions(Options options);

  void migrate(WdkModel wdkModel, CommandLine commandLine)
      throws WdkModelException, WdkUserException, NoSuchAlgorithmException,
      SQLException, JSONException;
}
