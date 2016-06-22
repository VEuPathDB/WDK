
/**
 * Fetches the step for the given ID and also finds the question and recordClass
 * for that step using the passed service.  Returns a promise whose value is an
 * object with properties { step, question, recordClass }.
 */
export function getStepBundlePromise(stepId, service) {

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

/**
 * Creates a single-record question for the passed recordClass and applies the
 * passed primary key to create a step of that question.  Returns a promise
 * whose value is an object with properties { step, question, recordClass }.
 */
export function getSingleRecordStepBundlePromise(recordClass, primaryKeyString) {

  // create single-record question and step for this record class
  let questionName = '__' + recordClass.name + '__singleRecordQuestion__';
  let step = {
      // fill primary key string so we know which single record this question is 
    displayName: primaryKeyString,
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

  // return a promise containing our generated bundle
  return Promise.resolve({ recordClass, question, step });
}
