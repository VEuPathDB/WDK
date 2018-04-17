import React from 'react';

import Defaults from 'Mesa/Defaults';

const Utils = {
  stringValue (value) {
    switch (typeof value) {
      case 'string':
        if (Utils.isHtml(value)) return Utils.htmlStringValue(value);
        else return value;
      case 'number':
      case 'boolean':
        return value.toString();
      case 'object':
        if (Array.isArray(value)) return value.map(Utils.stringValue).join(', ');
        if (value === null) return '';
        else return JSON.stringify(value);
      case 'undefined':
      default:
        return '';
    };
  },

  htmlStringValue (html) {
    let tmp = document.createElement("DIV");
    tmp.innerHTML = html;
    return tmp.textContent || tmp.innerText || '';
  },

  wordCount (text) {
    if (typeof text !== 'string') return undefined;
    return text.trim().split(' ').filter(x => x.length).length;
  },

  reverseText (text) {
    if (typeof text !== 'string' || !text.length) return text;
    return text.split('').reverse().join('');
  },

  trimInitialPunctuation (text) {
    if (typeof text !== 'string' || !text.length) return text;
    while (text.search(/[a-zA-Z0-9]/) !== 0) {
      text = text.substring(1);
    };
    return text;
  },

  trimPunctuation (text) {
    if (typeof text !== 'string' || !text.length) return text;
    text = Utils.trimInitialPunctuation(text);
    text = Utils.reverseText(text);
    text = Utils.trimInitialPunctuation(text);
    text = Utils.reverseText(text);
    return text;
  },

  truncate (text, cutoff) {
    if (typeof text !== 'string' || typeof cutoff !== 'number') return text;
    let count = Utils.wordCount(text);
    if (count < cutoff) return text;
    let words = text.trim().split(' ').filter(x => x.length);
    let threshold = Math.ceil(cutoff * 0.66);
    let short = words.slice(0, threshold).join(' ');

    return Utils.trimPunctuation(short) + '...';
  },

  objectContainsQuery (obj, query) {
    let searchable = Utils.stringValue(obj).toLowerCase();
    return Utils.stringContainsQuery(searchable, query);
  },

  stringContainsQuery(str, query) {
    let queryParts = query.toLowerCase().split(' ');
    return queryParts.every(part => str.indexOf(part) >= 0);
  },

  homogenizeColumn (column = {}, rows = []) {
    if (!column || !column.key) throw new Error('Cannot homogenize a column without a `.key`');
    if (!column.type && rows.length) {
      let isHtmlColumn = rows.some(row => Utils.isHtml(row[column.key]));
      let isNumberColumn = rows.every(row => !row[column.key] || !row[column.key].length || Utils.isNumeric(column.key));
      if (isHtmlColumn) column.type = 'html';
      else if (isNumberColumn) column.type = 'number';
    }
    if (!column.name) column.name = Utils.ucFirst(column.key);
    return Object.assign({}, Defaults.column, column);
  },

  processColumns (columns = []) {
    return columns.map(Utils.homogenizeColumn);
  },

  columnFromKey (key, rows = []) {
    return Utils.homogenizeColumn({ key }, rows);
  },

  columnsFromRows (rows = []) {
    const keys = [];
    rows.forEach(row => {
      Object.keys(row).forEach(prop => keys.indexOf(prop) < 0 && keys.push(prop));
    });
    return keys.map(key => Utils.columnFromKey(key, rows));
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
          break;
        default:
          break;
      }
      columns.push(column);
    };
    columns = Utils.processColumns(columns);
    return columns;
  },

  numberSort (items, sortByKey, ascending) {
    let result = items.sort((a, b) => {
      let A = a[sortByKey] ? parseFloat(a[sortByKey]) : 0;
      let B = b[sortByKey] ? parseFloat(b[sortByKey]) : 0;
      return A < B;
    });
    return ascending ? result.reverse() : result;
  },

  textSort (items, sortByKey, ascending = true) {
    let result = items.sort((a, b) => {
      let A = typeof a[sortByKey] === 'string' ? a[sortByKey].trim() : '';
      let B = typeof b[sortByKey] === 'string' ?  b[sortByKey].trim() : '';
      return A < B;
    });
    return ascending ? result.reverse() : result;
  },

  ucFirst (text) {
    if (typeof text !== 'string' || !text.length) return text;
    return text[0].toUpperCase() + text.slice(1);
  },

  isHtml (text, strict = false) {
    if (typeof text !== 'string') return false;
    if (strict && (text[0] !== '<' || text[text.length - 1] !== '>')) return false;

    let parser = new DOMParser().parseFromString(text, 'text/html');
    return Array.from(parser.body.childNodes).some(node => node.nodeType === 1);
  },

  isNumeric (value) {
    return !Array.isArray(value) && (value - parseFloat(value) + 1) >= 0;
  }
};

export default Utils;
