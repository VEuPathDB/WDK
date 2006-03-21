/**
 * 
 */
package org.gusdb.wdk.model.test.stress;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.test.stress.StressTestRunner.RunnerState;

/**
 * @author: Jerric
 * @created: Mar 14, 2006
 * @modified by: Jerric
 * @modified at: Mar 14, 2006
 * 
 */
public class StressTestScheduler implements Runnable {

    private static Logger logger = Logger.getLogger(StressTestScheduler.class);

    private Stack<StressTestTask> pendingTasks;
    private Stack<StressTestTask> finishedTasks;
    private List<StressTestRunner> runners;
    private int maxDelay;
    private int runnerNum;
    private boolean running;
    private boolean finished;
    private Random rand;

    /**
     * 
     */
    public StressTestScheduler(int runnerNum, int maxDelay) {
        this.runnerNum = runnerNum;
        this.maxDelay = maxDelay;
        pendingTasks = new Stack<StressTestTask>();
        finishedTasks = new Stack<StressTestTask>();
        runners = new ArrayList<StressTestRunner>(runnerNum);
        running = false;
        finished = false;
        rand = new Random(System.currentTimeMillis());
    }

    public void addTestTask(StressTestTask testTask) {
        pendingTasks.push(testTask);
    }

    public void run() {
        running = true;
        finished = false;
        // create runners
        runners.clear();
        for (int i = 1; i <= runnerNum; i++) {
            StressTestRunner runner = new StressTestRunner(i);
            Thread thread = new Thread(runner);
            thread.setName("Runner Thread #" + runner.getRunnerId());
            thread.start();
            runners.add(runner);
        }

        while (running) {
            // for each runner, check if it is finished or idling
            for (StressTestRunner runner : runners) {
                if (runner.getState() == RunnerState.Executing) continue;
                // get finished task, if have
                if (runner.getState() == RunnerState.Finished) {
                    StressTestTask task = runner.getFinishedTask();
                    if (task != null) finishedTasks.add(task);
                }
                // assign a new task if possible
                if (runner.getState() == RunnerState.Idle
                        && !pendingTasks.isEmpty()) {
                    StressTestTask task = pendingTasks.pop();
                    int delay = rand.nextInt(maxDelay);
                    try {
                        runner.setClientTask(task, delay);
                    } catch (InvalidStatusException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            // sleep for a while
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {}
        }

        // wait until all runners are stopped
        boolean waiting = true;
        while (waiting) {
            waiting = false;
            for (StressTestRunner runner : runners) {
                logger.debug("Runner #"+runner.getRunnerId()+" state: " + runner.getState().name());

                if (runner.getState() == RunnerState.Executing) {
                    waiting = true;
                    // break;
                }
 
                // get finished task, if have
                if (runner.getState() == RunnerState.Finished) {
                    StressTestTask task = runner.getFinishedTask();
                    if (task != null) finishedTasks.add(task);
                }
            }
            System.out.println();

            if (waiting) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {}
            }
        }
        finished = true;
        logger.debug("The Stress Task Scheduler is stopped.");
    }

    public void stop() {
        logger.debug("Stopping the Stress Test Scheduler...");
        for (StressTestRunner runner : runners) {
            runner.stop();
        }
        running = false;
    }

    public StressTestTask[] getFinishedTasks() {
        StressTestTask[] tasks = new StressTestTask[finishedTasks.size()];
        finishedTasks.toArray(tasks);
        return tasks;
    }

    public boolean hasPendingTask() {
        return !pendingTasks.isEmpty();
    }

    public boolean isFinished() {
        return finished;
    }

    public int getPendingTaskCount() {
        return pendingTasks.size();
    }

    public int getFinishedTaskCount() {
        return finishedTasks.size();
    }
    
    public int getExecutingTaskCount() {
        int count = 0;
        for (StressTestRunner runner : runners) {
            if (runner.getState() == RunnerState.Executing) count++;
        }
        return count;
    }
}
