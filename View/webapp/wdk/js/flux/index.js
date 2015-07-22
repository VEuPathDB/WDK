/**
 * Bootstrap the WDK Flux application. Initialize the environment and wire up
 * dependencies.
 *
 * Export the wdk object.
 */

import React from 'react';
import Router from 'react-router';
import Immutable from 'immutable';
import _ from 'lodash';
import Context from './core/context';
import Routes from './routes';
import * as components from './components';
import * as stores from './stores';
import * as actions from './actions';

// expose libraries to global object, but only if they aren't already defined
if (window._ == null) window._ = _;
if (window.Immutable == null) window.Immutable = Immutable;
if (window.React == null) window.React = React;
if (window.ReactRouter == null) window.ReactRouter = Router;


let Wdk = {

  /**
   * Starts a WDK application instance based on the provided configuration.
   *
   * @param {object} config Application configuration
   * @param {string} config.endpoint Base URL for the RESTful WDK Service
   * @param {string} config.rootUrl Root element to render application
   * @param {element} config.rootElement Root element to render application
   */
  createApplication(config) {
    config.routes = Routes.getRoutes(config.rootUrl);
    let context = Context.createContext(config);
    for (let name in stores) {
      let Store = stores[name];
      context.addStore(Store, Store.createStore(context));
    }
    for (let name in actions) {
      let Actions = actions[name];
      context.addActions(Actions, Actions.createActions(context));
    }
    return context;
  },

  stores,

  actions,

  components

};


// store scroll position, and update upon reload
let scrollKey = 'previousScrollPosition';

function storeScrollPosition() {
  let scrollPosition = JSON.stringify([ window.scrollX, window.scrollY ]);
  window.sessionStorage.setItem(scrollKey, scrollPosition);
}

window.addEventListener('scroll', storeScrollPosition);

document.addEventListener('DOMContentLoaded', function(setScrollPosition) {
  let scrollPosition = JSON.parse(window.sessionStorage.getItem(scrollKey));
  if (scrollPosition != null) {
    console.log('scrolling to', scrollPosition);
    window.scrollTo(...scrollPosition);
  }
});

export default Wdk;
