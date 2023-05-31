
# Synapflow Final Project Documentation

## Abstract
	The purpose of this project is to create a simple productivity timer and event planner app. To study via the Pomodoro Technique, studying for 25 minutes, taking a break for 5. While also creating events to keep track of and to be alerted on to make sure that you are staying on track. We believe there is a benefit for an app like this one, removing extra distractions and creating exactly what is needed to keep you motivated and on track for success. 

## Description of App 
SynapFlow is a time management app that allows you to track your study times and keep track of important events like exams, birthdays, or meetings. Features include: 

### Timer: Primarily structured around the Pomodoro Method (25 minutes of focused work, 5 minute break intervals). Users have the option to pause, resume, or completely stop a timed session.
### Analytics: Tracks users' study sessions progress by tracking completed number of study sessions over the last few days, and understanding your study sessions over the last month which are visually displayed through user friendly chart graphics.
### Tasks: Creating event tasks with short descriptions and date/timeframes to be alerted when you need to be working on something.
### Rewards: Users can acquire achievement badges that serve as reminders of progress and accomplishments which instill a sense of motivation to study consistently and log more sessions for greater milestones.
### Calendar: Clean user friendly calendar which serves as a hub to display all previous and newly created tasks which can be marked as complete or deleted. Dates within the calendar containing any tasks are represented by a blue dot visual so that users can identify which days contain tasks to be completed.
Discussion 


## List of Implemented Concept

1.Navigation 
The navigation bar exists inside our home activity, where it also displays the current fragment. The bar allows you to access the home timer, the analytics page, task create page, trophies, and the calendar. 

2.Fragments 
Near the entirety of our application is ran inside of fragments. The only page that is not, is the main login page. We utilized Fragments for the timer, analytics, task, trophy, and calendar pages. Both plots as well are fragments inside of the analytics fragment.

3.Firebase
Firebase was the core of our project's information. The first portion we implemented was the Google Authentication for the users to login with. This decision was so that we could easily deal with accounts and login systems and not have to worry about passwords and lost accounts. We then used the token UID generated as the users ID for the firestore database.

In Firestore we created a database, using the UID as keys for each user, we then saved some general information, like a count of how many completed study sessions the user has completed, a long with a map of dates, with a string of the time they completed a task, so that this information could be later accessed. We didn’t fully utilize the times finished, we could have instead used a map with an int counter, but this was a part of something we attempted with the analytics.  
	 
4.API 
AndroidChart API: We discovered Android chart as an alternative to AndroidPlot. The formatting, code setup, and design was significantly cleaner and we decided we wanted the format of AndroidChart for our design.

Material Progress Bar: The progress bar ‘wheel’ was something we found while researching how to do the timer, via the walkthrough I began with. The wheel for the timer counts down/gradually depletes as the time counts down and ultimately disappears on completion. Functional visual element that provides users a quick and easy way to monitor the time remaining during a study session.

Calendar API: The calendar API was a github repository we found that allowed us to integrate date information to display for the user. It also was very simple for us to add tasks to it due to it being simple enough to take in a date as a key, so that we could display the information in the recycler view.
	
 5. Notification
AddTaskFragment: After a task is created in the add task fragment we create a pending intent to the NotificationBroadcastReciever (see below) with the task details. We then used the Android Alarm Manager class to set an alarm at the date and time set in the fragment in the OS to trigger that broadcast to the NotificationBroadcastReciever.

NotificationBroadcastReciever: When this class receives a broadcast from the OS it builds a new notification with our channel and task details. Once that is done it will then send the notification to the user that they have a task to complete.

 6. Recycler view 
Recycler view was used as a way to display tasks inside the calendar. Inside the calendar at the bottom, we designed it so that it displays the name, description time and if the task is completed. That way the user can keep track of the information the had described the task with. Along with being able to complete the task and delete the task from that window as well.

 7. Material Design 
Material Design was a trickier implementation for us. We implemented our own image in the home screen of our app. We also utilized trophy images, along with icons for the navigation bar. We also changed the default color and appearance of the app, attempting to make it look more completed.

 8. Permission
Permissions were implemented to allow for both notifications and we also implemented it to access the users location.  The notifications need the permissions to be able to run properly. While implementing the extra logic for the location was to make sure we could do as much as we can with the functionality.

 9. LiveData
LiveData is used to store all of the variable information for the timer data. We utilized LiveData so that we can store the timer and state of the project without fear of losing the data while we access other portions of the app. Once you return the timer fragment, the app will reinitialize the timer and either continue it, continue to have it paused, or finish the timer and run the code related to the timer finishing. 

 10. Unit Testing
Plot Unit Testing: For the plots, we created some unit tests to make sure the lines and plots were created as expected based on the map that is stored inside the function.

Task Unit Testing: For the tasks, we created some tests to make sure that each of the variables within the Task object are updated and stored correctly.

Calendar Unit Testing: For fun dayBinderIsCalledOnDayChanged(), this test checks to make sure that the dayBinder is working properly to update any changes made to a selected day. For fun programmaticScrollWorksAsExpected(), this test checks to confirm that the scrollToMonth correctly scrolls to the specified month.


## Contributions
### Sean Perry
- Created the color design for the app.
- Created the Analytics classes, fragments, and functions to draw and display the data to the app.
- Created the test cases for the Plot class.
- Implemented the Firebase authentication.
- Implemented the main Firestore app, and created the General data types.
- Supported Brad a small amount on the Calendars with the portion that reads in the Firestore data, and helped with the recycler view.
- Created the timer functionality, UI for the timer, the LiveData class that .
- Helped with minor problems, since I had a higher availability.
### Bradley
- Built the calendar fragment
- Helped support Timer development
- Researched and implemented the API for the calendar app
- Added the test cases for the Calendar code
- Built the functionality for bottom navigation menu
- Implemented permissions requests to respect users privacy and preferences
- Trevor Kvanvig
- Implemented the rewards fragment
- Built the integration into firebase to help the rewards functionality.
- Supported Firestore development
- Helped clean up on other fragments UI
- Created the logic for each of the rewards
### Jamison
- Created the AddTaskFragment implementation and UI to create a new task.
- Created the Task class that stores all of the task data.
- Created functions in the FireStoreData class for all of the different Firestore functionality including adding a task, updating a task, deleting a task and completing a task.
- Created the notifications for when a task is due.
- Created test cases for the tasks.
- Contributed in making the slides and report.


## Conclusion
	We believe our app is a great solution to those who need something that can keep them busy studying and keep track of their tasks in one place. Using the Pomodoro Method is proven to be a strong method for studying. Viewing your completed sessions per day is valuable to keep you motivated. Tasks help you stay organized, and you can plan ahead your tasks. Rewards help you stay motivated and give you that much more to work towards. Having your schedule mapped out on a calendar is a great way to visually know what is going on. 
	In conclusion this app allowed us to work as a team and implement an app to our expectations. We ran into issues and problems that we did not anticipate in the beginning of the project, and that caused us to scrap some portions entirely, like the work manager. We quickly adapted as a team to find a new solution and were able to bring our idea to life. We were able to avoid scope creep, and managed to keep every expectation under manageable deadlines. Our team dynamic was great throughout the entire project, and we learned a lot of lessons as a group that we plan on taking with us into our new careers as Software Engineers.
