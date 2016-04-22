package com.runMyErrand.controllers;

import java.awt.List;
import java.util.ArrayList;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.runMyErrand.logic.DateManager;
import com.runMyErrand.model.TaskInfo;
import com.runMyErrand.model.UserInfo;
import com.runMyErrand.services.MasterTaskServices;
import com.runMyErrand.services.TaskServices;
import com.runMyErrand.services.UserServices;
import com.runMyErrand.services.MemberServices;
/*Manages all task related mechanisms */
@Controller
@SuppressWarnings({"rawtypes", "unchecked"})
public class TaskController {
	
	private static final Logger logger = Logger.getLogger(TaskController.class);
	
	/* retrieves all unassigned tasks of a particular room so that tasks can be assigned*/
	@RequestMapping(value="/overduetasks",method=RequestMethod.GET)
	public static ModelAndView overdueTasks(HttpSession session)
	{
		logger.debug("entering overdue tasks");
		ModelAndView model=new ModelAndView("overduetasks");
		UserInfo user = (UserInfo)session.getAttribute("user");
		ArrayList list_comp=(ArrayList) TaskServices.alloverduetasks(user.getRoom());
		logger.debug("after modelview"+list_comp);
		model.addObject("list_comp",list_comp);
		return model;
	
	}
	/*//	model.addObject("roomies", roomies);
		model.addObject("list_comp", tasksc);
		return model;	/*	model.addObject("list_roomy",list_roomy);
		logger.debug("after modelview"+list_comp);
		model.addObject("list_comp",list_comp);
		return model;*/
	
	/*List[] overduename = new List[2] ;
		overduename = (List[]) TaskServices.alloverduetasks(user.getRoom());
			List usernames = overduename[0];
			List tasksc = overduename[1];
			logger.debug("completedtasks retrieved");
			
		//	model.addObject("roomies", roomies);
			model.addObject("overduename", tasksc);
			model.addObject("username", usernames);
			return model;*/

	@RequestMapping(value="/alltasks")
	public ModelAndView allTasks(HttpSession session){
		
		ModelAndView model= new ModelAndView("alltasks");
		
		//ArrayList<UserInfo>roomies = (ArrayList<UserInfo>)session.getAttribute("roomies");
		UserInfo user = (UserInfo)session.getAttribute("user");
		ArrayList Alltasks = (ArrayList) TaskServices.completetask(user.getRoom());
		logger.debug("unassignedtasks retrieved");
		model.addObject("alltasks", Alltasks);
		/*model.addObject("unassigned", unassignedtasks);*/
		return model;
	}	
	

	
	@RequestMapping(value="/unassignedtask")
	public ModelAndView unassignedTasks(HttpSession session){
		
		ModelAndView model= new ModelAndView("unassignedtasks");
		
		ArrayList<UserInfo>roomies = (ArrayList<UserInfo>)session.getAttribute("roomies");
		UserInfo user = (UserInfo)session.getAttribute("user");
		ArrayList unassignedtasks = (ArrayList) TaskServices.retrieveUnassignedTasks(user.getRoom());
		logger.debug("unassignedtasks retrieved");
		
		model.addObject("roomies", roomies);
		model.addObject("unassigned", unassignedtasks);
		return model;
	}
	
	/* updates tables when task is assigned*/
	@RequestMapping(value="/Assigntask.do" ,method = RequestMethod.POST)
	public ModelAndView assigntask(@RequestParam("taskid") int taskid, @RequestParam("assigned") String assignedto,
			HttpSession session, @RequestParam("length") int length){
		
		logger.info("task: "+taskid);
		logger.info("assignedto: "+assignedto);
		UserInfo user = (UserInfo)session.getAttribute("user");
		TaskServices.assignTask(taskid, assignedto, user.getRoom());
		if(length>1){
			MasterTaskServices.updateAssignedTaskPoints(taskid, user.getRoom());
			logger.debug("updated tasks");
		}
		return new ModelAndView("forward:unassignedtask");
	}
	
	/** 
	 *  The method adds tasks to task and master table;
	 * 	it further updates total points in roominfo which is required while calculating pending score; 
	 * 	and updates pending scores of all the users of the room.
	 * 
	 * @param task (Model Object TaskInfo)
	 * 		 ModelAttributes sets the task information given by user
	 * @param session
	 * 		 
	 * @return ModelAndView (forwards request to Dashboard Controller)			
	 **/
	
	@RequestMapping(value = "/addtask.do", method = RequestMethod.POST)
	public ModelAndView addtask(@ModelAttribute("task") TaskInfo task,HttpSession session,@RequestParam("flag") String flag) {
		logger.debug("Entering add task controller");
		logger.debug("Flag from page: "+flag);
		UserInfo user = (UserInfo)session.getAttribute("user");
		logger.debug("Task Description: "+task.getTaskDescription());
		logger.debug("Task points: "+task.getPoints());
		logger.debug("Number of days: "+task.getNumber_of_days());
		String end_date = DateManager.recurring(task.getStart_date(), task.getNumber_of_days());
		task.setEnd_date(end_date);
		int addFlag = Integer.parseInt(flag);
		if(addFlag == 0){
			int masterid = MasterTaskServices.insertMasterTask(task, user.getRoom());
			task.setMasterId(masterid);			
		}
		TaskServices.addTask(task, user.getRoom());
		logger.debug("adding points to roominfo");
		//MemberServices.addPoints(task.getPoints(), user.getRoom());
		//UserServices.pendingScoresBatchUpdate(user.getRoom());
		return new ModelAndView("forward:dashboard");

	}
	
	/* Edit task does the necessary job when the user completes the task. the database is updated and 
	 * recurrence is checked*/
	@RequestMapping(value = "/edittask", method = RequestMethod.POST)
	public ModelAndView editMyTask(
			@RequestParam("taskid") int taskid, HttpSession session, @RequestParam("completed") String completed) {

		logger.debug("Entering edittask");
		int status = -1;
		ModelAndView model = new ModelAndView("forward:dashboard");
		
		logger.debug("checking if task is todo or completed");
		if (completed.equalsIgnoreCase("done")) {
			logger.debug("taskdone");
			status = 1;
		} else {
			logger.debug("taskdone");
			status = 0;
		}
				
		UserInfo user = (UserInfo) session.getAttribute("user");
		
		logger.debug("getting score and setting Pending Score: Previous " + user.getPendingscore());
		float score = TaskServices.updateTaskStatus(taskid ,status, user.getEmail());
		user.setPendingscore(MemberServices.updatePendingScore(user.getRoom(), score));
		
		logger.debug("Updating User Score");
		UserServices.updateUserScore(user.getEmail(), score, user.getPendingscore());
		
	/*	logger.debug("Updating dates if tasks complete");
		if (status == 1) {
			TaskServices.checkRecurrence(taskid, user.getRoom());
		}
		
	*/	return model;
	}
}
