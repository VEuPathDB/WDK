wdk.namespace('wdk.views', function(ns, $) {
  'use strict';

  /**
   * Base View class for WDK Views.
   *
   * This class sets up lifecycle event hooks such as `didInitialize`.
   * Events are also emitted using `Backbone.Events` methods.
   *
   * @class View
   * @namespace wdk.views
   * @extends Backbone.View
   */
  var View = ns.View = Backbone.View.extend({

    constructor: function() {
      var view = this;
      this.runloop = wdk.core.RunLoop.create(this);
      if (view.mixins) {
        view.mixins.forEach(function(mixin) {
          view.applyMixin(mixin);
        });
      }
      Backbone.View.prototype.constructor.apply(this, arguments);
    },

    /**
     * Initialize Element and set up event hooks.
     *
     * This method is reserved for setup logic. Override with caution.
     * If overriding is necessary, and you want to retain the setup
     * logic WDK puts in place, take care to call the parent method:
     * ```
     * var View = wdk.core.View;
     * var MyView = View.extend({
     *   initialize: function() {
     *     View.prototype.initialize.apply(this, arguments);
     *     ...
     *   }
     * });
     * ```
     *
     * @method initialize
     * @private
     */
    initialize: function() {
      this.runloop.defer(this.didInitialize);
    },

    /**
     * Apply mixin to object instance
     *
     * @method applyMixin
     * @param {Object} mixin Mixin to apply to object
     */
    applyMixin: function(mixin) {
      var view = this;
      Object.keys(mixin).forEach(function(prop) {
        view[prop] = mixin[prop];
      });
    },

    /**
     * Mixins defined here will be applied to View instances and any
     * decendent View instances.
     *
     * @property mixins
     * @type Array{Object}
     */
    mixins: null,

    /**
     * Lifecycle management
     *
     * @property runloop
     * @private
     * @type wdk.core.RunLoop
     */
    runloop: null,

    /**
     * Override `didInitialize` to provide custom DOM manipulation
     * and DOM event handling. This method is called after the View
     * and its associated element have been initialized.
     *
     * By default, this method is a noop.
     *
     * @method didInitialize
     */
    didInitialize: function() { }
  });

  View.create = wdk.core.BaseObject.create;
});
