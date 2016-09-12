package org.gusdb.wdk.model.fix.table.steps;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.FormatUtil.Style;
import org.gusdb.wdk.model.fix.table.TableRowInterfaces.RowResult;

public class StepQuestionUpdater {

  private static final Logger LOG = Logger.getLogger(StepQuestionUpdater.class);

  private final Map<String,String> _mapping;

  public StepQuestionUpdater(String filename, boolean dumpLoadedFile) throws IOException {
    LOG.info("Loading new question mapping from file: " + filename);
    Properties mapping = new Properties();
    mapping.load(new FileInputStream(filename));
    LOG.info("Loaded " + mapping.size() + " mappings.");
    _mapping = new HashMap<String,String>();
    for (Entry<Object,Object> entry : mapping.entrySet()) {
      _mapping.put((String)entry.getKey(), (String)entry.getValue());
    }
    if (dumpLoadedFile) {
      LOG.info("Question name mapping loaded from file: <" + filename + ">" + NL +
          FormatUtil.prettyPrint(_mapping, Style.MULTI_LINE));
    }
  }

  public boolean updateQuestionName(RowResult<StepData> result) {
    String questionName  = result.getRow().getQuestionName();
    if (_mapping.containsKey(questionName)) {
      result.getRow().setQuestionName(_mapping.get(questionName));
      result.setShouldWrite(true);
      return true;
    }
    return false;
  }

}
