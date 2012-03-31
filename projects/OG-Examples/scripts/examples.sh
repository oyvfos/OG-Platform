#!/bin/bash

BASENAME=${0##*/}                                                                                                               
COMPONENT=${BASENAME%.sh}                                                                                                       
BASEDIR="$(dirname $(dirname $(readlink -f $0)))"                                                                               
SCRIPTDIR=${BASEDIR}/scripts
PROJECT=og-examples
PROJECTJAR=og-examples.jar

cd "${BASEDIR}" || exit 1

if [ ! -d ${BASEDIR}/temp/hsqldb ]; then
  echo HSQL database directory not found, invoking init-examples-db.sh script to create and populate database...
  ${BASEDIR}/scripts/init-examples-db.sh
fi

. ${SCRIPTDIR}/componentserver-init-utils.sh

# Read default configs
load_default_config

# Component specific default configs
CONFIG=classpath:fullstack/example-bin.properties
LOGBACK_CONFIG=examples-logback.xml

# User customizations
load_component_config ${PROJECT} ${COMPONENT}

CLASSPATH=$(build_classpath)
if [ -f ${PROJECTJAR} ]; then
  CLASSPATH=${PROJECTJAR}:${CLASSPATH}
else
  CLASSPATH=build/${PROJECTJAR}:${CLASSPATH}
fi
CLASSPATH=config:${CLASSPATH}

RETVAL=0
case "$1" in
  start)
    start
    ;;
  debug)
    debug
    ;;
  stop)
    stop
    ;;
  status)
    status
    ;;
  restart|reload)
    stop
    start
    ;;
  *)
    echo "Usage: $0 {start|stop|restart|status|reload|debug}"
esac

exit ${RETVAL}