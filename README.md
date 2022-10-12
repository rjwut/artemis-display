# Artemis Display

> **[🔽 DOWNLOAD LATEST RELEASE (.jar file)](https://github.com/rjwut/artemis-display/releases/latest)**

**Artemis Display** is an application that connects to an _Artemis: Spaceship Bridge Simulator_ server to display real-time gameplay information. The displays provided by this application are information radiators, not interactive consoles. Think of it as functional set dressing that you can add to your bridge.

## Requirements

_Artemis Display_'s two basic requirements are that it must run under a graphical user interface (not a headless system) and that it must have Java 8 or later installed. (See [java.com](http://www.java.com/) for information on how to install Java.)

Beyond that, the processor, memory, and graphical requirements will depend on the displays and configuration options you select. More complex displays and more intensive graphical options will require higher-end hardware, but simpler displays should run on even low-end devices such as the Raspberry Pi.

## Startup

_Artemis Display_ requires no installation. Simply run the JAR file and a configuration dialog will appear where you can set your desired options, then click "Start" to launch the display. A command line interface (CLI) is also available (described in the "Command Line Interface" section below), allowing you to skip the configuration dialog and launch the display immediately.

## Configuration

### Basic Options

- **Host**: After scanning the local network, a list of running _Artemis_ servers will be shown. Click the "Scan" button to scan the network for servers again. You may select one of them to connect to, or you can enter a host name or address manually. If the server is not running on the default port, you may specify the port by appending it to the address, separated with a colon, like this: `192.168.1.7:2011`. Note that specifying the host manually allows you to choose a server that is not running now but which will eventually be available. Upon launching the display, _Artemis Display_ will continually retry until it is able to connect.
- **Ship number**: The ship whose status is to be displayed. This is a number between 1 and 8, inclusive. (The ship whose name starts out as "Artemis" is ship 1.)
- **Artemis install path**: The directory where _Artemis_ is installed. On a Windows system, _Artemis Display_ will attempt to locate it automatically. If it is not found, you can manually select it by clicking the button. If omitted, some displays will have degraded functionality, and the systems display will be blank.
- **Display mode**: The type of display window to use. The options are fullscreen, windowed fullscreen, or windowed. Fullscreen is recommended if you are running one instance of _Artemis Display_ on this machine. Windowed fullscreen is the option you want if you are running multiple instances of _Artemis Display_ on this machine to show different displays on different monitors. If you select fullscreen but it isn't supported by your OS, _Artemis Display_ will automatically fall back to windowed fullscreen.
- **Monitor**: Which monitor will show the display. Click the "Identify" button to see what each monitor's number is.
- **Display**: Which display is to be shown. Each of the available display types is listed. (See the "Displays" section below for a description of each display.) You may also select "Custom layout", which will require you to select a layout file you've prepared earlier. (See the "Layouts" section below for details.)
- **Locale**: What locale will be used to render the display. Currently, _Artemis Display_ supports English and Spanish.

### Advanced Options

These options appear on the "Advanced" tab in the configuration dialog. Most of them have a performance impact when enabled, so many are off by default, but turning them on can make your display look nicer.

- **Antialiasing**: Causes lines and edges to render more smoothly, but decreases performance. Off by default.
- **Subpixel font rendering**: Causes text to have smoother edges, but decreases performance. Off by default.
- **Draw silhouettes**: Turning this off causes objects to be drawn as simple arrows in overhead views, which improves performance but doesn't look as nice. Note that ships always change to arrows if they get small enough, regardless of this setting. On by default.
- **Shake on impact**: Causes the screen to shake when the ship is struck by weapons fire. This effect has almost no impact on performance, but is available as an option so you can turn it off if you don't like it. On by default.
- **Dim on impact**: Causes the screen to dim when the ship is struck by weapons fire. This has a smaller impact on performance than "Static on impact," so you may wish to choose this if your machine struggles with rendering static. Off by default.
- **Static on impact**: Causes the screen to fill with static when the ship is struck by weapons fire. This can have a significant impact on performance but looks pretty cool. Off by default.

## Displays

The available display types are listed below. Any of these can be shown individually or arranged into composite layouts. (See the "Layouts" section below.) To specify a display type for a component in a layout, you must know that display's key; the key for each display is shown in parentheses in the title of the corresponding section.

Some displays draw information from data files in the _Artemis_ install directory, and will have degraded functionality if it is not provided. (See the relevant question in the FAQ.)

### Alert status (`alert`)

This provides a large alert status display, similar to the "Red alert" screens you see in _Star Trek_. The appearance of the display is determined as follows:

- **CONDITION RED**: The ship is at red alert. This display is animated.
- **CONDITION YELLOW**: The ship is not at red alert but the shields are up.
- **CONDITION BLUE**: The ship is held by a base's tractor beam with shields down and not at red alert. This display will be animated while the ship is being drawn toward the base, and become static when docking completes and resupply commences.
- **CONDITION GREEN**: None of the above conditions apply.

This display adapts its appearance according to its aspect (normal, wide, or tall). For example, you can make it a wide strip at the top or bottom of your layout and it will still look good.

### Bases status (`bases`)

Displays the status of all bases, both friendly and enemy. The display is a table with five columns:

- **Name**: A short name for the base. _Artemis Display_ will attempt to create a reasonable short name if the name of the base is longer than four characters. For example, "Arvonian Base 14" will become "A14". (Note that this may coincide with the name of a ship, but since this display only shows bases, it should remain unambiguous.) The name will be rendered in green if the base is friendly, red if it is an enemy, and dark gray if it has been destroyed.
- **Bearing**: The bearing from your ship to the base.
- **Distance**: How far away the base is. This number will turn blue if you're within docking distance.
- **Shield**: Displays the base's shield strength as a percentage. This will be green when the shields are at full strength and gradually turn red as the shields weaken.
- **Type**: The type of base. The possible types are: deep space, civilian, command, industrial, science, or enemy. Base type is determined by examining its shield strength, beam or torpedo ports, and build speed; a base that matches the profile of one of the stock base types will be categorized as such, even if it has a different name. Civilian and science bases have the same profile; a base with this profile will be categorized as a science base if it has "science" in the name; otherwise, it will be categorized as a civilian base.

### Captain target (`captain-target`)

Displays the object targeted by the captain on the captain's map console, with any available information about the target.

### Long range sensor display (`lrs`)

This view is similar to the LRS display in the stock client.

### Missions display (`missions`)

Shows all available missions, including energy offers. Each mission is shown as a card with the name of the contact or contacts for that mission. Missions are added to the display when the are initially offered by a contact. This will happen when you hail them, or for the missions where you are asked to bring something from one contact to another, when they hail you. Missions are removed from the display when completed or failed. (Destruction of a contact for an unmet mission requirement counts as a failure.) The "courier" missions have two contacts, the first of which will gray out after you have completed the pickup step of the mission.

### Ordnance display (`ordnance`)

Shows how much of each ordnance type is held by the player ship and each friendly base. The count turns yellow when it is less than 4 and red if it's 0. The ordnance amounts for bases start out unknown and are  populated as COMMs receives information from them. There are two ways that this information gets filled in:

- A base notifies COMMs that they finished building some ordnance. Only the count for that ordnance type for that base will be updated.
- COMMs hails a base and asks for their status, and they respond with their full ordnance list. The entire row for that base will be populated.

Note that there are some situations in which the amount of ordnance at a base may change without an update to COMMs:

- A player ship takes ordnance from the base.
- A mission reward is dropped off at a base.

These situations will make ordnance counts inaccurate until a new update is received. If it is suspected that a base's counts are out-of-date, the COMMs officer may ask a base for their status, which will cause the display to be updated.

### Player ship (`player-ship`)

Displays a wireframe of player ship, with general status information.

### Science target (`science-target`)

Displays a wireframe of the object targeted by the science officer, with any available information about the target.

### Systems status (`systems`)

Shows a rotating 3D view of the player ship with the systems grid overlaid on it. Nodes turn red as they are damaged. The positions of DAMCON teams are shown as diamonds. A readout showing how much damage each system has incurred is shown, as well.

### Tactical display (`tactical`)

Shows the area near the player ship. This view is similar to the display shown on the helm and weapons consoles.

### Tubes status display (`tubes`)

Displays the status of each torpedo tube. Each tube is displayed as a progress bar and is labeled with the name of the ordnance it contains (or "empty" if it contains no ordnance). The progress bar fills during loading and drains during unloading.

### Weapons target (`weapons-target`)

Displays a wireframe of the ship targeted by the weapons officer, with any available information about the target.

## Running

Once you have set your configuration options and launched the display, it will display "Waiting for server" and the server's expected address, then wait for the _Artemis_ server to become available. As soon as it is, it will immediately connect, and the display will change to show the words "[ship name] standing by." This lets you confirm that the display is connected to the server and is using the correct ship. Once the simulation starts, _Artemis Display_ will automatically show the configured display. When the simulation ends, it will go back to the "[ship name] standing by" screen. If the connection is lost, the "Waiting for server" message will reappear and _Artemis Display_ will keep trying to reconnect.

In fullscreen or fullscreen windowed modes, you can change which monitor is showing the display by pressing `SHIFT-LEFT` or `SHIFT-RIGHT`. To close _Artemis Display_, simply press `ESC`. In windowed mode, you can move or close with window with the standard window controls for your operating system.

## Command Line Interface

_Artemis Display_ can be configured and launched from the command line. This allows you to pre-set configuration values, and even launch the display immediately instead of showing the configuration dialog.

Syntax:

```bash
java [Java options] -jar <jarFile> [ArtemisDisplay options]
```

Simple example to launch an alert status display (assuming the JAR file is named `artemis-display-2.0.0.jar` and is in the current directory):

```bash
java -jar artemis-display-2.0.0.jar --host 192.168.1.7 --display alert
```

Available options:

- `--antialias`: (optional) Turns on antialiasing. This improves display appearance but decreases performance.
- `--artemis {dir}`: (optional) Specifies where _Artemis_ is installed. This should be followed by the location on disk where you have installed _Artemis_. If there is a space in the path, surround the path with double-quotes. If this option is omitted, _Artemis Display_ will attempt to auto-locate the installation. If it cannot be found, it will run without it, but as mentioned earlier, some displays will have degraded functionality, and the systems display will be blank.
- `--dim`: (optional) Turns on dimming the screen on impact.
- `--display {displayKey}`: (required if `--layout` is omitted) Specifies which display to show. This should be followed by the key for the desired display type.
- `--export-strings`: (optional) Creates a file named `strings.txt` that contains the localized strings for English (United States), then exits. All other options are ignored. The file will be created in the same directory as the JAR file. If the `strings.txt` file already exists, it will be overwritten. See the **Localization** section for more information. This option is not exposed in the configuration window.
- `--force-dialog`: (optional) By default, if all required parameters are provided on the command line, the configuration dialog is skipped and the display launches immediately. This option forces the configuration dialog to appear anyway.
- `--help`: (optional) Prints out command line help and exits. All other options are ignored.
- `--host {nameOrIp}`: (required) Specifies the address where the _Artemis_ server is located. This should be followed by the IP address or host name of the _Artemis_ server. If the server is not using the default port, you can append the port number to the address or host name, separated with a colon.
- `--layout {jsonFile}`: (required if `--display` is omitted) Specifies that a layout file should be used to build a compound display. This should be followed by the location on disk where the layout file is found. If there is a space in the path, surround the path with double-quotes. See the "Layouts" section below for details.
- `--locale {tag}`: (optional) The tag identifying the locale to use. (See "Locale Tags" under the "Localization" section below for details.) If omitted, _Artemis Display_ will auto-detect your locale from your operating system settings. If the locale is not supported by _Artemis Display_, it will fall back to American English (tag: `en-us`). Note that this only affects the configuration dialog and the displays; error messages reported at the command line are not internationalized.
- `--mode {mode}`: (optional) Which display mode to use. This should be followed by the name of the display mode. Valid options are `fullscreen`, `windowed-fullscreen`, or `windowed`. If omitted, `fullscreen` is assumed.
- `--monitor {number}`: (optional) Sets the monitor on which to show the display. This should be followed by a number greater than zero and less than or equal to the number of monitors you have. If omitted, `1` is assumed.
- `--no-shake`: (optional) Turns off the shaking effect when an impact occurs.
- `--no-silhouettes`: (optional) Renders simple arrows instead of ship silhouettes. This may improve performance on low-powered devices when rendering ships.
- `--ship {number}`: (optional) Indicates the ship whose status will be displayed. This should be followed by a number from `1` to `8`, inclusive. If omitted, `1` is assumed.
- `--static`: (optional) Causes the display to fill with static when an impact occurs. This looks cool but decreases performance.
- `--subpixel-font`: (optional) Turns on subpixel font rendering. This improves display appearance but decreases performance.

Java options generally do low-level things like control how much memory the application uses. You can usually ignore them. For more information about Java arguments, see the documentation for the version of Java you are running.

If all required options are specified and the `--force-dialog` option is omitted, _Artemis Display_ will skip the configuration dialog and display the screen immediately. Otherwise, the setup window will be displayed with the specified options already filled in.

## Layouts

A layout allows you to show multiple displays arranged on one screen. To use a layout, you must create a layout file, which is a [JSON](https://en.wikipedia.org/wiki/JSON) file that describes how the displays are arranged.

Layouts are ways of dividing the screen into smaller regions, each of which can have its own display. Layouts can also be nested.

_Artemis Display_ supports the following layout types:

- `absolute`: Displays can be arranged at arbitrary locations on the screen.
- `grid`: The screen is divided into a grid of rectangles of equal size.

### Examples

This example layout divides the screen into four quarters, and shows current ship status in the upper-left, and target statuses in the other three:

```json
{
  "type": "grid",
  "rows": 2,
  "cols": 2,
  "displays": [
    {
      "type": "player-ship",
      "title": "Artemis",
      "row": 0,
      "col": 0
    },
    {
      "type": "weapons-target",
      "title": "Weapons",
      "row": 0,
      "col": 1
    },
    {
      "type": "science-target",
      "title": "Science",
      "row": 1,
      "col": 0
    },
    {
      "type": "captain-target",
      "title": "Captain",
      "row": 1,
      "col": 1
    }
  ]
}
```

This example shows the ordnance display, with a picture-in-picture pane of the weapons target display at the lower-right:

```json
{
  "type": "absolute",
  "displays": [
    {
      "type": "ordnance",
      "x": "0%",
      "y": "0%",
      "width": "100%",
      "height": "100%"
    },
    {
      "type": "weapons-target",
      "x": "85%",
      "y": "85%",
      "width": "15%",
      "height": "15%",
      "border": true
    }
  ]
}
```

### Global Sub-display Parameters

All layouts support the following options on their sub-displays:

- `title`: (default = `null`) A string title to render at the top of the display. If `null`, no title is rendered.
- `border`: (default = `false`) A boolean value indicating whether the display should be rendered with a border around it.

### Absolute Layout

The absolute layout allows you to arrange displays arbitrarily. Each display is rendered in the order specified, so later displays will render on top of earlier ones.

Layout Parameters

This layout has no parameters.

Sub-display Parameters

The position and size of each sub-display is specified as fractions of the parent area width or height. These values may be expressed as percentages, fractions, or values between `0` and `1`. For example, `'25%'`, `'1/4'`, and `'0.25'` all represent the same value.

- `x`: (required) The position of the display's upper-left corner along the X-axis, where `0%` is at the left edge of the parent display area.
- `y`: (required) The position of the display's upper-left corner along the Y-axis, where `0%` is at the top edge of the parent display area.
- `width`: (required) The width of the display, as a percentage of the parent display area's width.
- `height`: (required) The height of the display, as a percentage of the parent display area's height.

### Grid Layout

The grid layout divides the parent display area into a grid of rectangles of equal size. Each sub-display is shown in a grid cell.

Layout Parameters

- `rows`: (required) The number of rows in the grid.
- `cols`: (required) The number of columns in the grid.

Sub-display Parameters

- `row`: (required) The index of the first row where this display is located. The topmost row's index is `0`.
- `col`: (required) The index of the first column where this display is located. The leftmost column's index is `0`.
- `rowSpan`: (default = `1`) The number of rows this display spans.
- `colSpan`: (default = `1`) The number of columns this display spans.

## Localization

_Artemis Display_ can be rendered using multiple locales. The information in this section explains how you can add support for a new locale to _Artemis Display_.

### Locale Tags

Each locale is represented by a _tag_. Each tag consists of two characters identifying the language, optionally followed by two more characters identifying a country, separated by a hyphen (`-`). _Artemis Display_ supports the following locales "out of the box":

| Tag     | Locale                  |
| ------- | ----------------------- |
| `en-us` | English (United States) |
| `es-mx` | español (México)        |

These tags can be seen in the locale selector on the configuration dialog.

To add support for a new locale, you will first need to know its tag. You can look up the appropriate language code and country code on these pages:

- [Language codes (column 639-1)](https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes)
- [Country codes (column Alpha-2)](https://en.wikipedia.org/wiki/ISO_3166-1#Current_codes)

For example, the tag for Canadian French would be `fr-ca`. German would be `de`.

### Creating a Strings File

All the text that can be shown by _Artemis Display_ is contained in a file called `strings.txt`. To build support for a new locale, you will first want a copy of this file to work with. To do this, use the `--export-strings` command line option as described in the **Command Line Interface** section above. This will create a copy of the English verison of `strings.txt` in the same directory as the JAR file. Open that file in your favorite text editor.

Each entry in the file is in the following format:

```txt
{key}={value}
```

When _Artemis Display_ wishes to display some text (called a _string_), it looks up the text for that locale by using its _key_. For example, the key `configDialog.connect` belongs to the label on the "Connect" button in the configuration dialog. If you look up the line that uses that key in the exported `strings.txt` file, it looks like this:

```txt
configDialog.connect=Connect
```

This is how _Artemis Display_ knows what text to put on the "Connect" button in each language.

Occasionally, the text contains placeholders to insert data. A placeholder consists of curly braces with a number inside: `0` for the first placeholder value, `1` for the next, and so on. For example, the entry for the text that appears on the screen when connected to a server with no simulation running looks like this:

```txt
canvas.standing_by={0} standing by
```

Here, the `{0}` is a placeholder for the name of the ship. If the ship's name were _Artemis_, the resulting text would be "Artemis standing by".

There are some additional formatting options that can be specified within the placeholders. You can find full details in the description of pattern strings on [this page](https://docs.oracle.com/javase/8/docs/api/java/text/MessageFormat.html), but the following examples should be adequate:

- `{0,number}`: Format the value for placeholder `0` as a number
- `{0,number,integer}`: Format the value for placeholder `0` as an integer, rounding as needed.
- `{0,number,0.0}`: Format the value for placeholder `0` as a number, rounding to one decimal place, which is always displayed even if it's `0`.

For example, when the distance to a target is more than 1000, this string is used:

```txt
target.range.far=Range: {0,number,0.0}k
```

The distance value, which has already been divided by 1000 before being plugged into the string, will be rounded to one decimal place. So if the distance to the target is 12,047, the formatted text will be "12.0k".

Lines which start with hash symbols (`#`) are comments and are ignored by _Artemis Display_. You can use comments and blank lines to make `strings.txt` more readable.

### Adding a Locale to _Artemis Display_

When your translation is complete, create a directory adjacent to the JAR file whose name is `locale_{tag}`, where `tag` is the locale tag. (So for French Canadian, the directory would be named `locale_fr-ca`.) Put the `strings.txt` file inside that directory. When you run the JAR, _Artemis Display_ will pick up your locale and make it an option in the selector in the configuration dialog. You will also be able to select it using the `--locale` command line option.

### Custom Fonts

You may need a custom font to support the characters required for your locale. Simply drop the appropriate `.ttf` file into the same directory as your `strings.txt` file and _Artemis Display_ will use it instead of the default font. If you put more than one font in the directory, it will pick one and ignore the others, so just use one.

## FAQ

### What versions of _Artemis_ is supported?

At this writing, 2.7.5 or later. The forthcoming _Artemis_ 3.0 likely has a completely different network protocol, so it will not work with that when it comes out.

### What display degradations occur if the _Artemis_ install directory is not specified?

- The systems display is blank.
- Wireframes and silhouettes are replaced with arrows.
- Vessel identification is unavailable.
- Base type (e.g. industrial, science, etc.) cannot be determined.
- All biomechs appear hostile (because they cannot be distinguished from other non-allied NPCs).
- NPCs may appear to have special abilities that they actually lack.
- Ship appears to have six torpedo tubes in the tubes display.
- Player ships with combat jump ability won't show the combat jump charge meter.

### Why doesn't the systems display show energy and coolant allocations or heat levels?

That information is only sent to the engineering console, and only one client may claim that console per ship. If _Artemis Display_ claimed that console in order to display that information, you could not have an engineer.
