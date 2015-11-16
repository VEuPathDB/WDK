/**
 * Sortable table for WDK-formatted data.
 *
 * This one used DataTables jQuery plugin
 */

import { Component, PropTypes } from 'react';
import mapValues from 'lodash/object/mapValues';
import { formatAttributeValue } from '../utils/componentUtils';

let $ = window.jQuery;

let formatColumns = columns => columns.map(
  column => Object.assign({
    data: column.name,
    className: 'wdk-DataTableCell wdk-DataTableCell__' + column.name,
    title: column.displayName || column.name
  }, column)
);

let formatData = data => data.map(
  row => mapValues(row, formatAttributeValue)
);

let formatSorting = (columns, sorting) => {
  if (sorting.length === 0) return [ [0, 'asc'] ];

  return sorting.map(sort => {
    let index = columns.findIndex(column => column.name === sort.name);
    if (index === -1) {
      console.warn("Could not determine sort index for the column " + sort.name);
      return [];
    }
    return [ index, sort.direction ]
  });
}

export default class DataTable extends Component {

  componentDidMount() {
    let tableOpts = Object.assign({}, DataTable.defaultDataTableOpts, {
      columns: formatColumns(this.props.columns),
      data: formatData(this.props.data),
      searching: this.props.isSearchable,
      order: formatSorting(this.props.columns, this.props.sorting),
      scrollY: this.props.height
    });

    if (this.props.height != null)
      tableOpts.scrollY = this.props.height;

    this.dataTable = $('<table class="wdk-DataTable">')
    .width(this.props.width)
    .appendTo(React.findDOMNode(this))
    .DataTable(tableOpts);
  }

  componentWillReceiveProps(nextProps) {
    this.dataTable
    .rows().remove()
    .rows.add(nextProps.data)
    .draw();
  }

  componentWillUnmount() {
    this.dataTable.destroy();
  }

  render() {
    return <div className="wdk-DataTableContainer"/>
  }
}

let CSSPropType = PropTypes.oneOfType([
  PropTypes.string,
  PropTypes.number
]);

DataTable.propTypes = {
  /**
   * Array of descriptors used for table column headers.
   * `name` is used as a key for looking up cell data in `data`.
   * `displayName` is an optional property used for the header text (`name` is
   * used as a fallback).
   */
  columns: PropTypes.arrayOf(
    PropTypes.shape({
      name: PropTypes.string.isRequired,
      displayName: PropTypes.string
    })
  ),

  /**
   * The data to display in the table. An array of objects whose keys correspond
   * to the `name` property in the `columns` prop.
   */
  data: PropTypes.arrayOf(PropTypes.object).isRequired,

  /**
   * Default sorting for the table. Each item indicates the column and direction.
   */
  sorting: PropTypes.arrayOf(PropTypes.shape({
    name: PropTypes.string.isRequired,
    direction: PropTypes.oneOf(['asc', 'desc']).isRequired
  })),

  /** width of the table - if a string, treated as a CSS unit; if a number, treated as px */
  width: CSSPropType,

  /** height of the table - if a string, treated as a CSS unit; if a number, treated as px */
  height: CSSPropType,

  /** can users search the table */
  isSearchable: PropTypes.bool
};

DataTable.defaultProps = {
  width: undefined,
  height: undefined,
  isSearchable: false
};

/** Default DataTables jQuery plugin options. */
DataTable.defaultDataTableOpts = {
  info: false,
  scrollX: true,
  deferRender: true,
  paging: false,
  scrollCollapse: true,
  searching: false
};
