import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class ProducerConsoleInput extends Thread {
	private volatile List<Integer> consoleInputQueue;
	private static final int MAX_STORAGE = 5;
	private volatile Semaphore mutex2;
	private volatile boolean isRunning;
	private Object consolesynch;
	public ProducerConsoleInput(Semaphore mtx, Object consolesn, List<Integer> consoleInputQueue) {
		this.mutex2 = mtx;
		this.consoleInputQueue = consoleInputQueue;
		this.consolesynch = consolesn;
	}
	
	@Override
	public void run(){

			isRunning = true;
			int size;
			Scanner in = new Scanner(System.in); 

			{
		try {
			while(isRunning)
			{
				
				mutex2.acquire();
				size = consoleInputQueue.size();
				mutex2.release();
				if (size== MAX_STORAGE)
				{
					synchronized (consolesynch) {
					consolesynch.wait();
					}
				}
				
				
				int i = in.nextInt();
				
				mutex2.acquire();
				consoleInputQueue.add(i);
				mutex2.release();
				size++;
				if(size > 0)
				{
					synchronized (consolesynch) {
					consolesynch.notify();
					}
				}				
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
			}
			
			in.close();
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


