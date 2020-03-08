# HTTP Access Log Valve

![build status](https://github.com/solence/HttpAccessLogValve/workflows/Java%20CI/badge.svg)

Modern deployments often use central log aggregators instead of individual log files, especially with containers. By default, Tomcat/TomEE only writes the access logs to a file, so an additional process needs to monitor the file and forward the data to an aggregator.

This library provides a log valve for Tomcat/TomEE to send access log data directly to an HTTP endpoint. This makes Tomcat/TomEE deployments in containers much easier, because there is no need for a forwarder process. In addition, no parsing configuration for raw data is necessary, because the data is sent in JSON format.


## How to use

Download the latest release and place the JAR in `$CATALINA_HOME/lib` or `$CATALINA_BASE/lib`, depending whether it should be used for a single or multiple instances. This library is designed to not depend on any other libraries to not interfere with the applications deployed on the Tomcat/TomEE.

To activate this log valve, replace the existing log valve configuration or add a new entry in `$CATALINA_BASE/conf/server.xml` in the `<Host>` section:

```xml
<Valve className="de.solence.valves.HttpAccessLogValve" />
```

The necessary configuration needs to be provided by JVM parameters or environemnt variables.

## Configuration

The following table lists all possible configuration parameters. They can either be set by JVM parameter or environment variable. If both are set, the JVM parameter is used.

### Mandatory parameters

|JVM Parameter|Environment variable|Description|
|-|-|-|
|httpaccesslogvalve.url|HTTPACCESSLOGVALVE_URL|The URL of the HTTP/HTTPS endpoint to sent to.|
|httpaccesslogvalve.token|HTTPACCESSLOGVALVE_TOKEN|The token to use for authentication against the endpoint.|

### Optional parameters

|JVM Parameter|Environment variable|Description|
|-|-|-|
|httpaccesslogvalve.host|HTTPACCESSLOGVALVE_HOST|The name of the host from which the data is sent. Defaults to the local hostname.|
|httpaccesslogvalve.index|HTTPACCESSLOGVALVE_INDEX|TODO|
|httpaccesslogvalve.source|HTTPACCESSLOGVALVE_SOURCE|TODO|
|httpaccesslogvalve.queue|HTTPACCESSLOGVALVE_QUEUE|The length of the queue of log events waiting to be sent. A longer queue is more likely to guarantee delivery of log events, when the network is slow or the endpoint unstable. It also increases memory consumtion. Log events will be lost when the queue is full. Defaults to 1000. |
|httpaccesslogvalve.timeout|HTTPACCESSLOGVALVE_TIMEOUT|The timeout used when connecting to the endpoint. Defaults to 1 minute|
|httpaccesslogvalve.shutdowntimeout|HTTPACCESSLOGVALVE_SHUTDOWNTIMEOUT|The time to wait after a shutdown has been initiated, until log events still in the queue have been sent. Defaults to 30 seconds|

### Example with JVM parameters

```sh
export CATALINA_OPTS="${CATALINA_OPTS} -Dhttpaccesslogvalve.url=http://localhost:8088/services/collector"
export CATALINA_OPTS="${CATALINA_OPTS} -Dhttpaccesslogvalve.token=123-456-789"
```

### Example with environment variables

```sh
export HTTPACCESSLOGVALVE_URL=http://localhost:8088/services/collector
export HTTPACCESSLOGVALVE_TOKEN=123-456-789
```

## License

MIT License. See [LICENSE.md](./LICENSE.md).

## Trademarks

Splunk® is a registered trademark of Splunk Inc.