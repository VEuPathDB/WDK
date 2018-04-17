import Utils from 'Mesa/Utils/Utils';

const RowUtils = {
  searchRowsForQuery (rows, columns, searchQuery) {
    if (!searchQuery || !rows || !rows.length) return rows;
    let searchableKeys = columns.filter(col => col.searchable).map(col => col.key);
    return rows.filter(row => {
      let searchable = {};
      searchableKeys.forEach(key => key in row ? searchable[key] = row[key] : null);
      searchable = Utils.stringValue(searchable);
      return Utils.objectContainsQuery(searchable, searchQuery);
    });
  },

  sortRowsByColumn (rows, byColumn, ascending) {
    if (!byColumn || !rows || !rows.length) return rows;
    if (byColumn.sortable) {
      switch (byColumn.type) {
        case 'number':
        case 'numeric':
          rows = Utils.numberSort(rows, byColumn.key, ascending);
          break;
        case 'html':
        case 'text':
        default:
          rows = Utils.textSort(rows, byColumn.key, ascending);
      }
    }
    return rows;
  }
};

export default RowUtils;
