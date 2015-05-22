package cs455.crawler;
//Tyler Decker
import java.util.LinkedList;

public class Worker extends Thread {
	
	private Task currentJob; //current job that is assigned to the worker thread
	private Thread t; //thread object
	private ThreadPoolManager myManager; //thread pool manager of worker
	private boolean killer;
	
	//constructor
	public Worker(ThreadPoolManager manager) {
		currentJob = null;
		t = new Thread(this);
		myManager = manager;
		killer = true;
	}
	//initialize the thread
	public void initialize() {
		t.start();
	}
	//assign a new task to the worker thread
	public void assign(Task newJob) {
		if(currentJob != null){
			synchronized(currentJob) {
				currentJob = newJob;
			}
		}else{
			currentJob = newJob;
		}
	}
	public void clear(){
		if(currentJob != null){
			synchronized(currentJob) {
				currentJob = null;
			}
		}
	}
	public String getTaskString() {
		return currentJob.getURL();
	}
	public Task getTask() {
		Task returner;
		synchronized(currentJob){
			returner = currentJob;
		}
		return returner;
	}
	//thread function
	public void run() {
		while(killer) {
			//want to prevent deadlock on worker, thread pool manager gets .2 seconds to assign a task before worker checks again
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				System.out.println("problem with worker thread");
			}
			//check if there is a job for the worker yet
			if(currentJob != null) {
				LinkedList<Task> newTasks = null;
				if(currentJob.getURL().contains(".doc") || currentJob.getURL().contains(".pdf") || currentJob.getURL().contains(".ps")){
					//ignore links on .doc and .pdf files
				}else{
					
					//worker performs work on task.
					newTasks = currentJob.execute();
					
					//go through each new task and start process of adding task to manager queue 
					synchronized(myManager){
						for(Task task : newTasks) {
							//System.out.println(this.getId() + " tries to add: "+task.getURL());
							myManager.addNewTask(this, task);
						}
					}
				}
				//preserve niceness invariant, worker waits 1 second (now 20 seconds due to complaints) before returning to pool
				try {
						Thread.sleep(1000);
				} catch (InterruptedException e) {
						System.out.println("problem with worker thread");
				}
				//return to the manager pool
				myManager.returnToPool(this);
			}
		}
	}
	public void die() {
		killer = false;
	}
	
}
