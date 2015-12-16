import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;

/**
 * A server program which accepts requests from clients to
 * capitalize strings.  When clients connect, a new thread is
 * started to handle an interactive dialog in which the client
 * sends in a string and the server thread sends back the
 * capitalized version of the string.
 *
 * The program is runs in an infinite loop, so shutdown in platform
 * dependent.  If you ran it from a console window with the "java"
 * interpreter, Ctrl+C generally will shut it down.
 */
public class Server {

    /**
     * Application method to run the server runs in an infinite loop
     * listening on port 9898.  When a connection is requested, it
     * spawns a new thread to do the servicing and immediately returns
     * to listening.  The server keeps a unique client number for each
     * client that connects just to show interesting logging
     * messages.  It is certainly not necessary to do this.
     */
	public static boolean start = true;
	public static HashMap<String, String> files = new HashMap<String, String>();
	public static Memory m = new Memory();
	static final int n_blocks = 128;
	static final int blocksize = 4096;
	static final int port = 8765;
	static char ident = 'A';
    public static void main(String[] args) throws Exception {
        //check and create .storage
  		File file1 = new File(".storage");  
  		if  (!file1 .exists()  && !file1 .isDirectory())    
  		{     
  		    System.out.println("not exist, create new");
  		    file1 .mkdir();  
  		} else 
  		{
  		    System.out.println("exist, delete then create");
  		    deleteFile(file1); 
  		    file1 .mkdir();  
  		}
  		
        System.out.printf("Block size is %d\n", blocksize);
        System.out.printf("Number of blocks is %d\n", n_blocks);
        System.out.printf("Listening on port %d\n", port);
        int clientNumber = 0;
        Capitalizer server;
        ServerSocket listener = new ServerSocket(port);
        try {
            while (true) {
                server = new Capitalizer(listener.accept(), clientNumber++);
                server.start();
            }
        } finally {
            listener.close();
        }
    }
    public static void store (String fileName, int bytes, String fileContents) throws IOException{ 
		//System.out.println("STORE SUCCESS!");
		FileWriter writer=new FileWriter(".storage/"+ fileName);
		writer.write(fileContents);
		writer.close();
	}
    
	public static String read (String fileName, int byteOffset, int length) throws IOException{ 
		StringBuilder res = new StringBuilder();
		if(!files.containsKey(fileName)){
			res.append("ERROR: NO SUCH FILE");
		} else {
			File file = new File(".storage/" + fileName);
			if(file.isFile() && file.exists()){
				long file_len = file.length();
				if(byteOffset + length > file_len){
					res.append("ERROR: INVALID BYTE RANGE");
				} else {
					res.append("Sent: ACK ");
					res.append(length);
					res.append(System.getProperty("line.separator"));//Above is the output in server console
					FileInputStream fin = new FileInputStream(file);
					byte fileContent[] = new byte[(int)file_len];
					fin.read(fileContent);
					String strFileContent = new String(fileContent).substring(byteOffset, byteOffset + length);
					res.append(strFileContent);
					res.append(System.getProperty("line.separator"));//Above is the output in client
					res.append("Sent ");
					res.append(length);
					res.append(" bytes (from ");
					int num = (int) Math.ceil((double) (byteOffset + length) / (double) blocksize);
					res.append(num);
					res.append(" '");
					String name = files.get(fileName);
					res.append(name);
					res.append("' ");
					res.append(" blocks) from offset ");
					res.append(byteOffset);
				}
			} else {
				res.append("ERROR: NO SUCH FILE");
			}
		}
		return res.toString();
	}
	
	public static String delete (String fileName){ 
		String result;
		File file = new File(".storage/"+fileName);  
		if(!file.exists()){
			result = "ERROR: NO SUCH FILE\n";
		}else{
			deleteFile(file);
			result = null;
		}
		return result;
	}
	
	public static String[] dir (HashMap<String, String> files){ 
		int size = files.size();
		String[] fileNames = new String[size];
		int i = 0;
		for(String x : files.keySet()){
			fileNames[i] = x;
			i++;
		}
		Arrays.sort(fileNames);
		return fileNames;
		//System.out.println("DIR SUCCESS!");
	}
	
	public final static boolean isNumeric(String s) {
		if (s != null && !"".equals(s.trim()))
			return s.matches("^[0-9]*$");
		else
			return false;
	}
	
    private static void deleteFile(File file){ 
		   if(file.exists()){ 
		    if(file.isFile()){ 
		     file.delete(); 
		    }else if(file.isDirectory()){ 
		     File files[] = file.listFiles(); 
		     for(int i=0;i<files.length;i++){ 
		      deleteFile(files[i]); 
		     } 
		    } 
		    file.delete(); 
		   }else{ 
		    System.out.println("error"+'\n'); 
		   } 
		} 

	/**
     * A private thread to handle capitalization requests on a particular
     * socket.  The client terminates the dialogue by sending a single line
     * containing only a period.
     */
    private static class Capitalizer extends Thread {
        private Socket socket;
        private int clientNumber;

        public Capitalizer(Socket socket, int clientNumber) {
            this.socket = socket;
            this.clientNumber = clientNumber;
            if(start == true){
            	log("Received incoming connection from " + socket.getLocalAddress().toString().substring(1));
            	start = false;
            }
        }

        /**
         * Services this thread's client by first sending the
         * client a welcome message then repeatedly reading strings
         * and sending back the capitalized version of the string.
         */
        public void run() {
            try {

                // Decorate the streams so we can send characters
                // and not just bytes.  Ensure output is flushed
                // after every newline.
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                // Send a welcome message to the client.
                System.out.println("Hello, you are client #" + clientNumber + ".");

                // Get messages from the client, line by line; return them
                // capitalized

                for(;;){
        			System.out.println("command:");
        			String line = in.readLine();
        			if (line == null) continue;
        			
        			String[] input; 
        			input  = line.split(" ");
        			//for(int i=0; i<input.length;i++){
        			//	System.out.println(input[i]);
        			//}
        			
        			if (input[0].equals("STORE")){
        				
        				String[] storeInput;
        				if((input.length==3)){
        					String fileName = input[1];
        					int bytes=0;
        						if(isNumeric(input[2])){
        							bytes = Integer.parseInt(input[2]);
        						}else{
        							System.out.println("Wrong Size Input!");
        							continue;
        						}
        					String fileContent =in.readLine();
        					//method store
        					long processNum =Thread.currentThread().getId();//change to real one
        					System.out.println("[thread " + processNum + "] Rcvd: STORE " + fileName + bytes);
        					if(files.containsKey(fileName)){
        						System.out.println("[thread " + processNum + "] ERROR: FILE EXISTS\n");
        					}else{				
        						store(fileName, bytes, fileContent);
        						String characterToString = Character.toString(ident);
        						//record in file map
        						files.put(fileName, characterToString);
        						ident++;
        						int n_bytes = fileName.length();
        						int n_block = (int) Math.ceil((double) (n_bytes) / (double) blocksize);
        						int n_cluster = (int) Math.ceil((double) (n_block) / (double) 32);
        						System.out.println("[thread " + processNum + "] Stored file '" + files.get(fileName) + "' (" +n_bytes+ " bytes; " + n_block + " blocks; " + n_cluster+" cluster)");	
        						System.out.println("[thread " + processNum + "] Simulated Clustered Disk Space Allocation:");
        						Process p = new Process(files.get(fileName), n_block);
        						m.addFirstAvailable(p);
        						m.printMemory();
        						System.out.println("[thread " + processNum + "] Sent: ACK");		
        					}
        				}else{
        					System.out.println("Wrong Command!");
        					continue;
        				}
        						
        						
        			}else if(input[0].equals("READ")){
            				if(input.length==4){
            					String fileName = input[1];
            					int byteOffset = Integer.parseInt(input[2]);
            					int length = Integer.parseInt(input[3]);
            					String res = read(fileName, byteOffset, length);
            					
            					System.out.print("[thread " +Thread.currentThread().getId() + "] ");
            					System.out.printf("Rcvd: READ %s %d %d\n", fileName, byteOffset, length);
            					
            					String[] parse = res.split(System.getProperty("line.separator"));
            					System.out.print("[thread " +Thread.currentThread().getId() + "] ");
            					System.out.println(parse[0]);
            					for(int i = 1; i < parse.length - 1; i++){
            						out.write("[thread " +Thread.currentThread().getId() + "] ");
            						out.write(parse[i]);
            						out.write(System.getProperty("line.separator"));
            						out.flush();
            					}
            					System.out.println(parse[parse.length - 1]);
            				}else{
            					System.out.println("Wrong Command!");
            					continue;
            				}
            				
            				
            			}else if(input[0].equals("DELETE")){
            				
            				if(input.length==2){
            					String fileName = input[1];
            					String dlt = delete(fileName);
            					long processNum = Thread.currentThread().getId();//change to real one
            					System.out.println("[thread " + processNum + "] Rcvd: DELETE " + fileName);
            					if(dlt == null){
            						int n_bytes = fileName.length();
            						int n_block = (int) Math.ceil((double) (n_bytes) / (double) blocksize);
            						
            						System.out.println("[thread " + processNum + "] Deleted " + fileName + " file '" + files.get(fileName) + "' (deallocated "+n_block+" blocks)");
            						//m()
            						for(Process i : m.get_occupied().keySet()){
            						    if(i.get_name().equals(files.get(fileName)))
            						    m.deleteMemory(i);
            						}
            						//System.out.println(n_block);
            						files.remove(fileName);
            						System.out.println("[thread " + processNum + "] Simulated Clustered Disk Space Allocation:");
            
            						m.printMemory();
            						System.out.println("[thread " + processNum + "] Sent: ACK");	
            					}else{
            						System.out.print("[thread " + processNum + "] " + dlt);	
            					}			
            				}else{
            					System.out.println("Wrong Command!");
            					continue;
            				}
            				
            			}else if(input[0].equals("DIR")){
            				
            				if(input.length==1){
            					String[] dirResult = dir(files);
            					for(int i = 0; i < files.size(); i++){
            						out.println(dirResult[i]);
            					}
            				}else{
            					System.out.println("Wrong Command!");
            					continue;
            				}
            				
            			}else{
            				System.out.println("Unsupported Command!");
            				continue;
            			}
            		}
         
            } catch (IOException e) {
                log("Error handling client# " + clientNumber + ": " + e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    log("Couldn't close a socket, what's going on?");
                }
                log("Connection with client# " + clientNumber + " closed");
            }
        }

        /**
         * Logs a simple message.  In this case we just write the
         * message to the server applications standard output.
         */
        private void log(String message) {
            System.out.println(message);
        }
    }
    
    
    
}