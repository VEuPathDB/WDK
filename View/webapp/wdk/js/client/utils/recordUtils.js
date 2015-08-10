export function makeKey(recordClass, id) {
  // order keys
  let idStr = Object.keys(id).sort().map(name => `name=${id[name]}`).join('&');
  return recordClass + '?' + idStr;
}
