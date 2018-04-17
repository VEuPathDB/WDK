export const initialState = {
  rows: [],
  columns: [],
  options: {},
  ui: {
    searchQuery: null,
    sort: {
      byColumn: null,
      ascending: true
    },
  }
};

export default function ReducerFactory (base = {}) {
  let startingState = Object.assign({}, initialState, base);

  return function Reducer (state = startingState, action = {}) {
    switch (action.type) {
      case 'UPDATE_OPTIONS': {
        let { options } = action;
        return Object.assign({}, state, { options });
      }

      case 'UPDATE_COLUMNS': {
        let { columns } = action;
        return Object.assign({}, state, { columns });
      }

      case 'UPDATE_ROWS': {
        let { rows } = action;
        return Object.assign({}, state, { rows });
      }

      case 'SET_COLUMN_WIDTH': {
        let { column, width } = action;
        let { columns } = state;
        let index = columns.indexOf(column);
        if (index < 0) return state;
        columns[index] = Object.assign({}, columns[index], { width });
        return Object.assign({}, state, { columns });
      }

      case 'TOGGLE_COLUMN_FILTER': {
        let { column } = action;
        let { columns } = state;
        if (!column.filterable) return state;

        let index = columns.indexOf(column);
        if (index < 0) return state;
        let { filter } = column;
        filter.enabled = !filter.enabled;
        columns[index] = Object.assign({}, columns[index], { filter });
        return Object.assign({}, state, { columns });
      }

      case 'TOGGLE_COLUMN_FILTER_VALUE': {
        let { column, value } = action;
        let { columns } = state;
        if (!column.filterable) return state;

        let index = columns.indexOf(column);
        if (index < 0) return state;
        let { filter } = column;
        if (filter.blacklist.includes(value)) {
          filter.blacklist.splice(filter.blacklist.indexOf(value), 1);
        } else {
          filter.blacklist.push(value);
        };
        columns[index] = Object.assign({}, columns[index], { filter });
        return Object.assign({}, state, { columns });
      }

      case 'SORT_BY_COLUMN': {
        let byColumn = action.column;
        let sort = Object.assign({}, state.ui.sort, { byColumn, ascending: true });
        let ui = Object.assign({}, state.ui, { sort });
        return Object.assign({}, state, { ui });
      }

      case 'TOGGLE_SORT_ORDER': {
        let { ascending } = state.ui.sort;
        let sort = Object.assign({}, state.ui.sort, { ascending: !ascending });
        let ui = Object.assign({}, state.ui, { sort });
        return Object.assign({}, state, { ui });
      }

      case 'HIDE_COLUMN': {
        let { column } = action;
        let { columns } = state;
        if (!column.hideable || column.hidden) return state;

        let index = columns.indexOf(column);
        let hidden = true;
        columns[index] = Object.assign({}, columns[index], { hidden });
        return Object.assign({}, state, { columns });
      }

      case 'SHOW_COLUMN': {
        let { column } = action;
        let { columns } = state;
        if (!column.hidden) return state;

        let index = columns.indexOf(column);
        let hidden = false;
        columns[index] = Object.assign({}, columns[index], { hidden });
        return Object.assign({}, state, { columns });
      }

      case 'SEARCH_BY_QUERY': {
        let { searchQuery } = action;
        if (!searchQuery || !searchQuery.length) searchQuery = null;
        let ui = Object.assign({}, state.ui, { searchQuery });
        return Object.assign({}, state, { ui })
      }

      default:
        return state;
    }
  };
};
