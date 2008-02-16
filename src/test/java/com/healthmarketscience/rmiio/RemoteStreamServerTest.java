/*
Copyright (c) 2007 Health Market Science, Inc.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
USA

You can contact Health Market Science at info@healthmarketscience.com
or at the following address:

Health Market Science
2700 Horizon Drive
Suite 200
King of Prussia, PA 19406
*/

package com.healthmarketscience.rmiio;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.healthmarketscience.rmiio.util.PipeBuffer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.lang.reflect.Field;


/**
 * Note, setting the system property {@code rmiio.fastTest} to {@code false}
 * when running the maven test suite will run more exhaustive tests (although
 * they take a while).
 * 
 * @author James Ahlborn
 */
public class RemoteStreamServerTest extends BaseRemoteStreamTest {

  private static final Log LOG = LogFactory.getLog(RemoteStreamServerTest.class);

  static final String TEST_FILE = "./src/test/data/file_transfer.data";
  static final int FILE_SIZE = 277672;
  private static final int PARTIAL_SIZE = 3000;
  
  private static boolean _deleteOnExit = true;

  public void testTransfer() throws Exception
  {
    File testFile = new File(TEST_FILE);
    LOG.debug("data file: " + testFile.getAbsolutePath());
    List<List<File>> tempFiles = mainTest(new String[]{TEST_FILE,
                                                 Boolean.FALSE.toString(),
                                                 Boolean.FALSE.toString(),
                                                 Boolean.FALSE.toString(),
                                                 ((Boolean)isFastTest()).toString()},
                                          _clientExceptions,
                                    _monitors);

    checkClientExceptions(0);

    checkFiles(testFile, tempFiles);

    if(isFastTest()) {
      checkMonitors(1, true);
    } else {
      checkMonitors(20, true);
    }
  }

  public void testEmptyTransfer() throws Exception
  {
    File emptyFile = File.createTempFile("src", ".dat");
    if(_deleteOnExit) {
      emptyFile.deleteOnExit();
    }
    LOG.debug("empty data file: " + emptyFile.getAbsolutePath());
    List<List<File>> tempFiles = mainTest(new String[]{emptyFile.getAbsolutePath()},
                                    _clientExceptions, _monitors);

    checkClientExceptions(0);
    
    checkFiles(emptyFile, tempFiles);

    checkMonitors(20, true);
  }

  public void testAbortedTransfer() throws Exception
  {
    File testFile = new File(TEST_FILE);
    LOG.debug("data file: " + testFile.getAbsolutePath());
    List<List<File>> tempFiles = mainTest(new String[]{TEST_FILE,
                                                 Boolean.TRUE.toString()},
                                    _clientExceptions, _monitors);

    // each client should have thrown an IOException
    checkClientExceptions(8);

    checkFiles(testFile, tempFiles);

    checkMonitors(8, false);
  }
  
  public void testDoubleClose() throws Exception
  {
    DummyIOStream iostream = new DummyIOStream();

    InputStream istream = RemoteInputStreamClient.wrap(iostream);

    assertEquals(0, iostream._numCloses);
    istream.close();
    assertEquals(1, iostream._numCloses);
    istream.close();
    assertEquals(1, iostream._numCloses);

    
    iostream = new DummyIOStream();

    OutputStream ostream = RemoteOutputStreamClient.wrap(iostream);

    assertEquals(0, iostream._numCloses);
    ostream.close();
    assertEquals(1, iostream._numCloses);
    ostream.close();
    assertEquals(1, iostream._numCloses);
  }

  public void testReverseTransfer() throws Exception
  {
    File testFile = new File(TEST_FILE);
    LOG.debug("data file: " + testFile.getAbsolutePath());
    List<List<File>> tempFiles = mainTest(new String[]{TEST_FILE,
                                                 Boolean.FALSE.toString(),
                                                 Boolean.TRUE.toString(),
                                                 Boolean.FALSE.toString(),
                                                 ((Boolean)isFastTest()).toString()},
                                    _clientExceptions,
                                    _monitors);

    checkClientExceptions(0);
    
    checkFiles(testFile, tempFiles);

    if(isFastTest()) {
      checkMonitors(1, true);
    } else {
      checkMonitors(20, true);
    }
  }

  public void testSkip() throws Exception
  {
    File testFile = new File(TEST_FILE);
    LOG.debug("data file: " + testFile.getAbsolutePath());
    List<List<File>> tempFiles = mainTest(new String[]{TEST_FILE,
                                                 Boolean.FALSE.toString(),
                                                 Boolean.FALSE.toString(),
                                                 Boolean.TRUE.toString()},
                                    _clientExceptions, _monitors);

    // each client should have thrown an IOException
    checkClientExceptions(0);

    checkFiles(testFile, tempFiles);

    checkMonitors(4, true);
  }

  public void testReserialize() throws Exception
  {
    SerializableInputStream istream =
      new SerializableInputStream(new FileInputStream(TEST_FILE));
    Field inField = istream.getClass().getDeclaredField("_remoteIn");
    inField.setAccessible(true);
    assertTrue(inField.get(istream) instanceof GZIPRemoteInputStream);

    SerializableInputStream istreamRem = simulateRemote(istream);

    Object istreamRemIn = inField.get(istreamRem);
    assertFalse(istreamRemIn instanceof GZIPRemoteInputStream);
     
    istreamRem = simulateRemote(istream);

    Object istreamRemInAlt = inField.get(istreamRem);
    assertFalse(istreamRemInAlt instanceof GZIPRemoteInputStream);
    assertNotSame(istreamRemIn, istreamRemInAlt);
    assertEquals(istreamRemIn, istreamRemInAlt);

    istreamRem = simulateRemote(istreamRem);

    istreamRemInAlt = inField.get(istreamRem);
    assertFalse(istreamRemInAlt instanceof GZIPRemoteInputStream);
    assertNotSame(istreamRemIn, istreamRemInAlt);
    assertEquals(istreamRemIn, istreamRemInAlt);
    
    istream.close();
  }

  private void checkFiles(File srcFile, List<List<File>> tempFiles)
    throws IOException
  {
    // compare all complete generated files to original
    for(File tempFile : tempFiles.get(0)) {
      assertTrue(compare(srcFile, tempFile, false) == 0);
    }

    // compare all partial generated files to original
    for(File tempFile : tempFiles.get(1)) {
      assertTrue(compare(srcFile, tempFile, true) == 0);
    }
  }
  
  private static File getTempFile(String prefix, List<List<File>> tempFiles,
                                  boolean doPartial,
                                  boolean doAbort,
                                  boolean doSkip)
    throws IOException
  {
    File tempFile = File.createTempFile(prefix, ".out");
    if(_deleteOnExit) {
      tempFile.deleteOnExit();
    }
    if(tempFiles != null) {
      if((!doPartial) && (!doAbort) && (!doSkip)) {
        tempFiles.get(0).add(tempFile);
      } else {
        tempFiles.get(1).add(tempFile);
      }
    }
    return tempFile;
  }


  public static int compare(File file1, File file2, boolean file2IsPartial)
    throws IOException
  {
    LOG.debug("Comparing " + file1 + " and " + file2);
    if(!file1.canRead() || !file2.canRead()) {
      return (file1.canRead() ? 1 :
              (file2.canRead() ? -1 : 0));
    }

    InputStream fileIStream1 =
      new BufferedInputStream(new FileInputStream(file1));
    InputStream fileIStream2 =
      new BufferedInputStream(new FileInputStream(file2));

    int b1;
    int b2;
    do {
      b1 = fileIStream1.read();
      b2 = fileIStream2.read();
    } while((b1 == b2) && (b1 != -1) && (b2 != -1));

    if(file2IsPartial && (b2 == -1)) {
      // file2 is a partial file and all existing bytes match, we're all good
      return 0;
    }
      
    return((b1 < b2) ? -1 :
           ((b1 > b2) ? 1 : 0));
  }

  private static RemoteInputStream createInputFileStream(
      String fileName,
      boolean unreliable,
      boolean useCompression,
      boolean doAbort,
      List<Closeable> localServers,
      List<AccumulateRemoteStreamMonitor<?>> monitors)
    throws IOException
  {
    RemoteInputStream istream = null;
    RemoteStreamServer<?, RemoteInputStream> server = null;

    AccumulateRemoteStreamMonitor<RemoteInputStreamServer> monitor =
      new AccumulateRemoteStreamMonitor<RemoteInputStreamServer>(doAbort);
    if(monitors != null) {
      monitors.add(monitor);
    }
    if(!unreliable) {
      if(useCompression) {
        istream =
          simulateRemote(
              (server = 
               (new GZIPRemoteInputStream(
                 new BufferedInputStream(new FileInputStream(fileName)),
                 monitor)))
              .export());
      } else {
        // test writeReplace by *not* exporting explicitly here
        istream =
          simulateRemote((RemoteInputStream)
              (server = 
               (new SimpleRemoteInputStream(
                 new BufferedInputStream(new FileInputStream(fileName)),
                 monitor))));
      }
    } else {
      if(useCompression) {
        istream =
          simulateRemote(
              (server = 
               (new UnreliableRemoteInputStreamServer(
                 (new GZIPRemoteInputStream(
                   new BufferedInputStream(new FileInputStream(fileName)),
                   monitor)))))
              .export());
      } else {
        istream =
          simulateRemote(
              (server = 
               (new UnreliableRemoteInputStreamServer(
                 (new SimpleRemoteInputStream(
                   new BufferedInputStream(new FileInputStream(fileName)),
                   monitor)))))
              .export());
      }
    }

    if(localServers != null) {
      localServers.add(server);
    }
    
    return istream;
  }

  private static RemoteOutputStream createOutputFileStream(
      String callerName,
      List<List<File>> tempFiles,
      boolean unreliable,
      boolean useCompression,
      boolean doPartial,
      boolean doAbort,
      List<Closeable> localServers,
      List<AccumulateRemoteStreamMonitor<?>> monitors)
    throws IOException
  {

    // create temp file
    File tempFile = getTempFile(callerName, tempFiles, doPartial, doAbort,
                                false);

    RemoteOutputStream ostream = null;
    RemoteStreamServer<?, RemoteOutputStream> server = null;

    AccumulateRemoteStreamMonitor<RemoteOutputStreamServer> monitor =
      new AccumulateRemoteStreamMonitor<RemoteOutputStreamServer>(doAbort);
    if(monitors != null) {
      monitors.add(monitor);
    }
    if(!unreliable) {
      if(useCompression) {
        // test writeReplace by *not* exporting explicitly here
        ostream =
          simulateRemote((RemoteOutputStream)
              (server = 
               (new GZIPRemoteOutputStream(
                 new BufferedOutputStream(new FileOutputStream(tempFile)),
                 monitor))));
      } else {
        ostream =
          simulateRemote(
              (server = 
               (new SimpleRemoteOutputStream(
                 new BufferedOutputStream(new FileOutputStream(tempFile)),
                 monitor)))
              .export());
      }
    } else {
      if(useCompression) {
        ostream =
          simulateRemote(
              (server = 
               (new UnreliableRemoteOutputStreamServer(
                 (new GZIPRemoteOutputStream(
                   new BufferedOutputStream(new FileOutputStream(tempFile)),
                   monitor)))))
              .export());
      } else {
        ostream =
          simulateRemote(
              (server = 
               (new UnreliableRemoteOutputStreamServer(
                 (new SimpleRemoteOutputStream(
                   new BufferedOutputStream(new FileOutputStream(tempFile)),
                   monitor)))))
              .export());
      }
    }

    if(localServers != null) {
      localServers.add(server);
    }
    
    return ostream;
  }

  private static void consumeInputStream(
      String callerName,
      InputStream in,
      List<List<File>> tempFiles,
      boolean doPartial,
      boolean doAbort,
      boolean doSkip)
    throws IOException
  {
    // create temp file
    File tempFile = getTempFile("client", tempFiles, doPartial, doAbort,
                                doSkip);
      
    OutputStream out =
      new BufferedOutputStream(new FileOutputStream(tempFile));
    try {
      copy(in, out, doPartial, doSkip);
    } finally {
      if(out != null) {
        try {
          out.close();
        } catch(IOException ignored) {}
      }
    }
      
    LOG.debug("Wrote file stream");
  }

  private static void generateOutputStream(
      String inFileName,
      OutputStream out,
      boolean doPartial)
    throws IOException
  {
    InputStream in =
      new BufferedInputStream(new FileInputStream(inFileName));
    try {
      copy(in, out, doPartial, false);
    } finally {
      if(in != null) {
        try {
          in.close();
        } catch(IOException ignored) {
        }
      }
    }
      
    LOG.debug("Sent file stream");
  }

  public static void copy(InputStream in, OutputStream out,
                          boolean doPartial, boolean doSkip)
    throws IOException
  {
    final int BUF_SIZE = 1024;
    byte[] tmp = new byte[BUF_SIZE];

    int initAvail = in.available();

    int skipAt = 0;
    if(doSkip) {
      skipAt = (FILE_SIZE % BUF_SIZE) - 1;
    }
    
    int numRead = 0;
    int totRead = 0;
    int iteration = 0;
    while((numRead = cycleRead(in, tmp, iteration)) != -1) {
      cycleWrite(out, tmp, numRead, iteration);
      totRead += numRead;

      if(doPartial && (totRead >= PARTIAL_SIZE)) {
        // just return
        return;
      }
      if(doSkip && (totRead >= skipAt)) {
        long toSkip = FILE_SIZE - totRead;
        long skipped = in.skip(toSkip);
        if(skipped != toSkip) {
          throw new IOException("skipped wrong?");
        }
      }
        
      ++iteration;
    }

    if(totRead > 0) {
      if(initAvail < 0) {
        throw new IOException("no bytes?");
      }
    }

    out.flush();
    if(in.available() != 0) {
      throw new IOException("more bytes?");
    }
  }

  private static boolean isFastTest()
  {
    // default to "true" if unspecified
    return !Boolean.FALSE.toString().equals(
      System.getProperty("rmiio.fastTest"));
  }
  

  @SuppressWarnings("unchecked")
  public static List<List<File>> mainTest(
      String[] args,
      final List<Throwable> clientExceptions,
      final List<AccumulateRemoteStreamMonitor<?>> monitors)
    throws Exception
  {
    final String testFile = args[0];
    final boolean doAbort = ((args.length > 1) ?
                             Boolean.parseBoolean(args[1]) :
                             false);
    final boolean reverse = ((args.length > 2) ?
                             Boolean.parseBoolean(args[2]) :
                             false);
    final boolean doSkip = ((args.length > 3) ?
                             Boolean.parseBoolean(args[3]) :
                             false);
    final boolean doFastTests = ((args.length > 4) ?
                                 Boolean.parseBoolean(args[4]) :
                                 false);
    final List<List<File>> tempFiles =
      Arrays.asList(
          Collections.synchronizedList(new LinkedList<File>()),
          Collections.synchronizedList(new LinkedList<File>()));

    FileServer server = new FileServer(testFile, tempFiles, monitors);
    final RemoteFileServer stub = (RemoteFileServer)
      simulateRemote(UnicastRemoteObject.exportObject(server, 0));
    LOG.debug("Server ready");

    LOG.debug("Sleeping 3000 ms...");
    Thread.sleep(3000);

    LOG.debug("Running 'reliable' tests");
    Thread clientThread = new Thread(new Runnable()
      {
        public void run() {
          clientExceptions.addAll(
              FileClient.main(stub, testFile, tempFiles, doAbort, reverse,
                              doSkip, false, doFastTests, monitors));
        }
      });
    clientThread.start();
    clientThread.join();

    if(!doFastTests) {
      server.setUnreliable(true);
    
      LOG.debug("Running 'unreliable' tests");
      clientThread = new Thread(new Runnable()
        {
          public void run() {
            clientExceptions.addAll(
                FileClient.main(stub, testFile, tempFiles, doAbort, reverse,
                                doSkip, true, doFastTests, monitors));
          }
        });
      clientThread.start();
      clientThread.join();
    }
    
    LOG.debug("Unexporting server");
    UnicastRemoteObject.unexportObject(server, true);

    
    
    return tempFiles;
  }

  /**
   * Returns the result of serializing and deserializing the given, exported
   * RemoteStub.
   * <p>
   * Note, i'm leaving the paragraph below because i believe it is related to
   * the problem, however, adding the serialization cycle did <b>not</b> solve
   * the hard ref problem (hence that code is still in place).
   * <p>
   * Evidently, something special happens to a RemoteStub when it is
   * serialized.  There were previously issues during testing where
   * RemoteStubs were throwing NoSuchObjectException on the first remote call.
   * In these cases, the server objects were being garbage collected before
   * ever being used.  Eventually, I realized that this was because the
   * RemoteStubs were not being serialized in the test code (because both the
   * client and server are in the same VM).  There was initially a hack in the
   * RemoteStreamServer to get around this problem by temporarily maintaining
   * a hard reference to the server object until the client makes the first
   * successful call (which could cause leaks if the client dies before making
   * the first call).  After determining that the issue was due to
   * serialization, I was able to make the problem disappear by forcing a
   * serialize/deserialize cycle on the RemoteStub in the test code before
   * handing it to the client thread.
   *
   * @param obj RMI stub to force into "remote" mode
   */
  @SuppressWarnings("unchecked")
  public static <T> T simulateRemote(T obj)
    throws IOException
  {
    PipeBuffer.InputStreamAdapter istream =
      new PipeBuffer.InputStreamAdapter();
    PipeBuffer.OutputStreamAdapter ostream =
      new PipeBuffer.OutputStreamAdapter();
    istream.connect(ostream);
    ObjectOutputStream objOstream = new ObjectOutputStream(ostream);
    objOstream.writeObject(obj);
    objOstream.close();

    ObjectInputStream objIstream = new ObjectInputStream(istream);
    try {
      obj = (T)objIstream.readObject();
    } catch(ClassNotFoundException e) {
      throw (IOException)((new IOException()).initCause(e));
    }
    objIstream.close();

    return obj;
  }
  
  public static void main(String[] args) throws Exception
  {
    int argc = 0;
    String appType = null;
    if(args.length > argc) {
      appType = args[argc++];
    }

    // create sub-argument list
    int subArgsLength = ((args.length > argc) ? (args.length - argc) : 0);
    String[] subArgs = new String[subArgsLength];
    if(subArgsLength > 0) {
      System.arraycopy(args, argc, subArgs, 0, subArgsLength);
    }

    List<AccumulateRemoteStreamMonitor<?>> monitors = new ArrayList<AccumulateRemoteStreamMonitor<?>>();
    List<Throwable> clientExceptions = new ArrayList<Throwable>();
    if(appType.equals("-server")) {
      FileServer.main(subArgs);
    } else if(appType.equals("-client")) {
      FileClient.main(subArgs);
    } else if(appType.equals("-test")) {
      mainTest(subArgs, clientExceptions, null);
    } else {
      LOG.debug("First argument must be '-server' or 'client'");
      System.exit(1);
    }

    if(!clientExceptions.isEmpty()) {
      LOG.debug("Client exceptions: " + clientExceptions);
    }
  }

    
  public interface RemoteFileServer extends Remote {

    public RemoteInputStream getInputFileStream(boolean useCompression,
                                                boolean doAbort)
      throws IOException;

    public RemoteOutputStream getOutputFileStream(boolean useCompression,
                                                  boolean doPartial,
                                                  boolean doAbort)
      throws IOException;

    public SerializableInputStream getSerialInputFileStream(
        boolean useCompression,
        boolean doAbort)
      throws IOException;

    public SerializableOutputStream getSerialOutputFileStream(
        boolean useCompression,
        boolean doPartial,
        boolean doAbort)
      throws IOException;

    public void useInputStream(
        RemoteInputStream istream,
        boolean doPartial,
        boolean doAbort)
      throws IOException;

    public void useOutputStream(RemoteOutputStream ostream,
                                boolean doPartial)
      throws IOException;
    
  }

  public static class FileServer
    implements RemoteFileServer
  {
  
    private String _name;
    private boolean _unreliable;
    private List<List<File>> _tempFiles;
    private List<AccumulateRemoteStreamMonitor<?>> _monitors;
  
    public FileServer(String name, List<List<File>> tempFiles,
                      List<AccumulateRemoteStreamMonitor<?>> monitors) {
      _name = name;
      _tempFiles = tempFiles;
      _monitors = monitors;
    }

    public void setUnreliable(boolean unreliable) {
      _unreliable = unreliable;
    }
    
    public RemoteInputStream getInputFileStream(boolean useCompression,
                                                boolean doAbort)
      throws IOException
    {
      return createInputFileStream(_name, _unreliable, useCompression,
                                   doAbort, null, _monitors);
    }

    public RemoteOutputStream getOutputFileStream(boolean useCompression,
                                                  boolean doPartial,
                                                  boolean doAbort)
      throws IOException
    {
      return createOutputFileStream("server", _tempFiles, _unreliable,
                                    useCompression, doPartial, doAbort,
                                    null, _monitors);
    }

    public SerializableInputStream getSerialInputFileStream(
        boolean useCompression,
        boolean doAbort)
      throws IOException
    {
      return new SerializableInputStream(getInputFileStream(useCompression,
                                                            doAbort));
    }

    public SerializableOutputStream getSerialOutputFileStream(
        boolean useCompression,
        boolean doPartial,
        boolean doAbort)
      throws IOException
    {
      return new SerializableOutputStream(getOutputFileStream(useCompression,
                                                              doPartial,
                                                              doAbort));
    }

    public void useInputStream(RemoteInputStream istream,
                               boolean doPartial,
                               boolean doAbort)
      throws IOException
    {
      InputStream in = null;
      try {

        in = RemoteInputStreamClient.wrap(istream);

        consumeInputStream("client", in, _tempFiles, doPartial, doAbort,
                           false);
        
      } finally {
        if(in != null) {
          try {
            in.close();
          } catch(IOException ignored) {
            LOG.debug("In close failed" + ignored);
          }
        }
      }
    }

    public void useOutputStream(RemoteOutputStream ostream,
                                boolean doPartial)
      throws IOException
    {
      OutputStream out = null;
      try {

        out = RemoteOutputStreamClient.wrap(ostream);

        generateOutputStream(_name, out, doPartial);
        
      } finally {
        if(out != null) {
          try {
            out.close();
          } catch(IOException ignored) {
            LOG.debug("Out close failed" + ignored);
          }
        }
      }
    }
    
    public static void main(String args[])
    {
      try {
        FileServer server = new FileServer(args[0], null, null);
        server.setUnreliable(true);
        RemoteFileServer stub = (RemoteFileServer)
          simulateRemote(UnicastRemoteObject.exportObject(server, 0));

        // bind to registry
        Registry registry = LocateRegistry.getRegistry();
        registry.bind("RemoteFileServer", stub);

        LOG.debug("Server ready");

      } catch(Exception e) {
        LOG.debug("Server exception: " + e.toString());
        e.printStackTrace();
      }

    }

  }

  public static class FileClient
  {
    
    private FileClient() {
    
    }

    private static void getFile(RemoteFileServer stub,
                                boolean useCompression,
                                List<List<File>> tempFiles,
                                boolean doPartial,
                                boolean doAbort,
                                boolean doSkip,
                                boolean useSerial)
      throws Exception
    {
      InputStream in = null;
      try {

        LOG.debug("Getting in file stream");
        if(!useSerial) {
          in = RemoteInputStreamClient.wrap(
              stub.getInputFileStream(useCompression, doAbort));
        } else {
          in = stub.getSerialInputFileStream(useCompression, doAbort);
        }

        consumeInputStream("client", in, tempFiles, doPartial, doAbort,
                           doSkip);
        
      } finally {
        if(in != null) {
          try {
            in.close();
          } catch(IOException ignored) {
            LOG.debug("In close failed" + ignored);
          }
        }
      }
      
    }

    private static void getFileReverse(
        RemoteFileServer stub,
        boolean unreliable,
        boolean useCompression,
        List<List<File>> tempFiles,
        boolean doPartial,
        boolean doAbort,
        boolean useSerial,
        List<AccumulateRemoteStreamMonitor<?>> monitors)
      throws Exception
    {
      List<Closeable> localServer = new ArrayList<Closeable>(1);
      try {
        stub.useOutputStream(createOutputFileStream("client", tempFiles,
                                                    unreliable, useCompression,
                                                    doPartial, doAbort,
                                                    localServer, monitors),
                             doPartial);
      } finally {
        for(Closeable server : localServer) {
          server.close();
        }
      }
      
      LOG.debug("Wrote file stream");
    }

    private static void sendFile(RemoteFileServer stub,
                                 String inFileName,
                                 boolean useCompression,
                                 boolean doPartial,
                                 boolean doAbort,
                                 boolean useSerial)
      throws Exception
    {
      OutputStream out = null;
      try {

        LOG.debug("Getting out file stream");
        if(!useSerial) {
          out = RemoteOutputStreamClient.wrap(
              stub.getOutputFileStream(useCompression, doPartial,
                                       doAbort));
        } else {
          out = stub.getSerialOutputFileStream(useCompression, doPartial,
                                               doAbort);
        }

        generateOutputStream(inFileName, out, doPartial);

      } finally {
        if(out != null) {
          try {
            out.close();
          } catch(IOException ignored) {
            LOG.debug("Out close failed" + ignored);
          }
        }
      }
      
    }


    private static void sendFileReverse(
        RemoteFileServer stub,
        String inFileName,
        boolean unreliable,
        boolean useCompression,
        boolean doPartial,
        boolean doAbort,
        boolean useSerial,
        List<AccumulateRemoteStreamMonitor<?>> monitors)
      throws Exception
    {
      List<Closeable> localServer = new ArrayList<Closeable>(1);
      try {
        stub.useInputStream(createInputFileStream(inFileName,
                                                  unreliable, useCompression,
                                                  doAbort,
                                                  localServer, monitors),
                            doPartial, doAbort);
      } finally {
        for(Closeable server : localServer) {
          server.close();
        }
      }
      
      LOG.debug("Sent file stream");
    }
    

    public static List<Throwable> main(
        RemoteFileServer stub,
        String sendFileName,
        List<List<File>> tempFiles,
        boolean doAbort,
        boolean reverse,
        boolean doSkip,
        boolean unreliable,
        boolean doFastTests,
        List<AccumulateRemoteStreamMonitor<?>> monitors)
    {
      List<Throwable> exceptions = new ArrayList<Throwable>();
      
      // try uncompressed get (possibly aborted/skipped)
      try {
        if(!reverse) {
          getFile(stub, false, tempFiles, false, doAbort, doSkip, false);
        } else {
          getFileReverse(stub, unreliable, false, tempFiles, false, doAbort,
                         false, monitors);
        }
      } catch(Throwable t) {
        exceptions.add(t);
      }

      if(doFastTests) {
        return exceptions;
      }

      // try compressed get (possibly aborted/skipped)
      try {
        if(!reverse) {
          getFile(stub, true, tempFiles, false, doAbort, doSkip, false);
        } else {
          getFileReverse(stub, unreliable, true, tempFiles, false, doAbort,
                         false, monitors);
        }          
      } catch(Throwable t) {
        exceptions.add(t);
      }

      if(!doAbort && !doSkip) {
        // try partial uncompressed get
        try {
          if(!reverse) {
            getFile(stub, false, tempFiles, true, false, false, false);
          } else {
            getFileReverse(stub, unreliable, false, tempFiles, true, false,
                           false, monitors);
          }
        } catch(Throwable t) {
          exceptions.add(t);
        }

        // try partial compressed get
        try {
          if(!reverse) {
            getFile(stub, true, tempFiles, true, false, false, false);
          } else {
            getFileReverse(stub, unreliable, true, tempFiles, true, false,
                           false, monitors);
          }
        } catch(Throwable t) {
          exceptions.add(t);
        }
        
        // try compressed get using serializable stream
        try {
          if(!reverse) {
            getFile(stub, true, tempFiles, false, false, false, true);
          } else {
            getFileReverse(stub, unreliable, true, tempFiles, false, false,
                           true, monitors);
          }
        } catch(Throwable t) {
          exceptions.add(t);
        }
      }

      if(!doSkip) {
        // try uncompressed send (possibly aborted)
        try {
          if(!reverse) {
            sendFile(stub, sendFileName, false, false, doAbort, false);
          } else {
            sendFileReverse(stub, sendFileName, unreliable, false, false,
                            doAbort, false, monitors);
          }
        } catch(Throwable t) {
          exceptions.add(t);
        }

        // try compressed send (possibly aborted)
        try {
          if(!reverse) {
            sendFile(stub, sendFileName, true, false, doAbort, false);
          } else {
            sendFileReverse(stub, sendFileName, unreliable, true, false, doAbort,
                            false, monitors);
          }
        } catch(Throwable t) {
          exceptions.add(t);
        }

        if(!doAbort) {
          // try partial uncompressed send
          try {
            if(!reverse) {
              sendFile(stub, sendFileName, false, true, false, false);
            } else {
              sendFileReverse(stub, sendFileName, unreliable, false, true, false,
                              false, monitors);
            }
          } catch(Throwable t) {
            exceptions.add(t);
          }

          // try partial compressed send
          try {
            if(!reverse) {
              sendFile(stub, sendFileName, true, true, false, false);
            } else {
              sendFileReverse(stub, sendFileName, unreliable, true, true, false,
                              false, monitors);
            }
          } catch(Throwable t) {
            exceptions.add(t);
          }
        
          // try compressed send using serializable stream
          try {
            if(!reverse) {
              sendFile(stub, sendFileName, true, false, false, true);
            } else {
              sendFileReverse(stub, sendFileName, unreliable, true, false, false,
                              true, monitors);
            }
          } catch(Throwable t) {
            exceptions.add(t);
          }
        }
      }
      
      return exceptions;
    }
    
    public static void main(String args[])
    {
      String sendFileName = args[0];
      String host = (args.length < 2) ? null : args[1];
      try {
        Registry registry = LocateRegistry.getRegistry(host);
        RemoteFileServer stub = (RemoteFileServer)
          registry.lookup("RemoteFileServer");

        main(stub, sendFileName, null, false, false, false, false, false,
             null);
      
      } catch (Exception e) {
        LOG.debug("Client exception: " + e.toString());
        e.printStackTrace();
      }

    }
  
  }

  private static class UnreliableRemoteInputStreamServer
    extends RemoteStreamServer<RemoteInputStreamServer, RemoteInputStream>
    implements RemoteInputStream
  {
    private static final long serialVersionUID = 0L;

    private int _lastExPre = 1;
    private int _lastExPost = 1;
    private RemoteInputStreamServer _in;
    
    public UnreliableRemoteInputStreamServer(
      RemoteInputStreamServer in)
    {
      super(RemoteInputStreamServer.DUMMY_MONITOR);
      _in = in;
    }

    private void beUnreliable(boolean pre)
      throws RemoteException
    {
      if(pre) {
        if(((_lastExPre++) % 9) == 0) {
          throw new RemoteException("TESTING(PRE)");
        }
      } else {
        if(((_lastExPost++) % 3) == 0) {
          throw new RemoteException("TESTING(POST)");
        }
      }
    }
    
    public boolean usingGZIPCompression()
      throws IOException
    {
      beUnreliable(true);
      try {
        return _in.usingGZIPCompression();
      } finally {
        beUnreliable(false);
      }
    }
    
    public int available()
      throws IOException
    {
      beUnreliable(true);
      try {
        return _in.available();
      } finally {
        beUnreliable(false);
      }
    }

    public void close(boolean readSuccess)
      throws IOException
    {
      beUnreliable(true);
      try {
        _monitor.closed(_in, true);
        _in.close(readSuccess);
      } finally {
        beUnreliable(false);
      }
    }

    public byte[] readPacket(int packetId)
      throws IOException
    {
      beUnreliable(true);
      try {
        _monitor.bytesMoved(_in, 0, false);
        return _in.readPacket(packetId);
      } finally {
        beUnreliable(false);
      }
    }
             

    public long skip(long n, int skipId)
      throws IOException
    {
      beUnreliable(true);
      try {
        _monitor.bytesSkipped(_in, 0, false);
        return _in.skip(n, skipId);
      } finally {
        beUnreliable(false);
      }
    }

    @Override
    protected RemoteInputStreamServer getAsSub()
    { return _in.getAsSub(); }

    @Override
    public Class<RemoteInputStream> getRemoteClass() {
      return RemoteInputStream.class;
    }
    
    @Override
    protected Object getLock() { return _in.getLock(); }

    @Override
    protected void closeImpl(boolean writeSuccess)
      throws IOException
    { _in.closeImpl(writeSuccess); }
    
  }

  private static class UnreliableRemoteOutputStreamServer
    extends RemoteStreamServer<RemoteOutputStreamServer, RemoteOutputStream>
    implements RemoteOutputStream
  {
    private static final long serialVersionUID = 0L;

    private int _lastExPre = 1;
    private int _lastExPost = 1;
    private RemoteOutputStreamServer _out;
    
    public UnreliableRemoteOutputStreamServer(
      RemoteOutputStreamServer out)
    {
      super(RemoteOutputStreamServer.DUMMY_MONITOR);
      _out = out;
    }
    
    private void beUnreliable(boolean pre)
      throws RemoteException
    {
      if(pre) {
        if(((_lastExPre++) % 9) == 0) {
          throw new RemoteException("TESTING(PRE)");
        }
      } else {
        if(((_lastExPost++) % 3) == 0) {
          throw new RemoteException("TESTING(POST)");
        }
      }
    }
    
    public boolean usingGZIPCompression()
      throws IOException
    {
      beUnreliable(true);
      try {
        return _out.usingGZIPCompression();
      } finally {
        beUnreliable(false);
      }
    }
    
    public void close(boolean writeSuccess)
      throws IOException
    {
      beUnreliable(true);
      try {
        _monitor.closed(_out, true);
        _out.close(writeSuccess);
      } finally {
        beUnreliable(false);
      }
    }

    public void flush()
      throws IOException
    {
      beUnreliable(true);
      try {
        _out.flush();
      } finally {
        beUnreliable(false);
      }
    }

    public void writePacket(byte[] packet, int packetId)
      throws IOException
    {
      beUnreliable(true);
      try {
        _monitor.bytesMoved(_out, 0, false);
        _out.writePacket(packet, packetId);
      } finally {
        beUnreliable(false);
      }
    }
             
    @Override
    protected RemoteOutputStreamServer getAsSub()
    { return _out.getAsSub(); }
    
    @Override
    public Class<RemoteOutputStream> getRemoteClass() {
      return RemoteOutputStream.class;
    }
    
    @Override
    protected Object getLock() { return _out.getLock(); }

    @Override
    protected void closeImpl(boolean writeSuccess)
      throws IOException
    { _out.closeImpl(writeSuccess); }
    
  }

  private static class DummyIOStream
    implements RemoteOutputStream, RemoteInputStream
  {
    private int _numCloses;

    public boolean usingGZIPCompression()
      throws RemoteException
    {
      return false;
    }
  
    public void close(boolean transferSuccess)
      throws IOException, RemoteException
    {
      ++_numCloses;
    }

    public void flush()
      throws IOException, RemoteException
    {
    }

    public void writePacket(byte[] packet, int packetId)
      throws IOException, RemoteException
    {
    }

    public int available()
      throws IOException, RemoteException
    {
      return 0;
    }

    public byte[] readPacket(int packetId)
      throws IOException, RemoteException
    {
      return null;
    }

    public long skip(long n, int skipId)
      throws IOException, RemoteException
    {
      return 0;
    }
  }
  
}
