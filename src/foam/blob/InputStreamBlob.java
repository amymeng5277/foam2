/**
 * @license
 * Copyright 2018 The FOAM Authors. All Rights Reserved.
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package foam.blob;

import java.io.*;

public class InputStreamBlob
    extends AbstractBlob
    implements Closeable
{
  public static final int BUFFER_SIZE = 4096;

  protected int size_;
  protected int pos_ = 0;
  protected InputStream in_;

  public InputStreamBlob(InputStream in, int size) {
    size_ = size;
    in_ = in;
  }

  @Override
  public int read(OutputStream out, int offset, int length) {
    try {
      if ( offset != pos_ ) {
        throw new RuntimeException("Offset does not match stream position");
      }

      int n = 0;
      int read = 0;
      byte[] buffer = new byte[BUFFER_SIZE];
      while ( (n = in_.read(buffer, 0, buffer.length)) != -1 && read <= length ) {
        out.write(buffer, 0, n);
        read += n;
      }

      pos_ += read;
      return read;
    } catch (Throwable t) {
      return -1;
    }
  }

  @Override
  public int getSize() {
    return size_;
  }

  @Override
  public void close() throws IOException {
    in_.close();
  }
}