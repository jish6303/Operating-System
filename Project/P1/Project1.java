import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;

public class Project1 {
	public static void main(String[] args){
		try{
                   File file = new File(args[0]);
		Scanner reader = new Scanner(file);
		HashMap<Integer, Process> process_list = new HashMap<Integer, Process>();
		LinkedList<Integer> exec = new LinkedList<Integer>();
		PriorityQueue<Event> events = new PriorityQueue<Event>();
		int n = 0;
		final int t_cs = 13;
		int abs_time = 0; //current time to write next CPU process (nearest time CPU is available)
		int cur_io_time = 0; // current time to write next IO process; (nearest time I/O is available)
		boolean start = true;
		while(reader.hasNextLine()){ // read in files and initialization of structures
			String input = reader.nextLine();
			input = input.trim();
			if (input.equals("")||input.startsWith("#")){
				continue;
			} else {
				String[] list_str = input.split("\\|");
				int[] arg = new int[4];
				for (int i = 0; i < 4; i++){
					list_str[i] = list_str[i].trim();
					arg[i] = Integer.parseInt(list_str[i]);
				}
				Process process = new Process(arg[0], arg[1], arg[2], arg[3]);
				process_list.put(arg[0], process);
				exec.add(arg[0]);
			}
			n = process_list.size();
		}//So far so good.
		System.out.print("time 0ms: Simulator started ");
		printQueue(exec);//Function call to print current queue.
		while(!exec.isEmpty()){//If ready queue is empty, the program finish
			int first = exec.poll(); //The the index of first element in queue
			n--;
			Process first_p = process_list.get(first); 
			int burst_time = first_p.get_burstTime();
			int io_time = first_p.get_ioTime();

			abs_time += t_cs; // Update: time to write next CPU process: last CPU time, plus 13ms
			Event startCPU = new Event(abs_time, first_p, 0, false); //Make a new start CPU process
			abs_time += burst_time;
			Event completeCPU = new Event(abs_time, first_p, 0, true);//Make a new finish CPU process
			if(start == true){//Initialization of current_io time to be CPU time
				cur_io_time = abs_time;
				start = false;
			}
			events.add(startCPU);
			events.add(completeCPU);
			if(io_time > 0){//Make start I/O and finish I/O processes, only if I/O time is larger than 0;
				if(cur_io_time < abs_time){//As for the same cycle, I/O time depends on CPU time
					cur_io_time = abs_time; //Update io_time : CPU time
				}
				Event startIO = new Event(cur_io_time, first_p, 1, false);
				Event completeIO = new Event(cur_io_time + io_time, first_p, 1, true);
				if (first_p.get_curBurst() < first_p.get_numBurst() - 1){
					events.add(startIO);
					events.add(completeIO);	
				}
				//cur_io_time +=io_time; //Since there can be multiple I/O, one occurrence does not need to be after another.
			}
			first_p.incur_curBurst();//Increment the current burst by 1;
			while(!events.isEmpty() && events.peek().get_event_time() <= abs_time){//Release all events with finishing time before nearest CPU burst 
				Event temp = events.remove();//Getting the closest event (smallest finishing time)
				Process p = temp.get_name();
				int index = p.get_index();
				int time = temp.get_event_time();
				boolean found = false;
				int cur_burst = p.get_curBurst();
				for (Event i :events){//Finding process with same id and same cur_burst
					if(cur_burst == i.get_name().get_curBurst() && index == i.get_name().get_index()){
						found = true;
						break;
					}
				}//If not found then we make sure this process is the last of current burst. If not finished then add it back to ready queue
				if(found == false && !exec.contains(index) && p.get_curBurst() < p.get_numBurst()){
					exec.add(index);
					n++;
				}
				System.out.printf("time %dms: ", time);//Print; Unless adding it back to ready queue (if needed), we can not print the last event
				if(found == false && p.get_curBurst() == p.get_numBurst()){
					System.out.print(temp.printTerminate());//If finished, print terminate
				} else {
					System.out.print(temp.printEvent());
				}
				printQueue(exec);
				if(exec.isEmpty()&&!events.isEmpty()){
					//If ready queue is empty and events is not, 
					//this indicates program halt until nearest CPU complete time or I/O complete time (whichever occurs first) is reached
					//If all processes waiting are I/O processes, then nearest available CPU time is the nearest I/O complete time.
					//Therefore we update both abs_time and cur_io_time.
					int nextCPUavailable = Integer.MAX_VALUE;
					int nextIOavailable = Integer.MAX_VALUE;
					for (Event i :events){
						if(i.get_status() == true){
							if(i.get_location() == 0 && nextCPUavailable > i.get_event_time()){
								nextCPUavailable = i.get_event_time();
							}
							if(i.get_location() == 1 && nextIOavailable > i.get_event_time()){
								nextIOavailable =i.get_event_time();
							}
						}
					}
					if(abs_time < nextCPUavailable) abs_time = nextCPUavailable;
					if(cur_io_time < nextIOavailable) cur_io_time = nextIOavailable;
					if(abs_time == Integer.MAX_VALUE){
						abs_time = cur_io_time;
					}
				}
			}
		}
		System.out.printf("time %dms: Simulator ended\n", abs_time);
                } catch(FileNotFoundException e){
                    System.out.printf("Not able to find file: %s\n", args[0]);
                }
                
	}
	public static void printQueue(LinkedList<Integer> exec){
		System.out.print("[Q");
		for (int i = 0; i < exec.size(); i++){
			System.out.printf(" %d", exec.get(i));
		}
		System.out.println("]");
	}
}
