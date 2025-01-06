# OldSpoutLauncher

Launcher for Spoutcraft 1.6.4

This uses [a patch](https://github.com/ReSpouted/Grease) along with [RetroMCP](https://github.com/ReSpouted/RetroMCP-Legacy)
to reconstruct Spoutcraft's source code without sharing the entirety of Minecraft's code.

**This program must be run with java 8 to work properly.** If you don't know what this means, the scripts in the [releases](https://github.com/ReSpouted/OldSpoutLauncher/releases) will handle everything for you.

## How to run

**When launching the game for the first time, it may look like the launcher is frozen. This is expected, and you just need to wait it out. The times after that will launch faster.**

- Download the file that matches your operating system from [the latest release](https://github.com/ReSpouted/OldSpoutLauncher/releases/latest).
- **Windows:**
  - Open the folder where you downloaded the file with file explorer
  - Right click on the file and select `Extract all...`
  - Once you're done, you'll see a new folder with the same name as the file you downloaded. Open it.
  - Inside you will find a file called `launch` or `launch.ps1`. Right click it and select the option `Run with powershell`.
  - Wait a bit until the launcher window appears. You can then log in and launch the game from there.
- **MacOS:**
  - Open the folder where you downloaded the file with finder
  - Double click the file
  - You'll see a new folder with the same name as the file you downloaded. Select it and press `Command` + `Option` + `C`.
  - Run an application called `Terminal`. (Press `Command` + `Space`, type `Terminal` and press enter)
  - A black window will appear. In it, type `cd ` with a space at the end, then press `Command` (`⌘`) + `Shift` + `V`, then press enter.
  - type `./launch.sh` then press enter.
  - Wait a bit until the launcher window appears. You can then log in and launch the game from there.
- **Linux:**
  - Launch your terminal. You can probably find it by searching `terminal` in your start menu.
  - type `cd ~/Downloads` (or, if you downloaded the file somewhere else, enter that location instead) and press enter.
  - type `tar -xzf oldspoutlauncher-linux.tar.gz` and press enter.
  - type `cd oldspoutlauncher-linux` and press enter.
  - type `./launch.sh` and press enter.
  - Wait a bit until the launcher window appears. You can then log in and launch the game from there.

