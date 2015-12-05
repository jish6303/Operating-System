
public class Event implements Comparable<Event>{
	private int time;
	private int complete_time;
	private final int burst;
	private final MemoryLocation p;
	private final int location; // when CPU Location = 0, IO = 1;
	private boolean complete;//When started = false, finished = true;
	private boolean interupted;
	private final String name;
	public Event(){
		this.time = 0;
		this.burst = 0;
		this.p = new MemoryLocation();
		this.location = 0;
		this.complete = false;
		this.name = p.get_name();
		this.interupted = false;
	}
	public Event(int time, MemoryLocation p, int location, boolean complete, int complete_time){
		this.time = time;
		this.p = p;
		this.burst = p.get_curBurst();
		this.location = location;
		this.complete = complete;
		this.name = p.get_name();
		this.interupted = false;
		this.complete_time = complete_time;
	}
	
	public Event(int time, MemoryLocation p){
		this.time = time;
		this.p = p;
		this.location = -1;
		this.complete = true;
		this.name = p.get_name();
		this.interupted = false;
		this.complete_time = time;
		this.burst = p.get_curBurst();
	}
	
	public int get_event_time(){
		return time;
	}
    public MemoryLocation get_name(){
    	return p;
    }
    public int get_location(){
    	return location;
    }
    public boolean get_status(){
    	return complete;
    }
    
    public String get_process_name(){
    	return p.get_name();
    }
    
    public void set_interrupted(boolean status){
    	this.interupted = status;
    }
    
    public boolean get_interrupted(){
    	return this.interupted;
    }
    
    public void reset_start(int start){
    	this.time = start;
    }
    
    public int get_finish_time(){
    	return this.complete_time;
    }
    
    public void set_finish_time(int complete){
    	this.complete_time = complete;
    }
    
    public int get_numburst(){
    	return burst;
    }
    
    
    
	@Override
	public int compareTo(Event another) {
		// TODO Auto-generated method stub
		if (Integer.valueOf(time).compareTo(another.get_event_time()) == -1){
			return -1;
		} else if (Integer.valueOf(time).compareTo(another.get_event_time()) == 1){
			return 1;
		} 
		/*else if (Integer.valueOf(p.get_index()).compareTo(another.get_name().get_index()) == -1){
			return -1;
		} else if (Integer.valueOf(p.get_index()).compareTo(another.get_name().get_index()) == 1) {
			return 1;
		}*/ 
		else {
			if(this.complete == true && another.get_status()== false){
				return -1;
			} else if (this.complete == false && another.get_status()== true){
				return 1;
			} else {
				if( Integer.valueOf(location).compareTo(another.get_location()) == -1){
					return -1;
				} else if (Integer.valueOf(location).compareTo(another.get_location()) == 1){
					return 1;
				} else{
					return 0;
				}
			}
		}
	}
	
	public String printEvent(){
		StringBuilder res = new StringBuilder();
		res.append("Process '");
		res.append(p.get_name());
		res.append("' ");
		if(location == -1){
			res.append("added to system ");
		} else if (location == 0 && complete == false){
			res.append("started using the CPU ");
		}
		else if (location == 1 && complete == false){
			res.append("performing I/O ");
		}
		else if (location == 0 && complete == true){
			res.append("completed its CPU burst ");
		}
		else if (location == 1 && complete == true){
			res.append("completed I/O ");
		}
		return res.toString();
	}
	
	public String printTerminate(){
		StringBuilder res = new StringBuilder();
		res.append("Process '");
		res.append(p.get_name());
		res.append("' ");
		res.append("terminated ");
		return res.toString();
	}
}
