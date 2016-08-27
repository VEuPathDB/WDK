package org.gusdb.wdk.model.fix;

import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.fix.StepParamFilterModifier.StepParamFilterModifierPlugin;

/**
 * Performs operations on steps required for EuPathDB GUS4/alt-splice release
 * 
 * 1. Add matched transcript filter to all leaf transcript steps
 * 2. Add boolean filter to all boolean transcript steps
 * 3. Change filters: {} to [] when found
 * 4. Remove use_boolean_filter param when found
 * 5. For parameters that are filterParams: convert value "Unknown" to null
 * 
 * @author rdoherty
 *
 */
public class Gus4ReleaseStepMigrationPlugin implements StepParamFilterModifierPlugin {

  @Override
  public TwoTuple<Boolean, StepData> processStep(StepData step, WdkModel wdkModel) {
    return new TwoTuple<Boolean, StepData>(false, step);
  }

}
