'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = ReducerFactory;

var _InitialState = require('../State/InitialState');

var _InitialState2 = _interopRequireDefault(_InitialState);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _toConsumableArray(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = Array(arr.length); i < arr.length; i++) { arr2[i] = arr[i]; } return arr2; } else { return Array.from(arr); } }

function ReducerFactory() {
  var base = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};

  var startingState = Object.assign({}, _InitialState2.default, base);

  return function Reducer() {
    var state = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : startingState;
    var action = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : {};

    switch (action.type) {

      case 'SET_PAGINATION_ANCHOR':
        {
          var anchorIndex = action.anchorIndex;
          var uiState = state.uiState;
          var _uiState = uiState,
              paginationState = _uiState.paginationState;

          paginationState = Object.assign({}, paginationState, { anchorIndex: anchorIndex });
          uiState = Object.assign({}, uiState, { paginationState: paginationState });
          return Object.assign({}, state, { uiState: uiState });
        }

      case 'SET_PAGINATED_ITEMS_PER_PAGE':
        {
          var itemsPerPage = action.itemsPerPage;
          var _uiState2 = state.uiState;
          var _uiState3 = _uiState2,
              _paginationState = _uiState3.paginationState;

          _paginationState = Object.assign({}, _paginationState, { itemsPerPage: itemsPerPage });
          _uiState2 = Object.assign({}, _uiState2, { paginationState: _paginationState });
          return Object.assign({}, state, { uiState: _uiState2 });
        }

      /* Updates -=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~ */

      case 'UPDATE_OPTIONS':
        {
          var options = action.options;

          return Object.assign({}, state, { options: options });
        }

      case 'UPDATE_COLUMNS':
        {
          var columns = action.columns;

          return Object.assign({}, state, { columns: columns });
        }

      case 'UPDATE_ROWS':
        {
          var rows = action.rows;

          return Object.assign({}, state, { rows: rows });
        }

      case 'UPDATE_ACTIONS':
        {
          var actions = action.actions;

          return Object.assign({}, state, { actions: actions });
        }

      /* COL WIDTH -=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=- */

      case 'SET_COLUMN_WIDTH':
        {
          var column = action.column,
              width = action.width;
          var _columns = state.columns;

          var index = _columns.indexOf(column);
          if (index < 0) return state;
          _columns[index] = Object.assign({}, _columns[index], { width: width });
          return Object.assign({}, state, { columns: _columns });
        }

      /* FILTERS -=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~ */

      case 'TOGGLE_COLUMN_FILTER':
        {
          var _column = action.column;
          var _columns2 = state.columns;

          if (!_column.filterable) return state;

          var _index = _columns2.findIndex(function (col) {
            return col.key === _column.key;
          });
          if (_index < 0) return state;
          var filterState = _column.filterState;

          filterState.enabled = !filterState.enabled;
          _columns2[_index] = Object.assign({}, _columns2[_index], { filterState: filterState });
          return Object.assign({}, state, { columns: _columns2 });
        }

      case 'TOGGLE_COLUMN_FILTER_VALUE':
        {
          var _column2 = action.column,
              value = action.value;
          var _columns3 = state.columns;

          if (!_column2.filterable) return state;

          var _index2 = _columns3.findIndex(function (col) {
            return col.key === _column2.key;
          });
          console.log('TCFV: using index', _index2);
          if (_index2 < 0) return state;
          var _filterState = Object.assign({}, _column2.filterState);
          var blacklist = [].concat(_toConsumableArray(_filterState.blacklist));
          if (blacklist.includes(value)) blacklist = blacklist.filter(function (item) {
            return item !== value;
          });else blacklist = [].concat(_toConsumableArray(blacklist), [value]);
          _filterState = Object.assign({}, _filterState, { blacklist: blacklist });
          _columns3[_index2] = Object.assign({}, _column2, { filterState: _filterState });
          return Object.assign({}, state, { columns: _columns3 });
        }

      case 'TOGGLE_COLUMN_FILTER_VISIBILITY':
        {
          var _column3 = action.column;
          var _columns4 = state.columns;

          if (!_column3.filterable) return state;

          var _index3 = _columns4.findIndex(function (col) {
            return col.key === _column3.key;
          });
          console.log('TCFVis: using index', _index3);
          if (_index3 < 0) return state;
          var _filterState2 = Object.assign({}, _column3.filterState);
          _filterState2.visible = !_filterState2.visible;
          _columns4[_index3] = Object.assign({}, _columns4[_index3], { filterState: _filterState2 });
          return Object.assign({}, state, { columns: _columns4 });
        }

      case 'SET_COLUMN_BLACKLIST':
        {
          var _column4 = action.column,
              _blacklist = action.blacklist;
          var _columns5 = state.columns;

          if (!_column4.filterable) return state;

          var _index4 = _columns5.findIndex(function (col) {
            return col.key === _column4.key;
          });
          if (_index4 < 0) return state;
          var _filterState3 = _column4.filterState;

          _filterState3 = Object.assign({}, _filterState3, { blacklist: _blacklist });
          _columns5[_index4] = Object.assign({}, _columns5[_index4], { filterState: _filterState3 });
          return Object.assign({}, state, { columns: _columns5 });
        }

      case 'DISABLE_ALL_COLUMN_FILTERS':
        {
          var _columns6 = state.columns;

          _columns6 = _columns6.map(function (column) {
            var _column5 = column,
                filterable = _column5.filterable,
                filterState = _column5.filterState;

            var enabled = false;
            if (!filterable) return column;
            filterState = Object.assign({}, filterState, { enabled: enabled });
            column = Object.assign({}, column, { filterState: filterState });
            return column;
          });
          return Object.assign({}, state, { columns: _columns6 });
        }

      case 'SET_EMPTINESS_CULPRIT':
        {
          var emptinessCulprit = action.emptinessCulprit;
          var _uiState4 = state.uiState;

          _uiState4 = Object.assign({}, _uiState4, { emptinessCulprit: emptinessCulprit });
          return Object.assign({}, state, { uiState: _uiState4 });
        }

      /* COLUMN SORTING -=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~= */
      case 'SORT_BY_COLUMN':
        {
          var byColumn = action.column;
          var sort = Object.assign({}, state.uiState.sort, { byColumn: byColumn, ascending: true });
          var _uiState5 = Object.assign({}, state.uiState, { sort: sort });
          return Object.assign({}, state, { uiState: _uiState5 });
        }

      case 'TOGGLE_SORT_ORDER':
        {
          var ascending = state.uiState.sort.ascending;

          var _sort = Object.assign({}, state.uiState.sort, { ascending: !ascending });
          var _uiState6 = Object.assign({}, state.uiState, { sort: _sort });
          return Object.assign({}, state, { uiState: _uiState6 });
        }

      /* SHOW/HIDE COLUMN -=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-= */
      case 'TOGGLE_COLUMN_EDITOR':
        {
          var _uiState7 = state.uiState;

          var columnEditorOpen = !_uiState7.columnEditorOpen;
          _uiState7 = Object.assign({}, _uiState7, { columnEditorOpen: columnEditorOpen });
          return Object.assign({}, state, { uiState: _uiState7 });
        }

      case 'OPEN_COLUMN_EDITOR':
        {
          var _uiState8 = state.uiState;

          var _columnEditorOpen = true;
          _uiState8 = Object.assign({}, _uiState8, { columnEditorOpen: _columnEditorOpen });
          return Object.assign({}, state, { uiState: _uiState8 });
        }

      case 'CLOSE_COLUMN_EDITOR':
        {
          var _uiState9 = state.uiState;

          var _columnEditorOpen2 = false;
          _uiState9 = Object.assign({}, _uiState9, { columnEditorOpen: _columnEditorOpen2 });
          return Object.assign({}, state, { uiState: _uiState9 });
        }

      case 'HIDE_COLUMN':
        {
          var _column6 = action.column;
          var _columns7 = state.columns;

          if (!_column6.hideable || _column6.hidden) return state;

          var _index5 = _columns7.indexOf(_column6);
          var hidden = true;
          _columns7[_index5] = Object.assign({}, _columns7[_index5], { hidden: hidden });
          return Object.assign({}, state, { columns: _columns7 });
        }

      case 'SHOW_COLUMN':
        {
          var _column7 = action.column;
          var _columns8 = state.columns;

          if (!_column7.hidden) return state;

          var _index6 = _columns8.indexOf(_column7);
          var _hidden = false;
          _columns8[_index6] = Object.assign({}, _columns8[_index6], { hidden: _hidden });
          return Object.assign({}, state, { columns: _columns8 });
        }

      /* SEARCH -=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~= */
      case 'SEARCH_BY_QUERY':
        {
          var searchQuery = action.searchQuery;
          var _uiState10 = state.uiState;

          if (!searchQuery || !searchQuery.length) searchQuery = null;
          var _uiState11 = _uiState10,
              _paginationState2 = _uiState11.paginationState;

          _paginationState2 = Object.assign({}, _paginationState2, { anchorIndex: 1 });
          _uiState10 = Object.assign({}, _uiState10, { searchQuery: searchQuery }, { paginationState: _paginationState2 });
          return Object.assign({}, state, { uiState: _uiState10 });
        }

      case 'SELECT_ROW_BY_ID':
        {
          var id = action.id;
          var _uiState12 = state.uiState;
          var _uiState13 = _uiState12,
              selection = _uiState13.selection;

          if (selection.includes(id)) return state;
          selection.push(id);
          _uiState12 = Object.assign({}, _uiState12, { selection: selection });
          return Object.assign({}, state, { uiState: _uiState12 });
        }

      case 'SELECT_ROWS_BY_IDS':
        {
          var ids = action.ids;
          var _uiState14 = state.uiState;
          var _uiState15 = _uiState14,
              _selection = _uiState15.selection;

          var selectable = ids.filter(function (id) {
            return !_selection.includes(id);
          });
          _selection = [].concat(_toConsumableArray(_selection), _toConsumableArray(selectable));
          _uiState14 = Object.assign({}, _uiState14, { selection: _selection });
          return Object.assign({}, state, { uiState: _uiState14 });
        }

      case 'DESELECT_ROWS_BY_IDS':
        {
          var _ids = action.ids;
          var _uiState16 = state.uiState;
          var _uiState17 = _uiState16,
              _selection2 = _uiState17.selection;

          _selection2 = _selection2.filter(function (id) {
            return !_ids.includes(id);
          });
          _uiState16 = Object.assign({}, _uiState16, { selection: _selection2 });
          return Object.assign({}, state, { uiState: _uiState16 });
        }

      case 'DESELECT_ROW_BY_ID':
        {
          var _id = action.id;
          var _uiState18 = state.uiState;
          var _uiState19 = _uiState18,
              _selection3 = _uiState19.selection;

          var _index7 = _selection3.indexOf(_id);
          if (_index7 < 0) return state;
          _selection3.splice(_index7, 1);
          _uiState18 = Object.assign({}, _uiState18, { selection: _selection3 });
          return Object.assign({}, state, { uiState: _uiState18 });
        }

      case 'TOGGLE_ROW_SELECTION_BY_ID':
        {
          var _id2 = action.id;
          var _uiState20 = state.uiState;
          var _uiState21 = _uiState20,
              _selection4 = _uiState21.selection;

          var _index8 = _selection4.indexOf(_id2);
          if (_index8 < 0) _selection4.push(_id2);else _selection4.splice(_index8, 1);
          _uiState20 = Object.assign({}, _uiState20, { selection: _selection4 });
          return Object.assign({}, state, { uiState: _uiState20 });
        }

      case 'SELECT_ALL_ROWS':
        {
          var _uiState22 = state.uiState,
              _rows = state.rows;
          var _uiState23 = _uiState22,
              _selection5 = _uiState23.selection;

          _selection5 = _rows.map(function (row) {
            return row.__id;
          });
          _uiState22 = Object.assign({}, _uiState22, { selection: _selection5 });
          return Object.assign({}, state, { uiState: _uiState22 });
        }

      case 'CLEAR_ROW_SELECTION':
        {
          var _uiState24 = state.uiState;

          var _selection6 = [];
          _uiState24 = Object.assign({}, _uiState24, { selection: _selection6 });
          return Object.assign({}, state, { uiState: _uiState24 });
        }

      case 'RESET_UI_STATE':
        {
          var _uiState25 = Object.assign({}, startingState.uiState);
          return Object.assign({}, state, { uiState: _uiState25 });
        }

      default:
        return state;
    }
  };
};