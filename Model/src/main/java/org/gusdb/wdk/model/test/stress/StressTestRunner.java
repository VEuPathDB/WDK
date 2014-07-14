/**
 * 
 */
package org.gusdb.wdk.model.test.stress;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        Idle, Waiting, Finished, Executing,
    }
    
    private static Logger logger = Logger.getLogger( StressTestRunner.class );
    private static int runnerIdSeed = 0;
    
    private int runnerId;
    private StressTestTask task;
    private RunnerState state;
    private boolean running;
    private int delay;
    private boolean stopped;
    
    private String cookies;
    
    /**
     * 
     */
    public StressTestRunner( ) {
        this.runnerId = runnerIdSeed++;
        state = RunnerState.Idle;
        running = false;
        stopped = true;
    }
    
    public int getRunnerId() {
        return runnerId;
    }
    
    public RunnerState getState() {
        return this.state;
    }
    
    /**
     * @param task
     * @param delay
     *            delay in seconds before starting the task
     * @throws InvalidStatusException if runner is in invalid state
     */
    public synchronized void assignTask( StressTestTask task, int delay )
            throws InvalidStatusException {
        if ( state != RunnerState.Idle )
            throw new InvalidStatusException(
                    "Invalid runner state. Current is in: " + state.name() );
        
        logger.debug( "Assigning task: " + task.getTaskId() );
        
        this.task = task;
        // convert seconds into milliseconds
        this.delay = delay * 1000;
        this.state = RunnerState.Waiting;
        this.task.setRunnerId( runnerId );
    }
    
    public synchronized StressTestTask popFinishedTask() {
        if ( state != RunnerState.Finished ) return null;
        StressTestTask finishedTask = task;
        task = null;
        state = RunnerState.Idle;
        return finishedTask;
    }
    
    public synchronized void stop() {
        logger.info( "Stopping runner #" + runnerId + "..." );
        running = false;
    }
    
    public synchronized boolean isStopped() {
        return stopped;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        running = true;
        stopped = false;
        while ( running ) {
            // no new task, wait
            if ( state == RunnerState.Finished || state == RunnerState.Idle ) {
                try {
                    Thread.sleep( 500 );
                } catch ( InterruptedException ex ) {}
                continue;
            }
            // wait a period of the delay
            try {
                Thread.sleep( delay );
            } catch ( InterruptedException ex ) {}
            
            logger.debug( "Executing task: " + task.getTaskId() );
            state = RunnerState.Executing;
            // run the task and get the content of the requested page
            task.setStartTime( System.currentTimeMillis() );
            String content = retrievePage();
            if ( content != null ) checkException( content );
            
            logger.debug( "Finished task: " + task.getTaskId() );
            if ( task.getResultType() != ResultType.Succeeded )
                logger.error( task.getResultType().name() + "\t"
                        + task.getUrlItem() );
            
            task.setFinishTime( System.currentTimeMillis() );
            state = RunnerState.Finished;
        }
        stopped = true;
    }
    
    private String retrievePage() {
        try {
            UrlItem urlItem = task.getUrlItem();
            HttpURLConnection connection = urlItem.getConnection( cookies );
            // get new cookie
            readCookie( connection );
            
            BufferedInputStream in = new BufferedInputStream(
                    connection.getInputStream() );
            
            // check the http response
            int httpCode = connection.getResponseCode();
            String message = connection.getResponseMessage();
            if ( httpCode != HttpURLConnection.HTTP_OK ) {
                task.setResultType( ResultType.HttpError );
                task.setResultMessage( message );
                return null;
            }
            String contentType = connection.getContentType().toLowerCase();
            
            // read content into a buffer of byte array
            int length = connection.getContentLength();
            byte[ ] bytContent;
            // always use first method in SSL
            length = -1;
            if ( length == -1 ) { // length not known
                int result;
                List< Byte > buffer = new ArrayList< Byte >( 1024 );
                while ( ( result = in.read() ) != -1 )
                    buffer.add( ( byte ) result );
                bytContent = new byte[ buffer.size() ];
                for ( int i = 0; i < buffer.size(); i++ )
                    bytContent[ i ] = buffer.get( i );
                buffer.clear();
                buffer = null;
            } else { // content already known
                bytContent = new byte[ length ];
                int read = 0;
                while ( read < length ) {
                    int available = in.available();
                    if ( available + read > length ) available = length - read;
                    in.read( bytContent, read, available );
                    read += available;
                }
            }
            in.close();
            connection.disconnect();
            
            // determine the content type
            if ( contentType.startsWith( "text" ) ) {
                return new String( bytContent );
            } else {
                task.setResultType( ResultType.Succeeded );
                task.setResultMessage( "Retrieved non-text content" );
                return null;
            }
        } catch ( IOException ex ) {
            // ex.printStackTrace();
            task.setResultType( ResultType.ConnectionError );
            task.setResultMessage( ex.getMessage() );
            return null;
        }
    }
    
    private void readCookie( HttpURLConnection connection ) {
        Map< String, List< String >> header = connection.getHeaderFields();
        // TEST
        // for (String name : header.keySet()) {
        // System.out.println("Header: " + name);
        // }
        
        List< String > cookieList = header.get( "Set-Cookie" );
        if ( cookieList != null ) {
            StringBuffer sb = new StringBuffer();
            for ( String cookie : cookieList ) {
                if ( sb.length() > 0 ) sb.append( "," );
                sb.append( cookie );
            }
            cookies = sb.toString();
            // TEST
            logger.info( "Got cookie: " + cookies );
        }
    }
    
    private void checkException( String content ) {
        // check if the error string "unexpected error" or "exception" presents
        // in the content of the webpage
        content = content.toLowerCase();
        if ( content.indexOf( "unexpected error" ) >= 0
                || content.indexOf( "exception" ) >= 0
                || content.indexOf( "query cannot be executed" ) >= 0 ) {
            task.setResultType( ResultType.ApplicationException );
            task.setResultMessage( "Web application throws out an exception.\n"
                    + content );
        } else {
            task.setResultType( ResultType.Succeeded );
            task.setResultMessage( "Retrieved text content" );
        }
    }
}
