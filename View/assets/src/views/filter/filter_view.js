/**
 * This view manages state between FilterFieldsView and ResultsView.
 */
wdk.namespace('wdk.views.filter', function(ns) {
  'use strict';

  var FilterCollapsedView = ns.FilterCollapsedView;
  var FilterExpandedView = ns.FilterExpandedView;

  ns.FilterView = wdk.views.View.extend({

    events: {
      'click a[href="#collapse"]': function(e) {
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

    initialize: function() {
      this.filterCollapsedView = new FilterCollapsedView({ model: this.model });
      this.filterExpandedView = new FilterExpandedView({ model: this.model });
      this.collapse(true);
      this.listenTo(this.model.fields, 'select', function() {
        this.collapse(false);
      });
    },

    render: function() {
      this.$el.html(this.template(this.model.attributes));
      this.$('.collapsed').append(this.filterCollapsedView.render().el);
      this.$('.expanded').append(this.filterExpandedView.render().el);
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
