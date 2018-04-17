import React from 'react';

import MesaController from '../Ui/MesaController';
import MesaState from '../Utils/MesaState';

class Mesa extends React.Component {
  constructor (props) {
    super(props);
  }

  render () {
    const { state } = this.props;
    const { rows, filteredRows, columns, options, actions, uiState, eventHandlers } = state;
    return <MesaController {...state} />
  }
};

export default Mesa;
