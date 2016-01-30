import ReactDOM from 'react-dom';
import React from 'react';

export default class AccordionButton extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      "id": props.id
    };
    // hard bind the handleExpansion functions to the Accordion Button object
    this.handleExpansion = this.handleExpansion.bind(this);
  }

  handleExpansion() {
    this.props.toggleExpansion(this.state.id);
  }

  render() {
    let handleExpansion = this.handleExpansion;
    let expanded = this.props.expanded;
    let leaf = this.props.leaf;
    let id = this.props.id;

    return (
      <span className="wdk-CheckboxTree-accordionButton" onClick={handleExpansion}>
        {expanded && !leaf ? <i className="fa-li fa fa-caret-down"></i> : ""}
        {!expanded && !leaf ? <i className="fa-li fa wdk-CheckboxTree-icon fa-caret-right"></i> : ""}
      </span>
    )
  }
}