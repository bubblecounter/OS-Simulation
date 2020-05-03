# OS-Simulation
A program which will be able to execute several processes in a way that is similar to how computer systems works using methods and algorithms learned through Operating Systems course

## Project Description
  In this project, i created a program which is able to simulate the execution of multiple processes. Processes written by the given assembly language and program executes them. Program executes several processes in a way that is similar to how computer systems work. For this purpose, i used the methods and algorithms i have learned throughout the Operating System Course. 
## Challenges
  In this project challenges are mainly as follows; *loading multiple processes*, *limited memory*, *assigning I/O operations to blocked processes* and additional queues to deal with *multiple processes and input operations*.
  
## Detailed Description
  There are multiple processes but in the simulation, there is only *one CPU core*. This means that at a given time, only one process will be able to run while the other processes will be waiting for their turns. For the processes which are waiting their turns, i used a Java list structure as a queue to store the waiting processes. This list named as *Ready Queue*. 
  A *process* can give an output or receive an input depending on the value of *V Register*. When receiving an input, the process waits for the user to give an input, if this waiting process were to be done in CPU, then the execution cycle of CPU would be blocked and other processes would not be able to run. Because of that, for the processes which are waiting for an input, i used list as a queue and this queue is called *Blocked Queue*.
  In addition to these queues, i implemented two additional queues. There is an indetermined number of processes which will be executed in the simulation, but the memory is limited. It may not be possible to load all of the processes into the memory at the same time. I used a bounded buffer as a queue to keep the information of processes which will not be able to be loaded into the memory. This bounded buffer named as *File Input Queue*. 
  It is also possible for user to give an input while no process is waiting. Program stores these inputs for processes which will be waiting for an input. To store these inputs, i used a bounded buffer as a queue and it will be named *Console Input Queue*.

Outline of the project can be shown in the figure below

![](https://github.com/bubblecounter/OS-Simulation/blob/master/OS-simulation.jpg)
 
 More detailed info can be found in [here](https://github.com/bubblecounter/OS-Simulation/blob/master/CS307%20TermProject.pdf)
 
## How it works?
  There are 8 different processes written in assembly language in the source folder. Program executes them and creates output files. 
  

