import React from 'react';

import 'Mesa/Style/Mesa';
import Importer from 'Mesa/Utils/Importer';
import StoreFactory from 'Mesa/State/StoreFactory';
import TableController from 'Mesa/Ui/TableController';
import { updateOptions, updateColumns, updateRows } from 'Mesa/State/Actions';

class Mesa extends React.Component {
  constructor (props) {
    super(props);
    this.state = {};
    this.componentDidMount = this.componentDidMount.bind(this);
    this.componentWillMount = this.componentWillMount.bind(this);
    this.componentWillUnmount = this.componentWillUnmount.bind(this);
  }

  componentWillMount () {
    let { options, columns, rows } = this.props;

    rows = Importer.importRows(rows);
    options = Importer.importOptions(options);
    columns = Importer.importColumns(columns, rows, options);

    this.store = StoreFactory.create({ options, columns, rows });
    this.setState(this.store.getState());
  }

  componentWillReceiveProps (newProps) {
    const { dispatch } = this.store;

    if (newProps.options !== this.props.options) {
      let options = Importer.importOptions(newProps.options);
      dispatch(updateOptions(Object.assign({}, options)));
    }
    if (newProps.columns !== this.props.columns) {
      let columns = Importer.importColumns(newProps.columns, newProps.rows, newProps.options);
      dispatch(updateColumns([...columns]));
    }
    if (newProps.rows !== this.props.rows) {
      let rows = Importer.importRows(newProps.rows)
      dispatch(updateRows([...rows]));
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
    const state = this.state;
    const { children } = this.props;
    const { dispatch } = this.store;

    return (
      <div className="Mesa">
        <TableController
          state={state}
          dispatch={dispatch}
        >
          {children}
        </TableController>
      </div>
    );
  }
};

export default Mesa;
