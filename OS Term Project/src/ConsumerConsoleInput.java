import java.util.List;
import java.util.concurrent.Semaphore;

public class ConsumerConsoleInput extends Thread {
	private static final int MAX_STORAGE = 5;
	private volatile List<ProcessImage> blockedQueue;
	private volatile List<ProcessImage> readyQueue;
	private volatile List<Integer> consoleInputQueue;
	private volatile Semaphore mutex;
	private volatile Semaphore mutex2;
	private  boolean inserted;
	private volatile boolean isRunning;
	private Object consolesynch;
	
	
	public ConsumerConsoleInput(Semaphore mtx, Semaphore mutex2, Object consolesynch, List<ProcessImage> blockedQ, List<ProcessImage> readyQ,List<Integer> consoleInputQ) {
		this.mutex = mtx;
		this.mutex2 = mutex2;
		this.blockedQueue = blockedQ;
		this.readyQueue = readyQ;
		this.consoleInputQueue=consoleInputQ;
		this.consolesynch = consolesynch;
		
	}


	@Override
	public void run(){

		isRunning = true;
		int size;
		
		
		try {
			while (isRunning) {
				mutex2.acquire();
				size = consoleInputQueue.size();
				mutex2.release();
				
				if(size == 0)
				{
					synchronized (consolesynch) {
					consolesynch.wait();
					}
				}
				
				mutex2.acquire();
				int input = consoleInputQueue.get(0);
				consoleInputQueue.remove(0);
				mutex2.release();
				
				inserted =false;
				
				while(inserted == false)
				{
					mutex.acquire();
					boolean isBlockedQueueEmpty = blockedQueue.isEmpty();
					if (!isBlockedQueueEmpty) 
					{
						
						ProcessImage p = blockedQueue.get(0);
						blockedQueue.remove(0);
						p.V = input;
						readyQueue.add(p);
						mutex.release();
						
						inserted = true;
					}
					else
					{
						mutex.release();
						sleep(2000);
					}
				}
				size--;
				
				if(size == MAX_STORAGE - 1)
				{
					synchronized (consolesynch) {
					consolesynch.notify();	
					}
				}
			}
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		}
	

	public void stopThread() {
		isRunning = false;
	}
//	private void goToSleep() 
//	{
//		try {
//			wait();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//
//	}
}
