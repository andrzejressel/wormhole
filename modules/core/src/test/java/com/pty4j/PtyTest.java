/*
 * JPty - A small PTY interface for Java.
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.pty4j;


import com.google.common.base.Ascii;
import com.pty4j.unix.PtyHelpers;
import com.sun.jna.Platform;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Test cases for {@link PtyHelpers}.
 */
public class PtyTest {

  private static final int WAIT_TIMEOUT_SECONDS = TestUtil.getTestWaitTimeoutSeconds();

  private static @NotNull String convertInvisibleChars(@NotNull String s) {
    return s.replace("\n", "\\n").replace("\r", "\\r").replace("\b", "\\b")
      .replace("\u001b", "ESC")
      .replace(String.valueOf((char)Ascii.BEL), "BEL");
  }

  public static void writeToStdinAndFlush(@NotNull PtyProcess process, @NotNull String input,
                                          boolean hitEnter) throws IOException {
    String text = hitEnter ? input + (char) process.getEnterKeyCode() : input;
    process.getOutputStream().write(text.getBytes(StandardCharsets.UTF_8));
    process.getOutputStream().flush();
  }

  public static void assertProcessTerminatedNormally(@NotNull Process process) throws InterruptedException {
    assertProcessTerminated(0, process);
  }

  public static void assertProcessTerminatedBySignal(int signalNumber, @NotNull Process process) throws InterruptedException {
    assertProcessTerminated(128 + signalNumber, process);
  }

  private static void assertProcessTerminatedAbnormally(@NotNull Process process) throws InterruptedException {
    assertProcessTerminated(Integer.MIN_VALUE, process);
  }

  public static void assertProcessTerminated(int expectedExitCode, @NotNull Process process) throws InterruptedException {
    assertTrue("Process hasn't been terminated within timeout", process.waitFor(WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS));
    int exitValue = process.exitValue();
    if (expectedExitCode == Integer.MIN_VALUE) {
      assertTrue("Process terminated with exit code " + exitValue + ", non-zero exit code was expected", exitValue != 0);
    }
    else {
      assertEquals(expectedExitCode, exitValue);
    }
  }

  public static void assertAlive(@NotNull Process process) {
    try {
      int exitValue = process.exitValue();
      fail("process has terminated unexpectedly with exit code " + exitValue);
    }
    catch (Exception ignored) {
    }
  }

  private static @NotNull Map<String, String> mergeCustomAndSystemEnvironment(@NotNull Map<String, String> customEnv) {
    Map<String, String> env = new HashMap<>(System.getenv());
    env.putAll(customEnv);
    return env;
  }

  public static @NotNull Gobbler startStdoutGobbler(@NotNull PtyProcess process) {
    return new Gobbler(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8), null, process);
  }

  public static @NotNull Gobbler startStderrGobbler(@NotNull PtyProcess process) {
    return new Gobbler(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8), null, process);
  }

  @NotNull
  private static Gobbler startReader(@NotNull InputStream in, @Nullable CountDownLatch latch) {
    return new Gobbler(new InputStreamReader(in, StandardCharsets.UTF_8), latch, null);
  }

  public static class Gobbler implements Runnable {
    private final Reader myReader;
    private final CountDownLatch myLatch;
    private final @Nullable PtyProcess myProcess;
    private final StringBuffer myOutput;
    private final Thread myThread;
    private final BlockingQueue<String> myLineQueue = new LinkedBlockingQueue<>();
    private final ReentrantLock myNewTextLock = new ReentrantLock();
    private final Condition myNewTextCondition = myNewTextLock.newCondition();

    private Gobbler(@NotNull Reader reader, @Nullable CountDownLatch latch, @Nullable PtyProcess process) {
      myReader = reader;
      myLatch = latch;
      myProcess = process;
      myOutput = new StringBuffer();
      myThread = new Thread(this, "Stream gobbler");
      myThread.start();
    }

    @Override
    public void run() {
      try {
        char[] buf = new char[32 * 1024];
        String linePrefix = "";
        while (true) {
          int count = myReader.read(buf);
          if (count <= 0) {
            myReader.close();
            return;
          }
          myOutput.append(buf, 0, count);
          linePrefix = processLines(linePrefix + new String(buf, 0, count));
          myNewTextLock.lock();
          try {
            myNewTextCondition.signalAll();
          }
          finally {
            myNewTextLock.unlock();
          }
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      } finally {
        if (myLatch != null) {
          myLatch.countDown();
        }
      }
    }

    @NotNull
    private String processLines(@NotNull String text) {
      int start = 0;
      while (true) {
        int end = text.indexOf('\n', start);
        if (end < 0) {
          return text.substring(start);
        }
        myLineQueue.add(text.substring(start, end + 1));
        start = end + 1;
      }
    }

    @NotNull
    public String getOutput() {
      return myOutput.toString();
    }

    public void awaitFinish() throws InterruptedException {
      myThread.join(TimeUnit.SECONDS.toMillis(WAIT_TIMEOUT_SECONDS));
    }

    @Nullable
    public String readLine() throws InterruptedException {
      return readLine(TimeUnit.SECONDS.toMillis(WAIT_TIMEOUT_SECONDS));
    }

    @Nullable
    public String readLine(long awaitTimeoutMillis) throws InterruptedException {
      String line = myLineQueue.poll(awaitTimeoutMillis, TimeUnit.MILLISECONDS);
      if (line != null) {
        line = cleanWinText(line);
      }
      return line;
    }

    private boolean awaitTextEndsWith(@NotNull String suffix, long timeoutMillis) {
      long startTimeMillis = System.currentTimeMillis();
      long nextTimeoutMillis = timeoutMillis;
      do {
        myNewTextLock.lock();
        try {
          try {
            if (endsWith(suffix)) {
              return true;
            }
            myNewTextCondition.await(nextTimeoutMillis, TimeUnit.MILLISECONDS);
            if (endsWith(suffix)) {
              return true;
            }
          }
          catch (InterruptedException e) {
            e.printStackTrace();
            return false;
          }
        }
        finally {
          myNewTextLock.unlock();
        }
        nextTimeoutMillis = startTimeMillis + timeoutMillis - System.currentTimeMillis();
      } while (nextTimeoutMillis >= 0);
      return false;
    }

    private boolean endsWith(@NotNull String suffix) {
      String text = cleanWinText(myOutput.toString());
      return text.endsWith(suffix);
    }

    @NotNull
    private static String cleanWinText(@NotNull String text) {
      if (Platform.isWindows()) {
        text = text.replace("\u001B[0m", "").replace("\u001B[m", "").replace("\u001B[0K", "").replace("\u001B[K", "")
          .replace("\u001B[?25l", "").replace("\u001b[?25h", "").replaceAll("\u001b\\[\\d*G", "")
                .replace("\u001b[2J", "").replaceAll("\u001B\\[\\d*;?\\d*H", "")
                .replaceAll("\u001B\\[\\d*X", "")
                .replaceAll(" *\\r\\n", "\r\n").replaceAll(" *$", "").replaceAll("(\\r\\n)+\\r\\n$", "\r\n");
        int oscInd = 0;
        do {
          oscInd = text.indexOf("\u001b]0;", oscInd);
          int bellInd = oscInd >= 0 ? text.indexOf(Ascii.BEL, oscInd) : -1;
          if (bellInd >= 0) {
            text = text.substring(0, oscInd) + text.substring(bellInd + 1);
          }
        } while (oscInd >= 0);
        int backspaceInd = text.indexOf(Ascii.BS);
        while (backspaceInd >= 0) {
          text = text.substring(0, Math.max(0, backspaceInd - 1)) + text.substring(backspaceInd + 1);
          backspaceInd = text.indexOf(Ascii.BS);
        }
      }
      return text;
    }

    public void assertEndsWith(@NotNull String expectedSuffix) {
      assertEndsWith(expectedSuffix, TimeUnit.SECONDS.toMillis(WAIT_TIMEOUT_SECONDS));
    }

    private void assertEndsWith(@NotNull String expectedSuffix, long timeoutMillis) {
      boolean ok = awaitTextEndsWith(expectedSuffix, timeoutMillis);
      if (!ok) {
        String output = getOutput();
        String cleanOutput = cleanWinText(output);
        String actual = cleanOutput.substring(Math.max(0, cleanOutput.length() - expectedSuffix.length()));
        if (expectedSuffix.equals(actual)) {
          fail("awaitTextEndsWith could detect suffix within timeout, but it is there");
        }
        expectedSuffix = convertInvisibleChars(expectedSuffix);
        actual = convertInvisibleChars(actual);
        int lastTextSize = 1000;
        String lastText = output.substring(Math.max(0, output.length() - lastTextSize));
        if (output.length() > lastTextSize) {
          lastText = "..." + lastText;
        }
        assertEquals("Unmatched suffix (trailing text: " + convertInvisibleChars(lastText) +
          (myProcess != null ? ", " + getProcessStatus(myProcess) : "") + ")", expectedSuffix, actual);
        fail("Unexpected failure");
      }
    }
  }

  private static @NotNull String getProcessStatus(@NotNull PtyProcess process) {
    boolean running = process.isAlive();
    Integer exitCode = getExitCode(process);
    if (running && exitCode == null) {
      return "alive process";
    }
    return "process running:" + running + ", exit code:" + (exitCode != null ? exitCode : "N/A");
  }

  private static @Nullable Integer getExitCode(@NotNull PtyProcess process) {
    Integer exitCode = null;
    try {
      exitCode = process.exitValue();
    }
    catch (IllegalThreadStateException ignored) {
    }
    return exitCode;
  }

  private static void fail(String message) {
    throw new RuntimeException("Test failed: " + message);
  }

  private static void assertTrue(String message, boolean check) {
    if (!check) {
      fail(message);
    }
  }

  private static void assertEquals(String message, Object expected, Object actual) {
    assertTrue(message, Objects.equals(expected, actual));
  }

  private static void assertEquals(Object expected, Object actual) {
    assertTrue(expected + " != " + actual, Objects.equals(expected, actual));
  }

}
