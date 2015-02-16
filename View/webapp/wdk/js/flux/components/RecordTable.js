import _ from 'lodash';
import React from 'react';
import FixedDataTable from 'fixed-data-table';
import Dialog from './Dialog';

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

const primaryKeyName = 'primary_key';
const cellClassName = 'wdk-RecordTable-cell';

const $ = window.jQuery;
const { Table, Column, ColumnGroup } = FixedDataTable;
const { PropTypes } = React;

const isColumnResizing = false;

const sortClassMap = {
  ASC:  'ui-icon ui-icon-arrowthick-1-n',
  DESC: 'ui-icon ui-icon-arrowthick-1-s'
};

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
      tableHeight: 0,
      columnWidths: this.props.meta.attributes.reduce((widths, attr) => {
        widths[attr.name] = attr.name === primaryKeyName ? 400 : 200;
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

    this._updateTableHeight();
    $(window).on('resize', this._updateTableHeight);
  },

  componentWillUnmount() {
    $(window).off('resize', this._updateTableHeight);
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
    if (attribute.name === primaryKeyName) {
      let href = '#' + attribute.value;
      let record = this.props.records[index];
      let handlePrimaryKeyClick = _.partial(this.handlePrimaryKeyClick, record);
      return (
        <div className="wdk-RecordTable-attributeValue">
          <a
            href={href}
            onClick={handlePrimaryKeyClick}
            dangerouslySetInnerHTML={{__html: formatAttribute(attribute) }}
          />
        </div>
      );
    }
    else {
      return (
        <div
          className="wdk-RecordTable-attributeValue"
          dangerouslySetInnerHTML={{__html: formatAttribute(attribute) }}
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
    let { sorting } = this.props.displayInfo;
    // let sortSpec = _.find(sorting, { attributeName: attribute.name });
    let sortSpec = sorting[0];
    let sortClass = sortSpec.attributeName === attribute.name
      ? sortClassMap[sortSpec.direction] : '';

    let sort = _.partial(this.handleSort, attribute);
    let hide = _.partial(this.handleHideColumn, attribute);

    return (
      <div onClick={sort} className="wdk-RecordTable-headerWrapper">
        <span>{formatHeader(attribute.displayName)}</span>
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

  _updateTableHeight() {
    const table = this.refs.table.getDOMNode();
    this.setState({
      tableHeight: window.innerHeight - table.offsetTop - 10
    });
  },

  render() {
    // creates variables: meta, records, and visibleAttributes
    let { meta, records, displayInfo: {  visibleAttributes } } = this.props;
    let visibleNames = this.state.pendingVisibleAttributes.map(a => a.name);

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
            <form onSubmit={this.handleAttributeSelectorSubmit} ref="attributeSelector">
              <ul className="wdk-RecordTable-AttributeSelector">
                {meta.attributes.map(attribute => {
                  const { name, displayName } = attribute;
                  return (
                    <li key={name}>
                      <input type="checkbox"
                        id={'column-select-' + name}
                        name="pendingAttribute"
                        value={name}
                        onChange={this.togglePendingAttribute}
                        checked={visibleNames.indexOf(name) > -1}/>
                      <label htmlFor={'column-select-' + name}> {displayName} </label>
                    </li>
                  );
                })}
              </ul>
              <button>Update</button>
            </form>
          </Dialog>

        <Table
          ref="table"
          width={window.innerWidth - 20}
          maxHeight={this.state.tableHeight}
          rowsCount={records.length}
          rowHeight={30}
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
            cellClassName={cellClassName + " wdk-RecordTable-rowNumber"}
          />
          */}

          {visibleAttributes.map(attribute => {
            let isPk = attribute.name === primaryKeyName;
            let cellClassNames = attribute.name + ' ' + attribute.className +
              ' ' + cellClassName;
            let width = this.state.columnWidths[attribute.name];
            let flexGrow = isPk ? 2 : 1;

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

/* Helper functions */

function formatHeader(value) {
  return capitalize(value.replace(/_/g, ' '));
}

function capitalize(value) {
  return value[0].toUpperCase() + value.slice(1);
}

/** TODO Look up or inject custom formatters */
function formatAttribute(attribute) {
  return attribute.value;

  // FIXME Add type to attribute definition
  // let { value, type } = attribute;
  // switch(type) {
  //   case 'text': return value;
  //   case 'link': return (<a href={value.url}>{value.display}</a>);
  //   default: throw new TypeError(`Unkonwn type "${attribute.type}"` +
  //                                ` for attribute ${attribute.name}`);
  // }
}

/**
 * Return the attributes for the row at index `rowIndex`
 *
 * @param {number} rowIndex
 */
const getRow = function(rowIndex) {
  let rowData = this.props.records[rowIndex].attributes;
  return _.indexBy(rowData, 'name');
};
const rowNumberRenderer = (_, __, ___, rowIndex) => rowIndex + 1;
const getRowNumberColumnWidth = length => String(length).length * 16;

/**
 * Function that doesn't do anything. This is the default for many
 * optional handlers. We can do an equality check as a form of feature
 * detection. E.g., if onSort === noop, then we won't enable sorting.
 */
function noop(){}

export default RecordTable;
