package lib.yuncorelot.body;

import java.util.List;
import java.util.Map;

import lib.yuncorelot.Base;

public class User extends Base{
	private String total;
	private String index;
	private String current_count;
	List<Map<String,String>> list;
	private List<Map<String,String>> change;
	private List<String> delete;
	
	public void setTotal(String total){
		this.total = total;
	}
	
	public String getTotal(){
		return this.total;
	}
	
	public void setIndex(String index){
		this.index = index;
	}
	
	public String getIndex(){
		return this.index;
	}
	
	public void setCurrent_count(String current_count){
		this.current_count = current_count;
	}
	
	public String getCurrent_count(){
		return this.current_count;
	}
	
	public void setList(List<Map<String,String>> list){
		this.list = list;
	}
	
	public List<Map<String,String>> getList(){
		return this.list;
	}
	
	public void setChange(List<Map<String,String>> change){
		this.change = change;
	}
	
	public List<Map<String,String>> getChange(){
		return this.change;
	}
	
	public void setDelete(List<String> delete){
		this.delete = delete;
	}
	
	public List<String> getDelete(){
		return this.delete;
	}

}