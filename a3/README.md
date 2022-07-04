Kefan Cao
20898903 k33cao
kotlinc-jvm 1.6.10
macOS 12.1

**Notes**
- To help facilitate marking, I've added an option where you are able to 
spam bullets in order to beat all the aliens. You can find this option on 
line 20 in the `GameView.kt` file. Change `bulletRate` to 0 in order to remove 
the time constraint on the firing rate. Otherwise, it is defaulted to 30 which 
brings the firing rate down to 2 shots per second. 


- When the ship gets hit by a enemy bullet. A new ship is spawn 
in the same spot and remains invincible for 5 seconds and is able to 
move around freely while everything else becomes frozen; however, as soon as the ship opens fire, the game starts up and gameplay resumes as normal. The freezing is dictated by 
the ship flashing. 
