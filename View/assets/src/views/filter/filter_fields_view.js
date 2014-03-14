wdk.namespace('wdk.views.filter', function(ns) {
  'use strict';

  var FieldListView = ns.FieldListView;
  var FieldDetailView = ns.FieldDetailView;

  /**
   * A FilterView is composed of several components:
   * - A selectable list of available filters for a given data collection.
   * - A detail view for a specific filter.
   *
   */
  ns.FilterFieldsView = wdk.views.View.extend({

    template: wdk.templates['filter/filter_fields.handlebars'],

    className: 'filters context ui-helper-clearfix',

    initialize: function() {
      this.fieldList = new FieldListView({ model: this.model });
      this.fieldDetail = new FieldDetailView({ model: this.model });

      this.listenTo(this.fieldList, 'select', this.renderDetail);
      this.listenTo(this.model.fields, 'change:filterValues', this.setFilter);

      this.render();
    },

    render: function() {
      this.$el.html(this.template(this.model.filteredData));
      this.fieldList.setElement(this.$('.field-list')).render();
      this.fieldDetail.setElement(this.$('.field-detail')).render();
    },

    renderDetail: function(field) {
      this.fieldDetail.render(field);
    },

    // FIXME This logic should be in the Field model
    setFilter: function(field, filterValues) {
      var filters = this.model.filters;

      // remove previous filters for this field
      filters.remove(filters.where({ field: field }));

      if (filterValues) {
        var filter = _.extend({
          field: field,
          operation: field.get('filter')
        }, filterValues);

        filters.add(filter);
      }
    }

  });

});
