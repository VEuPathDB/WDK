import React from 'react';
import ReactRouter from 'react-router';
import wrappable from '../utils/wrappable';
import {
  formatAttributeValue
} from '../utils/stringUtils';

let { Link, Navigation } = ReactRouter;
let primaryKeyName = 'primary_key';

let AnswerTableCell = React.createClass({

  propTypes: {
    // TODO Put reusable propTypes in a module
    attribute: React.PropTypes.object,
    record: React.PropTypes.object.isRequired,
    recordClass: React.PropTypes.string.isRequired,
    width: React.PropTypes.number.isRequired
  },

  mixins: [ Navigation ],

  render() {
    if (this.props.attribute.value == null) {
      return null;
    }

    let { attribute, record, recordClass, width } = this.props;
    let type = attribute.type;

    if (attribute.name === primaryKeyName) {
      let href = this.makeHref('record', { class: recordClass }, record.id);
      return (
        <Link
          {...this.props}
          className="wdk-AnswerTable-recordLink"
          to={href}
          dangerouslySetInnerHTML={{__html: formatAttributeValue(attribute.value, type) }}
        />
      );
    }
    else {
      return (
        <div
          {...this.props}
          dangerouslySetInnerHTML={{__html: formatAttributeValue(attribute.value, type) }}
        />
      );
    }
  }

});

export default wrappable(AnswerTableCell);
