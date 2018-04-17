import React from 'react';

import SelectionCounter from 'Mesa/Ui/SelectionCounter';

class ActionToolbar extends React.PureComponent {
  constructor (props) {
    super(props);
  }

  render () {
    const { state, dispatch, filteredRows } = this.props;
    const { actions } = state;

    let list = actions
      .filter(action => action.element)
      .map(action => (
        <div key={action.__id} className="ActionToolbar-Item">
          {action.element}
        </div>
      ));

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
