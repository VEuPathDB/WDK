import React from 'react';

import SelectionCounter from 'Mesa/Ui/SelectionCounter';

class ActionToolbar extends React.PureComponent {
  constructor (props) {
    super(props);
    this.dispatchAction = this.dispatchAction.bind(this);
    this.renderActionItem = this.renderActionItem.bind(this);
  }

  getSelectedRows () {
    const { state } = this.props;
    const { rows, ui } = state;
    const { selection } = ui;
    return selection
      .map(id => rows.find(row => row.__id === id))
      .filter(row => row);
  }

  dispatchAction (action) {
    const { handler, callback } = action;
    const { columns, rows } = this.props.state;
    const selectedRows = this.getSelectedRows();

    if (typeof handler === 'function') selectedRows.forEach(row => handler(row, columns));
    if (typeof callback === 'function') callback(selectedRows, columns, rows);
  }

  renderActionItem (action) {
    let { element } = action;
    let selectedRows = this.getSelectedRows();
    let className = 'ActionToolbar-Item' + (action.selectionRequired && !selectedRows.length ? ' disabled' : '');

    if (typeof element !== 'string' && !React.isValidElement(element)) {
      if (typeof element === 'function') element = element(selectedRows);
    }
    const handler = () => this.dispatchAction(action);
    return (
      <div key={action.__id} className={className} onClick={handler}>
        {element}
      </div>
    );
  }

  render () {
    const { state, dispatch, filteredRows } = this.props;
    const { actions } = state;

    let list = actions
      .filter(action => action.element)
      .map(this.renderActionItem);

    return (
       <div className="Toolbar ActionToolbar">
         <div className="ActionToolbar-Info">
           <SelectionCounter
             state={state}
             dispatch={dispatch}
             filteredRows={filteredRows}
           />
         </div>
         {list}
       </div>
    );
  }
};

export default ActionToolbar;
