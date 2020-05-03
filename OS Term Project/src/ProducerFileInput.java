import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Semaphore;

public class ProducerFileInput extends Thread {
	private volatile List<ProcessImage> fileInputQueue;
	private static final int MAX_STORAGE = 5;
	Assembler assembler2 = new Assembler();
	private Semaphore mutex3;
	Object filesynch;
	private volatile boolean isRunning;
	public ProducerFileInput(Semaphore mtx, Object filesn, List<ProcessImage> fileInputQueue) {
		this.mutex3 = mtx;
		this.fileInputQueue = fileInputQueue;
		this.filesynch = filesn;
	}
	
	@Override
	public void run(){

			
			
		try {

			BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream("inputSequence.txt"), StandardCharsets.UTF_8));
			System.out.println("inputSequence.txt started to be read...");
			String line="";
			
			
			while((line = br.readLine()) != null && line.trim().isEmpty()==false) 
			{
				String elements[] = line.split(" ");
				String filename = elements[0].substring(0,elements[0].length()-4);
				
				int waitTime = Integer.parseInt(elements[1]);
				mutex3.acquire();
				int size =fileInputQueue.size();
				mutex3.release();
				if (size == MAX_STORAGE)
				{
					synchronized (filesynch) {
					filesynch.wait();
					}
				}
				System.out.println( "Creating binary file for "+ elements[0] +"...") ;
				int instructionSize = assembler2.createBinaryFile(elements[0], filename + ".bin");
//				System.out.println("Binary file " + filename + ".bin created");
				
				mutex3.acquire();
				fileInputQueue.add(new ProcessImage(filename +".bin" ,0 , instructionSize));
				mutex3.release();
				
				System.out.println(filename + ".bin added to fileInputQueue");
				
				mutex3.acquire();
				size =fileInputQueue.size();
				mutex3.release();
				
				if(size > 0)
				{
					synchronized (filesynch) {
					filesynch.notify();
					}
				}
				sleep(waitTime);
			}
			br.close();
			}
		
		catch(Exception e) {
			e.printStackTrace();
		
		}
	}
	
/*	private void goToSleep() 
	{
		try {
			wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}*/
	public void stopThread() {
		isRunning = false;
	}	
}


