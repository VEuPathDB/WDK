import {
  QuestionsAdded,
  RecordClassesAdded
} from '../ActionType';

function createActions({ dispatcher, service }) {
  return {
    fetchCommonData() {
      // First, create a Promise for the question resource (the ajax request will
      // be made as soon as possible (which will more-or-less be when the current
      // method's execution is complete).
      var questionPromise = service.getResource('/question?expandQuestions=true');

      // Then, create a Promise for the recordClass
      var recordClassPromise = service.getResource('/record?expandRecordClasses=true');

      Promise.all([questionPromise, recordClassPromise])
        .then(function([ questions, recordClasses ]) {
          dispatcher.dispatch(QuestionsAdded({ questions }));
          dispatcher.dispatch(RecordClassesAdded({ recordClasses }));
        });
    }
  };
}

export default { createActions };
