export function sortByColumn (column) {
  if (!column) return;
  return { type: 'SORT_BY_COLUMN', column };
};

export function toggleSortOrder () {
  return { type: 'TOGGLE_SORT_ORDER' };
};

export function setSortOrder (ascending = true) {
  return { type: 'SET_SORT_ORDER', ascending };
};

export function filterByColumnValues (column, values) {
  if (!column || !values) return;
  return { type: 'FILTER_BY_COLUMN_VALUES', column, values };
}

export function toggleColumnFilterValue (value) {
  if (!value) return;
  return { type: 'TOGGLE_COLUMN_FILTER_VALUE', value };
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
