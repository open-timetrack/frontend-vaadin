#!/bin/zsh
docker run -d --mount type=bind,source=$(pwd)/db,target=/var/lib/postgresql/data -e POSTGRES_PASSWORD=root  postgres:15.3