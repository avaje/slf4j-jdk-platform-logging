/*
 * Copyright (c) 2004-2021 QOS.ch
 * All rights reserved.
 *
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 *
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 *
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package io.avaje.logging.slf4j.test;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.System.LoggerFinder;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The present test is fragile in the sense that it sets up SimpleLogger
 * with a StringPrintStream and reverts to the old stream when done.
 * <p>
 * Any tests running simultaneously (and using SimpleLogger) will be affected
 * by this. Moreover, since SimpleLogger is initialized by the call to LoggerFactory
 * and tests also using LoggerFactory will also be affected.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
class SLF4JPlatformLoggingTest {

  static final String PREFIX = "org.slf4j.simpleLogger.";
  static final String SIMPLE_LOGGER_FILE_PROPERTY = PREFIX + "logFile";
  static final String SIMPLE_LOGGER_THREAD_NAME_PROPERTY = PREFIX + "showThreadName";

  static final String EXPECTED_FINDER_CLASS = "io.avaje.logging.slf4j.SLF4JSystemLoggerFinder";


  static final PrintStream oldErr = System.err;
  static StringPrintStream SPS = new StringPrintStream(oldErr, false);

  @BeforeAll
  static void beforeClass() {
    System.setErr(SPS);
    //System.setProperty(SIMPLE_LOGGER_FILE_PROPERTY, targetFile);
    System.setProperty(SIMPLE_LOGGER_THREAD_NAME_PROPERTY, "false");
  }

  @AfterAll
  static void afterClass() {
    System.setErr(oldErr);
    System.clearProperty(SIMPLE_LOGGER_THREAD_NAME_PROPERTY);
  }

  @AfterEach
  void tearDown() {
    SPS.stringList.clear();
  }

  @Test
  void smoke() {
    LoggerFinder finder = System.LoggerFinder.getLoggerFinder();
    assertEquals(EXPECTED_FINDER_CLASS, finder.getClass().getName());
    Logger systemLogger = finder.getLogger("smoke", null);
    systemLogger.log(Level.INFO, "hello");
    systemLogger.log(Level.INFO, "hello {0} {1}", "world", "again");

    List<String> results = SPS.stringList;
    assertEquals(2, results.size());
    assertEquals("INFO smoke - hello", results.get(0));
    assertEquals("INFO smoke - hello world again", results.get(1));
  }

  @Test
  void throwTest() {
    LoggerFinder finder = System.LoggerFinder.getLoggerFinder();
    assertEquals(EXPECTED_FINDER_CLASS, finder.getClass().getName());

    Logger systemLogger = finder.getLogger("throwTest", null);
    systemLogger.log(Level.INFO, "we have a problem", new Exception());

    List<String> results = SPS.stringList;
    //INFO throwTest - a problem
    //java.lang.Exception
    //        at org.slf4j.jdk.platform.logging/org.slf4j.jdk.platform.logging.SLF4JPlatformLoggingTest.throwTest(SLF4JPlatformLoggingTest.java:92)

    assertEquals("INFO throwTest - we have a problem", results.get(0));
    assertEquals(Exception.class.getName(), results.get(1));
    assertTrue(results.get(2).contains("at "));
    assertTrue(results.get(2).contains(this.getClass().getName()));
  }

}
