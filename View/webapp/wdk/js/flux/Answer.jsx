import React from 'react';
import AnswerStore from '../stores/AnswerStore';

var PropTypes = React.PropTypes;

export default React.createClass({

  propTypes: {
    questionName: PropTypes.string.required
  },

  getInitialState() {
    return AnswerStore.getState();
  },

  componentWillMount() {
    AnswerStore.subscribe(this.setState);
    AnswerStore.loadAnswer(this.props.questionName);
  },

  componentDidUnmount() {
    AnswerStore.unsubscribe(this.setState);
  }

});
