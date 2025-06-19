package org.gusdb.wdk.model.fix.table.steps;

import static org.gusdb.fgputil.FormatUtil.join;
import static org.gusdb.fgputil.functional.FunctionalInterfaces.equalTo;
import static org.gusdb.fgputil.functional.FunctionalInterfaces.negate;
import static org.gusdb.fgputil.functional.Functions.filter;
import static org.gusdb.fgputil.functional.Functions.mapToList;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.gusdb.fgputil.ListBuilder;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.fix.table.TableRowInterfaces.TableRowWriter;

public class StepDataWriter implements TableRowWriter<StepData> {

  // imported constants
  protected static final String STEP_ID = StepDataFactory.STEP_ID;
  protected static final String[] COLS = StepDataFactory.COLS;
  protected static final Map<String, Integer> SQLTYPES = StepDataFactory.SQLTYPES;
  
  private static final List<String> UPDATE_COLS = filter(Arrays.asList(COLS), negate(equalTo(STEP_ID)));

  private static final String UPDATE_COLS_TEXT = join(mapToList(UPDATE_COLS, col -> col + " = ?").toArray(), ", ");

  private static final Integer[] UPDATE_PARAMETER_TYPES =
      mapToList(new ListBuilder<String>().addAll(UPDATE_COLS).add(STEP_ID).toList(),
          key -> SQLTYPES.get(key)).toArray(new Integer[COLS.length]);

  @Override
  public List<String> getTableNamesForBackup(String schema) {
    return List.of(schema + "steps");
  }

  @Override
  public String getWriteSql(String schema) {
    return "update " + schema + "steps set " + UPDATE_COLS_TEXT + " where " + STEP_ID + " = ?";
  }

  @Override
  public Integer[] getParameterTypes() {
    return UPDATE_PARAMETER_TYPES;
  }

  @Override
  public Collection<Object[]> toValues(StepData row) {
    return ListBuilder.asList(new Object[] {
        row.getLeftChildId(),
        row.getRightChildId(),
        row.getProjectId(),
        row.getQuestionName(),
        row.getParamFilters().toString(),
        row.getStepId()
    });
  }

  @Override
  public void setUp(WdkModel wdkModel) throws Exception {
    // nothing to do here
  }

  @Override
  public void tearDown(WdkModel wdkModel) throws Exception {
    // nothing to do here
  }

}
