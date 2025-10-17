package org.gusdb.wdk.model.answer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.DefaultResultSizePlugin;
import org.gusdb.wdk.model.record.PrimaryKeyDefinition;
import org.gusdb.wdk.model.record.ResultSize;

public class ResultSizeFactory {

  private static final Logger LOG = Logger.getLogger(ResultSizeFactory.class);

  private final AnswerValue _answerValue;

  //size of total result
  private Integer _resultSize;



  private Map<String, Integer> _resultSizesByProject = null;

  public ResultSizeFactory(AnswerValue answerValue) {
    _answerValue = answerValue;
  }

  /**
   * @return number of pages needed to display entire result given the current page size
   */
  public int getPageCount() throws WdkModelException {
    int total = getResultSize();
    int pageSize = _answerValue.getEndIndex() - _answerValue.getStartIndex() + 1;
    int pageCount = (int) Math.round(Math.ceil((float) total / pageSize));
    return pageCount;
  }

  public int getResultSize() throws WdkModelException {
    QueryInstance<?> idsQueryInstance = _answerValue.getIdsQueryInstance();
    boolean isCacheable = idsQueryInstance.getQuery().isCacheable();
    if (_resultSize == null || !isCacheable) {
      _resultSize = new DefaultResultSizePlugin().getResultSize(_answerValue);
    }
    LOG.info("getting result size: cache=" + _resultSize + ", isCacheable=" + isCacheable);
    return _resultSize;
  }

  public int getDisplayResultSize() throws WdkModelException {
    ResultSize plugin = _answerValue.getQuestion().getRecordClass().getResultSizePlugin();
    LOG.debug("getting Display result size.");
    return plugin.getResultSize(_answerValue);
  }

  public Map<String, Integer> getResultSizesByProject() throws WdkModelException {
    if (_resultSizesByProject == null) {
      _resultSizesByProject = new LinkedHashMap<String, Integer>();
      Question question = _answerValue.getQuestion();
      QueryInstance<?> queryInstance = _answerValue.getIdsQueryInstance();

      // make sure the project_id is defined in the record
      PrimaryKeyDefinition primaryKey = question.getRecordClass().getPrimaryKeyDefinition();
      if (!primaryKey.hasColumn(Utilities.COLUMN_PROJECT_ID)) {
        String projectId = question.getWdkModel().getProjectId();
        // no project_id defined in the record, use the full size
        _resultSizesByProject.put(projectId, getResultSize());
      }
      else {
        // need to run the query first for portal
        Optional<String> message = queryInstance.getResultMessage();
        try (ResultList resultList = queryInstance.getResults()){
          boolean hasMessage = (message.isPresent() && message.get().length() > 0);
          if (hasMessage) {
            String[] sizes = message.get().split(",");
            for (String size : sizes) {
              String[] parts = size.split(":");
              if (parts.length > 1 && parts[1].matches("^\\d++$")) {
                _resultSizesByProject.put(parts[0], Integer.parseInt(parts[1]));
              }
              else {
                // make sure if the message is not expected, the
                // correct result size can still be retrieved
                // from
                // cached result.
                hasMessage = false;
              }
            }
          }
          // if the previous step fails, make sure the result size can
          // still be calculated from cache.
          if (!hasMessage) {
            while (resultList.next()) {
              if (!hasMessage) {
                // also count by project
                String project = resultList.get(Utilities.COLUMN_PROJECT_ID).toString();
                int subCounter = 0;
                if (_resultSizesByProject.containsKey(project))
                  subCounter = _resultSizesByProject.get(project);
                // if subContent < 0, it is an error code. don't
                // change it.
                if (subCounter >= 0)
                  _resultSizesByProject.put(project, ++subCounter);
              }
            }
          }
        }
      }
    }
    return _resultSizesByProject;
  }


  public void clear() {
    _resultSize = null;
    _resultSizesByProject = null;
  }

}
