import java.util.List;
import java.util.concurrent.Semaphore;

public class ConsumerFileInput extends Thread{
	private static final int MAX_STORAGE = 5;
	private volatile List<ProcessImage> fileInputQueue;
	private volatile List<ProcessImage> readyQueue;
	Assembler assembler3 = new Assembler();
	private volatile Semaphore mutex;
	private volatile Semaphore mutex3;
	private volatile Boolean[] bitmap;
	private volatile Memory memory;
	private volatile boolean isRunning;
	private Semaphore mutexbitmap;
	Object filesynch;
	public ConsumerFileInput(Semaphore mtx, Semaphore mutex3, Semaphore mutexbm, Object filesn, List<ProcessImage> fileInputQueue, List<ProcessImage> readyQ, Boolean[] map, Memory m) {
		this.mutex = mtx;
		this.mutex3 = mutex3;
		this.mutexbitmap = mutexbm;
		this.fileInputQueue = fileInputQueue;
		this.readyQueue = readyQ;
		this.bitmap = map;
		this.memory=m;
		this.filesynch = filesn;
	}
	@Override
	public void run(){

		isRunning = true;
		int size;
		
		try {
				while(isRunning)
				{
					mutex3.acquire();
					size = fileInputQueue.size();
					
					mutex3.release();
					
					if(size==0)
					{
						synchronized (filesynch) {
						filesynch.wait();
						}
					}
					ProcessImage tmp = fileInputQueue.get(0);
					String processName = tmp.processName;
					int instructionSize = tmp.LR;
					
					System.out.println("Trying to find space for " +processName + " in memory using FirstFit Algorithm");
					
					int position = firstFit(instructionSize,bitmap);
					
					while(position==(-1))
					{
						sleep(2000);
						position = firstFit(instructionSize,bitmap);
					}
					
					System.out.println("Suitable memory location founded for " + processName );

					tmp.BR=position;
					tmp.LR= (instructionSize + position);

					
					char[] process = assembler3.readBinaryFile(instructionSize, processName);

					mutex.acquire();
					readyQueue.add(fileInputQueue.get(0)); // readyqueue ya ekledi.
					memory.addInstructions(process, instructionSize, position,bitmap);
					mutex.release();
					
					System.out.println( processName + " added to ReadyQueue" );	
					System.out.println( processName + " loaded to Memory" );
					System.out.println( processName + " removed from File Input Queue" );
					
					fileInputQueue.remove(0);
					size--;
					
	
					
					if(size == MAX_STORAGE - 1)
					{
						synchronized (filesynch) {
						filesynch.notify();	
						}
					}
					
				}
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		}
	
	private int firstFit(int processSize, Boolean[] bitmap2) 
	{
		int foundedsize=0;
		try {
			mutexbitmap.acquire();
			for (int i = 0; i < bitmap2.length; i++) 
			{
				Boolean boolean1 = bitmap2[i];
				if (boolean1 == false) 
				{
					foundedsize++;
					if(foundedsize==processSize)
					{
						mutexbitmap.release();
						return (i-processSize+1);
						
					}
				} 
				else 
				{
					foundedsize=0;
				}
				
			}
			mutexbitmap.release();
			return -1;
			
		} 
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	public void stopThread() {
		isRunning = false;
	}
/*	private void goToSleep() 
	{
		try {
			wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}*/
}
