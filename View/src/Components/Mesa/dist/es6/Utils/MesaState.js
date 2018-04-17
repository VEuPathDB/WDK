'use strict';

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

var badType = function badType(fn, parameter, expected, actual) {
  throw new TypeError('<' + fn + '>: parameter "' + pararmeter + '"  is not of type ' + expected + ' (got ' + actual + ')');
};

// Mesa State Reducers

var MesaState = {
  setRows: function setRows(state, rows) {
    if (!Array.isArray(rows)) {
      badType('setRows', 'rows', 'array', typeof rows === 'undefined' ? 'undefined' : _typeof(rows));
      return state;
    }
    return Object.assign({}, state, { rows: rows });
  },
  setFilteredRows: function setFilteredRows(state, filteredRows) {
    if (!Array.isArray(filteredRows)) return state;
    return Object.assign({}, state, { filteredRows: filteredRows });
  },
  setColumns: function setColumns(state, columns) {
    if (!Array.isArray(columns)) return state;
    return Object.assign({}, state, { columns: columns });
  },
  setActions: function setActions(state, actions) {
    if (!Array.isArray(actions)) return state;
    return Object.assign({}, state, { actions: actions });
  },
  setSelectedRows: function setSelectedRows(state, selectedRows) {
    if (!Array.isArray(selectedRows)) return state;
    return Object.assign({}, state, { filteredRows: filteredRows });
  },
  selectRows: function selectRows(state, predicate) {
    if (typeof predicate !== 'function') return state;
    var rows = state.rows;

    var selectedRows = rows.filter(predicate);
    var uiState = Object.assign({}, state.uiState ? state.uiState : {}, { selectedRows: selectedRows });
    return Object.assign({}, state, { uiState: uiState });
  },
  selectRowsByIndex: function selectRowsByIndex(state, indexes) {
    if (!Array.isArray(indexes) || !indexes.every(function (index) {
      return typeof index === 'number';
    })) return state;
  },
  setSearchQuery: function setSearchQuery(state, query) {},
  setPage: function setPage(state, pageNumber) {},
  setSortColumnKey: function setSortColumnKey(state, columnKey) {},
  setSortDirection: function setSortDirection(state, direction) {
    var validDirections = ['asc', 'desc'];
  },
  deselectAllRows: function deselectAllRows(state) {},
  selectAllRows: function selectAllRows(state) {},
  callActionOnSelectedRows: function callActionOnSelectedRows(state, action) {},
  showColumnByKey: function showColumnByKey(state, columnKey) {},
  hideColumnByKey: function hideColumnByKey(state, columnKey) {}
};

// Event hooks