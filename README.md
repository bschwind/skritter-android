# Skritter Android

Skritter Android is a native implementation of Skritter

## Building

**Requirements**
- Android SDK
- Environment variable "ANDROID_HOME" is set to Android's root sdk directory (example: /Users/user1/Projects/android-lib/sdk/)
- Android SDK Build-tools: I believe revision 17 is what's needed, but installing all of them is a catch-all way to ensure you have what you need
- Gradle (optional)

**Command Line**

If you don't have gradle installed, simply run the Gradle wrapper file

*nix:

	./gradlew installDebug

Windows:

	gradlew installDebug

Similarly, if you have gradle installed, simply replace "gradlew" with "gradle"

To see a list of all available gradle tasks, run

	gradle tasks

**Android Studio**

Make sure Android Studio is running the latest version. Check for updates and install the latest version.

Open Project -> Navigate to the project's root directory, and choose open, and select settings.gradle. Android Studio should do the work of building out the project. Plug in your device and run the project with the green play button to deploy and run it on your device.

**Eclipse**

Eclipse? Why do you hate yourself? Regardless, you should be able to import the project from the root directory, and Eclipse will recognize what's going on. This is assuming you've installed ADT for Eclipse. I was able to deploy the app, but received an ANR when the app ran. I haven't had time to look into that yet.

## Links

API Documentation: http://beta.skritter.com/api/v0/docs

Media: http://skritter.joshmcfarland.net/media.zip