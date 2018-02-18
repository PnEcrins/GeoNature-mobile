# Set up a device for development

Before you can start debugging on your device, there are a few things you must do:

1. On the device, open the **Settings** app, select **Developer options**, and then enable
**USB debugging**.
  > **Note:** If you do not see **Developer options**, follow the instructions to [enable developer options](#enable-developer-options).

2. Set up your system to detect your device.
* **Windows:** Install a USB driver for Android Debug Bridge (adb). For an installation guide and
links to OEM drivers, follow this [link](https://developer.android.com/studio/run/win-usb.html).
* **Mac OS X:** It just works. Skip this step.
* **Ubuntu Linux**: Use `apt-get install` to install the `android-tools-adb package`. This gives you
a community-maintained default set of `udev` rules for all Android devices.
Make sure that you are in the `plugdev` group. If you see the following error message, adb did not
find you in the `plugdev` group:
  > error: insufficient permissions for device: udev requires plugdev group membership

  Use `id` to see what groups you are in. Use `sudo usermod -aG plugdev $LOGNAME` to add yourself to
the `plugdev` group.

## Enable developer options

On Android 4.1 and lower, the **Developer options** screen is available by default.
On Android 4.2 and higher, you must enable this screen as follows:

1. Open the **Settings** app.
1. (Only on Android 8.0 or higher) Select **System**.
1. Scroll to the bottom and select **About phone**.
1. Scroll to the bottom and tap **Build number** 7 times.
1. Return to the previous screen to find **Developer options** near the bottom.

At the top of the **Developer options** screen, you can toggle the options on and off. You probably
want to keep this on. When off, most options are disabled except those that don't require
communication between the device and your development computer.

Next, you should scroll down a little and enable **USB debugging**. This allows Android Studio and
other SDK tools to recognize your device when connected via USB, so you can use the debugger and
other tools.

## How to fix "insufficient permissions for device: verify udev rules."

Check if you are in `plugdev` group and if not, use `sudo usermod -aG plugdev $LOGNAME` to add
yourself to the `plugdev` group.

Find the bus and device id assigned by the kernel with `lsusb` command:

```
lsusb
...
Bus 003 Device 010: ID 18d1:4ee7 Google Inc.
...
```

Create a udev rules file `/etc/udev/rules.d/51-android.rules` as root if it doesn't exist and add
this line:

```
SUBSYSTEM=="usb", ATTR{idVendor}=="18d1", MODE="0664", GROUP="plugdev"
```

where `idVendor` value come from the output of lsusb. From the above example, the right `idVendor`
value is **18d1**. 

Now assign read permissions on the file, reload `udev` and reload the `adb` daemon:

```
sudo chmod a+r /etc/udev/rules.d/51-android.rules
sudo udevadm control --reload-rules
adb kill-server
adb start-server
```

You may have to disconnect and connect again your device to the USB port. You should see it by
issuing this command:

```
adb devices
```