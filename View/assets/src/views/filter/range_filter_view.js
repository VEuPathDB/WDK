wdk.namespace('wdk.views.filter', function(ns) {
  'use strict';

  ns.RangeFilterView = wdk.views.View.extend({

    events: {
      'plothover .chart'      : 'handlePlotHover',
      'plotselected .chart'   : 'handlePlotSelected',
      'plotselecting .chart'  : 'handlePlotSelecting',
      'plotunselected .chart' : 'handlePlotUnselected',
      'keyup input'           : 'handleFormChange'
    },

    template: wdk.templates['filter/range_filter.handlebars'],

    plot: null,

    initialize: function(options) {
      this.options = _.extend({
        xAxisTitle: this.model.get('display'),
        yAxisTitle: 'Frequency'
      }, options);
    },

    render: function() {
      var field = this.model;
      var values = field.get('values');
      var filterValues = field.get('filterValues');

      var distribution = _(values).countBy();
      var xdata = _(distribution).keys().map(Number);
      var ydata = _(distribution).values().map(Number);

      var fdistribution = _(field.get('filteredValues')).countBy();
      var xfdata = _(fdistribution).keys().map(Number);
      var yfdata = _(fdistribution).values().map(Number);

      var min = _.min(values);
      var max = _.max(values);
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

      return this;
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
      if (item) {
        var x = item.datapoint[0];
        var y = item.datapoint[1];
        tooltip
          .css({
            display:'inline-block',
            top: item.pageY + 5,
            left: item.pageX + 5
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

      this.model.set('filterValues', {
        min: min,
        max: max
      });
    },

    handlePlotUnselected: function(event) {
      this.$min.val(null);
      this.$max.val(null);

      this.model.set('filterValues', null);
    },

    handleFormChange: function(e) {
      var min = this.$min.val();
      var max = this.$max.val();

      this.plot.setSelection({
        xaxis: {
          from: min,
          to: max
        }
      }, true);

      this.model.set('filterValues', {
        min: min,
        max: max
      });
    }

  });

});
