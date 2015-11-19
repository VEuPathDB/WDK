/**
 * Sortable table for WDK-formatted data.
 *
 * This one used DataTables jQuery plugin
 */

import { Component, PropTypes } from 'react';
import ReactDOM from 'react-dom';
import mapValues from 'lodash/object/mapValues';
import { formatAttributeValue } from '../utils/componentUtils';

let $ = window.jQuery;

let expandColumn = {
  className: 'wdk-DataTableCell wdk-DataTableCellExpand',
  orderable: false,
  defaultContent: ''
};

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

  constructor() {
    super(...arguments);
    this.childRowNodes = new Map();
  }

  componentDidMount() {
    let node = ReactDOM.findDOMNode(this);

    let data = formatData(this.props.data);

    let columns = this.props.childRow
      ? [ expandColumn, ...formatColumns(this.props.columns) ]
      : formatColumns(this.props.columns);


    let order = formatSorting(columns, this.props.sorting.length === 0
      ? [ { name: this.props.columns[0].name, direction: 'asc' } ] : this.props.sorting);

    let tableOpts = Object.assign({}, DataTable.defaultDataTableOpts, {
      columns,
      data,
      order,
      searching: this.props.isSearchable,
      scrollY: this.props.height
    });

    if (this.props.height != null)
      tableOpts.scrollY = this.props.height;

    this.dataTable = $('<table class="wdk-DataTable">')
    .width(this.props.width)
    .appendTo(node)
    .DataTable(tableOpts);

    $(node).on('click', 'td.wdk-DataTableCellExpand', e => {
      let tr = $(e.target).closest('tr');
      let row = this.dataTable.row(tr);

      if (row.child.isShown()) {
        row.child.hide();
        tr.removeClass('wdk-DataTableRow__expanded');
      }

      else {
        if (!this.childRowNodes.has(tr.get(0))) {
          this.childRowNodes.set(tr.get(0), document.createElement('div'));
          row.child(this.childRowNodes.get(tr.get(0)));
        }
        this.renderChildRow(row, this.childRowNodes.get(tr.get(0)));
        row.child.show();
        tr.addClass('wdk-DataTableRow__expanded');
      }
    });
  }

  // FIXME This is probably not what we want to do. We probably want to call the
  // appropriate DataTable api method for each property that changed.
  componentWillReceiveProps(nextProps) {
    this.dataTable
    .rows().remove()
    .rows.add(nextProps.data)
    .draw();
    this.removeChildRows();
  }

  componentWillUnmount() {
    this.dataTable.destroy();
    this.removeChildRows();
  }

  removeChildRows() {
    for (let childNode of this.childRowNodes.values()) {
      ReactDOM.unmountComponentAtNode(childNode);
    }
    this.childRowNodes.clear();
  }

  renderChildRow(row, targetNode) {
    if (typeof this.props.childRow === 'string') {
      targetNode.innerHTML = this.props.childRow;
    }

    else {
      let props = { rowIndex: row.index(), rowData: row.data() };
      ReactDOM.render(React.createElement(this.props.childRow, props), targetNode);
    }
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
  isSearchable: PropTypes.bool,

  /**
   * Determines the body of child rows. If this is provided, each table row will
   * be rendered with an expansion toggle. This can be a string, a function, or
   * a React Component. If it is a function, the function will receive the same
   * props argument that the React Component would receive as props:
   *
   *    props: {
   *      rowIndex: number;
   *      rowData: Object;
   *    }
   */
  childRow: PropTypes.oneOfType([
    PropTypes.node,
    PropTypes.func
  ])

};

DataTable.defaultProps = {
  width: undefined,
  height: undefined,
  isSearchable: false,
  sorting: []
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
