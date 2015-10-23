import React from 'react';
import DataTable from './DataTable.jquery';
import { wrappable } from '../utils/componentUtils';

let RecordTable = React.createClass({

  mixins: [ React.addons.PureRenderMixin ],

  render() {
    let { table, tableMeta } = this.props;
    if (table.length === 0 || tableMeta.attributes.length === 0) {
      return null;
    }
    return <DataTable columns={this.props.tableMeta.attributes} data={this.props.table} />;
  }

});

export default wrappable(RecordTable);
