export function updateRows (rows) {
  if (!rows || !Array.isArray(rows)) rows = [];
  return { type: 'UPDATE_ROWS', rows };
};

export function resetUiState () {
  return { type: 'RESET_UI_STATE' };
}

export function toggleColumnEditor () {
  return { type: 'TOGGLE_COLUMN_EDITOR' };
}

export function openColumnEditor () {
  return { type: 'OPEN_COLUMN_EDITOR' };
}

export function closeColumnEditor () {
  return { type: 'CLOSE_COLUMN_EDITOR' };
}

export function selectRowById (id) {
  return { type: 'SELECT_ROW_BY_ID', id };
};

export function selectRowsByIds (ids) {
  return { type: 'SELECT_ROWS_BY_IDS', ids };
}

export function deselectRowById (id) {
  return { type: 'DESELCT_ROW_BY_ID', id };
}

export function deselectRowsByIds (ids) {
  return { type: 'DESELECT_ROWS_BY_IDS', ids };
}

export function toggleRowSelectionById (id) {
  return { type: 'TOGGLE_ROW_SELECTION_BY_ID', id };
}

export function selectAllRows () {
  return { type: 'SELECT_ALL_ROWS' };
}

export function clearRowSelection () {
  return { type: 'CLEAR_ROW_SELECTION' };
}

export function setPaginatedActiveItem (activeItem) {
  if (typeof activeItem !== 'number' || activeItem <= 0) return;
  return { type: 'SET_PAGINATED_ACTIVE_ITEM', activeItem };
}

export function setPaginatedItemsPerPage (itemsPerPage) {
  if (typeof itemsPerPage !== 'number' || itemsPerPage <= 0) return;
  return { type: 'SET_PAGINATED_ITEMS_PER_PAGE', itemsPerPage };
}

export function updateColumns (columns) {
  if (!columns || !Array.isArray(columns)) columns = [];
  return { type: 'UPDATE_COLUMNS', columns };
};

export function updateOptions (options) {
  if (!options || typeof options !== 'object') options = {};
  return { type: 'UPDATE_OPTIONS', options };
};

export function updateActions (actions) {
  if (!actions || !Array.isArray(actions)) actions = [];
  return { type: 'UPDATE_ACTIONS', actions };
}

export function sortByColumn (column) {
  if (!column) return {};
  return { type: 'SORT_BY_COLUMN', column };
};

export function setColumnWidth (column, width) {
  if (!column || typeof width !== 'number') return {};
  return { type: 'SET_COLUMN_WIDTH', column, width };
};

export function toggleSortOrder () {
  return { type: 'TOGGLE_SORT_ORDER' };
};

export function toggleColumnFilter (column) {
  if (!column) return {};
  return { type: 'TOGGLE_COLUMN_FILTER', column };
};

export function toggleColumnFilterVisibility (column) {
  if (!column) return {};
  return { type: 'TOGGLE_COLUMN_FILTER_VISIBILITY', column };
};

export function toggleColumnFilterValue (column, value) {
  if (!column || !value) return {};
  return { type: 'TOGGLE_COLUMN_FILTER_VALUE', column, value };
};

export function disableAllColumnFilters () {
  return { type: 'DISABLE_ALL_COLUMN_FILTERS' };
}

export function setColumnBlackList (column, blacklist) {
  if (!column || !Array.isArray(blacklist)) return {};
  return { type: 'SET_COLUMN_BLACKLIST', column, blacklist };
};

export function setEmptinessCulprit (emptinessCulprit) {
  if (typeof emptinessCulprit !== 'string' || !emptinessCulprit.length) emptinessCulprit = null;
  return { type: 'SET_EMPTINESS_CULPRIT', emptinessCulprit };
}

export function searchByQuery (searchQuery) {
  return { type: 'SEARCH_BY_QUERY', searchQuery };
};

export function hideColumn (column) {
  if (!column) return;
  return { type: 'HIDE_COLUMN', column };
};

export function showColumn (column) {
  if (!column) return;
  return { type: 'SHOW_COLUMN', column };
};
