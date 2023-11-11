package com.mgagauz.log4j2.logfmt;

import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

@Plugin(name = "CustomLabel", category = Node.CATEGORY, printObject = true)
public class CustomLabel {

	public static final CustomLabel[] EMPTY_ARRAY = {};
	private final String name;
	private final String value;
	private final boolean resolvable;

	protected CustomLabel(String name, String value) {
		this.name = name;
		this.value = value;
		this.resolvable = null != value && value.startsWith("${");
	}

	@PluginFactory
	public static CustomLabel createLabel(
			@PluginAttribute("name") String key,
			@PluginAttribute("value") String value) {
		if (key == null) {
			throw new IllegalStateException("Property name cannot be null");
		}

		if (value == null) {
			throw new IllegalStateException("Property value cannot be null");
		}

		return new CustomLabel(key, value);
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public boolean isResolvable() {
		return resolvable;
	}

	@Override
	public String toString() {
		return name + '=' + value;
	}
}
