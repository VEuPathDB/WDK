wdk.namespace('wdk.views.filter', function(ns) {
  'use strict';

  ns.FilterItemView = wdk.views.View.extend({

    tagName: 'li',

    template: wdk.templates['filter/filter_item.handlebars'],

    events: {
      'click .remove': 'removeItem'
    },

    initialize: function() {
      this.render();
    },

    render: function() {
      var html = this.template({ description: _.result(this.model, 'description') });
      this.$el.html(html);
    },

    removeItem: function(e) {
      this.model.get('field').set('filterValues', null);
    }

  });

});
