# StudySpace
Your helper for improving productivity as a student.

# Introduction
StudySpace is a productivity application targeted towards students. The app consists of three key features: a calendar for deadline tracking and scheduling, a task list, and a Pomodoro study timer.  As academic tools increasingly become digital, StudySpace combines all your study tools on one digitized platform allowing all your digital learning materials to coexist in one place. This application was developed in Kotlin on Android Studio, and can be run from a phone or phone emulator. Each instance of the application will also have to connect to its own SQLite database that stores information about tasks and taks categories.

# Visual Demonstration
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
## How to build and install the app locally: 
Optional: If you want to run on an emulator, Android Studio is the recommended IDE to run the application. You will only need to run step 1 if using Android Studio. Run the application via the PersistentTimerApp folder. 
Step 1: Open up your terminal app and clone the repository: git clone https://github.com/ushaapldg/INET4121-Study-App.git
Step 2: Change directory to the app directory: cd INET-4121-Study-App
Step 3: Be sure to install any independencies: npm install
Step 4: To run the build, use npm run build OR npm start OR npm run dev to run in developer mode.

## Modifications 
1. Frontend & User Interface (UI)
If you are changing how the app looks or how users interact with the screen:
•Main Pages: Look in src/pages/ or src/app/ (for Next.js) or pages/ (for Nuxt/Vue). Each file typically corresponds to a URL route (e.g., about.tsx is /about).
•Reusable Components: Look in src/components/. This is where you find UI elements like Navbar, Footer, Button, or Card that appear on multiple pages.
•Global Styles: Styles are usually in src/styles/ or a file like globals.css. If using Tailwind, configuration is in tailwind.config.js.
•Static Assets: Images, icons, and fonts are typically kept in the public/ or src/assets/ folder.

2. Backend & Data Logic
If you are changing how data is processed, saved, or retrieved:
•API Endpoints: In modern full-stack frameworks (like Next.js), look in src/app/api/. In dedicated backends (Node/Express), look in src/routes/ or src/controllers/.
•Database Models: To change how data is structured in your database, look in src/models/ (for Mongoose/Sequelize) or prisma/schema.prisma (if using Prisma).
•Business Logic/Services: Complex logic is often abstracted into a src/services/ or src/lib/ folder to keep the controllers clean.

3. Application Configuration
If you are changing how the app connects to external services or behaves:
•Environment Variables: Sensitive data like API keys and database URLs are found in .env or .env.local.
•Dependencies: To add or update libraries, modify package.json (JavaScript/TypeScript), requirements.txt (Python), or Gemfile (Ruby).
•App Config: Framework-specific settings are in files like next.config.js, nuxt.config.ts, or vite.config.ts.

# Known Issues and Future Expansion
## Account Security
Implementing an account and security feature that allows users to login and save any of their tasks to their account. Adding security also allows account security within our application.
## Improved UI/UX
Conducting thorough user research to identify pain points for users and create better experience for student productivity.
## Lock Feature
Adding a lock feature where once you start your study session, notifications are disabled. As well as making apps that the user selects to be disabled while in the session.
## Web App Capability
Creating a browser version for web accessibility so users can access their productivity tools even without their phones.
