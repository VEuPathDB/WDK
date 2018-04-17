export const initialState = {
  searchQuery: null,
  filter: {
    byColumn: null,
    valueWhitelist: []
  },
  sort: {
    byColumn: null,
    ascending: true
  },
  selectedRows: [],
  hiddenColumns: [],
  columnWidths: {}
};

export function Reducers (state = initialState, action = {}) {
  switch (action.type) {

    case 'SET_COLUMN_WIDTH': {
      let { column, width } = action;
      let { key } = column;
      let { columnWidths } = state;
      if (!key || typeof width !== 'number') return state;
      columnWidths = Object.assign({}, columnWidths, { [key]: width });
      return Object.assign({}, state, { columnWidths });
    }

    case 'FILTER_BY_COLUMN_VALUES': {
      let byColumn = action.column;
      let valueWhitelist = action.values;
      let filter = Object.assign({}, state.sort, { byColumn, valueWhitelist });
      return Object.assign({}, state, { filter });
    }

    case 'TOGGLE_COLUMN_FILTER_VALUE': {
      let { value } = action;
      let { valueWhitelist } = state.filter;
      if (valueWhitelist.includes(value)) {
        valueWhitelist.splice(valueWhitelist.indexOf(value), 1);
      } else {
        valueWhitelist.push(value);
      };
      let filter = Object.assign({}, state.filter, { valueWhitelist });
      return Object.assign({}, state, { filter });
    }

    case 'SORT_BY_COLUMN': {
      let byColumn = action.column;
      let sort = Object.assign({}, state.sort, { byColumn, ascending: true });
      return Object.assign({}, state, { sort });
    }

    case 'TOGGLE_SORT_ORDER': {
      let { ascending } = state.sort;
      let sort = Object.assign({}, state.sort, { ascending: !ascending });
      return Object.assign({}, state, { sort });
    }

    case 'SET_SORT_ORDER': {
      let { ascending } = action;
      let sort = Object.assign({}, state.sort, { ascending });
      return Object.assign({}, state, { sort });
    }

    case 'HIDE_COLUMN': {
      let { column } = action;
      let isNew = state.hiddenColumns.indexOf(column) < 0;
      if (!isNew) return state;
      let { hiddenColumns } = state;
      hiddenColumns.push(column);
      return Object.assign({}, state, { hiddenColumns });
    }

    case 'SHOW_COLUMN': {
      let { column } = action;
      let index = state.hiddenColumns.indexOf(column);
      let isNew = index < 0;
      if (isNew) return state;
      let { hiddenColumns } = state;
      hiddenColumns.splice(index, 1);
      return Object.assign({}, state, { hiddenColumns });
    }

    case 'SEARCH_BY_QUERY': {
      let { searchQuery } = action;
      if (!searchQuery || !searchQuery.length) searchQuery = null;
      return Object.assign({}, state, { searchQuery })
    }

    default:
      return state;
  }
};

export default Reducers;
