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
  var BaseObject = ns.BaseObject = function () { };


  /**
   * Create an instance of a class.
   *
   * @method create
   * @static
   */
  BaseObject.create = function create(opts) {
    var klass = this;
    var o = Object.create(klass.prototype);
    klass.apply(o, arguments);

    // process mixins
    if (opts && opts.mixins) {
      opts.mixins.forEach(klass.applyMixin.bind(o));
    }

    return o;
  };


  /**
   * Create an inheriting subclass.
   *
   * May be called with multiple object prototypes. This is useful when
   * multiple inheritance is desired. Prototype properties are added
   * left-to-right, meaning properties on the last prototype object
   * will overwrite any properties of the same name in previous prototype
   * properties:
   *
   * ```
   * var MyObject = BaseObject.extend({
   *   say: function() {
   *     return 'I am first';
   *   }
   * },{
   *   say: function() {
   *     return 'I am last';
   *   }
   * });
   *
   * var myObject = MyObject.create();
   * myObject.say(); // returns 'I am last'
   * ```
   *
   * @method extend
   * @param {Object[, Object...]} properties Objects with properties to add to
   *   prototype
   * @static
   */
  BaseObject.extend = function extend() {
    var args = Array.prototype.slice.call(arguments);
    var parent = this;
    var child;

    // reduce multiple prototype properties into a single object
    var c_protoProps = args.reduce(function(target, source) {
      Object.keys(source).forEach(function(k) {
        target[k] = source[k];
      });
      return target;
    }, {})

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

    return child;
  };

  /**
   * reopenClass allows static properties to be added to a class.
   *
   */
  BaseObject.reopenClass = function reopenClass(c_staticProps) {
    var klass = this;
    if (c_staticProps) {
      // copy new static props
      Object.keys(c_staticProps).forEach(function(k) {
        klass[k] = c_staticProps[k];
      });
    }
    return this;
  };

  BaseObject.applyMixin = function applyMixin(mixin) {
    var klass = this;
    Object.keys(mixin).forEach(function(k) {
      klass[k] = mixin[k];
    });
    return klass;
  };

});
