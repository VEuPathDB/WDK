export const initialState = {
  rows: [],
  columns: [],
  options: {},
  ui: {
    emptinessCulprit: null,
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

      /* Updates -=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~ */
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

      /* COL WIDTH -=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=- */

      case 'SET_COLUMN_WIDTH': {
        let { column, width } = action;
        let { columns } = state;
        let index = columns.indexOf(column);
        if (index < 0) return state;
        columns[index] = Object.assign({}, columns[index], { width });
        return Object.assign({}, state, { columns });
      }

      /* FILTERS -=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~ */

      case 'TOGGLE_COLUMN_FILTER': {
        let { column } = action;
        let { columns } = state;
        if (!column.filterable) return state;

        let index = columns.findIndex(col => col.key === column.key);
        if (index < 0) return state;
        let { filterState } = column;
        filterState.enabled = !filterState.enabled;
        columns[index] = Object.assign({}, columns[index], { filterState });
        return Object.assign({}, state, { columns });
      }

      case 'TOGGLE_COLUMN_FILTER_VALUE': {
        let { column, value } = action;
        let { columns } = state;
        if (!column.filterable) return state;

        let index = columns.findIndex(col => col.key === column.key);
        if (index < 0) return state;
        let { filterState } = column;
        let { blacklist } = filterState;
        if (blacklist.includes(value)) blacklist = blacklist.filter(item => item !== value)
        else blacklist = [...blacklist, value];
        filterState = Object.assign({}, filterState, { blacklist });
        columns.splice(index, 1, Object.assign({}, column, { filterState }));
        return Object.assign({}, state, { columns });
      }

      case 'TOGGLE_COLUMN_FILTER_VISIBILITY': {
        let { column } = action;
        let { columns } = state;
        if (!column.filterable) return state;

        let index = columns.findIndex(col => col.key === column.key);
        if (index < 0) return state;
        let { filterState } = column;
        filterState.visible = !filterState.visible;
        columns[index] = Object.assign({}, columns[index], { filterState });
        return Object.assign({}, state, { columns });
      }

      case 'SET_COLUMN_BLACKLIST': {
        let { column, blacklist } = action;
        let { columns } = state;
        if (!column.filterable) return state;

        let index = columns.findIndex(col => col.key === column.key);
        if (index < 0) return state;
        let { filterState } = column;
        filterState = Object.assign({}, filterState, { blacklist });
        columns[index] = Object.assign({}, columns[index], { filterState });
        return Object.assign({}, state, { columns });
      }

      case 'DISABLE_ALL_COLUMN_FILTERS': {
        let { columns } = state;
        columns = columns.map(column => {
          let { filterable, filterState } = column;
          let enabled = false;
          if (!filterable) return column;
          filterState = Object.assign({}, filterState, { enabled });
          column = Object.assign({}, column, { filterState });
          return column;
        });
        return Object.assign({}, state, { columns });
      }

      case 'SET_EMPTINESS_CULPRIT': {
        let { emptinessCulprit } = action;
        let { ui } = state;
        ui = Object.assign({}, ui, { emptinessCulprit });
        return Object.assign({}, state, { ui });
      }

      /* COLUMN SORTING -=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~= */
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

      /* SHOW/HIDE COLUMN -=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-= */
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

      /* SEARCH -=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~= */
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
