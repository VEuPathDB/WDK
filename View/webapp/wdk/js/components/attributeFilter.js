/* global _ */
import React from 'react';
import FixedDataTable from 'fixed-data-table';
import Loading from '../flux/components/Loading';
import Dialog from '../flux/components/Dialog';

wdk.namespace('wdk.components.attributeFilter', function(ns) {
  'use strict';

  var { PropTypes } = React;
  var { Table, Column } = FixedDataTable;
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
      distribution: PropTypes.object,
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
      onIgnored: PropTypes.func
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

    isIgnored: function(field) {
      return this.props.ignored.indexOf(field.term) > -1;
    },

    getRow: function(index) {
      return this.props.filteredData[index];
      // return _.cloneDeep(this.props.filteredData[index]);
    },

    getCellData: function(cellDataKey, rowData) {
      return cellDataKey == '__primary_key__' ? rowData :
        this.props.metadata[cellDataKey][rowData.term].join(', ');
    },

    renderPk: function(cellData) {
      var handleIgnored = event => {
        this.props.onIgnored(cellData, !event.target.isChecked);
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
      var { fields, selectedFields, metadata, filteredData, displayName, tabWidth } = this.props;
      var { dialogIsOpen, pendingSelectedFields } = this.state;

      if (!tabWidth) return null;

      return (
        <div className="wdk-AttributeFilter-FilteredData">

          <div className="ui-helper-clearfix" style={{padding: 10}}>
            <div style={{float: 'left'}}>Showing {filteredData.length} {displayName}</div>
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
            headerHeight={30}
          >
          <Column
            label="Name"
            dataKey="__primary_key__"
            fixed={true}
            width={200}
            cellDataGetter={this.getCellData}
            cellRenderer={this.renderPk}
          />
          {selectedFields.map(field => {
            return (
              <Column
                label={field.display}
                dataKey={field.term}
                width={130}
                cellDataGetter={this.getCellData}
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
      return this.props.store.getState();
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

    render: function() {
      var {
        data,
        filteredData,
        fields,
        columns,
        ignored,
        filters,
        selectedField,
        distributionMap,
        isLoading,
        tabWidth
      } = this.state;

      var displayName = this.props.displayName;
      var selectedFilter = _.find(filters, filter => {
        return filter.field.term === _.result(selectedField, 'term');
      });

      var actions = this.props.actions;

      var filteredNotIgnored = filteredData.filter(datum => !datum.isIgnored);

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

          <div className="filter-view">
            <div className="collapse-wrapper" style={{ display: this.state.collapsed ? 'none' : 'block' }}>
              <a href="#" onClick={this.handleCollapseClick}>Collapse</a>
            </div>
            <button onClick={this.handleExpandClick}
              style={{
                display: !this.state.collapsed ? 'none' : 'block'
              }} >Select {displayName}</button>

            {/* Tabs */}

            <div className="filter-param-tabs" style={{ display: this.state.collapsed ? 'none' : 'block' }}>
              <ul>
                <li><a href="#filters">Select {displayName}</a></li>
                <li><a href="#data">View selection ({filteredNotIgnored.length})</a></li>
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
                  filteredData={filteredData}
                  selectedFields={columns}
                  fields={fields}
                  ignored={ignored}
                  onIgnored={this.handleIgnored}
                  metadata={this.props.store.metadata}/>
              </div>
            </div>
          </div>
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

      return (
        <div className="membership-filter">

          <div className="membership-wrapper">
            <div className="membership-table-panel">
              <table>
                <thead>
                  <tr>
                    <th colSpan="2">
                      <div className="toggle-links">
                        <a href="#select-all" onClick={this.handleSelectAll}>select all</a>
                        {' | '}
                        <a href="#clear-all" onClick={this.handleRemoveAll}>clear all</a>
                      </div>
                    </th>
                    <th colSpan="3">
                      <div>{this.props.displayName}</div>
                    </th>
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
                        <td><span className="percent">{percentage.toFixed(2) + '%'}</span></td>
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


  var unwrapXaxisRange = function unwrap(flotRanges) {
    var { from, to } = flotRanges.xaxis;
    var min = Number(from.toFixed(2));
    var max = Number(to.toFixed(2));
    return { min, max };
  };

  fieldComponents.number = React.createClass({

    componentDidMount: function() {
      $(this.getDOMNode())
        .on('plotselected .chart', this.handlePlotSelected)
        .on('plotselecting .chart', this.handlePlotSelecting)
        .on('plotunselected .chart', this.handlePlotUnselected)
        .on('plothover .chart', this.handlePlotHover);

      this.throttledHandleResize = _.throttle(this.handleResize, 100);
      $(window).on('resize', this.throttledHandleResize);

      this.createPlot();
      this.createTooltip();
      if (this.props.filter) this.doPlotSelection();
    },

    componentWillUnmount: function() {
      $(window).off('resize', this.throttledHandleResize);
    },

    componentDidUpdate: function(prevProps) {
      var doPlotSelection = false;
      var prevMin = this.refs.min.getDOMNode().value;
      var prevMax = this.refs.max.getDOMNode().value;
      var min = null;
      var max = null;

      prevMin = prevMin === '' ? null : Number(prevMin);
      prevMax = prevMax === '' ? null : Number(prevMax);

      if (this.props.filter) {
        min = this.props.filter.values.min;
        max = this.props.filter.values.max;
      }

      if (!_.isEqual(prevProps.distribution, this.props.distribution)) {
        this.createPlot();
        doPlotSelection = true;
      }

      if (prevMin !== min && prevMax !== max) {
        this.refs.min.getDOMNode().value = min;
        this.refs.max.getDOMNode().value = max;
        doPlotSelection = true;
      }

      if (doPlotSelection) {
        this.doPlotSelection();
      }
    },

    handleResize: function() {
      this.plot.resize();
      this.plot.setupGrid();
      this.plot.draw();
      this.doPlotSelection();
    },

    handlePlotSelected: function(event, ranges) {
      var { min, max } = unwrapXaxisRange(ranges);
      this._updateInputs(min, max);
      this._updateFilter(min, max);
      // this.setSelectionTotal(filter);
    },

    handlePlotSelecting: function(event, ranges) {
      if (!ranges) return;

      var { min, max } = unwrapXaxisRange(ranges);
      this._updateInputs(min, max);
    },

    handlePlotUnselected: function() {
      this.props.onAddFilter(this.props.field, { min: null, max: null });
    },

    doPlotSelection: function() {
      var values = _.pluck(this.props.distribution, 'value');
      var minNodeVal = this.refs.min.getDOMNode().value;
      var maxNodeVal = this.refs.max.getDOMNode().value;
      var min = minNodeVal === '' ? null : minNodeVal;
      var max = maxNodeVal === '' ? null : maxNodeVal;

      if (min === null && max === null) {
        this.plot.clearSelection(true);
      } else {
        this.plot.setSelection({
          xaxis: {
            from: min === null ? _.min(values) : min,
            to: max === null ? _.max(values) : max
          }
        }, true);
      }
    },


    _updateInputs: function(min, max) {
      this.refs.min.getDOMNode().value = min;
      this.refs.max.getDOMNode().value = max;
    },

    _updateFilter: _.debounce(function(min, max) {
      this.props.onAddFilter(this.props.field, { min, max });
    }, 50),

    createPlot: function() {
      var { distribution } = this.props;
      var dist = _.filter(distribution, item => _.isNumber(item.value));

      var series = _.reduce(dist, (acc, item) => {
        if (_.isUndefined(item.count)) return acc;
        return acc.concat([ [ Number(item.value), Number(item.count) ] ]);
      }, []);

      var fSeries = _.reduce(dist, (acc, item) => {
        if (_.isUndefined(item.filteredCount)) return acc;
        return acc.concat([ [ Number(item.value), Number(item.filteredCount) ] ]);
      }, []);

      var values = _.pluck(dist, 'value');
      var min = _.min(values);
      var max = _.max(values);

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

      if (this.plot) this.plot.destroy();

      this.$chart = $(this.getDOMNode()).find('.chart');
      this.plot = $.plot(this.$chart, seriesData, plotOptions);

      // // activate Read more link if text is overflowed
      // var p = this.$('.description p').get(0);
      // if (p && p.scrollWidth > p.clientWidth) {
      //   this.$('.description .read-more').addClass('visible');
      // }

      // return this;
    },

    createTooltip: function() {
      // do we need this ref?
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
        qtipApi.set('content.text',
          this.props.field.display + ': ' + item.datapoint[0] +
          '<br/>' + this.props.displayName + ': ' + item.datapoint[1]);
        qtipApi.elements.tooltip.stop(1, 1);
        qtipApi.show(item);
      }
    },

    handleChange: _.debounce(function() {
      var min = this.refs.min.getDOMNode().value;
      var max = this.refs.max.getDOMNode().value;
      min = min === '' ? null : Number(min);
      max = max === '' ? null : Number(max);
      this._updateFilter(min, max);
      this.doPlotSelection();
    }, 200),

    render: function() {
      var { field, distribution } = this.props;
      var dist = _.filter(distribution, item => _.isNumber(item.value));
      var size = _.reduce(dist, (acc, item) => acc + item.count, 0);
      var sum = _.reduce(dist, (acc, item) => acc + (item.value * item.count), 0);
      var values = _.pluck(dist, 'value');
      var distMin = _.min(values);
      var distMax = _.max(values);
      var distAvg = (sum / size).toFixed(2);
      var { min, max } = this.props.filter ? this.props.filter.values : {};
      var selectionTotal = this.props.filter
        ? " (" + this.props.filter.selection.length + " selected) "
        : '';

      return (
        <div className="range-filter">
          <div className="overview">
            <dl className="ui-helper-clearfix">
              <dt>Avg</dt>
              <dd>{distAvg}</dd>
              <dt>Min</dt>
              <dd>{distMin}</dd>
              <dt>Max</dt>
              <dd>{distMax}</dd>
            </dl>
          </div>

          <div>
            {'Between '}
            <input onChange={this.handleChange} ref="min" type="text" size="6" placeholder={distMin} defaultValue={min}/>
            {' and '}
            <input onChange={this.handleChange} ref="max" type="text" size="6" placeholder={distMax} defaultValue={max}/>
            <span className="selection-total">{selectionTotal}</span>
          </div>

          <div>
            <div className="chart"></div>
            <div className="chart-title x-axis">{field.display} ({field.units})</div>
            <div className="chart-title y-axis">{this.props.displayName}</div>
          </div>
        </div>
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
