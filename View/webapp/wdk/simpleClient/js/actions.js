
//**************************************************
// Dispatcher
//**************************************************

// use the raw Flux dispatcher
var Dispatcher = Flux.Dispatcher;

// types of actions sent through the dispatcher
var ActionType = {
  CHANGE_QUESTION_ACTION: "changeQuestionAction",
  CHANGE_PARAM_ACTION:    "changeParamAction",
  CHANGE_PAGING_ACTION:   "changePagingAction",
  CHANGE_RESULTS_ACTION:  "changeResultsAction"
};

//**************************************************
// Helper functions
//**************************************************

var Util = (function() {

  // public methods
  var exports = {
    getAnswerRequestJson: getAnswerRequestJson
  };

  function getAnswerRequestJson(questionName, params, pagination) {
    var paramPack = params.map(function(param) {
      return { name: param.name, value: param.value }; });
    var offset = (pagination.pageNum - 1) * pagination.pageSize;
    var numRecords = pagination.pageSize;
    if (offset < 0) offset = 0;
    if (numRecords < 1) numRecords = 10;
    return {
      questionDefinition: {
        questionName: questionName,
        params: paramPack,
        filters: []
      },
      displayInfo: {
        pagination: { offset: offset, numRecords: numRecords },
        columns: null,
        sorting: null
      }
    };
  }

  return exports;

})();

//**************************************************
// Action-Creator functions interact with the server
//**************************************************

var ActionCreator = function(serviceUrl, dispatcher) {

  // public methods
  var exports = {
    setQuestion: setQuestion,
    setParamValue: setParamValue,
    setPaging: setPaging,
    loadResults: loadResults
  };

  // private data
  var _serviceUrl = serviceUrl;
  var _dispatcher = dispatcher;

  function setQuestion(questionName) {
    jQuery.ajax({
      type: "GET",
      url: _serviceUrl + "/question/" + questionName + "/params",
      success: function(data, textStatus, jqXHR) {
        _dispatcher.dispatch({
          actionType: ActionType.CHANGE_QUESTION_ACTION,
          data: {
            questionName: questionName,
            params: data
          }
        });
      },
      error: function(jqXHR, textStatus, errorThrown ) {
        alert("Error: Unable to load params for question " + questionName);
      }
    });
  }

  function setParamValue(paramName, value) {
    _dispatcher.dispatch({
      actionType: ActionType.CHANGE_PARAM_ACTION,
      data: { paramName: paramName, value: value }
    });
  }

  function setPagination(newPagination) {
    _dispatcher.dispatch({
      actionType: ActionType.CHANGE_PAGING_ACTION,
      data: newPagination
    });
  }

  function loadResults(data) {
    jQuery.ajax({
      type: "POST",
      url: _serviceUrl + "/answer",
      contentType: 'application/json; charset=UTF-8',
      data: Util.getAnswerRequestJson(data.selectedQuestion, data.params, data.pagination),
      dataType: "json",
      success: function(data, textStatus, jqXHR) {
        _dispatcher.dispatch({ actionType: ActionType.CHANGE_RESULTS_ACTION, data: { results: data }});
      },
      error: function(jqXHR, textStatus, errorThrown ) {
        // TODO: dispatch a CHANGE_RESULTS_ACTION with the specific error (i.e. probably user input problem)
        alert("Error: Unable to load results");
      }
    });
  }

  return exports;
}

