/**
 * This view manages state between FilterFieldsView and ResultsView.
 */
wdk.namespace('wdk.views.filter', function(ns) {
  'use strict';

  var FilterItemsView = ns.FilterItemsView;
  var FilterFieldsView = ns.FilterFieldsView;
  var ResultsView = ns.ResultsView;

  ns.FilterView = wdk.views.View.extend({

    events: {
      'click .summary a': function(e) {
        var link = e.currentTarget;
        e.preventDefault();
        this.setContext(link.getAttribute('href').slice(1));
      }
    },

    className: 'filter',

    template: wdk.templates['filter/filter.handlebars'],

    initialize: function(properties) {
      this.listenTo(this.model.filteredData, 'reset', this.setCount);
      this.filterFieldsView = new FilterFieldsView({ model: this.model });
      this.resultsView = new ResultsView({ model: this.model });
      this.filterItemsView = new FilterItemsView({ model: this.model.filters });
      this.render();
    },

    render: function() {
      this.$el.html(this.template(this.model.attributes));

      this.$el.prepend(this.filterItemsView.el);
      this.$el.append(this.filterFieldsView.el);
      this.$el.append(this.resultsView.el);

      this.setCount();
      this.setContext('hide');

      return this;
    },

    setCount: function() {
      var count = this.model.filteredData.length;
      this.$('.summary .count').html(count ? count.toString().bold() : 'No');
      return this;
    },

    /**
     * Determine which panel to show.
     *
     * Options are 'filters', 'results', or neither.
     */
    setContext: function(context) {
      // show other links
      this.$('.summary li').removeClass('hidden')
      this.$('a[href="#' + context + '"]').parent().addClass('hidden');

      this.filterFieldsView.hide();
      this.resultsView.hide();

      if (context === 'filters') {
        this.filterFieldsView.show();
      }
      if (context === 'results') {
        this.resultsView.show();
      }

      return this;
    }

  });

});
