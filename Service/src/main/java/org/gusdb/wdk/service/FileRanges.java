package org.gusdb.wdk.service;

import static org.gusdb.fgputil.FormatUtil.isInteger;
import static org.gusdb.wdk.service.service.AbstractWdkService.getStreamingOutput;

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
import org.gusdb.wdk.service.statustype.PartialContentStatusType;

public class FileRanges {

  private static final Logger LOG = Logger.getLogger(FileRanges.class);

  public static final String RANGE_HEADER = "Range";
  public static final String CONTENT_RANGE_HEADER = "Content-Range";

  private static final Long DEFAULT_RANGE_BEGIN = 0L;
  private static final String SIZE_UNITS = "bytes";
  private static final String RANGE_HEADER_VALUE_PREFIX = SIZE_UNITS + "=";

  public static Range<Long> parseRangeHeaderValue(String rangeStr) {
    if (rangeStr == null) {
      return new Range<>(DEFAULT_RANGE_BEGIN, Range.empty());
    }
    if (!rangeStr.startsWith(RANGE_HEADER_VALUE_PREFIX)) {
      throw new BadRequestException("Endpoint does not support non-byte range requests.");
    }
    if (rangeStr.startsWith("-")) {
      throw new BadRequestException("Range must have a begin value.");
    }
    String[] tokens = rangeStr.substring(RANGE_HEADER_VALUE_PREFIX.length()).split("-");
    if (tokens.length > 2) {
      throw new BadRequestException("Currently only a single range is supported");
    }
    if (isInteger(tokens[0]) && (tokens.length == 1 || isInteger(tokens[1]))) {
      Range<Long> range = new Range<>(Long.parseLong(tokens[0]),
          tokens.length == 1 ? null : Long.parseLong(tokens[1]));
      if (range.getBegin() < 0 || range.getEnd() < 0) {
        throw new BadRequestException("Range values must be positive integers.");
      }
    }
    throw new BadRequestException("Range header must be of the form '" + RANGE_HEADER_VALUE_PREFIX + "<min>-<max>'.");
  }

  public static Response getFileChunkResponse(Path filePath, Range<Long> byteRange) throws WdkModelException {
    FileInputStream fileIn = null;
    try {
      long fileLength = getFileLength(filePath);

      // if full file requested, simply return file
      if (byteRange.getBegin() == 0 && !byteRange.hasEnd()) {
        return Response
            .ok(filePath.toFile())
            .type(MediaType.APPLICATION_OCTET_STREAM)
            .header(CONTENT_RANGE_HEADER, SIZE_UNITS + " */" + fileLength)
            .build();
      }

      // resolve end and check that it is not greater than file length
      if (!byteRange.hasEnd()) {
        byteRange.setEnd(fileLength - 1);
      }
      else if (fileLength < byteRange.getEnd()) {
        throw new BadRequestException("End of range must be <= " + fileLength + " for this resource.");
      }

      // create an input stream that will only read to the end of the specified range
      Wrapper<Long> bytesRead = new Wrapper<Long>().set(0L);
      long bytesToRead = byteRange.getEnd() - byteRange.getBegin() + 1; // inclusive range
      fileIn = new FileInputStream(filePath.toFile()) {
        @Override
        public int read() throws IOException {
          if (bytesRead.get() >= bytesToRead) {
            return -1;
          }
          bytesRead.set(bytesRead.get() + 1);
          return super.read();
        }
      };
      fileIn.skip(byteRange.getBegin() - 1);

      return Response
          .ok(getStreamingOutput(fileIn))
          .type(MediaType.APPLICATION_OCTET_STREAM)
          .status(new PartialContentStatusType())
          .header(CONTENT_RANGE_HEADER, SIZE_UNITS + " " + byteRange.getBegin() +
              "-" + (byteRange.hasEnd() ? byteRange.getEnd() : "") + "/" + bytesToRead)
          .build();
    }
    catch(IOException e) {
      IoUtil.closeQuietly(fileIn);
      LOG.error("Could not read requested file: " + filePath, e);
      throw new WdkModelException("Unable to read required resource.", e);
    }
  }

  private static long getFileLength(Path filePath) throws IOException {
    try (FileInputStream input = new FileInputStream(filePath.toFile())) {
      return input.available();
    }
  }
}
