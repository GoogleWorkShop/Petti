# **Petti** - You'll never walk alone

The best app for dog owners who like to walk with their neighbours

## About the APP

Petti was created in order to let dog owners the opprutinty to know other dog-owners around their geographical position.

The app has two main features:
1. Map-based display of dog owners based on static locations given by the users themselves.
2. Dynamic list display of dog-owners which are near the user - updated in real time.

Additionally, the app has more useful features for dog owners: Food and vaccinations reminders; map display of near parks, pet stores and vets.

## Architectural overview
### Server Side:
- Project database deployed on firebase.
- Firebase contains: User info and last location, privacy setting, timestamp to indicate when last time he wanted to walk, chat framework.
- Geofire: Calculate neighbors based on location info from server.
### Client side:
- Based on android with personal settings saved as persistent.
- Show other people who want to walk now on map as markers (Google maps API), Or as items with info on a grid.
- Real time chat.
- Show interesting markers (vets, shop and parks) on map (Google maps API).
- Create “food running low” phone notifications.
- Save future treatments as calendar events (Google calendar API).

## Main APIs
As mentioned above, Petti app uses "Firebase" as its BaaS (beckend as a sericve),
 hence it uses its API for manipulating the DB.
Having said that, We have encapsulated these firebase APIs and created our custom ones to serve our use-cases better,
 eventually creating 3 main API modules for the differnet screens / activities:
"API.java", "ChatApi.java" and "LocationsApi.java" 
### API.java main methods:
-initDataBaseApi - sets the the main DB references - the useres one, the dog photos one, and the owner photos one.
- getCurrOwnerData()- gets the current (one which runs the app) data of the owner
- getCurrDogData() - gets the current (one which runs the app) data of the dog
-setDog(dog), setOwner(owner) - set current DB reference of owner/dog
- createUser(name, mail) - create a new user (owner + dog) in the DB, with name <name> and mail <mail>.
- getUserRef(uid) - gets the DB ref of the user whose id is <uid>
- getCurrUserRef() - gets the DB ref of the current user
- isMatchedWith(String uid) - checks if the current user and the user whose id is uid are matched (can see each other in the nearby user section)
- blockUser(uid) - prevents from the user whose id is <uid> to send messages to the current user
- unblockUser(uid) - removes the block from the user whose id is <uid> to send messages to the current user
- isBlockedByMe(uid) - checks if the user whose id is <uid> is blocked by the current user.
- isBlockingMe(uid) - checks if the user  whose id is <uid> is blocking the current user.
### LocationsAPI.java main methods:
-initLocationApi() - sets the the locations DB references - the dynamic locations (for the walk now feature) and static locatios ref (for the neighbouring dogs feature).
- addStaticLocation/addLoction() - adds a static location to the DB / adds a dynamic location to the DB
- attachNearByUserListener() - attaches geoFire lisetner to the current user, so data about near by users can be recognized and put in the DB.
- detachNearByUserListener() - dettaches the geoFire listener.
### ChatApi.java main methods:
- initchatDB() - sets the the chat DB references - the "messages" one.
- sendChatMessage(toUid, text) - sends the text message to the user whose id is toUid. 
- getMagRefById(otherUserId) - gets all the messages between the current user and the user whose id is otherUserId.

## Project structure
The source code - apart from the resources which has its own directory - is devided into two main sub-directories:
1. the "petti" sub-directory which contains all of the app's activities and fragments. These sources essentially represents all of the user-interface code.
2. the "db" sub-directory which contatins both firebase API modles (named "API.java", "ChatApi.java" and "LocationsApi.java") 
   and other database class representations ("owner.java", "Dog.java", and "ChatMessage.java").

## Website (including APK download link)

http://emaohi.wixsite.com/petti

## Contributers

Nir Bar-Joseph, Amir Harari, Raz Mayshar, Yahav Ben-Daviv, Roee Segev


