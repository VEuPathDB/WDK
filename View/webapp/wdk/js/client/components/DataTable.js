/**
 * Sortable table for WDK-formatted data.
 *
 * This one used DataTables jQuery plugin
 */

import { Component, PropTypes } from 'react';
import ReactDOM from 'react-dom';
import mapValues from 'lodash/object/mapValues';
import partial from 'lodash/function/partial';
import { formatAttributeValue, wrappable } from '../utils/componentUtils';

const $ = window.jQuery;

const expandColumn = {
  className: 'wdk-DataTableCell wdk-DataTableCellExpand',
  orderable: false,
  defaultContent: ''
};

let DataTable = props => {
  return (
    <div ref={node => setupTable(node, props)} className="wdk-DataTableContainer"/>
  );
};

export default wrappable(DataTable);

let setupTable = (node, props) => {
  if (node == null) return;

  // Destroy DataTable instance, removing DOM node and event handlers
  $(node).find('table:first').DataTable().destroy(true);

  let {
    childRow,
    sorting,
    searchable,
    height,
    width
  } = props;

  let data = formatData(props.data);

  let columns = childRow
    ? [ expandColumn, ...formatColumns(props.columns) ]
    : formatColumns(props.columns);

  let order = formatSorting(columns, sorting.length === 0
    ? [ { name: props.columns[0].name, direction: 'ASC' } ] : sorting);

  let tableOpts = Object.assign({}, DataTable.defaultDataTableOpts, {
    columns,
    data,
    order,
    searching: searchable
  });

  if (height != null)
    Object.assign(tableOpts, {
      scrollY: height,
      scrollX: true,
      scrollCollapse: !childRow
    });

  let $table = $('<table class="wdk-DataTable">')
  .width(width)
  .appendTo(node);

  let dataTable = $table.DataTable(tableOpts);

  let showChildRow_ = partial(showChildRow, dataTable, childRow);
  let hideChildRow_ = partial(hideChildRow, dataTable);

  $table.on('click', 'td.wdk-DataTableCellExpand', e => {
    updateChildRows(() => {
      let tr = $(e.target).closest('tr');
      let row = dataTable.row(tr);
      if (row.child.isShown()) {
        hideChildRow_(row.node());
      }

      else {
        showChildRow_(row.node());
      }
    }, dataTable, $table);
  });

  $table.on('click', 'th.wdk-DataTableCellExpand', () => {
    updateChildRows(() => {
      let allShown = isAllChildRowsShown(dataTable);
      let update = allShown ? hideChildRow_ : showChildRow_;

      for (let tr of dataTable.rows().nodes().toArray()) {
        update(tr);
      }
    }, dataTable, $table);
  });
};

let updateChildRows = (fn, dataTable, $table) => {
  // update state of row children with provided function
  fn();

  // update css based on new table state
  let allShown = true;

  for (let tr of dataTable.rows().nodes().toArray()) {
    let row = dataTable.row(tr);
    let isShown = row.child.isShown();
    $(tr).toggleClass('wdk-DataTableRow__expanded', isShown);
    allShown = allShown && isShown;
  }

  $table.find('th.wdk-DataTableCellExpand').closest('tr')
  .toggleClass('wdk-DataTableRow__expanded', allShown);
};

let showChildRow = (dataTable, childRow, tr) => {
  let row = dataTable.row(tr);
  let div = document.createElement('div');
  row.child(div);
  renderChildRow(row, div, childRow);
  row.child.show();
};

let hideChildRow = (dataTable, tr) => {
  let row = dataTable.row(tr);
  row.child.hide();
}

let isAllChildRowsShown = (dataTable) => {
  return dataTable.rows().indexes().toArray().every(i => dataTable.row(i).child.isShown());
}

let updateToggleAllChildrenClass = (allShown) => {
  $('th.wdk-DataTableCellExpand').closest('tr')
  .toggleClass('wdk-DataTableRow__expanded', !allShown);
}

/*
let removeChildRows = () => {
  for (let childNode of this.childRowNodes.values()) {
    ReactDOM.unmountComponentAtNode(childNode);
  }
  this.childRowNodes.clear();
}
*/

let renderChildRow = (row, targetNode, childRow) => {
  if (typeof childRow === 'string') {
    targetNode.innerHTML = childRow;
  }

  else {
    let props = { rowIndex: row.index(), rowData: row.data() };
    ReactDOM.render(React.createElement(childRow, props), targetNode);
  }
}


// helpers
// -------

let formatColumns = columns => columns.map(
  column => Object.assign({
    data: column.name,
    className: 'wdk-DataTableCell wdk-DataTableCell__' + column.name,
    title: column.displayName || column.name,
    // defaultContent: '<i class="wdk-DataTableCell____n_a">N/A</i>'
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
    return [ index, sort.direction.toLowerCase() ]
  });
}


// Component Properties
// --------------------

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
    direction: PropTypes.oneOf(['ASC', 'DESC']).isRequired
  })),

  /** width of the table - if a string, treated as a CSS unit; if a number, treated as px */
  width: CSSPropType,

  /** height of the table - if a string, treated as a CSS unit; if a number, treated as px */
  height: CSSPropType,

  /** can users search the table */
  searchable: PropTypes.bool,

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
  searchable: true,
  sorting: []
};

/** Default DataTables jQuery plugin options. */
DataTable.defaultDataTableOpts = {
  info: false,
  deferRender: true,
  paging: false,
  searching: true,
  language: {
    search: '_INPUT_',
    searchPlaceholder: 'Search table'
  }
};
