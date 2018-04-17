import React from 'react';

import {
  setColumnBlackList,
  toggleColumnFilter,
  toggleColumnFilterValue,
  toggleColumnFilterVisibility
} from 'Mesa/State/Actions';

import Events from 'Mesa/Utils/Events';
import Icon from 'Mesa/Components/Icon';
import Toggle from 'Mesa/Components/Toggle';
import Utils from 'Mesa/Utils/Utils';

class ColumnFilter extends React.PureComponent {
  constructor (props) {
    super(props);
    this.openMenu = this.openMenu.bind(this);
    this.clearAll = this.clearAll.bind(this);
    this.selectAll = this.selectAll.bind(this);
    this.renderMenu = this.renderMenu.bind(this);
    this.toggleFilter = this.toggleFilter.bind(this);
    this.renderTrigger = this.renderTrigger.bind(this);
    this.renderMenuItem = this.renderMenuItem.bind(this);
    this.componentDidMount = this.componentDidMount.bind(this);
    this.handleTriggerClick = this.handleTriggerClick.bind(this);
    this.updateFilterableValues = this.updateFilterableValues.bind(this);
    this.componentWillReceiveProps = this.componentWillReceiveProps.bind(this);
    this.state = { filterableValues: [], touched: false };
  }

  openMenu () {
    const { column, dispatch } = this.props;
    const { filterState } = column;
    if (filterState.visible) return;
    let { touched } = this.state;

    dispatch(toggleColumnFilterVisibility(column));

    if (!touched && !filterState.enabled) {
      dispatch(toggleColumnFilter(column));
      this.setState({ touched: true });
    }

    this.filterCloseListener = Events.add('click', (e) => {
      let within = e.path.includes(this.refs.menu);
      if (!within) this.closeMenu();
    });
  }

  closeMenu () {
    const { column, dispatch } = this.props;
    if (!column.filterState.visible || !this.filterCloseListener) return;

    dispatch(toggleColumnFilterVisibility(column));
    Events.remove(this.filterCloseListener);
  }

  componentWillReceiveProps (newProps) {
    let { column, rows } = newProps;
    if (column === this.props.column && rows === this.props.rows) return;
    this.updateFilterableValues();
  }

  componentDidMount () {
    this.updateFilterableValues();
  }

  updateFilterableValues () {
    let { column, state } = this.props;
    let filterableValues = Array.from(new Set(state.rows.map(row => row[column.key])));
    this.setState({ filterableValues });
  }

  handleTriggerClick () {
    let { column, dispatch } = this.props;
    if (!column.filterable) return;
    let { filterState } = column;
    return filterState.visible ? this.closeMenu() : this.openMenu();
  }

  renderTrigger () {
    const { column, state } = this.props;
    const { filterState } = column;
    const icon = 'filter ' + (filterState.enabled ? 'active' : 'inactive');
    const trigger = !column.filterable
      ? null
      : (
        <Icon
          fa={icon + ' Trigger FilterTrigger'}
          onClick={this.handleTriggerClick}
        />
      );
    return trigger;
  }

  toggleValue (value) {
    const { column, dispatch } = this.props;
    dispatch(toggleColumnFilterValue(column, value))
  }

  toggleFilter () {
    const { column, dispatch } = this.props;
    dispatch(toggleColumnFilter(column));
  }

  selectAll () {
    const { dispatch, column } = this.props;
    const { filterableValues } = this.state;
    dispatch(setColumnBlackList(column, []));
  }

  clearAll () {
    const { dispatch, column } = this.props;
    const { filterableValues } = this.state;
    dispatch(setColumnBlackList(column, [...filterableValues]));
  }

  renderMenuItem (value) {
    const { blacklist, enabled } = this.props.column.filterState;
    const checkbox = blacklist.includes(value) ? 'square' : 'check-square';
    return (
      <div key={value} onClick={() => this.toggleValue(value)}>
        <Icon fa={checkbox} className={enabled ? '' : 'disabled'} /> {value}
      </div>
    );
  }

  renderMenu () {
    const { state, column } = this.props;
    const { filterable, filterState } = column;
    const { filterableValues } = this.state;
    const { blacklist, enabled, visible } = filterState;
    const items = filterableValues.map(this.renderMenuItem);

    const menu = (!filterable || !visible)
      ? null
      : (
        <div className="FilterMenu" ref="menu">
          <big>
            <b>{column.name}</b> filter
            <span className="faded"> ({enabled ? 'on' : 'off'})</span>
            <Toggle
              style={{ float: 'right' }}
              onChange={this.toggleFilter}
              enabled={enabled}
            />
          </big>
          <div>
            <a onClick={this.selectAll}>Select All</a>
            {' | '}
            <a onClick={this.clearAll}>Clear All</a>
          </div>
          <div className="FilterMenu-CheckList">
            {items}
          </div>
        </div>
      );
    return menu;
  }

  render () {
    const trigger = this.renderTrigger();
    const menu = this.renderMenu();

    return (
      <div className="ColumnFilter-Wrapper">
        {trigger}
        {menu}
      </div>
    )
  }
};

export default ColumnFilter;
