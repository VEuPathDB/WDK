import React from 'react';
import { Link } from 'react-router';
import wrappable from '../utils/wrappable';
import ContextMixin from '../utils/contextMixin';

let QuestionListController = React.createClass({

  mixins: [ ContextMixin ],

  componentDidMount() {
    let { questionStore } = this.context.stores;
    let { questionActions } = this.context.actions;
    this.storeSubscription = questionStore.subscribe(state => {
      this.setState(state);
    });
    questionActions.loadQuestions();
  },

  componentWillUnmount() {
    this.storeSubscription.dispose();
  },

  render() {
    if (!this.state) { return null; }
    let { questions, error } = this.state;

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

export default wrappable(QuestionListController);
