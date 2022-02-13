# ConnectionService APP
This app demonstrates the usage of ConnectionService

## What is ConnectionService?
An abstract service that should be implemented by any apps which either:

1. Can make phone calls (VoIP or otherwise) and want those calls to be integrated into the built-in phone app. Referred to as a system managed ConnectionService.
2. Are a standalone calling app and don't want their calls to be integrated into the built-in phone app. Referred to as a self managed ConnectionService.

## Build Instructions
Before building the project, you need to register your app with Firebase for receiving FCM notification.

**Note:** You can use Postman to send the payload and works as your server..

## References
1. <a href="https://developer.android.com/reference/android/telecom/ConnectionService">ConnectionService</a>;
2. <a href="https://developer.android.com/guide/topics/connectivity/telecom/selfManaged">Build a calling app</a>;

## App Video demo
https://user-images.githubusercontent.com/7091438/153768101-b6a603f4-5647-4538-9143-c940feb6ebd8.mp4

## Apps Screens

<table>
  <tr>
    <td>App screen asking for Grant Call permission</td>
     <td>Home page</td>
     <td>Asking for Call Deflect permission</td>
  </tr>
  <tr>
    <td><img src="https://github.com/sunnyc111/ConnectionService/blob/master/screen1.jpg"></td>
    <td><img src="https://github.com/sunnyc111/ConnectionService/blob/master/screen2.jpg"></td>
    <td><img src="https://github.com/sunnyc111/ConnectionService/blob/master/screen3.jpg"></td>
  </tr>
 </table>

<table>
  <tr>
    <td>Granting call deflect permission</td>
     <td>Incoming call FCM notification</td>
     <td>Showing call screen after clicking on Answer</td>
  </tr>
  <tr>
    <td><img src="https://github.com/sunnyc111/ConnectionService/blob/master/screen4.jpg"></td>
    <td><img src="https://github.com/sunnyc111/ConnectionService/blob/master/screen5.jpg"></td>
    <td><img src="https://github.com/sunnyc111/ConnectionService/blob/master/screen6.jpg"></td>
  </tr>
 </table>
