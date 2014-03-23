wdk.namespace('wdk.views.filter', function(ns) {
  'use strict';

  ns.RangeFilterView = wdk.views.View.extend({

    plot: null,

    min: null,

    max: null,

    events: {
      'plothover .chart'      : 'handlePlotHover',
      'plotselected .chart'   : 'handlePlotSelected',
      'plotselecting .chart'  : 'handlePlotSelecting',
      'plotunselected .chart' : 'handlePlotUnselected',
      'keyup input'           : 'handleFormChange'
    },

    template: wdk.templates['filter/range_filter.handlebars'],

    constructor: function(filterService) {
      var initArgs = [].slice.call(arguments, 1);
      this.filterService = filterService;
      wdk.views.View.apply(this, initArgs);
    },

    initialize: function(options) {
      this.options = _.extend({
        xAxisTitle: this.model.get('display'),
        yAxisTitle: 'Frequency'
      }, options);
      this.listenTo(this.model, 'change', function(field, options) {
          this.render();
      });
      this.listenTo(this.filterService.filters, 'add remove', function(filter, filters, opts) {
        if (filter.get('field') === this.model.get('term') && opts.origin !== this) {
          this.render();
        }
      });
    },

    render: function() {
      var field = this.model;
      var filterService = this.filterService;
      var filter = filterService.filters.findWhere({
        field: field.get('term')
      });
      var filterValues = filter ? filter.pick('min', 'max') : null;

      var values = field.get('values').map(Number);

      var distribution = _(values).countBy();
      var xdata = _(distribution).keys().map(Number);
      var ydata = _(distribution).values().map(Number);

      var fdistribution = _(field.get('filteredValues')).countBy();
      var xfdata = _(fdistribution).keys().map(Number);
      var yfdata = _(fdistribution).values().map(Number);

      var min = this.min = _.min(values);
      var max = this.max = _.max(values);
      var sum = _(values).reduce(function(sum, num) {
        return sum + num;
      }, 0);
      var avg = (sum / _.size(values)).toFixed(2);

      // [ [x1, y1], [x2, y2], ... ]
      var seriesData = [{
        data: _.zip(xdata, ydata),
        color: '#000'
      },{
        data: _.zip(xfdata, yfdata),
        color: 'red'
      }];

      var plotOptions = {
        series: {
          bars: {
            show: true,
            barWidth: .5,
            lineWidth: 0,
            align: 'center'
          }
        },
        xaxis: {
          //mode: 'categories',
          min: min - 10,
          max: max + 10,
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
          color: '#66A4E7' // was '#2a6496'
        }
      };

      this.$el.html(this.template({
        field: field.attributes,
        options: this.options,
        distribution: distribution,
        min: min,
        max: max,
        avg: avg
      }));

      this.plot = wdk.$.plot(this.$('.chart'), seriesData, plotOptions);

      this.$min = this.$('input[name="min"]');
      this.$max = this.$('input[name="max"]');

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

      var resizePlot = _.debounce(this.resizePlot.bind(this), 100);
      $(window).on('resize.range_filter', resizePlot);

      return this;
    },

    didShow: function() {
      this.resizePlot();
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
      var tooltip = this.$('.chart-tooltip');
      var offset = this.$el.offset();
      if (item) {
        var x = item.datapoint[0];
        var y = item.datapoint[1];
        tooltip
          .css({
            display:'inline-block',
            top: item.pageY - offset.top + 5,
            left: item.pageX - offset.left + 5
          })
          .html('<strong>' + this.model.get('display') + '</strong> ' + x +
                '<br><strong>Frequency</strong> ' + y);
      } else {
        tooltip.css('display', 'none');
      }
    },

    handlePlotSelecting: function(event, ranges) {
      if (!ranges) return;

      var min = ranges.xaxis.from.toFixed(2);
      var max = ranges.xaxis.to.toFixed(2);

      this.$min.val(min);
      this.$max.val(max);
    },

    handlePlotSelected: function(event, ranges) {
      var min = ranges.xaxis.from.toFixed(2);
      var max = ranges.xaxis.to.toFixed(2);
      var filters = this.filterService.filters;
      var field = this.model;

      filters.remove(filters.where({ field: field.get('term') }), { origin: this });
      filters.add({
        field: field.get('term'),
        operation: field.get('filter'),
        min: min,
        max: max
      }, { origin: this });
    },

    handlePlotUnselected: function(event) {
      var filters = this.filterService.filters;
      var field = this.model;

      this.$min.val(null);
      this.$max.val(null);

      filters.remove(filters.where({ field: field.get('term') }), { origin: this });
    },

    handleFormChange: function(e) {
      var min = this.$min.val() === '' ? null : this.$min.val();
      var max = this.$max.val() === '' ? null : this.$max.val();
      var filters = this.filterService.filters;
      var field = this.model;

      filters.remove(filters.where({ field: field.get('term') }), { origin: this });

      if (min === null && max === null) {
        this.plot.clearSelection(true);
      } else {
        this.plot.setSelection({
          xaxis: {
            from: min === null ? this.min : min,
            to: max === null ? this.max : max
          }
        }, true);

        filters.add({
          field: field.get('term'),
          operation: field.get('filter'),
          min: min,
          max: max
        }, { origin: this });
      }
    },

    resizePlot: function() {
      this.plot.resize();
      this.plot.setupGrid();
      this.plot.draw();
      this.handleFormChange();
    },

    didDestroy: function() {
      $(window).off('resize.range_filter');
    }

  });

});
