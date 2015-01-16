
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
    selectedQuestion: Store.NO_QUESTION_SELECTED,
    paramOrdering: [],
    paramValues: {},
    results: null,
    pagination: { pageNum: 1, pageSize: 10 },
    isLoading: false
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
        // data is { questionName: String, params: Array }
        _data.selectedQuestion = payload.data.questionName;
        _data.paramOrdering = payload.data.params.map(function(p) { return p.name; });
        _data.paramValues = {};
        payload.data.params.forEach(function(param) {
          _data.paramValues[param.name] = param;
          _data.paramValues[param.name].value = param.defaultValue;
        });
        // clear results
        _data.results = null;
        break;
      case ActionType.CHANGE_PARAM_ACTION:
        // data is { paramName: String, value: Any }
        _data.paramValues[payload.data.paramName].value = payload.data.value;
        break;
      case ActionType.CHANGE_PAGING_ACTION:
        // data is { pageNum: Number, pageSize: Number }
        _data.pagination = payload.data;
        break;
      case ActionType.CHANGE_RESULTS_ACTION:
        // data is { results: Any }
        _data.results = payload.data.results;
        break;
      case ActionType.SET_LOADING_ACTION:
        // data is Boolean
        _data.isLoading = payload.data;
      default:
        // this store does not support other actions
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

