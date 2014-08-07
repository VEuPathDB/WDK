package org.gusdb.wdk.jmx.mbeans;

import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.wdk.model.WdkModel;

/**
 * MBean for database connection pool statistics, including
 * counts on active and idle connections in the pool,
 * counts of connections the application borrows and returns to the pool.
 *
 */
public abstract class AbstractConnectionPool extends BeanBase {

  DatabaseInstance database;

  public AbstractConnectionPool() {
    super();  
    this.database = getDb(wdkModel);
  }

  protected abstract DatabaseInstance getDb(WdkModel model);

  /**
   * Return the name of the class that is initiating and managing
   * the connection pool this mbean is reporting about. That is,
   * the source of the data, distinct from any other connection pools
   * that may exist elsewhere in the application.
   */
  public String getPoolOwnerClassname() {
    return database.getClass().getName();
  }
  

  /**
   * Return the number of instances currently borrowed from this pool.
   *
   * @see org.gusdb.fgputil.db.pool.DatabaseInstance#getActiveCount()
   */
  public int getNumActive() {
    return database.getActiveCount();
  }

  /**
   * Return the number of instances currently idle in this pool
   *
   * @see org.gusdb.fgputil.db.pool.DatabaseInstance#getIdleCount()
   */
  public int getNumIdle() {
    return database.getIdleCount();
  }

  /**
   * Returns the minimum number of objects allowed in the pool before the 
   * evictor thread (if active) spawns new objects
   * This value is managed by the connection pool library.
   *
   * @see org.gusdb.fgputil.db.pool.DatabaseInstance#getMinIdle()
   */
  public int getMinIdle() {
    return database.getMinIdle();
  }

  /**
   * Returns the cap on the number of "idle" instances in the pool.
   * This value is managed by the connection pool library.
   *
   * @see org.gusdb.fgputil.db.pool.DatabaseInstance#getMaxIdle()
   */
  public int getMaxIdle() {
    return database.getMaxIdle();
  }

  /**
   * Returns the minimum amount of time an object may sit idle in the pool 
   * before it is eligible for eviction by the idle object evictor (if any).
   * This value is managed by the connection pool library.
   *
   * @see org.gusdb.fgputil.db.pool.DatabaseInstance#getMinEvictableIdleTimeMillis()
   */
  public long getMinEvictableIdleTimeMillis() {
    return database.getMinEvictableIdleTimeMillis();
  }

  /**
   * Returns the minimum amount of time an object may sit idle in the pool 
   * before it is eligible for eviction by the idle object evictor (if any),
   * with the extra condition that at least "minIdle" amount of object remain in the pool.
   * This value is managed by the connection pool library.
   *
   * @see org.gusdb.fgputil.db.pool.DatabaseInstance#getSoftMinEvictableIdleTimeMillis()
   */
  public long getSoftMinEvictableIdleTimeMillis() {
    return database.getSoftMinEvictableIdleTimeMillis();
  }

  /**
   * Returns the number of milliseconds to sleep between runs of the idle object evictor thread.
   * This value is managed by the connection pool library.
   *
   * @see org.gusdb.fgputil.db.pool.DatabaseInstance#getTimeBetweenEvictionRunsMillis()
   */
  public long getTimeBetweenEvictionRunsMillis() {
    return database.getTimeBetweenEvictionRunsMillis();
  }


  /**
   * When true, objects will be validated before being returned by the borrowObject() method.
   * This value is managed by the connection pool library.
   *
   * @see org.gusdb.fgputil.db.pool.DatabaseInstance#getTestOnBorrow()
   */
	public boolean getTestOnBorrow() {
    return database.getTestOnBorrow();
	}
	
  /**
   * When true, objects will be validated before being returned to the pool within the returnObject(T).
   * This value is managed by the connection pool library.
   *
   * @see org.gusdb.fgputil.db.pool.DatabaseInstance#getTestOnReturn()
   */
  public boolean getTestOnReturn() {
    return database.getTestOnReturn();
  }

  /**
   * When true, objects will be validated by the idle object evictor (if any).
   * This value is managed by the connection pool library.
   *
   * @see org.gusdb.fgputil.db.pool.DatabaseInstance#getTestWhileIdle()
   */
  public boolean getTestWhileIdle() {
    return database.getTestWhileIdle();
  }

  /**
   * Returns the number of connections borrowed from the
   * connection pool. This value is managed by the application.
   *
   * @see org.gusdb.fgputil.db.pool.DatabaseInstance#getNumConnectionsOpened
   */
  public int getBorrowedCount() {
    return database.getNumConnectionsOpened();
  }
  
  /**
   * Returns the number of connections returned to the
   * connection pool. This value is managed by the application.
   *
   * @see org.gusdb.fgputil.db.pool.DatabaseInstance#getNumConnectionsClosed
   */
  public int getReturnedCount() {
    return database.getNumConnectionsClosed();
  }

  /**
   * Returns the number of connections returned to the
   * connection pool. This value is managed by the application.
   *
   * @see org.gusdb.fgputil.db.pool.DatabaseInstance#getNumConnectionsClosed
   */
  public int getCurrentlyOpenCount() {
    return database.getConnectionsCurrentlyOpen();
  }

  public String getUnclosedConnectionInfo() {
    return database.getUnclosedConnectionInfo();
  }
}
