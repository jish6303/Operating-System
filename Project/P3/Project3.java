
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Scanner;


public class Project3 {
	//Name: Jiaju Shen(shenj6), Chenxi Piao(Piaoc)
	final static int t_cs = 13;
	final static int t_slide = 80;
	final static int t_memmove = 10;
	static int abs_time = 0;//current time to write next CPU process (nearest time CPU is available if we create an event and let it occupy CPU)
	static int cur_io_time = 0; // current time to write next IO process; (nearest time I/O is available)
	static int[][] rr_stat = new int[3][6];
	static double[][] rr_cpu_stat = new double[3][3];
	static double n1;
	static double n2;
	static double n3;
	static int n4;
	static int n5;
	static int n6;
	static double n7;
	static ArrayList<ArrayList<Integer>> turnaround_time = new ArrayList<ArrayList<Integer>>();
	static HashMap<MemoryLocation, Integer> processList = new HashMap<MemoryLocation, Integer>();
	//We need a 3 * 6 integer array to hold statistics of round robin
	//Turnaround time 0, wait time 1, burst time 2, number of context switch 3 , defragmentation time 4 and total time 5
	public static void main(String[] args) throws IOException{
		
		if (args.length == 0){
			System.out.println("Please input the file name as an argument!");
			System.exit(-1);
		}
		if (args.length !=1){	//check the number of args
			System.out.println("Wrong args!");
			System.exit(-1);
		}
		
		try{
			File file = new File(args[0]);
			Scanner reader = new Scanner(file);
			//HashMap<String, Process> process_list = new HashMap<String, Process>();
			int total_num_burst = 0;
			LinkedList<MemoryLocation> srt = new LinkedList<MemoryLocation>();
			LinkedList<MemoryLocation> exec = new LinkedList<MemoryLocation>();
			LinkedList<MemoryLocation> exec2 = new LinkedList<MemoryLocation>();
			LinkedList<MemoryLocation> rr = new LinkedList<MemoryLocation>();
			while(reader.hasNextLine()){ // read in files and initialization of structures
				String input = reader.nextLine();
				input = input.trim();
				if (input.equals("")||input.startsWith("#")){
					continue;
				} else {
					String[] list_str = input.split("\\|");
					int[] arg = new int[5];
					String name = list_str[0].trim();
					for (int i = 1; i < 6; i++){
						list_str[i] = list_str[i].trim();
						arg[i-1] = Integer.parseInt(list_str[i]);
					}
					MemoryLocation process = new MemoryLocation(name, arg[0], arg[1], arg[2], arg[3], arg[4]);
					total_num_burst += process.get_numBurst();
					/*srt.add(process);
					rr.add(process);*/
					exec.add(process);
					exec2.add(process);
					//process_list.put(name, process);
					//exec.add(name);
				}
			}//So far so good.
			reader.close();
			Collections.sort(exec, new Comparator<MemoryLocation>(){
				public int compare(MemoryLocation p1, MemoryLocation p2){
					return p1.get_arrivalTime() - p2.get_arrivalTime();
				}

			});
			
			
			//Shortest Remaining Time (SRT)
			for (int i = 0; i<3; i++){
				if(i==0){
					System.out.println("time 0ms: Simulator started for SRT and First-Fit");
				}else if(i==1){
					System.out.println("time 0ms: Simulator started for SRT and Next-Fit");
				}else{
					System.out.println("time 0ms: Simulator started for SRT and Best-Fit");
				}
				DataOb dataOb = ProjectSrt.srtPro(args[0], i);
				n1 = dataOb.att;
				n2 = dataOb.awt;
				n3 = dataOb.acbt;
				n4 = dataOb.ncs;
				n5 = dataOb.tst;
				n6 = dataOb.tdt;
				n7 = dataOb.dp;
			}
			//Round Robin (RR)
			for(int j = 0; j < exec.size(); j++){
				ArrayList<Integer> time = new ArrayList<Integer>();
				turnaround_time.add(time);
				processList.put(exec.get(j), j);
			}
			for(int i = 0; i < 3; i++){
				exec2.clear();
				exec2.addAll(exec);
				for(MemoryLocation p : exec2){
					p.reset_curBurst();
				}
				rr.clear();
				System.out.print("time 0ms: Simulator started for RR (t_slice 80) and ");
				if(i == 0){
					System.out.println("First-Fit");
				} else if(i == 1){
					System.out.println("Next-Fit");
				} else {
					System.out.println("Best-Fit");
				}
				round_robin(rr, exec2, i);
				for(int j = 0; j < 3; j++){
					rr_cpu_stat[i][j] = (double) ((double) (rr_stat[i][j]) / (double) (total_num_burst));
				}
			}
		}catch (FileNotFoundException e){
		    // do stuff here..
		    System.out.println("FileNotFoundException: test.txt");
		}
		
		BufferedWriter writer = null;
        try {
            //create a temporary file
            File logFile = new File("simout.txt");

            // This will output the full path where the file will be written to...
            System.out.println(logFile.getCanonicalPath());

            writer = new BufferedWriter(new FileWriter(logFile));
            for(int i = 0; i < 3; i++){
            	//write SRT stats
            	writer.write("Algorithm SRT, ");
            	if(i == 0){
    				writer.write("Memory first fit");
    			} else if(i == 1){
    				writer.write("Memory next fit");
    			} else {
    				writer.write("Memory Best fit");
    			}
            	/*double n1 = dataOb.att;
				double n2 = dataOb.awt;
				double n3 = dataOb.acbt;
				int n4 = dataOb.ncs;
				int n5 = dataOb.tst;
				int n6 = dataOb.tdt;
				double n7 = dataOb.dp;*/
            	writer.newLine();
            	StringBuilder res = new StringBuilder();
            	res.append("-- average turnaround time: ");
            	res.append(String.format("%.2f", n1));
            	res.append("ms");
            	res.append(System.getProperty("line.separator"));
            	res.append("-- average wait time: ");
            	res.append(String.format("%.2f",n2));
            	res.append("ms");
            	res.append(System.getProperty("line.separator"));
            	res.append("-- average CPU burst time: ");
            	res.append(String.format("%.2f",n3));
            	res.append("ms");
            	res.append(System.getProperty("line.separator"));
            	res.append("-- total number of context switches: ");
            	res.append(n4);
            	res.append(System.getProperty("line.separator"));
            	res.append("-- total simulation time: ");
            	res.append(n5);
            	res.append("ms");
            	res.append(System.getProperty("line.separator"));
            	res.append("-- total defragmentation time: ");
            	res.append(n6);
            	res.append("ms");
            	res.append(System.getProperty("line.separator"));
            	res.append("-- defragmentation percentage: ");
            	res.append(String.format("%.2f",n7));
            	res.append("%");
            	res.append(System.getProperty("line.separator"));
            	res.append(System.getProperty("line.separator"));
            	writer.write(res.toString());
            }
            for(int i = 0; i < 3; i++){
            	//write RR stats
            	writer.write("Algorithm RR, ");
            	if(i == 0){
    				writer.write("Memory first fit");
    			} else if(i == 1){
    				writer.write("Memory next fit");
    			} else {
    				writer.write("Memory Best fit");
    			}
            	writer.newLine();
            	StringBuilder res = new StringBuilder();
            	res.append("-- average turnaround time: ");
            	res.append(String.format("%.2f",rr_cpu_stat[i][0]));
            	res.append("ms");
            	res.append(System.getProperty("line.separator"));
            	res.append("-- average wait time: ");
            	res.append(String.format("%.2f",rr_cpu_stat[i][1]));
            	res.append("ms");
            	res.append(System.getProperty("line.separator"));
            	res.append("-- average CPU burst time: ");
            	res.append(String.format("%.2f",rr_cpu_stat[i][2]));
            	res.append("ms");
            	res.append(System.getProperty("line.separator"));
            	res.append("-- total number of context switches: ");
            	res.append(rr_stat[i][3]);
            	res.append(System.getProperty("line.separator"));
            	res.append("-- total simulation time: ");
            	res.append(rr_stat[i][5]);
            	res.append("ms");
            	res.append(System.getProperty("line.separator"));
            	res.append("-- total defragmentation time: ");
            	res.append(rr_stat[i][4]);
            	res.append("ms");
            	res.append(System.getProperty("line.separator"));
            	res.append("-- defragmentation percentage: ");
            	double perc = (double)((double) rr_stat[i][4]/ (double) rr_stat[i][5]) * 100;
            	res.append(String.format("%.2f",perc));
            	res.append("%");
            	res.append(System.getProperty("line.separator"));
            	res.append(System.getProperty("line.separator"));
            	writer.write(res.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // Close the writer regardless of what happens...
                writer.close();
            } catch (Exception e) {
            }
        }
    }
		
	private static void round_robin(LinkedList<MemoryLocation> rr, LinkedList<MemoryLocation> exec, int i) {
		int total_time = 0;
		int nextCPUtime = 0;//This is to describe next CPU event
		int t_total_turn_around = 0;
		int t_total_burst = 0;
		int t_total_wait = 0;
		HashMap<MemoryLocation, Integer> map = new HashMap<MemoryLocation, Integer>();
		Memory m = new Memory();
		PriorityQueue<Event> events = new PriorityQueue<Event>();
		ArrayList<Event> eventlist = new ArrayList<Event>();//This is easier to iterate through priority queue
		Collections.sort(exec, new Comparator<MemoryLocation>(){
			public int compare(MemoryLocation p1, MemoryLocation p2){
				return p1.get_arrivalTime() - p2.get_arrivalTime();
			}
		});
		for(MemoryLocation p : exec){
			int arrival_time = p.get_arrivalTime();
			Event e = new Event(arrival_time, p);
			events.add(e);
		}
		eventlist.addAll(events);
		//By this time the events list should have all items "events" has
		int min = events.peek().get_event_time();
		abs_time = min;
		nextCPUtime = min + t_cs;
		while(events.peek().get_event_time() <= min){
			//Initiate the run, clear every process that has arrived at time min (0) but not yet taken the CPU
			abs_time += t_cs;
			Event init = events.poll();
			eventlist.remove(init);
			MemoryLocation first = init.get_name();
			rr.add(first);
			int index = processList.get(first);
			ArrayList<Integer> time_in = turnaround_time.get(index);
			time_in.add(init.get_event_time());
			if(i == 0){
				m.addFirstAvailable(first);
			} else if(i == 1){
				m.addSecondAvailable(first);
			} else {
				m.addBestAvailable(first);
			}
			if(m.get_need_defragmentation() == true){
				System.out.printf("time %dms: Process '%s' unable to be added; lack of memory\n", min, first.get_name());
				System.out.printf("time %dms: Starting defragmentation (suspending all processes)\n", min);
				m.defragmentation();
				int units = m.get_defragmention_moved_units();
				nextCPUtime = units * t_memmove;
				rr_stat[i][4] += units * t_memmove;
				System.out.printf("time %dms: Completed defragmentation (moved %d memory units)\n", min + units * t_memmove, units);
				abs_time = units * t_memmove;
				System.out.printf("time %dms: Simulated Memory:\n");
				m.printMemory();
				if(i == 0){
					m.addFirstAvailable(first);
				} else if(i == 1){
					m.addSecondAvailable(first);
				} else {
					m.addBestAvailable(first);
				}
				min += units * t_memmove;
			}
			System.out.printf("time %dms: ", min);
			System.out.print(init.printEvent());//No need to reset the rest events' interrupted
			printQueue(rr);
			System.out.printf("time %dms: Simulated Memory:\n", min);
			m.printMemory();
			int cpu_time = first.get_burstTime();
			int io_time = first.get_ioTime();
			Event startCPU = new Event(abs_time, first, 0, false, abs_time+cpu_time);
			Event completeCPU = new Event(abs_time + cpu_time, first, 0, true, abs_time+cpu_time);
			Event startIO = new Event(abs_time + cpu_time, first, 1, false, abs_time+cpu_time+io_time);
			Event completeIO = new Event(abs_time + cpu_time + io_time, first, 1, true, abs_time + cpu_time + io_time);
			abs_time += cpu_time;
			events.add(startCPU);
			events.add(completeCPU);
			if(io_time >0){
				events.add(startIO);
				events.add(completeIO);
			}
			eventlist.add(startCPU);
			eventlist.add(completeCPU);
			if(io_time >0){
				eventlist.add(startIO);
				eventlist.add(completeIO);
			}
		}
		abs_time += t_cs;
		while(!rr.isEmpty() || !events.isEmpty()){//Thought:
			//0.Print all events with a finishing time earlier than the current nextCPUtime
			//1.Find the first event in event priority queue
			//2.If it is memory allocation
			//3.If it is a existing event about to take CPU (CPU start)
			//	3.1 If remaining time exceeds t_slide
			//  3.2 If remaining time does not exceed t_slide
			//4.If it is other event (CPU complete, IOstart, IOcomplete)
			//5.Increment nextCPUtime (by t_slide or other amount)
			rr_printNextEvents(m, eventlist, events, rr, nextCPUtime, i);
			if(rr.isEmpty() && events.isEmpty()){
				break;
			}
			Event event = events.poll();
			eventlist.remove(event);
			MemoryLocation process = event.get_name();
			if(nextCPUtime < event.get_event_time()){
				nextCPUtime = event.get_event_time();
			}
			if(event.get_location() == -1){
				System.out.printf("time %dms: ", event.get_event_time());
				System.out.print(event.printEvent());//No need to reset the rest events' interrupted
				printQueue(rr);
				System.out.println("time 0ms: Simulated Memory:");
				m.printMemory();
				rr.add(process);
				int index = processList.get(process);
				ArrayList<Integer> time_in = turnaround_time.get(index);
				time_in.add(event.get_event_time());
				int cpu_time = process.get_burstTime();
				int io_time = process.get_ioTime();
				Event startCPU = new Event(abs_time, process, 0, false, abs_time+cpu_time);
				Event completeCPU = new Event(abs_time + cpu_time, process, 0, true, abs_time+cpu_time);
				Event startIO = new Event(abs_time + cpu_time, process, 1, false, abs_time+cpu_time+io_time);
				Event completeIO = new Event(abs_time + cpu_time + io_time, process, 1, true, abs_time + cpu_time + io_time);
				abs_time += cpu_time;
				events.add(startCPU);
				events.add(completeCPU);
				if(io_time >0){
					events.add(startIO);
					events.add(completeIO);
				}
				eventlist.add(startCPU);
				eventlist.add(completeCPU);
				if(io_time >0){
					eventlist.add(startIO);
					eventlist.add(completeIO);
				}
			} else if(event.get_location() == 0 && event.get_status() == false){//First of four
				//Find if there are other processes waiting in rr queue
				//If there is, then find the 4 events leading by starting CPU
				//Delete the rest three including CPU complete, IO start and IO complete
				//Print preemption event and ready queue
				//Find the four events of the process following the current CPU event and change start time
				//Change the starting time and finishing time of the eight processes and put them back in events
				rr.poll();
				rr_stat[i][3] ++;
				int finish = event.get_finish_time();
				System.out.printf("time %dms: ", event.get_event_time());
				System.out.print(event.printEvent());//No need to reset the rest events' interrupted
				printQueue(rr);
				if(finish > nextCPUtime + t_slide && !rr.isEmpty()){//If this event finishes before CPU slot closing
					//We need preemption
					sort_eventsList(eventlist);
					MemoryLocation next = rr.peek();
					int lowest_burst_preemptor = Integer.MAX_VALUE;
					ArrayList<Event> changee = new ArrayList<Event>();//To be preempted
					ArrayList<Event> changor = new ArrayList<Event>();//To preempt
					changee.add(event);
					for(Event preempt : eventlist){
						if(event.get_numburst() == preempt.get_numburst() && event.get_name() == preempt.get_name()){
							events.remove(preempt);
							changee.add(preempt);
						} else if(preempt.get_name() == next){
							if(preempt.get_numburst() < lowest_burst_preemptor){
								changor.clear();
								lowest_burst_preemptor = preempt.get_numburst();
								changor.add(preempt);
								events.remove(preempt);
							} else if (preempt.get_numburst() == lowest_burst_preemptor){
								changor.add(preempt);
								events.remove(preempt);
							}
						}
					}
					eventlist.clear();
					eventlist.addAll(events);
					sort_eventsList(eventlist);
					nextCPUtime += t_slide;
					rr_printNextEvents(m, eventlist, events, rr, nextCPUtime, i);
					rr.add(process);
					System.out.printf("time %dms: ", nextCPUtime);
					System.out.print(process.printPreemption());
					printQueue(rr);
					sort_eventsList(changee);
					sort_eventsList(changor);
					int gap = 0;
					nextCPUtime += t_cs;
					for(int index = 0; index < changor.size(); index++){//Update next processes' events starting time and finish time
						Event moveForward = changor.get(index);
						if(index == 0){
							gap = moveForward.get_event_time() - nextCPUtime;
						}
						moveForward.reset_start(moveForward.get_event_time() - gap);
						moveForward.set_finish_time(moveForward.get_finish_time() - gap);
						if(index == 0){
							finish = moveForward.get_finish_time();
						}
					}
					gap = 0;
					sort_eventsList(eventlist);
					boolean reset = false;
					for(Event e: eventlist){//For all events except for memory allocation events
						if(e.get_location() != -1){
							if(e.get_status() == false && e.get_location() == 0){
								if(gap == 0){
									gap = e.get_event_time() - (finish + t_cs); 
								}
							}
							if(e.get_interrupted() == false){
								//Only change those whose "interrupted" is false
								//Don't change those events whose time is not changeable
								//i.e. events that is waiting to finish IO (Only 1 of 4 left in current burst)
								e.reset_start(e.get_event_time() - gap);
								e.set_finish_time(e.get_finish_time() - gap);
							}
							if(e.get_status() == false && e.get_location() == 0){
								reset = true;
								abs_time = e.get_finish_time() + t_cs;
							}
						}
					}
					if(reset == false){
						abs_time = finish + t_cs;
					}
					events.clear();
					events.addAll(eventlist);
					//Update abs_time;
					events.addAll(changor);
					eventlist.addAll(changor);
					sort_eventsList(eventlist);
					for(int index = 0; index < changee.size(); index++){//Update preemptee's events starting time and finish time
						Event moveBack = changee.get(index);
						if(index == 0){//Update the startCPU to start at the new time and finish t_slide earlier
							gap = abs_time-moveBack.get_event_time();
							moveBack.reset_start(moveBack.get_event_time() + gap);
							moveBack.set_finish_time(moveBack.get_finish_time() + gap - t_slide);
						} else {//The rest start at the new time, with running time the same
							moveBack.reset_start(moveBack.get_event_time() + gap - t_slide);
							moveBack.set_finish_time(moveBack.get_finish_time() + gap - t_slide);
						}
					}
					events.addAll(changee);
					eventlist.addAll(changee);
					sort_eventsList(eventlist);
				} else {
					if(!rr.isEmpty()) {
						nextCPUtime = event.get_finish_time() + t_cs;
					} else {//rr is empty, meaning there is one process taking over CPU and one 
						for(Event e: eventlist){//Find the first 
							if(e.get_status() == true && e.get_location() == 1){
								nextCPUtime = e.get_finish_time();
								break;
							}
						}
					}
					if(nextCPUtime > abs_time){
						abs_time = nextCPUtime;
					}
				}
			} else {//not first four
				boolean found = false;
				boolean preemption = false;
				MemoryLocation toBePreempted = process;//Initialization, to be changed later if there is preemption
				int prev = abs_time;
				System.out.printf("time %dms: ", event.get_event_time());
				if(process.get_ioTime() == 0 || (event.get_location() == 1 && event.get_status() == true) && process.get_curBurst() == process.get_numBurst() - 1){
					System.out.print(event.printTerminate());//If finished, print terminate
					int index = processList.get(process);
					int start = turnaround_time.get(index).get(0);
					int period = event.get_event_time() - start;
					t_total_turn_around += period - process.get_ioTime() * process.get_numBurst();
					t_total_burst += process.get_burstTime() * process.get_numBurst();
					t_total_wait = t_total_turn_around - t_total_burst;
					turnaround_time.get(index).clear();
					rr_stat[i][0] += t_total_turn_around;
					rr_stat[i][1] += t_total_wait;
					rr_stat[i][2] += t_total_burst;
					if(rr.contains(process)) {
						rr.remove(process);
						rr_stat[i][3] ++;
					}
					printQueue(rr);
					System.out.printf("time %dms: Simulated Memory:\n", event.get_event_time());
					m.deleteMemory(process);
					m.printMemory();
				} else{
					System.out.print(event.printEvent());
				}
				//Need to reset the rest events' interrupted to be true, only if the event is the 3rd out of 4.
				//Once the third is output, the time of 4th cannot be changed
				if(event.get_location() == 1 && event.get_status() == false){
					for(Event notchangeable : eventlist){
						if(notchangeable.get_location() == 1 && notchangeable.get_status() == true
								&& notchangeable.get_name().equals(event.get_name())
								&& notchangeable.get_numburst() == event.get_numburst()){
							notchangeable.set_interrupted(true);
							events.clear();
							events.addAll(eventlist);
						}
					}
				} else if(event.get_location() == 1 && event.get_status() == true){
					int cpu_time = process.get_burstTime();
					int io_time = process.get_ioTime();
					if(!m.get_occupied().isEmpty()) m.deleteMemory(process);
					//Add events for restarting
					if(process.get_curBurst() < process.get_numBurst() - 1){
						if(i == 0){
							m.addFirstAvailable(process);
						} else if(i == 1){
							m.addSecondAvailable(process);
						} else {
							m.addBestAvailable(process);
						}
						if(m.get_need_defragmentation() == true){
							System.out.printf("time %dms: Process '%s' unable to be added; lack of memory\n", event.get_event_time(), event.get_process_name());
							System.out.printf("time %dms: Starting defragmentation (suspending all processes)\n", event.get_name());
							m.defragmentation();
							int units = m.get_defragmention_moved_units();
							nextCPUtime += units * t_memmove;
							System.out.printf("time %dms: Completed defragmentation (moved %d memory units)\n", event.get_event_time() + units * t_memmove, units);
							abs_time = units * t_memmove;
							System.out.printf("time %dms: Simulated Memory:\n", event.get_event_time(), event.get_event_time() + units * t_memmove);
							m.printMemory();
							if(i == 0){
								m.addFirstAvailable(process);
							} else if(i == 1){
								m.addSecondAvailable(process);
							} else {
								m.addBestAvailable(process);
							}
						}
						rr.add(process);
						process.incur_curBurst();
						//Find the latest CPU start event
						//If found, abs_time = thatevent.get_finish_time() + t_cs
						//If not found (the current process which just completed IO, will occupy CPU) abs_time = event.get_finishtime() + t_cs
						for(Event CPUevent : eventlist){
							if(CPUevent.get_location() == 0 && CPUevent.get_status() == false){
								found = true;
								if(abs_time < CPUevent.get_finish_time()){
									abs_time = CPUevent.get_finish_time();
								}
							}
						}
						sort_eventsList(eventlist);
						if(found == false && has_unlockedEvents(eventlist) == true){
							preemption = true;
							for(Event CPUevent : eventlist){
								if(CPUevent.get_location() == 1 && CPUevent.get_status() == false ||
										CPUevent.get_location() == 0 && CPUevent.get_status() == true){
									toBePreempted = CPUevent.get_name();
								}
							}
							
						}
						if(found == false){
							abs_time = event.get_finish_time() + t_cs;
							nextCPUtime += t_cs;
						} else {
							abs_time += t_cs;
						}
						Event startCPU = new Event(abs_time, process, 0, false, abs_time+cpu_time);
						Event completeCPU = new Event(abs_time + cpu_time, process, 0, true, abs_time+cpu_time);
						Event startIO = new Event(abs_time + cpu_time, process, 1, false, abs_time+cpu_time+io_time);
						Event completeIO = new Event(abs_time + cpu_time + io_time, process, 1, true, abs_time + cpu_time + io_time);
						abs_time += cpu_time;
						events.add(startCPU);
						events.add(completeCPU);
						if(io_time >0){
							events.add(startIO);
							events.add(completeIO);
						}
						eventlist.add(startCPU);
						eventlist.add(completeCPU);
						if(io_time >0){
							eventlist.add(startIO);
							eventlist.add(completeIO);
						}
						sort_eventsList(eventlist);
					}
				}
				if(!m.get_occupied().isEmpty()){
					printQueue(rr);
				}
				//After that we need to think if the newly added process will preempt processes occupying CPU
				if(preemption == true && !toBePreempted.equals(process)){
					System.out.printf("time %dms: ", event.get_event_time());
					System.out.print(toBePreempted.printPreemption());
					int remaining = prev;
					int newstart = 0;
					int newcomplete = 0;
					for(Event e : eventlist){
						if(e.get_name().equals(process) && e.get_location() == 0){
							newstart = e.get_finish_time() + t_cs;
							break;
						}
					}
					newcomplete = newstart + remaining;
					Event resetStart = new Event(newstart, toBePreempted, 0, false, newstart + remaining);
					abs_time = newcomplete;
					eventlist.add(resetStart);
					sort_eventsList(eventlist);
					int gap = 0;
					boolean changed = false;
					for(Event e : eventlist){
						if(!e.get_name().equals(process) && !e.equals(resetStart)){
							if(e.get_name().equals(toBePreempted) && 
									(e.get_location() == 0 && e.get_status() == true ||
									 e.get_location() == 1 && e.get_status() == false)){
								if(changed == false){
									gap = newcomplete - e.get_event_time();
									changed = true;
								}
							}
							if(gap > 0 && e.get_interrupted() == false){
								e.reset_start(e.get_event_time() + gap);
								e.set_finish_time(e.get_finish_time() + gap);
							}
						}
					}
					events.clear();
					events.addAll(eventlist);
					rr.add(toBePreempted);
					printQueue(rr);
				}
				
			}
			if(rr.isEmpty() && events.isEmpty()){
				nextCPUtime = event.get_finish_time();
			}
		}
		System.out.printf("time %dms: Simulator ended\n\n", nextCPUtime);
		total_time = nextCPUtime;
		rr_stat[i][5] = total_time;
	}
	public static void printQueue(LinkedList<MemoryLocation> exec){
		System.out.print("[Q");
		for (int i = 0; i < exec.size(); i++){
			System.out.printf(" %s", exec.get(i).get_name());
		}
		System.out.println("]");
	}
	
	public static ArrayList<Event> sort_eventsList(ArrayList<Event> events){
		Collections.sort(events, new Comparator<Event>(){
			public int compare(Event e1, Event e2){
				return e1.get_event_time() - e2.get_event_time();
			}
		});
		return events;
	}
	
	public static void rr_printNextEvents(Memory m, ArrayList<Event> eventlist, PriorityQueue<Event> events, LinkedList<MemoryLocation> rr, int nextCPUtime, int i){
		int t_total_turn_around = 0;
		int t_total_burst = 0;
		int t_total_wait = 0;
		while(!events.isEmpty() && events.peek().get_finish_time() <= nextCPUtime){
			//Print all events finishing before next CPU event
			Event execute = events.poll();
			eventlist.remove(execute);
			MemoryLocation process = execute.get_name();
			int cpu_time = process.get_burstTime();
			int io_time = process.get_ioTime();
			if(execute.get_location() == 0 && execute.get_status() == false){
				rr.remove(process);
				rr_stat[i][3] ++;
			}
			if(execute.get_location() == -1){
				if(i == 0){
					m.addFirstAvailable(process);
				} else if(i == 1){
					m.addSecondAvailable(process);
				} else {
					m.addBestAvailable(process);
				}
				if(m.get_need_defragmentation() == true){
					System.out.printf("time %dms: Process '%s' unable to be added; lack of memory\n", execute.get_event_time(), execute.get_process_name());
					System.out.printf("time %dms: Starting defragmentation (suspending all processes)\n", execute.get_name());
					m.defragmentation();
					int units = m.get_defragmention_moved_units();
					nextCPUtime += units * t_memmove;
					rr_stat[i][4] += units * t_memmove;
					System.out.printf("time %dms: Completed defragmentation (moved %d memory units)\n", execute.get_event_time() + units * t_memmove, units);
					abs_time = units * t_memmove;
					System.out.printf("time %dms: Simulated Memory:\n", execute.get_event_time(), execute.get_event_time() + units * t_memmove);
					m.printMemory();
					if(i == 0){
						m.addFirstAvailable(process);
					} else if(i == 1){
						m.addSecondAvailable(process);
					} else {
						m.addBestAvailable(process);
					}
				}
				abs_time += t_cs;
				Event startCPU = new Event(abs_time, process, 0, false, abs_time+cpu_time);
				Event completeCPU = new Event(abs_time + cpu_time, process, 0, true, abs_time+cpu_time);
				Event startIO = new Event(abs_time + cpu_time, process, 1, false, abs_time+cpu_time+io_time);
				Event completeIO = new Event(abs_time + cpu_time + io_time, process, 1, true, abs_time + cpu_time + io_time);
				abs_time += cpu_time;
				events.add(startCPU);
				events.add(completeCPU);
				if(io_time >0){
					events.add(startIO);
				events.add(completeIO);
				}
				eventlist.add(startCPU);
				eventlist.add(completeCPU);
				if(io_time >0){
					eventlist.add(startIO);
					eventlist.add(completeIO);
				}
			}
			System.out.printf("time %dms: ", execute.get_event_time());
			if(execute.get_location() == -1){
				System.out.print(execute.printEvent());
				rr.add(process);
				int index = processList.get(process);
				ArrayList<Integer> time_in = turnaround_time.get(index);
				time_in.add(execute.get_event_time());
				printQueue(rr);
				System.out.printf("time %dms: ", execute.get_event_time());
				System.out.println("Simulated Memory:");
				m.printMemory();
			} else if(process.get_ioTime() == 0 || (execute.get_location() == 1 && execute.get_status() == true) && process.get_curBurst() == process.get_numBurst() - 1){
				System.out.print(execute.printTerminate());//If finished, print terminate
				int index = processList.get(process);
				int start = turnaround_time.get(index).get(0);
				int period = execute.get_event_time() - start;
				t_total_turn_around += period - process.get_ioTime() * process.get_numBurst();
				t_total_burst += process.get_burstTime() * process.get_numBurst();
				t_total_wait = t_total_turn_around - t_total_burst;
				turnaround_time.get(index).clear();
				rr_stat[i][0] += t_total_turn_around;
				rr_stat[i][1] += t_total_wait;
				rr_stat[i][2] += t_total_burst;
				m.deleteMemory(process);
				if(rr.contains(process)) {
					rr.remove(process);
					rr_stat[i][3] ++;
				}
				printQueue(rr);
				System.out.printf("time %dms: Simulated Memory:\n", execute.get_event_time());
				m.printMemory();
			} else {
				boolean found = false;
				boolean preemption = false;
				MemoryLocation toBePreempted = process;//Initialization, to be changed later if there is preemption
				int prev = abs_time;//Prev indicates when the process is supposed to finish
				System.out.print(execute.printEvent());
				//Need to reset the rest events' interrupted to be true, only if the event is the 3rd out of 4.
				//Once the third is output, the time of 4th cannot be changed
				if(execute.get_location() == 1 && execute.get_status() == false){//3rd event, need to lock the 4th event
					for(Event notchangeable : eventlist){
						if(notchangeable.get_location() == 1 && notchangeable.get_status() == true
								&& notchangeable.get_name().equals(execute.get_name())
								&& notchangeable.get_numburst() == execute.get_numburst()){
							notchangeable.set_interrupted(true);
							events.clear();
							events.addAll(eventlist);
						}
					}
					printQueue(rr);
				} else if(execute.get_location() == 1 && execute.get_status() == true){//4th event
					m.deleteMemory(process);
					//Add events for restarting
					if(process.get_curBurst() < process.get_numBurst() - 1){
						if(i == 0){
							m.addFirstAvailable(process);
						} else if(i == 1){
							m.addSecondAvailable(process);
						} else {
							m.addBestAvailable(process);
						}
						if(m.get_need_defragmentation() == true){
							rr.add(process);
							printQueue(rr);
							System.out.printf("time %dms: Process '%s' unable to be added; lack of memory\n", execute.get_event_time(), execute.get_process_name());
							System.out.printf("time %dms: Starting defragmentation (suspending all processes)\n", execute.get_event_time());
							System.out.printf("time %dms: stimulated memory:\n",execute.get_event_time());
							m.printMemory();
							m.defragmentation();
							int units = m.get_defragmention_moved_units();
							nextCPUtime += units * t_memmove;
							rr_stat[i][4] += units * t_memmove;
							System.out.printf("time %dms: Completed defragmentation (moved %d memory units)\n", execute.get_event_time() + units * t_memmove, units);
							System.out.printf("time %dms: Simulated Memory:\n", execute.get_event_time() + units * t_memmove);
							m.printMemory();
							if(i == 0){
								m.addFirstAvailable(process);
							} else if(i == 1){
								m.addSecondAvailable(process);
							} else {
								m.addBestAvailable(process);
							}
							System.out.printf("time %dms: Process '%s' added to the system\n", execute.get_event_time() + units * t_memmove, execute.get_process_name());
							System.out.printf("time %dms: Simulated Memory:\n", execute.get_event_time() + units * t_memmove);
							m.printMemory();
							execute.set_finish_time(execute.get_finish_time() + units * t_memmove);
							//execute.reset_start(execute.get_event_time() + units * t_memmove);
						} else{
							rr.add(process);
							printQueue(rr);
						}
						process.incur_curBurst();
				    }
					//Find the latest CPU start event
					//If found, abs_time = thatevent.get_finish_time() + t_cs
					//If not found (the current process which just completed IO, will occupy CPU) abs_time = event.get_finishtime() + t_cs
					for(Event CPUevent : eventlist){
						if(CPUevent.get_location() == 0 && CPUevent.get_status() == false){
							found = true;
							if(abs_time < CPUevent.get_finish_time()){
								abs_time = CPUevent.get_finish_time();
							}
						}
					}
					sort_eventsList(eventlist);
					if(found == false && has_unlockedEvents(eventlist) == true){
						preemption = true;
						for(Event CPUevent : eventlist){
							if(CPUevent.get_location() == 1 && CPUevent.get_status() == false ||
									CPUevent.get_location() == 0 && CPUevent.get_status() == true){
								toBePreempted = CPUevent.get_name();
							}
						}
						
					}
					if(preemption == true){
						for(Event CPUevent : eventlist){
							if(CPUevent.get_name().equals(toBePreempted)){
								if(CPUevent.get_status() == true && CPUevent.get_location() == 0 ||
								   CPUevent.get_status() == false && CPUevent.get_location() == 1){
									prev = CPUevent.get_event_time() - execute.get_event_time();
									break;
								}
							}
						}
					}
					if(preemption == true && nextCPUtime > execute.get_finish_time()){
						nextCPUtime = execute.get_finish_time() + t_slide;
					}
					if(found == false){
						abs_time = execute.get_finish_time() + t_cs;
						nextCPUtime += t_cs;
					} else {
						abs_time += t_cs;
					}
					Event startCPU = new Event(abs_time, process, 0, false, abs_time+cpu_time);
					Event completeCPU = new Event(abs_time + cpu_time, process, 0, true, abs_time+cpu_time);
					Event startIO = new Event(abs_time + cpu_time, process, 1, false, abs_time+cpu_time+io_time);
					Event completeIO = new Event(abs_time + cpu_time + io_time, process, 1, true, abs_time + cpu_time + io_time);
					abs_time += cpu_time;
					events.add(startCPU);
					events.add(completeCPU);
					if(io_time >0){
						events.add(startIO);
						events.add(completeIO);
					}
					eventlist.add(startCPU);
					eventlist.add(completeCPU);
					if(io_time >0){
						eventlist.add(startIO);
						eventlist.add(completeIO);
					}
					sort_eventsList(eventlist);
			} else if(execute.get_status() == false && execute.get_location() == 0){//1st event
				//Find the latest CPU start event
				//If found, abs_time = thatevent.get_finish_time() + t_cs
				//If not found (the current process which just completed IO, will occupy CPU) abs_time = event.get_finishtime() + t_cs
				for(Event CPUevent : eventlist){
					if(CPUevent.get_location() == 0 && CPUevent.get_status() == false){
						found = true;
						if(abs_time < CPUevent.get_finish_time()){
							abs_time = CPUevent.get_finish_time();
						}
					}
				}
				sort_eventsList(eventlist);
				if(found == false && has_unlockedEvents(eventlist) == true){
					preemption = true;
					for(Event CPUevent : eventlist){
						if(CPUevent.get_location() == 1 && CPUevent.get_status() == false ||
								CPUevent.get_location() == 0 && CPUevent.get_status() == true){
							toBePreempted = CPUevent.get_name();
						}
					}
					
				}
				if(preemption == true){
					for(Event CPUevent : eventlist){
						if(CPUevent.get_name().equals(toBePreempted)){
							if(CPUevent.get_status() == true && CPUevent.get_location() == 0 ||
							   CPUevent.get_status() == false && CPUevent.get_location() == 1){
								prev = CPUevent.get_event_time() - execute.get_event_time();
								break;
							}
						}
					}
				}
				if(preemption == true && nextCPUtime > execute.get_finish_time()){
					nextCPUtime = execute.get_finish_time() + t_slide;
				}
				if(found == false){
					abs_time = execute.get_finish_time() + t_cs;
					nextCPUtime += t_cs;
				} else {
					abs_time += t_cs;
				}
				Event startCPU = new Event(abs_time, process, 0, false, abs_time+cpu_time);
				Event completeCPU = new Event(abs_time + cpu_time, process, 0, true, abs_time+cpu_time);
				Event startIO = new Event(abs_time + cpu_time, process, 1, false, abs_time+cpu_time+io_time);
				Event completeIO = new Event(abs_time + cpu_time + io_time, process, 1, true, abs_time + cpu_time + io_time);
				abs_time += cpu_time;
				events.add(startCPU);
				events.add(completeCPU);
				if(io_time >0){
					events.add(startIO);
					events.add(completeIO);
				}
				eventlist.add(startCPU);
				eventlist.add(completeCPU);
				if(io_time >0){
					eventlist.add(startIO);
					eventlist.add(completeIO);
				}
				sort_eventsList(eventlist);
				printQueue(rr);
			} else {
				printQueue(rr);
			}
			//After that we need to think if the newly added process will preempt processes occupying CPU
			if(preemption == true && !toBePreempted.equals(process)){
					System.out.printf("time %dms: ", execute.get_finish_time());
					System.out.print(toBePreempted.printPreemption());
					int remaining = prev;
					int newstart = 0;
					int newcomplete = 0;
					for(Event e : eventlist){
						if(e.get_name().equals(process) && e.get_location() == 0){
							newstart = e.get_finish_time() + t_cs;
							break;
						}
					}
					newcomplete = newstart + remaining;
					Event resetStart = new Event(newstart, toBePreempted, 0, false, newstart + remaining);
					abs_time = newcomplete;
					eventlist.add(resetStart);
					sort_eventsList(eventlist);
					int gap = 0;
					boolean changed = false;
					for(Event e : eventlist){
						if(!e.get_name().equals(process) && !e.equals(resetStart)){
							if(e.get_name().equals(toBePreempted) && 
									(e.get_location() == 0 && e.get_status() == true ||
									 e.get_location() == 1 && e.get_status() == false)){
								if(changed == false){
									gap = newcomplete - e.get_event_time();
									changed = true;
								}
							}
							if(gap > 0 && e.get_interrupted() == false){
								e.reset_start(e.get_event_time() + gap);
								e.set_finish_time(e.get_finish_time() + gap);
							}
						}
					}
					events.clear();
					events.addAll(eventlist);
					rr.add(toBePreempted);
					printQueue(rr);
			}
			}

		}
	}
	

	
	public static boolean has_unlockedEvents(ArrayList<Event> eventlist){
		if(eventlist.isEmpty()) return false;
		for(Event e : eventlist){
			if(e.get_location() == 1) return true;
			else if (e.get_location() == 0 && e.get_status() == true) return true;
		}
		return false;
	}
}

