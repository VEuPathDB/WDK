
//**************************************************
// Store
//**************************************************

var Store = function(dispatcher, initialValue) {

  // public methods
  var exports = {
    register: register,
    get: get
  };

  // private data
  var _data = {
    questions: initialValue,
    selectedQuestion: { name: Store.NO_QUESTION_SELECTED, parameters: [] },
    paramOrdering: [],
    paramValues: {},
    results: null,
    resultStats: null,
    pagination: { pageNum: 1, pageSize: 10 },
    isLoading: false,
    showAttributes: false,
    allAttributes: [],
    selectedAttributes: [],
    type: ""
  };
  var _registeredCallbacks = [];

  // returns current store state
  function get() {
    return _data;
  }

  // registers a function to call when state has changed
  function register(callback) {
    _registeredCallbacks.push(callback);
  }

  // calls all registered functions, passing itself as only param
  function updateRegisteredViews() {
    _registeredCallbacks.forEach(function(func) { func(exports); });
  }

  // handles actions from the dispatcher
  function handleAction(payload) {
    // payload delivered is object in the form:
    //   { actionType: ActionType, data: Any }
    switch(payload.actionType) {
      case ActionType.CHANGE_QUESTION_ACTION:
        // quesiton data is from QuestionFormatter
        _data.selectedQuestion = payload.data.question;
        _data.paramOrdering = payload.data.question.parameters.map(function(p) { return p.name; });
        _data.paramValues = {};
        payload.data.question.parameters.forEach(function(param) {
          _data.paramValues[param.name] = param;
          _data.paramValues[param.name].value = param.defaultValue;
        });
        // set up attributes
        _data.showAttributes = false;
        _data.allAttributes = [].concat(
            payload.data.question.dynamicAttributes,
            payload.data.recordClass.attributes);
        _data.selectedAttributes = payload.data.question.defaultAttributes;
        // clear results
        _data.results = null;
        _data.resultStats = null;
        break;
      case ActionType.CHANGE_PARAM_ACTION:
        // data is { paramName: String, value: Any }
        _data.paramValues[payload.data.paramName].value = payload.data.value;
        break;
      case ActionType.CHANGE_PAGING_ACTION:
        // data is { pageNum: Number, pageSize: Number }
        _data.pagination = payload.data;
        break;
      case ActionType.SET_ATTRIBUTES_VISIBLE:
        // data is Boolean (true if visible)
        _data.showAttributes = payload.data;
        break;
      case ActionType.SET_SELECTED_ATTRIBUTES:
        // data is String[] representing new selected attributes
        _data.selectedAttributes = payload.data.slice();
        break;
      case ActionType.SET_LOADING_ACTION:
        // data is Boolean (true if loading)
        _data.isLoading = payload.data;
        break;
      case ActionType.CHANGE_RESULTS_ACTION:
        // data is raw answer response
        _data.type = Store.ANSWER_RESULT;
        _data.results = payload.data;
        _data.resultStats = { pageNum: _data.pagination.pageNum };
        break;
      case ActionType.CREATE_STEP_ACTION:
        // data is raw step response
        _data.type = Store.STEP_RESULT;
        _data.results = payload.data;
        break;
      case ActionType.CREATE_TEMP_RESULT_ACTION:
        // data is string (URL)
        _data.type = Store.TEMP_RESULT;
        _data.results = payload.data;
        break;
      default:
        // this store does not support any other actions
    }
    // then alert registered views of change
    updateRegisteredViews();
  }

  // register action handler with the dispatcher
  dispatcher.register(handleAction);

  return exports;
}

// public constants
Store.NO_QUESTION_SELECTED = "_none_";
Store.ANSWER_RESULT = "_answer_";
Store.STEP_RESULT = "_step_";
Store.TEMP_RESULT = "_temp_result_";

