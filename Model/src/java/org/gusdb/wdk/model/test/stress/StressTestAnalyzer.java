/**
 * 
 */
package org.gusdb.wdk.model.test.stress;

import org.gusdb.wdk.model.test.stress.StressTestTask.ResultType;

/**
 * @author: Jerric
 * @created: Mar 16, 2006
 * @modified by: Jerric
 * @modified at: Mar 16, 2006
 * 
 */
public class StressTestAnalyzer {

    private StressTestTask[] tasks;

    /**
     * 
     */
    public StressTestAnalyzer(StressTestTask[] tasks) {
        this.tasks = tasks;
    }

    public int getTaskCount() {
        return tasks.length;
    }

    public int getTaskCount(String taskType) {
        int count = 0;
        for (StressTestTask task : tasks) {
            UrlItem urlItem = task.getUrlItem();
            if (urlItem.getUrlType().equalsIgnoreCase(taskType)) count++;
        }
        return count;
    }

    public int getSucceededTaskCount() {
        int count = 0;
        for (StressTestTask task : tasks) {
            if (task.getResultType() == ResultType.Succeeded) count++;
        }
        return count;
    }

    public int getSucceededTaskCount(String taskType) {
        int count = 0;
        for (StressTestTask task : tasks) {
            UrlItem urlItem = task.getUrlItem();
            if (task.getResultType() == ResultType.Succeeded
                    && urlItem.getUrlType().equalsIgnoreCase(taskType))
                count++;
        }
        return count;
    }

    public int getFailedTaskCount() {
        int count = 0;
        for (StressTestTask task : tasks) {
            if (task.getResultType() != ResultType.Succeeded) count++;
        }
        return count;
    }

    public int getFailedTaskCount(String taskType) {
        int count = 0;
        for (StressTestTask task : tasks) {
            UrlItem urlItem = task.getUrlItem();
            if (task.getResultType() != ResultType.Succeeded
                    && urlItem.getUrlType().equalsIgnoreCase(taskType))
                count++;
        }
        return count;
    }

    public int getFailedTaskCount(ResultType resultType) {
        int count = 0;
        for (StressTestTask task : tasks) {
            if (task.getResultType() == resultType) count++;
        }
        return count;
    }

    public int getFailedTaskCount(ResultType resultType, String taskType) {
        int count = 0;
        for (StressTestTask task : tasks) {
            UrlItem urlItem = task.getUrlItem();
            if (task.getResultType() == resultType
                    && urlItem.getUrlType().equalsIgnoreCase(taskType))
                count++;
        }
        return count;
    }

    public float getTaskSuccessRatio() {
        float success = getSucceededTaskCount();
        return (success / tasks.length);
    }

    public float getTaskSuccessRatio(String taskType) {
        float success = getSucceededTaskCount(taskType);
        int count = getTaskCount(taskType);
        return (count == 0)? 0: (success / count);
    }

    public float getTotalResponseTime() {
        long sum = 0;
        for (StressTestTask task : tasks) {
            sum += task.getDuration();
        }
        return (sum / 1000F);
    }

    public float getTotalResponseTime(String taskType) {
        long sum = 0;
        for (StressTestTask task : tasks) {
            UrlItem urlItem = task.getUrlItem();
            if (urlItem.getUrlType().equalsIgnoreCase(taskType))
                sum += task.getDuration();
        }
        return (sum / 1000F);
    }

    public float getAverageResponseTime() {
        float total = getTotalResponseTime();
        return (total / tasks.length);
    }

    public float getAverageResponseTime(String taskType) {
        float total = getTotalResponseTime(taskType);
        int count = getTaskCount(taskType);
        return (count == 0)? 0: (total / count);
    }

    public float getTotalSucceededResponseTime() {
        long sum = 0;
        for (StressTestTask task : tasks) {
            if (task.getResultType() == ResultType.Succeeded)
                sum += task.getDuration();
        }
        return (sum / 1000F);

    }

    public float getTotalSucceededResponseTime(String taskType) {
        long sum = 0;
        for (StressTestTask task : tasks) {
            UrlItem urlItem = task.getUrlItem();
            if (task.getResultType() == ResultType.Succeeded
                    && urlItem.getUrlType().equalsIgnoreCase(taskType))
                sum += task.getDuration();
        }
        return (sum / 1000F);
    }

    public float getAverageSucceededResponseTime() {
        float total = getTotalSucceededResponseTime();
        int count = getSucceededTaskCount();
        return (count == 0)? 0: (total / count);
    }

    public float getAverageSucceededResponseTime(String taskType) {
        float total = getTotalSucceededResponseTime(taskType);
        int count = getSucceededTaskCount(taskType);
        return (count == 0)? 0: (total / count);
    }

    public void print() {
        // print out results

        // print out all types results
        System.out.println("------------------ All Types --------------------");
        System.out.println("# of Total Tasks: " + getTaskCount());
        System.out.print("# of Succeeded Tasks: " + getSucceededTaskCount());
        System.out.println("\t (" + (getTaskSuccessRatio() * 100) + "%)");
        System.out.println("# of Failed Tasks: " + getFailedTaskCount());
        System.out.println("# of Connection Error: "
                + getFailedTaskCount(ResultType.ConnectionError));
        System.out.println("# of HTTP Error: "
                + getFailedTaskCount(ResultType.HttpError));
        System.out.println("# of Application Error: "
                + getFailedTaskCount(ResultType.ApplicationException));
        System.out.print("Response Time - Total: " + getTotalResponseTime());
        System.out.println("\tAverage: " + getAverageResponseTime());
        System.out.print("SucceededResponse Time - Total: " + getTotalSucceededResponseTime());
        System.out.println("\tAverage: " + getAverageSucceededResponseTime());

        // print out specific types results
        String[] types = { StressTester.TYPE_HOME_URL,
                StressTester.TYPE_QUESTION_URL,
                StressTester.TYPE_XML_QUESTION_URL,
                StressTester.TYPE_RECORD_URL };
        for (String type : types) {
            System.out.println();
            System.out.println("------------------ Type: " + type
                    + " --------------------");
            System.out.println("# of Total Tasks: " + getTaskCount(type));
            System.out.print("# of Succeeded Tasks: "
                    + getSucceededTaskCount(type));
            System.out.println("\t (" + (getTaskSuccessRatio(type) * 100)
                    + "%)");
            System.out.println("# of Failed Tasks: " + getFailedTaskCount(type));
            System.out.println("# of Connection Error: "
                    + getFailedTaskCount(ResultType.ConnectionError, type));
            System.out.println("# of HTTP Error: "
                    + getFailedTaskCount(ResultType.HttpError, type));
            System.out.println("# of Application Error: "
                    + getFailedTaskCount(ResultType.ApplicationException, type));
            System.out.print("Response Time - Total: " + getTotalResponseTime(type));
            System.out.println("\tAverage: " + getAverageResponseTime(type));
            System.out.print("SucceededResponse Time - Total: " + getTotalSucceededResponseTime(type));
            System.out.println("\tAverage: " + getAverageSucceededResponseTime(type));
        }
    }
}
