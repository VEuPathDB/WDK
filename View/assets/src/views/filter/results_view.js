wdk.namespace('wdk.views.filter', function(ns) {
  'use strict';

  ns.ResultsView = wdk.views.View.extend({

    events: {
      'mouseover td': 'setTitle'
    },

    template: wdk.templates['filter/results.handlebars'],

    className: 'results',

    constructor: function() {
      Handlebars.registerHelper('property', function(key, context, options) {
        return context[key];
      });
      wdk.views.View.apply(this, arguments);
    },

    initialize: function() {
      this.listenTo(this.model.filteredData, 'reset', this.render);
    },

    render: function() {
      this.$el.html(this.template({
        fields: this.model.fields.toJSON(),
        filteredData: this.model.filteredData.toJSON()
      }));
      return this;
    },

    setTitle: function(e) {
      var td = e.currentTarget;
      if (td.scrollWidth > td.clientWidth) {
        td.setAttribute('title', $(td).text());
      }
    }

  });

});
