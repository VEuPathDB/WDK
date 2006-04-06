/**
 * 
 */
package org.gusdb.wdk.model.test.stress;


/**
 * @author: Jerric
 * @created: Mar 14, 2006
 * @modified by: Jerric
 * @modified at: Mar 31, 2006
 * 
 */
public class StressTestTask {

    public static enum ResultType {
        Unknown, Succeeded, HttpError, ConnectionError, ApplicationException,
    }

    private static long taskIdSeed = 0;

    private long taskId;
    private UrlItem urlItem;

    private long startTime;
    private long finishTime;

    private ResultType resultType;
    private String resultMessage;

    private int runnerId;

    /**
     * 
     */
    public StressTestTask(UrlItem urlItem) {
        taskId = taskIdSeed++;
        this.urlItem = urlItem;
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
     * @return
     */
    public UrlItem getUrlItem() {
        return urlItem;
    }

    /**
     * @return Returns the resultType.
     */
    public ResultType getResultType() {
        return resultType;
    }

    /**
     * @param resultType The resultType to set.
     */
    public void setResultType(ResultType resultType) {
        this.resultType = resultType;
    }

    /**
     * @return Returns the resultMessage.
     */
    public String getResultMessage() {
        return resultMessage;
    }

    /**
     * @param resultMessage The resultMessage to set.
     */
    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
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

    /**
     * @return Returns the startTime.
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * @param startTime The startTime to set.
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * @return Returns the finishTime.
     */
    public long getFinishTime() {
        return finishTime;
    }

    /**
     * @param finishTime The finishTime to set.
     */
    public void setFinishTime(long finishTime) {
        this.finishTime = finishTime;
    }

    /**
     * @return Returns the running time of the task, in millisecond
     */
    public long getDuration() {
        return (finishTime - startTime);
    }
}
