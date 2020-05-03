import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;

public class OS extends Thread {

	private final int QUANTUM = 5;

	private CPU cpu;
	private volatile Memory memory;
	private volatile List<ProcessImage> readyQueue;
	private volatile List<ProcessImage> blockedQueue;
	private volatile List<ProcessImage> fileInputQueue;
	private volatile List<Integer> consoleInputQueue;
	private volatile Semaphore mutex;//for ready and blocked queue
	private volatile Semaphore mutex2;//for console input queue
	private volatile Semaphore mutex3;//for file input queue
	private volatile Semaphore mutexbitmap;
	private ProducerFileInput fileProducer;
	private ConsumerFileInput fileConsumer;
	private ProducerConsoleInput consoleProducer;
	private ConsumerConsoleInput consoleConsumer;
	public static int x=5;
	Object filesynch = new Object();
	Object consolesynch = new Object();
	private volatile Boolean[] bitmap;
	
	public OS(int size) {
		this.mutexbitmap = new Semaphore(1);
		this.memory = new Memory(size,mutexbitmap);
		bitmap = new Boolean[size];
		Arrays.fill(bitmap, false);
		this.cpu = new CPU(memory,bitmap,mutex);
		this.mutex = new Semaphore(1);
		this.mutex2 = new Semaphore(1);
		this.mutex3 = new Semaphore(1);
		
		this.readyQueue = new ArrayList<ProcessImage>();
		this.blockedQueue = new ArrayList<ProcessImage>();
		this.fileInputQueue = new ArrayList<ProcessImage>();
		this.consoleInputQueue = new ArrayList<Integer>();
		this.consoleConsumer = new ConsumerConsoleInput(mutex,mutex2,consolesynch,blockedQueue,readyQueue,consoleInputQueue);
		this.consoleProducer = new ProducerConsoleInput(mutex2,consolesynch,consoleInputQueue);
		this.fileConsumer = new ConsumerFileInput(mutex,mutex3,mutexbitmap,filesynch, fileInputQueue, readyQueue, bitmap,  memory);
		this.fileProducer = new ProducerFileInput(mutex3,filesynch,fileInputQueue);
		
		fileProducer.start();
		fileConsumer.start();
		consoleProducer.start();
		consoleConsumer.start();
		
		
	}


/*	public void loadProcess(String processFile,Assembler assembler)
	{
		try {
			System.out.println( "Creating binary file for "+ processFile+"...") ;
			int instructionSize = assembler.createBinaryFile(processFile, "assemblyInput.bin");
			char[] process = assembler.readBinaryFile(instructionSize, "assemblyInput.bin");

			System.out.println("Loading process to memory...");

			mutex.acquire();

			readyQueue.add(new ProcessImage(processFile,memory.getEmptyIndex(),instructionSize)); // readyqueue ya ekledi.

			mutex.release();

			this.memory.addInstructions(process, instructionSize, memory.getEmptyIndex(),bitmap); // memory e koydu.
			System.out.println("Process is loaded !");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}*/

	@Override
	public void run() {
		try {

				
			while (true) {
				mutex.acquire();
				boolean isBlockedQueueEmpty = blockedQueue.isEmpty();
				boolean isReadyQueueEmpty = readyQueue.isEmpty();
				mutex.release();

				while(isBlockedQueueEmpty && isReadyQueueEmpty)
				{
					sleep(2000);
					mutex.acquire();
					isBlockedQueueEmpty = blockedQueue.isEmpty();
					isReadyQueueEmpty = readyQueue.isEmpty();
					mutex.release();
				}

				if (!isReadyQueueEmpty) {
					System.out.println("Executing " + (readyQueue.get(0)).processName);
					cpu.transferFromImage(readyQueue.get(0));
					for (int i = 0; i < QUANTUM; i++) {
						if (cpu.getPC() < cpu.getLR()) {
							cpu.fetch(); 
							int returnCode = cpu.decodeExecute();

							if (returnCode == 0)  {
								System.out.println("Process " + readyQueue.get(0).processName + " made a system call for ");
								if (cpu.getV() == 0) {
									System.out.println( "Input, transfering to blocked queue and waiting for input...");
									ProcessImage p=new ProcessImage();
									this.cpu.transferToImage(p);
									
									mutex.acquire();
									readyQueue.remove(0);
									blockedQueue.add(p);
									mutex.release();
								} 
								else { //syscall for output
									System.out.print("Output Value: ");
									ProcessImage p=new ProcessImage();
									cpu.transferToImage(p);

									mutex.acquire();
									readyQueue.remove(0);
									System.out.println( p.V +"\n");
									readyQueue.add(p);
									mutex.release();
								}
								//Process blocked, need to end quantum prematurely
								break;
							}
						}
						else {
							System.out.println("Process " + readyQueue.get(0).processName +" has been finished! Removing from the queue...\n" );
							ProcessImage p = new ProcessImage();
							cpu.transferToImage(p);
							p.writeToDumpFile();
							mutex.acquire();
							memory.remove(readyQueue.get(0),bitmap);
							readyQueue.remove(0);
							mutex.release();
							break;
						}

						if (i == QUANTUM - 1) {
							//quantum finished put the process at the end of readyQ
							System.out.println ("Context Switch! Allocated quantum have been reached, switching to next process...\n");
							ProcessImage p = new ProcessImage();
							cpu.transferToImage(p);  

							mutex.acquire();
							readyQueue.remove(0);
							readyQueue.add(p);
							mutex.release();
						}
					}
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
