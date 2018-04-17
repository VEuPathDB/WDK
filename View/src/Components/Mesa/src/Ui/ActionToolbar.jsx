import React from 'react';
import PropTypes from 'prop-types';

import SelectionCounter from '../Ui/SelectionCounter';

class ActionToolbar extends React.PureComponent {
  constructor (props) {
    super(props);
    this.dispatchAction = this.dispatchAction.bind(this);
    this.renderActionItem = this.renderActionItem.bind(this);
    this.renderActionItemList = this.renderActionItemList.bind(this);
  }

  getSelection () {
    const { rows, options } = this.props;
    const { isRowSelected } = options;

    if (typeof isRowSelected !== 'function') return [];
    return rows.filter(isRowSelected);
  }

  dispatchAction (action) {
    const { handler, callback } = action;
    const { rows, columns } = this.props;
    const selection = this.getSelection();

    if (action.selectionRequired && !selection.length) return;
    if (typeof handler === 'function') selection.forEach(row => handler(row, columns));
    if (typeof callback === 'function') return callback(selection, columns, rows);
  }

  renderActionItem ({ action }) {
    let { element } = action;
    let selection = this.getSelection();
    let className = 'ActionToolbar-Item' + (action.selectionRequired && !selection.length ? ' disabled' : '');

    if (typeof element !== 'string' && !React.isValidElement(element)) {
      if (typeof element === 'function') element = element(selection);
    }

    let handler = () => this.dispatchAction(action);
    return (
      <div key={action.__id} className={className} onClick={handler}>
        {element}
      </div>
    );
  }

  renderActionItemList ({ actions }) {
    const ActionItem = this.renderActionItem;
    return actions
      .filter(action => action.element)
      .map(action => <ActionItem action={action} />);
  }

  render () {
    const { rows, actions, eventHandlers } = this.props;
    const { onRowSelect, onRowDeselect } = eventHandlers ? eventHandlers : {};

    const ActionList = this.renderActionItemList;
    const selection = this.getSelection();

    return (
       <div className="Toolbar ActionToolbar">
         <div className="ActionToolbar-Info">
           <SelectionCounter
             rows={rows}
             selection={selection}
             onRowSelect={onRowSelect}
             onRowDeselect={onRowDeselect}
           />
         </div>
         <ActionList />
       </div>
    );
  }
};

ActionToolbar.propTypes = {
  rows: PropTypes.array,
  actions: PropTypes.array,
  eventHandlers: PropTypes.array
};

export default ActionToolbar;
