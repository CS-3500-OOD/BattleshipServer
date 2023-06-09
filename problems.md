

### 1. fix stdin issue where printing too much stuff 
#### Status: :x: 

idea: Use JavaFX to make a quick application with a text+console thing that will work with the InputListener

problem: Can't be headless, prevents us from running on the DO server...

solution?: Make a separate application that connects to the input listener? Won't work because the server will have to connect to the input listener application and I would have to hardcode the ip for that...

unsure how to solve this one right now

### 2. winners on whitelist should also be kicked
#### Status: :x:

should be ~~simple~~ fix, just change in the GamesManager

### 3. whitelist turns off after everyone goes
#### Status: :x:

should be ~~simple~~ fix, just change in the GamesManager

### 4. players can join as an empty string name (bypasses whitelist)
#### Status: :x:
 - players can sql inject :(

Add in player name sanitization. Add a method within the ClientSignupAttempt.
Do this while also adding the ip whitelist support


### 5. only let one client from one ip at a time...
#### Status: :x:

Think we need to save `name:ip` pair in whitelist
Make the ip component optional in whitelist. If the ip is not present, then accept the name
on any connection. When the game is over, save the winners as `name:ip` form.


### 6. Add a time stamp to the observer json data
#### Status: :x:

Make sure the timestamp works with mongodb. Might have a specific setup