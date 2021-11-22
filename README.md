# Assignment2 - Golf Ireland Android application.

Name: David Corrigan

## Overview.

This is an android application which is used to log the golf courses of Ireland. It is a crowd soured model where registered users can add details of courses.
It has the following functionality currently. (but more to come in the future)

+ A new Register and login pages for the user, plus a signout option
+ A list of the existing course added to the system are displayed on login with the following information
  + Course Name
  + Course Description
  + Course Province
  + Course Par
  + Course Image
  + Course Location 
+ There is the functionality to update all these by a user.
+ There is the functionality to add new courses with these details
+ An Map overview page displays a map with the location of all the courses display. These can be clicked on to see name and description.
+ A My Courses menu option allows a user to see only course which they have added.
+ There is a search facility to search courses by title, description, province and par, it match on any partial match.
+ A Nav Bar display some options as well as the logged in users details.



## Setup requirements.

Once the repo has been cloned from https://github.com/Davidcorrigan1/GolfPOIAndroidApp.git
then a file called google_maps_api.xml will need to be added to the values folder with your google API key in the format below.

A zip of the latest release can be had here: https://github.com/Davidcorrigan1/GolfPOIAndroidApp/archive/refs/tags/v1.0.2.zip

<resources>
    <string name="google_maps_key" templateMergeStrategy="preserve" translatable="false"><-Key here -></string>
</resources>
