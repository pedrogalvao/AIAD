This is a War (board game) version for multi-agent system. The agents decide who to attack and who to make alliances with. The game ends when there's only one player with territories.

# System architecture

The map is a graph and each territory is a node.

Agents have the following behaviours:

	- Attacks randomly
	- Choose territories with highest difference in number of troops. Sometimes decide to not attack at all

# TODOs

# Feedback prof

- Fornecer argumentos de execução como parâmetro - Guilherme
  - Começar novo jogo (criar novo mapa após o jogo anterior acabar)
  - Passar os parâmetros
- Aumentar a complexidade da negociação 
- Escrever CSV com dados gerados pelas execuções



### Communication strategies

- Make alliances with the player that has most territories / troops
- Attribute a value in function of distance to player and so on



## Game modifications

- Territories starts with random number of troops [3, 10] for example.
- Troops gets different attack strength (a random multiplier [0,1] that multiplies the number of attacking and defending troops and then subtracts to get the resulting number of troops in that territory; the result is then rounded to the nearest integer)
