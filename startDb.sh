#!/bin/zsh
docker run -d --mount type=bind,source=$(pwd)/db,target=/var/lib/postgresql/data -e POSTGRES_PASSWORD=root --restart always -p 50432:5432 postgres:15.5