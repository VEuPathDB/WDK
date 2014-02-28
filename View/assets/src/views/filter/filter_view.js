/**
 * This view manages state between FilterFieldsView and ResultsView.
 */
wdk.namespace('wdk.views.filter', function(ns) {
  'use strict';

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

    template: wdk.templates['filter/filter.handlebars'],

    initialize: function(properties) {
      this.listenTo(this.model.filteredData, 'reset', this.setCount);
      this.render();
    },

    render: function() {
      this.$el.html(this.template(this.model.attributes));
      this.setCount();
      this.setContext('filters');

      var filterFieldsView = new FilterFieldsView({
        el: this.$('.filters'),
        model: this.model
      });

      var resultsView = new ResultsView({
        el: this.$('.results'),
        model: this.model
      });
      return this;
    },

    setCount: function() {
      var count = this.model.filteredData.length;
      this.$('.summary .count').html(count ? count.toString().bold() : 'No');
      return this;
    },

    setContext: function(context) {
      // show other links
      this.$('a').show();
      this.$('a[href="#' + context + '"]').hide();

      // show context div
      this.$('.context').hide();
      this.$('.' + context).show();

      return this;
    }

  });

});
