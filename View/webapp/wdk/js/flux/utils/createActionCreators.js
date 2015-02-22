/**
 * Created an ActionCreators factory function.
 *
 * The returned factory will itself return a proxy for the methods provided
 * by `baseObject`. Each method in the proxy, when invoked, will call the
 * original method of `baseObject`, but using a context that provides the
 * application dispatcher's `dispatch` method, and the configured application
 * `serviceAPI` instance.
 *
 * Example:
 *
 *   var MyActions = createActionCreators({
 *     loadResource(resourceId) {
 *       this.dispatch({ type: 'loading' });
 *       this.serviceAPI.get(resourceId)
 *       .then(resource => this.dispatch({ type: 'loaded', resource: resource })
 *       .catch(error => this.dispatch({ type: 'error', error: error });
 *     }
 *   });
 */
export default function createActionCreators(baseObject) {

  // A factory function that returns a proxy to `baseObject`.
  // The application dispatcher and serviceAPI will be injected by the injector
  // at runtime.
  return function createWdkActions(dispatcher, serviceAPI) {

    // Create context argument in which to call action creators
    var context = {
      dispatch: dispatcher.dispatch.bind(dispatcher),
      serviceAPI: serviceAPI
    };

    // Proxy to base object. Methods will be filled in below.
    var proxyObject = {};

    // Create proxies for methods and call in `context`.
    // For each method in methods, this will create a new method with the same
    // name on the WdkActions object we are constructing. When invoked, the
    // new method will be called using `context` above. This means that `this`
    // will refer to `context`.
    for (var method in baseObject) {
      /* jshint -W083 */
      if (typeof baseObject[method] === 'function') {
        // Use immediately invoked function so proxyObject[method] doesn't
        // create a closure over `method`.
        (function(method) {
          proxyObject[method] = function() {
            baseObject[method].apply(context, arguments);
          };
        }(method));
      }
    }

    return proxyObject;
  };
}
