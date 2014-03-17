wdk.namespace('wdk.views.filter', function(ns) {
  'use strict';

  ns.ResultsView = wdk.views.View.extend({

    events: {
      'mouseover td': 'setTitle'
    },

    template: wdk.templates['filter/results.handlebars'],

    className: 'results context ui-helper-clearfix',

    dataTable: null,

    constructor: function() {
      Handlebars.registerHelper('property', function(key, context, options) {
        return context[key];
      });
      wdk.views.View.apply(this, arguments);
    },

    initialize: function() {
      this.listenTo(this.model.filteredData, 'reset', this.render);
      this.render();
    },

    render: function() {
      this.$el.html(this.template({
        fields: this.model.fields.toJSON(),
        filteredData: this.model.filteredData.toJSON()
      }));
      this.dataTable = this.$('.results-table').wdkDataTable({ bFilter: false }).dataTable();

      $(window).on('resize', _.debounce(this.resizeTable.bind(this), 100));

      return this;
    },

    setTitle: function(e) {
      var td = e.currentTarget;
      if (td.scrollWidth > td.clientWidth) {
        td.setAttribute('title', $(td).text());
      }
    },

    didShow: function() {
      this.dataTable.fnDraw();
    },

    resizeTable: function() {
      this.dataTable.fnDraw();
    }

  });

});
