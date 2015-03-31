import partialRight from 'lodash/function/partialRight';
import React from 'react';
import { Link } from 'react-router';
import { Column } from 'fixed-data-table';
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
const { PropTypes } = React;

// Constants
const PRIMARY_KEY_NAME = 'primary_key';
const CELL_CLASS_NAME = 'wdk-RecordTable-cell';

/**
 * Function that doesn't do anything. This is the default for many
 * optional handlers. We can do an equality check as a form of feature
 * detection. E.g., if onSort === noop, then we won't enable sorting.
 */
const noop = () => {};

const not = func => (...args) => !func(...args);

/**
 * Higher-order function that returns a predicate to test the value of an
 * iterable item whose key is `key` against the value `val`.
 *
 * Example
 *
 *     const iterable = new Map({ name: 'Dave' });
 *     const nameIsDave = where('name', 'Dave');
 *     nameIsDave(iterable); //=> true
 *
 * @param {string} key Where to find the property
 * @param {any} val The test value
 * @return boolean
 */
const where = (key, val) => iter => iter.get(key) === val;

const AttributeSelectorItem = React.createClass({

  propTypes: {
    attribute: PropTypes.object.isRequired,
    isChecked: PropTypes.bool,
    onChange: PropTypes.func.isRequired
  },

  render() {
    const { attribute } = this.props;
    const name = attribute.get('name');
    const displayName = attribute.get('displayName');
    return (
      <li key={name}>
        <input type="checkbox"
          id={'column-select-' + name}
          name="pendingAttribute"
          value={name}
          onChange={this.props.onChange}
          disabled={!attribute.get('isRemovable')}
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
          {this.props.attributes.map(this._renderItem).toArray()}
        </ul>
        <div className="wdk-RecordTable-AttributeSelectorButtonWrapper">
          <button>Update Columns</button>
        </div>
      </form>
    );
  },

  _renderItem(attribute) {
    return (
      <AttributeSelectorItem
        key={attribute.get('name')}
        isChecked={this.props.selectedAttributes.contains(attribute)}
        attribute={attribute}
        onChange={this.props.onChange}
      />
    );
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

  contextTypes: {
    getCellRenderer: PropTypes.func.isRequired
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
      columnWidths: this.props.meta.get('attributes').reduce((widths, attr) => {
        const name = attr.get('name');
        const displayName = attr.get('displayName');
        // 8px per char, plus 12px for sort icon
        const width = Math.max(displayName.length * 8.5 + 12, 200);
        widths[name] = name === PRIMARY_KEY_NAME ? 400 : width;
        return widths;
      }, {})
    }, this._getInitialAttributeSelectorState());
  },

  getRow(rowIndex) {
    return this.props.records.getIn([rowIndex, 'attributes']);
  },

  getCellData(name, attributes) {
    return attributes.get(name);
  },

  componentWillReceiveProps(nextProps) {
    this.setState({
      pendingVisibleAttributes: nextProps.displayInfo.get('visibleAttributes')
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
    const attributes = this.props.meta.get('attributes');
    const sortSpec = this.props.displayInfo.getIn(['sorting', 0]);
    const attribute = attributes.find(where('name', name));
    // Determine the sort direction. If the attribute is the same, then
    // we will reverse the direction... otherwise, we will default to `ASC`.
    const direction = sortSpec.get('attributeName') === name
      ? sortSpec.get('direction') === 'ASC' ? 'DESC' : 'ASC'
      : 'ASC';
    this.props.onSort(attribute, direction);
  },

  // TODO remove
  handleChangeColumns(attributes) {
    this.props.onChangeColumns(attributes);
  },

  handleHideColumn(name) {
    const attributes = this.props.displayInfo.get('visibleAttributes')
      .filter(not(where('name', name)));
    this.props.onChangeColumns(attributes);
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

  handlePrimaryKeyClick(record, event) {
    this.props.onRecordClick(record);
    event.preventDefault();
  },

  /**
   * Filter unchecked checkboxes and map to attributes
   */
  togglePendingAttribute() {
    const form = this.refs.attributeSelector.getDOMNode();
    const attributes = this.props.meta.get('attributes');
    const visibleAttributes = this.props.displayInfo.get('visibleAttributes');

    const checkedAttrs = [].slice.call(form.pendingAttribute)
      .filter(a => a.checked)
      .map(a => attributes.find(attr => attr.get('name') === a.value));

    // Remove visible atributes that are not checked.
    // Then, concat checked attributes that are not currently visible.
    const pendingVisibleAttributes = visibleAttributes
      .filter(attr => checkedAttrs.find(p => p.get('name') === attr.get('name')))
      .concat(checkedAttrs.filter(attr => !visibleAttributes.find(a => a.get('name') === attr.get('name'))));

    this.setState({ pendingVisibleAttributes });
  },

  /**
   * Returns a React-renderable object for a particular cell.
   *
   * @param {any} attribute Value returned by `getRow`.
   */
  renderCell(attribute, attributeName, attributes, index, columnData, width) {
    // const width = this.state.columnWidths[attributeName] - 10;
    if (attribute.get('name') === PRIMARY_KEY_NAME) {
      const record = this.props.records.get(index);
      // const href = '#' + attribute.get('value');
      // const handlePrimaryKeyClick = _.partial(this.handlePrimaryKeyClick, record);
      const href = this.props.recordHrefGetter(record);
      return (
        <div
          style={{ width: width - 12 }}
          className="wdk-RecordTable-attributeValue"
        >
          <Link
            className="wdk-RecordTable-recordLink"
            to={href}
            dangerouslySetInnerHTML={{__html: formatAttributeValue(attribute) }}
          />
        </div>
      );
    }
    else {
      return (
        <div
          style={{ width: width - 12 }}
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
    return (
      <span title={attribute.get('help')}>
        {formatAttributeName(attribute.get('displayName'))}
      </span>
    );
  },

  _getInitialAttributeSelectorState() {
    return {
      pendingVisibleAttributes: this.props.displayInfo.get('visibleAttributes'),
      attributeSelectorOpen: false
    };
  },

  // TODO Find a better way to specify row height
  render() {
    // creates variables: meta, records, and visibleAttributes
    const { pendingVisibleAttributes } = this.state;
    const { meta, records, displayInfo } = this.props;
    const visibleAttributes = displayInfo.get('visibleAttributes');
    const sortSpec = displayInfo.getIn(['sorting', 0]);

    const cellRenderer = this.context.getCellRenderer(meta.get('class'), this.renderCell) || this.renderCell;

    return (
      <div className="wdk-RecordTable">

        <p className="wdk-RecordTable-AttributeSelectorOpenButton">
          <button onClick={this.handleOpenAttributeSelectorClick}>Add Columns</button>
        </p>

          <Dialog
            modal={true}
            open={this.state.attributeSelectorOpen}
            onClose={this.handleAttributeSelectorClose}
            title="Select Columns">
            <AttributeSelector
              ref="attributeSelector"
              attributes={meta.get('attributes')}
              selectedAttributes={pendingVisibleAttributes}
              onSubmit={this.handleAttributeSelectorSubmit}
              onChange={this.togglePendingAttribute}
            />
          </Dialog>

        <Table
          ref="table"
          width={window.innerWidth - 45}
          maxHeight={this.props.height - 32}
          rowsCount={records.size}
          rowHeight={28}
          rowGetter={this.getRow}
          headerHeight={35}
          sortDataKey={sortSpec.get('attributeName')}
          sortDirection={sortSpec.get('direction')}
          onSort={this.handleSort}
          onHideColumn={this.handleHideColumn}
        >

          {visibleAttributes.map(attribute => {
            const name = attribute.get('name');
            const isPk = name === PRIMARY_KEY_NAME;
            const cellClassNames = name + ' ' + attribute.get('className') +
              ' ' + CELL_CLASS_NAME;
            const width = this.state.columnWidths[name];
            // const flexGrow = isPk ? 2 : 1;

            return (
              <Column
                key={name}
                dataKey={name}
                fixed={isPk}
                label={attribute}
                headerRenderer={this.renderHeader}
                cellRenderer={partialRight(cellRenderer, this.renderCell)}
                cellDataGetter={this.getCellData}
                width={width}
                isResizable={true}
                isSortable={attribute.get('isSortable')}
                isRemovable={attribute.get('isRemovable')}
                cellClassName={cellClassNames}
              />
            );
          }).toArray()}
        </Table>

      </div>
    );
  }

});

export default RecordTable;
