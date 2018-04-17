'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.callActionOnSelectedRows = exports.moveColumnToIndex = exports.setSortDirection = exports.setSortColumnKey = exports.setEmptinessCulprit = exports.setSearchQuery = exports.setSelectionPredicate = exports.create = exports.getUiState = exports.getEventHandlers = exports.getOptions = exports.getActions = exports.getColumns = exports.getFilteredRows = exports.getRows = exports.getSelectedRows = exports.setEventHandlers = exports.setOptions = exports.setUiState = exports.setActions = exports.setColumnOrder = exports.setColumns = exports.filterRows = exports.setFilteredRows = exports.setRows = undefined;

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

var _Errors = require('../Utils/Errors');

var _Utils = require('../Utils/Utils');

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
  state = setUiState(state, uiState ? uiState : {});
  state = setEventHandlers(state, eventHandlers ? eventHandlers : {});
  state = setFilteredRows(state, filteredRows ? filteredRows : rows ? rows : []);
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

var moveColumnToIndex = exports.moveColumnToIndex = function moveColumnToIndex(state, columnKey, toIndex) {
  if (typeof columnKey !== 'string') return (0, _Errors.badType)('changeColumnIndex', '"columnKey" should be a string.', TypeError);
  if (typeof toIndex !== 'number') return (0, _Errors.badType)('changeColumnIndex', '"toIndex" should be a number"', TypeError);
  if (!'columns' in state) return (0, _Errors.missingFromState)('changeColumnIndex', 'columns', state) || state;

  var oldColumns = getColumns(state);
  var fromIndex = oldColumns.findIndex(function (_ref2) {
    var key = _ref2.key;
    return columnKey === key;
  });
  if (fromIndex < 0) return (0, _Errors.fail)('changeColumnIndex', 'column with key "' + columnKey + '" not found.') || state;
  var columns = (0, _Utils.repositionItemInList)(oldColumns, fromIndex, toIndex);
  return Object.assign({}, state, { columns: columns });
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
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9VdGlscy9NZXNhU3RhdGUuanMiXSwibmFtZXMiOlsic2V0Um93cyIsInN0YXRlIiwicm93cyIsInJlc2V0RmlsdGVyZWRSb3dzIiwiQXJyYXkiLCJpc0FycmF5IiwiZmlsdGVyZWRSb3dzIiwicmVwbGFjZW1lbnRzIiwiT2JqZWN0IiwiYXNzaWduIiwic2V0RmlsdGVyZWRSb3dzIiwiZmlsdGVyUm93cyIsInByZWRpY2F0ZSIsImZpbHRlciIsInNldENvbHVtbnMiLCJjb2x1bW5zIiwia2V5cyIsIm1hcCIsImNvbCIsImtleSIsImluaXRpYWxVaVN0YXRlIiwidWlTdGF0ZSIsImNvbHVtbk9yZGVyIiwiZm9yRWFjaCIsImluY2x1ZGVzIiwic2V0Q29sdW1uT3JkZXIiLCJzZXRBY3Rpb25zIiwiYWN0aW9ucyIsInNldFVpU3RhdGUiLCJzZXRPcHRpb25zIiwib3B0aW9ucyIsInNldEV2ZW50SGFuZGxlcnMiLCJldmVudEhhbmRsZXJzIiwiZ2V0U2VsZWN0ZWRSb3dzIiwib25seUZpbHRlcmVkUm93cyIsImlzUm93U2VsZWN0ZWQiLCJnZXRSb3dzIiwiZ2V0RmlsdGVyZWRSb3dzIiwiZ2V0Q29sdW1ucyIsImdldEFjdGlvbnMiLCJnZXRPcHRpb25zIiwiZ2V0RXZlbnRIYW5kbGVycyIsImdldFVpU3RhdGUiLCJjcmVhdGUiLCJzZXRTZWxlY3Rpb25QcmVkaWNhdGUiLCJzZXRTZWFyY2hRdWVyeSIsInNlYXJjaFF1ZXJ5Iiwic2V0RW1wdGluZXNzQ3VscHJpdCIsImVtcHRpbmVzc0N1bHByaXQiLCJzZXRTb3J0Q29sdW1uS2V5IiwiY29sdW1uS2V5IiwiY3VycmVudFVpU3RhdGUiLCJzb3J0Iiwic2V0U29ydERpcmVjdGlvbiIsImRpcmVjdGlvbiIsIlN5bnRheEVycm9yIiwibW92ZUNvbHVtblRvSW5kZXgiLCJ0b0luZGV4IiwiVHlwZUVycm9yIiwib2xkQ29sdW1ucyIsImZyb21JbmRleCIsImZpbmRJbmRleCIsImNhbGxBY3Rpb25P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S0FBN0QsS0FBdUVBLEtBQTlFO0FBQ0YsTUFBSSxPQUFPOEQsTUFBUCxLQUFrQixVQUF0QixFQUNFLE9BQU8scUJBQVEsMEJBQVIsRUFBb0MsUUFBcEMsRUFBOEMsVUFBOUMsU0FBaUVBLE1BQWpFLHlDQUFpRUEsTUFBakUsTUFBNEU5RCxLQUFuRjs7QUFFRixNQUFNZ0UsZUFBZWhDLGdCQUFnQmhDLEtBQWhCLEVBQXVCaUMsZ0JBQXZCLENBQXJCO0FBQ0EsTUFBSThCLEtBQUosRUFBV0QsT0FBT0UsWUFBUCxFQUFYLEtBQ0tBLGFBQWExQyxPQUFiLENBQXFCd0MsTUFBckI7QUFDTCxTQUFPOUQsS0FBUDtBQUNELENBVk0iLCJmaWxlIjoiTWVzYVN0YXRlLmpzIiwic291cmNlc0NvbnRlbnQiOlsiaW1wb3J0IHsgZmFpbCwgYmFkVHlwZSwgbWlzc2luZ0Zyb21TdGF0ZSB9IGZyb20gJy4uL1V0aWxzL0Vycm9ycyc7XG5pbXBvcnQgeyByZXBvc2l0aW9uSXRlbUluTGlzdCB9IGZyb20gJy4uL1V0aWxzL1V0aWxzJztcblxuLyogICAgQmFzaWMgU2V0dGVycyAgICovXG5leHBvcnQgY29uc3Qgc2V0Um93cyA9IChzdGF0ZSwgcm93cywgcmVzZXRGaWx0ZXJlZFJvd3MgPSB0cnVlKSA9PiB7XG4gIGlmICghQXJyYXkuaXNBcnJheShyb3dzKSlcbiAgICByZXR1cm4gYmFkVHlwZSgnc2V0Um93cycsICdyb3dzJywgJ2FycmF5JywgdHlwZW9mIHJvd3MpIHx8IHN0YXRlO1xuICBsZXQgZmlsdGVyZWRSb3dzID0gWy4uLnJvd3NdO1xuICBsZXQgcmVwbGFjZW1lbnRzID0gT2JqZWN0LmFzc2lnbih7fSwgeyByb3dzIH0sIHJlc2V0RmlsdGVyZWRSb3dzID8geyBmaWx0ZXJlZFJvd3MgfSA6IHt9KTtcbiAgcmV0dXJuIE9iamVjdC5hc3NpZ24oe30sIHN0YXRlLCByZXBsYWNlbWVudHMpO1xufTtcblxuZXhwb3J0IGNvbnN0IHNldEZpbHRlcmVkUm93cyA9IChzdGF0ZSwgZmlsdGVyZWRSb3dzKSA9PiB7XG4gIGlmICghQXJyYXkuaXNBcnJheShmaWx0ZXJlZFJvd3MpKVxuICAgIHJldHVybiBiYWRUeXBlKCdzZXRGaWx0ZXJlZFJvd3MnLCAnZmlsdGVyZWRSb3dzJywgJ2FycmF5JywgdHlwZW9mIGZpbHRlcmVkUm93cykgfHwgc3RhdGU7XG4gIHJldHVybiBPYmplY3QuYXNzaWduKHt9LCBzdGF0ZSwgeyBmaWx0ZXJlZFJvd3MgfSk7XG59O1xuXG5leHBvcnQgY29uc3QgZmlsdGVyUm93cyA9IChzdGF0ZSwgcHJlZGljYXRlKSA9PiB7XG4gIGlmICh0eXBlb2YgcHJlZGljYXRlICE9PSAnZnVuY3Rpb24nKVxuICAgIHJldHVybiBiYWRUeXBlKCdmaWx0ZXJSb3dzJywgJ3ByZWRpY2F0ZScsICdmdW5jdGlvbicsIHR5cGVvZiBwcmVkaWNhdGUpIHx8IHN0YXRlO1xuICBpZiAoIUFycmF5LmlzQXJyYXkoc3RhdGUucm93cykpXG4gICAgcmV0dXJuIG1pc3NpbmdGcm9tU3RhdGUoJ2ZpbHRlclJvd3MnLCAncm93cycsIHN0YXRlKSB8fCBzdGF0ZTtcbiAgY29uc3QgZmlsdGVyZWRSb3dzID0gc3RhdGUucm93cy5maWx0ZXIocHJlZGljYXRlKTtcbiAgcmV0dXJuIHNldEZpbHRlcmVkUm93cyhzdGF0ZSwgZmlsdGVyZWRSb3dzKTtcbn1cblxuZXhwb3J0IGNvbnN0IHNldENvbHVtbnMgPSAoc3RhdGUsIGNvbHVtbnMpID0+IHtcbiAgaWYgKCFBcnJheS5pc0FycmF5KGNvbHVtbnMpKVxuICAgIHJldHVybiBiYWRUeXBlKCdzZXRDb2x1bW5zJywgJ2NvbHVtbnMnLCAnYXJyYXknLCB0eXBlb2YgY29sdW1ucykgfHwgc3RhdGU7XG4gIGNvbnN0IGtleXMgPSBjb2x1bW5zLm1hcChjb2wgPT4gY29sLmtleSk7XG4gIGNvbnN0IGluaXRpYWxVaVN0YXRlID0gc3RhdGUudWlTdGF0ZSA/IHN0YXRlLnVpU3RhdGUgOiB7fTtcbiAgbGV0IGNvbHVtbk9yZGVyID0gaW5pdGlhbFVpU3RhdGUuY29sdW1uT3JkZXIgPyBpbml0aWFsVWlTdGF0ZS5jb2x1bW5PcmRlciA6IFtdO1xuICBrZXlzLmZvckVhY2goa2V5ID0+IHtcbiAgICBpZiAoIWNvbHVtbk9yZGVyLmluY2x1ZGVzKGtleSkpIGNvbHVtbk9yZGVyID0gWy4uLmNvbHVtbk9yZGVyLCBrZXldO1xuICB9KTtcbiAgY29sdW1uT3JkZXIgPSBjb2x1bW5PcmRlci5maWx0ZXIoa2V5ID0+IGtleXMuaW5jbHVkZXMoa2V5KSk7XG4gIGNvbnN0IHVpU3RhdGUgPSBPYmplY3QuYXNzaWduKHt9LCBpbml0aWFsVWlTdGF0ZSwgeyBjb2x1bW5PcmRlciB9KTtcbiAgcmV0dXJuIE9iamVjdC5hc3NpZ24oe30sIHN0YXRlLCB7IGNvbHVtbnMsIHVpU3RhdGUgfSk7XG59O1xuXG5leHBvcnQgY29uc3Qgc2V0Q29sdW1uT3JkZXIgPSAoc3RhdGUsIGNvbHVtbk9yZGVyKSA9PiB7XG4gIGlmICghQXJyYXkuaXNBcnJheShjb2x1bW5PcmRlcikpXG4gICAgcmV0dXJuIGJhZFR5cGUoJ3NldENvbHVtbk9yZGVyJywgJ2NvbHVtbk9yZGVyJywgJ2FycmF5JywgdHlwZW9mIGNvbHVtbk9yZGVyKTtcbiAgY29uc3QgaW5pdGlhbFVpU3RhdGUgPSBzdGF0ZS51aVN0YXRlID8gc3RhdGUudWlTdGF0ZSA6IHt9O1xuICBjb25zdCB1aVN0YXRlID0gT2JqZWN0LmFzc2lnbih7fSwgaW5pdGlhbFVpU3RhdGUsIHsgY29sdW1uT3JkZXIgfSk7XG4gIHJldHVybiBPYmplY3QuYXNzaWduKHt9LCBzdGF0ZSwgeyB1aVN0YXRlIH0pO1xufVxuXG5leHBvcnQgY29uc3Qgc2V0QWN0aW9ucyA9IChzdGF0ZSwgYWN0aW9ucykgPT4ge1xuICBpZiAoIUFycmF5LmlzQXJyYXkoYWN0aW9ucykpXG4gICAgcmV0dXJuIGJhZFR5cGUoJ3NldEFjdGlvbnMnLCAnYWN0aW9ucycsICdhcnJheScsIHR5cGVvZiBhY3Rpb25zKSB8fCBzdGF0ZTtcbiAgcmV0dXJuIE9iamVjdC5hc3NpZ24oe30sIHN0YXRlLCB7IGFjdGlvbnMgfSk7XG59O1xuXG5leHBvcnQgY29uc3Qgc2V0VWlTdGF0ZSA9IChzdGF0ZSwgdWlTdGF0ZSkgPT4ge1xuICBpZiAodHlwZW9mIHVpU3RhdGUgIT09ICdvYmplY3QnKVxuICAgIHJldHVybiBiYWRUeXBlKCdzZXRVaVN0YXRlJywgJ3VpU3RhdGUnLCAnb2JqZWN0JywgdHlwZW9mIHVpU3RhdGUpIHx8IHN0YXRlO1xuICByZXR1cm4gT2JqZWN0LmFzc2lnbih7fSwgc3RhdGUsIHsgdWlTdGF0ZSB9KTtcbn07XG5cbmV4cG9ydCBjb25zdCBzZXRPcHRpb25zID0gKHN0YXRlLCBvcHRpb25zKSA9PiB7XG4gIGlmICh0eXBlb2Ygb3B0aW9ucyAhPT0gJ29iamVjdCcpXG4gICAgcmV0dXJuIGJhZFR5cGUoJ3NldE9wdGlvbnMnLCAnb3B0aW9ucycsICdvYmplY3QnLCB0eXBlb2Ygb3B0aW9ucykgfHwgc3RhdGU7XG4gIHJldHVybiBPYmplY3QuYXNzaWduKHt9LCBzdGF0ZSwgeyBvcHRpb25zIH0pO1xufTtcblxuZXhwb3J0IGNvbnN0IHNldEV2ZW50SGFuZGxlcnMgPSAoc3RhdGUsIGV2ZW50SGFuZGxlcnMpID0+IHtcbiAgaWYgKHR5cGVvZiBldmVudEhhbmRsZXJzICE9PSAnb2JqZWN0JylcbiAgICByZXR1cm4gYmFkVHlwZSgnc2V0RXZlbnRIYW5kbGVycycsICdldmVudEhhbmRsZXJzJywgJ29iamVjdCcsIHR5cGVvZiBldmVudEhhbmRsZXJzKSB8fCBzdGF0ZTtcbiAgcmV0dXJuIE9iamVjdC5hc3NpZ24oe30sIHN0YXRlLCB7IGV2ZW50SGFuZGxlcnMgfSk7XG59O1xuXG5leHBvcnQgY29uc3QgZ2V0U2VsZWN0ZWRSb3dzID0gKHN0YXRlLCBvbmx5RmlsdGVyZWRSb3dzID0gdHJ1ZSkgPT4ge1xuICBpZiAob25seUZpbHRlcmVkUm93cyAmJiAhJ2ZpbHRlcmVkUm93cycgaW4gc3RhdGUpXG4gICAgcmV0dXJuIG1pc3NpbmdGcm9tU3RhdGUoJ2dldFNlbGVjdGVkUm93cycsICdmaWx0ZXJlZFJvd3MnLCBzdGF0ZSkgfHwgc3RhdGU7XG4gIGNvbnN0IHsgZmlsdGVyZWRSb3dzIH0gPSBzdGF0ZTtcbiAgaWYgKG9ubHlGaWx0ZXJlZFJvd3MgJiYgIUFycmF5LmlzQXJyYXkoZmlsdGVyZWRSb3dzKSlcbiAgICByZXR1cm4gYmFkVHlwZSgnZ2V0U2VsZWN0ZWRSb3dzJywgJ2ZpbHRlcmVkUm93cycsICdhcnJheScsIHR5cGVvZiBmaWx0ZXJlZFJvd3MpIHx8IHN0YXRlO1xuXG4gIGlmICghb25seUZpbHRlcmVkUm93cyAmJiAhJ3Jvd3MnIGluIHN0YXRlKVxuICAgIHJldHVybiBtaXNzaW5nRnJvbVN0YXRlKCdnZXRTZWxlY3RlZFJvd3MnLCAnZmlsdGVyZWRSb3dzJywgc3RhdGUpIHx8IHN0YXRlO1xuICBjb25zdCB7IHJvd3MgfSA9IHN0YXRlO1xuICBpZiAoIW9ubHlGaWx0ZXJlZFJvd3MgJiYgIUFycmF5LmlzQXJyYXkocm93cykpXG4gICAgcmV0dXJuIGJhZFR5cGUoJ2dldFNlbGVjdGVkUm93cycsICdyb3dzJywgJ2FycmF5JywgdHlwZW9mIHJvd3MpIHx8IHN0YXRlO1xuXG4gIGlmICghJ29wdGlvbnMnIGluIHN0YXRlKVxuICAgIHJldHVybiBtaXNzaW5nRnJvbVN0YXRlKCdnZXRTZWxlY3RlZFJvd3MnLCAnb3B0aW9ucycsIHN0YXRlKSB8fCBzdGF0ZTtcbiAgaWYgKHR5cGVvZiBzdGF0ZS5vcHRpb25zICE9PSAnb2JqZWN0JylcbiAgICByZXR1cm4gYmFkVHlwZSgnZ2V0U2VsZWN0ZWRSb3dzJywgJ29wdGlvbnMnLCAnb2JqZWN0JywgdHlwZW9mIG9wdGlvbnMpIHx8IHN0YXRlO1xuICBjb25zdCB7IG9wdGlvbnMgfSA9IHN0YXRlO1xuXG4gIGlmICghJ2lzUm93U2VsZWN0ZWQnIGluIG9wdGlvbnMpXG4gICAgcmV0dXJuIG1pc3NpbmdGcm9tU3RhdGUoJ2dldFNlbGVjdGVkUm93cycsICdvcHRpb25zLmlzUm93U2VsZWN0ZWQnLCBvcHRpb25zKSB8fCBzdGF0ZTtcbiAgY29uc3QgeyBpc1Jvd1NlbGVjdGVkIH0gPSBzdGF0ZTtcbiAgaWYgKHR5cGVvZiBpc1Jvd1NlbGVjdGVkICE9PSAnZnVuY3Rpb24nKVxuICAgIHJldHVybiBiYWRUeXBlKCdnZXRTZWxlY3RlZFJvd3MnLCAnb3B0aW9ucy5pc1Jvd1NlbGVjdGVkJywgJ2Z1bmN0aW9uJywgdHlwZW9mIGlzUm93U2VsZWN0ZWQpIHx8IHN0YXRlO1xuXG4gIHJldHVybiAob25seUZpbHRlcmVkUm93cyA/IGZpbHRlcmVkUm93cyA6IHJvd3MpLmZpbHRlcihpc1Jvd1NlbGVjdGVkKTtcbn07XG5cbmV4cG9ydCBjb25zdCBnZXRSb3dzID0gKHN0YXRlKSA9PiB7XG4gIGNvbnN0IHsgcm93cyB9ID0gc3RhdGU7XG4gIGlmICghQXJyYXkuaXNBcnJheShyb3dzKSkge1xuICAgIGJhZFR5cGUoJ2dldFJvd3MnLCAncm93cycsICdhcnJheScsIHR5cGVvZiByb3dzKTtcbiAgICByZXR1cm4gW107XG4gIH1cbiAgcmV0dXJuIHJvd3M7XG59XG5cbmV4cG9ydCBjb25zdCBnZXRGaWx0ZXJlZFJvd3MgPSAoc3RhdGUpID0+IHtcbiAgY29uc3QgeyBmaWx0ZXJlZFJvd3MgfSA9IHN0YXRlO1xuICBpZiAoIUFycmF5LmlzQXJyYXkoZmlsdGVyZWRSb3dzKSkge1xuICAgIGJhZFR5cGUoJ2dldEZpbHRlcmVkUm93cycsICdmaWx0ZXJlZFJvd3MnLCAnYXJyYXknLCB0eXBlb2YgZmlsdGVyZWRSb3dzKTtcbiAgICByZXR1cm4gW107XG4gIH1cbiAgcmV0dXJuIGZpbHRlcmVkUm93cztcbn1cblxuZXhwb3J0IGNvbnN0IGdldENvbHVtbnMgPSAoc3RhdGUpID0+IHtcbiAgY29uc3QgeyBjb2x1bW5zIH0gPSBzdGF0ZTtcbiAgaWYgKCFBcnJheS5pc0FycmF5KGNvbHVtbnMpKSB7XG4gICAgYmFkVHlwZSgnZ2V0Q29sdW1ucycsICdjb2x1bW5zJywgJ2FycmF5JywgdHlwZW9mIGNvbHVtbnMpO1xuICAgIHJldHVybiBbXTtcbiAgfVxuICByZXR1cm4gY29sdW1ucztcbn1cblxuZXhwb3J0IGNvbnN0IGdldEFjdGlvbnMgPSAoc3RhdGUpID0+IHtcbiAgY29uc3QgeyBhY3Rpb25zIH0gPSBzdGF0ZTtcbiAgaWYgKCFBcnJheS5pc0FycmF5KGFjdGlvbnMpKSB7XG4gICAgYmFkVHlwZSgnZ2V0QWN0aW9ucycsICdhY3Rpb25zJywgJ2FycmF5JywgdHlwZW9mIGFjdGlvbnMpO1xuICAgIHJldHVybiBbXTtcbiAgfVxuICByZXR1cm4gYWN0aW9ucztcbn1cblxuZXhwb3J0IGNvbnN0IGdldE9wdGlvbnMgPSAoc3RhdGUpID0+IHtcbiAgY29uc3QgeyBvcHRpb25zIH0gPSBzdGF0ZTtcbiAgaWYgKHR5cGVvZiBvcHRpb25zICE9PSAnb2JqZWN0Jykge1xuICAgIGJhZFR5cGUoJ2dldE9wdGlvbnMnLCAnb3B0aW9ucycsICdvYmplY3QnLCB0eXBlb2Ygb3B0aW9ucyk7XG4gICAgcmV0dXJuIHt9O1xuICB9XG4gIHJldHVybiBvcHRpb25zO1xufVxuXG5leHBvcnQgY29uc3QgZ2V0RXZlbnRIYW5kbGVycyA9IChzdGF0ZSkgPT4ge1xuICBjb25zdCB7IGV2ZW50SGFuZGxlcnMgfSA9IHN0YXRlO1xuICBpZiAodHlwZW9mIGV2ZW50SGFuZGxlcnMgIT09ICdvYmplY3QnKSB7XG4gICAgYmFkVHlwZSgnZ2V0RXZlbnRIYW5kbGVycycsICdldmVudEhhbmRsZXJzJywgJ29iamVjdCcsIHR5cGVvZiBldmVudEhhbmRsZXJzKTtcbiAgICByZXR1cm4gW107XG4gIH1cbiAgcmV0dXJuIGV2ZW50SGFuZGxlcnM7XG59XG5cbmV4cG9ydCBjb25zdCBnZXRVaVN0YXRlID0gKHN0YXRlKSA9PiB7XG4gIGNvbnN0IHsgdWlTdGF0ZSB9ID0gc3RhdGU7XG4gIGlmICh0eXBlb2YgdWlTdGF0ZSAhPT0gJ29iamVjdCcpIHtcbiAgICBiYWRUeXBlKCdnZXRVaVN0YXRlJywgJ3VpU3RhdGUnLCAnb2JqZWN0JywgdHlwZW9mIHVpU3RhdGUpO1xuICAgIHJldHVybiB7fTtcbiAgfVxuICByZXR1cm4gdWlTdGF0ZTtcbn1cblxuLyogICAgR2VuZXJpYyBzdGF0ZSBcImNyZWF0ZVwiIGZ1bmN0aW9uICAgKi9cblxuZXhwb3J0IGNvbnN0IGNyZWF0ZSA9ICh7IHJvd3MsIGZpbHRlcmVkUm93cywgY29sdW1ucywgb3B0aW9ucywgYWN0aW9ucywgZXZlbnRIYW5kbGVycywgdWlTdGF0ZSB9LCBzdGF0ZSA9IHt9KSA9PiB7XG4gIHN0YXRlID0gc2V0Um93cyhzdGF0ZSwgcm93cyA/IHJvd3MgOiBbXSk7XG4gIHN0YXRlID0gc2V0Q29sdW1ucyhzdGF0ZSwgY29sdW1ucyA/IGNvbHVtbnMgOiBbXSk7XG4gIHN0YXRlID0gc2V0T3B0aW9ucyhzdGF0ZSwgb3B0aW9ucyA/IG9wdGlvbnMgOiB7fSk7XG4gIHN0YXRlID0gc2V0QWN0aW9ucyhzdGF0ZSwgYWN0aW9ucyA/IGFjdGlvbnMgOiBbXSk7XG4gIHN0YXRlID0gc2V0VWlTdGF0ZShzdGF0ZSwgdWlTdGF0ZSA/IHVpU3RhdGUgOiB7fSk7XG4gIHN0YXRlID0gc2V0RXZlbnRIYW5kbGVycyhzdGF0ZSwgZXZlbnRIYW5kbGVycyA/IGV2ZW50SGFuZGxlcnMgOiB7fSk7XG4gIHN0YXRlID0gc2V0RmlsdGVyZWRSb3dzKHN0YXRlLCBmaWx0ZXJlZFJvd3MgPyBmaWx0ZXJlZFJvd3MgOiByb3dzID8gcm93cyA6IFtdKTtcbiAgcmV0dXJuIHN0YXRlO1xufTtcblxuLyogICAgRGVlcGVyLCBtb3JlIHNwZWNpZmljIHNldHRlcnMgICAqL1xuXG5leHBvcnQgY29uc3Qgc2V0U2VsZWN0aW9uUHJlZGljYXRlID0gKHN0YXRlLCBwcmVkaWNhdGUpID0+IHtcbiAgaWYgKHR5cGVvZiBwcmVkaWNhdGUgIT09ICdmdW5jdGlvbicpXG4gICAgcmV0dXJuIGJhZFR5cGUoJ3NldFNlbGVjdGlvblByZWRpY2F0ZScsICdwcmVkaWNhdGUnLCAnZnVuY3Rpb24nLCB0eXBlb2YgcHJlZGljYXRlKSB8fCBzdGF0ZTtcbiAgY29uc3Qgb3B0aW9ucyA9IE9iamVjdC5hc3NpZ24oe30sIHN0YXRlLm9wdGlvbnMgPyBzdGF0ZS5vcHRpb25zIDoge30sIHsgaXNSb3dTZWxlY3RlZDogcHJlZGljYXRlIH0pO1xuICByZXR1cm4gT2JqZWN0LmFzc2lnbih7fSwgc3RhdGUsIHsgb3B0aW9ucyB9KTtcbn07XG5cbmV4cG9ydCBjb25zdCBzZXRTZWFyY2hRdWVyeSA9IChzdGF0ZSwgc2VhcmNoUXVlcnkpID0+IHtcbiAgaWYgKHR5cGVvZiBzZWFyY2hRdWVyeSAhPT0gJ3N0cmluZycgJiYgc2VhcmNoUXVlcnkgIT09IG51bGwpXG4gICAgcmV0dXJuIGJhZFR5cGUoJ3NldFNlYXJjaFF1ZXJ5JywgJ3NlYXJjaFF1ZXJ5JywgJ3N0cmluZycsIHR5cGVvZiBzZWFyY2hRdWVyeSkgfHwgc3RhdGU7XG5cbiAgY29uc3QgdWlTdGF0ZSA9IE9iamVjdC5hc3NpZ24oe30sIHN0YXRlLnVpU3RhdGUgPyBzdGF0ZS51aVN0YXRlIDoge30sIHsgc2VhcmNoUXVlcnkgfSk7XG4gIHJldHVybiBPYmplY3QuYXNzaWduKHt9LCBzdGF0ZSwgeyB1aVN0YXRlIH0pO1xufTtcblxuZXhwb3J0IGNvbnN0IHNldEVtcHRpbmVzc0N1bHByaXQgPSAoc3RhdGUsIGVtcHRpbmVzc0N1bHByaXQpID0+IHtcbiAgaWYgKHR5cGVvZiBlbXB0aW5lc3NDdWxwcml0ICE9PSAnc3RyaW5nJyAmJiBlbXB0aW5lc3NDdWxwcml0ICE9PSBudWxsKVxuICAgIHJldHVybiBiYWRUeXBlKCdzZXRFbXB0aW5lc3NDdWxwcml0JywgJ2VtcHRpbmVzc0N1bHByaXQnLCAnc3RyaW5nJywgdHlwZW9mIGVtcHRpbmVzc0N1bHByaXQpIHx8IHN0YXRlO1xuXG4gIGNvbnN0IHVpU3RhdGUgPSBPYmplY3QuYXNzaWduKHt9LCBzdGF0ZS51aVN0YXRlID8gc3RhdGUudWlTdGF0ZSA6IHt9LCB7IGVtcHRpbmVzc0N1bHByaXQgfSk7XG4gIHJldHVybiBPYmplY3QuYXNzaWduKHt9LCBzdGF0ZSwgeyB1aVN0YXRlIH0pO1xufTtcblxuZXhwb3J0IGNvbnN0IHNldFNvcnRDb2x1bW5LZXkgPSAoc3RhdGUsIGNvbHVtbktleSkgPT4ge1xuICBpZiAodHlwZW9mIGNvbHVtbktleSAhPT0gJ3N0cmluZycpXG4gICAgcmV0dXJuIGJhZFR5cGUoJ3NldFNvcnRDb2x1bW5LZXknLCAnY29sdW1uS2V5JywgJ3N0cmluZycsIHR5cGVvZiBjb2x1bW5LZXkpIHx8IHN0YXRlO1xuXG4gIGNvbnN0IGN1cnJlbnRVaVN0YXRlID0gT2JqZWN0LmFzc2lnbih7fSwgc3RhdGUudWlTdGF0ZSA/IHN0YXRlLnVpU3RhdGUgOiB7fSk7XG4gIGNvbnN0IHNvcnQgPSBPYmplY3QuYXNzaWduKHt9LCBjdXJyZW50VWlTdGF0ZS5zb3J0ID8gY3VycmVudFVpU3RhdGUuc29ydCA6IHt9LCB7IGNvbHVtbktleSB9KTtcbiAgY29uc3QgdWlTdGF0ZSA9IE9iamVjdC5hc3NpZ24oe30sIGN1cnJlbnRVaVN0YXRlLCB7IHNvcnQgfSk7XG4gIHJldHVybiBPYmplY3QuYXNzaWduKHt9LCBzdGF0ZSwgeyB1aVN0YXRlIH0pO1xufTtcblxuZXhwb3J0IGNvbnN0IHNldFNvcnREaXJlY3Rpb24gPSAoc3RhdGUsIGRpcmVjdGlvbikgPT4ge1xuICBpZiAodHlwZW9mIGRpcmVjdGlvbiAhPT0gJ3N0cmluZycpXG4gICAgcmV0dXJuIGJhZFR5cGUoJ3NldFNvcnREaXJlY3Rpb24nLCAnZGlyZWN0aW9uJywgJ3N0cmluZycsIHR5cGVvZiBkaXJlY3Rpb24pIHx8IHN0YXRlO1xuICBpZiAoIVsnYXNjJywgJ2Rlc2MnXS5pbmNsdWRlcyhkaXJlY3Rpb24pKVxuICAgIHJldHVybiBmYWlsKCdzZXRTb3J0RGlyZWN0aW9uJywgJ1wiZGlyZWN0aW9uXCIgbXVzdCBiZSBlaXRoZXIgXCJhc2NcIiBvciBcImRlc2NcIicsIFN5bnRheEVycm9yKSB8fCBzdGF0ZTtcblxuICBjb25zdCBjdXJyZW50VWlTdGF0ZSA9IE9iamVjdC5hc3NpZ24oe30sIHN0YXRlLnVpU3RhdGUgPyBzdGF0ZS51aVN0YXRlIDoge30pO1xuICBjb25zdCBzb3J0ID0gT2JqZWN0LmFzc2lnbih7fSwgY3VycmVudFVpU3RhdGUuc29ydCA/IGN1cnJlbnRVaVN0YXRlLnNvcnQgOiB7fSwgeyBkaXJlY3Rpb24gfSk7XG4gIGNvbnN0IHVpU3RhdGUgPSBPYmplY3QuYXNzaWduKHt9LCBjdXJyZW50VWlTdGF0ZSwgeyBzb3J0IH0pO1xuICByZXR1cm4gT2JqZWN0LmFzc2lnbih7fSwgc3RhdGUsIHsgdWlTdGF0ZSB9KTtcbn07XG5cbmV4cG9ydCBjb25zdCBtb3ZlQ29sdW1uVG9JbmRleCA9IChzdGF0ZSwgY29sdW1uS2V5LCB0b0luZGV4KSA9PiB7XG4gIGlmICh0eXBlb2YgY29sdW1uS2V5ICE9PSAnc3RyaW5nJylcbiAgICByZXR1cm4gYmFkVHlwZSgnY2hhbmdlQ29sdW1uSW5kZXgnLCAnXCJjb2x1bW5LZXlcIiBzaG91bGQgYmUgYSBzdHJpbmcuJywgVHlwZUVycm9yKTtcbiAgaWYgKHR5cGVvZiB0b0luZGV4ICE9PSAnbnVtYmVyJylcbiAgICByZXR1cm4gYmFkVHlwZSgnY2hhbmdlQ29sdW1uSW5kZXgnLCAnXCJ0b0luZGV4XCIgc2hvdWxkIGJlIGEgbnVtYmVyXCInLCBUeXBlRXJyb3IpO1xuICBpZiAoISdjb2x1bW5zJyBpbiBzdGF0ZSlcbiAgICByZXR1cm4gbWlzc2luZ0Zyb21TdGF0ZSgnY2hhbmdlQ29sdW1uSW5kZXgnLCAnY29sdW1ucycsIHN0YXRlKSAgfHwgc3RhdGU7XG5cbiAgY29uc3Qgb2xkQ29sdW1ucyA9IGdldENvbHVtbnMoc3RhdGUpO1xuICBjb25zdCBmcm9tSW5kZXggPSBvbGRDb2x1bW5zLmZpbmRJbmRleCgoeyBrZXkgfSkgPT4gY29sdW1uS2V5ID09PSBrZXkpO1xuICBpZiAoZnJvbUluZGV4IDwgMCkgcmV0dXJuIGZhaWwoJ2NoYW5nZUNvbHVtbkluZGV4JywgYGNvbHVtbiB3aXRoIGtleSBcIiR7Y29sdW1uS2V5fVwiIG5vdCBmb3VuZC5gKSB8fCBzdGF0ZTtcbiAgY29uc3QgY29sdW1ucyA9IHJlcG9zaXRpb25JdGVtSW5MaXN0KG9sZENvbHVtbnMsIGZyb21JbmRleCwgdG9JbmRleCk7XG4gIHJldHVybiBPYmplY3QuYXNzaWduKHt9LCBzdGF0ZSwgeyBjb2x1bW5zIH0pO1xufVxuXG5leHBvcnQgY29uc3QgY2FsbEFjdGlvbk9uU2VsZWN0ZWRSb3dzID0gKHN0YXRlLCBhY3Rpb24sIGJhdGNoID0gZmFsc2UsIG9ubHlGaWx0ZXJlZFJvd3MgPSB0cnVlKSA9PiB7XG4gIGlmICghJ3NlbGVjdGVkUm93cycgaW4gc3RhdGUpXG4gICAgcmV0dXJuIG1pc3NpbmdGcm9tU3RhdGUoJ2NhbGxBY3Rpb25PblNlbGVjdGVkUm93cycsICdzZWxlY3RlZFJvd3MnLCBzdGF0ZSkgfHwgc3RhdGU7XG4gIGlmICh0eXBlb2YgYWN0aW9uICE9PSAnZnVuY3Rpb24nKVxuICAgIHJldHVybiBiYWRUeXBlKCdjYWxsQWN0aW9uT25TZWxlY3RlZFJvd3MnLCAnYWN0aW9uJywgJ2Z1bmN0aW9uJywgdHlwZW9mIGFjdGlvbikgfHwgc3RhdGU7XG5cbiAgY29uc3Qgc2VsZWN0ZWRSb3dzID0gZ2V0U2VsZWN0ZWRSb3dzKHN0YXRlLCBvbmx5RmlsdGVyZWRSb3dzKTtcbiAgaWYgKGJhdGNoKSBhY3Rpb24oc2VsZWN0ZWRSb3dzKVxuICBlbHNlIHNlbGVjdGVkUm93cy5mb3JFYWNoKGFjdGlvbik7XG4gIHJldHVybiBzdGF0ZTtcbn07XG4iXX0=