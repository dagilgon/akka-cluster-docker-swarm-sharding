# akka-cluster-docker-swarm-sharding

# Compile with this to build the docker image
sbt publishLocal

# Deploy docker stack with ...
sudo docker stack deploy --compose-file docker-compose.yml akka

# Monitor service logs with ...
sudo docker service logs akka_app-seed1

# Create actors on demand ...

http://0.0.0.0:8888/ramdom?from=yo&to=pepe&payload=hola%20majo


