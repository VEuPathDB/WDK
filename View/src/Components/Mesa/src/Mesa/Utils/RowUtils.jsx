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
  },

  filterRowsByColumns (rows, columns) {
    let filters = columns
      .filter(column => column.filterable && column.filterState.enabled)
      .map(({ key, filterState }) => {
        const { blacklist } = filterState;
        return { key, blacklist };
      });
    return rows.filter(row => {
      let result = !filters.some(({ key, blacklist }) => blacklist.includes(row[key]));
      return result;
    });
  },

  getRowsByPage (rows, page, options) {
    if (!rows || !page || !options) return rows;
    const { paginate, rowsPerPage } = options;
    if (!paginate || !rowsPerPage || typeof rowsPerPage !== 'number') return rows;
    if (rows.length <= rowsPerPage) return rows;
    let start = (rowsPerPage * (page - 1));
    return rows.slice(start, start + rowsPerPage);
  },

  getPageCount (rows, options) {
    if (!rows || !options) return 1;
    const { paginate, rowsPerPage } = options;
    if (!paginate || !rowsPerPage || typeof rowsPerPage !== 'number') return 1;
    if (rows.length <= rowsPerPage) return 1;
    else return Math.floor(rows.length / rowsPerPage) + (rows.length % rowsPerPage === 0 ? 0 : 1);
  },

  getSpanByPage (rows, page, options) {
    if (!rows || !page || !options) return [1, rows.length, rows.length];
    const { paginate, rowsPerPage } = options;
    if (!paginate || !rowsPerPage || typeof rowsPerPage !== 'number') return [1, rows.length, rows.length];
    if (rows.length <= rowsPerPage) return [1, rows.length, rows.length];
    let start = (rowsPerPage * (page - 1)) + 1;
    let end = (start + rowsPerPage - 1) > rows.length ? rows.length : (start + rowsPerPage - 1);
    let total = rows.length;
    return [ start, end, total ];
  }
};

export default RowUtils;
