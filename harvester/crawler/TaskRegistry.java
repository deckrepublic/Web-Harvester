package cs455.crawler;
//Tyler Decker
import java.util.Iterator;
import java.util.LinkedList;

//responsible 	 storing 
public class TaskRegistry implements Iterable<Task>{
	
	private final LinkedList<Task> completedTasks = new LinkedList<Task>();
	
	public TaskRegistry(){}
	
	public int getSize(){
		return completedTasks.size();
	}
	//add a completed url that is crawled to the registry
	public void addCompletedTask(Task url){
		synchronized(completedTasks){
			completedTasks.add(url);
		}
	}
	//find task used for graph creation
	public Task findTask(String url){
		synchronized(completedTasks) {
			for (Task check : completedTasks) {
				if (check.getURL().equals(url)) return check;
			}
		return null;
		}
	}
	//check if task is completed or a duplicate before adding the task to available task queue in thread pool manager
	public boolean isDuplicateTask(String url){
		//go through each string and check
		synchronized(completedTasks) {
			for (Task check : completedTasks) {
				if (check.getURL().equals(url)) return true;
			}
			return false;
		}
	}
	public Iterator<Task> iterator() {
		return completedTasks.iterator();
	}
	
}
