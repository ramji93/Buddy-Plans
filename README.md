Buddy Plans is a free messenger app to connect with your friends even in the slowest network connections. It is designed to send and receive messages in all types of connections (4G,3G,2G,Wifi). 

PlayStore link : https://play.google.com/store/apps/details?id=com.chatapp.ramji.buddyplans

Developer notes :

This app works extensively on the features provided by firebase. Following are the firebase features I have made most out of :

Authentication : Used for logging into the app using already existing facebook or Google credentials without creating new ones.

Database : For mainiaing the app's cloud database. This is a no SQL database but needs to be structured according to the app's requirements. My app will reflect the changes in the cloud database using initialised listeners.

Cloud Storage : Database is only for storing text messages and location coordinates. I need cloud storage for storing picture messages and profile pictures. After saving a file in cloud storage, the path of the saved file will be refered in database.

Cloud functions : Probably the most intelligent feature in firebase. I need to implement notifications for notifying the users that new message is received. But I dont use a server as i rely on firebase. Firebase Cloud Functions comes to my rescue. You can write a function in node.js which will be running 24 x 7 on a remote firebase server. These functions can utilize other firebase features using some apis. So I have written a function which will fire up when there is a change in database and notify the concerned users using notifications.


Following are the features of my app BUDDY PLANS :

AUTHENTICATION: Anyone can use Buddy Plans app with either Google or Facebook account as the app uses their authentication. Users are requested to create an account in order to use the app.

1:1 SHARING: User can search for their buddies in the app and include them as friends. On becoming friends they can start one to one conversation. This private chat can only be with friends. User will get notifications when not using the app. If a green circle appears just near friend's image in the title bar, then the friend is online. Else the friend is offline.

GROUP SHARING: User can form a group with his/her buddies and share messages in the group for everyone to enjoy group fun. Only friends can be included inside a group. Inside group chat user can switch to private chat by clicking on another user. User will get notifications when not using the app. 

MULTIMEDIA: User can send photos and locations apart from text messages with their buddies. 

CLOUD MESSAGING: User when not connected to internet will still receive messages but just doesn't show up in the app. All messages will be stored in the cloud while app will be updated with new messages once internet connection resumes.

SEND REMINDERS: User can send reminders in both private chat and group chat. Title and time of reminder is required to send a reminder. This feature is available only for favorite chats. The intended friend will receive a reminder notification with 'Save Reminder' option to save it in the calendar. In case of group chat the reminder will be sent to every member of the group.

VIEW PROFILES: User can view his/her profile and make changes to profile picture. Profile consists of user image, email id and Facebook page link if the user logged in using a Facebook account. User can also view his/her friend's profile through private chat.
