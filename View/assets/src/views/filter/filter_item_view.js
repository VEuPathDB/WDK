wdk.namespace('wdk.views.filter', function(ns) {
  'use strict';

  ns.FilterItemView = wdk.views.View.extend({

    tagName: 'li',

    template: wdk.templates['filter/filter_item.handlebars'],

    events: {
      'click .remove': 'handleRemove',
      'click .select': 'handleSelect'
    },

    constructor: function(filterService) {
      var restArgs = [].slice.call(arguments, 1);
      this.filterService = filterService;
      wdk.views.View.apply(this, restArgs);
    },

    initialize: function() {
      var term = this.model.get('field');
      this.field = this.filterService.fields.findWhere({ term: term });
      this.render();
    },

    render: function() {
      var html = this.template({
        filter: this.model.condition(),
        field: this.field.attributes
      });
      this.$el.html(html);
    },

    handleRemove: function(e) {
      this.model.collection.remove(this.model);
    },

    handleSelect: function(e) {
      e.preventDefault();
      this.field.select();
    },

    select: function() {
      this.$el.addClass('selected');
    },

    unselect: function() {
      this.$el.removeClass('selected');
    },

    toggleSelect: function(field) {
      field === this.field ? this.select() : this.unselect();
    }

  });

});
