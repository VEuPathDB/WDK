const InitialState = {
  rows: [],
  columns: [],
  options: {},
  ui: {
    columnEditorOpen: false,
    emptinessCulprit: null,
    searchQuery: null,
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
