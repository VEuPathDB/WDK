package org.gusdb.wdk.model.migrate;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.gusdb.wdk.model.WdkModel;

/**
 * @author jerric
 *
 *  1. add columns to steps table: project_id, project_version, question_name, result_message
 *  2. UPDATE those columns
 *  3. drop steps_fk02 constraint
 *  4. drop INDEX steps_idx01 through steps_idx08
 *  5. drop steps.answer_id (or drop not null constraint)
 *  6. CREATE indexes.
 *  
 */
public class Migrator_b18_b19 implements Migrator {

  public Migrator_b18_b19() {
    // TODO Auto-generated constructor stub
  }

  @Override
  public Options declareOptions() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void migrate(WdkModel wdkModel, CommandLine commandLine) {
    // TODO Auto-generated method stub
    
  }

}
