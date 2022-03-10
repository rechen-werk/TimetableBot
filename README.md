# TimetableBot
Bot that has your JKU timetable and notifies you about upcoming courses.

## Commands
The JKU-Bot currently supports 8 commands.

### /kusss
With this command you can submit a mandatory url, from where the bot downloads the users timetable weekly.
If you type */kuss url:get* the bot gives further instructions on how to obtain the link.

### /unkusss
With /unkuss the bot completely forgets about the user performing the command.

### /schedule
With this command you can get your schedule. Further parameters define in which range the schedule should be calculated.
Per default the bot returns the schedule of the current day.

Option "when" contains 6 predefined offsets: {today, tomorrow, yesterday, this week, next week, last week}

Option "date" allows you to get the schedule of a specific day using the format DD.MM.YYYY

Option "range" lets you choose between "day" and "week" which is specifically usefule in combination with date.

Option "user" allows you to get the schedule of another user if they are set to public.

### /nextcourse
This command returns the next course of the user, or if specified with "user" the user that is given to the bot. The user has to be public though.

### /setpublic
Sets your visibility to public, allowing other users to view your schedule.
This also gives you the possibility to view others schedules.

### /setprivate
Sets your visibility to private, prohibiting other users to view your schedule.
This removes your right to view others schedules.

### /notify
With this option enabled the bot notifies you about upcoming courses in the private chat.

Option "minutes" sets the amount of minutes on how much before the course the user is notified.

Option "option" allows you to opt out of notifications if you set this parameter to false.

### /update
Forces a update of your schedule.
