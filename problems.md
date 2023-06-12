

### 1. fix stdin issue where printing too much stuff 
#### Status: :white_check_mark:

IDEA: reroute all loggers to log files

Update: I was able to separate out logging into files. ~~I just need to find a program that will view
the files remotely and update as they are logged to.~~

Update 2: Will use `tail -f logs/server.log` AFTER the server is running to get the input

### 2. winners on whitelist should also be kicked
#### Status: :white_check_mark:

should be ~~simple~~ fix, just change in the GamesManager

Update: Alex tested and seemed like a non-issue

### 3. whitelist turns off after everyone goes
#### Status: :white_check_mark:

should be ~~simple~~ fix, just change in the GamesManager

Update: Could not find issue

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
