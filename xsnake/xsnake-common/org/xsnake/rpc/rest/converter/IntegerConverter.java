package org.xsnake.rpc.rest.converter;

public class IntegerConverter implements IConverter<Integer>{

	@Override
	public Integer converter(String str) throws ConverterException {
		try{
			return Integer.parseInt(str);
		}catch(Exception e){
			throw new ConverterException(e.getMessage());
		}
	}
}
