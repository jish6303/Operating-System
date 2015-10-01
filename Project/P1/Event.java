public class Event implements Comparable<Event>{
	private final int time;
	private final Process p;
	private final int location; // when CPU Location = 0, IO = 1;
	private boolean complete;//When started = false, finished = true;
	private final int index;
	public Event(){
		this.time = 0;
		this.p = new Process();
		this.location = 0;
		this.complete = false;
		this.index = p.get_index();
	}
	public Event(int time, Process p, int location, boolean complete){
		this.time = time;
		this.p = p;
		this.location = location;
		this.complete = complete;
		this.index = p.get_index();
	}
	public int get_event_time(){
		return time;
	}
    public Process get_name(){
    	return p;
    }
    public int get_location(){
    	return location;
    }
    public boolean get_status(){
    	return complete;
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
					return Integer.valueOf(index).compareTo(another.get_name().get_index());
				}
			}
		}
	}
	
	public String printEvent(){
		StringBuilder res = new StringBuilder();
		res.append("P");
		res.append(p.get_index());
		res.append(" ");
		if (location == 0 && complete == false){
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
		res.append("P");
		res.append(p.get_index());
		res.append(" ");
		res.append("terminated ");
		return res.toString();
	}
}
