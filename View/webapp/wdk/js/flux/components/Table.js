import partial from 'lodash/function/partial';
import noop from 'lodash/utility/noop';
import { Table } from 'fixed-data-table';
import React from 'react/addons';

/**
 * Wrapper of FixedDataTable.Table component which adds the ability to
 *
 *     - sort columns
 *     - add / remove columns
 *
 */

const SORT_CLASS_MAP = {
  ASC:  'fa fa-lg fa-sort-alpha-asc',
  DESC: 'fa fa-lg fa-sort-alpha-desc'
};

// Bookkeeping for `Table` prop `isColumnResizing`.
let isColumnResizing = false;

const WdkTable = React.createClass({

  propTypes: {

    // Indicates sorted column. This is the same as the dataKey attribute
    // specified in Column.
    sortDataKey: React.PropTypes.string,

    // Direction column is sorted.
    sortDirection: React.PropTypes.oneOf(['ASC', 'DESC']),

    onSort: React.PropTypes.func,

    onHideColumn: React.PropTypes.func
  },

  getDefaultProps() {
    return {
      onSort: noop,
      onHideColumn: noop
    };
  },

  getInitialState() {
    return {
      columnWidths: this._getColumnWidths(this.props)
    };
  },

  componentWillReceiveProps(nextProps) {
    this.setState({
      columnWidths: this._getColumnWidths(nextProps)
    });
  },

  _getColumnWidths(props) {
    var columnWidths = this.state ? this.state.columnWidths : {};

    React.Children.forEach(props.children, child => {
      if (!columnWidths[child.props.dataKey]) {
        columnWidths[child.props.dataKey] = child.props.width || 200;
      }
    });

    return columnWidths;
  },

  handleColumnResize(newWidth, dataKey) {
    isColumnResizing = false;
    this.state.columnWidths[dataKey] = newWidth;
    this.setState({
      columnWidths: this.state.columnWidths
    });
  },

  handleSort(dataKey, event) {
    event.preventDefault();
    this.props.onSort(dataKey);
  },

  handleHideColumn(dataKey, event) {
    event.stopPropagation();
    this.props.onHideColumn(dataKey);
  },

  renderHeader(columnComponent, ...rest) {
    const { dataKey, headerRenderer, isRemovable, isSortable } = columnComponent.props;
    const className = 'wdk-RecordTable-headerWrapper' +
      (isSortable ? ' wdk-RecordTable-headerWrapper_sortable' : '');
    const sortClass = this.props.sortDataKey == columnComponent.props.dataKey
      ? SORT_CLASS_MAP[this.props.sortDirection] : SORT_CLASS_MAP['ASC'] + ' wdk-RecordTable-unsorted';
    const sort = isSortable ? partial(this.handleSort, dataKey) : noop;
    const hide = partial(this.handleHideColumn, dataKey);
    const title = isSortable ? 'Click to sort table by this column.' : '';

    return (
      <div title={title} onClick={sort} className={className}>
        <span>{headerRenderer ? headerRenderer(...rest) : rest[0]}</span>
        {isSortable ? <span className={sortClass}/> : null}
        {isRemovable ? (
          <span className="ui-icon ui-icon-close"
            title="Hide column"
            onClick={hide}/>
        ) : null}
      </div>
    );
  },

  render() {
    const defaultTableProps = {
      isColumnResizing: isColumnResizing,
      onColumnResizeEndCallback: this.handleColumnResize
    };
    const tableProps = Object.assign({}, defaultTableProps, this.props);

    return (
      <Table {...tableProps}>
        {React.Children.map(this.props.children, child => {
          const headerRenderer = partial(this.renderHeader, child);
          const isResizable = child.props.isResizable != null
            ? child.props.isResizable : true;

          return React.addons.cloneWithProps(child, {
            headerRenderer,
            isResizable,
            width: this.state.columnWidths[child.props.dataKey]
          });
        })}
      </Table>
    );
  }

});

export default WdkTable;
