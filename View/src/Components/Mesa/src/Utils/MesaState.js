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
  selectRows (state, predicate) {

  },
  selectRowsByIndex (state, indexes) {

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
