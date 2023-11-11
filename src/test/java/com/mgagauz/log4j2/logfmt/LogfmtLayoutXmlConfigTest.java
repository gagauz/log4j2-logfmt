package com.mgagauz.log4j2.logfmt;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LogfmtLayoutXmlConfigTest {

	private static final String LOG_FILE_NAME = "test-1.log";

	@AfterClass
	public static void cleanupClass() {
		ThreadContext.clearAll();
		System.getProperties().remove("log4j.configurationFile");
		System.getProperties().remove("sysProp");
		cleanupFile();
	}

	@BeforeClass
	public static void setupClass() {
		System.setProperty("log4j.configurationFile", "test-1.xml");
		System.setProperty("sysProp", "testProp");
		ThreadContext.clearAll();
		cleanupFile();
	}

	@Test
	public void testAdditionalFields() throws Exception {
		Logger log = LogManager.getLogger();
		ThreadContext.put("key1", "value1");
		log.info("Test log message");
		try (InputStream is = new FileInputStream(new File(LOG_FILE_NAME))) {
			String logLine = new String(is.readAllBytes());
			String tname = Thread.currentThread().getName();
			long tid = Thread.currentThread().getId();
			assertThat(logLine)
					.contains("timestamp=")
					.contains("level=INFO")
					.contains("logger=" + getClass().getName())
					.contains("thread_name=" + tname)
					.contains("thread_id=" + tid)
					.contains("message=\"Test log message\"")
					.contains("sysProp=testProp")
					.contains("_key1=value1");
		}

	}


	private static void cleanupFile() {
		File f = new File(LOG_FILE_NAME);
		if (f.exists()) {
			f.delete();
		}
	}
}