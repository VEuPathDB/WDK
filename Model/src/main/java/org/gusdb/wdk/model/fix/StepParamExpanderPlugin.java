package org.gusdb.wdk.model.fix;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Reducer;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.fix.table.TableRowInterfaces.RowResult;
import org.gusdb.wdk.model.fix.table.TableRowInterfaces.TableRowUpdaterPlugin;
import org.gusdb.wdk.model.fix.table.TableRowInterfaces.TableRowWriter;
import org.gusdb.wdk.model.fix.table.TableRowUpdater;
import org.gusdb.wdk.model.fix.table.steps.StepData;
import org.gusdb.wdk.model.fix.table.steps.StepDataFactory;

public class StepParamExpanderPlugin implements TableRowUpdaterPlugin<StepData> {

  private static final Logger LOG = Logger.getLogger(StepParamExpanderPlugin.class);
  
  public static class ParamExpanderLoader extends StepDataFactory {

    public ParamExpanderLoader() {
      super(true);
    }

    @Override
    public String getRecordsSql(String schema, String projectId) {
      // ignore projectId for now; must select cols from StepDataFactory so we can properly load StepData objs
      String sql = StepParamExpander.getSelectForColumns(schema, projectId, SELECT_COLS_TEXT);
      LOG.info("Returning the following SQL: " + sql);
      return sql;
    }
  }

  public static class StepWithParams extends StepData {

    public Map<String, Set<String>> params;
    public int valueCount;

    public StepWithParams(StepData base) {
      super(base);
      params = StepParamExpander.parseDisplayParams(getStepId().intValue(), getParamFilters());
      valueCount = Functions.reduce(params.values().iterator(), new Reducer<Set<String>, Integer>() {
        @Override
        public Integer reduce(Set<String> obj, Integer incomingValue) {
          return incomingValue + obj.size();
        }
      }, 0);
    }
  }

  private static class ParamValueWriter implements TableRowWriter<StepData> {

    @Override
    public void setUp(WdkModel wdkModel) throws Exception {
      StepParamExpander.createParamTable(wdkModel);
    }

    @Override
    public void tearDown(WdkModel wdkModel) throws Exception {
      // nothing to do here
    }

    @Override
    public String getWriteSql(String schema) {
      return StepParamExpander.getInsertSql();
    }

    @Override
    public Integer[] getParameterTypes() {
      return new Integer[]{ Types.INTEGER, Types.VARCHAR, Types.VARCHAR };
    }

    @Override
    public Collection<Object[]> toValues(StepData obj) {
      StepWithParams stepWithParams = (StepWithParams)obj;
      Long stepId = obj.getStepId();
      List<Object[]> values = new ArrayList<>();
      for (Entry<String, Set<String>> entry : stepWithParams.params.entrySet()) {
        String paramName = entry.getKey();
        for (String paramValue : entry.getValue()) {
          values.add(new Object[]{ stepId, paramName, StepParamExpander.truncateTerm(paramValue) });
        }
      }
      return values;
    }
  }

  private AtomicInteger _numParams = new AtomicInteger(0);

  @Override
  public void configure(WdkModel wdkModel, List<String> additionalArgs) throws Exception {
    // no configuration needed
  }

  @Override
  public TableRowUpdater<StepData> getTableRowUpdater(WdkModel wdkModel) {
    return new TableRowUpdater<StepData>(new ParamExpanderLoader(), new ParamValueWriter(), this, wdkModel);
  }

  @Override
  public RowResult<StepData> processRecord(StepData nextRow) throws Exception {
    StepWithParams replacement = new StepWithParams(nextRow);
    _numParams.addAndGet(replacement.valueCount);
    return new RowResult<StepData>(replacement).setShouldWrite(!replacement.params.isEmpty());
  }

  @Override
  public void dumpStatistics() {
    LOG.info("Wrote " + _numParams.get() + " total param rows to the step_params table.");
  }

}
