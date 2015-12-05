
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Memory {
	private boolean need_defrag;
	private int units_moved;
	private final int capacity = 256;
	private HashMap<MemoryLocation, ArrayList<Integer>> occupied = new HashMap<MemoryLocation, ArrayList<Integer>>();
	//private boolean content[] = new boolean[capacity];
	private ArrayList<MemoryLocation> memProcessList = new ArrayList<MemoryLocation>();
	
	public ArrayList<ArrayList<Integer>> get_available(){
		ArrayList<ArrayList<Integer>> res = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> slots = new ArrayList<Integer>();
		slots.add(0);
		slots.add(255);
		res.add(slots);
		for(int i = 0; i < memProcessList.size(); i++) {
			MemoryLocation p = memProcessList.get(i);
			ArrayList<Integer> bound = occupied.get(p);
			int occupied_from = bound.get(0);
			int occupied_to = bound.get(1);
			ArrayList<Integer> left = new ArrayList<Integer>();
			ArrayList<Integer> right = new ArrayList<Integer>();
			for(ArrayList<Integer> slot: res){
				int free_from = slot.get(0);
				int free_to = slot.get(1);
				if(free_from <= occupied_from && free_to >= occupied_to){
					res.remove(slot);
					if(free_from <= occupied_from - 1){
						left.add(free_from);
						left.add(occupied_from - 1);
					}
					if(free_to >= occupied_to + 1){
						right = new ArrayList<Integer>();
						right.add(occupied_to + 1);
						right.add(free_to);
					}
					if(!left.isEmpty() || !right.isEmpty()){
						if(!left.isEmpty()){
							res.add(left);
						}
						if(!right.isEmpty()){
							res.add(right);
						}
						break;
					}
				}
			}
		}
		return res;
	}
	
	public void printMemory(){
		for(int i = 0; i < 32; i++){
			System.out.print("=");
		}
		System.out.println();
		for(int i = 0; i < capacity; i++){
			boolean output = false;
			for(int j = 0; j < memProcessList.size(); j++){
				MemoryLocation p = memProcessList.get(j);
				if(i >= occupied.get(p).get(0) && i <= occupied.get(p).get(1)){
					System.out.printf("%s",p.get_name());
					output = true;
				}
			}
			if (output == false){
				System.out.print(".");
			}
			if ((i + 1) % 32 == 0){
				System.out.println();
			}
		}
		for(int i = 0; i < 32; i++){
			System.out.print("=");
		}
		System.out.println();
	}
	
	public void addFirstAvailable(MemoryLocation p){
		boolean success = false;
		ArrayList<ArrayList<Integer>> available = get_available();
		int start, end;
		for(int i = 0; i < available.size() && success == false; i++){
			start = available.get(i).get(0);
			end = available.get(i).get(1);
			int gap = end - start + 1;
			if(gap >= p.get_memory()){
				success = true;
				ArrayList<Integer> put = new ArrayList<Integer>();
				put.add(start);
				put.add(start + p.get_memory() - 1);
				occupied.put(p, put);
			}
		}
		if(success == true){
			memProcessList.add(p);
		} else {
			need_defrag = true;
		}
	}
	
	public void addSecondAvailable(MemoryLocation p){//Assume memory holds p
		int count = 0;
		boolean success = false;
		ArrayList<ArrayList<Integer>> available = get_available();
		int start, end;
		for(int i = 0; i < available.size() && success == false; i++){
			start = available.get(i).get(0);
			end = available.get(i).get(1);
			int gap = end - start + 1;
			if(gap >= p.get_memory()){
				if(count == 1){
					success = true;
					ArrayList<Integer> put = new ArrayList<Integer>();
					put.add(start);
					put.add(start + p.get_memory() - 1);
					occupied.put(p, put);
				} else {
					count++;
				}
			}
		}
		if(success == true){
			memProcessList.add(p);
		} else {
			if(available.size() == 1){
				addFirstAvailable(p);
			} else{
				need_defrag = true;
			}
		}
	}
	
	public void addBestAvailable(MemoryLocation p){
		int min = Integer.MAX_VALUE;
		ArrayList<ArrayList<Integer>> available = get_available();
		int start, end;
		int toInsert = -1;
		Collections.sort(available, new Comparator<ArrayList<Integer>>(){
			public int compare(ArrayList<Integer> a1, ArrayList<Integer> a2){
				int size1 = a1.get(1) - a1.get(0);
				int size2 = a2.get(1) - a2.get(0);
				return size1 - size2;
			}
		});
		for(int i = 0; i < available.size(); i++){
			start = available.get(i).get(0);
			end = available.get(i).get(1);
			int gap = end - start + 1;
			if(gap >= p.get_memory() && gap < min){
				min = gap;
				toInsert = i;
			}
		}
		if(toInsert != -1){
			start = available.get(toInsert).get(0);
			ArrayList<Integer> put = new ArrayList<Integer>();
			put.add(start);
			put.add(start + p.get_memory() - 1);
			occupied.put(p, put);
			memProcessList.add(p);
		} else {
			need_defrag = true;
		}
	}
	
	public void defragmentation(){
		int sum = 0;
		Memory mock = new Memory();
		Collections.sort(this.memProcessList, new Comparator<MemoryLocation>(){
			public int compare(MemoryLocation p1, MemoryLocation p2){
				int start1 = occupied.get(p1).get(0);
				int start2 = occupied.get(p2).get(0);
				return start1 - start2;
			}
		});
		for(MemoryLocation x : this.memProcessList){
			mock.addFirstAvailable(x);
			int thisstart = this.occupied.get(x).get(0);//The start point of this process in this memory
			int newstart = mock.occupied.get(x).get(0);//The start point of this process in a new memory(this memory after defragmentation)
			sum += thisstart - newstart;
		}
		this.deleteAllMemory();
		for(MemoryLocation x : mock.memProcessList){
			this.addFirstAvailable(x);
		}
		this.units_moved = sum;
		need_defrag = false;
	}
	
	public void deleteMemory(MemoryLocation p){
		boolean merge = false;
		ArrayList<ArrayList<Integer>> available = get_available();
		int occupied_start = occupied.get(p).get(0);
		int occupied_end = occupied.get(p).get(1);
		for(ArrayList<Integer> freeslot: available){
			int free_from = freeslot.get(0);
			int free_to = freeslot.get(1);
			if(free_to + 1 == occupied_start){
				merge = true;
				freeslot.remove(1);
				freeslot.add(occupied_end);
				break;
			} else if (free_from -1 == occupied_end){
				merge = true;
				freeslot.remove(0);
				freeslot.add(0, occupied_start);
				break;
			}
		}
		if(merge == false){
			ArrayList<Integer> slot = new ArrayList<Integer>();
			slot.add(occupied_start);
			slot.add(occupied_end);
			available.add(slot);
		}
		occupied.remove(p);
		memProcessList.remove(p);
	}
	
	public void deleteAllMemory(){
		occupied.clear();
		memProcessList.clear();
		need_defrag = false;
		units_moved = 0;
	}
	
	public boolean get_need_defragmentation(){
		return need_defrag;
	}
	
	public int get_defragmention_moved_units(){
		return units_moved;
	}

	public HashMap<MemoryLocation, ArrayList<Integer>> get_occupied(){
		return this.occupied;
	}
}
