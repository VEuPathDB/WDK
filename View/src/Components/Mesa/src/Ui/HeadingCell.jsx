import React from 'react';

import Templates from '../Templates';
import Icon from '../Components/Icon';
import Tooltip from '../Components/Tooltip';
// import ColumnSorter from '../Ui/ColumnSorter';
// import ColumnFilter from '../Ui/ColumnFilter';

class HeadingCell extends React.PureComponent {
  constructor (props) {
    super(props);
    this.renderContent = this.renderContent.bind(this);
    this.handleSortClick = this.handleSortClick.bind(this);
    this.renderSortTrigger = this.renderSortTrigger.bind(this);
    this.renderHelpTrigger = this.renderHelpTrigger.bind(this);
  }

  renderContent () {
    const { column, columnIndex } = this.props;
    if ('renderHeading' in column) return column.renderHeading(column, columnIndex);
    return Templates.heading(column, columnIndex);
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
    const { columnKey, direction } = sort ? sort : {};
    const { onSort } = eventHandlers ? eventHandlers : {};
    const { key, sortable } = column ? column : {};
    const isActive = columnKey === key;

    if (!sortable || (typeof onSort !== 'function' && !isActive)) return null;

    const sortIcon = !isActive
      ? 'sort inactive'
      : direction === 'asc'
        ? 'sort-amount-asc active'
        : 'sort-amount-desc active';

    return (<Icon fa={sortIcon + ' Trigger SortTrigger'} />);
  }

  renderHelpTrigger () {
    const { column } = this.props;
    if (!column.helpText) return null;
    return (
      <Tooltip className="Trigger HelpTrigger" text={column.helpText}>
        <Icon fa="question-circle" />
        <span ref={ref => this.triggerSpot = ref}>yo</span>
      </Tooltip>
    )
  }

  render () {
    const { column, state, dispatch } = this.props;
    const { headingStyle, width } = column;

    const widthObj = width ? { width, maxWidth: width, minWidth: width } : {};

    const style = Object.assign({}, headingStyle ? headingStyle : {}, widthObj);

    const Content = this.renderContent;
    const SortTrigger = this.renderSortTrigger;
    const HelpTrigger = this.renderHelpTrigger;
    const ClickBoundary = this.renderClickBoundary;

    return column.hidden ? null : (
      <th
        key={column.key}
        style={style}
        ref={el => this.element = el}
        onClick={e => column.sortable ? this.handleSortClick() : null}
      >
        <SortTrigger />
        <Content />
        <ClickBoundary>
          <HelpTrigger />
        </ClickBoundary>
      </th>
    );
  }
};

export default HeadingCell;
