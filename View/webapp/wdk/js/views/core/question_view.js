wdk.namespace('wdk.views.core', function(ns) {
  'use strict';

  /**
   * @class QuestionView
   * @namespace wdk.views.core
   * @extends wdk.core.View
   */
  ns.QuestionView = Backbone.View.extend({

    delegateViews: null,

    constructor: function() {
      this.delegateViews = [];
      Backbone.View.apply(this, arguments);
    },

    initialize: function() {
      var questionFullName = this.$el.data('questionFullName');

      this.$form = this.$el.closest('form');

      // turn off autocompletion
      this.$form.prop('autocomplete', 'off');

      // set up parameter handlers
      // TODO refactor handlers to their own Views
      if (this.$el.data('showParams') === true) {
        wdk.parameterHandlers.init(this.$el);
      }

      // initialize delegate views
      this.createDelegateView(questionFullName);
    },

    /**
     * Delegate to custom view
     */
    createDelegateView: function(viewName) {
      var View = wdk.app.getView('question:' + viewName);
      if (typeof View !== 'undefined') {
        // Use $form as the element for convenience
        var view = new View({ el: this.$form });
        this.delegateViews.push(view);
      }
    }

  });

});
