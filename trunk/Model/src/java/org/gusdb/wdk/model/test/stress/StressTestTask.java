/**
 * 
 */
package org.gusdb.wdk.model.test.stress;

import java.net.URL;

/**
 * @author: Jerric
 * @created: Mar 14, 2006
 * @modified by: Jerric
 * @modified at: Mar 14, 2006
 * 
 */
public class StressTestTask {

    public static enum TaskState {
        Pending, Executing, Finished,
    }

    public static enum ResultType {
        Unknown, Succeeded, HttpError, ConnectionError, ApplicationException,
    }

    private static long taskIdSeed = 0;

    private long taskId;
    private URL testUrl;
    private String testUrlType;

    private TaskState state;
    private long startTime;
    private long finishTime;

    private ResultType resultType;
    private String resultMessage;

    private int runnerId;

    /**
     * 
     */
    public StressTestTask(URL testUrl, String testUrlType) {
        taskId = taskIdSeed++;
        this.testUrl = testUrl;
        this.testUrlType = testUrlType;
        state = TaskState.Pending;
        resultType = ResultType.Unknown;
        resultMessage = "Unknown";
    }

    /**
     * @return Returns the taskId.
     */
    public long getTaskId() {
        return taskId;
    }

    /**
     * @return Returns the testUrl.
     */
    public URL getTestUrl() {
        return testUrl;
    }

    /**
     * @return Returns the testUrlType.
     */
    public String getTestUrlType() {
        return testUrlType;
    }

    /**
     * @return Returns the state.
     */
    public TaskState getState() {
        return state;
    }

    /**
     * @return Returns the resultType.
     */
    public ResultType getResultType() {
        return resultType;
    }

    /**
     * @return Returns the resultMessage.
     */
    public String getResultMessage() {
        return resultMessage;
    }

    /**
     * @return Returns the running time of the task, in millisecond
     */
    public long getDuration() {
        return (finishTime - startTime);
    }

    /**
     * @return Returns the runnerId.
     */
    public int getRunnerId() {
        return runnerId;
    }

    /**
     * @param runnerId The runnerId to set.
     */
    public void setRunnerId(int runnerId) {
        this.runnerId = runnerId;
    }

    public void startTask() {
        state = TaskState.Executing;
        startTime = System.currentTimeMillis();
    }

    public void finishTask(ResultType resultType, String resultMessage) {
        finishTime = System.currentTimeMillis();
        state = TaskState.Finished;
        this.resultType = resultType;
        this.resultMessage = resultMessage;
    }
}
