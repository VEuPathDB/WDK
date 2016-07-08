import $ from 'jquery';
import {createElement, PropTypes} from 'react';
import {render, unmountComponentAtNode} from 'react-dom';
import {formatAttributeValue, wrappable, PureComponent} from '../utils/componentUtils';
import RealTimeSearchBox from './RealTimeSearchBox';

const expandButton = '<button type="button" class="wdk-DataTableCellExpand"></button>';
const expandColumn = {
  className: 'wdk-DataTableCell wdk-DataTableCell__childRowToggle',
  orderable: false,
  title: expandButton,
  defaultContent: expandButton
};

/**
 * Sortable table for WDK-formatted data.
 *
 * This one used DataTables jQuery plugin
 */
class DataTable extends PureComponent {

  componentDidMount() {
    this._childRowContainers = new Map();
    this._setup();
  }

  componentDidUpdate() {
    this._destroy();
    this._setup();
  }

  componentWillUnmount() {
    this._destroy();
  }

  /** Initialize datatable plugin and set up handlers for creating child rows */
  _setup() {
    let {
      childRow,
      data,
      sorting,
      searchable,
      height,
      width
    } = this.props;

    let columns = childRow
      ? [ expandColumn, ...formatColumns(this.props.columns) ]
      : formatColumns(this.props.columns);

    let order = formatSorting(columns, sorting.length === 0
      ? [ { name: this.props.columns[0].name, direction: 'ASC' } ] : sorting);

    let tableOpts = Object.assign({}, DataTable.defaultDataTableOpts, {
      columns,
      data,
      order,
      searching: searchable,
      headerCallback: (thead) => {
        let i = 0;
        let $ths = $(thead).find('th');
        if (childRow) {
          $ths.eq(i++).attr('title', 'Show or hide all row details');
        }
        for (let column of this.props.columns) {
          if (column.help != null) $ths.eq(i++).attr('title', column.help);
        }
      }
    });

    if (height != null)
      Object.assign(tableOpts, {
        scrollY: height,
        scrollX: true,
        scrollCollapse: !childRow
      });

    this._$table = $('<table class="wdk-DataTable">')
    .width(width)
    .appendTo(this.node);

    this._dataTable = this._$table.DataTable(tableOpts);

    // click handler for expand single row
    this._$table.on('click', 'td .wdk-DataTableCellExpand', e => {
      let tr = $(e.target).closest('tr');
      let row = this._dataTable.row(tr);
      if (row.child.isShown()) {
        this._hideChildRow(row.node());
      }
      else {
        this._showChildRow(row.node());
      }
      this._updateChildRowClassNames();
    });

    // click handler for expand all rows
    this._$table.on('click', 'th .wdk-DataTableCellExpand', () => {
      // if all are shown, then hide all, otherwise show any that are hidden
      let allShown = areAllChildRowsShown(this._dataTable);
      let update = allShown ? this._hideChildRow : this._showChildRow;
      for (let tr of this._dataTable.rows().nodes().toArray()) {
        update.call(this, tr);
      }
      this._updateChildRowClassNames();
    });
  }

  /** Update class names of child row expand buttons based on datatable state */
  _updateChildRowClassNames() {
    let allShown = true;
    for (let tr of this._dataTable.rows().nodes().toArray()) {
      let row = this._dataTable.row(tr);
      let isShown = row.child.isShown();
      $(tr).toggleClass('wdk-DataTableRow__expanded', isShown);
      allShown = allShown && isShown;
    }
    this._$table
      .find('th .wdk-DataTableCellExpand')
      .closest('tr')
      .toggleClass('wdk-DataTableRow__expanded', allShown);
  }

  /** Append child row container node to table row and show it */
  _showChildRow(tableRowNode) {
    let { childRow } = this.props;
    let row = this._dataTable.row(tableRowNode);
    let childRowContainer = this._getChildRowContainer(tableRowNode);
    row.child(childRowContainer);
    if (typeof childRow === 'string') {
      childRowContainer.innerHTML = childRow;
    }
    else {
      let props = { rowIndex: row.index(), rowData: row.data() };
      render(createElement(childRow, props), childRowContainer);
    }
    row.child.show();
  }

  /** Hide child row */
  _hideChildRow(tableRowNode) {
    let row = this._dataTable.row(tableRowNode);
    row.child.hide();
  }

  /** Get child row container from cache, or create and add to cache first */
  _getChildRowContainer(tableRowNode) {
    if (!this._childRowContainers.has(tableRowNode)) {
      this._childRowContainers.set(tableRowNode, document.createElement('div'));
    }
    return this._childRowContainers.get(tableRowNode);
  }

  /** Unmount all child row components and destroy the datatable instance */
  _destroy() {
    this._dataTable.destroy(true);
    for (let container of this._childRowContainers.values()) {
      unmountComponentAtNode(container);
    }
    this._childRowContainers.clear();
  }

  render() {
    return (
      <div>
        {this.props.searchable && (
          <RealTimeSearchBox
            className="wdk-DataTableSearchBox"
            placeholderText="Search this table..."
            onSearchTermChange={term => this._dataTable.search(term).draw()}
            delayMs={0}
          />
        )}
        <div ref={node => this.node = node} className="wdk-DataTableContainer"/>
      </div>
    );
  }

}

export default wrappable(DataTable);

/** helper to determine if all child rows are visible */
function areAllChildRowsShown(dataTable) {
  return dataTable.rows().indexes().toArray().every(i => dataTable.row(i).child.isShown());
}

// helpers
// -------

/** Map WDK table attribute fields to datatable data format */
function formatColumns(columns) {
  return columns.map(
    column => ({
      data: column.name,
      className: 'wdk-DataTableCell wdk-DataTableCell__' + column.name,
      title: column.displayName || column.name,
      type: column.sortType,
      visible: column.isDisplayable,
      searchable: column.isDisplayable,
      orderable: column.isSortable,
      render(data, type) {
        let value = formatAttributeValue(data);
        if (type === 'display' && value != null) {
          return '<div class="wdk-DataTableCellContent">' + value + '</div>'
        }
        return value;
      }
    })
  );
}

/** Map WDK table sorting to datatable data format */
function formatSorting(columns, sorting = []) {
  return sorting.length === 0 ? [ [0, 'asc'] ] : sorting.map(sort => {
    let index = columns.findIndex(column => column.data === sort.name);
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
  dom: 'lript',
  autoWidth: false,
  deferRender: true,
  paging: false,
  searching: true,
  language: {
    info: 'Showing _TOTAL_ ',
    infoFiltered: 'of _MAX_ ',
    infoEmpty: 'Showing 0 ',
    infoPostFix: 'rows'
  }
};
