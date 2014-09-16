package org.gusdb.wdk.model.test.sanity.tests;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.query.param.ParamValuesSet;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.test.sanity.SanityTester.ElementTest;
import org.gusdb.wdk.model.test.sanity.SanityTester.Statistics;
import org.gusdb.wdk.model.test.sanity.TestResult;
import org.gusdb.wdk.model.user.User;

public class RecordClassTest implements ElementTest {

  private final User _user;
  private final RecordClass _recordClass;
  private final ParamValuesSet _paramValuesSet;

  public RecordClassTest(User user, RecordClass recordClass) {
    _user = user;
    _recordClass = recordClass;
    _paramValuesSet = recordClass.getParamValuesSet();
  }

  @Override
  public String getTestName() {
    return "RECORD " + _recordClass.getFullName();
  }

  @Override
  public String getCommand() {
    return "wdkRecord -model " + _recordClass.getWdkModel().getProjectId() +
        " -record " + _recordClass.getFullName() +
        " -primaryKey " + _paramValuesSet.getCmdLineString();
  }

  @Override
  public TestResult test(Statistics stats) throws Exception {
    TestResult result = new TestResult(this);
    result.setExpected("Expect to create RecordInstance");
    try {
      Map<String, String> paramValues = _paramValuesSet.getParamValues();
      Map<String, Object> pkValues = new LinkedHashMap<String, Object>();
      for (String key : paramValues.keySet()) {
        pkValues.put(key, paramValues.get(key));
      }
      RecordInstance recordInstance = new RecordInstance(_user, _recordClass, pkValues);
      recordInstance.print();
      result.setReturned("Created RecordInstance");
      result.setPassed(true);
      return result;
    }
    finally {
      result.stopTimer();
      if (result.isPassed()) {
        stats.recordsPassed++;
      }
      else {
        stats.recordsFailed++;
      }
    }
  }
}
