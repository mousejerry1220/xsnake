package org.xsnake.rpc.rest.converter;

public class FloatConverter implements IConverter<Float>{

	@Override
	public Float converter(String str) throws ConverterException {
		try{
			return Float.valueOf(str);
		}catch(Exception e){
			throw new ConverterException(e.getMessage());
		}
	}
}
