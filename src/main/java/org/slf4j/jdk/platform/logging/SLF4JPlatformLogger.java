/**
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
package org.slf4j.jdk.platform.logging;

import static java.util.Objects.requireNonNull;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.slf4j.Logger;

/**
 * Adapts {@link Logger} to {@link System.Logger}.
 */
class SLF4JPlatformLogger implements System.Logger {

    private final Logger slf4jLogger;

    public SLF4JPlatformLogger(Logger logger) {
        this.slf4jLogger = requireNonNull(logger);
    }

    @Override
    public String getName() {
        return slf4jLogger.getName();
    }

    @Override
    public boolean isLoggable(Level jplLevel) {
        if (jplLevel == Level.ALL) {
            return true;
        }
        if (jplLevel == Level.OFF) {
            return false;
        }
        return isEnabledForLevel(toSLF4JLevel(jplLevel));
    }

    /**
     * Returns whether this Logger is enabled for a given {@link Level}.
     */
    private boolean isEnabledForLevel(org.slf4j.event.Level level) {
        switch (level) {
            case TRACE:
                return slf4jLogger.isTraceEnabled();
            case DEBUG:
                return slf4jLogger.isDebugEnabled();
            case INFO:
                return slf4jLogger.isInfoEnabled();
            case WARN:
                return slf4jLogger.isWarnEnabled();
            case ERROR:
                return slf4jLogger.isErrorEnabled();
            default:
                throw new IllegalArgumentException("Level [" + level + "] not recognized.");
        }
    }

    /**
     * Transform a {@link Level} to {@link org.slf4j.event.Level}.
     * 
     * This method assumes that Level.ALL or Level.OFF never reach this method.
     */
    private org.slf4j.event.Level toSLF4JLevel(Level jplLevel) {
        switch (jplLevel) {
        case TRACE:
            return org.slf4j.event.Level.TRACE;
        case DEBUG:
            return org.slf4j.event.Level.DEBUG;
        case INFO:
            return org.slf4j.event.Level.INFO;
        case WARNING:
            return org.slf4j.event.Level.WARN;
        case ERROR:
            return org.slf4j.event.Level.ERROR;
        default:
            reportUnknownLevel(jplLevel);
            return org.slf4j.event.Level.TRACE;
        }
    }

    @Override
    public void log(Level jplLevel, ResourceBundle bundle, String msg, Throwable thrown) {
        log(jplLevel, bundle, msg, thrown, (Object[]) null);
    }

    @Override
    public void log(Level jplLevel, ResourceBundle bundle, String format, Object... params) {
        log(jplLevel, bundle, format, null, params);
    }

    /**
     * Single point of processing taking all possible paramets.
     */
    private void log(Level jplLevel, ResourceBundle bundle, String msg, Throwable thrown, Object... params) {
        if (jplLevel == Level.OFF) {
            return;
        }
        if (jplLevel == Level.ALL) {
            performLog(org.slf4j.event.Level.TRACE, bundle, msg, thrown, params);
            return;
        }

        performLog(toSLF4JLevel(jplLevel), bundle, msg, thrown, params);
    }

    private void performLog(org.slf4j.event.Level slf4jLevel, ResourceBundle bundle, String msg, Throwable thrown, Object... params) {
        switch (slf4jLevel) {
            case ERROR: {
                if (slf4jLogger.isErrorEnabled()) {
                    slf4jLogger.error(message(bundle, msg, params), thrown);
                }
                break;
            }
            case WARN: {
                if (slf4jLogger.isWarnEnabled()) {
                    slf4jLogger.warn(message(bundle, msg, params), thrown);
                }
                break;
            }
            case INFO: {
                if (slf4jLogger.isInfoEnabled()) {
                    slf4jLogger.info(message(bundle, msg, params), thrown);
                }
                break;
            }
            case DEBUG: {
                if (slf4jLogger.isDebugEnabled()) {
                    slf4jLogger.debug(message(bundle, msg, params), thrown);
                }
                break;
            }
            case TRACE: {
                if (slf4jLogger.isTraceEnabled()) {
                    slf4jLogger.trace(message(bundle, msg, params), thrown);
                }
                break;
            }
        }
    }

    private String message(ResourceBundle bundle, String msg, Object[] params) {
        String message = resourceStringOrMessage(bundle, msg);
        if (params != null && params.length > 0) {
            message = String.format(message, params);
        }
        return message;
    }

    private void reportUnknownLevel(Level jplLevel) {
        String message = "Unknown log level [" + jplLevel + "]";
        IllegalArgumentException iae = new IllegalArgumentException(message);
        org.slf4j.helpers.Util.report("Unsupported log level", iae);
    }

    private static String resourceStringOrMessage(ResourceBundle bundle, String msg) {
        if (bundle == null || msg == null) {
            return msg;
        }
        // ResourceBundle::getString throws:
        //
        // * NullPointerException for null keys
        // * ClassCastException if the message is no string
        // * MissingResourceException if there is no message for the key
        //
        // Handle all of these cases here to avoid log-related exceptions from crashing the JVM.
        try {
            return bundle.getString(msg);
        } catch (MissingResourceException ex) {
            return msg;
        } catch (ClassCastException ex) {
            return bundle.getObject(msg).toString();
        }
    }

}
