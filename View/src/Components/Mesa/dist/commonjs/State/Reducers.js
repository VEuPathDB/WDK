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

      case 'SET_PAGINATED_ACTIVE_ITEM':
        {
          var activeItem = action.activeItem;
          var ui = state.ui;
          var _ui = ui,
              pagination = _ui.pagination;

          pagination = Object.assign({}, pagination, { activeItem: activeItem });
          ui = Object.assign({}, ui, { pagination: pagination });
          return Object.assign({}, state, { ui: ui });
        }

      case 'SET_PAGINATED_ITEMS_PER_PAGE':
        {
          var itemsPerPage = action.itemsPerPage;
          var _ui2 = state.ui;
          var _ui3 = _ui2,
              _pagination = _ui3.pagination;

          _pagination = Object.assign({}, _pagination, { itemsPerPage: itemsPerPage });
          _ui2 = Object.assign({}, _ui2, { pagination: _pagination });
          return Object.assign({}, state, { ui: _ui2 });
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
          var _ui4 = state.ui;

          _ui4 = Object.assign({}, _ui4, { emptinessCulprit: emptinessCulprit });
          return Object.assign({}, state, { ui: _ui4 });
        }

      /* COLUMN SORTING -=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~= */
      case 'SORT_BY_COLUMN':
        {
          var byColumn = action.column;
          var sort = Object.assign({}, state.ui.sort, { byColumn: byColumn, ascending: true });
          var _ui5 = Object.assign({}, state.ui, { sort: sort });
          return Object.assign({}, state, { ui: _ui5 });
        }

      case 'TOGGLE_SORT_ORDER':
        {
          var ascending = state.ui.sort.ascending;

          var _sort = Object.assign({}, state.ui.sort, { ascending: !ascending });
          var _ui6 = Object.assign({}, state.ui, { sort: _sort });
          return Object.assign({}, state, { ui: _ui6 });
        }

      /* SHOW/HIDE COLUMN -=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-=~=-= */
      case 'TOGGLE_COLUMN_EDITOR':
        {
          var _ui7 = state.ui;

          var columnEditorOpen = !_ui7.columnEditorOpen;
          _ui7 = Object.assign({}, _ui7, { columnEditorOpen: columnEditorOpen });
          return Object.assign({}, state, { ui: _ui7 });
        }

      case 'OPEN_COLUMN_EDITOR':
        {
          var _ui8 = state.ui;

          var _columnEditorOpen = true;
          _ui8 = Object.assign({}, _ui8, { columnEditorOpen: _columnEditorOpen });
          return Object.assign({}, state, { ui: _ui8 });
        }

      case 'CLOSE_COLUMN_EDITOR':
        {
          var _ui9 = state.ui;

          var _columnEditorOpen2 = false;
          _ui9 = Object.assign({}, _ui9, { columnEditorOpen: _columnEditorOpen2 });
          return Object.assign({}, state, { ui: _ui9 });
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
          var _ui10 = state.ui;

          if (!searchQuery || !searchQuery.length) searchQuery = null;
          var _ui11 = _ui10,
              _pagination2 = _ui11.pagination;

          _pagination2 = Object.assign({}, _pagination2, { activeItem: 1 });
          _ui10 = Object.assign({}, _ui10, { searchQuery: searchQuery }, { pagination: _pagination2 });
          return Object.assign({}, state, { ui: _ui10 });
        }

      case 'SELECT_ROW_BY_ID':
        {
          var id = action.id;
          var _ui12 = state.ui;
          var _ui13 = _ui12,
              selection = _ui13.selection;

          if (selection.includes(id)) return state;
          selection.push(id);
          _ui12 = Object.assign({}, _ui12, { selection: selection });
          return Object.assign({}, state, { ui: _ui12 });
        }

      case 'SELECT_ROWS_BY_IDS':
        {
          var ids = action.ids;
          var _ui14 = state.ui;
          var _ui15 = _ui14,
              _selection = _ui15.selection;

          var selectable = ids.filter(function (id) {
            return !_selection.includes(id);
          });
          _selection = [].concat(_toConsumableArray(_selection), _toConsumableArray(selectable));
          _ui14 = Object.assign({}, _ui14, { selection: _selection });
          return Object.assign({}, state, { ui: _ui14 });
        }

      case 'DESELECT_ROWS_BY_IDS':
        {
          var _ids = action.ids;
          var _ui16 = state.ui;
          var _ui17 = _ui16,
              _selection2 = _ui17.selection;

          _selection2 = _selection2.filter(function (id) {
            return !_ids.includes(id);
          });
          _ui16 = Object.assign({}, _ui16, { selection: _selection2 });
          return Object.assign({}, state, { ui: _ui16 });
        }

      case 'DESELECT_ROW_BY_ID':
        {
          var _id = action.id;
          var _ui18 = state.ui;
          var _ui19 = _ui18,
              _selection3 = _ui19.selection;

          var _index7 = _selection3.indexOf(_id);
          if (_index7 < 0) return state;
          _selection3.splice(_index7, 1);
          _ui18 = Object.assign({}, _ui18, { selection: _selection3 });
          return Object.assign({}, state, { ui: _ui18 });
        }

      case 'TOGGLE_ROW_SELECTION_BY_ID':
        {
          var _id2 = action.id;
          var _ui20 = state.ui;
          var _ui21 = _ui20,
              _selection4 = _ui21.selection;

          var _index8 = _selection4.indexOf(_id2);
          if (_index8 < 0) _selection4.push(_id2);else _selection4.splice(_index8, 1);
          _ui20 = Object.assign({}, _ui20, { selection: _selection4 });
          return Object.assign({}, state, { ui: _ui20 });
        }

      case 'SELECT_ALL_ROWS':
        {
          var _ui22 = state.ui,
              _rows = state.rows;
          var _ui23 = _ui22,
              _selection5 = _ui23.selection;

          _selection5 = _rows.map(function (row) {
            return row.__id;
          });
          _ui22 = Object.assign({}, _ui22, { selection: _selection5 });
          return Object.assign({}, state, { ui: _ui22 });
        }

      case 'CLEAR_ROW_SELECTION':
        {
          var _ui24 = state.ui;

          var _selection6 = [];
          _ui24 = Object.assign({}, _ui24, { selection: _selection6 });
          return Object.assign({}, state, { ui: _ui24 });
        }

      case 'RESET_UI_STATE':
        {
          var _ui25 = Object.assign({}, startingState.ui);
          return Object.assign({}, state, { ui: _ui25 });
        }

      default:
        return state;
    }
  };
};