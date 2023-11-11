# log4j2-logfmt
Logfmt layout for log4j2

## Usage
Sample xml configuration:

	<?xml version="1.0" encoding="UTF-8"?>
	<Configuration status="WARN" packages="com.mgagauz.log4j2.logfmt"> <!-- add package for additional lookups -->
		<Appenders>
			<RollingFile name="File" fileName="server.log"
				filePattern="server-%d{yyyy-MM-dd}.log">
				<LogfmtLayout includeContext="true">
					<!-- Custom static label -->
					<CustomLabel name="app" value="${spring:spring.application.name}" />
				</LogfmtLayout>
				...
			</RollingFile>
		</Appenders>
		...
	</Configuration>

The output line will include the following fields:

|Name|Description|
|----|-----------|
|timestamp|epoch time in milliseconds (see nanoTime)|
|level|Log level|
|logger|Logger name| 
|thread_id| As it states | 
|thread_name| As it states  | 
|message|Escaped log message|
|exception|Escaped exception stacktrace (see includeStacktrace)|

Also there will be additional static labels and context data (see includeContext, contextKeyPrefix)

	timestamp=1699653906166 level=INFO logger=com.example.logging.Application thread_id=18 thread_name=http-nio-8080-exec-5 message="Sample output message" exception= _traceId=654ea91dfc5415305de24d16b919022e app=application-name

### Layout configuration attributes:
|Attribute name| Description|Default|
|--------------|------------|-------|
|`includeContext`| Include context values (MDC) in output, in the following format: `_key1=value1 _key2=value2`| false |
|`contextKeyPrefix`| The context key in output will be prepended with this string | _ |
|`includeStacktrace`| Include stacktrace in output | false |
|`nanoTime`| Output timestamp as epoch time in nanoseconds instead of milliseconds | false |

### Addiotional fields
Additional static fields can be introduced by adding children elements to the Logfmtlayout node:
```
<LogfmtLayout includeContext="true">
  <!-- Custom static labels -->
  <CustomLabel name="text" value="some_text" />
  <CustomLabel name="sys_prop" value="${sys:some_system_property}" />
  <CustomLabel name="spring_prop" value="${spring:some_spring_prop}" />
</LogfmtLayout>
```
