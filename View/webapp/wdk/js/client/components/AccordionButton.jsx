import ReactDOM from 'react-dom';
import React from 'react';

export default class AccordionButton extends React.Component {

  constructor(props) {
    super(props);
    // hard bind the handleExpansion functions to the Accordion Button object
    this.handleExpansion = this.handleExpansion.bind(this);
  }

  handleExpansion() {
    this.props.toggleExpansion(this.props.node);
  }

  render() {
    let expanded = this.props.expanded;
    let leaf = this.props.leaf;

    return (
      <span className="wdk-CheckboxTree-accordionButton" onClick={this.handleExpansion}>
        {expanded && !leaf ? <i className="fa-li fa fa-caret-down"></i> : ""}
        {!expanded && !leaf ? <i className="fa-li fa wdk-CheckboxTree-icon fa-caret-right"></i> : ""}
      </span>
    )
  }
}