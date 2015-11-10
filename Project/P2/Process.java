public class Process {
	private final int index;
	private final int burst_time;
	private final int num_burst;
	private final int io_time;
	private int cur_burst;
	private int priority;
	
	public Process(){
		index = 0;
		burst_time = 0;
		num_burst = 0;
		io_time = 0;
		cur_burst = 0;
		priority = 0;
	}
	public Process(int index, int burst_time, int num_burst, int io_time){
		cur_burst = 0;
		this.index = index;
		this.burst_time = burst_time;
		this.num_burst = num_burst;
		this.io_time = io_time;
		this.priority = 0;
	}
	public int get_index(){
		return this.index;
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
	
	public void incre_priority(){
		this.priority++;
	}
	
	public int get_priority(){
		return this.priority;
	}
    public boolean equals(Process another){
    	if(this.index!=another.get_index()){
    		return false;
    	} else if (this.burst_time != another.get_burstTime()) {
    		return false;
    	} else if (this.io_time != another.get_ioTime()) {
    		return false;
    	} else if (this.num_burst != another.get_numBurst()){
    		return false;
    	} else {
    		return true;
    	}
    }
}
