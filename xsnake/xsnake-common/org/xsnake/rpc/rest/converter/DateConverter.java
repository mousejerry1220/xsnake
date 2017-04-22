package org.xsnake.rpc.rest.converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DateConverter implements IConverter<Date>{

	
	List<SimpleDateFormat> list = new ArrayList<SimpleDateFormat>();
	
	public DateConverter(){
		list.add(new SimpleDateFormat("yyyy/MM/dd"));
		list.add(new SimpleDateFormat("yyyyMMdd"));
		list.add(new SimpleDateFormat("yyyy-MM-dd"));
	}
	
	@Override
	public Date converter(String str) throws ConverterException {
		for(SimpleDateFormat sdf : list){
			try{
				return sdf.parse(str);
			}catch(ParseException e){
				//什么也不做
			}
		}
		throw new ConverterException("日期类型转换错误");
	}
}
