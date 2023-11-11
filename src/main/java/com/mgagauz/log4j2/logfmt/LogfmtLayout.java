package com.mgagauz.log4j2.logfmt;

import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.layout.ByteBufferDestination;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.apache.logging.log4j.core.util.SystemNanoClock;
import org.apache.logging.log4j.spi.AbstractLogger;
import org.apache.logging.log4j.util.Strings;

import static com.mgagauz.log4j2.logfmt.Literals.DQUOTE;
import static com.mgagauz.log4j2.logfmt.Literals.ESCAPED_DQUOTE;
import static com.mgagauz.log4j2.logfmt.Literals.ESCAPED_NL;
import static com.mgagauz.log4j2.logfmt.Literals.EXCEPTION;
import static com.mgagauz.log4j2.logfmt.Literals.LEVEL;
import static com.mgagauz.log4j2.logfmt.Literals.LOGGER_NAME;
import static com.mgagauz.log4j2.logfmt.Literals.MESSAGE;
import static com.mgagauz.log4j2.logfmt.Literals.NL;
import static com.mgagauz.log4j2.logfmt.Literals.SPACE;
import static com.mgagauz.log4j2.logfmt.Literals.THREAD_ID;
import static com.mgagauz.log4j2.logfmt.Literals.THREAD_NAME;
import static com.mgagauz.log4j2.logfmt.Literals.TIMESTAMP;
import static java.util.Optional.ofNullable;

@Plugin(name = "LogfmtLayout", category = Node.CATEGORY, elementType = Layout.ELEMENT_TYPE, printObject = true)
public class LogfmtLayout extends AbstractStringLayout {

	private static final ThreadLocal<PrintWriter> threadLocal = new ThreadLocal<>();

	private final boolean includeStacktrace;
	private final boolean nanoTime;
	private final boolean includeContext;
	private final String contextKeyPrefix;
	private final BiConsumer<LogEvent, StringBuilder> additionalLabelsAppender;

	public static class Builder<B extends Builder<B>> extends AbstractStringLayout.Builder<B>
			implements org.apache.logging.log4j.core.util.Builder<LogfmtLayout> {
		@PluginBuilderAttribute
		private boolean includeStacktrace;
		@PluginBuilderAttribute
		private boolean nanoTime;
		@PluginBuilderAttribute
		private boolean includeContext;
		@PluginBuilderAttribute
		private String contextKeyPrefix = "_";
		@PluginBuilderAttribute
		private String includeFields;
		@PluginBuilderAttribute
		private String excludeFields;
		@PluginElement("CustomLabel")
		private CustomLabel[] additionalLabels;

		public Builder() {
			setCharset(StandardCharsets.UTF_8);
		}

		@Override
		public LogfmtLayout build() {
			return new LogfmtLayout(getConfiguration(),
					getCharset(),
					includeStacktrace,
					nanoTime,
					includeContext,
					contextKeyPrefix,
					additionalLabels);
		}

		public boolean isIncludeStacktrace() {
			return includeStacktrace;
		}

		public B setIncludeStacktrace(boolean includeStacktrace) {
			this.includeStacktrace = includeStacktrace;
			return asBuilder();
		}

		public boolean isNanoTime() {
			return nanoTime;
		}

		public B setNanoTime(boolean nanoTime) {
			this.nanoTime = nanoTime;
			return asBuilder();
		}

		public boolean isIncludeContext() {
			return includeContext;
		}

		public B setIncludeContext(boolean includeContext) {
			this.includeContext = includeContext;
			return asBuilder();
		}

		public String getContextKeyPrefix() {
			return contextKeyPrefix;
		}

		public Builder<B> setContextKeyPrefix(String contextKeyPrefix) {
			this.contextKeyPrefix = contextKeyPrefix;
			return asBuilder();
		}

		public String getIncludeFields() {
			return includeFields;
		}

		public B setIncludeFields(String includeFields) {
			this.includeFields = includeFields;
			return asBuilder();
		}

		public String getExcludeFields() {
			return excludeFields;
		}

		public B setExcludeFields(String excludeFields) {
			this.excludeFields = excludeFields;
			return asBuilder();
		}

		public CustomLabel[] getAdditionalLabels() {
			return additionalLabels;
		}

		public B setAdditionalLabels(CustomLabel[] labels) {
			this.additionalLabels = labels;
			return asBuilder();
		}

		@Override
		@SuppressWarnings("unchecked")
		public B asBuilder() {
			return (B) this;
		}
	}

	protected LogfmtLayout(Configuration configuration, Charset charset, boolean includeStacktrace, boolean nanoTime,
			boolean includeContext, String contextKeyPrefix, CustomLabel[] additionalLabels) {
		super(configuration, charset, null, null);
		this.includeStacktrace = includeStacktrace;
		this.nanoTime = nanoTime;
		this.includeContext = includeContext;
		this.contextKeyPrefix = ofNullable(contextKeyPrefix).orElse(Strings.EMPTY);
		this.additionalLabelsAppender = createAdditionalLabelsAppender(additionalLabels, configuration);
		if (nanoTime) {
			configuration.setNanoClock(new SystemNanoClock());
		}
	}

	@Override
	public void encode(LogEvent event, ByteBufferDestination destination) {
		StringBuilder text = getStringBuilder();
		convertLogEventToText(event, text);
		getStringBuilderEncoder().encode(text, destination);
		trimToMaxSize(text);
	}

	@PluginBuilderFactory
	public static <B extends Builder<B>> B newBuilder() {
		return new Builder<B>().asBuilder();
	}

	@Override
	public String toSerializable(LogEvent event) {
		StringBuilder output = getStringBuilder();
		convertLogEventToText(event, output);
		return output.toString();
	}

	private void convertLogEventToText(LogEvent event, StringBuilder output) {
		output.append(TIMESTAMP);
		if (nanoTime) {
			output.append(event.getNanoTime());
		} else {
			output.append(event.getTimeMillis());
		}
		output.append(' ');
		output.append(LEVEL).append(event.getLevel().name()).append(' ');
		output.append(LOGGER_NAME).append(event.getLoggerName()).append(' ');
//		output.append(LOGGER_FQCN).append(event.getLoggerFqcn()).append(' ');
		output.append(THREAD_ID).append(event.getThreadId()).append(' ');
		output.append(THREAD_NAME).append(event.getThreadName()).append(' ');
		output.append(MESSAGE)
			.append('"')
			.append(sanitize(event.getMessage().getFormattedMessage()))
			.append('"')
			.append(' ');
		if (includeContext) {
			event.getContextData().forEach((k, v) -> {
				output.append(contextKeyPrefix + k).append('=').append(v).append(' ');
			});
		}

		if (includeStacktrace) {
			output.append(EXCEPTION);
			if (null != event.getThrown()) {
				output.append('"');
				event.getThrown()
						.printStackTrace(new StringBuilderPrintWriter(output));
				output.append('"');
			}
			output.append(' ');
		}
		additionalLabelsAppender.accept(event, output);
		output.append('\n');
	}

	protected static PrintWriter getPrintWriter() {
		if (AbstractLogger.getRecursionDepth() > 1) {
			return new StringBuilderPrintWriter(getStringBuilder());
		}
		PrintWriter result = threadLocal.get();
		if (result == null) {
			result = new StringBuilderPrintWriter(getStringBuilder());
			threadLocal.set(result);
		}
		return result;
	}

	private static void validateAdditionalLabels(final Configuration config,
			final CustomLabel[] additionalLabels) {
		if (null == config && Arrays.stream(additionalLabels).anyMatch(CustomLabel::isResolvable)) {
				throw new IllegalArgumentException(
						"configuration needs to be set when there are additional fields with variables");
		}
	}

	private String sanitize(String message) {
		return message.replace(DQUOTE, ESCAPED_DQUOTE).replace(NL, ESCAPED_NL);
	}

	private static BiConsumer<LogEvent, StringBuilder> createAppender(final String field) {
		return (event, output) -> output
				.append(field)
				.append(event.getLevel().name())
				.append(' ');
	}

	private static BiConsumer<LogEvent, StringBuilder> createAdditionalLabelsAppender(CustomLabel[] additionalLabels,
			Configuration configuration) {
		validateAdditionalLabels(configuration, additionalLabels);
		if (null == additionalLabels || additionalLabels.length == 0) {
			// NOOP
			return (logEvent, builder) -> {
			};
		}
		if (Arrays.stream(additionalLabels).anyMatch(CustomLabel::isResolvable)) {
			final StrSubstitutor strSubstitutor = configuration.getStrSubstitutor();
			return (logEvent, builder) -> {
				for (final CustomLabel pair : additionalLabels) {
					builder.append(pair.getName()).append('=');
					if (pair.isResolvable()) {
						builder.append(strSubstitutor.replace(logEvent, pair.getValue()));
					} else {
						builder.append(pair.getValue());
					}
					builder.append(' ');
				}
			};
		}
		final String keyValuesString = Arrays.stream(additionalLabels)
				.map(CustomLabel::toString)
				.collect(Collectors.joining(SPACE));
		return (logEvent, builder) -> builder.append(keyValuesString);
	}

}