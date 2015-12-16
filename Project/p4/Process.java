public class Process {
	private final String name;
	private int memory;
	
	public Process(){
		name = "";
		memory = 0;
	}

	
	public Process(String name, int space){
		this.name = name;
		this.memory = space;
	}
	public String get_name(){
		return this.name;
	}
	
	public int get_memory(){
		return memory;
	}
	
    public boolean equals(Process another){
    	if(this.name!=another.get_name()){
    		return false;
    	} else if(this.memory != another.get_memory()){
    		return false;
    	} else {
    		return true;
    	}
    }
    
}

