const InitialState = {
  rows: [],
  columns: [],
  options: {},
  actions: [],
  uiState: {
    columnEditorOpen: false,
    emptinessCulprit: null,
    searchQuery: null,
    selection: [],
    sort: {
      byColumn: null,
      ascending: true
    },
    paginationState: {
      anchorIndex: 1,
      itemsPerPage: 20
    }
  }
};

export default InitialState;
