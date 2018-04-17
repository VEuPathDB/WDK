import React from 'react';

import MesaController from '../Ui/MesaController';

class Mesa extends React.Component {
  constructor (props) {
    super(props);
  }

  render (props) {
    return <MesaController {...props} />
  }
};

export default Mesa;
