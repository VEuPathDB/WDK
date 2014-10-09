wdk.namespace('wdk.views.core', function(ns, $) {
  'use strict';

  //
  // This view allows a View to be collapsible
  //
  //
  // OPTIONS
  //
  //   required:
  //     - view             Backbone.View instance
  //
  //   optional:
  //     - collapseString   Text used in collapse trigger element
  //     - expandString     Text used in expand trigger element
  //     - collapseTemplate Template function to create collapse trigger element HTML
  //     - expandTemplate   Template function to create expand trigger element HTML
  //

  ns.CollapsibleView = Backbone.View.extend({

    template: wdk.templates['filter/filter.handlebars'],

    collapseString: 'Collapse',

    expandString: 'Expand',

    collapseTemplate: _.template('<button><%= collapseString %></button>'),

    expandTemplate: _.template('<button><%= expandString %></button>'),

    initialize: function(options) {
      this.collapseString = options.collapseString || this.collapseString;
      this.expandString = options.expandString || this.expandString;
      this.collapseTemplate = options.collapseTemplate || this.collapseTemplate;
      this.expandTemplate = options.expandTemplate || this.expandTemplate;
      this.view = options.view;
      this.render();
      this.collapse(options.collapse);
    },

    render: function() {
      this.$collapse = $(this.collapseTemplate({ collapseString: this.collapseString }));
      this.$expand = $(this.expandTemplate({ expandString: this.expandString }));

      this.$collapse.click(function(e) {
        e.preventDefault();
        this.collapse(true);
      }.bind(this));

      this.$expand.click(function(e) {
        e.preventDefault();
        this.collapse(false);
      }.bind(this));

      this.$el.append(this.$collapse, this.$expand, this.view.el);
      return this;
    },

    collapse: function(collapsed) {
      if (collapsed) {
        this.$collapse.hide();
        this.$expand.show();
        this.view.$el.hide();
      } else {
        this.$collapse.show();
        this.$expand.hide();
        this.view.$el.show();
      }
    }

  });

});
