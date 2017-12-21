import React from 'react';
import PropTypes from 'prop-types';

import CheckboxTree from '../../CheckboxTree';
import Dialog from '../../Dialog';
import { lazy } from '../../../utils/componentUtils';
import { getTree } from '../../../utils/FilterServiceUtils';

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

  return lazy(render => require(['../Table'], render))(LazyFilteredData)
})();

export default FilteredData;

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
