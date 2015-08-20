Client-Server TicTacToe on GWT
------------------------------

Just another one TicTacToe on GWT. It was written as an experiment in the past.


How it works
------------

Clients perform actions via the Server and receive Messages from it. There are 
five messages:
- PlayerEnter - player entered the waiting room
- PlayerExit - player exited the waiting room (by starting a game or closing the browser tab)
- GameStart - new game was started
- GameEnd - game was ended
- Move - new move was done in the game

The Client asks the Server every second by calling getMessages(), and the Server
answers by sending messages kept for that client. This is a pure polling method 
(request-response), inefficient but simple. For practical tasks, server push or 
some other technique are much better.

The Client checks moves as well as the Server, so the player actions are shown 
on the client side immediately.


Notes about Google App Engine (GAE)
-----------------------------------

The game is not designed for GAE because this distributed server can create 
many instances of the same servlet (and even place them in different JVMs). 
It's assumed that there is only one servlet instance as written in Servlet 
Specification for non-distributed environment. So the server data is stored 
inside the servlet for simplicity. With GAE, the data could be stored e.g. 
via DataNucleus + JDO/JPA.

Nonetheless the game was tested on GAE. It worked until GAE decided to create 
a new servlet instance.


Known issues
------------

If the game is running in different tabs, html changes can be drawn with some 
delay when you switch the tabs. That's because a page can be displayed before 
the changes take effect. So it's better to test the game in different windows.

There were also two errors with Development Mode in Chrome: "Something other 
than a 'type' was returned from JSNI method" and "Unable to initiate the 
asynchronous service invocation". They did not affect the game. As far as 
I read these were known errors of the Chrome plugin.


Installation (Eclipse)
----------------------

The source files can be imported into an empty GWT project.

1. Install Eclipse for Java (and JRE if needed).
2. Install GWT SDK and GWT Plugin for Eclipse.
3. Set this SDK in plugin settings:
   Window -> Preferences -> Google -> Web Toolkit -> Add.
4. Create an empty Web Application Project:
   File -> New -> Project -> Google -> Web Application Project.
   In the package field, set "tictactoe" (the source code is stored in that package).
   Uncheck "Generate project sample code" and "Use Google App Engine".
5. Import sources into the project, overwriting existing files:
   File -> Import -> File System ("From directory" = sources folder, 
   "Into folder" = project folder).
6. Perhaps you will need to set "Super Development Mode" in project settings
   because "Classical Mode" seems to be no longer supported on many browsers:
   Project -> Settings -> Run/Debug Settings -> Edit -> GWT -> Super Development Mode.
7. Run -> Run, and then double-click the link in the Development Mode tab.
