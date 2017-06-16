/* global Scroller */
import 'lib/zynga-scroller/Animate';
import 'lib/zynga-scroller/Scroller';
import {partial, values, sum, noop} from 'lodash';
import React from 'react';
import PropTypes from 'prop-types';
import TouchableArea from './TouchableArea';

import { Table as FDTable } from 'fixed-data-table';
export { Column, ColumnGroup, Cell } from 'fixed-data-table';

// import css file
import 'fixed-data-table/dist/fixed-data-table.css';

/**
 * Wrapper of FixedDataTable.Table component which adds the ability to
 *
 *     - sort columns
 *     - add / remove columns
 *
 */

let SORT_CLASS_MAP = {
  ASC: 'fa fa-lg fa-sort-alpha-asc',
  DESC: 'fa fa-lg fa-sort-alpha-desc'
};

// Bookkeeping for `Table` prop `isColumnResizing`.
let isColumnResizing = false;

function isTouchDevice() {
  return 'ontouchstart' in document.documentElement // works on most browsers
      || 'ontouchstart' in document // works with chrome emulator mode
      || 'onmsgesturechange' in window; // works on ie10
}

export class Table extends React.Component {

  constructor(props) {
    super(props);
    this.handleScrollStart = this.handleScrollStart.bind(this);
    this.handleScrollEnd = this.handleScrollEnd.bind(this);
    this.handleColumnResize = this.handleColumnResize.bind(this);
    this.handleSort = this.handleSort.bind(this);
    this.handleHideColumn = this.handleHideColumn.bind(this);
    this.handleContentHeightChange = this.handleContentHeightChange.bind(this);
    this.renderHeader = this.renderHeader.bind(this);

    this.state = {
      disablePointerEvents: false,
      columnWidths: this._getColumnWidths(this.props),
      left: 0,
      top: 0
    };
  }

  componentWillReceiveProps(nextProps) {
    this.setState({
      columnWidths: this._getColumnWidths(nextProps)
    });
  }

  componentWillMount() {
    this.scroller = new Scroller(this._handleScroll);
  }

  _getColumnWidths(props) {
    var columnWidths = this.state ? this.state.columnWidths : {};

    React.Children.forEach(props.children, child => {
      if (!columnWidths[child.props.dataKey]) {
        columnWidths[child.props.dataKey] = child.props.width || 200;
      }
    });

    return columnWidths;
  }

  handleScrollStart() {
    this.setState({ disablePointerEvents: true });
  }

  handleScrollEnd() {
    this.setState({ disablePointerEvents: false });
  }

  _handleScroll(left, top) {
    this.setState({ left, top });
  }

  handleColumnResize(newWidth, dataKey) {
    isColumnResizing = false;
    const columnWidths = Object.assign({}, this.state.columnWidths, {
      [dataKey]: newWidth
    });
    this.setState({ columnWidths });
  }

  handleSort(dataKey, event) {
    event.preventDefault();
    this.props.onSort(dataKey);
  }

  handleHideColumn(dataKey, event) {
    event.stopPropagation();
    this.props.onHideColumn(dataKey);
  }

  handleContentHeightChange(height) {
    console.log('-- contentHeightChange', height);
    this.scroller.setDimensions(
      // clientWidth, e.g., width of visible table area
      this.props.width,
      // clientHeight, e.g. height of visible table area
      this.height || Math.min(this.maxHeight, height),
      // contentWidth, e.g., computed with of content within table
      sum(values(this.state.columnWidths)),
      // contentHeight, e.g., computed height of content within table
      height
    );
  }

  renderHeader(columnComponent, ...rest) {
    let { dataKey, headerRenderer, isRemovable, isSortable } = columnComponent.props;
    let width = rest[rest.length - 1];
    let className = 'wdk-AnswerTable-headerWrapper' +
      (isSortable ? ' wdk-AnswerTable-headerWrapper_sortable' : '');
    let sortClass = this.props.sortDataKey === columnComponent.props.dataKey
      ? SORT_CLASS_MAP[this.props.sortDirection] : SORT_CLASS_MAP.ASC + ' wdk-AnswerTable-unsorted';
    let sort = isSortable ? partial(this.handleSort, dataKey) : noop;
    let hide = partial(this.handleHideColumn, dataKey);
    let title = isSortable ? 'Click to sort table by ' + rest[0] + '.' : '';

    return (
      <div title={title} onClick={sort} className={className}>
        <span className="wdk-AnswerTable-header" style={{width: width - 64}}>{headerRenderer ? headerRenderer(...rest) : rest[0]}</span>
        {isSortable ? <span className={sortClass}/> : null}
        {isRemovable ? (
          <span className="ui-icon ui-icon-close"
            title="Hide column"
            onClick={hide}/>
        ) : null}
      </div>
    );
  }

  render() {
    let tableProps = Object.assign({
      isColumnResizing: isColumnResizing,
      onColumnResizeEndCallback: this.handleColumnResize
    }, this.props);

    tableProps.style = Object.assign({}, tableProps.style, {
      pointerEvents: this.state.disablePointerEvents ? 'none' : null
    });

    if (isTouchDevice()) {
      Object.assign(tableProps, {
        scrollTop: this.state.top,
        scrollLeft: this.state.left,
        overflowX: 'hidden',
        overflowY: 'hidden',
        onContentHeightChange: this.handleContentHeightChange
      });
    }

    return (
      <TouchableArea scroller={this.scroller}>
        <FDTable {...tableProps} onScrollStart={this.handleScrollStart} onScrollEnd={this.handleScrollEnd}>
          {React.Children.map(this.props.children, child => {
            let headerRenderer = partial(this.renderHeader, child);
            let isResizable = child.props.isResizable != null
              ? child.props.isResizable : true;

            return React.cloneElement(child, {
              headerRenderer,
              isResizable,
              width: this.state.columnWidths[child.props.dataKey]
            });
          })}
        </FDTable>
      </TouchableArea>
    );
  }

}

Table.propTypes = {

  // Indicates sorted column. This is the same as the dataKey attribute
  // specified in Column.
  sortDataKey: PropTypes.string,

  // Direction column is sorted.
  sortDirection: PropTypes.oneOf(['ASC', 'DESC']),

  onSort: PropTypes.func,

  onHideColumn: PropTypes.func
};

Table.defaultProps = {
  onSort: noop,
  onHideColumn: noop
};
