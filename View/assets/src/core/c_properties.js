/**
 * Properties for use on "Class" objects (i.e., constructor functions)
 */
wdk.namespace('wdk.core.c_properties', function(ns) {
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
   * Add this to a constructor function to get a factory function
   *
   * Example
   *     function Bar(foo) {
   *       this.foo = foo;
   *     }
   *
   *     Bar.create = wdk.core.c_properties.create;
   *
   * The following two lines return the same value:
   *
   *     var bar1 = new Bar('foo');
   *     var bar2 = Bar.create('foo');
   *
   *     bar1.foo; // 'foo'
   *     bar2.foo; // 'foo'
   */
  ns.create = function create() {
    var o = Object.create(this.prototype);
    return (this.apply(o, arguments), o);
  };

  /**
   * Add this to a constructor function to provide encapsulated inheritance
   *
   * Example
   *     function Foo() { }
   *
   *     Foo.extend = wdk.core.c_properties.extend;
   *
   *     var Bar = Foo.extend( prototype );
   *
   *     var foo = new Foo();
   *     var bar = new Bar();
   *
   *     foo instanceof Foo;  // true
   *     bar instanceof Bar;  // true
   *     bar instanceof Foo;  // true
   */
  ns.extend = function extend(c_protoProps, c_staticProps) {
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

});
