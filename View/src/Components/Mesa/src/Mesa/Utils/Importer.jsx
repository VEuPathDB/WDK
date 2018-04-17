import Utils from 'Mesa/Utils/Utils';
import { ColumnDefaults, OptionsDefaults } from 'Mesa/Defaults';

const Importer = {
  homogenizeColumn (column = {}, rows = []) {
    if (!column || !column.key) throw new Error('Cannot homogenize a column without a `.key`');
    if (!column.type && rows.length) {
      let isHtmlColumn = rows.some(row => Utils.isHtml(row[column.key]));
      let isNumberColumn = rows.every(row => !row[column.key] || !row[column.key].length || Utils.isNumeric(column.key));
      if (isHtmlColumn) column.type = 'html';
      else if (isNumberColumn) column.type = 'number';
    }
    if (column.primary) column.hideable = false;
    return Object.assign({}, ColumnDefaults, column);
  },

  columnsFromRows (rows = []) {
    const keys = [];
    if (!Array.isArray(rows)) return [];
    rows.forEach(row => Object.keys(row).forEach(prop => keys.indexOf(prop) < 0 && keys.push(prop)));
    return keys.map(key => Importer.homogenizeColumn({ key }, rows));
  },

  processColumns (columns = []) {
    if (!Array.isArray(columns)) return null;
    return columns
      .map(Importer.homogenizeColumn)
      .filter(col => !col.disabled);
  },

  columnsFromMap (map = {}) {
    let columns = [];
    for (let key in map) {
      let column = { key };
      let value = map[key];
      switch (typeof value) {
        case 'string':
          column.name = value;
        case 'object':
          column = Object.assign({}, column, value);
      }
      columns.push(column);
    };
    return Importer.processColumns(columns);
  },

  importColumns (columns, rows) {
    if (!columns || typeof columns !== 'object' || (Array.isArray(columns) && !columns.length)) {
      if (Array.isArray(rows) && rows.length) return Importer.columnsFromRows(rows);
      else throw new Error(`Couldn't import columns, no valid cols or rows provided.`);
    } else {
      if (!Array.isArray(columns)) return Importer.columnsFromMap(columns);
      return Importer.processColumns(columns);
    }
  },

  importOptions (options = {}) {
    return Object.assign({}, OptionsDefaults, options);
  }
};

export default Importer;
