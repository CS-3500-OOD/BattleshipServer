

### 1. fix stdin issue where printing too much stuff 
#### Status: :x:

IDEA: reroute all loggers to log files

### 2. winners on whitelist should also be kicked
#### Status: :x:

should be ~~simple~~ fix, just change in the GamesManager

### 3. whitelist turns off after everyone goes
#### Status: :x:

should be ~~simple~~ fix, just change in the GamesManager

### 4. players can join as an empty string name (bypasses whitelist)
#### Status: :white_check_mark:
 - players can sql inject :(

Add in player name sanitization. Add a method within the ClientSignupAttempt.


### 5. only let one client from one ip at a time...
#### Status: :x:

Think we need to save `name:ip` pair in whitelist
Make the ip component optional in whitelist. If the ip is not present, then accept the name
on any connection. When the game is over, save the winners as `name:ip` form.


### 6. Add a time stamp to the observer json data
#### Status: :x:

Make sure the timestamp works with mongodb. Might have a specific setup