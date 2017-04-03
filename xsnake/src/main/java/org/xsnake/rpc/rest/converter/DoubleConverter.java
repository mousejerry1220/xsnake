package org.xsnake.rpc.rest.converter;

public class DoubleConverter implements IConverter<Double>{

	@Override
	public Double converter(String str) throws ConverterException {
		try{
			return Double.parseDouble(str);
		}catch(Exception e){
			throw new ConverterException(e.getMessage());
		}
	}
}
