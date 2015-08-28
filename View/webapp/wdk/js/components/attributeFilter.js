/* global _ */
import React from 'react';
import FixedDataTable from 'fixed-data-table';
import Loading from '../client/components/Loading';
import Tooltip from '../client/components/Tooltip';
import Dialog from '../client/components/Dialog';
import Table from '../client/components/Table';

function noop(){}

var dateStringRe = /^(\d{4})(?:-(\d{2})(?:-(\d{2}))?)?$/;

/**
 * Returns an strftime style format string.
 */
function getFormatFromDateString(dateString) {
  var matches = dateString.match(dateStringRe);
  if (matches !== null) {
    var [ , Y, m, d ] = matches;
    return  d !== undefined ? '%Y-%m-%d'
          : m !== undefined ? '%Y-%m'
          : '%Y';
  }
}

/**
 * Returns a formatted date.
 *
 * @param {string} format strftime style format string
 * @param {Date} date
 */
function formatDate(format, date) {
  if (!(date instanceof Date)) {
    date = new Date(date);
  }
  return format
  .replace(/%Y/, date.getFullYear())
  .replace(/%m/, date.getMonth() + 1)
  .replace(/%d/, date.getDate());
}

wdk.namespace('wdk.components.attributeFilter', function(ns) {
  'use strict';

  var { PropTypes } = React;
  var { Column } = FixedDataTable;
  var { Fields } = wdk.models.filter;


  var FilterList = React.createClass({

    propTypes: {
      onFilterSelect: PropTypes.func.isRequired,
      onFilterRemove: PropTypes.func.isRequired,
      filters: PropTypes.array.isRequired,
      filteredDataCount: PropTypes.number.isRequired,
      dataCount: PropTypes.number.isRequired,
      selectedField: PropTypes.object
    },

    handleFilterSelectClick: function(filter, event) {
      event.preventDefault();
      this.props.onFilterSelect(filter.field);
    },

    handleFilterRemoveClick: function(filter, event) {
      event.preventDefault();
      this.props.onFilterRemove(filter);
    },

    render: function() {
      var { filteredDataCount, dataCount, filters, selectedField } = this.props;

      return (
        <div className="filter-items-wrapper">
          <span style={{ fontWeight: 'bold', padding: '.6em 0 .8em 0', display: 'inline-block' }}>
            {filteredDataCount} of {dataCount} selected
          </span>
          <ul style={{display: 'inline-block', paddingLeft: '.2em'}} className="filter-items">
            {_.map(filters, filter => {
              var className = _.result(selectedField, 'term') === filter.field.term ? 'selected' : '';
              var handleSelectClick = _.partial(this.handleFilterSelectClick, filter);
              var handleRemoveClick = _.partial(this.handleFilterRemoveClick, filter);

              return (
                <li key={filter.field.term} className={className}>
                  <div className="ui-corner-all">
                    <a className="select"
                      onClick={handleSelectClick}
                      href={'#' + filter.field}
                      title={filter.display}>{filter.display}</a>
                    {/* Use String.fromCharCode to avoid conflicts with
                        character ecoding. Other methods detailed at
                        http://facebook.github.io/react/docs/jsx-gotchas.html#html-entities
                        cause JSX to encode. String.fromCharCode ensures that
                        the encoding is done in the browser */}
                    <span className="remove"
                      onClick={handleRemoveClick}
                      title="remove restriction">{String.fromCharCode(215)}</span>
                  </div>
                </li>
              );
            })}
          </ul>
        </div>
      );
    }
  });

  var FieldList = React.createClass({
    propTypes: {
      fields: PropTypes.array.isRequired,
      onFieldSelect: PropTypes.func.isRequired,
      selectedField: PropTypes.object,
      trimMetadataTerms: PropTypes.bool
    },

    render: function() {
      var treeOpts = _.pick(this.props, 'trimMetadataTerms');
      var fieldsToTreeNodes = _.partial(Fields.getTree, treeOpts);
      var { fields } = this.props;
      var restProps = _.omit(this.props, 'fields', 'trimMetadataTerms');

      var treeNodes = fieldsToTreeNodes(fields);

      return (
        <div className="field-list">
          <div className="toggle-links"> </div>
          <FieldTree {...restProps} ItemComponent={PanelItem} treeNodes={treeNodes}/>
        </div>
      );
    }
  });

  var FieldFilter = React.createClass({
    propTypes: {
      displayName: PropTypes.string,
      field: PropTypes.object,
      filter: PropTypes.object,
      distribution: PropTypes.array,
      onAddFilter: PropTypes.func
    },

    getDefaultProps: function() {
      return {
        displayName: 'Items'
      };
    },

    render: function() {
      var FieldDetail = getFieldDetail(this.props.field);

      return (
        <div className="field-detail">
          { !this.props.field
            ? <EmptyField displayName={this.props.displayName}/>
            : (
                <div>
                  <h3>{this.props.field.display}</h3>
                  <div className="description">{this.props.field.description}</div>
                  <FieldDetail key={this.props.field.term}
                    displayName={this.props.displayName}
                    field={this.props.field}
                    distribution={this.props.distribution}
                    filter={this.props.filter}
                    onAddFilter={this.props.onAddFilter}/>
                  <div className="legend">
                    <div>
                      <div className="bar"><div className="fill"></div></div>
                      <div className="label">All {this.props.displayName}</div>
                    </div>
                    <div>
                      <div className="bar"><div className="fill filtered"></div></div>
                      <div className="label">{this.props.displayName} remaing when <em>other</em> criteria has been applied.</div>
                    </div>
                  </div>
                </div>
              )
          }
        </div>
      );
    }
  });

  var FilteredData = React.createClass({

    propTypes: {
      tabWidth: PropTypes.number,
      filteredData: PropTypes.array,
      fields: PropTypes.array,
      selectedFields: PropTypes.array,
      ignored: PropTypes.array,
      metadata: PropTypes.object,
      displayName: PropTypes.string,
      onFieldsChange: PropTypes.func,
      onIgnored: PropTypes.func,
      onSort: PropTypes.func,
      sortTerm: PropTypes.string,
      sortDirection: PropTypes.string
    },

    getInitialState: function() {
      return {
        dialogIsOpen: false,
        pendingSelectedFields: this.props.selectedFields
      };
    },

    componentWillReceiveProps: function(nextProps) {
      this.setState({
        pendingSelectedFields: nextProps.selectedFields
      });
    },

    openDialog: function(event) {
      event.preventDefault();
      this.setState({
        dialogIsOpen: true
      });
    },

    handleDialogClose: function() {
      this.setState({
        dialogIsOpen: false,
        pendingSelectedFields: this.props.selectedFields
      });
    },

    handleFieldSelect: function() {
      var form = this.refs.fieldSelector.getDOMNode();
      var fields = this.props.fields;
      var pendingSelectedFields = [].slice.call(form.field)
        .filter(field => field.checked)
        .map(field => fields.filter(f => f.term == field.value)[0]);
      this.setState({
        pendingSelectedFields: pendingSelectedFields
      });
    },

    handleFieldSubmit: function(event) {
      event.preventDefault();
      this.props.onFieldsChange(this.state.pendingSelectedFields);
      this.setState({
        dialogIsOpen: false
      });
    },

    handleSort: function(term) {
      this.props.onSort(term);
    },

    handleHideColumn: function(term) {
      var nextFields = this.props.selectedFields.filter(field => field.term != term)
      this.props.onFieldsChange(nextFields);
    },

    isIgnored: function(field) {
      return this.props.ignored.indexOf(field.term) > -1;
    },

    getRow: function(index) {
      return this.props.filteredData[index];
      // return _.cloneDeep(this.props.filteredData[index]);
    },

    getRowClassName: function(index) {
      return this.props.filteredData[index].isIgnored
        ? 'wdk-AttributeFilter-ItemIgnored'
        : 'wdk-AttributeFilter-Item';
    },

    getCellData: function(cellDataKey, rowData) {
      return cellDataKey == '__primary_key__' ? rowData :
        this.props.metadata[cellDataKey][rowData.term].join(', ');
    },

    renderPk: function(cellData) {
      var handleIgnored = event => {
        this.props.onIgnored(cellData, !event.target.checked);
      };
      return (
        <label>
          <input
            type="checkbox"
            checked={!cellData.isIgnored}
            onChange={handleIgnored}
          />
          {' ' + cellData.display + ' '}
        </label>
      );
    },

    render: function() {
      var { fields, selectedFields, metadata, filteredData, displayName, tabWidth, totalSize } = this.props;
      var { dialogIsOpen, pendingSelectedFields } = this.state;

      if (!tabWidth) return null;

      return (
        <div className="wdk-AttributeFilter-FilteredData">

          <div className="ui-helper-clearfix" style={{padding: 10}}>
            <div style={{float: 'left'}}>Showing {filteredData.length} of {totalSize} {displayName}</div>
            <div style={{float: 'right'}}>
              <button onClick={this.openDialog}>Add Columns</button>
            </div>
          </div>

          <Dialog
            modal={true}
            open={dialogIsOpen}
            onClose={this.handleDialogClose}
            title="Select Columns"
          >
            <div>
              <form ref="fieldSelector" onSubmit={this.handleFieldSubmit}>
                <div style={{textAlign: 'center', padding: 10}}>
                  <button>Update Columns</button>
                </div>
                <ul style={{listStyle: 'none'}}>
                  {fields.map(field => {
                    return (
                      <li>
                        <label>
                          <input
                            type="checkbox"
                            name="field"
                            checked={pendingSelectedFields.indexOf(field) > -1}
                            value={field.term}
                            onChange={this.handleFieldSelect}
                          />
                          {' ' + field.display + ' '}
                        </label>
                      </li>
                    );
                  })}
                </ul>
                <div style={{textAlign: 'center', padding: 10}}>
                  <button>Update Columns</button>
                </div>
              </form>
            </div>
          </Dialog>

          <Table
            width={tabWidth - 10}
            maxHeight={500}
            rowsCount={filteredData.length}
            rowHeight={25}
            rowGetter={this.getRow}
            rowClassNameGetter={this.getRowClassName}
            headerHeight={30}
            onSort={this.handleSort}
            onHideColumn={this.handleHideColumn}
            sortDataKey={this.props.sortTerm}
            sortDirection={this.props.sortDirection}
          >
          <Column
            label="Name"
            dataKey="__primary_key__"
            fixed={true}
            width={200}
            cellDataGetter={this.getCellData}
            cellRenderer={this.renderPk}
            isRemovable={false}
            isSortable={true}
          />
          {selectedFields.map(field => {
            return (
              <Column
                label={field.display}
                dataKey={field.term}
                width={200}
                cellDataGetter={this.getCellData}
                isRemovable={true}
                isSortable={true}
              />
            );
          })}
          </Table>
        </div>
      );
    }
  });

  var AttributeFilter = React.createClass({

    propTypes: {
      actions: PropTypes.object.isRequired,
      store: PropTypes.object.isRequired,
      displayName: PropTypes.string,
      trimMetadataTerms: PropTypes.bool
    },

    getDefaultProps: function() {
      return {
        displayName: 'Items',
        trimMetadataTerms: false
      };
    },

    getInitialState: function() {
      return Object.assign({
        sortTerm: '__primary_key__',
        sortDirection: 'ASC',
        collapsed: false,
      }, this.props.store.getState());
    },

    componentDidMount: function() {
      var $node = $(this.getDOMNode());
      this.props.store.on('change', function() {
        var newState = this.props.store.getState();
        this.setState(newState);
      }, this);
      $node.find('.filter-param-tabs').tabs({
        activate: function(event, ui) {
          this.setState({
            tabWidth: ui.newPanel.width()
          });
        }.bind(this)
      });
    },

    handleSelectFieldClick: function(field, event) {
      event.preventDefault();
      this.props.actions.selectField(field);
    },

    handleRemoveFilterClick: function(filter, event) {
      event.preventDefault();
      this.props.actions.removeFilter(filter);
    },

    handleCollapseClick: function(event) {
      event.preventDefault();
      this.setState({
        collapsed: true
      });
    },

    handleExpandClick: function(event) {
      event.preventDefault();
      this.setState({
        collapsed: false
      });
    },

    handleFieldsChange: function(fields) {
      this.props.actions.updateColumns(fields);
    },

    handleIgnored: function(datum, ignored) {
      if (ignored) this.props.actions.addIgnored(datum);
      else this.props.actions.removeIgnored(datum);
    },

    handleSort: function(fieldTerm) {
      var { sortTerm, sortDirection } = this.state;

      var direction = fieldTerm == sortTerm && sortDirection == 'ASC'
        ? 'DESC' : 'ASC';

      this.setState({
        sortTerm: fieldTerm,
        sortDirection: direction
      });
    },

    render: function() {
      var {
        data,
        filteredData,
        fields,
        columns,
        ignored,
        filters,
        invalidFilters,
        selectedField,
        distributionMap,
        isLoading,
        tabWidth,
        sortTerm,
        sortDirection
      } = this.state;

      var displayName = this.props.displayName;
      var selectedFilter = _.find(filters, filter => {
        return filter.field.term === _.result(selectedField, 'term');
      });

      var actions = this.props.actions;
      var metadata = this.props.store.metadata;

      var filteredNotIgnored = filteredData.filter(datum => !datum.isIgnored);

      var sortedFilteredData = _.sortBy(filteredData, function(datum) {
        var term = datum.term;
        return sortTerm == '__primary_key__' ? term : metadata[sortTerm][term];
      });

      if (sortDirection == 'DESC') sortedFilteredData.reverse();

      return (
        <div>
          {isLoading ? <Loading className="wdk-AttributeFilter-Loading" radius={4}/> : null}
          <FilterList
            onFilterSelect={actions.selectField}
            onFilterRemove={actions.removeFilter}
            filters={filters}
            filteredDataCount={filteredNotIgnored.length}
            dataCount={data.length}
            selectedField={selectedField}/>

          <InvalidFilterList filters={invalidFilters}/>

          <div className="filter-view">
            <button onClick={this.handleExpandClick}
              style={{
                display: !this.state.collapsed ? 'none' : 'block'
              }} >Select {displayName}</button>

            {/* Tabs */}

            <div className="filter-param-tabs" style={{ display: this.state.collapsed ? 'none' : 'block' }}>
              <ul className="wdk-AttributeFilter-TabNav">
                <li><a href="#filters">Select {displayName}</a></li>
                <li><a href="#data">View selected {displayName} ({filteredData.length})</a></li>
                <li>
                  <span
                    className="wdk-AttributeFilter-Collapse link"
                    title="Hide selection tool"
                    onClick={this.handleCollapseClick}
                  >Collapse</span>
                </li>
              </ul>


              {/* Main selection UI */}
              <div id="filters">
                <div className="filters ui-helper-clearfix">
                  <FieldList
                    fields={fields}
                    onFieldSelect={actions.selectField}
                    selectedField={selectedField}
                    trimMetadataTerms={this.props.trimMetadataTerms}/>

                  <FieldFilter
                    displayName={displayName}
                    field={selectedField}
                    filter={selectedFilter}
                    distribution={distributionMap[_.result(selectedField, 'term')]}
                    onAddFilter={actions.addFilter}/>
                </div>
              </div>

              {/* Results table */}

              <div id="data">
                <FilteredData
                  tabWidth={tabWidth}
                  displayName={displayName}
                  onFieldsChange={this.handleFieldsChange}
                  onIgnored={this.handleIgnored}
                  onSort={this.handleSort}
                  sortTerm={sortTerm}
                  sortDirection={sortDirection}
                  filteredData={sortedFilteredData}
                  totalSize={data.length}
                  selectedFields={columns}
                  fields={fields}
                  ignored={ignored}
                  metadata={metadata}/>
              </div>
            </div>
          </div>
        </div>
      );
    }
  });

  var InvalidFilterList = React.createClass({
    render: function() {
      var { filters } = this.props;

      if (_.isEmpty(filters)) return null;

      return (
        <div className="invalid-values">
          <p>Some of the options you previously selected are no longer available:</p>
          <ul>
            {_.map(filters, function(filter) {
              return (
                <li className="invalid">{filter.display}</li>
              );
            }, this)}
          </ul>
        </div>
      );
    }
  });

  var FieldTree = React.createClass({
    propTypes: {
      treeNodes: PropTypes.array.isRequired,
      onFieldSelect: PropTypes.func.isRequired,
      ItemComponent: PropTypes.func.isRequired,
      selectedField: PropTypes.object
    },

    render: function() {
      var { treeNodes } = this.props;
      var restProps = _.omit(this.props, 'treeNodes');
      return (
        <ul role="tree">
          {treeNodes.map(node => {
            return (<TreeNode key={node.field.term} node={node} {...restProps}/>);
          }, this)}
        </ul>
      );
    }
  });

  var TreeNode = React.createClass({
    propTypes: {
      node: PropTypes.object.isRequired,
      ItemComponent: PropTypes.func.isRequired,
      onFieldSelect: PropTypes.func.isRequired,
      selectedField: PropTypes.object,
    },

    getInitialState: function() {
      return {
        isCollapsed: true
      };
    },

    handleToggleClick: function() {
      this.setState({
        isCollapsed: !this.state.isCollapsed
      });
    },

    render: function() {
      var { node } = this.props;
      var restProps = _.omit(this.props, 'node');
      var { ItemComponent } = this.props;

      var className = _.result(this.props.selectedField, 'term') === node.field.term
        ? 'active' : '';

      return (
        <li key={node.field.term} className={className}>
          { node.children
            ? [
              <h4 onClick={_.partial(this.handleToggleClick, node)}
                key={node.field.term}
                role="treeitem"
                className={this.state.isCollapsed ? "collapsed link" : "link"}>{node.field.display}</h4>,
                <div key={node.field.term + '-children'}>
                  <ul>
                    {_.map(node.children, child => {
                      return (<TreeNode key={child.field.term} node={child} {...restProps}/>);
                    }, this)}
                  </ul>
                </div>
              ]
            : <div role="treeitem"><ItemComponent {...restProps} field={node.field}/></div>
          }
        </li>
      );
    }

  });


  var PanelItem = React.createClass({
    propTypes: {
      field: PropTypes.object.isRequired,
      onFieldSelect: PropTypes.func.isRequired
    },

    handleClick: function(event) {
      event.preventDefault();
      this.props.onFieldSelect(this.props.field);
    },

    render: function() {
      return (
        <a onClick={this.handleClick} href={"#" + this.props.field.term}>
        {this.props.field.display}</a>
      );
    }

  });

  // Reusable histogram field component. The parent component
  // is responsible for preparing the data.

  var unwrapXaxisRange = function unwrap(flotRanges) {
    if (flotRanges == null) {
      return { min: null, max: null };
    }

    var { from, to } = flotRanges.xaxis;
    var min = Number(from.toFixed(2));
    var max = Number(to.toFixed(2));
    return { min, max };
  };

  var distributionEntryPropType = React.PropTypes.shape({
    value: React.PropTypes.number.isRequired,
    count: React.PropTypes.number.isRequired,
    filteredCount: React.PropTypes.number.isRequired
  });

  var Histogram = React.createClass({

    propTypes: {
      distribution: React.PropTypes.arrayOf(distributionEntryPropType).isRequired,
      selectedMin: React.PropTypes.number,
      selectedMax: React.PropTypes.number,
      chartType: React.PropTypes.oneOf([ 'number', 'date' ]).isRequired,
      timeformat: React.PropTypes.string.isRequired,
      xaxisLabel: React.PropTypes.string,
      yaxisLabel: React.PropTypes.string,

      onSelected: React.PropTypes.func,
      onSelecting: React.PropTypes.func,
      onUnselected: React.PropTypes.func,
    },

    getDefaultProps() {
      return {
        xaxisLabel: 'X-Axis',
        yaxisLabel: 'Y-Axis',
        selectedMin: null,
        selectedMax: null,
        onSelected: noop,
        onSelecting: noop,
        onUnselected: noop
      };
    },

    getInitialState() {
      // Set default yAxis max based on distribution
      var yaxisMax = this.computeYAxisMax();
      return { yaxisMax };
    },

    computeYAxisMax() {
      var counts = this.props.distribution.map(entry => entry.count);
      // Reverse sort, then pull out first and second highest values
      var [ max, nextMax ] = counts.sort((a, b) => a < b ? 1 : -1);
      var yaxisMax = max >= nextMax * 2 ? nextMax : max;
      return yaxisMax + yaxisMax * 0.1;
    },

    componentWillMount() {
      this.handleResize = _.throttle(this.handleResize, 100);
    },

    componentDidMount: function() {
      $(window).on('resize', this.handleResize);
      $(this.getDOMNode())
        .on('plotselected .chart', this.handlePlotSelected)
        .on('plotselecting .chart', this.handlePlotSelecting)
        .on('plotunselected .chart', this.handlePlotUnselected)
        .on('plothover .chart', this.handlePlotHover);

      this.createPlot();
      this.createTooltip();
      this.drawPlotSelection();
    },

    componentWillUnmount: function() {
      $(window).off('resize', this.handleResize);
    },

    /**
     * Conditionally update plot and selection based on props and state:
     *  1. Call createPlot if distribution changed
     */
    componentDidUpdate: function(prevProps) {
      if (!_.isEqual(this.props.distribution, prevProps.distribution)) {
        this.createPlot();
        this.drawPlotSelection();
      }
      if (prevProps.selectedMin !== this.props.selectedMin || prevProps.selectedMax !== this.props.selectedMax) {
        this.drawPlotSelection();
      }
    },

    handleResize: function() {
      this.plot.resize();
      this.plot.setupGrid();
      this.plot.draw();
      this.drawPlotSelection();
    },

    handlePlotSelected: function(event, ranges) {
      var range = unwrapXaxisRange(ranges);
      this.props.onSelected(range);
    },

    handlePlotSelecting: function(event, ranges) {
      if (!ranges) return;
      var range = unwrapXaxisRange(ranges);
      this.props.onSelecting(range);
    },

    handlePlotUnselected: function() {
      var range = { min: null, max: null };
      this.props.onSelected(range);
    },

    drawPlotSelection: function() {
      var values = this.props.distribution.map(entry => entry.value);
      var currentSelection = unwrapXaxisRange(this.plot.getSelection());
      var { selectedMin, selectedMax } = this.props;

      // Selection already matches current state
      if (selectedMin === currentSelection.min && selectedMax === currentSelection.max) {
        return;
      }

      if (selectedMin === null && selectedMax === null) {
        this.plot.clearSelection(true);
      } else {
        this.plot.setSelection({
          xaxis: {
            from: selectedMin === null ? _.min(values) : selectedMin,
            to: selectedMax === null ? _.max(values) : selectedMax
          }
        }, true);
      }
    },

    createPlot: function() {
      var { distribution, chartType, timeformat } = this.props;

      var values = distribution.map(entry => entry.value);
      var min = _.min(values);
      var max = _.max(values);

      var barWidth = (max - min) * 0.005;

      var xaxisBaseOptions = chartType === 'date'
        ? { mode: 'time', timeformat: timeformat }
        : {};


      var seriesData = [{
        data: distribution.map(entry => [ entry.value, entry.count ]),
        color: '#AAAAAA'
      },{
        data: distribution.map(entry => [ entry.value, entry.filteredCount ]),
        color: '#DA7272',
        hoverable: false,
        points: { show: true }
      }];

      var plotOptions = {
        series: {
          bars: {
            show: true,
            fillColor: { colors: [{ opacity: 1 }, { opacity: 1 }] },
            barWidth: barWidth,
            lineWidth: 0,
            align: 'center'
          }
        },
        xaxis: Object.assign({
          min: Math.floor(min - barWidth),
          max: Math.ceil(max + barWidth),
          tickLength: 0
        }, xaxisBaseOptions),
        yaxis: {
          min: 0,
          max: this.state.yaxisMax
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

      if (this.plot) this.plot.destroy();

      this.$chart = $(this.getDOMNode()).find('.chart');
      this.plot = $.plot(this.$chart, seriesData, plotOptions);
    },

    createTooltip: function() {
      this.tooltip = this.$chart
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
        var entry = this.props.distribution[item.dataIndex];
        var formattedValue = this.props.chartType === 'date'
          ? formatDate(this.props.timeformat, entry.value)
          : entry.value;

        // FIXME Format date
        qtipApi.set('content.text',
          this.props.xaxisLabel + ': ' + formattedValue +
          '<br/>Total ' + this.props.yaxisLabel + ': ' + entry.count +
          '<br/>Matching ' + this.props.yaxisLabel + ': ' + entry.filteredCount);
        qtipApi.elements.tooltip.stop(1, 1);
        qtipApi.show(item);
      }
    },

    setYAxisMax: function(yaxisMax) {
      this.setState({ yaxisMax }, () => {
        this.plot.getOptions().yaxes[0].max = yaxisMax;
        this.plot.setupGrid();
        this.plot.draw();
      });
    },

    render: function() {
      var { yaxisMax } = this.state;
      var { xaxisLabel, yaxisLabel, distribution } = this.props;

      var counts = distribution.map(entry => entry.count);
      var countsMin = Math.min(...counts);
      var countsMax = Math.max(...counts);

      return (
        <div>
          <div className="chart"></div>
          <div className="chart-title x-axis">{xaxisLabel}</div>
          <div className="chart-title y-axis">
            <div>{yaxisLabel}</div>
            <div>
              <input
                style={{width: '90%'}}
                type="range" min={countsMin + 1} max={countsMax + countsMax * 0.1}
                title={yaxisMax}
                value={yaxisMax}
                autoFocus={true}
                onChange={e => this.setYAxisMax(Number(e.target.value))}/>
            </div>
          </div>
        </div>
      );
    }
  });

  var HistogramField = React.createClass({

    propTypes: {
      toFilterValue: React.PropTypes.func.isRequired,
      toHistogramValue: React.PropTypes.func.isRequired,
      onAddFilter: React.PropTypes.func.isRequired,
      field: React.PropTypes.object.isRequired,
      filter: React.PropTypes.object.isRequired,
      overview: React.PropTypes.node.isRequired,
      displayName: React.PropTypes.string.isRequired
    },

    componentWillMount() {
      this.updateFilter = _.debounce(this.updateFilter, 50);
    },

    handleChange() {
      var inputMin = this.refs.min.getDOMNode().value
      var inputMax = this.refs.max.getDOMNode().value
      var min = inputMin === '' ? null : this.props.toFilterValue(inputMin);
      var max = inputMax === '' ? null : this.props.toFilterValue(inputMax);
      this.props.onAddFilter(this.props.field, { min, max });
    },

    updateFilter(range) {
      var min = this.props.toFilterValue(range.min);
      var max = this.props.toFilterValue(range.max);
      this.props.onAddFilter(this.props.field, { min, max });
    },

    render: function() {
      var { field, filter, distribution, displayName } = this.props;
      var values = distribution.map(entry => entry.value);
      var distMin = this.props.toFilterValue(Math.min(...values));
      var distMax = this.props.toFilterValue(Math.max(...values));

      var { min, max } = filter ? filter.values : {};

      var selectedMin = min == null ? null : this.props.toHistogramValue(min);
      var selectedMax = max == null ? null : this.props.toHistogramValue(max);

      var selectionTotal = filter && filter.selection && filter.selection.length;

      var selection = selectionTotal != null
        ? " (" + selectionTotal + " selected) "
        : null;

      return (
        <div className="range-filter">

          <div className="overview">
            {this.props.overview}
          </div>

          <div>
            {'Between '}
            <input
              ref="min"
              type="text"
              size="6"
              placeholder={distMin}
              value={min}
              onChange={this.handleChange}
            />
            {' and '}
            <input
              ref="max"
              type="text"
              size="6"
              placeholder={distMax}
              value={max}
              onChange={this.handleChange}
            />
            <span className="selection-total">{selection}</span>
          </div>

          <Histogram
            distribution={distribution}
            onSelected={this.updateFilter}
            selectedMin={selectedMin}
            selectedMax={selectedMax}
            chartType={field.type}
            timeformat={this.props.timeformat}
            xaxisLabel={field.display}
            yaxisLabel={displayName}
          />
        </div>
      );
    }

  });

  var fieldComponents = {};

  fieldComponents.string = React.createClass({

    handleClick: function(event) {
      if (!$(event.target).is('input[type=checkbox]')) {
        var $target = $(event.currentTarget).find('input[type=checkbox]');
        $target.prop('checked', !$target.prop('checked'));
        this.handleChange();
      }
    },

    handleChange: function() {
      var field = this.props.field;
      var values = $(this.getDOMNode())
        .find('input[type=checkbox]:checked')
        .toArray()
        .map(_.property('value'));
      this.props.onAddFilter(field, values);
    },

    handleSelectAll: function(event) {
      event.preventDefault();
      var { field, distribution } = this.props;
      var values = _.pluck(distribution, 'value');
      this.props.onAddFilter(field, values);
    },

    handleRemoveAll: function(event) {
      event.preventDefault();
      var field = this.props.field;
      this.props.onAddFilter(field, []);
    },

    render: function() {
      var dist = this.props.distribution;
      var total = _.reduce(dist, (acc, item) => acc + item.count, 0);
      var displayName = this.props.displayName;
      var fieldDisplay = this.props.field.display;
      var tooltipContent =
       `<p>This table shows the distribution of ${displayName} with
        respect to ${fieldDisplay}.</p>

        <p>The <i>Total</i> column indicates the number of
        ${displayName} with the given ${fieldDisplay}
        value.</p>

        <p>The <i>Matching</i> column indicates the number of
        ${displayName} that match the critera chosen for other
        qualities and that have the given ${fieldDisplay}
        value.</p>

        <p>You may add or remove ${displayName} with specific ${fieldDisplay}
        values from your overall selection by checking or unchecking the
        corresponding checkboxes.</p>`;

      return (
        <div className="membership-filter">

          <div className="membership-wrapper">
            <div className="membership-table-panel">
              <div className="toggle-links">
                <a href="#select-all" onClick={this.handleSelectAll}>select all</a>
                {' | '}
                <a href="#clear-all" onClick={this.handleRemoveAll}>clear all</a>
              </div>
              <div style={{ position: 'absolute', right: '1em' }}>
                <Tooltip content={tooltipContent}>
                  <i className="fa fa-question-circle fa-lg" style={{ color: 'blue' }}/>
                </Tooltip>
              </div>
              <table>
                <thead>
                  <tr>
                    <th colSpan="2">{this.props.field.display}</th>
                    <th>Total {this.props.displayName}</th>
                    <th>Matching {this.props.displayName}</th>
                    <th>Distribution</th>
                  </tr>
                </thead>
                <tbody>
                  {_.map(this.props.distribution, item => {
                    // compute frequency, percentage, filteredPercentage
                    var percentage = (item.count / total) * 100;
                    var filteredPercentage = (item.filteredCount / total) * 100;
                    var isChecked = !this.props.filter || _.contains(this.props.filter.values, item.value);
                    var trClassNames = 'member' + (isChecked ? ' selected' : '');

                    return (
                      <tr key={item.value} className={trClassNames} onClick={this.handleClick}>
                        <td><input value={item.value} type="checkbox" checked={isChecked} onChange={this.handleChange}/></td>
                        <td><span className="value">{item.value}</span></td>
                        <td><span className="frequency">{item.count}</span></td>
                        <td><span className="frequency">{item.filteredCount}</span></td>
                        <td><div className="bar">
                          <div className="fill" style={{ width: percentage + '%' }}/>
                          <div className="fill filtered" style={{ width: filteredPercentage + '%' }}/>
                        </div></td>
                      </tr>
                    );
                  }, this)}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      );
    }
  });


  fieldComponents.number = React.createClass({

    // FIXME Handle intermediate strings S where Number(S) => NaN
    // E.g., S = '-'
    // A potential solution is to use strings for state and to
    // convert to Number when needed
    parseValue(value) {
      switch (typeof value) {
        case 'string': return Number(value);
        default: return value;
      }
    },

    toHistogramValue(value) {
      return Number(value);
    },

    toFilterValue(value) {
      return Number(value);
    },

    render() {
      var [ knownDist, unknownDist ] = _.partition(this.props.distribution, function(entry) {
        return entry.value !== 'Unknown';
      });

      var size = knownDist.reduce(function(sum, entry) {
        return entry.count + sum;
      }, 0);

      var sum = knownDist.reduce(function(sum, entry) {
        return entry.value * entry.count + sum;
      }, 0);

      var values = knownDist.map(entry => entry.value);
      var distMin = Math.min(...values);
      var distMax = Math.max(...values);
      var distAvg = (sum / size).toFixed(2);
      var unknownCount = unknownDist.reduce((sum, entry) => sum + entry.count, 0);
      var overview = (
        <dl className="ui-helper-clearfix">
          <dt>Avg</dt>
          <dd>{distAvg}</dd>
          <dt>Min</dt>
          <dd>{distMin}</dd>
          <dt>Max</dt>
          <dd>{distMax}</dd>
          <dt>Unknown</dt>
          <dd>{unknownCount}</dd>
        </dl>
      );

      return (
        <HistogramField
          {...this.props}
          distribution={knownDist}
          toFilterValue={this.toFilterValue}
          toHistogramValue={this.toHistogramValue}
          overview={overview}
        />
      );
    }
  });

  fieldComponents.date = React.createClass({

    componentWillMount() {
      this.timeformat = getFormatFromDateString(this.props.distribution[0].value);
    },

    componentWillUpdate(nextProps) {
      this.timeformat = getFormatFromDateString(nextProps.distribution[0].value);
    },

    toHistogramValue(value) {
      return new Date(value).getTime();
    },

    toFilterValue(value) {
      switch (typeof value) {
        case 'number': return formatDate(this.timeformat, value);
        default: return value;
      }
    },

    render: function() {
      var [ knownDist, unknownDist ] = _.partition(this.props.distribution, function(entry) {
        return entry.value !== 'Unknown';
      });


      var values = knownDist.map(entry => entry.value);
      var distMin = Math.min(...values);
      var distMax = Math.max(...values);

      var dateDist = knownDist.map(function(entry) {
        // convert value to time in ms
        return Object.assign({}, entry, {
          value: new Date(entry.value).getTime()
        });
      });

      var unknownCount = unknownDist.reduce((sum, entry) => sum + entry.count, 0);

      var overview = (
        <dl className="ui-helper-clearfix">
          <dt>Min</dt>
          <dd>{distMin}</dd>
          <dt>Max</dt>
          <dd>{distMax}</dd>
          <dt>Unknown</dt>
          <dd>{unknownCount}</dd>
        </dl>
      );

      return (
        <HistogramField
          {...this.props}
          timeformat={this.timeformat}
          distribution={dateDist}
          toFilterValue={this.toFilterValue}
          toHistogramValue={this.toHistogramValue}
          overview={overview}
        />
      );
    }
  });

  var EmptyField = React.createClass({
    render: function() {
      return (
        <div>
          <h3>You may reduce the selection of {this.props.displayName} by
            selecting qualities on the left.</h3>
          <p>For each quality, you can choose specific values to include. By
            default, all values are selected.</p>
        </div>
      );
    }
  });

  function getFieldDetail(field) {
    return fieldComponents[_.result(field, 'type')];
  }

  ns.AttributeFilter = AttributeFilter;
  ns.FilterList = FilterList;
  ns.FieldFilter = FieldFilter;
  ns.FilteredData = FilteredData;
});
