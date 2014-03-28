wdk.namespace('wdk.views.filter', function(ns) {
  'use strict';

  var FilterFieldsView = ns.FilterFieldsView;
  var ResultsView = ns.ResultsView;

  ns.FilterExpandedView = wdk.views.View.extend({

    template: wdk.templates['filter/filter_expanded.handlebars'],

    initialize: function() {
      //this.listenTo(this.model.filteredData, 'reset', this.setCount);

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
          view.showSubview(ui.newPanel.attr('id'));
        }
      });
      return this;
    },

    setCount: function() {
      this.$('a[href="#results"] .count').html(this.model.filteredData.length);
    },

    didShow: function() {
      var activeTab = this.$el.tabs('option', 'active');
      var id = this.$('.ui-tabs-panel').get(activeTab).id
      this.showSubview(id);
    },

    showSubview: function(id) {
      var subview;
      if (id === 'select') {
        subview = this.filterFieldsView;
      } else if (id === 'results') {
        subview = this.resultsView;
      }
      return subview.show();
    }
  });

});
