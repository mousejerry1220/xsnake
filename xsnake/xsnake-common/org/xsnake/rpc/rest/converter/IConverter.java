package org.xsnake.rpc.rest.converter;

public interface IConverter<T> {

	T converter(String str) throws ConverterException;
	
}
