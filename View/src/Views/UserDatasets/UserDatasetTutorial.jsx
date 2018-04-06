import React from 'react';

function UserDatasetTutorial ({ projectName, rootUrl }) {
  const orientationUrl = '';
  const galaxyUrl = 'https://eupathdb.globusgenomics.org/';
  return (
    <div className="row UserDataset-Tutorial">
      <div className="box xs-12 md-4">
        <img src={rootUrl + '/wdk/images/userDatasetHelp/tut-step-1.jpg'} />
        <p>
          Using <b><a href={galaxyUrl} target="_blank">EuPathDB Galaxy</a></b>, upload your data files and select the relevant genome (and genome project) for each.
        </p>
        <p>
          To learn more about <b>EuPathDB Galaxy</b>, including how to get started, see our <a href={orientationUrl} target="_blank">orientation tutorial</a>.
        </p>
      </div>
      <div className="box xs-12 md-4">
        <img src={rootUrl + '/wdk/images/userDatasetHelp/tut-step-2.jpg'} />
        <p>
          Using the <b>EuPathDB Export Tools</b> on the left-side navigation, prepare your dataset by selecting the files you’d like to use, and provide some general information about the dataset. This data can be edited later, from the <i>My Datasets</i> page.
        </p>
        <p>
          When you’re ready, <code>Execute</code> the export. The process of exporting to EuPathDB may take some time. Progress can be monitored from the right-side “History” panel in Galaxy.
        </p>
      </div>
      <div className="box xs-12 md-4">
        <img src={rootUrl + '/wdk/images/userDatasetHelp/tut-step-3.jpg'} />
        <ul>
          <li>You can now view, manage, share, and utilize your dataset in <b>{projectName}</b>.</li>
          <li>Datasets you’ve created contribute to a per-user upload limit/quota of <b>1 GB</b>.</li>
          <li><b>GBrowse</b>-compatible Bigwig files can be uploaded through the dataset’s detail page. <br/>Click the dataset name or status icon to see this page.</li>
        </ul>
      </div>
    </div>
  );
};

export default UserDatasetTutorial;
