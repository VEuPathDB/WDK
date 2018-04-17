import Utils from 'Mesa/Utils/Utils';
import { ColumnDefaults, OptionsDefaults, ActionDefaults } from 'Mesa/Defaults';

const Importer = {
  homogenizeColumn (column = {}, rows = [], options = {}) {
    if (!column || !column.key) throw new Error('Cannot homogenize a column without a `.key`');
    if (!column.type && rows.length) {
      let isHtmlColumn = rows.some(row => Utils.isHtml(row[column.key]));
      let isNumberColumn = rows.every(row => !row[column.key] || !row[column.key].length || Utils.isNumeric(column.key));
      if (isHtmlColumn) column.type = 'html';
      else if (isNumberColumn) column.type = 'number';
    }
    if (column.primary) column.hideable = false;
    let optionalDefaults = (options && 'columnDefaults' in options) ? options.columnDefaults : {};
    return Object.assign({}, ColumnDefaults, optionalDefaults, column);
  },

  columnsFromRows (rows = [], options) {
    if (!Array.isArray(rows)) return [];
    const keys = Utils.keysInList(rows, ['__id']);
    return keys.map(key => Importer.homogenizeColumn({ key }, rows, options));
  },

  processColumns (columns, rows, options) {
    if (!Array.isArray(columns)) return null;
    return columns
      .map(column => Importer.homogenizeColumn(column, rows, options))
      .filter(col => !col.disabled);
  },

  columnsFromMap (map = {}, rows = [], options = {}) {
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
    return Importer.processColumns(columns, rows, options);
  },

  importColumns (columns, rows, options) {
    if (!columns || typeof columns !== 'object' || (Array.isArray(columns) && !columns.length)) {
      if (Array.isArray(rows) && rows.length) return Importer.columnsFromRows(rows, options);
      else return [];
    } else {
      if (!Array.isArray(columns)) return Importer.columnsFromMap(columns, rows, options);
      return Importer.processColumns(columns, rows, options);
    }
  },

  importRows (rows) {
    if (!rows || !Array.isArray(rows) || rows.some(row => typeof row !== 'object')) return [];
    rows = rows.map(row => {
      let __id = Utils.uid();
      return Object.assign({}, row, { __id });
    });
    return rows;
  },

  importOptions (options = {}) {
    let result = Object.assign({}, OptionsDefaults, options);
    return result;
  },

  importActions (actions, options = {}) {
    if (!actions || !Array.isArray(actions) || actions.some(action => typeof action !== 'object')) return [];
    actions = (options.useDefaultActions ? [...actions, ...ActionDefaults] : actions);
    actions = actions.map(action => {
      let __id = action.__id ? action.__id : Utils.uid();
      return Object.assign({}, action, { __id });
    });
    return actions;
  }
};

export default Importer;
