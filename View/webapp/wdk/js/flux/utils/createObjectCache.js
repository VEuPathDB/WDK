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
  // Create an object without a prototype to prevent false positives (e.g.,
  // `toString`). See http://www.2ality.com/2013/10/dict-pattern.html
  var cache = Object.create(null);

  return {
    get(token) {
      return (token in cache)
        ? cache[token]
        : cache[token] = factories[token](...deps);
    }
  };
}
