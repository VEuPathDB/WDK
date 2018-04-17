'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

exports.updateRows = updateRows;
exports.resetUiState = resetUiState;
exports.toggleColumnEditor = toggleColumnEditor;
exports.openColumnEditor = openColumnEditor;
exports.closeColumnEditor = closeColumnEditor;
exports.selectRowById = selectRowById;
exports.selectRowsByIds = selectRowsByIds;
exports.deselectRowById = deselectRowById;
exports.deselectRowsByIds = deselectRowsByIds;
exports.toggleRowSelectionById = toggleRowSelectionById;
exports.selectAllRows = selectAllRows;
exports.clearRowSelection = clearRowSelection;
exports.setPaginatedActiveItem = setPaginatedActiveItem;
exports.setPaginatedItemsPerPage = setPaginatedItemsPerPage;
exports.updateColumns = updateColumns;
exports.updateOptions = updateOptions;
exports.updateActions = updateActions;
exports.sortByColumn = sortByColumn;
exports.setColumnWidth = setColumnWidth;
exports.toggleSortOrder = toggleSortOrder;
exports.toggleColumnFilter = toggleColumnFilter;
exports.toggleColumnFilterVisibility = toggleColumnFilterVisibility;
exports.toggleColumnFilterValue = toggleColumnFilterValue;
exports.disableAllColumnFilters = disableAllColumnFilters;
exports.setColumnBlackList = setColumnBlackList;
exports.setEmptinessCulprit = setEmptinessCulprit;
exports.searchByQuery = searchByQuery;
exports.hideColumn = hideColumn;
exports.showColumn = showColumn;
function updateRows(rows) {
  if (!rows || !Array.isArray(rows)) rows = [];
  return { type: 'UPDATE_ROWS', rows: rows };
};

function resetUiState() {
  return { type: 'RESET_UI_STATE' };
}

function toggleColumnEditor() {
  return { type: 'TOGGLE_COLUMN_EDITOR' };
}

function openColumnEditor() {
  return { type: 'OPEN_COLUMN_EDITOR' };
}

function closeColumnEditor() {
  return { type: 'CLOSE_COLUMN_EDITOR' };
}

function selectRowById(id) {
  return { type: 'SELECT_ROW_BY_ID', id: id };
};

function selectRowsByIds(ids) {
  return { type: 'SELECT_ROWS_BY_IDS', ids: ids };
}

function deselectRowById(id) {
  return { type: 'DESELCT_ROW_BY_ID', id: id };
}

function deselectRowsByIds(ids) {
  return { type: 'DESELECT_ROWS_BY_IDS', ids: ids };
}

function toggleRowSelectionById(id) {
  return { type: 'TOGGLE_ROW_SELECTION_BY_ID', id: id };
}

function selectAllRows() {
  return { type: 'SELECT_ALL_ROWS' };
}

function clearRowSelection() {
  return { type: 'CLEAR_ROW_SELECTION' };
}

function setPaginatedActiveItem(activeItem) {
  if (typeof activeItem !== 'number' || activeItem <= 0) return;
  return { type: 'SET_PAGINATED_ACTIVE_ITEM', activeItem: activeItem };
}

function setPaginatedItemsPerPage(itemsPerPage) {
  if (typeof itemsPerPage !== 'number' || itemsPerPage <= 0) return;
  return { type: 'SET_PAGINATED_ITEMS_PER_PAGE', itemsPerPage: itemsPerPage };
}

function updateColumns(columns) {
  if (!columns || !Array.isArray(columns)) columns = [];
  return { type: 'UPDATE_COLUMNS', columns: columns };
};

function updateOptions(options) {
  if (!options || (typeof options === 'undefined' ? 'undefined' : _typeof(options)) !== 'object') options = {};
  return { type: 'UPDATE_OPTIONS', options: options };
};

function updateActions(actions) {
  if (!actions || !Array.isArray(actions)) actions = [];
  return { type: 'UPDATE_ACTIONS', actions: actions };
}

function sortByColumn(column) {
  if (!column) return {};
  return { type: 'SORT_BY_COLUMN', column: column };
};

function setColumnWidth(column, width) {
  if (!column || typeof width !== 'number') return {};
  return { type: 'SET_COLUMN_WIDTH', column: column, width: width };
};

function toggleSortOrder() {
  return { type: 'TOGGLE_SORT_ORDER' };
};

function toggleColumnFilter(column) {
  if (!column) return {};
  return { type: 'TOGGLE_COLUMN_FILTER', column: column };
};

function toggleColumnFilterVisibility(column) {
  if (!column) return {};
  return { type: 'TOGGLE_COLUMN_FILTER_VISIBILITY', column: column };
};

function toggleColumnFilterValue(column, value) {
  if (!column || !value) return {};
  return { type: 'TOGGLE_COLUMN_FILTER_VALUE', column: column, value: value };
};

function disableAllColumnFilters() {
  return { type: 'DISABLE_ALL_COLUMN_FILTERS' };
}

function setColumnBlackList(column, blacklist) {
  if (!column || !Array.isArray(blacklist)) return {};
  return { type: 'SET_COLUMN_BLACKLIST', column: column, blacklist: blacklist };
};

function setEmptinessCulprit(emptinessCulprit) {
  if (typeof emptinessCulprit !== 'string' || !emptinessCulprit.length) emptinessCulprit = null;
  return { type: 'SET_EMPTINESS_CULPRIT', emptinessCulprit: emptinessCulprit };
}

function searchByQuery(searchQuery) {
  return { type: 'SEARCH_BY_QUERY', searchQuery: searchQuery };
};

function hideColumn(column) {
  if (!column) return;
  return { type: 'HIDE_COLUMN', column: column };
};

function showColumn(column) {
  if (!column) return;
  return { type: 'SHOW_COLUMN', column: column };
};