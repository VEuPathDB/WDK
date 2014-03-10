wdk.namespace('wdk.views.filter', function(ns) {
  'use strict';

  var FilterItemView = ns.FilterItemView;

  ns.FilterItemsView = wdk.views.View.extend({
    itemViews: null,

    tagName: 'ul',

    initialize: function() {
      this.itemViews = {};
      this.listenTo(this.model, 'add', this.addItem);
      this.listenTo(this.model, 'remove', this.removeItem);
      this.render();
    },

    render: function() {
      this.model.forEach(this.addItem.bind(this));
    },

    addItem: function(model) {
      var itemView = new FilterItemView({ model: model });
      this.$el.append(itemView.$el);
      this.itemViews[model.cid] = itemView;
    },

    removeItem: function(model) {
      this.itemViews[model.cid].remove();
      delete this.itemViews[model.cid];
    }
  });

});
