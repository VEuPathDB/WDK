wdk.namespace('wdk.views.filter', function(ns) {
  'use strict';

  ns.RangeFilterView = wdk.views.core.View.extend({

    plot: null,

    min: null,

    max: null,

    events: {
      'click .read-more a'    : 'expandDescription',
      'plothover .chart'      : 'handlePlotHover',
      'plotselected .chart'   : 'handlePlotSelected',
      'plotselecting .chart'  : 'handlePlotSelecting',
      'plotunselected .chart' : 'handlePlotUnselected',
      'keyup input'           : _.debounce(function(e) {
        this.handleFormChange(e);
      }, 300)
    },

    template: wdk.templates['filter/range_filter.handlebars'],

    constructor: function(filterService) {
      var initArgs = [].slice.call(arguments, 1);
      this.filterService = filterService;
      wdk.views.core.View.apply(this, initArgs);
    },

    initialize: function(options) {
      var filters = this.filterService.filters;
      this.options = options;
      this.listenTo(filters, 'add', this.addFilter);
      this.listenTo(filters, 'remove', this.removeFilter);
    },

    addFilter: function(filter, filters, options) {
      var update = options.origin !== this &&
        filter.get('field') === this.model.get('term');

      if (update) {
        this.$min.val(filter.get('min'));
        this.$max.val(filter.get('max'));
        this.doPlotSelection();
      }
    },

    removeFilter: function(filter, filters, options) {
      var update = options.origin !== this &&
        filter.get('field') === this.model.get('term');

      if (update) {
        this.$min.val(null);
        this.$max.val(null);
        this.doPlotSelection();
      }
    },

    render: function() {
      var field = this.model;
      var filter = this.controller.getFieldFilter(field);
      var filterValues = filter ? filter.pick('min', 'max') : null;

      var distribution = this.model.get('distribution')
        .filter(function(item) { return _.isNumber(item.value); });

      var size = distribution.reduce(function(acc, item) {
        return acc + item.count;
      }, 0);

      var series = distribution
        .reduce(function(acc, item) {
          if (_.isUndefined(item.count)) return acc;
          return acc.concat([ [ Number(item.value), Number(item.count) ] ]);
        }, []);

      var fSeries = distribution
        .reduce(function(acc, item) {
          if (_.isUndefined(item.filteredCount)) return acc;
          return acc.concat([ [ Number(item.value), Number(item.filteredCount) ] ]);
        }, []);

      var values = _.pluck(distribution, 'value');
      var min = this.min = _.min(values);
      var max = this.max = _.max(values);
      var sum = distribution.reduce(function(acc, item) {
        return acc + (item.value * item.count);
      }, 0);
      var avg = (sum / size).toFixed(2);

      var barWidth = (max - min) * 0.005;

      var seriesData = [{
        data: series,
        color: '#000'
      },{
        data: fSeries,
        color: 'red',
        hoverable: false
      }];

      var plotOptions = {
        series: {
          bars: {
            show: true,
            barWidth: barWidth,
            lineWidth: 0,
            align: 'center'
          }
        },
        xaxis: {
          min: Math.min(min, 0),
          max: Math.ceil(max + barWidth),
          tickLength: 0
        },
        grid: {
          clickable: true,
          hoverable: true,
          autoHighlight: false,
          borderWidth: 0
        },
        selection: {
          mode: 'x',
          color: '#66A4E7'
        }
      };

      this.$el.html(this.template({
        field: field.attributes,
        distribution: distribution,
        min: min,
        max: max,
        avg: avg,
        options: this.options
      }));

      this.plot = wdk.$.plot(this.$('.chart'), seriesData, plotOptions);
      this.$min = this.$('input[name="min"]');
      this.$max = this.$('input[name="max"]');
      this.setSelectionTotal(filter);

      if (filterValues) {
        this.$min.val(filterValues.min);
        this.$max.val(filterValues.max);
        this.plot.setSelection({
          xaxis: {
            from: filterValues.min,
            to: filterValues.max
          }
        }, true);
      }

      this.tooltip = this.$('.chart')
        .wdkTooltip({
          prerender: true,
          content: ' ',
          position: {
            target: 'mouse',
            viewport: this.$el,
            my: 'bottom center'
          },
          show: false,
          hide: {
            event: false,
            fixed: true
          }
        });

      $(window).on('resize.range_filter', this.resizePlot.bind(this));

      // activate Read more link if text is overflowed
      var p = this.$('.description p').get(0);
      if (p && p.scrollWidth > p.clientWidth) {
        this.$('.description .read-more').addClass('visible');
      }

      return this;
    },

    expandDescription: function(event) {
      event.preventDefault();
      this.$('.description p').toggleClass('expanded');
    },

    handlePlotClick: function(event, pos, item) {
      if (item) {
        if (item.highlighted) {
          this.plot.unhighlight(item.series, item.datapoint);
          item.highlighted = false;
        } else {
          this.plot.highlight(item.series, item.datapoint);
          item.highlighted = true;
        }
      }
    },

    handlePlotHover: function(event, pos, item) {
      var qtipApi = this.tooltip.qtip('api'),
          previousPoint;

      if (!item) {
        qtipApi.cache.point = false;
        return qtipApi.hide(item);
      }

      previousPoint = qtipApi.cache.point;

      if (previousPoint !== item.dataIndex) {
        qtipApi.cache.point = item.dataIndex;
        qtipApi.set('content.text',
          this.model.get('display') + ': ' + item.datapoint[0] +
          '<br/># ' + this.options.title + ': ' + item.datapoint[1]);
        qtipApi.elements.tooltip.stop(1, 1);
        qtipApi.show(item);
      }
    },

    handlePlotSelecting: function(event, ranges) {
      if (!ranges) return;

      var min = ranges.xaxis.from.toFixed(2);
      var max = ranges.xaxis.to.toFixed(2);

      this.$min.val(min);
      this.$max.val(max);
      this.setSelectionTotal();
    },

    handlePlotSelected: function(event, ranges) {
      var min = Number(ranges.xaxis.from.toFixed(2));
      var max = Number(ranges.xaxis.to.toFixed(2));
      var filters = this.filterService.filters;
      var field = this.model;

      filters.remove(filters.where({ field: field.get('term') }), { origin: this });

      var filter = filters.add({
        field: field.get('term'),
        operation: field.get('filter'),
        min: min,
        max: max
      }, { origin: this });

      this.setSelectionTotal(filter);
    },

    handlePlotUnselected: function() {
      var filters = this.filterService.filters;
      var field = this.model;

      this.$min.val(null);
      this.$max.val(null);

      this.setSelectionTotal();

      filters.remove(filters.where({ field: field.get('term') }), { origin: this });
    },

    handleFormChange: function() {
      var min = this.$min.val() === '' ? null : Number(this.$min.val());
      var max = this.$max.val() === '' ? null : Number(this.$max.val());

      var filters = this.filterService.filters;
      var field = this.model;

      // bail if values aren't changed
      var oldVals = _.result(this.plot.getSelection(), 'xaxis');
      if (oldVals && min === oldVals.from && max === oldVals.to) return;

      filters.remove(filters.where({ field: field.get('term') }), { origin: this });

      if (min === null && max === null) {
        this.setSelectionTotal();
      } else {
        var filter = filters.add({
          field: field.get('term'),
          operation: field.get('filter'),
          min: min,
          max: max
        }, { origin: this });
        this.setSelectionTotal(filter);
      }
      this.doPlotSelection();
    },

    doPlotSelection: function() {
      var min = this.$min.val() === '' ? null : this.$min.val();
      var max = this.$max.val() === '' ? null : this.$max.val();
      if (min === null && max === null) {
        this.plot.clearSelection(true);
      } else {
        this.plot.setSelection({
          xaxis: {
            from: min === null ? this.min : min,
            to: max === null ? this.max : max
          }
        }, true);
      }
    },

    setSelectionTotal: function(filter) {
      if (filter) {
        this.filterService.getFilteredData({ filters: [ filter ] })
          .then(function(filteredData) {
            this.$('.selection-total')
              .html('(' + filteredData.length + ' items selected)');
          }.bind(this));
      } else {
        this.$('.selection-total').empty();
      }
    },

    resizePlot: _.debounce(function resizePlot() {
      this.plot.resize();
      this.plot.setupGrid();
      this.plot.draw();
      this.doPlotSelection();
    }, 300),

    didShow: function() {
      this.resizePlot();
      $(window).on('resize.range_filter', this.resizePlot.bind(this));
    },

    didHide: function() {
      $(window).off('resize.range_filter');
    },

    undelegateEvents: function() {
      $(window).off('.range_filter');
      wdk.views.core.View.prototype.undelegateEvents.apply(this, arguments);
    }

  });

});
