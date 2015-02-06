export default function createObjectCache(factories, ...deps) {
  var _cache = {};
  var get = function get(token) {
    var object = _cache[token];
    if (typeof object === 'undefined') {
      object = _cache[token] = factories[token](...deps);
    }
    return object;
  };
  return { get };
}
