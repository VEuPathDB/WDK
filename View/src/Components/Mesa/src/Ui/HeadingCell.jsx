import React from 'react';

import Templates from '../Templates';
import ColumnSorter from '../Ui/ColumnSorter';
import ColumnFilter from '../Ui/ColumnFilter';
import { toggleSortOrder, sortByColumn } from '../State/Actions';

class HeadingCell extends React.PureComponent {
  constructor (props) {
    super(props);
  }

  renderContent () {
    const { column } = this.props;
    if ('renderHeading' in column) return column.renderHeading(column);
    return Templates.heading(column);
  }

  handleSortClick () {
    const { column, sort, eventHandlers } = this.props;
    const { onSort } = eventHandlers;
    if (typeof onSort !== 'function') return;
    const currentlySorting = sort.columnKey === column.key;
    const direction = currentlySorting && sort.direction === 'asc' ? 'desc' : 'asc';
    return onSort(column, direction);
  }

  renderClickBoundary ({ children }) {
    const style = { display: 'inline-block' };
    return (
      <div onClick={(e) => e.stopPropagation()} style={style}>
        {children}
      </div>
    );
  }

  renderSortTrigger () {
    const { column, sort, eventHandlers } = this.props;
    const { columnKey, direction } = sort;
    const { onSort } = eventHandlers;
    const { sortable } = column;
    const isActive = columnKey === column.key;

    if (!sortable || (typeof onSort !== 'function' && !isActive)) return null;

    const sortIcon = !active
      ? 'sort-amount-asc inactive'
      : direction === 'asc'
        ? 'sort-amount-asc active'
        : 'sort-amount-desc active';

    return (<Icon fa={sortIcon + ' Trigger SortTrigger'} />);
  }

  render () {
    const { column, state, dispatch } = this.props;
    const { headingStyle } = column;

    const Content = this.renderContent;
    const ClickBoundary = this.renderClickBoundary;
    const SortTrigger = this.renderSortTrigger;

    return column.hidden ? null : (
      <th
        key={column.key}
        style={headingStyle}
        ref={el => this.element = el}
        onClick={e => column.sortable ? this.handleSortClick() : null}
      >
        <SortTrigger />
        <Content />
        {/* {column.filterable && (
          <ClickBoundary>
            <ColumnFilter
              column={column}
              state={state}
              dispatch={dispatch}
            />
          </ClickBoundary>
        )} */}
      </th>
    )
  }
};

export default HeadingCell;
