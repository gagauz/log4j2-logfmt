package com.mgagauz.log4j2.logfmt;

import java.io.PrintWriter;
import java.io.Writer;

import static com.mgagauz.log4j2.logfmt.Literals.DQUOTE;
import static com.mgagauz.log4j2.logfmt.Literals.ESCAPED_NL;
import static com.mgagauz.log4j2.logfmt.Literals.ESCAPED_DQUOTE;

public class StringBuilderPrintWriter extends PrintWriter {

	private final StringBuilder stringBuilder;

	protected StringBuilderPrintWriter(Writer out) {
		super(Writer.nullWriter());
		this.stringBuilder = null;
	}

	public StringBuilderPrintWriter(StringBuilder stringBuilder) {
		super(Writer.nullWriter());
		this.stringBuilder = stringBuilder;
	}

	@Override
	public void println() {
		stringBuilder.append(ESCAPED_NL);
	}

	@Override
	public void print(Object object) {
		print(String.valueOf(object));
	}

	@Override
	public void println(Object object) {
		print(object);
		println();
	}

	@Override
	public void print(String string) {
		stringBuilder.append(string.replace(DQUOTE, ESCAPED_DQUOTE));
	}
}
