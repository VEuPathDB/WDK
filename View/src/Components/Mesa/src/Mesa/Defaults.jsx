  export const ColumnDefaults = {
  primary: false,
  searchable: true,
  sortable: true,
  editable: false,
  resizeable: true,
  truncated: true,

  filterable: false,
  filterState: {
    enabled: false,
    visible: false,
    blacklist: []
  },

  hideable: true,
  hidden: false,

  disabled: false,
  type: 'text'
};

export const OptionsDefaults = {
  title: null,
  toolbar: true,
  search: true,
  inline: false,
  className: null,
  paginate: false,
  rowsPerPage: 20,
  selectAllPages: false,
  editableColumns: true,
  overflowHeight: '16em',
  useDefaultActions: true,
  searchPlaceholder: 'Search This Table',
};
