package com.runMyErrand.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.runMyErrand.dao.TaskDao;
import com.runMyErrand.logic.DateManager;
import com.runMyErrand.model.TaskInfo;
import com.runMyErrand.model.UserInfo;

@SuppressWarnings({"rawtypes", "static-access"})

public class TaskServices {

private static final Logger logger = Logger.getLogger(TaskServices.class);
private static TaskDao taskdao;
	
	public static TaskDao getTaskDao(){
		return taskdao;
	}
	
	@Autowired
	public static void setTaskDao(TaskDao taskd){
		taskdao = taskd;
	}
	public static List alloverduetasks(String room) {
		logger.debug("Entering alloverdue tasks");
		getTaskDao();
		return TaskDao.alldueTasks(room);
	}
	/*public static List alloverduetasks(String room) {
		logger.debug("Entering alloverdue tasks");
		List<TaskInfo> allduename = getTaskDao().alldueTasks(room);
		return allduename;
		
 		/*getTaskDao();
		return TaskDao.alldueTasks(room);
	}*/	
	//service to retrieve all tasks
	public static List retriveAllTasks(String room) {
		logger.debug("Entering retrieveTasks");
		return getTaskDao().selectAll(room);
	}
	/*public static List completetask(String room) {
		logger.debug("Entering retrieveTasks");
		return getTaskDao().selectAlltasks(room);
	}*/
	
	
	//service to retrieve users tasks
	public static List<TaskInfo> retrieveMyTasks(String email)
	{
		logger.debug("retrieveMyTasks");
		getTaskDao();
		return TaskDao.selectAssigned(email);
	}
	
	//service to retrieve unassigned tasks 
	public static List retrieveUnassignedTasks(String room)
	{
		return getTaskDao().selectUnAssigned(room);
	}
	public static List completetask(String room)
	{
		return getTaskDao().selectAll(room);
	}
	
	//service to assin a task to user and update necessary tables
	public static void assignTask(int taskid, String name, String room)
	{
		logger.debug("Entering Assign task");
		getTaskDao().updateTaskAssignedto(taskid, name, room);
	}

	//service to add task and insert the the new task
	public static void addTask(TaskInfo task, String room) {
		getTaskDao().insertTask(task, room); 
		MemberServices.addPoints(task.getPoints(), room);
		UserServices.pendingScoresBatchUpdate(room);
	}

	//service to update task status as to completed or not and thus update the score
	public static float updateTaskStatus(int taskid, int completed, String email) {
		logger.debug("Entering updatetask");
        getTaskDao().updateTaskStatus(taskid, completed);
        
        logger.debug("entering updatescore");
        return TaskServices.changeUserScore(email);
	}
	
	public static float changeUserScore(String email){
		return getTaskDao().updateScore(email);
	}
	
	public static TaskInfo getSpecificTask(int taskid){
		return getTaskDao().getTask(taskid);
	}
	
	//service to check recurrence and do the necessary updates
	public static void checkRecurrence(String date){
		logger.debug("---Checking for RecurringTasks Due on "+ SchedulingService.getCurrentSystemDate()+"---");
		Date d = DateManager.convertStringDate(date);
		List<TaskInfo> tasks = getTaskDao().selectAll(d);
		logger.debug("Potential Recurring Tasks" + tasks);
		String type = null;
		
		for(int i=0; i<tasks.size(); i++){
			
			TaskInfo task = tasks.get(i);
			String room = task.getRoom();
			logger.debug("checking if recurrence is weekly, monthly or null");
			if (task.getRecurrence().equalsIgnoreCase("weekly")){
				logger.debug("weekly");
				type = "weekly";
			}
			else if(task.getRecurrence().equalsIgnoreCase("monthly")){
				logger.debug("monthly");
				type = "monthly";
			}
		
			if(type != null){
				logger.debug("recurring taskfound");
				if(type.equalsIgnoreCase("weekly"))
					date = DateManager.recurring(task.getEnd_date(), 8);
				else{
					date = DateManager.recurring(task.getEnd_date(), 31);
				}
			logger.debug("setting updated dates");
			task.setStart_date(DateManager.recurring(task.getEnd_date(),1));
			task.setEnd_date(date);
			task.setCompleted(0);
			task.setUseremail(null);
			task.setPoints(MasterTaskServices.getUpdatedPoints(task));
			TaskServices.addTask(task, room);
			//getTaskDao().insertTask(task, room);
			logger.debug("task inserted");
			getTaskDao().disableRecurrence(task.getTaskid());
			}
		}
		
		
		
	}
	public static List<String> getRooms(){
		return getTaskDao().getRooms();
	}
	
	public static float getTimeboxPoints(String room){
		return getTaskDao().getTimeboxPoints(room);
	}
	
	public static void updateAssignedPoints(int mastertaskid, float points){
		getTaskDao().updatePoints(mastertaskid, points);
	}

}
