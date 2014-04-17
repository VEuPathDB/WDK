wdk.namespace('wdk.core', function(ns, $) {
  'use strict';

  var BaseObject = ns.BaseObject;
  var Container = ns.Container;
  var RunLoop = ns.RunLoop;

  /**
   * Main application controller and IoC container
   *
   * Use it like this
   *
   * ```
   * var app = wdk.core.Application.create({
   *   rootElement: '#my-app',
   *   ready: function() {
   *     // run some code when the app is initialized
   *   }
   * });
   * ```
   *
   *
   * @class Application
   * @namespace wdk.core
   * @extends wdk.core.BaseObject
   *
   */
  var Application = ns.Application = BaseObject.extend({


    /**
     * Utility for managing lifecycle events
     *
     * @private
     * @property _runloop
     * @type wdk.core.RunLoop
     */
    _runloop: null,


    /**
     * Flag for when the Application has been initialized
     *
     * @private
     * @property _initialized
     * @type Boolean
     */
    _initialized: false,


    /**
     * This is the parent element of the application.
     * It can be an element or a jQuery-style selector.
     *
     * @property rootElement
     * @type Element|String
     * @default 'body'
     */
    rootElement: 'body',


    /**
     * Registry of views and associated (jQuery style) selectors
     *
     * @property
     * @type wdk.core.Container
     * @private
     *
     */
    _viewContainer: null,


    /**
     * Default Application constructor.
     *
     * Accepts an optional object of properties
     *
     * TODO: Determine what properties can be overridden?
     *
     * @method constructor
     * @param {Object} opts Options to override defaults
     */
    constructor: function(opts) {
      var app = this;
      opts = opts || {};

      app.rootElement = opts.rootElement || app.rootElement;
      app.ready = opts.ready || app.ready;

      app._runloop = RunLoop.create(app);
      app._viewContainer = Container.create();

      app._runloop.defer(function() {
        if ($.isReady) {
          app.initialize();
        } else {
          $(document).ready(function runApp() {
            app._runloop.defer(app.initialize);
          });
        }
      });
    },


    /**
     * Register a DOM selector with a particular View object.
     *
     * @method registerView
     * @param {String} name A unique name to identify the class of factory
     * @param {Function} factory A factory function
     */
    registerView: function() {
      //this.viewRegistry.push({ selector: selector, factory: factory });
      var viewContainer = this._viewContainer;
      viewContainer.register.apply(viewContainer, arguments);
      return this;
    },


    /**
     * Return a registered View, or null
     *
     * @methos getView
     * @param {String} name
     */
    getView: function() {
      var viewContainer = this._viewContainer;
      var View = viewContainer.get.apply(viewContainer, arguments);
      return View;
    },


    /**
     * Instantiate registered views
     *
     * @method initializeViews
     * @param {Object} node jQuery-wrapped root DOM element
     */
    initializeViews: function(node) {
      var app = this;

      node.find('[data-view-name]').each(function(idx, el) {
        var name = el.getAttribute('data-view-name');
        var defaultName = el.getAttribute('data-view-default');
        var viewFactory = app._viewContainer.get(name) || app._viewContainer.get(defaultName);

        if (viewFactory) {
          viewFactory.create({ el: el });
        }
      });
    },


    /**
     * Resolve views based on conventional naming
     *
     * For instance, if a View name is 'my-view', the resolver
     * may look for an object named Some.Namespace.MyView.
     *
     * @param {Object} node A View's top-level DOM node
     */
    viewResolver: function(name, defaultView) {
    },


    /**
     * Run DOM initializers starting from a given node.
     *
     * Application will do this by default on the rootElement,
     * but this is also useful to explicitly call for HTML
     * inserted via AJAX calls, etc.
     *
     * If a string is provided, jQuery will be used to first convert
     * the string to a DOM fragment, and the root node will be returned.
     *
     * Example:
     * ```
     * $.get('/item.html').then(app.initializeDOM.bind(app)).then(function($item) {
     *   $item.appendTo($el);
     * });
     * ```
     *
     * @method initializeDOM
     * @param {Object|String} rootElement A DOM node or string of HTML.
     * @return jQuery-wrapped initialized Element
     */
    initializeDOM: function(rootElement) {
      var $rootElement = $(rootElement);
      this.initializeViews($rootElement);
      $rootElement.attr('__initialized', true);
      return $rootElement;
    },


    /**
     * Regsiter callback for when application is ready
     *
     * @method ready
     * @return current instance
     */
    ready: function() {
      return this;
    },


    /**
     * Start the application.
     *
     * Registered views are initialized, and, if one is registered, the ready
     * callback is called.
     *
     * This method will only have an effect the first time it is called. All
     * subsequent calls will be a no-op.
     *
     * @method initialize
     */
    initialize: function() {
      var app = this;

      if (!app._initialized) {
        app.initializeDOM(app.rootElement);
        app.ready();
        app._initialized = true;
      }

      return this;
    }

  });

});
