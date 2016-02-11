
/**
 * Fetches the step for the given ID and also finds the question and recordClass
 * for that step using the passed service.  Returns a promise whose value is an
 * object with properties { step, question, recordClass }.
 */
export function getStepBundle(stepId, service) {

  let stepPromise = service.findStep(stepId);
  let questionPromise = stepPromise.then(step => {
    return service.findQuestion( q => q.name === step.answerSpec.questionName );
  });
  let recordClassPromise = questionPromise.then(question => {
    return service.findRecordClass( rc => rc.name === question.recordClassName );
  });

  return Promise.all([ stepPromise, questionPromise, recordClassPromise ])
    .then(([ step, question, recordClass ]) => ({ step, question, recordClass }));
}

export function getSingleRecordStepBundle(recordClass, primaryKeyString) {
  let questionName = '__' + recordClass.name + '__singleRecordQuestion__';
  let step = {
    displayName: 'Single Record',
    answerSpec: {
      questionName: questionName,
      parameters: {
        "primaryKeys": primaryKeyString
      }
    }
  };
  // TODO: if this is used in places other than step download form, may need
  //   to fill in more fields and think about what their values should be
  let question = {
    name: questionName,
    displayName: 'Single Record',
    //shortDisplayName: 'Single Record',
    //description: 'Retrieves a single record by ID',
    //help: '',
    //newBuild: 0,
    //reviseBuild: 0,
    //urlSegment: 'singleRecord',
    //'class': recordClass.name,
    //parameters: [ { name: 'primaryKeys' } ],
    defaultAttributes: [ ],
    dynamicAttributes: [ ],
    defaultSummaryView: '_default',
    //summaryViewPlugins: [ ],
    //stepAnalysisPlugins: [ ]
  };
  return { recordClass, question, step };
}