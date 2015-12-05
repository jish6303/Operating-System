

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class ProjectSrt {
	
	public static DataOb srtPro(String fileName, int i) throws IOException{
		/*
		if (args.length == 0){
			System.out.println("Please input the file name as an argument!");
			System.exit(-1);
		}
		if (args.length !=1){	//check the number of args
			System.out.println("Wrong args!");
			System.exit(-1);
		}
		*/ 
    	
    	double result3 = 0;
		double tt_time = 0;
		int sm_time = 0;
		int df_time = 0;
		int t_memmove = 10;
		double result3_ = 0;
		Memory m = new Memory();
		HashMap<Integer , MemoryLocation> mapMemo = new HashMap<Integer, MemoryLocation>();
		HashMap<Integer , Process> map = new HashMap<Integer , Process>();	//hashmap: store all info of processes 
		Queue<Integer> q = new LinkedList<Integer>();	//queue: store the order of processes
		int t_cs = 0;	//current time
		int n = 0;	//number of processes read
		int times = 0;

		int switchches = 0;
		
		File file = new File(fileName);	//read file
	    BufferedReader reader = null;
	    	try {
	    		reader = new BufferedReader(new FileReader(file));
	            String tempString = null;
	            String[] parts;

	            while ((tempString = reader.readLine()) != null) {	//store the info of each process into object process
	            	if (tempString.startsWith("#") || (tempString == "\n") || tempString.startsWith(" ")){
	            		continue;
	            	}else{
	            		parts = tempString.split("\\|");					
	            		Process process = new Process();	//instantiate the object for each process
	            		process.setProcNum(saver(parts[0].charAt(0)));
	            		process.setArrivalTime(Integer.parseInt(parts[1]));
	            		process.setBurstTime(Integer.parseInt(parts[2]));
	            		process.setOriginBurstTime(Integer.parseInt(parts[2]));
	            		process.setNumBurst(Integer.parseInt(parts[3])-1);
	            		process.setIoTime(Integer.parseInt(parts[4]));
	            		process.setMemory(Integer.parseInt(parts[5]));
	            		process.setIsWaitingForIoComplete(false);
	            		process.setOrder(n);
	            		process.setWaiting(true);
	            		String name = parts[0];
	            		int memory = Integer.parseInt(parts[5]);
	            		MemoryLocation memo = new MemoryLocation(name, -1, -1, -1, -1, memory);
	            		mapMemo.put(process.getProNum(), memo);
	            		map.put(process.getProNum(), process);	//put the process info into hashmap
	            		if(Integer.parseInt(parts[1])==0){
	            			q.add(process.getProNum());
	            			map.get(process.getProNum()).printInfo(7, process.getArrivalTime(), buildQueueString(q).toString() , -1);
	            			process.setWaiting(false);
	            			if(i == 0){
	            				m.addFirstAvailable(memo);
	            			} else if(i == 1){
	            				m.addSecondAvailable(memo);
	            			} else {
	            				m.addBestAvailable(memo);
	            			}
	            			m.printMemory();
	            		}
	            		n++;
	            	}
	            }
	            reader.close();
	        } catch (IOException e) {	//IO exception
	            e.printStackTrace();
	            System.exit(-1);
	        } finally {
	            if (reader != null) {
	                try {
	                    reader.close();
	                } catch (IOException e1) {
	                }
	            }
	        }
	    	for (Process value : map.values()) {
				result3_ = result3_ + value.getNumBurst();
				result3 = result3 + value.getBurstTime()*value.getNumBurst();
			}
	    	
	    	int temp1 = 0;
	    	for(Process p : map.values()){
	    		temp1 = temp1 + p.getOringinBurstTime()*(p.getNumBurst()+1);
	    		times = times + (p.getNumBurst()+1);
	 
	    	}
	    	
	    	sortPrior(q, map);
	    	
	    	while(q.peek() != null){	//when queue is not empty, then run code below
	    		
	    		int currentProc = q.poll();	//take out the first process from queue
	    		t_cs = t_cs + 13;
    			
	    		int prior;
	    		int endCheck;	//check whether all processes end
	    		int newComing;
	    		
	    		while((prior = priorProcess(t_cs, map))!=-1){	//fist check of prior process which needs io, between io finishing and new cpu burst starting
	    			int tempTime = map.get(prior).getIoCompleteTime();
	    			Boolean preempted = false;
	    			int temp = 0;

	    			//--
	    			while((newComing = checkWaiting(map, map.get(prior).getIoCompleteTime()))!=-1){
	    				q.add(newComing);  					    				
	    				if(i == 0){
	    					m.addFirstAvailable(mapMemo.get(newComing));
	    				} else if(i == 1){
	    					m.addSecondAvailable(mapMemo.get(newComing));
	    				} else {
	    					m.addBestAvailable(mapMemo.get(newComing));
	    				}
	    				if(m.get_need_defragmentation() == true){
            				System.out.printf("time %dms: Process '%s' unable to be added; lack of memory\n", map.get(newComing).getArrivalTime(),newComing);
							System.out.printf("time %dms: Starting defragmentation (suspending all processes)\n", map.get(newComing).getArrivalTime());
							System.out.printf("time %dms: stimulated memory:\n",map.get(newComing).getArrivalTime());
							m.printMemory();
							m.defragmentation();
							System.out.printf("time %dms: Completed defragmentation (moved %d memory units)\n", map.get(newComing).getArrivalTime() + m.get_defragmention_moved_units() * t_memmove, m.get_defragmention_moved_units());
							System.out.printf("time %dms: Simulated Memory:\n", map.get(newComing).getArrivalTime() + m.get_defragmention_moved_units() * t_memmove);
							df_time = df_time + m.get_defragmention_moved_units() * t_memmove;
							m.printMemory();
							if(i == 0){
		    					m.addFirstAvailable(mapMemo.get(newComing));
		    				} else if(i == 1){
		    					m.addSecondAvailable(mapMemo.get(newComing));
		    				} else {
		    					m.addBestAvailable(mapMemo.get(newComing));
		    				}
							System.out.printf("time %dms: Process '%s' added to the system\n", map.get(newComing).getArrivalTime() + m.get_defragmention_moved_units() * t_memmove, newComing);
							System.out.printf("time %dms: Simulated Memory:\n", map.get(newComing).getArrivalTime() + m.get_defragmention_moved_units() * t_memmove);
							m.printMemory();
            			}else{
            				map.get(newComing).printInfo(7, map.get(newComing).getArrivalTime(), buildQueueString(q).toString() , -1);
            				m.printMemory();
            			}
            			sortPrior(q, map);
            			map.get(newComing).setWaiting(false);
	    				/*if(checkPreempted(newComing, map, currentProc, map.get(currentProc).getBurstTime())){
	    					preempted=true;			
	    					temp1 = currentProc;
	    					currentProc = newComing;
	    					
	    					Queue<Integer> tempq1 = new LinkedList<Integer>();
	    	    			for (int i = 0; i< q.size()-1; i++){
	    	    				tempq1.add(q.poll());
	    	    			}
	    					q.clear();
	    					for (Integer x : tempq1){
	    	    				q.add(x);
	    	    			}
	    					q.add(currentProc);
	    					
	    					sortPrior(q, map);
	    					map.get(temp1).printInfo(6, map.get(newComing).getArrivalTime(), buildQueueString(q).toString(), currentProc);
	    					t_cs = map.get(newComing).getArrivalTime()+13;
	    					map.get(newComing).printInfo(1, t_cs, buildQueueString(q).toString(), -1);
	    					switchches++;
	    					preempted = false;	    					
	    				}
	    				*/
	    				
	    			}
	    			//--
	    			Queue<Integer> tempq = new LinkedList<Integer>();
	    			for (Integer x : q){
	    				tempq.add(x);
	    			}
	    			if(map.get(prior).getNumBurst()!=0){
	    				if(checkPreempted(prior, map, currentProc, map.get(currentProc).getBurstTime())){
	    					preempted = true;
	    					q.add(currentProc);
	    					temp = currentProc;
	    					currentProc = prior;
	    					sortPrior(q, map);
	    				}else{
	    					tempq.add(prior);
	    					q.add(prior);	//if process io ends, check the number of burst left, and put back to the queue if possible
	    					sortPrior(q, map);
	    					sortPrior(tempq, map);
	    				}
	    				map.get(prior).printInfo(4, tempTime, buildQueueString(tempq).toString(), -1);
	    				m.deleteMemory(mapMemo.get(prior));
	    				if(i == 0){
	    					m.addFirstAvailable(mapMemo.get(prior));
	    				} else if(i == 1){
	    					m.addSecondAvailable(mapMemo.get(prior));
	    				} else {
	    					m.addBestAvailable(mapMemo.get(prior));
	    				}
	    				if(m.get_need_defragmentation() == true){
            				System.out.printf("time %dms: Process '%s' unable to be added; lack of memory\n", map.get(newComing).getArrivalTime(),newComing);
							System.out.printf("time %dms: Starting defragmentation (suspending all processes)\n", map.get(newComing).getArrivalTime());
							System.out.printf("time %dms: stimulated memory:\n",map.get(newComing).getArrivalTime());
							m.printMemory();
							m.defragmentation();
							System.out.printf("time %dms: Completed defragmentation (moved %d memory units)\n", map.get(newComing).getArrivalTime() + m.get_defragmention_moved_units() * t_memmove, m.get_defragmention_moved_units());
							System.out.printf("time %dms: Simulated Memory:\n", map.get(newComing).getArrivalTime() + m.get_defragmention_moved_units() * t_memmove);
							df_time = df_time + m.get_defragmention_moved_units() * t_memmove;
							m.printMemory();
							if(i == 0){
		    					m.addFirstAvailable(mapMemo.get(newComing));
		    				} else if(i == 1){
		    					m.addSecondAvailable(mapMemo.get(newComing));
		    				} else {
		    					m.addBestAvailable(mapMemo.get(newComing));
		    				}	
            			}
	    				map.get(prior).setNumBurst(map.get(prior).getNumBurst()-1);
	    				map.get(prior).setIsWaitingForIoComplete(false); //reset io prior info
	    				
	    				if(preempted){
	    					map.get(temp).printInfo(6, map.get(prior).getIoCompleteTime(), buildQueueString(q).toString(), currentProc);
	    					t_cs = map.get(prior).getIoCompleteTime()+13;
	    					map.get(prior).printInfo(1, t_cs, buildQueueString(q).toString(), -1);
	    					switchches++;
	    					preempted = false;
	    					t_cs = t_cs+map.get(prior).getBurstTime();
	    				}
	    				map.get(prior).setIoCompleteTime(0);
	    				
	    			}
	    		}
	    		
	    		map.get(currentProc).printInfo(1, t_cs, buildQueueString(q).toString(), -1);
	    		switchches++;
	    		t_cs = t_cs + map.get(currentProc).getBurstTime();
	    		
	    		while((prior = priorProcess(t_cs, map))!=-1){	// second check of prior process which needs io, between cpu burst starting and cpu burst finishing
	    			
	    			int tempTime = map.get(prior).getIoCompleteTime();
	    			Boolean preempted = false;
	    			int temp = 0;
	    			
	    			while((newComing = checkWaiting(map, map.get(prior).getIoCompleteTime()))!=-1){
	    				q.add(newComing);
	    				map.get(newComing).printInfo(7, map.get(newComing).getArrivalTime(), buildQueueString(q).toString() , -1);
	    				sortPrior(q, map);
	    				map.get(newComing).setWaiting(false);
	    				if(i == 0){
            				m.addFirstAvailable(mapMemo.get(newComing));
            			} else if(i == 1){
            				m.addSecondAvailable(mapMemo.get(newComing));
            			} else {
            				m.addBestAvailable(mapMemo.get(newComing));
            			}
	    				if(m.get_need_defragmentation() == true){
            				System.out.printf("time %dms: Process '%s' unable to be added; lack of memory\n", map.get(newComing).getArrivalTime(),newComing);
							System.out.printf("time %dms: Starting defragmentation (suspending all processes)\n", map.get(newComing).getArrivalTime());
							System.out.printf("time %dms: stimulated memory:\n",map.get(newComing).getArrivalTime());
							m.printMemory();
							m.defragmentation();
							System.out.printf("time %dms: Completed defragmentation (moved %d memory units)\n", map.get(newComing).getArrivalTime() + m.get_defragmention_moved_units() * t_memmove, m.get_defragmention_moved_units());
							System.out.printf("time %dms: Simulated Memory:\n", map.get(newComing).getArrivalTime() + m.get_defragmention_moved_units() * t_memmove);
							df_time = df_time + m.get_defragmention_moved_units() * t_memmove;
							m.printMemory();
							if(i == 0){
		    					m.addFirstAvailable(mapMemo.get(newComing));
		    				} else if(i == 1){
		    					m.addSecondAvailable(mapMemo.get(newComing));
		    				} else {
		    					m.addBestAvailable(mapMemo.get(newComing));
		    				}
							System.out.printf("time %dms: Process '%s' added to the system\n", map.get(newComing).getArrivalTime() + m.get_defragmention_moved_units() * t_memmove, newComing);
							System.out.printf("time %dms: Simulated Memory:\n", map.get(newComing).getArrivalTime() + m.get_defragmention_moved_units() * t_memmove);
							m.printMemory();
            			}else{
            				map.get(newComing).printInfo(7, map.get(newComing).getArrivalTime(), buildQueueString(q).toString() , -1);
            				m.printMemory();
            			}
	    			}
	    			
	    			Queue<Integer> tempq = new LinkedList<Integer>();
	    			for (Integer x : q){
	    				tempq.add(x);
	    			}
	    			if(map.get(prior).getNumBurst()!=0){
	    				if(checkPreempted(prior, map, currentProc, t_cs-map.get(prior).getIoCompleteTime())){
	    					preempted = true;
	    					q.add(currentProc);
	    					temp = currentProc;
	    					currentProc = prior;
	    					sortPrior(q, map);
	    					map.get(temp).setBurstTime(t_cs-map.get(prior).getIoCompleteTime());
	    				}else{
	    					tempq.add(prior);
	    					q.add(prior);	//if process io ends, check the number of burst left, and put back to the queue if possible
	    					sortPrior(q, map);
	    					sortPrior(tempq, map);
	    				}
	    				map.get(prior).printInfo(4, tempTime, buildQueueString(tempq).toString(), -1);
	    				map.get(prior).setNumBurst(map.get(prior).getNumBurst()-1);
	    				map.get(prior).setIsWaitingForIoComplete(false); //reset io prior info
	    				
	    				if(preempted){
	    					map.get(temp).printInfo(6, map.get(prior).getIoCompleteTime(), buildQueueString(q).toString(), currentProc);
	    					t_cs = map.get(prior).getIoCompleteTime()+13;
	    					map.get(prior).printInfo(1, t_cs, buildQueueString(q).toString(), -1);
	    					switchches++;
	    					preempted = false;
	    					t_cs = t_cs+map.get(prior).getBurstTime();
	    				}
	    				map.get(prior).setIoCompleteTime(0);
	    				
	    			}
	    		}
	    		
	    		if(map.get(currentProc).getNumBurst()==0){	//complete all cpu burst, print terminated info
	    			map.get(currentProc).printInfo(5, t_cs, buildQueueString(q).toString(), -1);
	    			tt_time = tt_time - map.get(currentProc).getArrivalTime() + tt_time;
	    			sm_time = t_cs;
	    			m.deleteMemory(mapMemo.get(currentProc));
        			m.printMemory();
	    			map.get(currentProc).setIsWaitingForIoComplete(false);
	    		}else{	//still need burst, print burst complete info
	    			map.get(currentProc).printInfo(2, t_cs, buildQueueString(q).toString(), -1);
	    			map.get(currentProc).setBurstTime(map.get(currentProc).getOringinBurstTime());
	    		}
	    		
	    		
	    		if((map.get(currentProc).getIoTime()!=0) && (map.get(currentProc).getNumBurst()!=0)){	//check whether process needs io
	    			map.get(currentProc).printInfo(3, t_cs, buildQueueString(q).toString(), -1);
	    			map.get(currentProc).setIoCompleteTime(t_cs + map.get(currentProc).getIoTime());
	    			map.get(currentProc).setIsWaitingForIoComplete(true);
	    		}
	    		
	    		while ( (q.peek() == null) && (( endCheck = nextOutput(map))!=-1) ){ //when queue empty, still some processes need burst, check next availability
	    			map.get(endCheck).setIsWaitingForIoComplete(false);
	    			if(map.get(endCheck).getNumBurst()==0){
	    				map.get(endCheck).printInfo(4, map.get(endCheck).getIoCompleteTime(), buildQueueString(q).toString(), -1);
	    				t_cs = map.get(endCheck).getIoCompleteTime();
	    				map.get(endCheck).setIoCompleteTime(0);
	    			}else{
	    				q.add(endCheck);	//put next available process back to the queue
	    				sortPrior(q, map);
	    				map.get(endCheck).setNumBurst(map.get(endCheck).getNumBurst()-1);
	    				map.get(endCheck).printInfo(4, map.get(endCheck).getIoCompleteTime() , buildQueueString(q).toString(), -1);
	    				t_cs = map.get(endCheck).getIoCompleteTime();
	    				map.get(endCheck).setIoCompleteTime(0);
	    				break;
	    			}	    			
	    		}
	    	}   


	    	for (Process value : map.values()) {
				tt_time = tt_time - value.getIoTime()*(value.getNumBurst()-1);
			}
	    	System.out.println("time " + t_cs +"ms: Simulator for SRT ended [Q]\n\n");
	    	DataOb dataOb = new DataOb();
	    	dataOb.att = tt_time/result3_;
	    	dataOb.acbt = result3/result3_;
	    	dataOb.awt = dataOb.att-dataOb.acbt;
	    	dataOb.ncs = switchches;
	    	dataOb.tst = sm_time;
	    	dataOb.tdt = df_time;
	    	dataOb.dp = dataOb.tdt/dataOb.tst;
	    	
	    	return dataOb;
	}
	
	public static int checkWaiting(HashMap<Integer , Process> currentProcessMap, int nextTime) {
		int newComing = -1;
		int tempTime = nextTime;
		int tempOrder = 100;
		for (Process value : currentProcessMap.values()) {
			if(value.getWaiting()){
				if(value.getArrivalTime()<tempTime){
					if(value.getOrder()<tempOrder){
						newComing = value.getProNum();
						tempOrder = value.getOrder();
					}
				}
			}
		}
		return newComing;
	}
	
	public static StringBuilder buildQueueString(Queue<Integer> q){	//build the queue string for printing 
		StringBuilder qOut = new StringBuilder();
		qOut.append("[Q");	
    	for (Integer x : q) { 
    		qOut.append(" ");
    		qOut.append(x);
    	}
    	qOut.append("]");
		return qOut;
	}	
	
	public static int priorProcess(int nextOutputTime, HashMap<Integer , Process> currentProcessMap){	//check the process who needs print first
		
		int tempTime = nextOutputTime;
		int tempKey = -1;
		
		for (Process value : currentProcessMap.values()) {
			if(value.getIsWaitingForIoComplete()){
				if((value.getIoCompleteTime()!=0) && (value.getIoCompleteTime() < tempTime)){
					tempKey = value.getProNum();
					tempTime = value.getIoCompleteTime();
				}
			}
		}
		
		return tempKey;
	}
	
	public static int nextOutput(HashMap<Integer , Process> currentProcessMap){	//check the process left who needs burst when queue is empty
		int nextKey = -1;
		int nextTime = 0;
		
		for (Process value : currentProcessMap.values()) {
			if(value.getIsWaitingForIoComplete()){
				nextTime = value.getIoCompleteTime();
				nextKey = value.getProNum();
				break;
			}
		}
		if (nextTime != 0){
			for (Process value : currentProcessMap.values()) {
				if(value.getIsWaitingForIoComplete()){
					if(value.getIoCompleteTime()<nextTime){
						nextTime = value.getIoCompleteTime();
						nextKey = value.getProNum();
					}
				}
			}
		}
		
		return nextKey;
	}
	public static void sortPrior(Queue q, HashMap<Integer , Process> map){
		int[] qArray = new int[q.size()];
		int index = 0;
		while(q.peek()!=null){
			qArray[index]=(int) q.poll();
			index++;
		}
		int temp = 0;
		for(int i = 0; i<qArray.length; i++){
			for(int j = i+1; j<qArray.length; j++){
				if(map.get(qArray[i]).getBurstTime() > map.get(qArray[j]).getBurstTime()){
					temp = qArray[j];
					qArray[j] = qArray[i];
					qArray[i] = temp;
				}
			}		
		}
		
		for(int i = 0; i<qArray.length; i++){
			q.add(qArray[i]);
		}
	}
	public static int saver(char x){
		return (x-64);
	}
	

	public static Boolean checkPreempted(int newProcess, HashMap<Integer , Process> map, int oldProcess, int burstLeftTime){
		if (map.get(newProcess).getBurstTime() < burstLeftTime){
		return true;
		}
		return false;
	}
}
