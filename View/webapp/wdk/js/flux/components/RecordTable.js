import _ from 'lodash';
import React from 'react';
import FixedDataTable from 'fixed-data-table';
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
const { Table, Column, ColumnGroup } = FixedDataTable;
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

  componentWillReceiveProps(nextProps) {
    this.getRow = _.memoize(_.bind(getRow, this));
    this.setState({
      pendingVisibleAttributes: nextProps.displayInfo.visibleAttributes
    });
  },

  componentWillMount() {
    this.getRow = _.memoize(_.bind(getRow, this));
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

  handleSort(attribute) {
    const sortSpec = this.props.displayInfo.sorting[0];
    // Determine the sort direction. If the attribute is the same, then
    // we will reverse the direction... otherwise, we will default to `ASC`.
    const direction = sortSpec.attributeName === attribute.name
      ? sortSpec.direction === 'ASC' ? 'DESC' : 'ASC'
      : 'ASC';
    this.props.onSort(attribute, direction);
  },

  // TODO remove
  handleChangeColumns(attributes) {
    this.props.onChangeColumns(attributes);
  },

  handleHideColumn(attribute, e) {
    e.stopPropagation(); // prevent click event from bubbling to sort handler
    const attributes = this.props.displayInfo.visibleAttributes;
    this.props.onChangeColumns(attributes.filter(attr => attr !== attribute));
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
    const { sorting } = this.props.displayInfo;
    // const sortSpec = _.find(sorting, { attributeName: attribute.name });
    const sortSpec = sorting[0];
    const sortClass = sortSpec.attributeName === attribute.name
      ? SORT_CLASS_MAP[sortSpec.direction] : '';

    const sort = _.partial(this.handleSort, attribute);
    const hide = _.partial(this.handleHideColumn, attribute);

    return (
      <div onClick={sort} className="wdk-RecordTable-headerWrapper">
        <span>{formatAttributeName(attribute.displayName)}</span>
        <span className={sortClass}/>
        <span className="ui-icon ui-icon-close"
          title="Hide column"
          onClick={hide}/>
      </div>
    );
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
    const { meta, records, displayInfo: {  visibleAttributes } } = this.props;
    const { pendingVisibleAttributes } = this.state;

    return (
      <div>

        <p>
          <button onClick={this.handleOpenAttributeSelectorClick}>More data</button>
        </p>

          <Dialog
            modal={true}
            open={this.state.attributeSelectorOpen}
            onClose={this.handleAttributeSelectorClose}
            title="Choose columns to shoe or hide">
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
          scrollTop={0}
          scrollLeft={0}
          overflowX="auto"
          overfloxY="auto"
          headerHeight={40}
          isColumnResizing={isColumnResizing}
          onColumnResizeEndCallback={this.handleColumnResize}
        >

        {/*
          <Column
            fixed={true}
            label=""
            dataKey="#"
            width={getRowNumberColumnWidth(records.length)}
            align="right"
            cellRenderer={rowNumberRenderer}
            cellClassName={CELL_CLASS_NAME + " wdk-RecordTable-rowNumber"}
          />
          */}

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
                cellClassName={cellClassNames}
              />
            );
          })}
        </Table>

      </div>
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
        <ul className="wdk-RecordTable-AttributeSelector">
          {this.props.attributes.map(this._renderItem)}
        </ul>
        <button>Update</button>
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
    let index = 0;
    let testAttribute;
    const { selectedAttributes } = this.props;
    while (testAttribute = selectedAttributes[index++]) {
      if (_.isEqual(testAttribute, attribute))
        return true;
    }
    return false;
  }

});


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


/**
 * Return the attributes for the row at index `rowIndex`
 *
 * @param {number} rowIndex
 */
const getRow = function(rowIndex) {
  const rowData = this.props.records[rowIndex].attributes;
  return _.indexBy(rowData, 'name');
};
const rowNumberRenderer = (_, __, ___, rowIndex) => rowIndex + 1;
const getRowNumberColumnWidth = length => String(length).length * 16;

/**
 * Function that doesn't do anything. This is the default for many
 * optional handlers. We can do an equality check as a form of feature
 * detection. E.g., if onSort === noop, then we won't enable sorting.
 */
const noop = () => {};

export default RecordTable;
