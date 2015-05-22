package cs455.crawler;
//Tyler Decker

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Queue;

import cs455.harvester.Crawler;


//manages the worker threads for crawling webpages
public class ThreadPoolManager {
	
	private Queue<Worker> availableWorkerQueue; //Queue responsible for managing available workers for tasks
	private Queue<Task> availableTaskQueue = new LinkedList<Task>(); //Queue responsible for managing available tasks
	private TaskRegistry registry; //tracks completed tasks
	private Crawler myCrawler; //crawler parent
	private Queue<Task> handoffTaskQueue = new LinkedList<Task>();

	
	//constructor takes in initialized workers
	public ThreadPoolManager(Queue<Worker> availableWorkerQueue, Crawler myCrawler) {
		this.availableWorkerQueue = availableWorkerQueue;
		this.registry = new TaskRegistry();
		this.myCrawler = myCrawler;
	}
	//add a new task to the available task queue
	public void addNewTask(Worker worker, Task task) {
		synchronized(this){
		synchronized(availableTaskQueue){
		try {
			if(myCrawler.checkDomain(task.getURL())){
					//check if duplicate task, is already in task queue and if the new task is not the same task that spawned it
					if(!registry.isDuplicateTask(task.getURL())){
							if(!checkTaskQueue(task) && !task.getParent().getURL().equals(task.getURL())){
								if(worker != null){ 
									//writeToFile(new String("Worker " + worker.getId() + " adds: " + task));
									//System.out.println("Worker " + worker.getId() + " adds: " + task);
								}
								this.availableTaskQueue.add(task);
							}
					}
			}
			else{
				myCrawler.passOnTask(task);
			}
		}catch (MalformedURLException e) {
			System.out.println("problem checking task for root domain");
		}
	  }}
	}

	public boolean checkTaskQueue(Task task){
		synchronized(availableTaskQueue){
		for(Task check : availableTaskQueue){
			if(check.getURL().equals(task.getURL())) return true;
		}
		return false;
		}
	}
	//when initializing the thread pool manager
	public void addWorker(Worker worker) {
		synchronized(availableWorkerQueue) {
			this.availableWorkerQueue.add(worker);
		}
	}
	public void addCompletedTask(Worker worker){
		
			synchronized(registry) {
				//really trying to get rid of duplicate tasks its getting so hard...
				registry.addCompletedTask(worker.getTask());
			}
		
	}
	public void addHandOffTask(Task task){
		
		synchronized(handoffTaskQueue) {
			//really trying to get rid of duplicate tasks its getting so hard...
			handoffTaskQueue.add(task);
		}
	
	}
	public Task pullHandOffTask(){
		
		synchronized(handoffTaskQueue) {
			//really trying to get rid of duplicate tasks its getting so hard...
			return handoffTaskQueue.poll();
		}
	
	}
	//return a worker that has finished their task to the available worker queue
	public void returnToPool(Worker worker) {

			synchronized(availableWorkerQueue) {
				worker.clear();
				this.availableWorkerQueue.add(worker);
			}
	}
	//set up graph for file writing
	private void setUpGraph() {
		//go through each task's child and add the incoming edge to that task that holds the child
		for(Task task : registry) {
			for(Task childTask : task.getChildren()){
				Task maybe = registry.findTask(childTask.getURL());
				if(maybe != null){
					maybe.addToInEdges(task.getURL());
				}
			}
		}
	} 
	private void writeGraph() {
		String domain = myCrawler.getDomain();
		domain = domain.replace("http://", "");
		domain = domain.replace("/", "-");
		String directory = new String("/tmp/cs455-tylerjms/" + domain + "/");
		Path pathToBrokenLinks = Paths.get(directory + "broken-links/" + "links");
		try {
			Files.createDirectories(pathToBrokenLinks.getParent());
			Files.createFile(pathToBrokenLinks);
		} catch (IOException e1) {
			System.out.println("problem building graph");
		}
		for(Task task : registry) {
			try {
				
				String edittedURL = task.getURL().replace("http://", "");
				edittedURL = edittedURL.replace("/", "-");
				Path pathToInFile = Paths.get(directory + "nodes/" + edittedURL + "/in");
				Path pathToOutFile = Paths.get(directory + "nodes/" + edittedURL + "/out");
				
				Files.createDirectories(pathToInFile.getParent());
				
				Files.createFile(pathToInFile);
				Files.createFile(pathToOutFile);
				
				File fileIn = new File(directory + "nodes/" + edittedURL + "/in");
				File fileOut = new File(directory + "nodes/" + edittedURL + "/out");
				File fileBrokenLinks = new File(directory + "broken-links/" + "links");
				
				FileWriter fwIn = new FileWriter(fileIn.getAbsoluteFile());
				BufferedWriter bwIn = new BufferedWriter(fwIn);
				
				FileWriter fwOut = new FileWriter(fileOut.getAbsoluteFile());
				BufferedWriter bwOut = new BufferedWriter(fwOut);
				
				FileWriter fwBroke = new FileWriter(fileBrokenLinks.getAbsoluteFile());
				BufferedWriter bwBroke = new BufferedWriter(fwBroke);
				
				for(String parent : task.getInEdges()){
					bwIn.write(parent + "\n");
				}
				
				for(Task child : task.getChildren()){
					bwOut.write(child.getURL() + "\n");
				}
				
				for(String broke : task.getBrokenLinks()){
					bwBroke.write(broke + "\n");
				}
				
				bwIn.close();
				bwOut.close();
				bwBroke.close();
				
			} catch (IOException e) {
				System.out.println("problem building graph, please make sure there is no cs455-tylerjms directory in /tmp/");
			}
		}
	}
	//thread pool runner
	public void run() {
		while(true) {
			Worker availableWorker;
			synchronized(availableWorkerQueue) {
				availableWorker = availableWorkerQueue.poll();
			}
			//if we have an available worker we can now check if we have a task that needs doing
			if(availableWorker != null) {
				boolean foundTask = false; //task checker
				while(!foundTask) {
					Task availableTask;
					synchronized(availableTaskQueue) {
						availableTask = availableTaskQueue.poll();

					
						//if we have a task for the worker to perform then we set the workers task and move on to the next worker/task
						if(availableTask != null) {
							if(myCrawler.getCompleteness()) myCrawler.updateCompleteness(false);

						
							//assign the task
							//String Url = availableTask.getURL();
							synchronized(myCrawler){
							availableWorker.assign(availableTask);
							}

							//System.out.println("Worker " + availableWorker.getId() + " adds completed task: " + Url);
							addCompletedTask(availableWorker);

							//System.out.println("new task");
							//move on to next worker
							foundTask = true;
							availableWorker = null;
						}
						}
						synchronized(registry){
							if(registry.getSize() == 1){
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}
						synchronized(availableTaskQueue) {
							if(availableTaskQueue.isEmpty()){
								if(!myCrawler.getCompleteness()) myCrawler.updateCompleteness(true);
								foundTask = true; //try and break loop if we can
							}
						}
				}synchronized(availableWorkerQueue) {
					if(availableWorker != null){
						returnToPool(availableWorker);
						availableWorker = null;
					}
				}
				if(myCrawler.getCompleteness() && handoffTaskQueue.isEmpty() && myCrawler.getHandoffCompleteness() && myCrawler.getCompletenessOfOthers()){

					System.out.println("waiting 20 seconds for any lingering tasks to finish");
					try {
						Thread.sleep(20000);
					} catch (InterruptedException e) {
						System.out.println("problem with worker thread");
					}
					setUpGraph();
					//write directories
					writeGraph();
					System.out.println("finished!");
					//once that is done close everything and break the loop
					System.exit(0);
				}
			}
		}
	}


}
