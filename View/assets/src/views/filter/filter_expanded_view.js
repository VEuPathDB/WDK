wdk.namespace('wdk.views.filter', function(ns) {
  'use strict';

  var FilterFieldsView = ns.FilterFieldsView;
  var ResultsView = ns.ResultsView;

  ns.FilterExpandedView = wdk.views.View.extend({

    template: wdk.templates['filter/filter_expanded.handlebars'],

    initialize: function() {
      this.listenTo(this.model.filteredData, 'reset change', this.setCount);

      this.filterFieldsView = new FilterFieldsView({ model: this.model });
      this.resultsView = new ResultsView({ model: this.model });
    },

    render: function() {
      var _this = this;

      this.$el.html(this.template(this.model));
      this.$('#select').append(this.filterFieldsView.el);
      this.$('#results').append(this.resultsView.el);
      this.$('.tabs').tabs({
        activate: function(e, ui) {
          _this.showSubview(ui.newPanel.attr('id'));
        }
      });
      return this;
    },

    setCount: function() {
      var count = this.model.filteredData.where({ ignored: false }).length;
      this.$('a[href="#results"] .count').html(count);
    },

    didShow: function() {
      var activeTab = this.$('.tabs').tabs('option', 'active');
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
