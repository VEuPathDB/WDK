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
  searchPlaceholder: 'Search This Table',
  toolbar: true,
  search: true,
  className: null,
  inline: false,
  fixedWidth: false,
  overflowHeight: '16em'
};
