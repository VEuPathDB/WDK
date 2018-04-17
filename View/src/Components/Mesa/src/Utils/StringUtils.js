export function stringValue (value) {
  switch (typeof value) {
    case 'string':
      if (isHtml(value)) {
        return htmlStringValue(value);
      } else {
        return value;
      }
    case 'number':
    case 'boolean':
      return value.toString();
    case 'object':
      if (Array.isArray(value)) {
        return value.map(stringValue).join(', ');
      } else if (value === null) {
        return '';
      } else {
        return JSON.stringify(value);
      }
    case 'undefined':
    default:
      return '';
  };
};

export function isHtml (text, strict = false) {
  if (typeof text !== 'string') return false;
  if (strict && (text[0] !== '<' || text[text.length - 1] !== '>')) return false;

  const parser = new DOMParser().parseFromString(text, 'text/html');
  return Array.from(parser.body.childNodes).some(node => node.nodeType === 1);
}

export function htmlStringValue (html) {
  const tmp = document.createElement("DIV");
  tmp.innerHTML = html;
  return tmp.textContent || tmp.innerText || '';
};

export function sortFactory (accessor) {
  accessor = (typeof accessor == 'function' ? accessor : (value) => value);
  return function (a, b) {
    let A = accessor(a);
    let B = accessor(b);
    return A === B ? 0 :(A < B ? 1 : -1);
  };
};

export function numberSort (list, key, ascending = true) {
  const accessor = (val) => val[key]
    ? parseFloat(val[key])
    : 0;
  const result = list.sort(sortFactory(accessor));
  return ascending ? result.reverse() : result;
};

export function arraysMatch (a, b) {
  if (!Array.isArray(a) || !Array.isArray(b)) return undefined;
  if (a.length !== b.length) return false;
  while (a.length) {
    if (a.shift() !== b.shift()) return false;
  }
  return true;
}

export function textSort (_list, key, ascending = true) {
  const list = [..._list];
  const accessor = (val) => typeof val[key] === 'string'
    ? val[key].trim()
    : stringValue(val[key]);
  const preSort = list.map(accessor);
  const sorted = list.sort(sortFactory(accessor));
  const postSort = sorted.map(accessor);
  const result = arraysMatch(preSort, postSort) ? list : sorted;
  return ascending ? result.reverse() : result;
};
