import React from 'react';

import { Seq } from 'Utils/IterableUtils';

export default class MultiField extends React.Component {
  static getHelpContent(props) {
    return (
      <div>Select multiple criteria for {props.field.displayName}</div>
    );
  }

  render() {
    return (
      <div>
        {Seq.from(this.props.fields.values())
          .filter(field => field.parent === this.props.activeField)
          .map(field => <div>{field.display}</div>)}
      </div>
    );
  }
}
