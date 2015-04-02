import noop from 'lodash/utility/noop';
import React from 'react';
import Router from 'react-router';
import { getRoutes } from './router';
import createObjectCache from './utils/createObjectCache';

var createRouterCallback = function createRouterCallback(rootElement, context) {
  return function runRoute(Handler, state) {
    React.withContext(context, function render() {
      React.render( <Handler {...state} />, rootElement);
    });
  };
};

var runApplication = function runApplication({
  baseUrl,
  dispatcher,
  serviceAPI,
  stores,
  actionCreators,
  rootElement,
  routerLocation,
  recordComponentResolver = noop,
  cellRendererResolver = noop
}) {

  if (dispatcher == null) {
    throw Error('A dispatcher was not defined');
  }
  if (serviceAPI == null) {
    throw Error('A serviceAPI was not defined');
  }

  var routes = getRoutes(baseUrl);
  var storeCache = createObjectCache(stores, dispatcher);
  var actionCreatorsCache = createObjectCache(actionCreators, dispatcher, serviceAPI);

  // This is used below in `React.withContext`. Properties of this object will
  // be available in React components that declare them using the
  // `contextTypes` property. React will read this propery on each component
  // and expose the defined context properties (via `this.context`).
  //
  // EXAMPLE
  //
  //     // ...
  //
  //     contextTypes: {
  //       getStore: React.PropTypes.func.isRequired
  //     },
  //
  //     getStore: function(storeName) {
  //         return this.context.getStore(storeName);
  //     }
  //
  //     // ...
  //
  // See `./mixins/createStoreMixin` for an example of its usage.
  var reactContext = {
    getStore(name) {
      return storeCache.get(name);
    },
    getActions(name) {
      return actionCreatorsCache.get(name);
    },
    getRecordComponent(recordClass, defaultComponent) {
      return recordComponentResolver(recordClass, defaultComponent);
    },
    getCellRenderer(recordClass, defaultRenderer) {
      return cellRendererResolver(recordClass, defaultRenderer);
    }
  };

  var routerCallback = createRouterCallback(rootElement, reactContext);
  Router.run(routes, routerLocation, routerCallback);
};

export { runApplication };
