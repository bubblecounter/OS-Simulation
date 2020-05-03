import java.util.concurrent.Semaphore;

public class Memory {

	private int memorySize;
	private char[] memory;
	private volatile Semaphore mutexbitmap;


	public Memory(int size, Semaphore mutexbm) {
		memorySize = size;
		memory = new char[size];
		this.mutexbitmap = mutexbm;

	}

	void addInstructions(char[] buffer, int bufferSize, int BR,Boolean[] bitmap)
	{
		for (int i = BR; i < bufferSize+BR; i++)
		{
			this.memory[i] = buffer[i - BR];
			bitmap[i]=true;
		}
	}


	char[]getInstruction(int PC, int BR)
	{
		char[]instruction = new char[4];
		instruction[0]=memory[PC+BR];
		instruction[1]=memory[PC+BR+1];
		instruction[2]=memory[PC+BR+2];
		instruction[3]=memory[PC+BR+3];

		return instruction;

	}

	public int getMemorySize() {
		return memorySize;
	}

	public void remove(ProcessImage processImage, Boolean[] bitmap) 
	{
		try {
			mutexbitmap.acquire();
			for (int i = processImage.BR; i < processImage.LR; i++) 
			{
				bitmap[i]= false;
			}		
			mutexbitmap.release();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
