/* global __DEV__ */
import {mapValues, values} from 'lodash';
import {createElement} from 'react';
import * as ReactDOM from 'react-dom';

import Dispatcher from './dispatcher/Dispatcher';
import WdkService from './utils/WdkService';
import Root from './controllers/Root';
import { loadAllStaticData } from './actioncreators/StaticDataActionCreators';

import * as Components from './components';
import * as Stores from './stores';
import * as Controllers from './controllers';

/**
 * Initialize the application.
 *
 * @param {string} option.rootUrl Root URL used by the router. If the current
 *   page's url does not begin with this option's value, the application will
 *   not render automatically.
 * @param {string|HTMLElement} option.rootElement Where to mount the
 *   application. Can be a selector string or an element. If this option does
 *   not resolve to an element after the DOMContentLoaded event is fired, the
 *   application will not render automatically.
 * @param {string} option.endpoint Base URL for WdkService.
 * @param {HTMLElement} option.rootElement DOM node to render the application.
 * @param {React.Element} option.applicationRoutes Additional routes to register
 *   with the Router.
 * @param {Object} option.storeWrappers Mapping from store name to replacement
 *   class
 */
export function initialize({ rootUrl, rootElement, endpoint, applicationRoutes, storeWrappers }) {

  // define the elements of the Flux architecture
  let wdkService = new WdkService(endpoint);
  let dispatcher = new Dispatcher;
  let makeDispatchAction = getDispatchActionMaker(dispatcher, { wdkService });
  let stores = configureStores(dispatcher, storeWrappers);

  // load static WDK data into service cache and view stores that need it
  makeDispatchAction()(loadAllStaticData());

  // log all actions in dev environments
  if (__DEV__) logActions(dispatcher, stores);

  if (location.pathname.startsWith(rootUrl)) {
    // render the root element once page has completely loaded
    document.addEventListener('DOMContentLoaded', function() {
      let container = rootElement instanceof HTMLElement
        ? rootElement
        : document.querySelector(rootElement);
      if (container != null) {
        let applicationElement = createElement(
          Root, {
            rootUrl,
            makeDispatchAction,
            stores,
            applicationRoutes
          });
        ReactDOM.render(applicationElement, container);
      }
      else if (__DEV__) {
        console.log('Could not resolve rootElement %o. Application will not render automatically.', rootElement);
      }
    });
  }
  else if (__DEV__) {
    console.log('The current page url does not start with the rootUrl %o. Application router will not be rendered.', rootUrl);
  }

  // return WDK application components
  return { wdkService, makeDispatchAction, stores };
}

/**
 * Creates a `stores` object.
 *
 * @param {Dispatcher} dispatcher
 * @param {Object} named functions that return store override classes
 */
function configureStores(dispatcher, storeWrappers) {
  let storeClasses = wrapStores(storeWrappers);
  let storeInstances = {};
  Object.keys(storeClasses).forEach(function(className) {
    if (className != 'WdkStore') {
      storeInstances[className] =
        new storeClasses[className](dispatcher, className, storeInstances);
    }
  });
  return storeInstances;
}

/**
 * Apply WDK Store wrappers. Keys of `storeWrappers` should correspond to WDK
 * Store names. Values of `storeWrappers` are functions that take the current
 * Store class and return a new Store class.
 *
 * @param {Object} storeWrappers
 */
function wrapStores(storeWrappers) {
  let stores = Object.assign({}, Stores);
  for (let key in storeWrappers) {
    let wdkStore = stores[key];
    if (wdkStore == null) {
      console.log("Creating new application store: `%s`.", key);
    }
    let storeWrapper = storeWrappers[key];
    let storeWrapperType = typeof storeWrapper;
    if (storeWrapperType !== 'function') {
      console.error("Expected Store wrapper for `%s` to be a 'function', " +
          "but is `%s`.  Skipping...", key, storeWrapperType);
      continue;
    }
    stores[key] = (wdkStore == null ? storeWrapper() : storeWrapper(wdkStore));
  }
  return stores;
}

/**
 * Create a function that takes a channel and creates a dispatch function
 * `dispatchAction` that forwards calls to `dispatcher.dispatch` using the
 * channel as a scope for the audience of the action.  In dispatchAction:
 *
 * If `action` is a function, it will be called with `dispatchAction` and
 * `services`. Calling it with `dispatchAction` allows for composability since
 * an action function can in turn call another action function. This is useful
 * for creating higher-order dispatch helpers, such as latest, once, etc.
 *
 * If `action` is an object, `dispatcher.dispatch` will be called with it.
 *
 * An `action` function should ultimately return an object to invoke a dispatch.
 *
 * @param {Dispatcher} dispatcher
 * @param {any?} services
 */
function getDispatchActionMaker(dispatcher, services) {
  let logError = console.error.bind(console, 'Error in dispatchAction:');
  return function makeDispatchAction(channel) {
    if (channel === undefined) {
      console.warn("Call to makeDispatchAction() with no channel defined.");
    }
    return function dispatchAction(action) {
      if (typeof action === 'function') {
        // Call the function with dispatchAction and services
        return action(dispatchAction, services);
      }
      else if (isPromise(action)) {
        return action.then(result => dispatchAction(result)).then(undefined, logError);
      }
      else if (action == null) {
        console.error("Warning: Action received is not defined or is null");
      }
      else if (action.type == null) {
        console.error("Warning: Action received does not have a `type` property", action);
      }
      if (action != null) {
        // assign channel if requested
        action.channel = (action.isBroadcast ? undefined : channel);
      }
      return dispatcher.dispatch(action);
    };
  };
}

/**
 * Apply Component wrappers to WDK components and controllers. Keys of
 * 'componentWrappers' should correspond to Component or Controller names in
 * WDK. Values of `componentWrappers` are factories that return a new component.
 *
 * Note that this function applies wrappers "globally", meaning that all apps
 * returned by initialize will use the wrapped components, regardless of when
 * initialize and wrapComponents are called.
 *
 * @param {Object} componentWrappers
 */
export function wrapComponents(componentWrappers) {
  for (let key in componentWrappers) {
    // look in Components for class by this name
    let Component = Components[key];
    // if not found, look in Controllers
    if (Component == null) {
      Component = Controllers[key];
    }
    // if still not found, warn and skip
    if (Component == null) {
      console.warn("Cannot wrap unknown WDK Component '" + key + "'.  Skipping...");
      continue;
    }
    // if found component/controller is not wrappable, log error and skip
    if (!("wrapComponent" in Component)) {
      console.error("Warning: WDK Component `%s` is not wrappable.  WDK version will be used.", key);
      continue;
    }
    // wrap found component/controller
    Component.wrapComponent(componentWrappers[key]);
  }
}

/**
 * Log all actions and Store state changes to the browser console.
 *
 * @param {Dispatcher} dispatcher
 * @param {Object} stores
 */
function logActions(dispatcher, stores) {
  dispatcher.register(action => {
    dispatcher.waitFor(values(stores).map(s => s.getDispatchToken()));
    console.group(action.type);
    console.info("dispatching", action);
    console.info("state", mapValues(stores, store => store.getState()));
    console.groupEnd(action.type);
  });
}

/**
 * Detect if `maybePromise` is a Promise.
 * @param {any} maybePromise
 * @returns {boolean}
 */
function isPromise(maybePromise) {
  return maybePromise != null && typeof maybePromise.then === 'function';
}
