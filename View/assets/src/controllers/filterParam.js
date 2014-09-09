/* global RSVP */
wdk.namespace('wdk.controllers', function(ns) {
  'use strict';

  // imports
  var LocalFilterService = wdk.models.filter.LocalFilterService;
  var FilterItemsView = wdk.views.filter.FilterItemsView;
  var FilterView = wdk.views.filter.FilterView;
  var Fields = wdk.models.filter.Fields;
  var Field = wdk.models.filter.Field;

  // helpers
  var countByValues = _.compose(_.countBy, _.values);

  /**
   * options:
   *  - data: Array of data objects to filter
   *  - fields: Array of metadata terms
   *  - filters: Array of filter objects
   *  - ignored: Array of data ids to ignore
   *  - title: String name used in UI
   *  - trimMetadataTerms: Boolean, when true remove parents w/ one child
   *  - defaultColumns: Array of field names to show in results view
   */
  ns.FilterParam = wdk.views.View.extend({

    className: 'filter-param',

    initialize: function(options) {
      this.data = options.data;
      this.metadata = options.metadata || {};
      this.ignored = options.ignored || [];
      this.title = options.title || 'Items';
      this.questionName = options.questionName;
      this.name = options.name;
      this.fields = new Fields(options.fields);
      this.trimMetadataTerms = Boolean(options.trimMetadataTerms);
      this.defaultColumns = options.defaultColumns;
      this.filterService = new LocalFilterService({
        data: this.data,
        metadata: this.metadata,
      });

      this.listenTo(this.filterService, 'change:filteredData', this.updateValue);
      this.listenTo(this.filterService, 'change:filteredData', this._setSelectedFieldDistribution);

      var itemsView = new FilterItemsView(this.filterService, {
        model: this.filterService.filters,
        controller: this
      });

      var filterView = new FilterView({
        model: this.filterService,
        controller: this,
        defaultColumns: this.defaultColumns,
        trimMetadataTerms: this.trimMetadataTerms
      });

      var metadataPromises = (options.filters || [])
        .map(function(filter) {
          return this.getMetadata(this.fields.get(filter.field));
        }.bind(this));

      RSVP.all(metadataPromises).then(function() {
        this.filterService.filters.reset(options.filters);
        this.$el
          .append(itemsView.render().el)
          .append(filterView.render().el);
      }.bind(this));
    },

    // Update the UI with new data. This gets triggered when user changes the
    // value of a dependent parameter, so we can skip the init stuff.
    update: function(data) {
      this.filterService.reset(data);
      return this;
    },

    isIgnored: function(datum) {
      var term = _.isObject(datum) ? datum.term : datum;
      return _.contains(this.ignored, term);
    },

    toggleIgnored: function(datum, isIgnored) {
      var term = _.isObject(datum) ? datum.term : datum;
      var index = _.indexOf(this.ignored, term);
      var doUpdate = false;

      if (isIgnored && index === -1) {
        this.ignored.push(term);
        doUpdate = true;
      } else if (!isIgnored && index > -1) {
        this.ignored.splice(index, 1);
        doUpdate = true;
      }
      if (doUpdate) this.updateValue();
      return this;
    },

    // This is the user's selection of data with all
    // filters applied, and all ignored values removed.
    getSelectedData: function() {
      var ignored = this.ignored;
      return this.filterService.get('filteredData')
        .reduce(function(acc, data) {
          if (!_.contains(ignored, data.term)) acc.push(data);
          return acc;
        }, []);
    },

    // Trigger events with selected values.
    // value - serialization of param
    // selectedData - just the values selected
    updateValue: function() {
      var data = this.getSelectedData();
      var value = {
        values: _.pluck(data, 'term'),
        ignored: this.ignored,
        filters: this.filterService.filters
      };
      this.trigger('change:value', this, value);
      this.trigger('change:selectedData', this, data);
      return this;
    },

    _setSelectedFieldDistribution: function() {
      var field = this.selectedField;
      return field && this.getFieldDistribution(field)
        .then(function(distribution) {
          field.set('distribution', distribution);
        });
    },

    // returns a promise that resolves to metadata for a property:
    //     [ { data_term: metadata_value }, ... ]
    getMetadata: function(field) {
      return new RSVP.Promise(function(resolve, reject) {
        var term = field.get('term');

        // if it's cached, return a promise that resolves immediately
        if (this.metadata[term]) {
          resolve(this.metadata[term]);
        }

        var metadataUrl = wdk.webappUrl('getMetadata.do');
        var metadataUrlParams = {
          questionFullName: this.questionName,
          name: this.name,
          json: true,
          property: term
        };

        $.getJSON(metadataUrl, metadataUrlParams)
          .then(function(metadata) {
            metadata = _.indexBy(metadata, 'sample');
            // cache metadata and transform to a dict
            this.metadata[term] = this.data
              .reduce(function(acc, d) {
                acc[d.term] = metadata[d.term]
                  ? metadata[d.term].value
                  : Field.UNKNOWN_VALUE;
                return acc;
              }, {});
            resolve(this.metadata[term]);
          }.bind(this))
          .fail(reject);
      }.bind(this));
    },

    // returns a promse which resolves to an array of objects:
    // { value, count, filteredCount }
    getFieldDistribution: function(field) {
      var term = field.get('term');

      // Retrieve metadata and filtered data and return a promise
      return RSVP.hash({
        metadata: this.getMetadata(field),
        filteredData: this.filterService.getFilteredData({
          filters: this.filterService.filters,
          omit: [ term ]
        })
      }).then(function(results) {
        var filteredMetadata = results.filteredData
          .reduce(function(acc, fd) {
            acc[fd.term] = results.metadata[fd.term];
            return acc;
          }, {});
        var counts = countByValues(results.metadata);
        var filteredCounts = countByValues(filteredMetadata);

        return _.keys(counts).map(function(value) {
          return {
            value: value,
            count: Number(counts[value]),
            filteredCount: filteredCounts[value]
          };
        });
      }.bind(this));
    },

    selectField: function(field) {
      this.selectedField = field;
      this._setSelectedFieldDistribution().then(function() {
        this.trigger('select:field', field);
      }.bind(this));
      return this;
    },

    getFieldFilter: function(field) {
      return this.filterService.filters.findWhere({
        field: field.get('term')
      });
    }

  });

});
