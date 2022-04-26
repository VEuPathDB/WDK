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
 * @author Jerric
 */
public class StressTestRunner implements Runnable {

    private static Logger logger = Logger.getLogger( StressTestRunner.class );

    public static enum RunnerState {
        Idle, Waiting, Finished, Executing,
    }

    private static int RUNNER_ID_SEED = 0;

    private int _runnerId;
    private StressTestTask _task;
    private RunnerState _state;
    private boolean _running;
    private int _delay;
    private boolean _stopped;
    private String _cookies;

    public StressTestRunner( ) {
        _runnerId = RUNNER_ID_SEED++;
        _state = RunnerState.Idle;
        _running = false;
        _stopped = true;
    }
    
    public int getRunnerId() {
        return _runnerId;
    }
    
    public RunnerState getState() {
        return _state;
    }
    
    /**
     * @param task
     * @param delay
     *            delay in seconds before starting the task
     * @throws InvalidStatusException if runner is in invalid state
     */
    public synchronized void assignTask( StressTestTask task, int delay )
            throws InvalidStatusException {
        if ( _state != RunnerState.Idle )
            throw new InvalidStatusException(
                    "Invalid runner state. Current is in: " + _state.name() );
        
        logger.debug( "Assigning task: " + task.getTaskId() );
        
        _task = task;
        // convert seconds into milliseconds
        _delay = delay * 1000;
        _state = RunnerState.Waiting;
        _task.setRunnerId( _runnerId );
    }
    
    public synchronized StressTestTask popFinishedTask() {
        if ( _state != RunnerState.Finished ) return null;
        StressTestTask finishedTask = _task;
        _task = null;
        _state = RunnerState.Idle;
        return finishedTask;
    }
    
    public synchronized void stop() {
        logger.info( "Stopping runner #" + _runnerId + "..." );
        _running = false;
    }
    
    public synchronized boolean isStopped() {
        return _stopped;
    }

    @Override
    public void run() {
        _running = true;
        _stopped = false;
        while ( _running ) {
            // no new task, wait
            if ( _state == RunnerState.Finished || _state == RunnerState.Idle ) {
                try {
                    Thread.sleep( 500 );
                } catch ( InterruptedException ex ) {}
                continue;
            }
            // wait a period of the delay
            try {
                Thread.sleep( _delay );
            } catch ( InterruptedException ex ) {}
            
            logger.debug( "Executing task: " + _task.getTaskId() );
            _state = RunnerState.Executing;
            // run the task and get the content of the requested page
            _task.setStartTime( System.currentTimeMillis() );
            String content = retrievePage();
            if ( content != null ) checkException( content );
            
            logger.debug( "Finished task: " + _task.getTaskId() );
            if ( _task.getResultType() != ResultType.Succeeded )
                logger.error( _task.getResultType().name() + "\t"
                        + _task.getUrlItem() );
            
            _task.setFinishTime( System.currentTimeMillis() );
            _state = RunnerState.Finished;
        }
        _stopped = true;
    }
    
    private String retrievePage() {
        try {
            UrlItem urlItem = _task.getUrlItem();
            HttpURLConnection connection = urlItem.getConnection( _cookies );
            // get new cookie
            readCookie( connection );
            
            BufferedInputStream in = new BufferedInputStream(
                    connection.getInputStream() );
            
            // check the http response
            int httpCode = connection.getResponseCode();
            String message = connection.getResponseMessage();
            if ( httpCode != HttpURLConnection.HTTP_OK ) {
                _task.setResultType( ResultType.HttpError );
                _task.setResultMessage( message );
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
                _task.setResultType( ResultType.Succeeded );
                _task.setResultMessage( "Retrieved non-text content" );
                return null;
            }
        } catch ( IOException ex ) {
            // ex.printStackTrace();
            _task.setResultType( ResultType.ConnectionError );
            _task.setResultMessage( ex.getMessage() );
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
            _cookies = sb.toString();
            // TEST
            logger.info( "Got cookie: " + _cookies );
        }
    }
    
    private void checkException( String content ) {
        // check if the error string "unexpected error" or "exception" presents
        // in the content of the webpage
        content = content.toLowerCase();
        if ( content.indexOf( "unexpected error" ) >= 0
                || content.indexOf( "exception" ) >= 0
                || content.indexOf( "query cannot be executed" ) >= 0 ) {
            _task.setResultType( ResultType.ApplicationException );
            _task.setResultMessage( "Web application throws out an exception.\n"
                    + content );
        } else {
            _task.setResultType( ResultType.Succeeded );
            _task.setResultMessage( "Retrieved text content" );
        }
    }
}
