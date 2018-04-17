import InitialState from 'Mesa/State/InitialState';

export default function ReducerFactory (base = {}) {
  let startingState = Object.assign({}, InitialState, base);

  return function Reducer (state = startingState, action = {}) {
    switch (action.type) {

      case 'SET_PAGINATED_ACTIVE_ITEM': {
        let { activeItem } = action;
        let { ui } = state;
        let { pagination } = ui;
        pagination = Object.assign({}, pagination, { activeItem });
        ui = Object.assign({}, ui, { pagination });
        return Object.assign({}, state, { ui });
      }

      case 'SET_PAGINATED_ITEMS_PER_PAGE': {
        let { itemsPerPage } = action;
        let { ui } = state;
        let { pagination } = ui;
        pagination = Object.assign({}, pagination, { itemsPerPage });
        ui = Object.assign({}, ui, { pagination });
        return Object.assign({}, state, { ui });
      }

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

      case 'UPDATE_ACTIONS': {
        let { actions } = action;
        return Object.assign({}, state, { actions });
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
        console.log('TCFV: using index', index);
        if (index < 0) return state;
        let filterState = Object.assign({}, column.filterState);
        let blacklist = [...filterState.blacklist];
        if (blacklist.includes(value)) blacklist = blacklist.filter(item => item !== value)
        else blacklist = [ ...blacklist, value ];
        filterState = Object.assign({}, filterState, { blacklist });
        columns[index] = Object.assign({}, column, { filterState });
        return Object.assign({}, state, { columns });
      }

      case 'TOGGLE_COLUMN_FILTER_VISIBILITY': {
        let { column } = action;
        let { columns } = state;
        if (!column.filterable) return state;

        let index = columns.findIndex(col => col.key === column.key);
        console.log('TCFVis: using index', index);
        if (index < 0) return state;
        let filterState = Object.assign({}, column.filterState);
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
      case 'TOGGLE_COLUMN_EDITOR': {
        let { ui } = state;
        let columnEditorOpen = !ui.columnEditorOpen;
        ui = Object.assign({}, ui, { columnEditorOpen });
        return Object.assign({}, state, { ui });
      }

      case 'OPEN_COLUMN_EDITOR': {
        let { ui } = state;
        let columnEditorOpen = true;
        ui = Object.assign({}, ui, { columnEditorOpen });
        return Object.assign({}, state, { ui });
      }

      case 'CLOSE_COLUMN_EDITOR': {
        let { ui } = state;
        let columnEditorOpen = false;
        ui = Object.assign({}, ui, { columnEditorOpen });
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

      /* SEARCH -=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~= */
      case 'SEARCH_BY_QUERY': {
        let { searchQuery } = action;
        let { ui } = state;
        if (!searchQuery || !searchQuery.length) searchQuery = null;
        let { pagination } = ui;
        pagination = Object.assign({}, pagination, { activeItem: 1 });
        ui = Object.assign({}, ui, { searchQuery }, { pagination });
        return Object.assign({}, state, { ui })
      }

      case 'SELECT_ROW_BY_ID': {
        let { id } = action;
        let { ui } = state;
        let { selection } = ui;
        if (selection.includes(id)) return state;
        selection.push(id);
        ui = Object.assign({}, ui, { selection });
        return Object.assign({}, state, { ui });
      }

      case 'SELECT_ROWS_BY_IDS': {
        let { ids } = action;
        let { ui } = state;
        let { selection } = ui;
        let selectable = ids.filter(id => !selection.includes(id));
        selection = [...selection, ...selectable];
        ui = Object.assign({}, ui, { selection });
        return Object.assign({}, state, { ui });
      }

      case 'DESELECT_ROWS_BY_IDS': {
        let { ids } = action;
        let { ui } = state;
        let { selection } = ui;
        selection = selection.filter(id => !ids.includes(id));
        ui = Object.assign({}, ui, { selection });
        return Object.assign({}, state, { ui });
      }

      case 'DESELECT_ROW_BY_ID': {
        let { id } = action;
        let { ui } = state;
        let { selection } = ui;
        let index = selection.indexOf(id);
        if (index < 0) return state;
        selection.splice(index, 1);
        ui = Object.assign({}, ui, { selection });
        return Object.assign({}, state, { ui });
      }

      case 'TOGGLE_ROW_SELECTION_BY_ID': {
        let { id } = action;
        let { ui } = state;
        let { selection } = ui;
        let index = selection.indexOf(id);
        if (index < 0) selection.push(id);
        else selection.splice(index, 1);
        ui = Object.assign({}, ui, { selection });
        return Object.assign({}, state, { ui });
      }

      case 'SELECT_ALL_ROWS': {
        let { ui, rows } = state;
        let { selection } = ui;
        selection = rows.map(row => row.__id);
        ui = Object.assign({}, ui, { selection });
        return Object.assign({}, state, { ui });
      }

      case 'CLEAR_ROW_SELECTION': {
        let { ui } = state;
        let selection = []
        ui = Object.assign({}, ui, { selection });
        return Object.assign({}, state, { ui });
      }

      case 'RESET_UI_STATE': {
        let ui = Object.assign({}, startingState.ui);
        return Object.assign({}, state, { ui });
      }

      default:
        return state;
    }
  };
};
