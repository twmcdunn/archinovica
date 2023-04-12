# archinovica
A digital augmented instrument (DAI) for extended just intonation

This read me file focuses on using the instrument in performance; more information to come for developers.

To use the instrument:
1. Download 'Archinovica.jar'
2. Connect your electronic piano/keyboard to the computer
3. Turn 'local off' on your keyboard (consult the manual for your keyboard)
4. Double click the .jar file

If nothing happens, the program has been blocked by your security settings. On mac, go to System Preferences > Security & Privacy and click "open anyway"

When playing the archinovica, the most resonant and pure tuning of all intervals is used by default. To use alternative tunings, hold the right or left foot pedal or both on your keyboard during the attack of a chord; this will cause the algorithm to jump across 5-limit space, rather than moving to the closest possible tuning.

To reset the tuning of the instrument, press 'R'. For full-screen mode, press 'Z'.

---------Harmonic Design----------

If you have a specific harmonic plan you would like your piece to follow in 5-limit space, consider using the program "Harmonic Design.jar" also included in this repository.  Simply download and double click the .jar file to get started.

Using Harmonic Design is as easy as 1, 2, 3
1.  Design shapes in 5-limit space
2.  Arrange shapes in relation to each other
3.  Design chord progressions


Here are more details about each step:

1. The program opens in sandbox mode.  Here you can design shapes in 5-limit space. Navigate to the pitches you want in your shape using the arrow keys, and press the space bar to add the selected pitch to the shape.  You can create chord groups within your shape by holding shift while pressing the space bar on each pitch you wish to include in the chord, then release shift to create the chord. Press 'R' to reset sandbox mode and start over.
2. Once you are satisfied with your shape and its subgroups, press enter. Give your shape a name. Then press enter once more to switch to composition mode (alternatively, choose Sandbox > Hide Sandbox from the menu).  Now you can move your object where you want it using WASD.  Navigate within the shape using either the mouse or the arrow keys. Click and drag the whole screen to move your view.  Use + and - to zoom in and out. You can swith navigation modes between individual pitches 'P' and pitch groups (the chords you built in step 1) 'G'.  
3. Hold shift and press space over each note that you would like to include in a chord, then release shift to add the chord to your progression. Press 'R' to reset and start a new progression.  In order to produce the intended tuning, L between chords indicates the Left intonation pedal must be held, 'R' the right pedal, and 'LR' both.

You can switch the focus between various objects in composition mode using "Focus On" and selecting the name of the object.  You will only be able to navigate to objects that are in focus. Use Focus On > Composite to allow navigation to all objects.  You can change the transparency of the selected object using Composition > Set Alpha.  You can create highlighted groups of pitches using the Highlight menu.

To save your composition space, chose File > Save As.  This will allow you to access the space later.

