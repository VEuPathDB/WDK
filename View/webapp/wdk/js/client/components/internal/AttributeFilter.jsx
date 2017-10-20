import natsort from 'natural-sort'; //eslint-disable-line
import $ from 'jquery';
import { Seq } from '../../utils/IterableUtils';
import { lazy } from '../../utils/componentUtils';
import {
  getFilterValueDisplay,
  getTree,
  isRange
} from '../../utils/FilterServiceUtils';
import {
  clamp,
  debounce,
  get,
  find,
  isEmpty,
  isEqual,
  map,
  memoize,
  min,
  max,
  noop,
  padStart,
  partial,
  partition,
  pick,
  reduce,
  sortBy,
  throttle,
  uniq
} from 'lodash';
import React from 'react';
import PropTypes from 'prop-types';
import { findDOMNode } from 'react-dom';
import Icon from '../IconAlt';
import Loading from '../Loading';
import Tooltip from '../Tooltip';
import Dialog from '../Dialog';
import CheckboxTree from '../CheckboxTree';
import DateSelector from '../DateSelector';
import { MesaController as Mesa, ModalBoundary } from 'mesa';
import 'mesa/dist/css/mesa.css';

const dateStringRe = /^(\d{4})(?:-(\d{2})(?:-(\d{2}))?)?$/;
const UNKNOWN_DISPLAY = 'unknown';
const UNKNOWN_VALUE = '@@unknown@@';

/**
 * @typedef {string[]} StringFilterValue
 */
/**
 * @typedef {{ min: number, max: number }} NumberFilterValue
 */
/**
 * @typedef {Object} Filter
 * @property {string} field
 * @property {StringFilterValue | NumberFilterValue} value
 */

/**
 * @typedef {Object} FilterListProps
 * @property {number} dataCount
 * @property {number} filteredDataCount
 * @property {string?} selectedField
 * @property {function(string): void} onFilterSelect
 * @property {function(Filter): void} onFilterRemove
 * @property {Array<Filter>} filters
 */

/**
 * List of filters configured by the user.
 *
 * Each filter can be used to update the active field
 * or to remove a filter.
 */
class FilterList extends React.Component {

  /**
   * @param {FilterListProps} props
   * @return {React.Component<FilterListProps, void>}
   */
  constructor(props) {
    super(props);
    this.handleFilterSelectClick = this.handleFilterSelectClick.bind(this);
    this.handleFilterRemoveClick = this.handleFilterRemoveClick.bind(this);
  }

  /**
   * @param {Filter} filter
   * @param {Event} event
   */
  handleFilterSelectClick(filter, event) {
    event.preventDefault();
    this.props.onFilterSelect(filter.field);
  }

/**
 * @param {Filter} filter
 * @param {Event} event
 */
  handleFilterRemoveClick(filter, event) {
    event.preventDefault();
    this.props.onFilterRemove(filter);
  }

  render() {
    var { fields, filters, selectedField } = this.props;

    return (
      <div className="filter-items-wrapper">
        {this.props.renderSelectionInfo(this.props)}
        <ul style={{display: 'inline-block', paddingLeft: '.2em'}} className="filter-items">
          {map(filters, filter => {
            var className = selectedField === filter.field ? 'selected' : '';
            var handleSelectClick = partial(this.handleFilterSelectClick, filter);
            var handleRemoveClick = partial(this.handleFilterRemoveClick, filter);
            var field = fields.get(filter.field);
            var display = getFilterValueDisplay(field, filter);

            return (
              <li key={filter.field} className={className}>
                <div className="ui-corner-all">
                  <a className="select"
                    onClick={handleSelectClick}
                    href={'#' + filter.field}
                    title={display}>{field.display}</a>
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

}

FilterList.propTypes = {
  onFilterSelect: PropTypes.func.isRequired,
  onFilterRemove: PropTypes.func.isRequired,
  fields: PropTypes.instanceOf(Map).isRequired,
  filters: PropTypes.array.isRequired,
  selectedField: PropTypes.string,
  renderSelectionInfo: PropTypes.func
};

FilterList.defaultProps = {
  renderSelectionInfo(parentProps) {
    const { filteredDataCount, dataCount } = parentProps;
    return(
      <span style={{ fontWeight: 'bold', padding: '.6em 0 .8em 0', display: 'inline-block' }}>
        {filteredDataCount} of {dataCount} selected
      </span>
    );
  }
};

/**
 * Renders a Field node.
 */
function FieldListNode({ node, onFieldSelect, isActive }) {
  return node.children.length > 0
    ? (
      <div className="wdk-Link wdk-AttributeFilterFieldParent">{node.field.display}</div>
    ) : (
      <a
        className={'wdk-AttributeFilterFieldItem' +
          (isActive ? ' wdk-AttributeFilterFieldItem__active' : '')}
        href={'#' + node.field.term}
        onClick={e => {
          e.preventDefault();
          onFieldSelect(node.field);
        }}>
        <Icon fa={isRange(node.field) ? 'bar-chart-o' : 'list'}/> {node.field.display}
      </a>
    );
}

FieldListNode.propTypes = {
  node: PropTypes.object.isRequired,
  onFieldSelect: PropTypes.func.isRequired,
  isActive: PropTypes.bool.isRequired
}

/**
 * Tree of Fields, used to set the active field.
 */
class FieldList extends React.Component {

  constructor(props) {
    super(props);
    this.handleFieldSelect = this.handleFieldSelect.bind(this);
    this.nodeComponent = this.nodeComponent.bind(this);

    this.state = {
      searchTerm: '',

      // expand branch containing selected field
      expandedNodes: this._getPathToField(this.props.fields.get(this.props.selectedField))
    };
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.selectedField === nextProps.selectedField) return;

    const selectedField = nextProps.fields.get(nextProps.selectedField);
    if (
      selectedField.parent != null &&
      !this.state.expandedNodes.includes(selectedField.parent)
    ) {
      this.setState({
        expandedNodes: uniq(this.state.expandedNodes.concat(
          this._getPathToField(selectedField)))
      });
    }
  }

  handleFieldSelect(field) {
    this.props.onFieldSelect(field.term);
    const expandedNodes = Seq.from(this.state.expandedNodes)
      .concat(this._getPathToField(field))
      .uniq()
      .toArray();
    this.setState({ searchTerm: '', expandedNodes });
  }

  nodeComponent({node}) {
    return (
      <FieldListNode
        node={node}
        onFieldSelect={this.handleFieldSelect}
        isActive={this.props.selectedField === node.field.term}
      />
    );
  }

  _getPathToField(field, path = []) {
    if (field == null || field.parent == null) return path;
    return this._getPathToField(this.props.fields.get(field.parent),
      path.concat(field.parent))
  }

  render() {
    var { autoFocus, fields } = this.props;

    return (
      <div className="field-list">
        <CheckboxTree
          autoFocusSearchBox={autoFocus}
          tree={getTree(fields.values())}
          expandedList={this.state.expandedNodes}
          getNodeId={node => node.field.term}
          getNodeChildren={node => node.children}
          onExpansionChange={expandedNodes => this.setState({ expandedNodes })}
          isSelectable={false}
          nodeComponent={this.nodeComponent}
          isSearchable={true}
          searchBoxPlaceholder="Find a quality"
          searchTerm={this.state.searchTerm}
          onSearchTermChange={searchTerm => this.setState({searchTerm})}
          searchPredicate={(node, searchTerms) =>
            searchTerms.every(searchTerm =>
              node.field.display.toLowerCase().includes(searchTerm.toLowerCase()))}
        />
      </div>
    );
  }
}

FieldList.propTypes = {
  autoFocus: PropTypes.bool,
  fields: PropTypes.instanceOf(Map).isRequired,
  onFieldSelect: PropTypes.func.isRequired,
  selectedField: PropTypes.string
};


/**
 * Main interactive filtering interface for a particular field.
 */
function FieldFilter(props) {
  let FieldDetail = getFieldDetailComponent(props.field);
  let fieldDetailProps = {
    displayName: props.displayName,
    field: props.field,
    distribution: props.distribution,
    filter: props.filter,
    fieldState: props.fieldState,
    onChange: props.onChange,
    onSort: props.onMemberSort
  };
  let className = 'field-detail';
  if (props.useFullWidth) className += ' ' + className + '__fullWidth';
  if (props.addTopPadding) className += ' ' + className + '__topPadding';

  return (
    <div className={className}>
      {!props.field ? <EmptyField displayName={props.displayName}/> : (
        <div>
          {props.distribution && <FilterLegend {...fieldDetailProps} />}
          <h3>
            {props.field.display + ' '}
            {props.field.description && (
              <Tooltip content={props.field.description}>
                <i className="fa fa-question-circle" style={{ color: 'blue', fontSize: '1rem' }}/>
              </Tooltip>
            )}
          </h3>
          <div>{FieldDetail.getHelpContent(fieldDetailProps)}</div>
          {!props.distribution ? <Loading/> : <FieldDetail {...fieldDetailProps} />}
        </div>
      )}
    </div>
  );
}

FieldFilter.propTypes = {
  displayName: PropTypes.string,
  field: PropTypes.object,
  fieldState: PropTypes.object,
  filter: PropTypes.object,
  distribution: PropTypes.array,
  onChange: PropTypes.func,
  onMemberSort: PropTypes.func,
  useFullWidth: PropTypes.bool,
  addTopPadding: PropTypes.bool
};

FieldFilter.defaultProps = {
  displayName: 'Items'
}

/**
 * Legend used for all filters
 */
function FilterLegend(props) {
  return (
    <div className="filter-param-legend">
      <div>
        <div className="bar"><div className="fill"></div></div>
        <div className="label">All {props.displayName} having "{props.field.display}"</div>
      </div>
      <div>
        <div className="bar"><div className="fill filtered"></div></div>
        <div className="label">Matching {props.displayName} when <em>other</em> criteria have been applied.</div>
      </div>
    </div>
  );

  // TODO Either remove the commented code below, or replace using provided total counts
  // const totalCounts = Seq.from(props.distribution)
  //   // FIXME Always filter nulls when they are moved to different section for non-range fields
  //   .filter(entry => !props.field.isRange || entry.value != null)
  //   .reduce(concatCounts, { count: 0, filteredCount: 0 });

  // return (
  //   <div className="filter-param-legend">
  //     <div>
  //       <div className="bar"><div className="fill"></div></div>
  //       <div className="label"><strong>{totalCounts.count} {props.displayName}</strong> &ndash; All {props.displayName} having "{props.field.display}"</div>
  //     </div>
  //     <div>
  //       <div className="bar"><div className="fill filtered"></div></div>
  //       <div className="label"><strong>{totalCounts.filteredCount} {props.displayName}</strong> &ndash; Matching {props.displayName} when <em>other</em> criteria have been applied.</div>
  //     </div>
  //   </div>
  // )
}

FilterLegend.propTypes = FieldFilter.propTypes;


var FilteredData = (function() {

  /**
   * Table of filtered data when filtering on the client side.
   */
  class LazyFilteredData extends React.Component {

    constructor(props) {
      super(props)
      this.openDialog = this.openDialog.bind(this);
      this.handleDialogClose = this.handleDialogClose.bind(this);
      this.handleExpansionClick = setStateFromArgs(this, 'expandedNodes');
      this.handleSelectionChange = setStateFromArgs(this, 'pendingSelectedFields');
      this.handleSearchTermChange = setStateFromArgs(this, 'searchTerm');
      this.handleFieldSubmit = this.handleFieldSubmit.bind(this);
      this.handleSort = this.handleSort.bind(this);
      this.handleHideColumn = this.handleHideColumn.bind(this);
      this.isIgnored = this.isIgnored.bind(this);
      this.getRow = this.getRow.bind(this);
      this.getRowClassName = this.getRowClassName.bind(this);
      this.getCellData = this.getCellData.bind(this);
      this.getPkCellData = this.getPkCellData.bind(this);
      this.renderPk = this.renderPk.bind(this);

      this.state = {
        dialogIsOpen: false,
        pendingSelectedFields: this.props.selectedFields,
        expandedNodes: undefined,
        searchTerm: ''
      };
    }

    componentWillReceiveProps(nextProps) {
      this.setState({
        pendingSelectedFields: nextProps.selectedFields
      });
    }

    openDialog(event) {
      event.preventDefault();
      this.setState({
        dialogIsOpen: true
      });
    }

    handleDialogClose() {
      this.setState({
        dialogIsOpen: false,
        pendingSelectedFields: this.props.selectedFields
      });
    }

    handleFieldSubmit(event) {
      event.preventDefault();
      this.props.onFieldsChange(this.state.pendingSelectedFields);
      this.setState({
        dialogIsOpen: false
      });
    }

    handleSort(term) {
      this.props.onSort(term);
    }

    handleHideColumn(removedField) {
      var nextFields = this.props.selectedFields.filter(field => field != removedField)
      this.props.onFieldsChange(nextFields);
    }

    isIgnored(field) {
      return this.props.ignoredData.indexOf(field) > -1;
    }

    getRow(index) {
      return this.props.filteredData[index];
    }

    getRowClassName(index) {
      return this.isIgnored(this.props.filteredData[index])
        ? 'wdk-AttributeFilter-ItemIgnored'
        : 'wdk-AttributeFilter-Item';
    }

    getCellData(cellDataKey, rowData) {
      return this.props.metadata[cellDataKey][rowData.term].join(', ');
    }

    getPkCellData(cellDataKey, rowData) {
      return {
        datum: rowData,
        isIgnored: this.isIgnored(rowData)
      }
    }

    renderPk(cellData) {
      let { datum, isIgnored } = cellData;
      var handleIgnored = () => {
        // this.props.onIgnored(datum, !isIgnored);
      };
      let checkboxStyle = { visibility: 'hidden' };
      return (
        <label style={{ overflow: 'hidden', whiteSpace: 'nowrap' }}>
          <input
            type="checkbox"
            style={checkboxStyle}
            checked={!isIgnored}
            onChange={handleIgnored}
          />
          {' ' + datum.display + ' '}
        </label>
      );
    }

    render() {
      var { fields, selectedFields, filteredData, displayName, tabWidth, totalSize } = this.props;
      var { dialogIsOpen } = this.state;

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
            <div className="wdk-AttributeFilter-FieldSelector">
              <form ref="fieldSelector" onSubmit={this.handleFieldSubmit}>
                <div style={{textAlign: 'center', padding: 10}}>
                  <button>Update Columns</button>
                </div>
                <CheckboxTree
                  tree={getTree(fields.values())}
                  getNodeId={node => node.field.term}
                  getNodeChildren={node => node.children}
                  onExpansionChange={this.handleExpansionClick}
                  nodeComponent={({node}) => <span>{node.field.display}</span> }
                  expandedList={this.state.expandedNodes}
                  isSelectable={true}
                  selectedList={this.state.pendingSelectedFields}
                  onSelectionChange={this.handleSelectionChange}
                  searchBoxPlaceholder="Find a quality"
                  searchTerm={this.state.searchTerm}
                  onSearchTermChange={this.handleSearchTermChange}
                  searchPredicate={(node, searchTerms) =>
                    searchTerms.every(searchTerm =>
                      node.field.display.toLowerCase().includes(searchTerm.toLowerCase()))}
                  currentList={selectedFields}
                />
                <div style={{textAlign: 'center', padding: 10}}>
                  <button>Update Columns</button>
                </div>
              </form>
            </div>
          </Dialog>

          <this.props.Table
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
            <this.props.Column
              label="Name"
              dataKey="__primary_key__"
              fixed={true}
              width={200}
              cellDataGetter={this.getPkCellData}
              cellRenderer={this.renderPk}
              isRemovable={false}
              isSortable={true}
            />
            {selectedFields.map(fieldTerm => {
              const field = fields.get(fieldTerm);
              return (
                <this.props.Column
                  label={field.display}
                  dataKey={field.term}
                  width={200}
                  cellDataGetter={this.getCellData}
                  isRemovable={true}
                  isSortable={true}
                />
              );
            })}
          </this.props.Table>
        </div>
      );
    }
  }

  LazyFilteredData.propTypes = {
    tabWidth: PropTypes.number,
    totalSize: PropTypes.number.isRequired,
    filteredData: PropTypes.array,
    fields: PropTypes.instanceOf(Map),
    selectedFields: PropTypes.array,
    ignoredData: PropTypes.array,
    metadata: PropTypes.object,
    displayName: PropTypes.string,
    onFieldsChange: PropTypes.func,
    onIgnored: PropTypes.func,
    onSort: PropTypes.func,
    sortTerm: PropTypes.string,
    sortDirection: PropTypes.string,
  };

  return lazy(render => require(['./Table'], render))(LazyFilteredData)
})();


/**
 * Primary component
 */
export class AttributeFilter extends React.Component {

  constructor(props) {
    super(props);
    this.handleSelectFieldClick = this.handleSelectFieldClick.bind(this);
    this.handleCollapseClick = this.handleCollapseClick.bind(this);
    this.handleExpandClick = this.handleExpandClick.bind(this);
    this.handleFieldsChange = this.handleFieldsChange.bind(this);
    this.handleIgnored = this.handleIgnored.bind(this);
    this.handleSort = this.handleSort.bind(this);
    this.handleFilterRemove = this.handleFilterRemove.bind(this);
    this.handleFieldFilterChange = this.handleFieldFilterChange.bind(this);

    this.state = {
      sortTerm: '__primary_key__',
      sortDirection: 'ASC',
      collapsed: false
    };
  }

  componentDidMount() {
    var $node = $(findDOMNode(this));
    $node.find('.filter-param-tabs').tabs({
      activate: (event, ui) => {
        this.setState({
          tabWidth: ui.newPanel.width()
        });
      }
    });
  }

  /**
   * @param {string} field
   * @param {Event} event
   */
  handleSelectFieldClick(field, event) {
    event.preventDefault();
    this.props.onActiveFieldChange(field);
  }

/**
 * @param {Event} event
 */
  handleCollapseClick(event) {
    event.preventDefault();
    this.setState({
      collapsed: true
    });
  }

/**
 * @param {Event} event
 */
  handleExpandClick(event) {
    event.preventDefault();
    this.setState({
      collapsed: false
    });
  }

/**
 * Columns in data table change
 * @param {*} fields
 */
  handleFieldsChange(fields) {
    this.props.onColumnsChange(fields);
  }

  /**
   * @deprecated
   */
  handleIgnored(datum, ignored) {
    let ignoredData = ignored
      ? this.props.ignoredData.concat(datum)
      : this.props.ignoredData.filter(d => d !== datum);
    this.props.onIgnoredDataChange(ignoredData);
  }

/**
 *
 * @param {string} fieldTerm
 */
  handleSort(fieldTerm) {
    let { sortTerm, sortDirection } = this.state;
    let direction = fieldTerm == sortTerm && sortDirection == 'ASC'
      ? 'DESC' : 'ASC';
    this.setState({
      sortTerm: fieldTerm,
      sortDirection: direction
    });
  }

  handleFilterRemove(filter) {
    let filters = this.props.filters.filter(f => f !== filter);
    this.props.onFiltersChange(filters);
  }

  /**
   * @param {Field} field Field term id
   * @param {any} value Filter value
   * @param {boolean} includeUnknown Indicate if items with an unknown value for the field should be included.
   */
  handleFieldFilterChange(field, value, includeUnknown) {
    let filters = this.props.filters.filter(f => f.field !== field.term);
    this.props.onFiltersChange(shouldAddFilter(field, value, includeUnknown, this.props.activeFieldSummary)
      ? filters.concat({ field: field.term, value, includeUnknown })
      : filters
    );
  }

  render() {
    var {
      dataCount,
      filteredData,
      fields,
      columns,
      ignoredData,
      filters,
      invalidFilters,
      activeField,
      activeFieldSummary,
      fieldMetadataMap
    } = this.props;

    let {
      tabWidth,
      sortTerm,
      sortDirection
    } = this.state;

    var displayName = this.props.displayName;
    var selectedFilter = find(filters, filter => {
      return filter.field === activeField;
    });

    var filteredNotIgnored = filteredData.filter(datum => ignoredData.indexOf(datum) === -1);

    var sortedFilteredData = sortBy(filteredData, function(datum) {
      var term = datum.term;
      return sortTerm == '__primary_key__' ? term : fieldMetadataMap[sortTerm][term];
    });

    if (sortDirection == 'DESC') sortedFilteredData.reverse();

    return (
      <div>
        <FilterList
          onFilterSelect={this.props.onActiveFieldChange}
          onFilterRemove={this.handleFilterRemove}
          filters={filters}
          filteredDataCount={filteredNotIgnored.length}
          dataCount={dataCount}
          fields={fields}
          selectedField={activeField}
          renderSelectionInfo={this.props.renderSelectionInfo}
        />

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
              {this.props.collapsible && (
                <li>
                  <span
                    className="wdk-AttributeFilter-Collapse link"
                    title="Hide selection tool"
                    onClick={this.handleCollapseClick}
                  >Collapse</span>
                </li>
              )}

            </ul>


            {/* Main selection UI */}
            <div id="filters">
              <div className="filters ui-helper-clearfix">
                <FieldList
                  fields={fields}
                  onFieldSelect={this.props.onActiveFieldChange}
                  selectedField={activeField}
                />

                <FieldFilter
                  displayName={displayName}
                  field={this.props.fields.get(activeField)}
                  filter={selectedFilter}
                  distribution={activeFieldSummary}
                  onChange={this.handleFieldFilterChange}
                  addTopPadding
                />
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
                totalSize={dataCount}
                selectedFields={columns}
                fields={fields}
                ignoredData={ignoredData}
                metadata={fieldMetadataMap}/>
            </div>
          </div>
        </div>
      </div>
    );
  }

}

AttributeFilter.propTypes = {

  displayName: PropTypes.string,
  collapsible: PropTypes.bool,

  // state
  fields: PropTypes.instanceOf(Map).isRequired,
  filters: PropTypes.array.isRequired,
  dataCount: PropTypes.number.isRequired,
  filteredData: PropTypes.array.isRequired,
  ignoredData: PropTypes.array.isRequired,
  columns: PropTypes.array.isRequired,
  activeField: PropTypes.string,
  activeFieldSummary: PropTypes.array,
  fieldMetadataMap: PropTypes.object.isRequired,
  renderSelectionInfo: PropTypes.func,

  // not sure if these belong here
  isLoading: PropTypes.bool,
  invalidFilters: PropTypes.array,  // derivable?

  // event handlers
  onActiveFieldChange: PropTypes.func.isRequired,
  onFiltersChange: PropTypes.func.isRequired,
  onColumnsChange: PropTypes.func.isRequired,
  onIgnoredDataChange: PropTypes.func.isRequired

};

AttributeFilter.defaultProps = {
  displayName: 'Items',
  collapsible: true
};

/**
 * Filtering UI for server-side filtering.
 */
export class ServerSideAttributeFilter extends React.Component {

  constructor(props) {
    super(props);
    this.handleSelectFieldClick = this.handleSelectFieldClick.bind(this);
    this.handleFilterRemove = this.handleFilterRemove.bind(this);
    this.handleFieldFilterChange = this.handleFieldFilterChange.bind(this);
    this.handleMemberSort = this.handleMemberSort.bind(this);
  }

  handleSelectFieldClick(field, event) {
    event.preventDefault();
    this.props.onActiveFieldChange(field);
  }

  handleFilterRemove(filter) {
    let filters = this.props.filters.filter(f => f !== filter);
    this.props.onFiltersChange(filters);
  }

  /**
   * @param {Field} field Field term id
   * @param {any} value Filter value
   * @param {boolean} includeUnknown Indicate if items with an unknown value for the field should be included.
   */
  handleFieldFilterChange(field, value, includeUnknown) {
    let filters = this.props.filters.filter(f => f.field !== field.term);
    this.props.onFiltersChange(shouldAddFilter(field, value, includeUnknown, this.props.activeFieldSummary)
      ? filters.concat({ field: field.term, value, includeUnknown })
      : filters
    );
  }

  handleMemberSort(field, state) {
    this.props.onMemberSort(field, state);
  }

  render() {
    var {
      autoFocus,
      hideFilterPanel,
      hideFieldPanel,
      dataCount,
      filteredDataCount,
      fields,
      filters,
      invalidFilters,
      activeField,
      activeFieldState,
      activeFieldSummary
    } = this.props;

    var displayName = this.props.displayName;
    var selectedFilter = find(filters, filter => {
      return filter.field === activeField;
    });

    return (
      <div>
        {hideFilterPanel || (
          <FilterList
            onFilterSelect={this.props.onActiveFieldChange}
            onFilterRemove={this.handleFilterRemove}
            filters={filters}
            fields={fields}
            filteredDataCount={filteredDataCount}
            dataCount={dataCount}
            selectedField={activeField}
            renderSelectionInfo={this.props.renderSelectionInfo}
          />
        )}

        <InvalidFilterList filters={invalidFilters}/>

        {/* Main selection UI */}
        <div className="filters ui-helper-clearfix">
          {hideFieldPanel || (
            <FieldList
              autoFocus={autoFocus}
              fields={fields}
              onFieldSelect={this.props.onActiveFieldChange}
              selectedField={activeField}
            />
          )}

          <FieldFilter
            displayName={displayName}
            field={fields.get(activeField)}
            fieldState={activeFieldState}
            filter={selectedFilter}
            distribution={activeFieldSummary}
            onChange={this.handleFieldFilterChange}
            onMemberSort={this.handleMemberSort}
            useFullWidth={hideFieldPanel}
          />
        </div>
      </div>
    );
  }

}

ServerSideAttributeFilter.propTypes = {

  // options
  displayName: PropTypes.string,
  autoFocus: PropTypes.bool,
  hideFilterPanel: PropTypes.bool,
  hideFieldPanel: PropTypes.bool,
  renderSelectionInfo: PropTypes.func,

  // state
  fields: PropTypes.instanceOf(Map).isRequired,
  filters: PropTypes.array.isRequired,
  dataCount: PropTypes.number,
  filteredDataCount: PropTypes.number,
  activeField: PropTypes.string,
  activeFieldState: PropTypes.object,
  activeFieldSummary: PropTypes.array,

  // not sure if these belong here
  isLoading: PropTypes.bool,
  invalidFilters: PropTypes.array,  // derivable?

  // event handlers
  onActiveFieldChange: PropTypes.func.isRequired,
  onFiltersChange: PropTypes.func.isRequired,
  onMemberSort: PropTypes.func.isRequired

};

ServerSideAttributeFilter.defaultProps = {
  displayName: 'Items',
  hideFilterPanel: false,
  hideFieldPanel: false
};

/**
 * List of filters with invalid fields and/or values
 */
function InvalidFilterList(props) {
  var { filters } = props;

  if (isEmpty(filters)) return null;

  return (
    <div className="invalid-values">
      <p>Some of the options you previously selected are no longer available:</p>
      <ul>
        {map(filters, filter => (
          <li className="invalid">
            {JSON.stringify(pick(filter, 'field', 'value', 'includeUnknown'))}
          </li>
        ))}
      </ul>
    </div>
  );
}

InvalidFilterList.propTypes = {
  filters: PropTypes.array
}


var distributionEntryPropType = PropTypes.shape({
  value: PropTypes.number.isRequired,
  count: PropTypes.number.isRequired,
  filteredCount: PropTypes.number.isRequired
});

var Histogram = (function() {

  /** Common histogram component */
  class LazyHistogram extends React.Component {

    constructor(props) {
      super(props);
      this.handleResize = throttle(this.handleResize.bind(this), 100);
      this.setStateFromProps(props);
    }

    componentDidMount() {
      $(window).on('resize', this.handleResize);
      $(findDOMNode(this))
        .on('plotselected .chart', this.handlePlotSelected.bind(this))
        .on('plotselecting .chart', this.handlePlotSelecting.bind(this))
        .on('plotunselected .chart', this.handlePlotUnselected.bind(this))
        .on('plothover .chart', this.handlePlotHover.bind(this));

      this.createPlot();
      this.createTooltip();
      this.drawPlotSelection();
    }

    componentWillReceiveProps(nextProps) {
      if (this.props.distribution !== nextProps.distribution) {
        this.setStateFromProps(nextProps);
      }
    }

    /**
     * Conditionally update plot and selection based on props and state:
     *  1. Call createPlot if distribution changed
     */
    componentDidUpdate(prevProps) {
      if (!isEqual(this.props.distribution, prevProps.distribution)) {
        this.createPlot();
        this.drawPlotSelection();
      }
      if (
        prevProps.selectedMin !== this.props.selectedMin ||
        prevProps.selectedMax !== this.props.selectedMax
      ) {
        this.drawPlotSelection();
      }
    }

    componentWillUnmount() {
      $(window).off('resize', this.handleResize);
    }

    setStateFromProps(props) {
      // Set default yAxis max based on distribution
      var yaxisMax = this.computeYAxisMax(props);
      var values = props.distribution
        .map(entry => entry.value)
        .filter(value => value != null);
      var xaxisMin = Math.min(...values);
      var xaxisMax = Math.max(...values);
      this.state = { yaxisMax, xaxisMin, xaxisMax };
    }

    computeYAxisMax(props) {
      var counts = props.distribution.map(entry => entry.count);
      // Reverse sort, then pull out first and second highest values
      var [ max, nextMax ] = counts.sort((a, b) => a < b ? 1 : -1);
      // If max is more than twice the size of nextMax, assume it is
      // an outlier and use nextMax as the max
      var yaxisMax = max >= nextMax * 2 ? nextMax : max;
      return yaxisMax + yaxisMax * 0.1;
    }

    handleResize() {
      this.plot.resize();
      this.plot.setupGrid();
      this.plot.draw();
      this.drawPlotSelection();
    }

    handlePlotSelected(event, ranges) {
      var range = unwrapXaxisRange(ranges);
      this.props.onSelected(range);
    }

    handlePlotSelecting(event, ranges) {
      if (!ranges) return;
      var range = unwrapXaxisRange(ranges);
      this.props.onSelecting(range);
    }

    handlePlotUnselected() {
      var range = { min: null, max: null };
      this.props.onSelected(range);
    }

    drawPlotSelection() {
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
            from: selectedMin === null ? Math.min(...values) : selectedMin,
            to: selectedMax === null ? Math.max(...values) : selectedMax
          }
        }, true);
      }
    }

    createPlot() {
      var { distribution, chartType, timeformat } = this.props;

      var values = distribution.map(entry => entry.value);
      var min = Math.min(...values);
      var max = Math.max(...values);

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
        // points: { show: true }
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
          min: this.state.xaxisMin,
          max: this.state.xaxisMax,
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

      this.$chart = $(findDOMNode(this)).find('.chart');
      this.plot = $.plot(this.$chart, seriesData, plotOptions);
    }

    createTooltip() {
      this.tooltip = this.$chart
        .qtip({
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
          },
          style: {
            classes: 'qtip-tipsy'
          }
        });
    }

    handlePlotHover(event, pos, item) {
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
          '<br/>All ' + this.props.yaxisLabel + ': ' + entry.count +
          '<br/>Matching ' + this.props.yaxisLabel + ': ' + entry.filteredCount);
        qtipApi.elements.tooltip.stop(1, 1);
        qtipApi.show(item);
      }
    }

    setYAxisMax(yaxisMax) {
      this.setState({ yaxisMax }, () => {
        this.plot.getOptions().yaxes[0].max = yaxisMax;
        this.plot.setupGrid();
        this.plot.draw();
      });
    }

    setXAxisScale(xaxisMin, xaxisMax) {
      this.setState({ xaxisMin, xaxisMax }, () => {
        this.plot.getOptions().xaxes[0].min = xaxisMin;
        this.plot.getOptions().xaxes[0].max = xaxisMax;
        this.plot.setupGrid();
        this.plot.draw();
      });
    }

    render() {
      var { yaxisMax, xaxisMin, xaxisMax } = this.state;
      var { xaxisLabel, yaxisLabel, chartType, timeformat, distribution } = this.props;

      var counts = distribution.map(entry => entry.count);
      var countsMin = Math.min(...counts);
      var countsMax = Math.max(...counts);

      var values = distribution.map(entry => entry.value).filter(value => value != null);
      var valuesMin = Math.min(...values);
      var valuesMax = Math.max(...values);

      var xaxisMinSelector = chartType === 'date' ? (
        <DateSelector
          value={formatDate(timeformat, xaxisMin)}
          start={formatDate(timeformat, valuesMin)}
          end={formatDate(timeformat, xaxisMax)}
          onChange={value => this.setXAxisScale(new Date(value).getTime(), xaxisMax)}
        />
      ) : (
        <input
          type="number"
          value={xaxisMin}
          min={valuesMin}
          max={xaxisMax}
          onChange={e => this.setXAxisScale(Number(e.target.value), xaxisMax)}
        />
      );

      var xaxisMaxSelector = chartType === 'date' ? (
        <DateSelector
          value={formatDate(timeformat, xaxisMax)}
          start={formatDate(timeformat, xaxisMin)}
          end={formatDate(timeformat, valuesMax)}
          onChange={value => this.setXAxisScale(xaxisMin, new Date(value).getTime())}
        />
      ) : (
        <input
          type="number"
          value={xaxisMax}
          min={xaxisMin}
          max={valuesMax}
          onChange={e => this.setXAxisScale(xaxisMin, Number(e.target.value))}
        />
      );

      return (
        <div className="chart-container">
          <div className="chart"></div>
          <div className="chart-title x-axis">{xaxisLabel}</div>
          <div>
            Display {xaxisLabel} between {xaxisMinSelector} and {xaxisMaxSelector} <button
              type="button"
              onClick={() => this.setXAxisScale(valuesMin, valuesMax)}
            >reset</button>
          </div>
          <div className="chart-title y-axis">
            <div>{yaxisLabel}</div>
            <div>
              <input
                style={{width: '90%'}}
                type="range"
                min={Math.max(countsMin, 1)}
                max={countsMax + countsMax * 0.1}
                title={yaxisMax}
                value={yaxisMax}
                onChange={e => this.setYAxisMax(Number(e.target.value))}/>
            </div>
          </div>
        </div>
      );
    }

  }

  LazyHistogram.propTypes = {
    distribution: PropTypes.arrayOf(distributionEntryPropType).isRequired,
    selectedMin: PropTypes.number,
    selectedMax: PropTypes.number,
    chartType: PropTypes.oneOf([ 'number', 'date' ]).isRequired,
    timeformat: PropTypes.string,
    xaxisLabel: PropTypes.string,
    yaxisLabel: PropTypes.string,

    onSelected: PropTypes.func,
    onSelecting: PropTypes.func,
    onUnselected: PropTypes.func
  };

  LazyHistogram.defaultProps = {
    xaxisLabel: 'X-Axis',
    yaxisLabel: 'Y-Axis',
    selectedMin: null,
    selectedMax: null,
    onSelected: noop,
    onSelecting: noop,
    onUnselected: noop
  };

  return lazy(function(render) {
    require(
      [
        'lib/jquery-flot',
        'lib/jquery-flot-categories',
        'lib/jquery-flot-selection',
        'lib/jquery-flot-time'
      ],
      render)
  })(LazyHistogram);
})();

/**
 * Generic Histogram field component
 *
 * TODO Add binning
 * TODO Use bin size for x-axis scale <input> step attribute
 * TODO Interval snapping
 */
class HistogramField extends React.Component {

  static getHelpContent(props) {
    return (
      <div>
        Select a range of {props.field.display} values with the graph below.
      </div>
    );
   /*
   return (
      <div>
        <div>
          The graph below shows the distribution of {props.field.display} values.
          The red bar indicates the number of {props.displayName} that have the
          {props.field.display} value and your other selection criteria.
        </div>
        <div>
          The slider to the left of the graph can be used to scale the Y-axis.
        </div>
      </div>
    )
    */
  }

  constructor(props) {
    super(props);
    this.updateFilterValueFromSelection = debounce(this.updateFilterValueFromSelection.bind(this), 50);
    this.handleMinInputBlur = this.handleMinInputBlur.bind(this);
    this.handleMinInputKeyPress = this.handleMinInputKeyPress.bind(this);
    this.handleMinInputChange = this.handleMinInputChange.bind(this)
    this.handleMaxInputBlur = this.handleMaxInputBlur.bind(this);
    this.handleMaxInputKeyPress = this.handleMaxInputKeyPress.bind(this);
    this.handleMaxInputChange = this.handleMaxInputChange.bind(this)
    this.handleUnknownCheckboxChange = this.handleUnknownCheckboxChange.bind(this)
    this.cacheDistributionOperations(this.props);

    this.state = {
      includeUnknown: get(props.filter, 'includeUnknown', true),
      minInputValue: get(props.filter, 'value.min', this.distributionRange.min),
      maxInputValue: get(props.filter, 'value.max', this.distributionRange.max)
    };
  }

  componentWillReceiveProps(nextProps) {
    let distributionChanged = this.props.distribution !== nextProps.distribution;
    let filterChanged = this.props.filter !== nextProps.filter;

    if (distributionChanged) this.cacheDistributionOperations(nextProps);

    if (distributionChanged || filterChanged) {
      this.setState({
        minInputValue: get(nextProps.filter, 'value.min', this.distributionRange.min),
        maxInputValue: get(nextProps.filter, 'value.max', this.distributionRange.max)
      });
    }
  }

  cacheDistributionOperations(props) {
    this.convertedDistribution = props.distribution.map(entry =>
      Object.assign({}, entry, { value: props.toHistogramValue(entry.value)}));
    var values = this.convertedDistribution.map(entry => entry.value);
    var min = Math.min(...values);
    var max = Math.max(...values);
    this.convertedDistributionRange = { min, max };
    this.distributionRange = { min: props.toFilterValue(min), max: props.toFilterValue(max) };
  }

  formatRangeValue(value) {
    const { min, max } = this.convertedDistributionRange;
    return value ? this.props.toFilterValue(clamp(this.props.toHistogramValue(value), min, max)) : null;
  }

  handleMinInputChange(event) {
    this.setState({ minInputValue: event.target.value });
  }

  handleMinInputBlur() {
    this.updateFilterValueFromState();
  }

  handleMinInputKeyPress(event) {
    if (event.key === 'Enter') this.updateFilterValueFromState();
  }

  handleMaxInputChange(event) {
    this.setState({ maxInputValue: event.target.value });
  }

  handleMaxInputBlur() {
    this.updateFilterValueFromState();
  }

  handleMaxInputKeyPress(event) {
    if (event.key === 'Enter') this.updateFilterValueFromState();
  }

  updateFilterValueFromState() {
    const min = this.formatRangeValue(this.state.minInputValue);
    const max = this.formatRangeValue(this.state.maxInputValue);
    this.updateFilterValue({ min, max });
  }

  updateFilterValueFromSelection(range) {
    const min = this.formatRangeValue(range.min);
    const max = this.formatRangeValue(range.max);
    this.updateFilterValue({ min, max });
  }

  updateFilterValue(range) {
    // only emit change if range differs from filter
    if (this.rangeIsDifferent(range)) this.emitChange(range);
  }

  /**
   * @param {React.ChangeEvent.<HTMLInputElement>} event
   */
  handleUnknownCheckboxChange(event) {
    const includeUnknown = event.target.checked;
    this.setState({ includeUnknown });
    this.emitChange(get(this.props, 'filter.value'), includeUnknown);
  }

  rangeIsDifferent({ min, max }) {
    return this.props.filter == null
      ? min > this.distributionRange.min || max < this.distributionRange.max
      : min !== this.props.filter.min || max !== this.props.filter.max;
  }

  emitChange(range, includeUnknown = this.state.includeUnknown) {
    // Use range if strict subset, otherwise use undefined, which indicates
    // that the user wants everything known.
    const filterValue = (
      range &&
      range.min <= this.distributionRange.min &&
      range.max >= this.distributionRange.max
    ) ? undefined : range;

    this.props.onChange(this.props.field, filterValue, includeUnknown);

    this.setState({
      minInputValue: get(filterValue, 'min', this.distributionRange.min),
      maxInputValue: get(filterValue, 'max', this.distributionRange.max)
    });
  }

  render() {
    var { field, filter, displayName, unknownCount } = this.props;
    var distMin = this.distributionRange.min;
    var distMax = this.distributionRange.max;

    // if there is no filter value, then we want to select everything
    var filterValue = get(filter, 'value');
    var min = filterValue == null ? distMin : filterValue.min;
    var max = filterValue == null ? distMax : filterValue.max;
    var includeUnknown = get(filter, 'includeUnknown', this.state.includeUnknown);

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
          {'Select ' + field.display + ' between '}
          <input
            type="text"
            size="6"
            placeholder={distMin}
            value={this.state.minInputValue || ''}
            onChange={this.handleMinInputChange}
            onKeyPress={this.handleMinInputKeyPress}
            onBlur={this.handleMinInputBlur}
          />
          {' and '}
          <input
            type="text"
            size="6"
            placeholder={distMax}
            value={this.state.maxInputValue || ''}
            onChange={this.handleMaxInputChange}
            onKeyPress={this.handleMaxInputKeyPress}
            onBlur={this.handleMaxInputBlur}
          />
          {unknownCount > 0 && (
            <label className="include-unknown">
              {' '}
              <input
                type="checkbox"
                checked={includeUnknown}
                onChange={this.handleUnknownCheckboxChange}
              /> Include {unknownCount} Unknown
            </label>
          )}
          <span className="selection-total">{selection}</span>
        </div>

        <Histogram
          distribution={this.convertedDistribution}
          onSelected={this.updateFilterValueFromSelection}
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

}

HistogramField.propTypes = {
  distribution: PropTypes.array.isRequired,
  toFilterValue: PropTypes.func.isRequired,
  toHistogramValue: PropTypes.func.isRequired,
  onChange: PropTypes.func.isRequired,
  field: PropTypes.object.isRequired,
  filter: PropTypes.object,
  overview: PropTypes.node.isRequired,
  displayName: PropTypes.string.isRequired,
  unknownCount: PropTypes.number.isRequired,
  timeformat: PropTypes.string
};


/**
 * Membership field component
 */
class MembershipField extends React.Component {
  static getHelpContent(props) {
    var displayName = props.displayName;
    var fieldDisplay = props.field.display;
    return (
      <div>
        You may add or remove {displayName} with specific {fieldDisplay} values
        from your overall selection by checking or unchecking the corresponding
        checkboxes.
      </div>
    );
  }

  constructor(props) {
    super(props);
    this.handleItemClick = this.handleItemClick.bind(this);
    this.handleSelectAll = this.handleSelectAll.bind(this);
    this.handleRemoveAll = this.handleRemoveAll.bind(this);
    this.toFilterValue = this.toFilterValue.bind(this);
    this.getKnownValues = memoize(this.getKnownValues);
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.distribution !== nextProps.distribution) {
      this.getKnownValues.cache.clear();
    }
  }

  toFilterValue(value) {
    return this.props.field.type === 'string' ? String(value)
      : this.props.field.type === 'number' ? Number(value)
      : this.props.field.type === 'date' ? Date(value)
      : value;
  }

  getKnownValues() {
    return this.props.distribution
      .filter(({ value }) => value != null)
      .map(({ value }) => value);
  }

  getValuesForFilter() {
    return get(this.props, 'filter.value');
  }

  handleItemClick(value, addItem) {
    if (value == UNKNOWN_VALUE) {
      this.handleUnknownChange(addItem);
    }
    else {
      const currentFilterValue = this.getValuesForFilter() || this.getKnownValues();
      const filterValue = addItem
        ? currentFilterValue.concat(value)
        : currentFilterValue.filter(v => v !== value);

      this.emitChange(
        filterValue.length === this.getKnownValues().length ? undefined : filterValue,
        get(this.props, 'filter.includeUnknown', true)
      );
    }
  }

  handleUnknownChange(addUnknown) {
    this.emitChange(this.getValuesForFilter(), addUnknown);
  }

  handleSelectAll() {
    this.emitChange(undefined, true);
  }

  handleRemoveAll() {
    this.emitChange([], false);
  }

  handleSort(nextSort) {
    let sort = Object.assign({}, this.props.fieldState.sort, nextSort);
    this.props.onSort(this.props.field, sort);
  }

  emitChange(value, includeUnknown) {
    this.props.onChange(this.props.field, value, includeUnknown);
  }

  render() {
    var total = reduce(this.props.distribution, (acc, item) => acc + item.count, 0);

    // get filter, or create one for display purposes only
    var filterValue = get(this.props, 'filter.value', this.getKnownValues());
    var filterValueSet = new Set(filterValue);
    var includeUnknown = get(this.props, 'filter.includeUnknown', true);
    var useSort = (
      this.props.fieldState &&
      this.props.fieldState.sort &&
      typeof this.props.onSort === 'function'
    );

    return (
      <ModalBoundary>
        <div className="membership-filter">
          { useSort ? (
            <div className="membership-actions">
              <div className="membership-action membership-action__group-selected">
                <div className="membership-group-selected-label">Set selected values to top</div>
                <button
                  type="button"
                  className={'membership-group-selected-button membership-group-selected-button__' + (this.props.fieldState.sort.groupBySelected ? 'on' : 'off')}
                  onClick={() => {
                    this.handleSort(Object.assign(this.props.fieldState.sort, {}, {
                      groupBySelected: !this.props.fieldState.sort.groupBySelected
                    }));
                  }}
                >
                  {this.props.fieldState.sort.groupBySelected ? 'On' : 'Off'}
                </button>
              </div>
            </div>
          ) : null }

          <Mesa
            options={{
              isRowSelected: (row) => {
                var value = row.value == null ? UNKNOWN_VALUE : this.toFilterValue(row.value);
                return (value == UNKNOWN_VALUE && includeUnknown) || filterValueSet.has(value);
              },
              deriveRowClassName: (row) => {
                return 'member' + (row.filteredCount === 0 ? ' member__disabled' : '');
              },
                useStickyHeader: true,
                tableBodyMaxHeight: '80vh'
            }}
            uiState={this.props.fieldState}
            actions={[]}
            eventHandlers={{
              onRowSelect: (item) => this.handleItemClick(item.value, true),
              onRowDeselect: (item) => this.handleItemClick(item.value, false),
              onMultipleRowSelect: () => this.handleSelectAll(),
              onMultipleRowDeselect: () => this.handleRemoveAll(),
              onSort: ({key: columnKey}, direction) => useSort && this.handleSort({columnKey, direction})
            }}
            rows={this.props.distribution}
            columns={[
              {
                key: 'value',
                name: this.props.field.display,
                sortable: useSort,
                width: '30%',
                renderCell: ({ value }) =>
                  <div>{value == null ? UNKNOWN_DISPLAY : String(value)}</div>
              },
              {
                key: 'count',
                name: `All ${this.props.displayName}`,
                sortable: useSort,
                width: '15%',
                helpText: (
                  <div>
                    The number of <em>{this.props.displayName}</em> with the given <em>{this.props.field.display}</em> value.
                  </div>
                ),
                renderCell: ({ value }) => (
                  <div>{value.toLocaleString()}</div>
                )
              },
              {
                key: 'filteredCount',
                name: `Matching ${this.props.displayName}`,
                sortable: useSort,
                width: '15%',
                helpText: (
                  <div>
                    The number of <em>{this.props.displayName}</em> that match the critera chosen for other qualities and that have the given <em>{this.props.field.display}</em> value.
                  </div>
                ),
                renderCell: ({ value }) => (
                  <div>{value.toLocaleString()}</div>
                )
              },
              {
                key: 'distribution',
                name: 'Distribution',
                width: '35%',
                renderCell: ({ row }) => (
                  <div className="bar">
                    <div className="fill" style={{ width: (row.count / total * 100) + '%' }}/>
                    <div className="fill filtered" style={{ width: (row.filteredCount / total * 100) + '%' }}/>
                  </div>
                )
              }
            ]}
          >
          </Mesa>
        </div>
      </ModalBoundary>
    );
  }
}

MembershipField.propTypes = FieldFilter.propTypes


/**
 * Number field component
 */
class NumberField extends React.Component {

  static getHelpContent(props) {
    return HistogramField.getHelpContent(props);
  }

  constructor(props) {
    super(props);
    this.toHistogramValue = this.toHistogramValue.bind(this);
    this.toFilterValue = this.toFilterValue.bind(this);
  }

  // FIXME Handle intermediate strings S where Number(S) => NaN
  // E.g., S = '-'
  // A potential solution is to use strings for state and to
  // convert to Number when needed
  parseValue(value) {
    switch (typeof value) {
      case 'string': return Number(value);
      default: return value;
    }
  }

  toHistogramValue(value) {
    return Number(value);
  }

  toFilterValue(value) {
    return value;
  }

  render() {
    var [ knownDist, unknownDist ] = partition(this.props.distribution, function(entry) {
      return entry.value !== null;
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
      </dl>
    );

    return (
      <HistogramField
        {...this.props}
        distribution={knownDist}
        unknownCount={unknownCount}
        toFilterValue={this.toFilterValue}
        toHistogramValue={this.toHistogramValue}
        overview={overview}
      />
    );
  }

}

NumberField.propTypes = FieldFilter.propTypes;

/**
 * Date field component
 */
class DateField extends React.Component {

  static getHelpContent(props) {
    return HistogramField.getHelpContent(props);
  }

  constructor(props) {
    super(props);
    this.toHistogramValue = this.toHistogramValue.bind(this);
    this.toFilterValue = this.toFilterValue.bind(this);
  }

  componentWillMount() {
    this.setDateFormat(this.props.distribution);
  }

  componentWillUpdate(nextProps) {
    this.setDateFormat(nextProps.distribution);
  }

  setDateFormat(distribution) {
    const firstDateEntry = distribution.find(entry => entry.value != null);
    if (firstDateEntry == null) {
      console.warn('Could not determine date format. No non-null distribution entry.', distribution);
    }
    else {
      this.timeformat = getFormatFromDateString(firstDateEntry.value);
    }
  }

  toHistogramValue(value) {
    return new Date(value).getTime();
  }

  toFilterValue(value) {
    switch (typeof value) {
      case 'number': return formatDate(this.timeformat, value);
      default: return value;
    }
  }

  render() {
    var [ knownDist, unknownDist ] = partition(this.props.distribution, function(entry) {
      return entry.value !== null;
    });


    var values = sortBy(knownDist.map(entry => entry.value), value => new Date(value).getTime());
    var distMin = values[0];
    var distMax = values[values.length - 1];

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
      </dl>
    );

    return (
      <HistogramField
        {...this.props}
        timeformat={this.timeformat}
        distribution={dateDist}
        unknownCount={unknownCount}
        toFilterValue={this.toFilterValue}
        toHistogramValue={this.toHistogramValue}
        overview={overview}
      />
    );
  }

}

DateField.propTypes = FieldFilter.propTypes;

/**
 * Empty field component
 */
function EmptyField(props) {
  return (
    <div>
      <h3>You may reduce the selection of {props.displayName} by
        selecting qualities on the left.</h3>
      <p>For each quality, you can choose specific values to include. By
        default, all values are selected.</p>
    </div>
  );
}

EmptyField.propTypes = FieldFilter.propTypes;



// Helpers
// =======

/**
 * Determine if a filter should be created, or if the values represent the default state.
 *
 * @param {Field} field Field term id
 * @param {any} value Filter value
 * @param {boolean} includeUnknown
 * @param {SummaryCount[]} fieldSummary
 */
function shouldAddFilter(field, value, includeUnknown, fieldSummary) {

  // user doesn't want unknowns
  if (!includeUnknown) return true;

  // user wants everything except unknowns
  if (value == null) return !includeUnknown;

  if (isRange(field)) {
    const values = fieldSummary
      .filter(entry => entry.value != null)
      .map(entry => field.type === 'number' ? Number(entry.value) : entry.value);
    const summaryMin = min(values);
    const summaryMax = max(values);
    return (
      (value.min == null && value.max == null) ||
      (value.min != null && value.min > summaryMin) ||
      (value.max != null && value.max < summaryMax)
    );
  }

  return value.length !== fieldSummary.filter(item => item.value != null).length;
}

/**
 * Finds the component for a field.
 *
 * @param {Field} field
 */
function getFieldDetailComponent(field) {
  return field == null ? null
    : isRange(field) == false ? MembershipField
    : field.type == 'string' ? MembershipField
    : field.type == 'number' ? NumberField
    : field.type == 'date' ? DateField
    : null;
}


/**
 * Creates a function that can be used to update a component instance's state.
 * Arguments passed to the created function are used as property values for the
 * state object. The name of the property is determined by using its position
 * as an index for `argNames`.
 *
 * @param {React.Component} instance
 * @param {...string} argsNames Maps positional arguments to state property names.
 */
function setStateFromArgs(instance, ...argsNames) {
  const length = argsNames.length;
  return function (...args) {
    if (__DEV__ && length !== args.length) {
      console.error(
        'Unexpected number of arguments received in `setStateFromArgs`.',
        'Expected %d, but got %d',
        length,
        args.length
      );
    }
    const nextState = {};
    for (let i = 0; i < length; i++) {
      nextState[argsNames[i]] = args[i];
    }
    instance.setState(nextState);
  }
}

/**
 * Returns an strftime style format string.
 * @param {string} dateString
 */
function getFormatFromDateString(dateString) {
  var matches = dateString.match(dateStringRe);
  if (matches !== null) {
    var [ , , m, d ] = matches;
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
  .replace(/%Y/, String(date.getFullYear()))
  .replace(/%m/, padStart(String(date.getMonth() + 1), 2, '0'))
  .replace(/%d/, padStart(String(date.getDate()), 2, '0'));
}

/**
 * Creates a count object where `count` and `filteredCount` are the sum of
 * `countsA` and `countsB` properties.
 */
// FIXME Remove eslint rule when counts and percentages are figured out
// eslint-disable-next-line no-unused-vars, require-jsdoc
function concatCounts(countsA, countsB) {
  return {
    count: countsA.count + countsB.count,
    filteredCount: countsA.filteredCount + countsB.filteredCount
  }
}

/**
 * Reusable histogram field component. The parent component is responsible for
 * preparing the data.
 */
function unwrapXaxisRange(flotRanges) {
  if (flotRanges == null) {
    return { min: null, max: null };
  }

  var { from, to } = flotRanges.xaxis;
  var min = Number(from.toFixed(2));
  var max = Number(to.toFixed(2));
  return { min, max };
}
