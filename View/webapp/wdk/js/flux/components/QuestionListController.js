import React from 'react';
import { Link } from 'react-router';

var QuestionListController = React.createClass({

  propTypes: {
    application: React.PropTypes.object.isRequired
  },

  componentDidMount() {
    var store = this.props.application.getStore('questionStore');
    var actions = this.props.application.getActions('questionActions');
    this.storeSubscription = store.subscribe(state => {
      this.setState(state);
    });
    actions.loadQuestions();
  },

  componentWillUnmount() {
    this.storeSubscription.dispose();
  },

  render() {
    if (!this.state) return null;
    var { questions, error } = this.state;

    if (error) {
      return (
        <div>There was an error: {error}</div>
      );
    } else {
      return (
        <div>
          <ol>
            {questions.map(question => (
              <li key={question.name}>
                {question.displayName + ' - '}
                <Link to="answer" params={{ questionName: question.name }}>answer page</Link>
              </li>
            ))}
          </ol>
        </div>
      );
    }
  }

});

export default QuestionListController;
