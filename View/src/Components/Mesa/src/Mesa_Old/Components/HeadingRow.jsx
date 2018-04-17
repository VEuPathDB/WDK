import React from 'react';

import Events from 'Mesa/Utils/Events';
import Store from 'Mesa/State/Store';
import Icon from 'Mesa/Components/Icon';
import Utils from 'Mesa/Utils/Utils';
import Templates from 'Mesa/Templates';
import {
  toggleSortOrder,
  sortByColumn,
  setColumnWidth,
  filterByColumnValues,
  toggleColumnFilterValue
} from 'Mesa/State/Actions';

class HeadingRow extends React.Component {
  constructor (props) {
    super(props);
    this.state = {
      showColumnFilter: null
    };
    this.getRenderer = this.getRenderer.bind(this);
    this.renderHeadingCell = this.renderHeadingCell.bind(this);
    this.handleSortClick = this.handleSortClick.bind(this);
    this.handleFilterClick = this.handleFilterClick.bind(this);
    this.renderFilterMenu = this.renderFilterMenu.bind(this);
    this.openFilterMenu = this.openFilterMenu.bind(this);
    this.closeFilterMenu = this.closeFilterMenu.bind(this);
  }

  toggleColumnFilterValue (value) {
    Store.dispatch(toggleColumnFilterValue(value));
  }

  openFilterMenu (column) {
    this.setState({ showColumnFilter: column });
    this.filterCloseListener = Events.add('click', (e) => {
      let within = e.path.includes(this.refs[column.key]);
      if (!within) this.closeFilterMenu();
    });
  }

  closeFilterMenu () {
    Events.remove(this.filterCloseListener);
    this.setState({ showColumnFilter: null });
  }

  handleFilterClick ({ column }) {
    let { showColumnFilter } = this.state;
    if (showColumnFilter !== column) this.openFilterMenu(column);
    else return this.closeFilterMenu();

    let { byColumn } = Store.getState().filter;
    if (byColumn === column) return;

    let { rows } = this.props;
    let values = Array.from(new Set(rows.map(row => row[column.key])));
    Store.dispatch(filterByColumnValues(column, values));
  }

  renderFilterTrigger (column) {
    let { filter } = Store.getState();
    let isFilterActive = filter.byColumn === column;
    let icon = isFilterActive
      ? 'filter'
      : 'filter inactive';
    let filterTrigger = !column.filterable ? null : (
      <Icon
        fa={icon + ' Trigger FilterTrigger'}
        onClick={() => this.handleFilterClick({ column })}
      />
    );
    return filterTrigger;
  }

  renderFilterMenu (column) {
    let { showColumnFilter } = this.state;
    let { rows } = this.props;
    let { valueWhitelist } = Store.getState().filter;
    let possibleValues = Array.from(new Set(rows.map(row => row[column.key])));
    let filterMenu = showColumnFilter !== column ? null : (
      <div className="FilterMenu">
        Filter by <b>{column.name || column.key}</b>:
        <div className="FilterMenu-CheckList">
          {possibleValues.map(value => {
            let valueIsChecked = valueWhitelist.includes(value);
            return (
              <div key={value} onClick={() => this.toggleColumnFilterValue(value)}>
                <Icon fa={valueIsChecked ? 'check-square' : 'square' } />
                {value}
              </div>
            );
          })}
        </div>
      </div>
    );
    return filterMenu;
  }

  handleResizeStart (e, column) {
    this.resizeOrigin = Utils.getRealOffset(this.refs[column.key]).left;
  }

  handleResize (e, column) {
    let draggedTo = e.pageX;
    let width = draggedTo - this.resizeOrigin;
    Store.dispatch(setColumnWidth(column, width))
  }

  handleResizeEnd (e, column) {
    this.resizeOrigin = null;
  }

  renderResizeBar (column) {
    let { resizeable } = column;
    if (!resizeable) return null;
    return (
      <div
        className="ResizeBar"
        draggable={true}
        onDragStart={(e) => this.handleResizeStart(e, column)}
        onDrag={(e) => this.handleResize(e, column)}
        onDragEnd={(e) => this.handleResizeEnd(e, column)}
      > </div>
    )
  }

  renderHeadingCell (column) {
    let { showColumnFilter } = this.state;
    let content = this.getRenderer(column);
    let sortTrigger = this.renderSortTrigger(column);
    let filterTrigger = this.renderFilterTrigger(column);
    let filterMenu = this.renderFilterMenu(column);
    let resizeBar = this.renderResizeBar(column);
    let isHidden = Store.getState().hiddenColumns.includes(column);

    return isHidden ? null : (
      <th
        key={column.key}
        ref={column.key}
        onClick={() => sortTrigger ? this.handleSortClick({ column }) : null}
      >
        {sortTrigger}
        {content}
        {filterTrigger}
        {filterMenu}
        {resizeBar}
      </th>
    );
  }
};

export default HeadingRow;
