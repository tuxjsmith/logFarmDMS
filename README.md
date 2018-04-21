# logFarmDMS
Record and playback audio and video from multiple web cams simultaneously.

<b>CLONE THIS PROJECT</b>

- NetBeans > Team > GIT > Clone

- Repository URL: https://github.com/tuxjsmith/logFarmDMS.git

- GitHub user name.
- GitHub password

- Clone into: /home/userName/NetBeansProjects

If you see an error message:<br>
HIGHGUI ERROR: V4L: index 1 is not correct!

Then configuation.json has assumed two cameras are connected to your computer.
<pre>
"cameras": [{
	    "number": "0",
	    :
    }, {
            "number": "1",
	    :
    }]
</pre>

The error is not critical and the application will run normally. If the second camera configuration details are deleted:
<pre>
{
    "number": "1",
    :
}
</pre>

An error message will not be generated.

<br>
<b>CONTACT</b>

- irc.freenode.net #logFarm
- https://plus.google.com/u/0/communities/116277776693290335317

<br>
<b>DESCRIPTION</b>

- Record and playback, audio and video, from multiple webcams
  simultaneously.

- Video can be played back at any time, for any time period, for any
  camera and it's never necessary to stop recording.

- A single JSON configuration file for the application and all cameras.

<br>
<b>DEPENDENCIES</b>

- SQLlite: bundled.
- JSON Java package: bundled.
- OpenCV: bundled.

<br>
<b>PROJECT FORMAT</b>

NetBeans 8.2

- Select properties from top project node: logFarmDMS
- Right click > Properties
- Select Run.
- Add the following line to: VM Options ( including: - )<br>
<pre>
  -Djava.library.path="opencv_for_logfarmDMS/"
</pre>
  This assumes <b>opencv_for_logfarmDMS</b> is in the root of your logFarmDMS NetBeans project.
  
  <b>v0.9</b> of OpenCV is currently being used, <i>Later versions will not work</i>.

<br>
<b>HOW TO RECORD</b>

As soon as logFarm DMS starts, it will start recording from your
computer's webcam and microphone. If a webcam is not present then only
audio will be recorded.

<pre>
'recording'
</pre>

status will be shown at the bottom of the application.

logFarm DMS will search for two cameras connected to the computer by
default and only record from those it finds. '2' is a configuration
value in the configuration.json file:

<pre>
"number_of_cameras": "2"
</pre>

To stop recording either press the top left record button or close
logFarm DMS.

logFarm DMS will continually record even when the database size has
reached its maximum-allowed size, which is set in
configuration.json. The default maximum database size for each
camera's database is 10 gigabytes. When the maximum is reached logFarm
DMS deletes a few of the oldest video frames to make way for new ones,
allowing recording to continue without increasing the size of the
database.

FYI, the theoretical maximum size of each database is 140 terabytes !!

10 gigabytes is the default value and allows recording of
approximately two days of video before old video frames are deleted to
make way for new.

configuration.json can set a camera's startup behavior, including whether or not 
to automatically start recording:

<pre>
"start_recording_at_startup": "yes"
</pre>

<br>
<b>LIVE FEED</b>

Top right, live-feed from a camera. 

- Click the image to show a larger live-feed window.

  This window can be re-sized.

  The window's title states that you are viewing 'live' video.

<i>Live-feed is always active, even when not recording.</i>

<br>
<b>PLAY BACK</b>

- Click the play button, button left, to show a separate window.

  The floating live-feed and play back windows are the
  same window. Sorry if that confuses. Tap the small live-feed window
  and:
  - playback will stop automatically
  - live-feed will be displayed in the floating window
  
  tap the playback button:
  - the floating window will display playback video

  The floating window's title states whether or not it is displaying 'playback' 
  or live video.

  Video and audio will start to play from the position of the slider;
  the date and time for which is stated above the slider. Moving the
  slider plays video from that point.

  There is no need to stop recording while watching
  recorded video footage.

  <i>HOWEVER</i>, I recommend that you use headphones to listen to recorded
  video so that play back audio does not interfere with what is being
  recorded live.

<br>
<b>EXPORTING TO VIDEO FILE</b>

Requires:

- Sox (Sound eXchanger) :: http://sox.sourceforge.net
- Mencoder :: http://www.mplayerhq.hu

To install sox and mencoder:

- Linux

<pre>
sudo apt-get install sox mencoder
</pre>

- Windows and MacOS

  https://sourceforge.net/projects/sox/files/sox/
  http://www.mplayerhq.hu/design7/dload.html

The export feature copies a selection of video and audio data to a
directory inside the logFarmDMS directory or a user defined location:

- a pile of single images
- a pile of audio files

- createVideo.sh :: for Linux and MacOS
  createVideo.bat :: for Windows 

  the user must run one of these files to create: output.avi
  output.avi is the final video file

<br>
<b>DATABASE FILES</b>

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
text file with KEY:VALUE entries which logFarm DMS reads before
starting. Initially it's not very readable so you will have to
reformat it to make it comprehensible.

To change the location of any camera's or microphone's database
database file, change their value like this:

<pre>
"db_location" : "/aPath/somewhere"
</pre>

and save configuration.json

A camera's database maximum size is configurable with this:

<pre>
"maximum_db_size" : "10"
</pre>

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

<br>
<b>CAMERAS</b>

logFarm DMS will record and playback from multiple cameras
simultaneously.

logFarm DMS automatically finds webcams built into your computer and
those attached via USB sockets.

As soon as logFarm DMS is launched, recording starts from:

- all webcams
- and the computer's default microphone

This behaviour can be configured by editing: configuration.json

<br>
<b>MICROPHONES</b>

logFarm DMS will record from a single microphone, whichever microphone
is set as your computer's default. This can be a microphone embedded
inside a USB webcam, the microphone built into your computer or one
plugged in to a jack on your computer. It is up to the user to set the
desired microphone as the computer's default.

One way to configure your computer's default microphone on linux:

- pavucontrol

<pre>
sudo apt-get install pavucontrol
</pre>

  - Input Devices tab will list you microphones.

  	mute microphones that you don't want to use as your default
  	microphone

On windows:

- control panel > sound > Recording tab

  select a microphone and click the 'Set Default' button

<br>
<b>CONFIGURATION FILE :: configuration.json</b>

NOTE: KEY:VALUEs will change with future releases and have a GUI to
   	  make editing easier

configuration.json can be edited in an ordinary text editor.

configuration.json is automatically generated if it does not already exist in
the logFarm DMS directory. This is the case when logFarm DMS is run
for the first time.

configuration.json is a JSON file but it is not formatted in a way
that is comprehensible. Here are some examples to make it more
readable:

- Open with an editor that can format JSON files. For example open
  configuration.json with Netbeans then press the key-chord:

<pre>
shift, alt, f
</pre>

  configuration.json will be displayed in a more user freindly and readable way. It can then be edited and
  saved.

- jsonlint

  To install python-demjson

<pre>
      sudo apt-get install python-demjson
  	  cat configuration.json | jsonlint -f > ~/temp/configuration.json.too
</pre> 

		creates a new file called: ~/temp/configuration.json.too

	  you would edit this file and save it to the logFarmDMS
	  directory as: configuration.json

If you want to reset configuration.json to factory default simply
delete it. A new one will be created the next time you run logFarm DMS.

<br>
<b>THANK YOU</b>

I appreciate you taking the time to read this README. If you have <i>any</i> questions you can e-mail me at either:

- tuxjsmith@gmail.com
- paulb@logfarm.net

Best regards<br>
Paul Butterfield
