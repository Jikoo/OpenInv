package com.lishid.openinv.util;

import org.slf4j.Marker;
import org.slf4j.helpers.LegacyAbstractLogger;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.helpers.NormalizedParameters;
import org.slf4j.spi.LocationAwareLogger;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * An adapter for wrapping a {@link java.util.logging.Logger} as a {@link org.slf4j.Logger}.
 * <br>Largely based on {@code JDK14LoggerAdapter}, which is not present at runtime.
 */
public class JulLoggerAdapter extends LegacyAbstractLogger implements LocationAwareLogger {

  private final Logger wrapped;

  public JulLoggerAdapter(Logger wrapped) {
    this.wrapped = wrapped;
    this.name = wrapped.getName();
  }

  @Override
  protected String getFullyQualifiedCallerName() {
    return JulLoggerAdapter.class.getName();
  }

  @Override
  protected void handleNormalizedLoggingCall(
      org.slf4j.event.Level slf4jLevel,
      Marker marker,
      String msg,
      Object[] args,
      Throwable thrown
  ) {
    Level level = fromSlf4jLevel(slf4jLevel);

    if (wrapped.isLoggable(level)) {
      normalizedLog(getFullyQualifiedCallerName(), level, msg, args, thrown);
    }
  }

  private void normalizedLog(String fqcn, Level level, String msg, Object[] args, Throwable thrown) {
    String formatted = MessageFormatter.basicArrayFormat(msg, args);
    LogRecord logRecord = new LogRecord(level, formatted);
    logRecord.setLoggerName(getName());
    logRecord.setThrown(thrown);

    addSource(fqcn, logRecord);

    wrapped.log(logRecord);
  }

  private void addSource(String fqcn, LogRecord logRecord) {
    // TODO stackwalker?
    StackTraceElement[] trace = new Throwable().getStackTrace();
    int maxElements = 12;
    int lastIgnored = maxElements;
    // Start from 2; 0 is above and 1 is caller of internal method.
    for (int i = 2; i < maxElements; ++i) {
      if (isIgnored(trace[i].getClassName(), fqcn)) {
        lastIgnored = i;
      }
    }

    if (lastIgnored < maxElements - 1) {
      StackTraceElement caller = trace[lastIgnored + 1];
      logRecord.setSourceClassName(caller.getClassName());
      logRecord.setSourceMethodName(caller.getMethodName());
    }
  }

  private boolean isIgnored(String className, String fqcn) {
    if (className.equals(fqcn)) {
      return true;
    }
    // Ignore slf4j classes - they shouldn't be the source.
    if (className.startsWith("org.slf4j.")) {
      return true;
    }
    return className.equals(getFullyQualifiedCallerName());
  }

  @Override
  public void log(Marker marker, String callerFqn, int levelInt, String msg, Object[] args, Throwable thrown) {
    Level level = fromSlf4jLevel(org.slf4j.event.Level.intToLevel(levelInt));

    if (!wrapped.isLoggable(level)) {
      return;
    }

    NormalizedParameters params = NormalizedParameters.normalize(msg, args, thrown);
    normalizedLog(callerFqn, level, params.getMessage(), params.getArguments(), params.getThrowable());
  }

  private Level fromSlf4jLevel(org.slf4j.event.Level level) {
    return switch (level) {
      case TRACE -> Level.FINEST;
      case DEBUG -> Level.FINE;
      case INFO -> Level.INFO;
      case WARN -> Level.WARNING;
      case ERROR -> Level.SEVERE;
    };
  }

  @Override
  public boolean isTraceEnabled() {
    return wrapped.isLoggable(Level.FINEST);
  }

  @Override
  public boolean isDebugEnabled() {
    return wrapped.isLoggable(Level.FINE);
  }

  @Override
  public boolean isInfoEnabled() {
    return wrapped.isLoggable(Level.INFO);
  }

  @Override
  public boolean isWarnEnabled() {
    return wrapped.isLoggable(Level.WARNING);
  }

  @Override
  public boolean isErrorEnabled() {
    return wrapped.isLoggable(Level.SEVERE);
  }

}
