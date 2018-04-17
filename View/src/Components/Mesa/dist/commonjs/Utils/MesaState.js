"use strict";

// Mesa State Reducers

var MesaState = {
  setRows: function setRows(state, rows) {
    if (!Array.isArray(rows)) return state;
    return Object.assign({}, state, { rows: rows });
  },
  setFilteredRows: function setFilteredRows(state, filteredRows) {},
  setColumns: function setColumns(state) {},
  setActions: function setActions(state) {}
};