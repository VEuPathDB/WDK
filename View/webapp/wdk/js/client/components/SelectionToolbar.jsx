import React from 'react';

class SelectionToolbar extends React.Component {
  constructor (props) {
    super(props);
    this.applyToSelection = this.applyToSelection.bind(this);
  }

  applyToSelection (action) {
    let { selection } = this.props;
    let { handler, callback } = action;
    let originalSelection = selection;
    if (handler) selection.forEach(item => handler(item));
    if (callback) callback(originalSelection);
  }

  render () {
    const { actions, selection, children } = this.props;
    const className = 'selection-toolbar' + (selection.length ? '' : ' empty-selection');
    return (
      <div className={className}>
        <span className="selection-toolbar-children">
          {children}
        </span>
        <span className="selection-count">
          {selection.length ? selection.length : 'No'} Item{selection.length === 1 ? '' : 's'} Selected
        </span>
        {actions.map((action, index) => {
          return (
            <div className="toolbar-item" key={index} onClick={() => this.applyToSelection(action)}>
              {action.element}
            </div>
          );
        })}
      </div>
    );
  }
};

export default SelectionToolbar;
