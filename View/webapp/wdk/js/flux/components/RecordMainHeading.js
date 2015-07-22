import React from 'react';
import wrappable from '../utils/wrappable';

let RecordMainHeading = React.createClass({

  mixins: [ React.addons.PureRenderMixin ],

  render() {
    let { record, recordClass } = this.props;
    return (
      <h1 className="wdk-RecordMainHeading">{recordClass.displayName} {record.attributes.primary_key}</h1>
    );
  }

});

export default wrappable(RecordMainHeading);
