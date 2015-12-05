
public class MemoryLocation {
	private final String name;
	private final int arrival_time;
	private final int burst_time;
	private final int num_burst;
	private final int io_time;
	private int cur_burst;
	private int memory;
	
	public MemoryLocation(){
		name = "";
		arrival_time = 0;
		burst_time = 0;
		num_burst = 0;
		io_time = 0;
		cur_burst = 0;
		memory = 0;
	}

	
	public MemoryLocation(String name, int arrival_time, int burst_time, int num_burst, int io_time, int space){
		this.cur_burst = 0;
		this.name = name;
		this.arrival_time = arrival_time;
		this.burst_time = burst_time;
		this.num_burst = num_burst;
		this.io_time = io_time;
		this.memory = space;
	}
	public String get_name(){
		return this.name;
	}
	public int get_burstTime(){
		return this.burst_time;
	}
	
	public int get_numBurst(){
		return this.num_burst;
	}
	
	public int get_ioTime(){
		return this.io_time;
	}
	
	public int get_curBurst(){
		return this.cur_burst;
	}
	public void incur_curBurst(){
		cur_burst++;
	}
	
	public void decur_curBurst(){
		cur_burst--;
	}
	
	public void reset_curBurst(){
		this.cur_burst = 0;
	}
	
	public int get_memory(){
		return memory;
	}
	
	public int get_arrivalTime(){
		return arrival_time;
	}
	
    public boolean equals(MemoryLocation another){
    	if(this.name!=another.get_name()){
    		return false;
    	} else if (this.burst_time != another.get_burstTime()) {
    		return false;
    	} else if (this.io_time != another.get_ioTime()) {
    		return false;
    	} else if (this.num_burst != another.get_numBurst()){
    		return false;
    	} else if(this.memory != another.get_memory()){
    		return false;
    	} else {
    		return true;
    	}
    }
    
    public String printPreemption(){
		StringBuilder res = new StringBuilder();
		res.append("Process '");
		res.append(this.name);
		res.append("' preempted due to time slice expiration ");
		return res.toString();
	}
}
