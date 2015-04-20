/**
 * Creates an opinionated a service container.
 *
 * All factory function provided is expected to take the same set of
 * dependencies. A limitation of this implementation is that all dependencies
 * must be created before an object cache is created. For the current needs of
 * WDK, this should be fine.
 *
 * Example:
 *
 *     var factories = {
 *       factory1: someFactory,
 *       factory2: someOtherFactory
 *     };
 *     var objectCache = createObjectCache(factories, dep1, dep2);
 *     var object1 = objectCache.get('factory1');
 *
 * @param {object} factories key-value store of object factories
 * @param {...any} deps dependecies to inject into factories
 */
export default function createObjectCache(factories, ...deps) {
  var cache = new Map();

  return {
    get(token) {
      if (!cache.has(token)) {
        var factory = factories[token];
        var instance = new factory(...deps);
        cache.set(token, instance);
      }
      return cache.get(token);
    }
  };
}
