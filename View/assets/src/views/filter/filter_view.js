/**
 * This view manages state between FilterFieldsView and ResultsView.
 */
wdk.namespace('wdk.views.filter', function(ns) {
  'use strict';

  var FilterCollapsedView = ns.FilterCollapsedView;
  var FilterExpandedView = ns.FilterExpandedView;

  ns.FilterView = wdk.views.View.extend({

    events: {
      'click [data-action="collapse"]': function(e) {
        e.preventDefault();
        this.collapse(true);
      },

      'click [data-action="expand"]': function(e) {
        e.preventDefault();
        this.collapse(false);
      }
    },

    //className: 'filter',

    template: wdk.templates['filter/filter.handlebars'],

    initialize: function(options) {
      this.filterCollapsedView = new FilterCollapsedView(options);
      this.filterExpandedView = new FilterExpandedView(options);
      this.listenTo(this.controller, 'select:field', function() {
        this.collapse(false);
      });
    },

    render: function() {
      this.$el.html(this.template());
      this.$('.collapsed').append(this.filterCollapsedView.render().el);
      this.$('.expanded').append(this.filterExpandedView.render().el);
      this.collapse(false);
      return this;
    },

    collapse: function(collapsed) {
      if (collapsed) {
        this.filterCollapsedView.show();
        this.filterExpandedView.hide();
      } else {
        this.filterCollapsedView.hide();
        this.filterExpandedView.show();
      }
    }

  });

});
