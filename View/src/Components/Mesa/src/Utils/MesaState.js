// Mesa State Reducers

const MesaState = {
  setRows (state, rows) {
    if (!Array.isArray(rows)) return state;
    return Object.assign({}, state, { rows });
  },
  setFilteredRows (state, filteredRows) {

  },
  setColumns (state) {

  },
  setActions (state) {

  },

}
