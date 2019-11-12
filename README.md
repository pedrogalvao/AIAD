This is a War (board game) version for multi-agent system. The agents decide who to attack and who to make alliances with. The game ends when there's only one player with territories.

# System architecture

The map is a graph and each territory is a node.

Agents have the following behaviours:

	- Attacks randomly

# TODOs

## Make sure a game ends
- ~~Print the initial state of the map~~ 
- ~~Each X seconds, print the state of the map~~
- Print attacks to make sure they are working properly. Info before and after the attack
  - Agents still can attack itself ???
  - Somehow agent still has territory ownership even if his list of territories is empty
- If game not ending with those strategies, implement better strategy for agent



## Implement more complex strategies

### Attack strategies

- Agents gets the difference of troops in his territory vs frontier territories and attacks the one with highest difference
- Agents attribute parameters to the previous value in order to attack the frontiers or not 



### Communication strategies

- Make alliances with the player that has most territories / troops
- Attribute a value in function of distance to player and so on



## Game modifications

- Territories starts with random number of troops [3, 10] for example.
- Troops gets different attack strength (a random multiplier [0,1] that multiplies the number of attacking and defending troops and then subtracts to get the resulting number of troops in that territory; the result is then rounded to the nearest integer)