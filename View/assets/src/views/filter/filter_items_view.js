wdk.namespace('wdk.views.filter', function(ns) {
  'use strict';

  var FilterItemView = ns.FilterItemView;

  ns.FilterItemsView = wdk.views.View.extend({
    itemViews: null,

    className: 'filter-items',

    tagName: 'ul',

    constructor: function(filterService) {
      var restArgs = [].slice.call(arguments, 1);
      this.filterService = filterService;
      wdk.views.View.apply(this, restArgs);
    },

    initialize: function() {
      this.itemViews = {};
      this.listenTo(this.model, 'add', this.addItem);
      this.listenTo(this.model, 'remove', this.removeItem);
      this.listenTo(this.filterService.fields, 'select', this.toggleSelectItems);
    },

    render: function() {
      this.model.forEach(this.addItem.bind(this));
    },

    addItem: function(model) {
      var itemView = new FilterItemView(this.filterService, { model: model });
      this.$el.append(itemView.$el);
      this.itemViews[model.cid] = itemView;
    },

    removeItem: function(model) {
      this.itemViews[model.cid].remove();
      delete this.itemViews[model.cid];
    },

    toggleSelectItems: function(field) {
      _.invoke(this.itemViews, 'toggleSelect', field);
    }
  });

});
