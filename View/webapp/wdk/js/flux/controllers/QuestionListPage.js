import React from 'react';
import Router from 'react-router';
import createStoreMixin from '../mixins/createStoreMixin';
import createActionCreatorsMixin from '../mixins/createActionCreatorsMixin';

var { Link } = Router;
var storeMixin = createStoreMixin('questionListStore');
var actionsMixin = createActionCreatorsMixin('questionListPageActions');

var QuestionListPage = React.createClass({

  mixins: [ storeMixin, actionsMixin ],

  getStateFromStores(stores) {
    return stores.questionListStore.getState();
  },

  componentDidMount() {
    this.questionListPageActions.loadQuestions();
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
