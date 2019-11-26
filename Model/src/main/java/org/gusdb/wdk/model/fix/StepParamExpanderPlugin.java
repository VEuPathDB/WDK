package org.gusdb.wdk.model.fix;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.fix.table.TableRowInterfaces.RowResult;
import org.gusdb.wdk.model.fix.table.TableRowInterfaces.TableRowUpdaterPlugin;
import org.gusdb.wdk.model.fix.table.TableRowInterfaces.TableRowWriter;
import org.gusdb.wdk.model.fix.table.TableRowUpdater;
import org.gusdb.wdk.model.fix.table.steps.StepData;
import org.gusdb.wdk.model.fix.table.steps.StepDataFactory;

@Deprecated
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

    private static final int MAX_PARAM_VALUE_LENGTH = 4000;

    public Map<String, Set<String>> params = new HashMap<>();
    public int valueCount;

    public StepWithParams(StepData base) {
      super(base);
      Map<String, Set<String>> fullParams = StepParamExpander.parseDisplayParams(getStepId().intValue(), getParamFilters());
      // truncate param values that are too big- most likely BLAST sequence values
      valueCount = 0;
      for (String paramName : fullParams.keySet()) {
        Set<String> newValues = new HashSet<>();
        for (String paramValue : fullParams.get(paramName)) {
          int dbSize = FormatUtil.getUtf8EncodedBytes(paramValue).length;
          if (dbSize > MAX_PARAM_VALUE_LENGTH) {
            LOG.warn("Truncating value for parameter '" + paramName + "' (size=" + dbSize +
                ") that exceeds " + MAX_PARAM_VALUE_LENGTH + " bytes: " + paramValue);
            paramValue = FormatUtil.shrinkUtf8String(paramValue, MAX_PARAM_VALUE_LENGTH);
          }
          newValues.add(paramValue);
          valueCount++;
        }
        if (!newValues.isEmpty()) {
          params.put(paramName, newValues);
        }
      }
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
    LOG.info("Wrote " + _numParams.get() + " total param rows to the " + StepParamExpander.STEP_PARAMS_TABLE + " table.");
  }

}
