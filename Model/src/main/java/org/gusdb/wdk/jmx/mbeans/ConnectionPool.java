package org.gusdb.wdk.jmx.mbeans;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.wdk.jmx.BeanBase;

/**
 * MBean for database connection pool statistics, including
 * counts on active and idle connections in the pool,
 * counts of connections the application borrows and returns to the pool.
 *
 */
public class ConnectionPool extends BeanBase implements ConnectionPoolMBean {

  private static final Logger LOG = Logger.getLogger(OpenConnections.class);

  private DatabaseInstance _database;
  
  public ConnectionPool(DatabaseInstance database) {
    this._database = database;
  }

  /**
   * Return the name of the class that is initiating and managing
   * the connection pool this mbean is reporting about. That is,
   * the source of the data, distinct from any other connection pools
   * that may exist elsewhere in the application.
   */
  @Override
  public String getPoolOwnerClassname() {
    return _database.getClass().getName();
  }
  

  /**
   * Return the number of instances currently borrowed from this pool.
   *
   * @see org.gusdb.fgputil.db.pool.DatabaseInstance#getActiveCount()
   */
  @Override
  public int getNumActive() {
    return _database.getActiveCount();
  }

  /**
   * Return the number of instances currently idle in this pool
   *
   * @see org.gusdb.fgputil.db.pool.DatabaseInstance#getIdleCount()
   */
  @Override
  public int getNumIdle() {
    return _database.getIdleCount();
  }

  /**
   * Returns the minimum number of objects allowed in the pool before the 
   * evictor thread (if active) spawns new objects
   * This value is managed by the connection pool library.
   *
   * @see org.gusdb.fgputil.db.pool.DatabaseInstance#getMinIdle()
   */
  @Override
  public int getMinIdle() {
    return _database.getMinIdle();
  }

  /**
   * Returns the cap on the number of "idle" instances in the pool.
   * This value is managed by the connection pool library.
   *
   * @see org.gusdb.fgputil.db.pool.DatabaseInstance#getMaxIdle()
   */
  @Override
  public int getMaxIdle() {
    return _database.getMaxIdle();
  }

  /**
   * Returns the minimum amount of time an object may sit idle in the pool 
   * before it is eligible for eviction by the idle object evictor (if any).
   * This value is managed by the connection pool library.
   *
   * @see org.gusdb.fgputil.db.pool.DatabaseInstance#getMinEvictableIdleTimeMillis()
   */
  @Override
  public long getMinEvictableIdleTimeMillis() {
    return _database.getMinEvictableIdleTimeMillis();
  }

  /**
   * Returns the number of milliseconds to sleep between runs of the idle object evictor thread.
   * This value is managed by the connection pool library.
   *
   * @see org.gusdb.fgputil.db.pool.DatabaseInstance#getTimeBetweenEvictionRunsMillis()
   */
  @Override
  public long getTimeBetweenEvictionRunsMillis() {
    return _database.getTimeBetweenEvictionRunsMillis();
  }


  /**
   * When true, objects will be validated before being returned by the borrowObject() method.
   * This value is managed by the connection pool library.
   *
   * @see org.gusdb.fgputil.db.pool.DatabaseInstance#getTestOnBorrow()
   */
	@Override
  public boolean getTestOnBorrow() {
    return _database.getTestOnBorrow();
	}
	
  /**
   * When true, objects will be validated before being returned to the pool within the returnObject(T).
   * This value is managed by the connection pool library.
   *
   * @see org.gusdb.fgputil.db.pool.DatabaseInstance#getTestOnReturn()
   */
  @Override
  public boolean getTestOnReturn() {
    return _database.getTestOnReturn();
  }

  /**
   * When true, objects will be validated by the idle object evictor (if any).
   * This value is managed by the connection pool library.
   *
   * @see org.gusdb.fgputil.db.pool.DatabaseInstance#getTestWhileIdle()
   */
  @Override
  public boolean getTestWhileIdle() {
    return _database.getTestWhileIdle();
  }

  /**
   * Returns the number of connections borrowed from the
   * connection pool. This value is managed by the application.
   *
   * @see org.gusdb.fgputil.db.pool.DatabaseInstance#getNumConnectionsOpened
   */
  @Override
  public int getBorrowedCount() {
    return _database.getNumConnectionsOpened();
  }
  
  /**
   * Returns the number of connections returned to the
   * connection pool. This value is managed by the application.
   *
   * @see org.gusdb.fgputil.db.pool.DatabaseInstance#getNumConnectionsClosed
   */
  @Override
  public int getReturnedCount() {
    return _database.getNumConnectionsClosed();
  }

  /**
   * Returns the number of connections returned to the
   * connection pool. This value is managed by the application.
   *
   * @see org.gusdb.fgputil.db.pool.DatabaseInstance#getNumConnectionsClosed
   */
  @Override
  public int getCurrentlyOpenCount() {
    return _database.getConnectionsCurrentlyOpen();
  }

  /**
   * report summary information in string format with stacktraces, if any
   */
   @Override
  public String getUnclosedConnectionInfo() {
    return _database.getUnclosedConnectionInfo();
  }

  /**
   * record information to log file
   */
  @Override
  public void dumpOpenDBConnections() {
     LOG.info(_database.getUnclosedConnectionInfo());
  }

  /**
   * report information in string format. This is redundant 
   * with getUnclosedConnectionInfo() but when used in a GUI
   * viewer (e.g. jconsole) displays the data in a more 
   * human-readable format than the attribute field.
   */
  @Override
  public String showOpenDBConnections() {
    return _database.getUnclosedConnectionInfo();
  }

}
