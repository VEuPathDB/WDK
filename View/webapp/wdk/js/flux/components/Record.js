import React from 'react';

/**
 * Record detail component
 */

const Record = React.createClass({
  propTypes: {
    record: React.PropTypes.object.isRequired
  },

  render() {
    const { record, attributes } = this.props;
    const recordAttributes = _.indexBy(record.attributes, 'name');
    return (
      <div className="wdk-Record">
        <h3 dangerouslySetInnerHTML={{__html: recordAttributes.primary_key.value}}/>
        <div className="wdk-Record-attributes">
          {_.reject(attributes, { name: 'primary_key' }).map(attribute => {
            const { name, displayName } = attribute;
            const attrValue = recordAttributes[name].value;

            if (typeof attrValue === 'undefined') return null;

            return (
              <div>
                <h4>{displayName}</h4>
                <div dangerouslySetInnerHTML={{__html: attrValue}}/>
              </div>
            );
          })}
        </div>
      </div>
    );
  }
});

export default Record;
