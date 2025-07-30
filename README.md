<img width="82" align="left" src="https://raw.githubusercontent.com/kryptonbutterfly/HIDoverWifi_android/master/md/icon.svg"/>

# HID over Wifi Android

</br>
An android app that allows you to use your phone as a mouse and keyboard.

Requires a [service](https://github.com/kryptonbutterfly/HIDoverWifi_service) to be installed on the device to be controlled.

## Setup

In order to setup it's best to start with setting up the service first and come back to the setup of the app after that. [setup Service](https://github.com/kryptonbutterfly/HIDoverWifi_service#setup)

#### App Setup

* Download and install [HIDoverWifi.apk](https://github.com/kryptonbutterfly/HIDoverWifi_android/releases/download/v1.1.0/HIDoverWifi.apk)
* copy the public .p12 certificate to your phone
* connect to the wifi network your computer is connected to
* start the app
* open the settings
* Set `Address` to the ip of your pc
* Set the `server password`
* Ensure the keepalive interval is > 0 and < then the timeout configured in the service.
* click `Certificate File` and select the .p12 certificate
* Set the `Certificate Password` (the default password is `public`)
