import React from 'react';

const Doc = React.createClass({
  componentDidMount() {
    this.setTitle();
  },
  componentDidUpdate() {
    this.setTitle();
  },
  setTitle() {
    const { title } = this.props;
    document.title = title;
  },
  render() {
    return this.props.children;
  }
});

export default Doc;
