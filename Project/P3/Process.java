public class Process {
		private int procNum;
		private int burstTime;
		private int originBurstTime;
		private int numBurst;
		private int ioTime;
		private Boolean isWaitingForIoComplete;
		private int ioCompleteTime;
		private int priority;
		private Boolean bePreempted;
		private int arrivalTime;
		private int memory;
		private Boolean waiting;
		private int order;
		
		public void setWaiting(Boolean waiting){
			this.waiting = waiting;		
		}
		
		public void setOrder(int order){
			this.order = order;
		}
		
		public void setMemory(int memory){
			this.memory = memory;
		}
		
		public void setArrivalTime(int arrivalTime){
			this.arrivalTime = arrivalTime;
		}
	
		public void setPriority(int priority){
			this.priority = priority;
		}
		
		public void setOriginBurstTime(int originBurstTime){
			this.originBurstTime = originBurstTime;
		}
		
		public void setBePreempted(Boolean bePreempted){
			this.bePreempted = bePreempted;
		}
		
		public void setProcNum(int procNum){
			this.procNum = procNum;
		}
		
		public void setBurstTime(int burstTime){
			this.burstTime = burstTime;
		}
		
		public void setNumBurst(int numBurst){
			this.numBurst = numBurst;
		}

		public void setIoTime(int ioTime){
			this.ioTime = ioTime;
		}
		
		public void setIsWaitingForIoComplete(Boolean isWaitingForIoComplete){
			this.isWaitingForIoComplete = isWaitingForIoComplete;
		}
		
		public void setIoCompleteTime(int ioCompleteTime){
			this.ioCompleteTime = ioCompleteTime;
		}
		
		public Boolean getBePreempted(){
			return this.bePreempted;
		}
		
		public Boolean getWaiting(){
			return waiting;
		}
		
		public int getOrder(){
			return order;
		}
		
		public int getOringinBurstTime(){
			return this.originBurstTime;
		}
		
		public int getPriority(){
			return this.priority;
		}
		
		public int getProNum(){
			return this.procNum;
		}
		
		public int getMemory(){
			return this.memory;
		}
		
		public int getArrivalTime(){
			return this.arrivalTime;
		}
		
		public int getBurstTime(){
			return this.burstTime;
		}
		
		public int getNumBurst(){
			return this.numBurst;
		}
		
		public int getIoTime(){
			return this.ioTime;
		}
		
		public Boolean getIsWaitingForIoComplete(){
			return this.isWaitingForIoComplete;
		}

		public int getIoCompleteTime(){
			return this.ioCompleteTime;
		}
		public static char printer(int x){
			return (char) (x+64);
		}
		public static String queuePrinter(String x){
			StringBuilder q = new StringBuilder();
			for(int i = 0; i< x.length(); i++){
				if(Character.isDigit(x.charAt(i))){
					q.append((char)(x.charAt(i)+16));
				}else{
					q.append(x.charAt(i));
				}	
			}
			return q.toString();
		}
		public void printInfo (int index, int t, String queue, int preemptedProc){
			
			switch (index) {
			case 1: 
				System.out.println("time " + t +"ms: Process '" + printer(procNum) + "' started using the CPU "+ queuePrinter(queue));
				break;
			case 2: 
				System.out.println("time " + t +"ms: Process '" + printer(procNum) + "' completed its CPU burst "+ queuePrinter(queue));
				break;
			case 3: 
				System.out.println("time " + t +"ms: Process '" + printer(procNum) + "' performing I/O "+ queuePrinter(queue));
				break;
			case 4: 
				System.out.println("time " + t +"ms: Process '" + printer(procNum) + "' completed I/O "+ queuePrinter(queue));
				break;
			case 5: 
				System.out.println("time " + t +"ms: Process '" + printer(procNum) + "' terminated "+ queuePrinter(queue));
				break;
			case 6:
				System.out.println("time " + t +"ms: Process '" + printer(procNum) + "' preempted by Process '"+ printer(preemptedProc) + "' " + queuePrinter(queue));
			case 7:
				System.out.println("time " + t +"ms: Process '" + printer(procNum) + "' added to system "+ queuePrinter(queue));
				break;
			}
		}
}