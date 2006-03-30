/**
 * 
 */
package org.gusdb.wdk.model.test.stress;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.test.stress.StressTestTask.ResultType;

/**
 * @author: Jerric
 * @created: Mar 14, 2006
 * @modified by: Jerric
 * @modified at: Mar 14, 2006
 * 
 */
public class StressTestRunner implements Runnable {

    public static enum RunnerState {
        Idle, Finished, Executing,
    }

    private static Logger logger = Logger.getLogger(StressTestRunner.class);

    private int runnerId;
    private StressTestTask task;
    private RunnerState state;
    private boolean running;
    private int delay;
    private boolean active;

    /**
     * 
     */
    public StressTestRunner(int runnerId) {
        this.runnerId = runnerId;
        state = RunnerState.Idle;
        running = false;
        active = false;
    }

    public int getRunnerId() {
        return runnerId;
    }

    public RunnerState getState() {
        return this.state;
    }
    
    public boolean isActive() {
        return active;
    }

    public void setClientTask(StressTestTask task, int delay)
            throws InvalidStatusException {
        if (state != RunnerState.Idle)
            throw new InvalidStatusException(
                    "Invalid runner state. Current is in: " + state.name());

        logger.debug("Assigning task: " + task.getTaskId());

        this.task = task;
        // convert seconds into milliseconds
        this.delay = delay * 1000;
        this.state = RunnerState.Executing;
        this.task.setRunnerId(runnerId);
    }

    public StressTestTask getFinishedTask() {
        if (state != RunnerState.Finished) return null;
        StressTestTask finishedTask = task;
        task = null;
        state = RunnerState.Idle;
        return finishedTask;
    }

    public void stop() {
        running = false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {
        running = true;
        while (running) {
            // no new task, wait
            if (state == RunnerState.Finished || state == RunnerState.Idle) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {}
                continue;
            }
            logger.debug("Executing task: " + task.getTaskId());
            // wait a period of the delay
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ex) {}

            // run the task and get the content of the requested page
            active = true;
            task.startTask();
            String content = retrievePage();
            if (content != null) checkException(content);

            logger.debug("Finished task: " + task.getTaskId());
            if (task.getResultType() != ResultType.Succeeded)
                logger.error(task.getResultType().name() + "\t"
                        + task.getResultMessage() + "\t" + task.getTestUrl());
            active = false;
            state = RunnerState.Finished;
        }
    }

    private String retrievePage() {
        try {
            UrlItem urlItem = task.getTestUrl();
            HttpURLConnection connection = urlItem.getConnection();
            BufferedInputStream in = new BufferedInputStream(
                    connection.getInputStream());

            // check the http response
            int httpCode = connection.getResponseCode();
            String message = connection.getResponseMessage();
            if (httpCode != HttpURLConnection.HTTP_OK) {
                task.finishTask(ResultType.HttpError, message);
                return null;
            }
            String contentType = connection.getContentType().toLowerCase();

            // read content into a buffer of byte array
            int length = connection.getContentLength();
            byte[] bytContent;
            // always use first method in SSL
            length = -1;
            if (length == -1) { // length not known
                int result;
                List<Byte> buffer = new ArrayList<Byte>(1024);
                while ((result = in.read()) != -1)
                    buffer.add((byte) result);
                bytContent = new byte[buffer.size()];
                for (int i = 0; i < buffer.size(); i++)
                    bytContent[i] = buffer.get(i);
                buffer.clear();
                buffer = null;
            } else { // content already known
                bytContent = new byte[length];
                int read = 0;
                while (read < length) {
                    int available = in.available();
                    if (available + read > length) available = length - read;
                    in.read(bytContent, read, available);
                    read += available;
                }
            }
            in.close();
            connection.disconnect();

            // determine the content type
            if (contentType.startsWith("text")) {
                return new String(bytContent);
            } else {
                task.finishTask(ResultType.Succeeded,
                        "Retrieved non-text content");
                return null;
            }
        } catch (IOException ex) {
            // ex.printStackTrace();
            task.finishTask(ResultType.ConnectionError, ex.getMessage());
            return null;
        }
    }

    private void checkException(String content) {
        // check if the error string "unexpected error" or "exception" presents
        // in the content of the webpage
        content = content.toLowerCase();
        if (content.indexOf("unexpected error") >= 0
                || content.indexOf("exception") >= 0) {
            task.finishTask(ResultType.ApplicationException,
                    "Web application throws out an exception");
        } else {
            task.finishTask(ResultType.Succeeded, "Retrieved text content");
        }
    }
}
