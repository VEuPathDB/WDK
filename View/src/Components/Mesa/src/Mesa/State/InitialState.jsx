const InitialState = {
  rows: [],
  columns: [],
  options: {},
  actions: [],
  ui: {
    columnEditorOpen: false,
    emptinessCulprit: null,
    searchQuery: null,
    selection: [],
    sort: {
      byColumn: null,
      ascending: true
    },
    pagination: {
      activeItem: 1,
      itemsPerPage: 20
    }
  }
};

export default InitialState;
