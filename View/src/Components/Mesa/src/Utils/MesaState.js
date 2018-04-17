const badType = (fn, parameter, expected, actual) => {
  throw new TypeError(`<${fn}>: parameter "${pararmeter}"  is not of type ${expected} (got ${actual})`);
}

// Mesa State Reducers

const MesaState = {
  setRows (state, rows) {
    if (!Array.isArray(rows)) {
        badType('setRows', 'rows', 'array', typeof rows);
      return state;
    }
    return Object.assign({}, state, { rows });
  },
  setFilteredRows (state, filteredRows) {
    if (!Array.isArray(filteredRows)) return state;
    return Object.assign({}, state, { filteredRows });
  },
  setColumns (state, columns) {
    if (!Array.isArray(columns)) return state;
    return Object.assign({}, state, { columns });
  },
  setActions (state, actions) {
    if (!Array.isArray(actions)) return state;
    return Object.assign({}, state, { actions });
  },
  setSelectedRows (state, selectedRows) {
    if (!Array.isArray(selectedRows)) return state;
    return Object.assign({}, state, { filteredRows });
  },
  selectRows (state, predicate) {
    if (typeof predicate !== 'function') return state;
    const { rows } = state;
    const selectedRows = rows.filter(predicate);
    const uiState = Object.assign({}, state.uiState ? state.uiState : {}, { selectedRows });
    return Object.assign({}, state, { uiState });
  },
  selectRowsByIndex (state, indexes) {
    if (!Array.isArray(indexes) || !indexes.every(index => typeof index === 'number')) return state;
    
  },
  setSearchQuery (state, query) {

  },
  setPage (state, pageNumber) {

  },
  setSortColumnKey (state, columnKey) {

  },
  setSortDirection (state, direction) {
    const validDirections = [ 'asc', 'desc' ];
  },
  deselectAllRows (state) {

  },
  selectAllRows (state) {

  },
  callActionOnSelectedRows (state, action) {

  },
  showColumnByKey (state, columnKey) {

  },
  hideColumnByKey (state, columnKey) {

  },
};

// Event hooks
