wdk.namespace('wdk.views.filter', function(ns) {
  'use strict';

  ns.FilterCollapsedView = wdk.views.View.extend({

    template: wdk.templates['filter/filter_collapsed.handlebars'],

    initialize: function() {
      this.listenTo(this.model.filters, 'add remove', this.render);
    },

    render: function() {
      this.$el.html(this.template(this.model));
      return this;
    }
  });

});
