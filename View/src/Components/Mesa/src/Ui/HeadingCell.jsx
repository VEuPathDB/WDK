import React from 'react';

import Templates from '../Templates';
import Icon from '../Components/Icon';
import Tooltip from '../Components/Tooltip';
import { makeClassifier } from '../Utils/Utils';

const headingCellClass = makeClassifier('HeadingCell');

class HeadingCell extends React.PureComponent {
  constructor (props) {
    super(props);
    this.state = { offset: null };
    this.renderContent = this.renderContent.bind(this);
    this.handleSortClick = this.handleSortClick.bind(this);
    this.renderSortTrigger = this.renderSortTrigger.bind(this);
    this.renderHelpTrigger = this.renderHelpTrigger.bind(this);
    this.componentDidMount = this.componentDidMount.bind(this);
  }

  componentDidMount () {
    const { element } = this;
    if (!element) return;
    let offset = Tooltip.getOffset(element);
    this.setState({ offset });
  }

  renderContent () {
    const { column, columnIndex } = this.props;

    if ('renderHeading' in column) return column.renderHeading(column, columnIndex);

    const SortTrigger = this.renderSortTrigger;
    const HelpTrigger = this.renderHelpTrigger;
    const ClickBoundary = this.renderClickBoundary;

    return (
      <div className={headingCellClass('Content')}>
        <div className={headingCellClass(['Content', 'Aside'])}>
          <SortTrigger />
        </div>
        <div className={headingCellClass(['Content', 'Label'])}>
          {Templates.heading(column, columnIndex)}
        </div>
        <div className={headingCellClass(['Content', 'Aside'])}>
          <ClickBoundary>
            <HelpTrigger />
          </ClickBoundary>
        </div>
      </div>
    );
  }

  handleSortClick () {
    const { column, sort, eventHandlers } = this.props;
    const { onSort } = eventHandlers;
    if (typeof onSort !== 'function' || !column.sortable) return;
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
    const { offset } = this.state;
    const { top, left, height } = offset ? offset : {};
    const position = { top: top + height, left };

    if (!column.helpText) return null;
    return (
      <Tooltip position={position} className="Trigger HelpTrigger" text={column.helpText}>
        <Icon fa="question-circle" />
      </Tooltip>
    )
  }

  render () {
    const { column, state, dispatch } = this.props;
    const { headingStyle, width, renderHeading } = column;
    const widthObj = width ? { width, maxWidth: width, minWidth: width } : {};
    const style = Object.assign({}, headingStyle ? headingStyle : {}, widthObj);

    const Content = this.renderContent;

    return column.hidden ? null : (
      <th
        style={style}
        key={column.key}
        ref={(element) => this.element = element}
        className={headingCellClass() + ' ' + headingCellClass('key-' + column.key)}
        onClick={this.handleSortClick}
      >
        <Content />
      </th>
    );
  }
};

export default HeadingCell;
