
//**************************************************
// Dispatcher
//**************************************************

// use the raw Flux dispatcher
var Dispatcher = Flux.Dispatcher;

// types of actions sent through the dispatcher
var ActionType = {
  CHANGE_QUESTION_ACTION:    "changeQuestionAction",
  CHANGE_PARAM_ACTION:       "changeParamAction",
  CHANGE_PAGING_ACTION:      "changePagingAction",
  SET_ATTRIBUTES_VISIBLE:    "setAttributesVisible",
  SET_SELECTED_ATTRIBUTES:   "setSelectedAttributes",
  CHANGE_RESULTS_ACTION:     "changeResultsAction",
  SET_LOADING_ACTION:        "setLoadingAction",
  CREATE_STEP_ACTION:        "createStepAction",
  CREATE_TEMP_RESULT_ACTION: "createTempResultAction"
};

//**************************************************
// Helper functions
//**************************************************

var Util = (function() {

  // public methods
  var exports = {
    isPositiveInteger: isPositiveInteger,
    toggleArrayItem: toggleArrayItem,
    getAnswerRequestJson: getAnswerRequestJson,
    getStepRequestJson: getStepRequestJson
  };

  function isPositiveInteger(str) {
    return /^([1-9]\d*)$/.test(str);
  }

  function toggleArrayItem(array, item) {
    return (array.indexOf(item) == -1 ? array.concat([item]) :
      array.filter(function(candidate){ return (candidate != item); }));
  }

  function getAnswerRequestJson(question, paramMap, pagination, selectedAttributes) {
    var paramPack = {};
    Object.keys(paramMap).forEach(function(paramName) {
      paramPack[paramName] = paramMap[paramName].value;
    });
    var offset = (pagination.pageNum - 1) * pagination.pageSize;
    var numRecords = pagination.pageSize;
    if (offset < 0) offset = 0;
    if (numRecords < 1) numRecords = 10;
    return {
      answerSpec: {
        questionName: question.name,
        parameters: paramPack,
        filters: []
      },
      formatting: {
        formatConfig: {
          pagination: { offset: offset, numRecords: numRecords },
          attributes: selectedAttributes
          //sorting: null
        }
      }
    };
  }


  function getStepRequestJson(question, paramMap, pagination, selectedAttributes) {
    var paramPack = {};
    Object.keys(paramMap).forEach(function(paramName) {
     var paramValue = paramMap[paramName].value;
     paramPack[paramName] = paramValue == undefined ? null : paramValue;
    });
    var offset = (pagination.pageNum - 1) * pagination.pageSize;
    var numRecords = pagination.pageSize;
    if (offset < 0) offset = 0;
    if (numRecords < 1) numRecords = 10;
    return {
      answerSpec: {
        questionName: question.name,
        parameters: paramPack,
        filters: []
      },
      formatting: {
        formatConfig: {}
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
    setPagination: setPagination,
    setAttributesVisible: setAttributesVisible,
    setSelectedAttributes: setSelectedAttributes,
    loadResults: loadResults,
    createStep: createStep,
    createTempResult: createTempResult
  };

  // private data
  var _serviceUrl = serviceUrl;
  var _dispatcher = dispatcher;

  function setLoading(loading) {
    _dispatcher.dispatch({ actionType: ActionType.SET_LOADING_ACTION, data: loading });
  }

  function setQuestion(questionName) {
    var action = {
      actionType: ActionType.CHANGE_QUESTION_ACTION,
      data: {
        name: questionName,
        parameters: []
      }
    };
    // no need to load params when user selects none
    if (questionName == Store.NO_QUESTION_SELECTED) {
      _dispatcher.dispatch(action);
    }
    else {
      setLoading(true);
      jQuery.ajax({
        type: "GET",
        url: _serviceUrl + "/questions/" + questionName + "?expandParams=true&" +
          "expandAttributes=true&expandTables=true&expandTableAttributes=true",
        success: function(questionData, textStatus, jqXHR) {
          // got question, now get recordclass for that question
          var rcName = questionData.recordClassName;
          jQuery.ajax({
            type: "GET",
            url: _serviceUrl + "/records/" + rcName + "?expandAttributes=true",
            success: function(recordClassData, textStatus, jqXHR) {
              setLoading(false);
              action.data = {
                question: questionData,
                recordClass: recordClassData
              }
              _dispatcher.dispatch(action);
            },
            error: function(jqXHR, textStatus, errorThrown ) {
              setLoading(false);
              alert("Error: Unable to load attribute selections for question " + questionName);
            }
          });
        },
        error: function(jqXHR, textStatus, errorThrown ) {
          setLoading(false);
          alert("Error: Unable to load params for question " + questionName);
        }
      });
    }
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

  function setAttributesVisible(newIsVisible) {
    _dispatcher.dispatch({
      actionType: ActionType.SET_ATTRIBUTES_VISIBLE,
      data: newIsVisible
    });
  }

  function setSelectedAttributes(attributeArray) {
    _dispatcher.dispatch({
      actionType: ActionType.SET_SELECTED_ATTRIBUTES,
      data: attributeArray
    });
  }

  function loadResults(data) {
    setLoading(true);
    jQuery.ajax({
      type: "POST",
      url: _serviceUrl + "/answer",
      contentType: 'application/json; charset=UTF-8',
      data: JSON.stringify(Util.getAnswerRequestJson(data.selectedQuestion, data.paramValues, data.pagination, data.selectedAttributes)),
      dataType: "json",
      success: function(data, textStatus, jqXHR) {
        setLoading(false);
        _dispatcher.dispatch({ actionType: ActionType.CHANGE_RESULTS_ACTION, data: data });
      },
      error: function(jqXHR, textStatus, errorThrown ) {
        // TODO: dispatch a CHANGE_RESULTS_ACTION with the specific error (i.e. probably user input problem)
        setLoading(false);
        alert("Error: Unable to load results");
      }
    });
  }

  function createStep(data) {
    setLoading(true);
    jQuery.ajax({
      type: "POST",
      url: _serviceUrl + "/users/current/steps",
      contentType: 'application/json; charset=UTF-8',
      data: JSON.stringify(Util.getStepRequestJson(data.selectedQuestion, data.paramValues, data.pagination, data.selectedAttributes)),
      dataType: "json",
      success: function(data, textStatus, jqXHR) {
        setLoading(false);
        _dispatcher.dispatch({ actionType: ActionType.CREATE_STEP_ACTION, data: data });
      },
      error: function(jqXHR, textStatus, errorThrown ) {
        setLoading(false);
        alert("Error: Unable to create the step");
      }
    });
  }

  function createTempResult(data) {
    setLoading(true);
    jQuery.ajax({
      type: "POST",
      url: _serviceUrl + "/temporary-results",
      contentType: 'application/json; charset=UTF-8',
      data: JSON.stringify(Util.getAnswerRequestJson(data.selectedQuestion, data.paramValues, data.pagination, data.selectedAttributes)),
      dataType: "json",
      success: function(data, textStatus, jqXHR) {
        setLoading(false);
        // build URL to new resource
        var url = _serviceUrl + "/temporary-results/" + data.id;
        _dispatcher.dispatch({ actionType: ActionType.CREATE_TEMP_RESULT_ACTION, data: url });
      },
      error: function(jqXHR, textStatus, errorThrown ) {
        // TODO: dispatch a CHANGE_RESULTS_ACTION with the specific error (i.e. probably user input problem)
        setLoading(false);
        alert("Error: Unable to create a temporary result");
      }
    });
  }

  return exports;
}

