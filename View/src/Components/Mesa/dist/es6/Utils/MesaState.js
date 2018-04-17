'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.callActionOnSelectedRows = exports.setSortDirection = exports.setSortColumnKey = exports.setEmptinessCulprit = exports.setSearchQuery = exports.setSelectionPredicate = exports.create = exports.getUiState = exports.getEventHandlers = exports.getOptions = exports.getActions = exports.getColumns = exports.getFilteredRows = exports.getRows = exports.getSelectedRows = exports.setEventHandlers = exports.setOptions = exports.setUiState = exports.setActions = exports.setColumnOrder = exports.setColumns = exports.filterRows = exports.setFilteredRows = exports.setRows = undefined;

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

var _Errors = require('../Utils/Errors');

function _toConsumableArray(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = Array(arr.length); i < arr.length; i++) { arr2[i] = arr[i]; } return arr2; } else { return Array.from(arr); } }

/*    Basic Setters   */
var setRows = exports.setRows = function setRows(state, rows) {
  var resetFilteredRows = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : true;

  if (!Array.isArray(rows)) return (0, _Errors.badType)('setRows', 'rows', 'array', typeof rows === 'undefined' ? 'undefined' : _typeof(rows)) || state;
  var filteredRows = [].concat(_toConsumableArray(rows));
  var replacements = Object.assign({}, { rows: rows }, resetFilteredRows ? { filteredRows: filteredRows } : {});
  return Object.assign({}, state, replacements);
};

var setFilteredRows = exports.setFilteredRows = function setFilteredRows(state, filteredRows) {
  if (!Array.isArray(filteredRows)) return (0, _Errors.badType)('setFilteredRows', 'filteredRows', 'array', typeof filteredRows === 'undefined' ? 'undefined' : _typeof(filteredRows)) || state;
  return Object.assign({}, state, { filteredRows: filteredRows });
};

var filterRows = exports.filterRows = function filterRows(state, predicate) {
  if (typeof predicate !== 'function') return (0, _Errors.badType)('filterRows', 'predicate', 'function', typeof predicate === 'undefined' ? 'undefined' : _typeof(predicate)) || state;
  if (!Array.isArray(state.rows)) return (0, _Errors.missingFromState)('filterRows', 'rows', state) || state;
  var filteredRows = state.rows.filter(predicate);
  return setFilteredRows(state, filteredRows);
};

var setColumns = exports.setColumns = function setColumns(state, columns) {
  if (!Array.isArray(columns)) return (0, _Errors.badType)('setColumns', 'columns', 'array', typeof columns === 'undefined' ? 'undefined' : _typeof(columns)) || state;
  var keys = columns.map(function (col) {
    return col.key;
  });
  var initialUiState = state.uiState ? state.uiState : {};
  var columnOrder = initialUiState.columnOrder ? initialUiState.columnOrder : [];
  keys.forEach(function (key) {
    if (!columnOrder.includes(key)) columnOrder = [].concat(_toConsumableArray(columnOrder), [key]);
  });
  columnOrder = columnOrder.filter(function (key) {
    return keys.includes(key);
  });
  var uiState = Object.assign({}, initialUiState, { columnOrder: columnOrder });
  return Object.assign({}, state, { columns: columns, uiState: uiState });
};

var setColumnOrder = exports.setColumnOrder = function setColumnOrder(state, columnOrder) {
  if (!Array.isArray(columnOrder)) return (0, _Errors.badType)('setColumnOrder', 'columnOrder', 'array', typeof columnOrder === 'undefined' ? 'undefined' : _typeof(columnOrder));
  var initialUiState = state.uiState ? state.uiState : {};
  var uiState = Object.assign({}, initialUiState, { columnOrder: columnOrder });
  return Object.assign({}, state, { uiState: uiState });
};

var setActions = exports.setActions = function setActions(state, actions) {
  if (!Array.isArray(actions)) return (0, _Errors.badType)('setActions', 'actions', 'array', typeof actions === 'undefined' ? 'undefined' : _typeof(actions)) || state;
  return Object.assign({}, state, { actions: actions });
};

var setUiState = exports.setUiState = function setUiState(state, uiState) {
  if ((typeof uiState === 'undefined' ? 'undefined' : _typeof(uiState)) !== 'object') return (0, _Errors.badType)('setUiState', 'uiState', 'object', typeof uiState === 'undefined' ? 'undefined' : _typeof(uiState)) || state;
  return Object.assign({}, state, { uiState: uiState });
};

var setOptions = exports.setOptions = function setOptions(state, options) {
  if ((typeof options === 'undefined' ? 'undefined' : _typeof(options)) !== 'object') return (0, _Errors.badType)('setOptions', 'options', 'object', typeof options === 'undefined' ? 'undefined' : _typeof(options)) || state;
  return Object.assign({}, state, { options: options });
};

var setEventHandlers = exports.setEventHandlers = function setEventHandlers(state, eventHandlers) {
  if ((typeof eventHandlers === 'undefined' ? 'undefined' : _typeof(eventHandlers)) !== 'object') return (0, _Errors.badType)('setEventHandlers', 'eventHandlers', 'object', typeof eventHandlers === 'undefined' ? 'undefined' : _typeof(eventHandlers)) || state;
  return Object.assign({}, state, { eventHandlers: eventHandlers });
};

var getSelectedRows = exports.getSelectedRows = function getSelectedRows(state) {
  var onlyFilteredRows = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : true;

  if (onlyFilteredRows && !'filteredRows' in state) return (0, _Errors.missingFromState)('getSelectedRows', 'filteredRows', state) || state;
  var filteredRows = state.filteredRows;

  if (onlyFilteredRows && !Array.isArray(filteredRows)) return (0, _Errors.badType)('getSelectedRows', 'filteredRows', 'array', typeof filteredRows === 'undefined' ? 'undefined' : _typeof(filteredRows)) || state;

  if (!onlyFilteredRows && !'rows' in state) return (0, _Errors.missingFromState)('getSelectedRows', 'filteredRows', state) || state;
  var rows = state.rows;

  if (!onlyFilteredRows && !Array.isArray(rows)) return (0, _Errors.badType)('getSelectedRows', 'rows', 'array', typeof rows === 'undefined' ? 'undefined' : _typeof(rows)) || state;

  if (!'options' in state) return (0, _Errors.missingFromState)('getSelectedRows', 'options', state) || state;
  if (_typeof(state.options) !== 'object') return (0, _Errors.badType)('getSelectedRows', 'options', 'object', typeof options === 'undefined' ? 'undefined' : _typeof(options)) || state;
  var options = state.options;


  if (!'isRowSelected' in options) return (0, _Errors.missingFromState)('getSelectedRows', 'options.isRowSelected', options) || state;
  var isRowSelected = state.isRowSelected;

  if (typeof isRowSelected !== 'function') return (0, _Errors.badType)('getSelectedRows', 'options.isRowSelected', 'function', typeof isRowSelected === 'undefined' ? 'undefined' : _typeof(isRowSelected)) || state;

  return (onlyFilteredRows ? filteredRows : rows).filter(isRowSelected);
};

var getRows = exports.getRows = function getRows(state) {
  var rows = state.rows;

  if (!Array.isArray(rows)) {
    (0, _Errors.badType)('getRows', 'rows', 'array', typeof rows === 'undefined' ? 'undefined' : _typeof(rows));
    return [];
  }
  return rows;
};

var getFilteredRows = exports.getFilteredRows = function getFilteredRows(state) {
  var filteredRows = state.filteredRows;

  if (!Array.isArray(filteredRows)) {
    (0, _Errors.badType)('getFilteredRows', 'filteredRows', 'array', typeof filteredRows === 'undefined' ? 'undefined' : _typeof(filteredRows));
    return [];
  }
  return filteredRows;
};

var getColumns = exports.getColumns = function getColumns(state) {
  var columns = state.columns;

  if (!Array.isArray(columns)) {
    (0, _Errors.badType)('getColumns', 'columns', 'array', typeof columns === 'undefined' ? 'undefined' : _typeof(columns));
    return [];
  }
  return columns;
};

var getActions = exports.getActions = function getActions(state) {
  var actions = state.actions;

  if (!Array.isArray(actions)) {
    (0, _Errors.badType)('getActions', 'actions', 'array', typeof actions === 'undefined' ? 'undefined' : _typeof(actions));
    return [];
  }
  return actions;
};

var getOptions = exports.getOptions = function getOptions(state) {
  var options = state.options;

  if ((typeof options === 'undefined' ? 'undefined' : _typeof(options)) !== 'object') {
    (0, _Errors.badType)('getOptions', 'options', 'object', typeof options === 'undefined' ? 'undefined' : _typeof(options));
    return {};
  }
  return options;
};

var getEventHandlers = exports.getEventHandlers = function getEventHandlers(state) {
  var eventHandlers = state.eventHandlers;

  if ((typeof eventHandlers === 'undefined' ? 'undefined' : _typeof(eventHandlers)) !== 'object') {
    (0, _Errors.badType)('getEventHandlers', 'eventHandlers', 'object', typeof eventHandlers === 'undefined' ? 'undefined' : _typeof(eventHandlers));
    return [];
  }
  return eventHandlers;
};

var getUiState = exports.getUiState = function getUiState(state) {
  var uiState = state.uiState;

  if ((typeof uiState === 'undefined' ? 'undefined' : _typeof(uiState)) !== 'object') {
    (0, _Errors.badType)('getUiState', 'uiState', 'object', typeof uiState === 'undefined' ? 'undefined' : _typeof(uiState));
    return {};
  }
  return uiState;
};

/*    Generic state "create" function   */

var create = exports.create = function create(_ref) {
  var rows = _ref.rows,
      filteredRows = _ref.filteredRows,
      columns = _ref.columns,
      options = _ref.options,
      actions = _ref.actions,
      eventHandlers = _ref.eventHandlers,
      uiState = _ref.uiState;
  var state = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : {};

  state = setRows(state, rows ? rows : []);
  state = setColumns(state, columns ? columns : []);
  state = setOptions(state, options ? options : {});
  state = setActions(state, actions ? actions : []);
  state = setFilteredRows(state, filteredRows ? filteredRows : rows ? rows : []);
  state = setEventHandlers(state, eventHandlers ? eventHandlers : {});
  state = setUiState(state, uiState ? uiState : {});
  return state;
};

/*    Deeper, more specific setters   */

var setSelectionPredicate = exports.setSelectionPredicate = function setSelectionPredicate(state, predicate) {
  if (typeof predicate !== 'function') return (0, _Errors.badType)('setSelectionPredicate', 'predicate', 'function', typeof predicate === 'undefined' ? 'undefined' : _typeof(predicate)) || state;
  var options = Object.assign({}, state.options ? state.options : {}, { isRowSelected: predicate });
  return Object.assign({}, state, { options: options });
};

var setSearchQuery = exports.setSearchQuery = function setSearchQuery(state, searchQuery) {
  if (typeof searchQuery !== 'string' && searchQuery !== null) return (0, _Errors.badType)('setSearchQuery', 'searchQuery', 'string', typeof searchQuery === 'undefined' ? 'undefined' : _typeof(searchQuery)) || state;

  var uiState = Object.assign({}, state.uiState ? state.uiState : {}, { searchQuery: searchQuery });
  return Object.assign({}, state, { uiState: uiState });
};

var setEmptinessCulprit = exports.setEmptinessCulprit = function setEmptinessCulprit(state, emptinessCulprit) {
  if (typeof emptinessCulprit !== 'string' && emptinessCulprit !== null) return (0, _Errors.badType)('setEmptinessCulprit', 'emptinessCulprit', 'string', typeof emptinessCulprit === 'undefined' ? 'undefined' : _typeof(emptinessCulprit)) || state;

  var uiState = Object.assign({}, state.uiState ? state.uiState : {}, { emptinessCulprit: emptinessCulprit });
  return Object.assign({}, state, { uiState: uiState });
};

var setSortColumnKey = exports.setSortColumnKey = function setSortColumnKey(state, columnKey) {
  if (typeof columnKey !== 'string') return (0, _Errors.badType)('setSortColumnKey', 'columnKey', 'string', typeof columnKey === 'undefined' ? 'undefined' : _typeof(columnKey)) || state;

  var currentUiState = Object.assign({}, state.uiState ? state.uiState : {});
  var sort = Object.assign({}, currentUiState.sort ? currentUiState.sort : {}, { columnKey: columnKey });
  var uiState = Object.assign({}, currentUiState, { sort: sort });
  return Object.assign({}, state, { uiState: uiState });
};

var setSortDirection = exports.setSortDirection = function setSortDirection(state, direction) {
  if (typeof direction !== 'string') return (0, _Errors.badType)('setSortDirection', 'direction', 'string', typeof direction === 'undefined' ? 'undefined' : _typeof(direction)) || state;
  if (!['asc', 'desc'].includes(direction)) return (0, _Errors.fail)('setSortDirection', '"direction" must be either "asc" or "desc"', SyntaxError) || state;

  var currentUiState = Object.assign({}, state.uiState ? state.uiState : {});
  var sort = Object.assign({}, currentUiState.sort ? currentUiState.sort : {}, { direction: direction });
  var uiState = Object.assign({}, currentUiState, { sort: sort });
  return Object.assign({}, state, { uiState: uiState });
};

var callActionOnSelectedRows = exports.callActionOnSelectedRows = function callActionOnSelectedRows(state, action) {
  var batch = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : false;
  var onlyFilteredRows = arguments.length > 3 && arguments[3] !== undefined ? arguments[3] : true;

  if (!'selectedRows' in state) return (0, _Errors.missingFromState)('callActionOnSelectedRows', 'selectedRows', state) || state;
  if (typeof action !== 'function') return (0, _Errors.badType)('callActionOnSelectedRows', 'action', 'function', typeof action === 'undefined' ? 'undefined' : _typeof(action)) || state;

  var selectedRows = getSelectedRows(state, onlyFilteredRows);
  if (batch) action(selectedRows);else selectedRows.forEach(action);
  return state;
};