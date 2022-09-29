#! /bin/sh

# Shell script wrapper around the fop program,
# Copyright 2008 by Vincent Fourmond <fourmond@debian.org>
#
# Licensed under the same terms as fop itself, that is under
# the conditions of the Apache 2 licence.

# Include the wrappers utility script
. /usr/lib/java-wrappers/java-wrappers.sh

# comment this line if you want fop to run without headless property,
# or write a line containing
#  HEADLESS=
# in your fop configuration file.
HEADLESS=-Djava.awt.headless=true

# the following lines are copied from upstream `fop` wrapper:
#
# The default commons logger for JDK1.4 is JDK1.4Logger.
# To use a different logger, uncomment the one desired below
# LOGCHOICE=-Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.NoOpLog
LOGCHOICE=-Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog
# LOGCHOICE=-Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.Log4JLogger

# Logging levels
# Below option is only if you are using SimpleLog instead of the default JDK1.4 Logger.
# To set logging levels for JDK 1.4 Logger, edit the %JAVA_HOME%/JRE/LIB/logging.properties
# file instead.
# Possible SimpleLog values:  "trace", "debug", "info" (default), "warn", "error", or "fatal".
LOGLEVEL=-Dorg.apache.commons.logging.simplelog.defaultlog=INFO

for cf in /etc/fop.conf.d/*.conf; do
    if [ -f $cf ]; then
        . $cf;
    fi
done

# Load system-wide configuration, if any
if [ -f /etc/fop.conf ]; then
    . /etc/fop.conf
fi

# Load user's preferences, if any
if [ -f "$HOME/.foprc" ]; then
    . $HOME/.foprc
fi

# We prefer to use openjdk or Sun's java if available
find_java_runtime openjdk sun  || find_java_runtime

find_jars commons-io serializer xalan2 xml-apis
find_jars batik-all commons-logging xercesImpl xmlgraphics-commons
find_jars xml-apis-ext fontbox2

# We load the hyphenation jar at the request of the user.
if [ "$FOP_HYPHENATION_PATH" ]; then
    find_jars $FOP_HYPHENATION_PATH
fi
find_jars /usr/share/fop/fop-hyph.jar
find_jars fop

run_java $HEADLESS $LOGCHOICE $LOGLEVEL org.apache.fop.cli.Main "$@"
