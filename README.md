[![Build](https://github.com/avaje/slf4j-jdk-platform-logging/actions/workflows/build.yml/badge.svg)](https://github.com/avaje/slf4j-jdk-platform-logging/actions/workflows/build.yml)

# jsl-slf4j

## for SLF4J 1.7.x

Provides adapter for Java platform logging to **SLF4J 1.7.x**.

Adding this dependency to your project means that Java `System.Logger`
log messages will go to a `SLF4J 1.7 Logger` (and then onto whatever logging
backend your application has configured).

This works by registered a `System.LoggerFinder` that provides a `System.Logger`
that is an adapter to `SLF4J 1.7 Logger`.

## Why not part of SLF4J project?

Ideally this would be part of the SLF4J project and at some point it might be.
At the moment [Ceki is not keen](https://twitter.com/ceki/status/1483198536107839492)
so until that changes we have `io.avaje : jsl-slf4j` for use with SLF4J 1.7.x.


## SLF4J 2.x

Applications using SLF4J 2.x will NOT use this artifact but instead use the
Java platform logging adapter provided by SLF4J.

That is, SLF4J 2.x provides an adapter for Java platform logging but
this is **not compatible with SLF4J 1.7.x**. People that want to use Java
platform logger with SLF4J 1.7 will need to either:
- Use this `jsl-slf4j` adapter or a similar adapter
- Have Java platform logging go to JUL (which it does by default)
  and then use the JUL-SLF4J bridge to forward the logging events to the
  logging backend.

The SLF4J 2.x provided adapter for Java platform logging is at:
[github / qos-ch / slf4j / slf4j-jdk-platform-logging](https://github.com/qos-ch/slf4j/tree/master/slf4j-jdk-platform-logging)
