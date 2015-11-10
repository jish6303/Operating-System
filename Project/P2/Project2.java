import java.io.File;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream.GetField;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Scanner;


public class Project2 {
	public static void main(String[] args){
		try{
			File file = new File("test.txt");
			Scanner reader = new Scanner(file);
			HashMap<Integer, Process> process_list = new HashMap<Integer, Process>();
			LinkedList<Integer> exec = new LinkedList<Integer>();
			LinkedList<Process> srt = new LinkedList<Process>();
			LinkedList<Process> pwa = new LinkedList<Process>();
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
					srt.add(process);
					pwa.add(process);
					process_list.put(arg[0], process);
					exec.add(arg[0]);
				}
				n = process_list.size();
				Collections.sort(srt, new Comparator<Process>(){
					public int compare(Process p1, Process p2){
						return p1.get_burstTime() - p2.get_burstTime();
					}

				});
				Collections.sort(pwa, new Comparator<Process>(){
					public int compare(Process p1, Process p2){
						return p1.get_priority() - p2.get_priority();
					}

				});
			}//So far so good.
			reader.close();
			System.out.print("time 0ms: Simulator started for FCFS ");
			printQueue(exec);//Function call to print current queue.
			while(!exec.isEmpty()){//If ready queue is empty, the program finish
				int first = exec.poll(); //The the index of first element in queue
				n--;
				Process first_p = process_list.get(first); 
				int burst_time = first_p.get_burstTime();
				int io_time = first_p.get_ioTime();
				int total_time = burst_time + io_time;
				abs_time += t_cs; // Update: time to write next CPU process: last CPU time, plus 13ms
				Event startCPU = new Event(abs_time, first_p, 0, false, total_time); //Make a new start CPU process
				abs_time += burst_time;
				Event completeCPU = new Event(abs_time, first_p, 0, true, io_time);//Make a new finish CPU process
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
					Event startIO = new Event(cur_io_time, first_p, 1, false, io_time);
					Event completeIO = new Event(cur_io_time + io_time, first_p, 1, true, 0);
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
			System.out.printf("time %dms: Simulator for FCFS ended\n\n", abs_time);
			
			//Start doing SRT
			n = srt.size();
			abs_time = 0; //current time to write next CPU process (nearest time CPU is available)
			cur_io_time = 0; // current time to write next IO process; (nearest time I/O is available)
			start = true;
			System.out.print("time 0ms: Simulator started for SRT ");
			printSRTQueue(srt);//Function call to print current queue.
			clear(srt);
			Event cpu_user;
			int process_cpu = 0;
			int already_run = 0 - t_cs;
			boolean preempt = false;
			while(!srt.isEmpty()){//If ready queue is empty, the program finish
				Process first = srt.poll(); //The the index of first element in queue
				n--;
				int burst_time = first.get_burstTime();
				int io_time = first.get_ioTime();
				int total_time = burst_time + io_time;
				abs_time += t_cs; // Update: time to write next CPU process: last CPU time, plus 13ms
				Event startCPU = new Event(abs_time, first, 0, false, total_time); //Make a new start CPU process
				abs_time += burst_time;
				Event completeCPU = new Event(abs_time, first, 0, true, io_time);//Make a new finish CPU process
				if(start == true){//Initialization of current_io time to be CPU time
					cur_io_time = abs_time;
					start = false;
				}
				if(!events.contains(startCPU)) {
					add_update(events, startCPU);
				}
				if(!events.contains(completeCPU)) {
					add_update(events, completeCPU);
				}
				cpu_user = startCPU;
				process_cpu =cpu_user.get_name().get_index();
				if(io_time > 0){//Make start I/O and finish I/O processes, only if I/O time is larger than 0;
					if(cur_io_time < abs_time){//As for the same cycle, I/O time depends on CPU time
						cur_io_time = abs_time; //Update io_time : CPU time
					}
					Event startIO = new Event(cur_io_time, first, 1, false, io_time);
					Event completeIO = new Event(cur_io_time + io_time, first, 1, true, 0);
					if (first.get_curBurst() < first.get_numBurst() - 1){
						if(!events.contains(startIO)) events.add(startIO);
						if(!events.contains(completeIO)) events.add(completeIO);	
					}
					//cur_io_time +=io_time; //Since there can be multiple I/O, one occurrence does not need to be after another.
				}
				if(preempt == false){
					first.incur_curBurst();//Increment the current burst by 1;
				} else{
					if(first.get_curBurst() < first.get_numBurst()) preempt = false;
				}
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
					if(found == false && !srt.contains(p) && p.get_curBurst() < p.get_numBurst()){
						srt.add(p);
						Collections.sort(srt, new Comparator<Process>(){
							public int compare(Process p1, Process p2){
								return p1.get_burstTime() - p2.get_burstTime();
							}

						});
						n++;
					}
					System.out.printf("time %dms: ", time);//Print; Unless adding it back to ready queue (if needed), we can not print the last event
					if(found == false && p.get_curBurst() == p.get_numBurst()){
						System.out.print(temp.printTerminate());//If finished, print terminate
					} else {
						System.out.print(temp.printEvent());
					}
					printSRTQueue(srt);
					if(preempt == false && srt.size() == 1 && temp.get_location()==1 && temp.get_status() == true){
						Process preemptor = srt.peek();
						int remain = cpu_user.get_remaintime();
						if(temp.get_name().get_index() == preemptor.get_index() && preemptor.get_index() == cpu_user.get_name().get_index()){
							abs_time = time;
							cur_io_time = time;
							already_run = 0 - t_cs;
						}
						if(preemptor.get_index()!= cpu_user.get_name().get_index() && preemptor.get_burstTime() < remain){
							preempt = true;
							printPreempt(time, preemptor, cpu_user.get_name());
							already_run = already_run + time - cpu_user.get_event_time();
							abs_time = time + t_cs;
							burst_time = preemptor.get_burstTime();
							io_time = preemptor.get_ioTime();
							total_time = burst_time + io_time;
							srt.poll();
							srt.add(cpu_user.get_name());
							startCPU = new Event(abs_time, p, 0, false, total_time);
							abs_time += burst_time;
							cur_io_time = abs_time;
							completeCPU = new Event(abs_time, p, 0, true, io_time);
							if(!events.contains(startCPU)) events.add(startCPU);
							if(!events.contains(completeCPU)) events.add(completeCPU);
							for(Event preempted : events){
								if(preempted.get_name().get_index()!= process_cpu) continue;
								if(preempted.get_location()==0 && preempted.get_status() == false){
									if(preempted.get_remaintime() < 0){
										preempted.set_remaintime(0);
									} else{
										preempted.set_remaintime(preempted.get_remaintime()-already_run);
									}
								}
								if(preempted.get_location()==0 && preempted.get_status()== false){
									preempted.reset_starttime(abs_time);
								} else if (preempted.get_location()==0 && preempted.get_status()== true ||
										preempted.get_location()==1 && preempted.get_status()== false){
									preempted.reset_starttime(abs_time + preempted.get_name().get_burstTime() - already_run);
								} else {
									preempted.reset_starttime(abs_time + preempted.get_name().get_burstTime() - already_run + preempted.get_name().get_ioTime());
								}
							}
							cpu_user = startCPU;
							process_cpu=cpu_user.get_name().get_index();
							if(io_time > 0){
								if(cur_io_time < abs_time){
									cur_io_time = abs_time; //Update io_time : CPU time
								}
								Event startIO = new Event(cur_io_time, p, 1, false, io_time);
								Event completeIO = new Event(cur_io_time + io_time, p, 1, true, 0);
								if (p.get_curBurst() < p.get_numBurst() - 1){
									if(!events.contains(startIO)) events.add(startIO);
									if(!events.contains(completeIO)) events.add(completeIO);	
								}
							}
							cur_io_time += io_time;
							p.incur_curBurst();
							printSRTQueue(srt);
						}
					}
					if(srt.isEmpty()&&!events.isEmpty()){
						//If ready queue is empty and events is not, 
						//this indicates program halt until nearest CPU complete time or I/O complete time (whichever occurs first) is reached
						//If all processes waiting are I/O processes, then nearest available CPU time is the nearest I/O complete time.
						//Therefore we update both abs_time and cur_io_time.
						int nextCPUavailable = Integer.MAX_VALUE;
						int nextIOavailable = Integer.MAX_VALUE;
						Event temp1 = temp, temp2 = temp;
						for (Event i :events){
							if(i.get_status() == true){
								if(i.get_location() == 0 && nextCPUavailable > i.get_event_time()){
									nextCPUavailable = i.get_event_time();
									temp1 = i;
								}
								if(i.get_location() == 1 && nextIOavailable > i.get_event_time()){
									nextIOavailable =i.get_event_time();
									temp2 = i;
								}
							}
						}
						if(nextCPUavailable > nextIOavailable){
							nextCPUavailable = nextIOavailable;
							temp2 = temp1;
						}
						if(abs_time < nextCPUavailable) {
							abs_time = nextCPUavailable;
							cpu_user = temp1;
						}
						if(cur_io_time < nextIOavailable) {
							cur_io_time = nextIOavailable;
							cpu_user = temp2;
						}
						if(abs_time == Integer.MAX_VALUE){
							abs_time = cur_io_time;
							cpu_user = temp2;
						}
						process_cpu=cpu_user.get_name().get_index();
					}
				}
			}
			System.out.printf("time %dms: Simulator for SRT ended\n\n", abs_time);
			
			
		}catch (FileNotFoundException e){
		    // do stuff here..
		    System.out.println("FileNotFoundException: test.txt");
		}
		
	}
	private static void printPreempt(int time, Process preemptor,
			Process preempted) {
		System.out.printf("time %dms: P%d preempted by P%d ", time, preempted.get_index(), preemptor.get_index());
		
	}
	public static void printQueue(LinkedList<Integer> exec){
		System.out.print("[Q");
		for (int i = 0; i < exec.size(); i++){
			System.out.printf(" %d", exec.get(i));
		}
		System.out.println("]");
	}
	
	public static void printSRTQueue(LinkedList<Process> srt){
		System.out.print("[Q");
		for (int i = 0; i < srt.size(); i++){
			System.out.printf(" %d", srt.get(i).get_index());
		}
		System.out.println("]");
	}
	
	public static void clear(LinkedList<Process> srt){
		for(Process p : srt){
			p.reset_curBurst();
		}
	}
	
	public static void add_update(PriorityQueue<Event> events, Event e){
		boolean found = false;
		for(Event e1 : events){
			if(e1.get_status() == e.get_status() && e1.get_location() == e.get_location()){
				found = true;
				if(e1.get_event_time() > e.get_event_time()) e1.reset_starttime(e.get_event_time());
				break;
			}
		}
		if(found == false){
			events.add(e);
		}
	}
}
