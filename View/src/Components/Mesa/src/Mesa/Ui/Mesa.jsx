import React from 'react';

import 'Mesa/Style/Mesa';
import Importer from 'Mesa/Utils/Importer';
import StoreFactory from 'Mesa/State/StoreFactory';
import TableController from 'Mesa/Ui/TableController';
import {
  updateOptions,
  updateColumns,
  updateRows,
  updateActions,
  resetUiState
} from 'Mesa/State/Actions';

class Mesa extends React.Component {
  constructor (props) {
    super(props);
    this.state = {};
    this.componentDidMount = this.componentDidMount.bind(this);
    this.componentWillMount = this.componentWillMount.bind(this);
    this.componentWillUnmount = this.componentWillUnmount.bind(this);
  }

  componentWillMount () {
    let { options, columns, rows, actions } = this.props;

    rows = Importer.importRows(rows);
    options = Importer.importOptions(options);
    columns = Importer.importColumns(columns, rows, options);
    actions = Importer.importActions(actions, options);
    this.store = StoreFactory.create({ options, columns, rows, actions });
    this.setState(this.store.getState());
  }

  componentWillReceiveProps (newProps) {
    const { dispatch } = this.store;
    let { options, columns, rows, actions } = this.props;

    if (newProps.rows !== rows) {
      rows = Importer.importRows(newProps.rows)
      dispatch(updateRows([...rows]));
    };
    if (newProps.options !== options) {
      options = Importer.importOptions(newProps.options);
      dispatch(updateOptions(Object.assign({}, options)));
    };
    if (newProps.columns !== columns) {
      columns = Importer.importColumns(newProps.columns, rows, options);
      dispatch(updateColumns([...columns]));
      dispatch(resetUiState());
    };
    if (newProps.actions !== actions) {
      actions = Importer.importActions(newProps.actions, options);
      dispatch(updateActions([...actions]));
    };
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
