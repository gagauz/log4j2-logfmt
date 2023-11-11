package com.mgagauz.log4j2.logfmt;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.impl.JdkMapAdapterStringMap;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.lookup.JavaLookup;
import org.apache.logging.log4j.core.test.BasicConfigurationFactory;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LogfmtLayoutTest {
	static ConfigurationFactory configFactory = new BasicConfigurationFactory();

	@AfterClass
	public static void cleanupClass() {
		ConfigurationFactory.removeConfigurationFactory(configFactory);
		ThreadContext.clearAll();
	}

	@BeforeClass
	public static void setupClass() {
		ThreadContext.clearAll();
		ConfigurationFactory.setConfigurationFactory(configFactory);
		final LoggerContext ctx = LoggerContext.getContext();
		ctx.reconfigure();
	}

	LoggerContext ctx = LoggerContext.getContext();

	Logger rootLogger = this.ctx.getRootLogger();

	@Test
	public void testAdditionalFields() throws Exception {
		final LogfmtLayout layout = LogfmtLayout.newBuilder()
				.setAdditionalLabels(new CustomLabel[] {
						new CustomLabel("KEY1", "VALUE1"),
						new CustomLabel("KEY2", "${java:runtime}")
				})
				.setCharset(StandardCharsets.UTF_8)
				.setConfiguration(ctx.getConfiguration())
				.build();

		final Log4jLogEvent event = Log4jLogEvent.newBuilder()
				  .setLoggerName("a.B")
				  .setLevel(Level.DEBUG)
				  .setMessage(new SimpleMessage("M"))
				  .setThreadName("threadName")
				  .setTimeMillis(1)
				  .build();
		final String str = layout.toSerializable(event);
		assertThat(str)
				.contains("KEY1=VALUE1");
		assertThat(str)
				.contains("KEY2=" + new JavaLookup().getRuntime());
	}

	@Test
	public void testNanoTime() throws Exception {
		final LogfmtLayout layout = LogfmtLayout.newBuilder()
				.setCharset(StandardCharsets.UTF_8)
				.setConfiguration(ctx.getConfiguration())
				.setNanoTime(true)
				.build();

		final Log4jLogEvent event = Log4jLogEvent.newBuilder()
				.setLoggerName("a.B")
				.setLevel(Level.DEBUG)
				.setMessage(new SimpleMessage("M"))
				.setThreadName("threadName")
				.setTimeMillis(1L)
				.setNanoTime(2L)
				.build();
		final String str = layout.toSerializable(event);
		assertThat(str)
				.contains("timestamp=2");
	}

	@Test
	public void testStackTrace() throws Exception {
		final LogfmtLayout layout = LogfmtLayout.newBuilder()
				.setCharset(StandardCharsets.UTF_8)
				.setConfiguration(ctx.getConfiguration())
				.setIncludeStacktrace(true)
				.build();

		final Log4jLogEvent event = Log4jLogEvent.newBuilder()
				.setLoggerName("a.B")
				.setLoggerFqcn("f.q.c.n")
				.setLevel(Level.DEBUG)
				.setMessage(new SimpleMessage("M"))
				.setThreadName("threadName")
				.setTimeMillis(1L)
				.setThrown(new Exception("Error\"X\""))
				.build();
		final String str = layout.toSerializable(event);
		assertThat(str)
				.contains("exception=\"java.lang.Exception: Error\\\"X\\\"");
	}

	@Test
	public void testBasic() throws Exception {
		final LogfmtLayout layout = LogfmtLayout.newBuilder()
				.setCharset(StandardCharsets.UTF_8)
				.setConfiguration(ctx.getConfiguration())
				.build();

		final Log4jLogEvent event = Log4jLogEvent.newBuilder()
				.setLoggerName("a.B")
				.setLoggerFqcn("f.q.c.n")
				.setLevel(Level.DEBUG)
				.setMessage(new SimpleMessage("test \"escape\""))
				.setThreadId(12L)
				.setThreadName("th1")
				.setTimeMillis(1L)
				.setContextData(new JdkMapAdapterStringMap(Map.of("KEY1", "VALUE1")))
				.setThrown(new Exception("MSG"))
				.build();
		final String str = layout.toSerializable(event);
		assertThat(str)
				.contains("logger=a.B")
				.contains("level=DEBUG")
				.contains("thread_name=th1")
				.contains("thread_id=12")
				.contains("message=\"test \\\"escape\\\"\"")
				.doesNotContain("exception=")
				.doesNotContain("KEY1=");
	}

	@Test
	public void testContext() throws Exception {
		final LogfmtLayout layout = LogfmtLayout.newBuilder()
				.setCharset(StandardCharsets.UTF_8)
				.setConfiguration(ctx.getConfiguration())
				.setIncludeContext(true)
				.build();

		final Log4jLogEvent event = Log4jLogEvent.newBuilder()
				.setLoggerName("a.B")
				.setLoggerFqcn("f.q.c.n")
				.setLevel(Level.DEBUG)
				.setMessage(new SimpleMessage("M"))
				.setTimeMillis(1L)
				.setContextData(new JdkMapAdapterStringMap(Map.of("KEY1", "VALUE1")))
				.build();
		final String str = layout.toSerializable(event);
		assertThat(str)
				.contains("_KEY1=VALUE1");
	}

	@Test
	public void testContextKeyPrefix() throws Exception {
		final LogfmtLayout layout = LogfmtLayout.newBuilder()
				.setCharset(StandardCharsets.UTF_8)
				.setConfiguration(ctx.getConfiguration())
				.setIncludeContext(true)
				.setContextKeyPrefix("ctx_")
				.build();

		final Log4jLogEvent event = Log4jLogEvent.newBuilder()
				.setLoggerName("a.B")
				.setLoggerFqcn("f.q.c.n")
				.setLevel(Level.DEBUG)
				.setMessage(new SimpleMessage("M"))
				.setTimeMillis(1L)
				.setContextData(new JdkMapAdapterStringMap(Map.of("KEY1", "VALUE1")))
				.build();
		final String str = layout.toSerializable(event);
		assertThat(str)
				.contains("ctx_KEY1=VALUE1");
	}

}