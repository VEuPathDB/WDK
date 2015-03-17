import _ from 'lodash';
import React from 'react';
import FixedDataTable from 'fixed-data-table';
import Table from './Table';
import Dialog from './Dialog';
import {
  formatAttributeName,
  formatAttributeValue
} from '../utils/stringUtils';

/**
 * Generic table with UI features:
 *
 *   - Sort columns
 *   - Move columns
 *   - Show/Hide columns
 *   - Paging
 *
 *
 * NB: A View-Controller will need to pass handlers to this component:
 *
 *   - onSort(columnName: string, direction: string(asc|desc))
 *   - onMoveColumn(columnName: string, newPosition: number)
 *   - onShowColumns(columnNames: Array<string>)
 *   - onHideColumns(columnNames: Array<string>)
 *   - onNewPage(offset: number, numRecords: number)
 */

const $ = window.jQuery;
const { Column } = FixedDataTable;
const { PropTypes } = React;

// Constants
const PRIMARY_KEY_NAME = 'primary_key';
const CELL_CLASS_NAME = 'wdk-RecordTable-cell';
const SORT_CLASS_MAP = {
  ASC:  'ui-icon ui-icon-arrowthick-1-n',
  DESC: 'ui-icon ui-icon-arrowthick-1-s'
};

// Bookkeeping for `Table`
let isColumnResizing = false;


/**
 * Function that doesn't do anything. This is the default for many
 * optional handlers. We can do an equality check as a form of feature
 * detection. E.g., if onSort === noop, then we won't enable sorting.
 */
const noop = () => {};


const AttributeSelectorItem = React.createClass({

  propTypes: {
    attribute: PropTypes.object.isRequired,
    isChecked: PropTypes.bool,
    onChange: PropTypes.func.isRequired
  },

  render() {
    const { name, displayName } = this.props.attribute;
    return (
      <li key={name}>
        <input type="checkbox"
          id={'column-select-' + name}
          name="pendingAttribute"
          value={name}
          onChange={this.props.onChange}
          checked={this.props.isChecked}/>
        <label htmlFor={'column-select-' + name}> {formatAttributeName(displayName)} </label>
      </li>
    );
  }

});

const AttributeSelector = React.createClass({

  propTypes: {
    attributes: PropTypes.array.isRequired,
    selectedAttributes: PropTypes.array,
    onSubmit: PropTypes.func.isRequired,
    onChange: PropTypes.func.isRequired
  },

  render() {
    return (
      <form onSubmit={this.props.onSubmit}>
        <div className="wdk-RecordTable-AttributeSelectorButtonWrapper">
          <button>Update Columns</button>
        </div>
        <ul className="wdk-RecordTable-AttributeSelector">
          {this.props.attributes.map(this._renderItem)}
        </ul>
        <div className="wdk-RecordTable-AttributeSelectorButtonWrapper">
          <button>Update Columns</button>
        </div>
      </form>
    );
  },

  _renderItem(attribute) {
    const isChecked = this._isChecked(attribute);
    return (
      <AttributeSelectorItem
        isChecked={isChecked}
        attribute={attribute}
        onChange={this.props.onChange}
        selectedAttributes={this.props.selectedAttributes}
      />
    );
  },

  // XXX Seems like lodash would provide a method for this...
  _isChecked(attribute) {
    const { selectedAttributes } = this.props;
    for (let index = 0; index < selectedAttributes.length; index++) {
      if (_.isEqual(attribute, selectedAttributes[index])) return true;
    }
    return false;
  }

});

const RecordTable = React.createClass({

  propTypes: {
    meta: PropTypes.object.isRequired,
    displayInfo: PropTypes.object.isRequired,
    records: PropTypes.array.isRequired,
    onSort: PropTypes.func,
    onMoveColumn: PropTypes.func,
    onChangeColumns: PropTypes.func,
    onNewPage: PropTypes.func,
    onRecordClick: PropTypes.func.isRequired
  },

  getDefaultProps() {
    return {
      onSort: noop,
      onMoveColumn: noop,
      onChangeColumns: noop,
      onNewPage: noop
    };
  },

  /**
   * If this is changed, be sure to update handleAttributeSelectorClose()
   */
  getInitialState() {
    return Object.assign({
      columnWidths: this.props.meta.attributes.reduce((widths, attr) => {
        widths[attr.name] = attr.name === PRIMARY_KEY_NAME ? 400 : 200;
        return widths;
      }, {})
    }, this._getInitialAttributeSelectorState());
  },

  getRow(rowIndex) {
    return this.props.records[rowIndex].attributes;
  },

  componentWillReceiveProps(nextProps) {
    this.setState({
      pendingVisibleAttributes: nextProps.displayInfo.visibleAttributes
    });
  },

  componentDidMount() {
    // FIXME More research!
    // const { onMoveColumn } = this.props;
    const onMoveColumn = noop;

    if (onMoveColumn !== noop) {
      // Only set up column reordering if a callback is provided.
      //
      // We are using jQueryUI's .sortable() method to implement
      // visual drag-n-drop of table headings for reordering columns.
      // However, we prevent jQueryUI from actually altering the DOM.
      // Instead, we:
      //   1. Get the new position of the header item (jQueryUI has actually
      //      updated the DOM at this point).
      //   2. Cancel the sort event (.sortable("cancel")).
      //   3. Call the Action to update the column order, allowing React to
      //      update the DOM when it rerenders the component.
      //
      // A future iteration may be to use HTML5's draggable, thus removing the
      // jQueryUI dependency.
      // const $headerRow = $(this.refs.headerRow.getDOMNode());
      const $headerRow = $(this.getDOMNode()).find('.fixedDataTableCellGroup_cellGroup');
      $headerRow.sortable({
        items: '> .public_fixedDataTableCell_main',
        helper: 'clone',
        opacity: 0.7,
        placeholder: 'ui-state-highlight',
        stop(e, ui) {
          const { item } = ui;
          const columnName = item.data('column');
          const newPosition = item.index();
          // We want to let React update the position, so we'll cancel.
          $headerRow.sortable('cancel');
          onMoveColumn(columnName, newPosition);
        }
      });
    }
  },

  handleSort(name) {
    const attributes = this.props.meta.attributes;
    const sortSpec = this.props.displayInfo.sorting[0];
    const attribute = _.find(attributes, { name });
    // Determine the sort direction. If the attribute is the same, then
    // we will reverse the direction... otherwise, we will default to `ASC`.
    const direction = sortSpec.attributeName === name
      ? sortSpec.direction === 'ASC' ? 'DESC' : 'ASC'
      : 'ASC';
    this.props.onSort(attribute, direction);
  },

  // TODO remove
  handleChangeColumns(attributes) {
    this.props.onChangeColumns(attributes);
  },

  handleHideColumn(name) {
    const attributes = this.props.displayInfo.visibleAttributes;
    this.props.onChangeColumns(attributes.filter(attr => attr.name !== name));
  },

  handleNewPage() {
  },

  handleOpenAttributeSelectorClick() {
    this.setState({
      attributeSelectorOpen: !this.state.attributeSelectorOpen
    });
  },

  handleAttributeSelectorClose() {
    this.setState(this._getInitialAttributeSelectorState());
  },

  handleAttributeSelectorSubmit(e) {
    e.preventDefault();
    e.stopPropagation();
    this.props.onChangeColumns(this.state.pendingVisibleAttributes);
    this.setState({
      attributeSelectorOpen: false
    });
  },

  handleColumnResize(newWidth, dataKey) {
    isColumnResizing = false;
    this.state.columnWidths[dataKey] = newWidth;
    this.setState({
      columnWidths: this.state.columnWidths
    });
  },

  handlePrimaryKeyClick(record, event) {
    this.props.onRecordClick(record);
    event.preventDefault();
  },

  /**
   * Filter unchecked checkboxes and map to attributes
   */
  togglePendingAttribute() {
    const form = this.refs.attributeSelector.getDOMNode();
    const { attributes } = this.props.meta;
    const pendingVisibleAttributes = [].slice.call(form.pendingAttribute)
      .filter(a => a.checked)
      .map(a => attributes.filter(attr => attr.name === a.value)[0]);
    this.setState({ pendingVisibleAttributes });
  },

  /**
   * Returns a React-renderable object for a particular cell.
   *
   * @param {any} attribute Value returned by `getRow`.
   */
  renderCell(attribute, attributeName, attributes, index) {
    if (attribute.name === PRIMARY_KEY_NAME) {
      const href = '#' + attribute.value;
      const record = this.props.records[index];
      const handlePrimaryKeyClick = _.partial(this.handlePrimaryKeyClick, record);
      return (
        <div className="wdk-RecordTable-attributeValue">
          <a
            href={href}
            onClick={handlePrimaryKeyClick}
            dangerouslySetInnerHTML={{__html: formatAttributeValue(attribute) }}
          />
        </div>
      );
    }
    else {
      return (
        <div
          className="wdk-RecordTable-attributeValue"
          dangerouslySetInnerHTML={{__html: formatAttributeValue(attribute) }}
        />
      );
    }
  },

  /**
   * Returns a React-renderable object for a particular cell.
   *
   * @param {any} attribute Value of `label` prop of `Column`.
   */
  renderHeader(attribute) {
    return formatAttributeName(attribute.displayName);
  },

  _getInitialAttributeSelectorState() {
    return {
      pendingVisibleAttributes: this.props.displayInfo.visibleAttributes,
      attributeSelectorOpen: false
    };
  },

  // TODO Find a better way to specify row height
  render() {
    // creates variables: meta, records, and visibleAttributes
    const { meta, records, displayInfo: {  visibleAttributes, sorting } } = this.props;
    const { pendingVisibleAttributes } = this.state;
    const sortSpec = sorting[0];

    return (
      <div>

        <p>
          <button onClick={this.handleOpenAttributeSelectorClick}>Add Columns</button>
        </p>

          <Dialog
            modal={true}
            open={this.state.attributeSelectorOpen}
            onClose={this.handleAttributeSelectorClose}
            title="Select Columns">
            <AttributeSelector
              ref="attributeSelector"
              attributes={meta.attributes}
              selectedAttributes={pendingVisibleAttributes}
              onSubmit={this.handleAttributeSelectorSubmit}
              onChange={this.togglePendingAttribute}
            />
          </Dialog>

        <Table
          ref="table"
          width={window.innerWidth - 45}
          maxHeight={this.props.height - 32}
          rowsCount={records.length}
          rowHeight={28}
          rowGetter={this.getRow}
          headerHeight={40}
          sortDataKey={sortSpec.attributeName}
          sortDirection={sortSpec.direction}
          onSort={this.handleSort}
          onHideColumn={this.handleHideColumn}
        >

          {visibleAttributes.map(attribute => {
            const isPk = attribute.name === PRIMARY_KEY_NAME;
            const cellClassNames = attribute.name + ' ' + attribute.className +
              ' ' + CELL_CLASS_NAME;
            const width = this.state.columnWidths[attribute.name];
            const flexGrow = isPk ? 2 : 1;

            return (
              <Column
                fixed={isPk}
                label={attribute}
                dataKey={attribute.name}
                headerRenderer={this.renderHeader}
                cellRenderer={this.renderCell}
                width={width}
                flexGrow={flexGrow}
                isResizable={true}
                isSortable={true}
                isRemovable={!isPk}
                cellClassName={cellClassNames}
              />
            );
          })}
        </Table>

      </div>
    );
  }

});

export default RecordTable;
