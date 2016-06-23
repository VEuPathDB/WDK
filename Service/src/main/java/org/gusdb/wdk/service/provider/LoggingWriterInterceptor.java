package org.gusdb.wdk.service.provider;

import java.io.IOException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

@Provider
public class LoggingWriterInterceptor implements WriterInterceptor {

  private static final Logger LOG = Logger.getLogger(LoggingWriterInterceptor.class);

  private static final Level LOG_LEVEL = Level.INFO;

  @Override
  public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
    try {
      context.proceed();
    }
    finally {
      LOG.log(LOG_LEVEL, "Request complete");
    }
  }
}
