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

  getRealOffset (el) {
    let top = 0;
    let left = 0;
    do {
      top += el.offsetTop || 0;
      left += el.offsetLeft || 0;
      el = el.offsetParent;
    } while (el);
    return { top, left };
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

  numberSort (items, sortByKey, ascending) {
    let result = items.sort((a, b) => {
      let A = a[sortByKey] ? parseFloat(a[sortByKey]) : 0;
      let B = b[sortByKey] ? parseFloat(b[sortByKey]) : 0;
      return A === B ? 0 :(A < B ? 1 : -1);
      return result;
    });
    return ascending ? result.reverse() : result;
  },

  sortFactory (accessor) {
    accessor = (typeof accessor == 'function' ? accessor : (value) => value);
    return function (a, b) {
      let A = accessor(a);
      let B = accessor(b);
      return A === B ? 0 :(A < B ? 1 : -1);
    };
  },

  textSort (items, sortByKey, ascending = true) {
    let result = items.sort((a, b) => {
      let A = typeof a[sortByKey] === 'string' ? a[sortByKey].trim() : Utils.stringValue(a[sortByKey]);
      let B = typeof b[sortByKey] === 'string' ?  b[sortByKey].trim() : Utils.stringValue(b[sortByKey]);
      return A === B ? 0 :(A < B ? 1 : -1);
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
  },

  randomize (low = 0, high = 99) {
    return Math.floor(Math.random() * (high - low + 1) + low);
  },

  uid (len = 8) {
    let output = '';
    while (output.length < len) {
      let index = Utils.randomize(0, 35);
      if (index >= 10) output += String.fromCharCode(87 + index);
      else output += index.toString();
    };
    return output;
  },

  keysInList (list, blacklist = []) {
    if (!Array.isArray(list) || list.some(item => typeof item !== 'object')) return list;
    return list.reduce((keys, currentValue) => {
      Object.keys(currentValue)
        .forEach(key => keys.includes(key) || blacklist.includes(key) || keys.push(key));
      return keys;
    }, []);
  },

  createCsv (rows, columns) {
    if (!columns) columns = Utils.keysInList(rows).map(key => { key });
    columns = columns.filter(column => !column.hidden && !column.disabled);

    let outputLines = [];
    let keys = columns.map(({ key }) => key);
    let names = columns.map(column => column.name ? column.name : column.key);
    outputLines.push(names.join(','));
    rows.forEach(row => {
      let values = keys.map(key => {
        return Utils
          .stringValue(row[key])
          .replace(',', '\,')
          .replace('\n', '');
      });
      outputLines.push(values.join(','));
    });
    return outputLines.join('\n');
  }
  
};

export default Utils;
