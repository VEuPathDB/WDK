/**
 * A simple factory container for IoC.
 *
 *
 * A factory can be registered in a container, and
 * retrieved from a container.
 *
 * EXAMPLE USAGE
 *     var container = new Container();
 *     container.register(identifier, factory);
 *
 *     // ... elsewhere in the application
 *
 *     var instance = container.get(identifier);
 *     -- OR --
 *     var factory = container.get(identifier);
 *     var instance = container.get(identifier).create();
 */

wdk.namespace('wdk.core', function(ns) {
  'use strict';

  var BaseObject = wdk.core.BaseObject;

  var Container = ns.Container = BaseObject.extend({
    dict: null,

    constructor: function() {
      this.dict = {};
    },

    get: function(name) {
      return this.dict[name];
    },

    register: function(name, factory) {
      if (typeof this.dict[name] !== 'undefined') {
        throw new TypeError('A factory is already registered for ' + name);
      }

      if (typeof factory === 'undefined') {
        throw new TypeError('Cannot register an unknown factory \'' + name + '\'');
      }

      this.dict[name] = factory;

      return this;
    }
  });

});
