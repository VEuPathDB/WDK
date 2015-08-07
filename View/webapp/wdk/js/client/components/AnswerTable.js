import React from 'react';
import { Link } from 'react-router';
import { Column } from 'fixed-data-table';
import AnswerTableCell from './AnswerTableCell';
import AnswerTableHeader from './AnswerTableHeader';
import Table from './Table';
import Dialog from './Dialog';
import {
  formatAttributeName,
  formatAttributeValue
} from '../utils/stringUtils';
import wrappable from '../utils/wrappable';

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

let $ = window.jQuery;
let { PropTypes } = React;

// Constants
let PRIMARY_KEY_NAME = 'primary_key';
let CELL_CLASS_NAME = 'wdk-AnswerTable-cell';

/**
 * Function that doesn't do anything. This is the default for many
 * optional handlers. We can do an equality check as a form of feature
 * detection. E.g., if onSort === noop, then we won't enable sorting.
 */
function noop() {}


let AttributeSelectorItem = React.createClass({

  propTypes: {
    attribute: PropTypes.object.isRequired,
    isChecked: PropTypes.bool,
    onChange: PropTypes.func.isRequired
  },

  render() {
    let { attribute } = this.props;
    let name = attribute.name;
    let displayName = attribute.displayName;
    return (
      <li key={name}>
        <input type="checkbox"
          id={'column-select-' + name}
          name="pendingAttribute"
          value={name}
          onChange={this.props.onChange}
          disabled={!attribute.isRemovable}
          checked={this.props.isChecked}/>
        <label htmlFor={'column-select-' + name}> {formatAttributeName(displayName)} </label>
      </li>
    );
  }

});

let AttributeSelector = React.createClass({

  propTypes: {
    attributes: PropTypes.array.isRequired,
    selectedAttributes: PropTypes.array,
    onSubmit: PropTypes.func.isRequired,
    onChange: PropTypes.func.isRequired
  },

  render() {
    return (
      <form onSubmit={this.props.onSubmit}>
        <div className="wdk-AnswerTable-AttributeSelectorButtonWrapper">
          <button>Update Columns</button>
        </div>
        <ul className="wdk-AnswerTable-AttributeSelector">
          {this.props.attributes.map(this._renderItem)}
        </ul>
        <div className="wdk-AnswerTable-AttributeSelectorButtonWrapper">
          <button>Update Columns</button>
        </div>
      </form>
    );
  },

  _renderItem(attribute) {
    let isAttribute = attr => attr.name === attribute.name;
    return (
      <AttributeSelectorItem
        key={attribute.name}
        isChecked={this.props.selectedAttributes.some(isAttribute)}
        attribute={attribute}
        onChange={this.props.onChange}
      />
    );
  }

});

let AnswerTable = React.createClass({

  propTypes: {
    meta: PropTypes.object.isRequired,
    displayInfo: PropTypes.object.isRequired,
    records: PropTypes.array.isRequired,
    onSort: PropTypes.func,
    onMoveColumn: PropTypes.func,
    onChangeColumns: PropTypes.func,
    onNewPage: PropTypes.func,
    onRecordClick: PropTypes.func
  },

  getDefaultProps() {
    return {
      onSort: noop,
      onMoveColumn: noop,
      onChangeColumns: noop,
      onNewPage: noop,
      onRecordClick: noop
    };
  },

  /**
   * If this is changed, be sure to update handleAttributeSelectorClose()
   */
  getInitialState() {
    return Object.assign({
      columnWidths: this.props.meta.attributes.reduce((widths, attr) => {
        let name = attr.name;
        let displayName = attr.displayName;
        // 8px per char, plus 12px for sort icon
        let width = Math.max(displayName.length * 8.5 + 12, 200);
        widths[name] = name === PRIMARY_KEY_NAME ? 400 : width;
        return widths;
      }, {})
    }, this._getInitialAttributeSelectorState());
  },

  getRow(rowIndex) {
    return this.props.records[rowIndex].attributes;
  },

  getCellData(name, attributes) {
    return attributes[name];
  },

  componentWillReceiveProps(nextProps) {
    this.setState({
      pendingVisibleAttributes: nextProps.displayInfo.visibleAttributes
    });
  },

  componentDidMount() {
    this.node = React.findDOMNode(this);
    this.$node = $(this.node);
    this.addEventListeners();
    this.setupMoveColumn();
  },

  componentWillUnmount() {
    this.removeEventListeners();
  },

  addEventListeners() {
    this.$node.on('mouseenter', '.wdk-AnswerTable-attributeValue', this.setTitle);
    this.$node.on('mouseleave', '.wdk-AnswerTable-attributeValue', this.unsetTitle);
  },

  removeEventListeners() {
    this.$node.off('mouseenter', '.wdk-AnswerTable-attributeValue', this.setTitle);
    this.$node.off('mouseleave', '.wdk-AnswerTable-attributeValue', this.unsetTitle);
  },

  setTitle(e) {
    if (e.target.offsetWidth < e.target.scrollWidth) {
      e.target.setAttribute('title', e.target.textContent);
    }
  },

  unsetTitle(e) {
    e.target.removeAttribute('title');
  },

  setupMoveColumn() {

    // FIXME More research!
    // let { onMoveColumn } = this.props;
    let onMoveColumn = noop;

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
      // let $headerRow = $(this.refs.headerRow.getDOMNode());
      let $headerRow = this.$node.find('.fixedDataTableCellGroup_cellGroup');
      $headerRow.sortable({
        items: '> .public_fixedDataTableCell_main',
        helper: 'clone',
        opacity: 0.7,
        placeholder: 'ui-state-highlight',
        stop(e, ui) {
          let { item } = ui;
          let columnName = item.data('column');
          let newPosition = item.index();
          // We want to let React update the position, so we'll cancel.
          $headerRow.sortable('cancel');
          onMoveColumn(columnName, newPosition);
        }
      });
    }
  },

  handleSort(name) {
    let attributes = this.props.meta.attributes;
    let sortSpec = this.props.displayInfo.sorting[0];
    let attribute = attributes.find(attr => attr.name === name);
    // Determine the sort direction. If the attribute is the same, then
    // we will reverse the direction... otherwise, we will default to `ASC`.
    let direction = sortSpec.attributeName === name
      ? sortSpec.direction === 'ASC' ? 'DESC' : 'ASC'
      : 'ASC';
    this.props.onSort(attribute, direction);
  },

  // TODO remove
  handleChangeColumns(attributes) {
    this.props.onChangeColumns(attributes);
  },

  handleHideColumn(name) {
    let attributes = this.props.displayInfo.visibleAttributes
      .filter(attr => attr.name !== name);
    this.props.onChangeColumns(attributes);
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
    let form = React.findDOMNode(this.refs.attributeSelector);
    let attributes = this.props.meta.attributes;
    let visibleAttributes = this.props.displayInfo.visibleAttributes;

    let checkedAttrs = [].slice.call(form.pendingAttribute)
      .filter(a => a.checked)
      .map(a => attributes.find(attr => attr.name === a.value));

    // Remove visible atributes that are not checked.
    // Then, concat checked attributes that are not currently visible.
    let pendingVisibleAttributes = visibleAttributes
      .filter(attr => checkedAttrs.find(p => p.name === attr.name))
      .concat(checkedAttrs.filter(attr => !visibleAttributes.find(a => a.name === attr.name)));

    this.setState({ pendingVisibleAttributes });
  },

  /**
   * Returns a React-renderable object for a particular cell.
   *
   * @param {any} attribute Value returned by `getRow`.
   */
  renderCell(attribute, attributeName, attributes, index, columnData, width) {
    return (
      <div className="wdk-AnswerTable-attributeValue" style={{width: width - 12 }}>
        <AnswerTableCell
          attribute={attribute}
          record={this.props.records[index]}
          recordClass={this.props.meta.class}
          width={width}
        />
      </div>
    );
  },

  /**
   * Returns a React-renderable object for a particular cell.
   *
   * @param {any} attribute Value of `label` prop of `Column`.
   */
  renderHeader(label, dataKey, columnData) {
    let attributeMeta = columnData.attributeDefinition;

    return (
      <div className="wdk-AnswerTable-header" title={attributeMeta.help || ''}>
        <AnswerTableHeader attributeMeta={attributeMeta}/>
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
    let { pendingVisibleAttributes } = this.state;
    let { meta, records, displayInfo } = this.props;
    let visibleAttributes = displayInfo.visibleAttributes;
    let sortSpec = displayInfo.sorting[0];

    return (
      <div className="wdk-AnswerTable">

        <p className="wdk-AnswerTable-AttributeSelectorOpenButton">
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
          headerHeight={35}
          sortDataKey={sortSpec.attributeName}
          sortDirection={sortSpec.direction}
          onSort={this.handleSort}
          onHideColumn={this.handleHideColumn}
        >

          {visibleAttributes.map(attribute => {
            let name = attribute.name;
            let isPk = name === PRIMARY_KEY_NAME;
            let cellClassNames = name + ' ' + attribute.className +
              ' ' + CELL_CLASS_NAME;
            let width = this.state.columnWidths[name];
            let columnData = {
              attributeDefinition: attribute
            };
            // let flexGrow = isPk ? 2 : 1;

            return (
              <Column
                key={name}
                dataKey={name}
                fixed={isPk}
                headerRenderer={this.renderHeader}
                cellRenderer={this.renderCell}
                cellDataGetter={this.getCellData}
                columnData={columnData}
                width={width}
                isResizable={true}
                isSortable={attribute.isSortable}
                isRemovable={attribute.isRemovable}
                cellClassName={cellClassNames}
              />
            );
          })}
        </Table>

      </div>
    );
  }

});

export default wrappable(AnswerTable);
