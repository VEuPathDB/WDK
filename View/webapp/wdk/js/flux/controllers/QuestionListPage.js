import React from 'react';
import Router from 'react-router';
import createStoreMixin from '../mixins/createStoreMixin';
import createActionCreatorsMixin from '../mixins/createActionCreatorsMixin';

var { Link } = Router;
var storeMixin = createStoreMixin('questionStore');
var actionsMixin = createActionCreatorsMixin('questionActions');

var QuestionListPage = React.createClass({

  mixins: [ storeMixin, actionsMixin ],

  getStateFromStores(stores) {
    return stores.questionStore.getState();
  },

  componentDidMount() {
    this.questionActions.loadQuestions();
  },

  render() {
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

export default QuestionListPage;
