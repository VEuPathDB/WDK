wdk.namespace('wdk.core', function(ns) {
  'use strict';

  // Object.create polyfill
  // https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Object/create#Polyfill
  if (!Object.create) {
    Object.create = (function(){
      function F(){}

      return function(o){
        if (arguments.length != 1) {
          throw new Error('Object.create implementation only accepts one parameter.');
        }
        F.prototype = o
        return new F()
      }
    })()
  }

  /**
   *
   * @class BaseObject
   * @namespace wdk.core
   */
  var BaseObject = function () {
  };


  /**
   * Create an instance of a class.
   *
   * @method create
   * @static
   */
  BaseObject.create = function create() {
    var o = Object.create(this.prototype);
    return (this.apply(o, arguments), o);
  };


  /**
   * Create an inheriting subclass.
   *
   * @method extend
   * @static
   */
  BaseObject.extend = function extend(c_protoProps, c_staticProps) {
    var parent = this;
    var child;

    if (c_protoProps && c_protoProps.hasOwnProperty('constructor')) {
      child = c_protoProps.constructor;
    } else {
      // use parent's constructor function
      child = function () {
        return parent.apply(this, arguments);
      };
    }

    // copy static props from parent
    Object.keys(parent).forEach(function(k) {
      child[k] = parent[k];
    });

    // inheritance
    child.prototype = Object.create(parent.prototype);

    if (c_protoProps) {
      // copy user supplied prototype properties
      Object.keys(c_protoProps).forEach(function(k) {
        child.prototype[k] = c_protoProps[k];
      });
    }

    if (c_staticProps) {
      // copy new static props
      Object.keys(c_staticProps).forEach(function(k) {
        child[k] = c_staticProps[k];
      });
    }

    return child;
  };

  ns.BaseObject = BaseObject;

});
