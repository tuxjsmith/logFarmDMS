# logFarmDMS
Record and playback audio and video from multiple web cams simultaneously.

CONTACT

- irc.freenode.net #logFarm
- https://plus.google.com/u/0/communities/116277776693290335317


DESCRIPTION

- Record and playback, audio and video, from multiple webcams
  simultaneously.

- Video can be played back at any time, for any time period, for any
  camera and it's never necessary to stop recording.

- A single JSON configuration file for the application and all cameras.


DEPENDENCIES

- SQLlite: bundled.
- JSON Java package: bundled.
- OpenCV: bundled.


PROJECT FORMAT

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
  
  
  


