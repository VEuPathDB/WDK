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
  className: null,
  inline: false,
  paginate: true,
  rowsPerPage: 20,
  fixedWidth: false,
  overflowHeight: '16em',
  searchPlaceholder: 'Search This Table',
};
