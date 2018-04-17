import React from 'react';

import 'Mesa/Style/Mesa';
import Importer from 'Mesa/Utils/Importer';
import StoreFactory from 'Mesa/State/StoreFactory';
import TableController from 'Mesa/Ui/TableController';
import { updateOptions, updateColumns, updateRows } from 'Mesa/State/Actions';

class Mesa extends React.Component {
  constructor (props) {
    super(props);
    let { options, columns, rows } = this.props;

    options = Importer.importOptions(options);
    columns = Importer.importColumns(columns, rows)

    this.store = StoreFactory.create({ options, columns, rows });
    this.state = this.store.getState();
    this.componentDidMount = this.componentDidMount.bind(this);
    this.componentWillUnmount = this.componentWillUnmount.bind(this);
  }

  componentWillReceiveProps (newProps) {
    if (newProps.options !== this.props.options) {
      let options = Importer.importOptions(newProps.options);
      this.store.dispatch(updateOptions(options));
    }
    if (newProps.columns !== this.props.columns) {
      let columns = Importer.importColumns(newProps.columns, this.props.rows);
      this.store.dispatch(updateColumns(columns));
    }
    if (newProps.rows !== this.props.rows) {
      this.store.dispatch(updateRows(newProps.rows));
    }
  }

  componentDidMount () {
    this.unsubscribe = this.store.subscribe(() => {
      this.setState(this.store.getState());
    });
  }

  componentWillUnmount () {
    this.unsubscribe();
  }

  render () {
    let { dispatch } = this.store;
    let state = this.state;

    return (
      <div className="Mesa">
        <TableController
          state={state}
          dispatch={dispatch}
        />
      </div>
    );
  }
};

export default Mesa;
