# Kapture - Screen recorder ![Static Badge](https://img.shields.io/badge/version-v1.4.0-green) [![Static Badge](https://img.shields.io/badge/license-Apache_2.0-orange.svg)](https://opensource.org/licenses/Apache-2.0) ![Static Badge](https://img.shields.io/badge/apk_size-155_MB-7C39E0)
Change README language: 
[![en](https://img.shields.io/badge/lang-en-blue.svg)](https://github.com/hms-douglas/kapture)
[![pt-br](https://img.shields.io/badge/lang-pt--br-blue.svg)](https://github.com/hms-douglas/kapture/blob/master/readme/pt_br/README.md)
</br>
</br>
Kapture is a screen recorder for android that allows to capture microphone audio while sharing it with other apps.
</br>
</br>
I was looking for an app that allow me to record my screen and microphone while sharing the microphone with other apps, like games.
Unfortunately I didn't find any inside the Google Play.
The ones I found required sideload, which I was a bit concerned about installing because it required some dangerous permissions and I didn't have access to the code.
Therefore I decided to build one on my own. I'm also sharing it here. You can download the .apk and install it, or you can download the source code, read it and built it yourself.
</br>
</br>
<img src="readme/app.gif" width="40%"/>
##
### Features
<ul>
  <li>Capture the screen:
    <ul>
      <li>Set resolution;</li>
      <li>Set quality (bit rate);</li>
      <li>Set orientation;</li>
      <li>Set recording FPS.</li>
    </ul>
  </li>
  <li>Capture internal audio (if the app that is playing the audio allows it to be captured):
    <ul>
        <li>Capture in mono or stereo.</li>
    </ul>
  </li>
  <li>Capture microphone audio (while sharing with other apps):
    <ul>
      <li>Increase/decrease volume.</li>
    </ul>
  </li>
  <li>Pause/Resume capturing;</li>
  <li>Profiles;</li>
  <li>Floating UI;
      <ul>
         <li>Menu (Stop, pause, screenshot, draw, minimize, close, time, time limit, camera);</li>
         <li>Camera (front, back / extra styles);</li>
         <li>Text (extra styles);</li>
         <li>Draw (extra styles).</li>
         <li>Image;</li>
         <li>App shortcuts.</li>
      </ul>
   </li>
  <li>Generate extra video files:
   <ul>
      <li>Without audio;</li>
      <li>With internal audio only;</li>
      <li>With microphone audio only.</li>
    </ul>
  </li>
  <li>Generate extra audio files:
    <ul>
      <li>Both audio;</li>
      <li>Internal audio only;</li>
      <li>Microphone audio only.</li>
    </ul>
  </li>
  <li>Notification shortcuts;</li>
  <li>Quick tile shortcut:
    <ul>
      <li>Start/stop capturing;</li>
      <li>WiFi share.</li>
    </ul>
  </li>
  <li>Widgets:
    <ul>
      <li>Basic - Start/Stop;</li>
      <li>Full - Start/Stop and Pause/Resume;</li>
      <li>WiFi Share shortcut;</li>
      <li>Profiles shortcut;</li>
    </ul>
  </li>
  <li>Launcher static shortcuts;</li>
  <li>Manage all captures made by the app:
    <ul>
      <li>Check info like resolution, date of creation, size, ...</li>
      <li>Remove from the app;</li>
      <li>Delete file(s) from the device;</li>
      <li>Check related files (extra files generated);</li>
      <li>Rename;</li>
      <li>Share.</li>
    </ul>
  </li>
  <li>WiFi share;</li>
  <li>Countdown to start capturing;</li>
  <li>Multiple auto stop options;</li>
  <li>Multiple auto before start options;</li>
  <li>Internal viewer (player):
     <ul>
       <li>Audio player;</li>
       <li>Video player.</li>
     </ul>
  </li>
  <li>App language:
   <ul>
      <li>English</li>
      <li>Português (Brasil).</li>
    </ul>
  </li>
  <li>Light and dark mode (auto or manually).</li>
  <li>Storage control.</li>
  <li>Tablet UI.</li>
</ul>

##
### Sharing microphone
According to the Android <a href="https://developer.android.com/media/platform/sharing-audio-input" target="_blank" rel="noreferrer">documentation</a>, after the Android 10/11, apps can only share the microphone input between them in specific cases.
</br>
</br>
Kapture is built over an accessibility service, turning it into a specific case, like mentioned <a href="https://developer.android.com/media/platform/sharing-audio-input#accessibility_service_ordinary_app" target="_blank" rel="noreferrer">here</a> in the android docs.

##
### Android permissions (required to use)
<ul>
  <li>Microphone: Used to capture the microphone and internal audio:
    <ul>
      <li>android.permission.RECORD_AUDIO</li>
    </ul>
  </li>
  <li>Notification: Used to show notifications and start the foreground service:
    <ul>
      <li>android.permission.POST_NOTIFICATIONS</li>
    </ul>
  </li>
  <li>Storage: Used to create and manage the files:
    <ul>
      <li>android.permission.WRITE_EXTERNAL_STORAGE</li>
      <li>android.permission.READ_EXTERNAL_STORAGE</li>
      <li>android.permission.MANAGE_EXTERNAL_STORAGE</li>
    </ul>
  </li>
  <li>Secure settings: Used to share the microphone with other apps and start the accessibility service:
    <ul>
      <li>android.permission.WRITE_SECURE_SETTINGS</li>
      <li>android.permission.FOREGROUND_SERVICE</li>
    </ul>
  </li>
  <li>Internet: Used to search for updates (does not auto update), to open external links and to share files over WiFi:
    <ul>
      <li>android.permission.INTERNET</li>
      <li>android.permission.ACCESS_NETWORK_STATE</li>
    </ul>
  </li>
  <li>Camera: Used to show the camera overlay:
    <ul>
      <li>android.permission.CAMERA</li>
    </ul>
  </li>
  <li>Optimization: Used as shortcut to disable the phone's battery optimization for the app:
    <ul>
      <li>android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS</li>
    </ul>
  </li>
  <li>Packages: Used to launch apps and add shortcuts to the floating menu:
    <ul>
      <li>android.permission.QUERY_ALL_PACKAGES</li>
    </ul>
  </li>
</ul>

##
### Installing
#### Option 1:
- Download the source code, or clone this repository, and build the app using Android Studio.

#### Option 2:
<ul>
  <li>You can download the latest version .apk <a href="https://drive.google.com/file/d/1ARA2P2UIZ9leSuChZ1q2N5OHGd_t4vss/view" target="_blank" rel="noreferrer">here</a>*;</li>
  <li>You can check all previous versions .apk links for download <a href="https://github.com/hms-douglas/kapture/blob/master/dist/all.json" target="_blank" rel="noreferrer">here</a>*.</li>
</ul>
* All .apks listed here were built by me, are not minimified and are hosted on Google Drive.
* Google Play Protect may prevent the app from being installed using the .apk file. In this case, disable the Google Play Protect, install the file and then enable it again.

##
### Donations
- If you would like to support me, you can make a donation clicking on the button bellow... Thank you! ❤️
  <a href="https://www.paypal.com/donate/?hosted_button_id=7XGH7WXU5C7K6">
  <img src="readme/en/paypal.png" width="160" height="50"/>
  </a>

##
### Log
<b>v1.4.0</b>
<ul>
   <li>Quick tile for WiFi Share added;</li>
   <li>App shortcut added to floating menu;</li>
   <li>Password added to WiFi Share (+ "refresh password feature")</li>
   <li>Profiles added;</li>
   <li>Profiles widget added;</li>
   <li>Before start options added;</li>
   <li>Back to top button added to kaptures screen;</li>
   <li>Language is now manageable through the system menu;</li>
   <li>Notifications improved;</li>
   <li>Bug fix;</li>
   <li>Minor UI changes.</li>
</ul>
<b>v1.3.0</b>
<ul>
   <li>Auto stop option based on battery level added;</li>
   <li>Storage usage option added + Clear cache option;</li>
   <li>Image overlay added;</li>
   <li>Widgets added (Basic, Full, WiFi share);</li>
   <li>Option to control token recycling added;</li>
   <li>Option to make video seekable added inside the video viewer;</li>
   <li>Table UI support added;</li>
   <li>Draw overlay now supports colors with alpha channel;</li>
   <li>Draw overlay now supports screenshots (draw only and/or screen);</li>
   <li>FFmpeg license changed from LGPL v3.0 to GPL v3.0;</li>
   <li>Bug fix;</li>
   <li>Minor UI changes.</li>
</ul>
<b>v1.2.0</b>
<ul>
   <li>Loading performance improved;</li>
   <li>Pause/resume option added;</li>
   <li>Launcher static shortcuts added.</li>
</ul>
<b>v1.1.0</b>
<ul>
   <li>Fixed notification actions not closing activity when action is completed;</li>
   <li>Fixed notification channel name;</li>
   <li>Fixed some translation errors;</li>
   <li>New menu overlay UI;</li>
   <li>Camera overlay added;</li>
   <li>Text overlay added;</li>
   <li>Draw overlay added;</li>
   <li>WiFi share added;</li>
   <li>Countdown to start added;</li>
   <li>Auto stop options added;</li>
   <li>Screenshot (print screen) while capturing added;</li>
   <li>Option to enable/disable some notifications added;</li>
   <li>Capturing orientation added;</li>
   <li>Option to ignore phone's battery optimization added;</li>
   <li>UI updated;</li>
   <li>Credits updated.</li>
</ul>
<b>v1.0.0</b>
<ul>
  <li>Release.</li>
</ul>

##
### License
Copyright 2024 Douglas Silva

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
