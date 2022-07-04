Kefan Cao

20898903 k33cao

kotlinc-jvm 1.6.10

macOS 12.1

**Notes**
- To help facilitate marking, I've added an option where you are able to 
spam bullets in order to beat all the aliens. You can find this option on 
line 20 in the `GameView.kt` file. Change `bulletRate` to 0 in order to remove 
the time constraint on the firing rate. Otherwise, it is defaulted to 30 which 
brings the firing rate down to 2 shots per second. It becomes quite difficult in 
level 3 if you do not enable this :). 


- When the ship gets hit by a enemy bullet. A new ship is spawn 
in the same spot and remains invincible for 5 seconds and is able to 
move around freely while everything else becomes frozen; however, as soon as the ship opens fire, the game starts up and gameplay resumes as normal. The freezing is dictated by 
the ship flashing. 

- Speed for the aliens is not very fast initially at early levels and DO get faster as you strike down aliens, but because drastically increasing the speed at early levels will make 
it practically impossible to beat the game, I've chosen to increase the speed by 0.04
per alien defeated. Hence, the fastest it'll go given there are 50 aliens is level speed + 2 which seems reasonable. Level speed increases by 2 every level with level 1 starting at 4. 

- Scoring is based on the level you're on. For each alien you defeat in level 1, 1 point is added to your score. For each alien you defeat in level 2, 2 points are added to your score. For each alien you defeat in level 3, 3 points are added to your score. 

- The default size for this game is 1600x1200, so the game MUST be instantiated on a screen size at least this big, otherwise you will witness the game screen being undersized! If you're on a laptop, I recommend closing the laptop screen and using primarily the monitor, otherwise it will spawn the game screen according to your laptop size which is unlikely to be 1600x1200! Thank you!