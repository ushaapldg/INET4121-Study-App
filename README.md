Project Documentation

How to run the project: Make sure to run the project via the PersistentTimerApp folder.
Open the "App" folder -> Open the src folder -> Open the java/com/example/persistent... folder -> Open MainActivity.kt
This specific file will be the main file. It will create the application, and will allow us to make changes to the application when clicking buttons and etc.

To change the UI specifically, it will be listed in the "res" folder in the main folder.
Click the "layout" folder -> The layouts for everything should be posted those folders.

# Intro Paragraph

# Visual Demo
https://youtu.be/arqrQT-Kxzk

# User Instructions

## Overview
This app combines a simple daily task organizer with a built‑in countdown timer. Users can select dates on a calendar, create categories, add tasks for specific days and run a timer without leaving the app.

---

## Calendar Screen

### Opening the App
The app launches directly to the **Calendar** screen.

### Selecting Dates
- Tap any date to view tasks assigned to that day.
- Use the **<** and **>** icons to switch between months.
- Categories appear on every date, but **tasks are tied to the selected date**.

### Categories
- Tap the **+** button to create a new category.
- Categories appear across all dates.
- Long‑press a category name to delete it.
- Categories cannot be renamed.

### Tasks
- Add tasks under any category for the selected date.
- Tasks belong only to the date they were created on.
- Tap the circle icon next to a task to complete and immediately delete it.
- Tasks cannot be deleted manually except by completing them.

### Navigation
- Tap **Go to Timer** to switch to the Timer screen.

---

## Timer Screen

### Setting the Timer
You can set the countdown in two ways:

#### Preset Buttons
- **25 min**
- **5 min**
- **15 min**

Tapping a preset instantly fills the timer with that value.

#### Manual Entry
Enter hours, minutes, and seconds directly into the **HH : MM : SS** fields.

### Running the Timer
- Tap **Start** to begin the countdown.
- Tap **Reset** to clear the timer and enter a new value.
- The timer continues running even if you switch back to the Calendar screen.
- The timer does not prevent the phone from sleeping.
- The app does not send notifications or alerts when the timer finishes.

### Navigation
- Tap **Go to Calendar** to return to the Calendar screen.

---

## Requirements
- No permissions required.
- Project targets **SDK 20** (minimum Android version depends on your build settings).


# Developer Instructions
How to build and install the app locally: Optional: If you want to run on an emulator, Android Studio is the recommended IDE to run the application. You will only need to run step 1 if using Android Studio. Run the application via the PersistentTimerApp folder. Step 1: Open up your terminal app and clone the repository: git clone https://github.com/ushaapldg/INET4121-Study-App.git, Step 2: Change directory to the app directory: cd INET-4121-Study-App, Step 3: Be sure to install any independencies: npm install, Step 4: To run the build, use npm run build OR npm start OR npm run dev to run in developer mode.

How to modify the application - Usha

# Known Issues
Write in existing issues/future changes
