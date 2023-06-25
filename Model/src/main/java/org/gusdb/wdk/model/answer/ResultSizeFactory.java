package org.gusdb.wdk.model.answer;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.MapBuilder;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.answer.factory.AnswerValueFactory;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.DefaultResultSizePlugin;
import org.gusdb.wdk.model.record.PrimaryKeyDefinition;
import org.gusdb.wdk.model.record.ResultSize;

public class ResultSizeFactory {

  private static final Logger LOG = Logger.getLogger(ResultSizeFactory.class);

  private static final int THREAD_POOL_SIZE = 4;
  private static final int THREAD_POOL_TIMEOUT = 5; // timeout thread pool, in minutes

  private static enum FilterSizeType { STANDARD, DISPLAY }

  private final AnswerValue _answerValue;

  //size of total result
  private Integer _resultSize;

  // Map chain from boolean isDisplaySize -> filterName -> resultSize
  private Map<FilterSizeType, Map<String, Integer>> _resultSizesByFilter =
      new MapBuilder<FilterSizeType, Map<String, Integer>>()
        .put(FilterSizeType.STANDARD, new ConcurrentHashMap<String,Integer>())
        .put(FilterSizeType.DISPLAY, new ConcurrentHashMap<String, Integer>())
        .toMap();

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
    ResultSize plugin = _answerValue.getAnswerSpec().getQuestion().getRecordClass().getResultSizePlugin();
    LOG.debug("getting Display result size.");
    return plugin.getResultSize(_answerValue);
  }

  public Map<String, Integer> getResultSizesByProject() throws WdkModelException {
    if (_resultSizesByProject == null) {
      _resultSizesByProject = new LinkedHashMap<String, Integer>();
      Question question = _answerValue.getAnswerSpec().getQuestion();
      QueryInstance<?> queryInstance = _answerValue.getIdsQueryInstance();
      Optional<AnswerFilterInstance> filter = _answerValue.getAnswerSpec().getLegacyFilter();

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
        try (ResultList resultList = (filter.isPresent() ? filter.get().getResults(_answerValue) : queryInstance.getResults())){
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


  public int getFilterDisplaySize(String filterName)
      throws WdkModelException {
    return getFilterSize(filterName, true);
  }

  public int getFilterSize(String filterName)
      throws WdkModelException {
    return getFilterSize(filterName, false);
  }

  public Map<String, Integer> getFilterDisplaySizes() {
    return getFilterSizes(null, true);
  }

  public Map<String, Integer> getFilterSizes() {
    return getFilterSizes(null, false);
  }

  public Map<String, Integer> getFilterDisplaySizes(List<String> filterNames) {
    return getFilterSizes(filterNames, true);
  }

  public Map<String, Integer> getFilterSizes(List<String> filterNames) {
    return getFilterSizes(filterNames, false);
  }

  private Map<String, Integer> getFilterSizes(Collection<String> filterNames, boolean useDisplay) {
    Question question = _answerValue.getAnswerSpec().getQuestion();
    Map<String, AnswerFilterInstance> allFilters = question.getRecordClass().getFilterMap();
    if (filterNames == null) {
      // sizes requested for all filters
      filterNames = allFilters.keySet();
    }
    else {
      // check to make sure requested names are actually filters
      LOG.debug("Filter sizes requested for: " + FormatUtil.arrayToString(filterNames.toArray()));
      for (String name : filterNames) {
        if (!allFilters.containsKey(name)) {
          throw new WdkRuntimeException("Requested filter '" + name +
              "' is not a filter instance in " + question.getRecordClassName());
        }
      }
    }

    // create a map to hold results
    ConcurrentMap<String, Integer> sizes = new ConcurrentHashMap<>(filterNames.size());

    // use a thread pool to get filter sizes in parallel
    ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    for (String filterName : filterNames) {
      executor.execute(new FilterSizeTask(this, sizes, filterName, useDisplay));
    }

    // wait for executor to finish.
    executor.shutdown();
    try {
      if (!executor.awaitTermination(THREAD_POOL_TIMEOUT, TimeUnit.MINUTES)) {
        executor.shutdownNow();
      }
    }
    catch (InterruptedException ex) {
      executor.shutdownNow();
    }

    return sizes;
  }

  private int getFilterSize(String filterName, boolean useDisplay)
      throws WdkModelException {
    FilterSizeType sizeType = useDisplay ? FilterSizeType.DISPLAY : FilterSizeType.STANDARD;
    Integer size = _resultSizesByFilter.get(sizeType).get(filterName);
    if (size != null && _answerValue.getIdsQueryInstance().getQuery().isCacheable()) {
      return size;
    }

    // create a copy of this AnswerValue, overriding current AnswerFilter with one passed in
    AnswerValue modifiedAnswer = AnswerValueFactory.makeAnswer(_answerValue,
        AnswerSpec.builder(_answerValue.getAnswerSpec())
        .setLegacyFilterName(Optional.of(filterName))
        .buildRunnable(_answerValue.getUser(), _answerValue.getAnswerSpec().getStepContainer()));
    String idSql = modifiedAnswer.getIdSql();

    // if display count requested, use custom plugin; else use default
    ResultSize countPlugin = (useDisplay ?
        _answerValue.getAnswerSpec().getQuestion().getRecordClass().getResultSizePlugin() :
        new DefaultResultSizePlugin());

    // get size, cache, and return
    size = countPlugin.getResultSize(modifiedAnswer, idSql);
    _resultSizesByFilter.get(sizeType).put(filterName, size);
    return size;
  }

  public void clear() {
    _resultSize = null;
    _resultSizesByFilter.get(FilterSizeType.STANDARD).clear();
    _resultSizesByFilter.get(FilterSizeType.DISPLAY).clear();
    _resultSizesByProject = null;
  }

}
