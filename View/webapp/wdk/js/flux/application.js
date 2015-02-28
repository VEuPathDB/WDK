import noop from 'lodash/utility/noop';
import React from 'react';
import Router from 'react-router';
import { routes } from './router';
import createObjectCache from './utils/createObjectCache';

const createRouterCallback = function createRouterCallback(rootElement, context) {
  return function runRoute(Handler, state) {
    React.withContext(context, function render() {
      React.render( <Handler {...state} />, rootElement);
    });
  };
};

const runApplication = ({
  dispatcher,
  serviceAPI,
  stores,
  actionCreators,
  rootElement,
  location,
  recordComponentResolver = noop
}) => {

  if (dispatcher == null) {
    throw Error('A dispatcher was not defined');
  }
  if (serviceAPI == null) {
    throw Error('A serviceAPI was not defined');
  }

  const storeCache = createObjectCache(stores, dispatcher);
  const actionCreatorsCache = createObjectCache(actionCreators, dispatcher, serviceAPI);

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
  const reactContext = {
    getStore(name) {
      return storeCache.get(name);
    },
    getActions(name) {
      return actionCreatorsCache.get(name);
    },
    getRecordComponent(recordClass, defaultComponent) {
      return recordComponentResolver(recordClass, defaultComponent);
    }
  };

  const routerCallback = createRouterCallback(rootElement, reactContext);
  Router.run(routes, location, routerCallback);
};

export { runApplication };
