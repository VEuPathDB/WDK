import React from 'react';
import PropTypes from 'prop-types';

import Templates from '../Templates';
import Icon from '../Components/Icon';
import Tooltip from '../Components/Tooltip';
import { makeClassifier } from '../Utils/Utils';
import Events from '../Utils/Events';

const headingCellClass = makeClassifier('HeadingCell');

class HeadingCell extends React.PureComponent {
  constructor (props) {
    super(props);
    this.state = {
      offset: null,
      isDragging: false,
      isDragTarget: false
    };

    this.getClassName = this.getClassName.bind(this);
    this.getDomEvents = this.getDomEvents.bind(this);

    this.updateOffset = this.updateOffset.bind(this);
    this.renderContent = this.renderContent.bind(this);
    this.renderSortTrigger = this.renderSortTrigger.bind(this);
    this.renderHelpTrigger = this.renderHelpTrigger.bind(this);
    this.componentDidMount = this.componentDidMount.bind(this);

    this.onDrop = this.onDrop.bind(this);
    this.onClick = this.onClick.bind(this);
    this.onDragEnd = this.onDragEnd.bind(this);
    this.onDragExit = this.onDragExit.bind(this);
    this.onDragOver = this.onDragOver.bind(this);
    this.onDragStart = this.onDragStart.bind(this);
    this.onDragEnter = this.onDragEnter.bind(this);
    this.onDragLeave = this.onDragLeave.bind(this);
  }

  componentDidMount () {
    this.updateOffset();
    Events.add('scroll', this.updateOffset);
    Events.add('resize', this.updateOffset);
  }

  componentWillReceiveProps (newProps) {
    if (newProps && newProps.column !== this.props.column) {
      this.updateOffset();
    }
  }

  updateOffset () {
    const { element } = this;
    if (!element) return;
    let offset = Tooltip.getOffset(element);
    this.setState({ offset });
  }

  onClick () {
    const { column, sort, eventHandlers } = this.props;
    const { onSort } = eventHandlers;
    if (typeof onSort !== 'function' || !column.sortable) return;
    const currentlySorting = sort && sort.columnKey === column.key;
    const direction = currentlySorting && sort.direction === 'asc' ? 'desc' : 'asc';
    return onSort(column, direction);
  }

  // -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

  renderContent () {
    const { column, columnIndex } = this.props;
    if ('renderHeading' in column)
      return column.renderHeading(column, columnIndex);

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
    const { key, sortable } = column ? column : {};
    const { onSort } = eventHandlers ? eventHandlers : {};
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
      <Tooltip position={position} className="Trigger HelpTrigger" content={column.helpText}>
        <Icon fa="question-circle" />
      </Tooltip>
    );
  }

  // -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

  onDragStart (event) {
    const { key } = this.props.column;
    event.dataTransfer.effectAllowed = 'copy';
    event.dataTransfer.setData('text', key);
    this.setState({ isDragging: true });
    return event;
  }

  onDragEnd (event) {
    if (this.state.isDragging || this.state.isDragTarget)
      this.setState({ isDragging: false, isDragTarget: false });
    event.preventDefault();
  }

  onDragEnter (event) {
    const dragee = event.dataTransfer.getData('text');
    if (!this.state.isDragTarget) this.setState({ isDragTarget: true });
    event.preventDefault();
  }

  onDragExit (event) {
    const dragee = event.dataTransfer.getData('text');
    this.setState({ isDragTarget: false });
    event.preventDefault();
  }

  onDragOver (event) {
    event.preventDefault();
  }

  onDragLeave (event) {
    if (this.state.isDragTarget) this.setState({ isDragTarget: false });
    event.preventDefault();
  }

  onDrop (event) {
    event.preventDefault();
    const { eventHandlers, columnIndex } = this.props;
    const { onColumnReorder } = eventHandlers;
    if (typeof onColumnReorder !== 'function') return;
    const draggedColumn = event.dataTransfer.getData('text');
    this.setState({ isDragTarget: false });
    onColumnReorder(draggedColumn, columnIndex);
  }

  getDomEvents () {
    const {
      onClick,
      onDragStart, onDragEnd,
      onDragEnter, onDragExit,
      onDragOver,
      onDrop,
      onDragLeave
    } = this;
    return {
      onClick,
      onDragStart, onDragEnd,
      onDragEnter, onDragExit,
      onDragOver,
      onDrop,
      onDragLeave
    };
  }


  getClassName () {
    const { key } = this.props.column;
    const { isDragging, isDragTarget } = this.state;
    const modifiers = ['key-' + key];
    if (isDragging) modifiers.push('Dragging');
    if (isDragTarget) modifiers.push('DragTarget');
    const className = headingCellClass(null, modifiers);
    return className;
  }

  render () {
    const { column, eventHandlers } = this.props;
    const { key, headingStyle, width, renderHeading } = column;
    const widthStyle = width ? { width, maxWidth: width, minWidth: width } : {};

    const style = Object.assign({}, headingStyle ? headingStyle : {}, widthStyle);
    const ref = element => this.element = element;

    const children = this.renderContent();
    const className = this.getClassName();
    const domEvents = this.getDomEvents();

    const draggable = column.moveable
      && !column.primary
      && typeof eventHandlers.onColumnReorder === 'function';

    const props = { style, key, ref, draggable, children, className };

    return column.hidden ? null : <th {...props} {...domEvents} />
  }
};

HeadingCell.propTypes = {
  sort: PropTypes.object,
  eventHandlers: PropTypes.object,
  column: PropTypes.object.isRequired,
  columnIndex: PropTypes.number.isRequired
};

export default HeadingCell;
