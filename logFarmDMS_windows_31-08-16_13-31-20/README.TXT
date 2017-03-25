logFarm Digital Monitoring System



CONTACT

- irc.freenode.net #logFarm
- https://plus.google.com/u/0/communities/116277776693290335317



DESCRIPTION

Features:

- records audio and video from multiple webcams attached to your
  computer, simultaneously

- video can be played back at any time, for any time period, for any
  camera and it's never necessary to stop recording

- various options for each camera can be set in a single configuration
  file  



HOW TO INSTALL AND RUN

logFarm DMS is a Java application and requires Java 8.

- To install Oracle's java 8 on Ubuntu:

  http://www.webupd8.org/2012/09/install-oracle-java-8-in-ubuntu-via-ppa.html

- Windows and MacOS:

  http://java.com/en/


Windows :: logFarmDMS_windows.zip

To run:

- double click: logFarmDMS.exe

NOTE: Tested with Windows Vista. I don't have access to any other
	  version of Windows so I don't know if logFarm DMS works on other
	  Microsoft operating systems.


Linux :: logFarmDMS_linux.zip

To run:

- double click: launch.sh

- or use the command line:

  cd logFarmDMS
 
  launch.sh

- or use the command line:

  cd logFarmDMS

  java -jar logFarmDMS.jar
  
An optional Linux .desktop file is included which can be copied to the
Linux desktop but it will need to be edited so that these two lines
point at the logFarmDMS directory:

Exec=/pathTo/logFarmDMS/launch.sh 
Icon=/pathTo/logFarmDMS/icon_64.png


MacOS :: logFarmDMS_macos.zip

NOTE: Untested. I don't have access to an Apple computer. I suspect
	  it's just a case of:

- double click logFarmDMS.jar



HOW TO RECORD

As soon as logFarm DMS starts, it will start recording from your
computer's webcam and microphone. If a webcam is not present then only
audio will be recorded.

'recording' will be stated at the bottom of the application.

logFarm DMS will search for two cameras connected to the computer by
default and only record from those it finds. '2' is a configuration
value in the configuration.json file:

  "number_of_cameras": "2"

To stop recording either press the top left record button or close
logFarm DMS.

logFarm DMS will continually record even when the database size has
reached its maximum allowed size, which is set in
configuration.json. The default maximum database size for each
camera's database is 10 gigabytes. When the maximum is reached logFarm
DMS deletes a few of the oldest video frames to make way for new ones,
allowing recording to continue without increasing the size of the
database.

FYI, the theoretical maximum size of each database is 140 terabytes !!

10 gigabytes is the default value and allows recording of
approximately two days of video before old video frames are deleted to
make way for new.

configuration.json can be used to set a cameras startup behavior
including: automatically start recording or not:

  "start_recording_at_startup": "yes"



LIVE FEED

Top right, live-feed from a camera. 

- Click the image to show a larger live-feed window.

  This window can be re-sized.

  The window's title states that you are viewing 'live' video.

Live-feed is always active, even when not recording.



PLAY BACK

- Click the play button, button left to show a separate window.

  The separate live-feed window and separate play back windows are the
  same window.

  The window's title states that you are viewing 'playback' video.

  Video and audio will start to play from the position of the slider;
  the date and time for which is stated above the slider. Moving the
  slider plays video from that point.

  logFarm DMS has been designed to play back video and record
  simultaneously; there is no need to stop recording while watching
  recorded video footage.

  HOWEVER, I recommend that you use headphones to listen to recorded
  video so that play back audio does not interfere with what is being
  recorded live.



EXPORTING TO VIDEO FILE

Requires:

- Sox (Sound eXchanger) :: http://sox.sourceforge.net

- Mencoder :: http://www.mplayerhq.hu


To install sox and mencoder:

- Linux

  sudo apt-get install sox mencoder

- Windows and MacOS

  https://sourceforge.net/projects/sox/files/sox/

  http://www.mplayerhq.hu/design7/dload.html


The export feature copies a selection of video and audio data to a
directory inside logFarmDMS directory or a user defined location:

- a pile of single images

- a pile of audio files

- createVideo.sh :: for Linux and MacOS
  createVideo.bat :: for Windows 

  the user must run this file to create: output.avi

  output.avi is the final video file



DATABASE FILES

Each camera has its own database file as does the computer's default
microphone so if you have two cameras attached to your computer, for
example: one built in and another plugged into a USB socket, then you
will have three database files:

  logFarmDMS_0.db
  
  logFarmDMS_1.db

  logFarmDMSaudio.db

Initially these files will appear in the logFarmDMS directory but you
can instruct logFarm DMS to put them anywhere by editing
configuration.json.

Please see: CONFIGURATION FILE :: configuration.json

for details of how to edit this file. configuration.json is just a
text file with KEY:VALUE entries that logFarm DMS reads before
starting. Initially it's not very readable so you will have to
reformat it to make it comprehensible.

To change the location of any of a camera's or microphone's database
file locations, change their value like this:

  "db_location" : "/aPath/somewhere"

and save configuration.json

A camera's database maximum size is configurable with this:

  "maximum_db_size" : "10"

"10" means the database for that camera will not grow any larger than
10 gigabytes. A value of "5" would mean the database will not grow
any larger than 5 gigabytes.

When the maximum database size is reached, the oldest video image is
deleted to make room for a new one to be added. This gives us
continuous recording even though the database size is constrained.

You will have to experiment with this a bit but I have found capturing
video at 640x480, with a 10 gigabyte maximum, stores approximately
two days of video before old video frames are deleted to make way
for new ones.



CAMERAS

logFarm DMS will record and playback from multiple cameras
simultaneously.

logFarm DMS automatically finds webcams built into your computer and
those attached via USB sockets.

As soon as logFarm DMS is launched recording starts from:

- all webcams

- the computer's default microphone

This behaviour can be configured by editing: configuration.json


   
MICROPHONES

logFarm DMS will record from a single microphone, whichever microphone
is set as your computer's default. This can be a microphone embedded
inside a USB webcam, the microphone built into your computer or one
plugged in to a jack on your computer. It is up to the user to set the
desired microphone as the computer's default.

One way to configure your computer's default microphone on linux:

- pavucontrol

  sudo apt-get install pavucontrol

  pavucontrol

  - Input Devices tab will list you microphones.

  	mute microphones that you don't want to use as your default
  	microphone


On windows:

- control panel > sound > Recording tab

  select a microphone and click the 'Set Default' button



CONFIGURATION FILE :: configuration.json

NOTE: KEY:VALUEs will change with future releases and have a GUI to
   	  make editing easier

configuration.json can be edited in an ordinary text editor.

configuration.json is automatically generated if it does not exist in
the logFarm DMS directory. This is the case when logFarm DMS is run
for the first time.

configuration.json is a JSON file but it is not formatted in a way
that is comprehensible. Here are some examples to make it more
readable:

- Open with an editor that can format JSON files. For example open
  configuration.json with Netbeans then press the key-chord:

  shift, alt, f

  configuration.json will be reformatted. It can then be edited and
  saved.

- jsonlint

  - install python-demjson

      sudo apt-get install python-demjson

  	  cat configuration.json | jsonlint -f > ~/temp/configuration.json.too
  	  
		creates a new file called: ~/temp/configuration.json.too

	  	you would edit this file and save it to the logFarmDMS
	  	directory as: configuration.json


If you want to reset configuration.json to factory default simply
delete it. A new one will be created the next time you run logFarm DMS.



BLOG :: 24 July 16

Version: 3.01 alpha


Testing on Ubuntu 14.04:

- laptop embedded camera
- USB camera 
- USB camera microphone set as computer default

NetBeans Profiler shows there aren't any runaway memory leaks.

So Linux good.


Testing on a 32-bit acer with Windows Vista:

- USB camera
- computer microphone

With Task Manager open, it's clear there aren't any run away memory
leaks.

However when logFarm DMS is closed on this Windows Vista laptop, a
60-70 megabyte Java zombie is left in memory.

The zombie should be deleted by selecting it from the Task Manager and
clicking the 'End Process' button.

logFarm DMS can be restarted and run without any conflict but each
start and exit leaves another zombie.

Did lots of testing biased towards the opinion that it's down to
JavaCV 0.9 and not my programming but a more likely cause is that I am
not cleaning up thoroughly enough.

Regarding Microsoft Windows 10 and Apple MacOS, I don't have access to
either operating system to test with.



TODO

- Use latest JavaCV libraries.

- SQLite database files are great because they can be backed up easily
  so I want away to just play back backed-up video and audio from
  archived database files.

  Creating a backup procedure that zips up all database files would
  keep things organised nicely. A playback application would be
  pointed at individual zip files.

- Video and audio data recorded on one computer accessible from
  another computer.

  Three-tier architecture: 

  - One or many Java applications send and receive video and audio
  	data via Apache Tomcat Servers. Tomcat would handle all database
  	interactions.

  - Android client.

  - User access: username and password

- GUI for editing configuration.json

- Fast forward play back.

- Motion detection.

  - email notifications

- Editable date-time selection.



