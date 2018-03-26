import React from 'react';

import Link from 'Components/Link';
import { Mesa, MesaState } from 'mesa';
import Icon from 'Components/Icon/IconAlt';
import TextBox from 'Components/InputControls/TextBox';
import TextArea from 'Components/InputControls/TextArea';
import { bytesToHuman } from 'Utils/Converters';
import moment from 'Utils/MomentUtils';

const classify = (substyle = null) => `UserDatasetDetail${substyle ? '-' + substyle : ''}`;

function textCell (prop, transform) {
  const getValue = typeof transform === 'function'
    ? transform
    : (value) => value;
  return ({ row }) => prop in row
    ? <span>{getValue(row[prop])}</span>
    : null;
};

class UserDatasetDetailNew extends React.Component {
  constructor (props) {
    super(props);
    this.state = {
      editingCache: {},
      sharingModalOpen: false
    };
    this.isMyDataset = this.isMyDataset.bind(this);
    this.validateKey = this.validateKey.bind(this);
    this.editMeta = this.editMeta.bind(this);
    this.saveMetaEdit = this.saveMetaEdit.bind(this);
    this.cancelMetaEdit = this.cancelMetaEdit.bind(this);
    this.onMetaChange = this.onMetaChange.bind(this);
  }

  isMyDataset () {
    const { user, dataset } = this.props;
    return user && user.id && user.id === dataset.ownerUserId;
  }

  validateKey (key) {
    if (typeof key !== 'string' || !key.length)
      throw new TypeError(`Can't edit meta for invalid key: ${JSON.stringify(key)}`);
  }

  editMeta (key) {
    this.validateKey(key);
    return () => {
      const { userDataset } = this.props;
      const { editingCache } = this.state;
      const { meta } = userDataset;
      if (key in editingCache) return;
      this.setState({
        editingCache: {
          ...editingCache,
          [key]: meta[key]
        }
      });
    }
  }

  saveMetaEdit (key) {
    this.validateKey(key);
    return () => {
      const { userDataset } = this.props;
      const { editingCache } = this.state;
      const { updateUserDatasetDetail } = this.props;
      if (!key in editingCache) return;
      const value = editingCache[key];
      const meta = { ...userDataset.meta, [key]: value };
      updateUserDatasetDetail(userDataset, meta);
      this.cancelMetaEdit(key)();
    }
  }

  cancelMetaEdit (key) {
    this.validateKey(key);
    return () => {
      const { userDataset } = this.props;
      const editingCache = { ...this.state.editingCache };
      if (!key in editingCache) return;
      delete editingCache[key];
      this.setState({ editingCache });
    }
  }

  onMetaChange (key) {
    this.validateKey(key);
    return (value) => {
      const { editingCache } = this.state;
      this.setState({
        editingCache: { ...editingCache, [key]: value }
      });
    };
  }

  getInfoTableColumns ({ isOwner }) {
    return [
      {
        id: 'id',
        name: 'ID',
        renderCell: textCell('id')
      },
      {
        id: 'owner',
        name: 'Owner',
        renderCell: textCell('owner')
      },
      {
        id: 'projects',
        name: 'Compatible Projects',
        renderCell: textCell('projects', projects => projects.join(', '))
      },
      {
        id: 'created',
        name: 'Created',
        renderCell: textCell('created', created => moment(created).fromNow())
      },
      {
        id: 'size',
        name: 'Dataset Size',
        renderCell: textCell('size', size => bytesToHuman(size))
      },
      (!isOwner
        ? null
        : {
          id: 'percentQuotaUsed',
          name: 'Quota Usage',
          renderCell ({ row }) {
            let { percentQuotaUsed } = row;
            percentQuotaUsed = parseFloat(percentQuotaUsed);
            while (percentQuotaUsed > 100) {
              percentQuotaUsed = percentQuotaUsed / 1000000;
            }
            percentQuotaUsed = (Math.floor(percentQuotaUsed * 100)) / 100;
            return <span>{percentQuotaUsed}%</span>
          }
        }
      ),
      {
        id: 'type',
        name: 'Data Type',
        renderCell ({ row }) {
          const { type } = row;
          const { display, name, version } = type;
          return (
            <div>
              <b>{display}</b>
              <br />
              <span className="faded">{name} / {version}</span>
            </div>
          )
        }
      }
    ].filter(column => column)
  }

  getDependencyTableColumns () {
    return [
      {
        id: 'resourceDisplayName',
        name: 'Resource Display Name',
        renderCell ({ row }) {
          const { resourceDisplayName } = row;
          return resourceDisplayName;
        }
      },
      {
        id: 'resourceVersion',
        name: 'Resource Version',
        renderCell ({ row }) {
          const { resourceVersion } = row;
          return resourceVersion;
        }
      }
    ]
  }

  getFileTableColumns ({ datafiles, type }) {
    const trackData = type.data;
    const shouldShowGBrowseTrackUpload = Array.isArray(datafiles)
      && Array.isArray(trackData)
      && datafiles.some(({ name }) => {
        return trackData.find(uploadableFile => uploadableFile.dataFilename === name);
      });

    return [
      {
        id: 'name',
        name: 'File Name',
        renderCell ({ row }) {
          const { name } = row;
          return <code>{name}</code>
        }
      },
      {
        id: 'size',
        name: 'File Size',
        renderCell ({ row }) {
          const { size } = row;
          return bytesToHuman(size);
        }
      },
      {
        id: 'download',
        name: '',
        renderCell ({ row }) {
          const { name } = row;
          return (
            <a href={'#' + name} title="Download this file">
              <button className="btn btn-info">
                <Icon fa="download" className="left-side" />
                &nbsp;  Download
              </button>
            </a>
          )
        }
      },
      (!shouldShowGBrowseTrackUpload
        ? null
        : {
          id: 'gbrowse',
          name: 'GBrowse Status',
          renderCell ({ row }) {
            const { name } = row;
            const track = trackData.find(track => track.datafileName === name);
            const { errorMessage, uploadedAt, status, trackname } = track ? track : {};
            return (
              <div>
                {typeof uploadedAt === 'number'
                  ? `Last uploaded to GBrowse ${moment(uploadedAt).fromNow()}`
                  : `Not uploaded to GBrowse.`
                }
                <a onClick={alert('unimplemented: execute & watch upload...')} title="Upload This File to GBrowse">
                  <button className="btn btn-info">
                    <Icon fa="upload left-side"/> Upload
                  </button>
                </a>
              </div>
            );
          }
        }
      )
    ].filter(column => column);
  }

  renderInfoSection ({ isOwner, userDataset, actions }) {
    const infoTableState = MesaState.create({
      columns: actions.getInfoTableColumns({ isOwner }),
      rows: [ userDataset ]
    });
    return (
      <section>
        <h3 className={classify('SectionTitle')}>
          <Icon fa="info-circle"/>
          Dataset Information
        </h3>
        <div className="InfoTable">
          <Mesa state={infoTableState}/>
        </div>
      </section>
    );
  }

  renderFileSection ({ userDataset, actions }) {
    const fileTableState = MesaState.create({
      columns: actions.getFileTableColumns(userDataset),
      rows: userDataset.datafiles
    });
    return (
      <section>
        <h3 className={classify('SectionTitle')}>
          <Icon fa="files-o"/>
          Files in Dataset
        </h3>
        <Mesa state={fileTableState}/>
      </section>
    );
  }

  renderDependencySection ({ userDataset, actions }) {
    const depedencyTableState = MesaState.create({
      columns: actions.getDependencyTableColumns(userDataset),
      rows: userDataset.dependencies
    });
    return (
      <section>
        <h3 className={classify('SectionTitle')}>
          <Icon fa="puzzle-piece"/>
          Dataset Dependencies
        </h3>
        <Mesa state={depedencyTableState}/>
      </section>
    );
  }

  renderHeaderSection ({ userDataset, isOwner, actions, state }) {
    const { meta } = userDataset;
    const { editingCache } = state;
    const editingName = 'name' in editingCache;
    const editingSummary = 'summary' in editingCache;
    const editingDescription = 'description' in editingCache;
    return (
      <section>
        <div className={classify('Header')}>
          <Link to={'/workspace/datasets'}>
            <Icon fa="chevron-left"/>
            &nbsp; All User Datasets
          </Link>

          <h2 className={classify('Pretext')}>
            <b>{userDataset.owner}'s</b> Dataset
          </h2>
          <h1 className={classify('Title')}>
            {!isOwner
              ? null
              : (
                <a onClick={actions.editMeta('name')} title="Edit Dataset Name">
                  <Icon fa="pencil-square"/>
                </a>
              )
            }
            {editingName
              ? (
                <React.Fragment>
                    <TextBox
                      autoFocus={true}
                      value={editingCache.name}
                      onChange={actions.onMetaChange('name')}
                    />
                  <a onClick={actions.saveMetaEdit('name')} title="Save Name Changes">
                    <Icon fa="check-circle" className="save"/>
                  </a>
                  <a onClick={actions.cancelMetaEdit('name')} title="Cancel Name Changes">
                    <Icon fa="times-circle" className="cancel"/>
                  </a>
                </React.Fragment>
              ) : <span>{meta.name}</span>
            }
          </h1>

          <div className={classify('InfoRow row')}>

            <div className="box">
              <label className={classify('Label')}>
                <span>Summary</span>
                {!isOwner ? null : (
                  <a onClick={actions.editMeta('summary')}> Edit</a>
                )}
                {!editingSummary ? null : (
                  <React.Fragment>
                    <span className="spacer"/>
                    <a onClick={actions.saveMetaEdit('summary')} className="save">Save</a>
                    <a onClick={actions.cancelMetaEdit('summary')} className="cancel">Cancel</a>
                  </React.Fragment>
                )}
              </label>
              <div className={classify('Summary')}>
                {editingSummary
                  ? <TextArea onChange={actions.onMetaChange('summary')} value={editingCache.summary} />
                  : <p>{meta.summary}</p>
                }
              </div>
            </div>

            <div className="box">
              <label className={classify('Label')}>
                <span>Description</span>
                {!isOwner ? null : (
                  <a onClick={actions.editMeta('description')}> Edit</a>
                )}
                {!editingDescription ? null : (
                  <React.Fragment>
                    <span className="spacer"/>
                    <a onClick={actions.saveMetaEdit('description')} className="save">Save</a>
                    <a onClick={actions.cancelMetaEdit('description')} className="cancel">Cancel</a>
                  </React.Fragment>
                )}
              </label>
              <div className={classify('Description')}>
                {editingDescription
                  ? <TextArea value={editingCache.description} onChange={actions.onMetaChange('description')}/>
                  : <p>{meta.description}</p>
                }
              </div>
            </div>

            <div className={classify('Actions box')}>
              <button className="btn btn-error">
                <Icon fa="trash" className="left-side" />
                {isOwner ? 'Delete Dataset' : 'Remove From My Datasets'}
              </button>
              <button className="btn btn-success">
                <Icon fa="share-alt" className="left-side" />
                Share Dataset
              </button>
            </div>
          </div>
        </div>
      </section>
    )
  }

  renderDetailsSection ({ userDataset }) {
    return (
      <section>
        <details>
          <pre><code>{JSON.stringify(userDataset, null, '  ')}</code></pre>
        </details>
      </section>
    )
  }

  render () {
    const { state } = this;
    const { userDataset, isOwner, questionMap, getQuestionUrl, userDatasetUpdating, updateError } = this.props;
    const HeaderSection = this.renderHeaderSection;
    const InfoSection = this.renderInfoSection;
    const FileSection = this.renderFileSection;
    const DependencySection = this.renderDependencySection;
    const DetailsSection = this.renderDetailsSection;
    const actions = {
      editMeta: this.editMeta,
      saveMetaEdit: this.saveMetaEdit,
      onMetaChange: this.onMetaChange,
      cancelMetaEdit: this.cancelMetaEdit,
      getInfoTableColumns: this.getInfoTableColumns,
      getFileTableColumns: this.getFileTableColumns,
      getDependencyTableColumns: this.getDependencyTableColumns
    };
    const props = { userDataset, isOwner, actions, state };
    return (
      <div className={classify()}>
        <HeaderSection {...props}/>
        <InfoSection {...props}/>
        <FileSection {...props}/>
        <DependencySection {...props}/>
        <DetailsSection {...props}/>
      </div>
    )
  }
};

export default UserDatasetDetailNew;
