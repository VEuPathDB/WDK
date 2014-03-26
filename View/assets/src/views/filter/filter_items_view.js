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
      this.listenTo(this.filterService.filteredData, 'reset', this.updateTotal);
    },

    render: function() {
      var view = this;
      this.model.forEach(function(model) {
        view.addItem.call(view, model, { inRender: true });
      });
      this.updateTotal(this.filterService.filteredData);
    },

    addItem: function(model, options) {
      var itemView = new FilterItemView(this.filterService, { model: model });
      this.$el.append(itemView.$el);
      this.itemViews[model.cid] = itemView;
      if (!options.inRender) {
        itemView.select();
      }
    },

    removeItem: function(model) {
      this.itemViews[model.cid].remove();
      delete this.itemViews[model.cid];
    },

    toggleSelectItems: function(field) {
      _.invoke(this.itemViews, 'toggleSelect', field);
    },

    updateTotal: function(data) {
      this.$el.attr('data-total', data.length);
    }
  });

});
