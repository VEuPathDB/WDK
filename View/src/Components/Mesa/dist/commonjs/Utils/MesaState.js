'use strict';

// Mesa State Reducers

var MesaState = {
  setRows: function setRows(state, rows) {
    if (!Array.isArray(rows)) return state;
    return Object.assign({}, state, { rows: rows });
  },
  setFilteredRows: function setFilteredRows(state, filteredRows) {},
  setColumns: function setColumns(state) {},
  setActions: function setActions(state) {},
  selectRows: function selectRows(state, predicate) {},
  selectRowsByIndex: function selectRowsByIndex(state, indexes) {},
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