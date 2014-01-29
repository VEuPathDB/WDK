/**
 * Utilities to defer execution of functions within an event loop.
 *
 *   - defer is mostly sugar for setTimeout without a set time
 *   - deferOnce ensures a function is only called once within
 *     a specified time frame. The timeframe is 0 by default.
 */
wdk.namespace('wdk.core', function(ns) {

  var RunLoop = wdk.core.BaseObject.extend({

    constructor: function(defaultContext) {
      this.defaultContext = defaultContext || window;
      this.deferring = {};
    },

    defer: function defer(fn, timeout, context) {
      context = context || this.defaultContext;

      setTimeout(fn.bind(context), timeout);
    },

    deferOnce: function deferOnce(fn, timeout, context) {
      var runloop = this;
      context = context || this.defaultContext;

      if (fn in this.deferring) return;

      this.deferring[fn] = true;

      timeout = timeout | 0;

      setTimeout(function() {
        fn.call(context);
        delete runloop.deferring[fn];
      }, timeout);
    }

  });

  ns.RunLoop = RunLoop;

});
