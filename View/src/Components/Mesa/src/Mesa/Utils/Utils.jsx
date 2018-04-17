import React from 'react';

import Defaults from 'Mesa/Defaults';

const Utils = {
  stringValue (value) {
    switch (typeof value) {
      case 'string':
        return value;
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
    let short = words.slice(0, cutoff).join(' ');

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

  homogenizeColumn (column = {}) {
    return Object.assign({}, Defaults.column, column);
  },

  processColumns (columns = []) {
    return columns.map(Utils.homogenizeColumn);
  },

  columnFromKey (key) {
    return Utils.homogenizeColumn({ key });
  },

  columnsFromRows (rows = []) {
    const keys = [];
    rows.forEach(row => {
      Object.keys(row).forEach(prop => keys.indexOf(prop) < 0 && keys.push(prop));
    });
    return keys.map(Utils.columnFromKey);
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
      return a[sortByKey] < b[sortByKey];
    });
    return ascending ? result : result.reverse();
  },

  textSort (items, sortByKey, ascending = true) {
    let result = items.sort((a, b) => {
      return a[sortByKey].trim() < b[sortByKey].trim();
    });
    return ascending ? result : result.reverse();
  }
};

export default Utils;
