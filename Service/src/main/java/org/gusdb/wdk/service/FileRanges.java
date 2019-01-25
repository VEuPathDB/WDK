package org.gusdb.wdk.service;

import static org.gusdb.fgputil.FormatUtil.isInteger;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.IoUtil;
import org.gusdb.fgputil.Range;
import org.gusdb.fgputil.Wrapper;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.service.service.AbstractWdkService;
import org.gusdb.wdk.service.statustype.PartialContentStatusType;

public class FileRanges {

  private static final Logger LOG = Logger.getLogger(FileRanges.class);

  private static final String RANGE_HEADER_VALUE_PREFIX = "bytes=";

  public static Range<Long> parseRangeHeaderValue(String rangeStr) {
    if (rangeStr == null) {
      return new Range<>(1L, null);
    }
    if (!rangeStr.startsWith(RANGE_HEADER_VALUE_PREFIX)) {
      throw new BadRequestException("Endpoint does not support non-byte range requests.");
    }
    String[] tokens = rangeStr.substring(RANGE_HEADER_VALUE_PREFIX.length()).split("-");
    if (tokens.length > 2) {
      throw new BadRequestException("Currently only a single range is supported");
    }
    if (isInteger(tokens[0]) && (tokens.length == 1 || isInteger(tokens[1]))) {
      Range<Long> range = new Range<>(Long.parseLong(tokens[0]),
          tokens.length == 1 ? null : Long.parseLong(tokens[1]));
      if (range.getMin() < 0 || range.getMax() < 0) {
        throw new BadRequestException("Range values must be positive integers.");
      }
    }
    throw new BadRequestException("Range header must be of the form '" + RANGE_HEADER_VALUE_PREFIX + "<min>-<max>");
  }

  public static Response getFileChunkResponse(Path filePath, Range<Long> range) throws WdkModelException {
    // if full file requested, simply return file
    if (range.getFirst() == 1 && range.getSecond() == null) {
      return Response.ok(filePath.toFile(), MediaType.APPLICATION_OCTET_STREAM).build();
    }
    // otherwise must slice out the requested range
    FileInputStream fileIn = null;
    try {
      Wrapper<Long> bytesRead = new Wrapper<Long>().set(0L);
      long maxBytesToRead = range.getMax() - range.getMin() + 1; // inclusive range
      fileIn = new FileInputStream(filePath.toFile()) {
        @Override
        public int read() throws IOException {
          if (bytesRead.get() >= maxBytesToRead) {
            return -1;
          }
          bytesRead.set(bytesRead.get() + 1);
          return super.read();
        }
      };
      fileIn.skip(range.getMin() - 1);

      return Response.ok(AbstractWdkService.getStreamingOutput(fileIn), MediaType.APPLICATION_OCTET_STREAM)
          .status(new PartialContentStatusType())
          .build();
    }
    catch(IOException e) {
      IoUtil.closeQuietly(fileIn);
      LOG.error("Could not read requested file: " + filePath, e);
      throw new WdkModelException("Unable to read required resource.", e);
    }
  }
}
