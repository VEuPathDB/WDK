import React from 'react';

import Utils from 'Mesa/Utils/Utils';
import Icon from 'Mesa/Components/Icon';

class TruncatedText extends React.Component {
  constructor (props) {
    super(props);
    this.state = { expanded: false };
    this.toggleExpansion = this.toggleExpansion.bind(this);
  }

  toggleExpansion () {
    let { expanded } = this.state;
    this.setState({ expanded: !expanded });
  }

  render () {
    let { expanded } = this.state;
    let { className, cutoff, text } = this.props;
    cutoff = typeof cutoff === 'number' ? cutoff : 100;
    let expandable = Utils.wordCount(text) > cutoff;

    className = 'TruncatedText' + (className ? ' ' + className : '');
    text = expanded ? text : Utils.truncate(text, cutoff);

    return (
      <div className={className}>
        {text}
        {expandable && (
          <button className="TruncatedText-Toggle" onClick={this.toggleExpansion}>
            {expanded ? 'Show Less' : 'Show More'}
            <Icon fa={expanded ? 'angle-double-up' : 'angle-double-down'} />
          </button>
        )}
      </div>
    );
  }
};

export default TruncatedText;
