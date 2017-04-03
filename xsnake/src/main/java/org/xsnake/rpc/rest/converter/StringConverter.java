package org.xsnake.rpc.rest.converter;

public class StringConverter implements IConverter<String>{

	@Override
	public String converter(String str) throws ConverterException {
		return str;
	}
}
