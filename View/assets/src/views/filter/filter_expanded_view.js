wdk.namespace('wdk.views.filter', function(ns) {
  'use strict';

  var FilterFieldsView = ns.FilterFieldsView;
  var ResultsView = ns.ResultsView;

  ns.FilterExpandedView = wdk.views.View.extend({

    template: wdk.templates['filter/filter_expanded.handlebars'],

    initialize: function() {
      this.listenTo(this.model.filteredData, 'reset', this.setCount);

      this.filterFieldsView = new FilterFieldsView({ model: this.model });
      this.resultsView = new ResultsView({ model: this.model });
    },

    render: function() {
      var view = this;

      this.$el.html(this.template(this.model));
      this.$('#select').append(this.filterFieldsView.el);
      this.$('#results').append(this.resultsView.el);
      this.$el.tabs({
        activate: function(e, ui) {
          var id = ui.newPanel.attr('id');
          if (id === 'select') {
            view.filterFieldsView.show();
          } else if (id === 'results') {
            view.resultsView.show();
          }
        }
      });
      return this;
    },

    setCount: function() {
      this.$('a[href="#results"] .count').html(this.model.filteredData.length);
    }
  });

});
