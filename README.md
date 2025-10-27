# SHORTENER BACKEND

## About

A simple Shortener Backend for a Url Shortener.

This project is based upon:

- Spring Boot
- Redis
- Testcontainers

This project has dependencies to two other services:

- [Shortener Urlservice](https://github.com/soserdev/shortener-urlservice)
- [Shortener Keygenerator](https://github.com/soserdev/shortener-keygenerator)

## Test the API

Shorten url:

```bash
curl -v -H'Content-Type: application/json' -d'{"url": "https://www.manning.com/books/spring-in-action-sixth-edition"}' http://localhost:8080/shorturl
```

Get short url - _no redirect_:

```bash
curl -v http://localhost:8080/shorturl/1fa
```

Redirect to url:

```bash
curl -v http://localhost:8080/1fa
```


## Docker Compose

Generate Docker Image for the backend if needed:

```bash
docker build  -t soserdev/shortener-backend:latest -t soserdev/shortener-backend:0.0.1 -f Dockerfile .
```

Start the containers for the keygenerator and urlservice:

```bash
docker compose -f compose.yaml up
```

Start the containers for the backend, the keygenerator, and the urlservice:

```bash
docker compose -f docker-compose.yaml up
```

Stop the containers:

```bash
docker compose stop
```

Stop and remove the containers:

```bash
docker compose down
```

## Dev with Docker Compose

Create short url:

```bash
curl -v -H'Content-Type: application/json' -d'{"url": "https://www.manning.com/books"}' http://localhost:8080/shorturl
```

I'd like to check if url is saved in Mongo.

Get containers:

```bash
docker ps -a
```

Result like:

```bash
CONTAINER ID   IMAGE                                    COMMAND                  CREATED          STATUS          PORTS                      NAMES
0e220b5eae1d   soserdev/shortener-urlservice:latest     "java -noverify -XX:…"   55 seconds ago   Up 52 seconds   0.0.0.0:8082->8080/tcp     shortener-backend-urlservice-1
c7ca37c87617   mongo:7.0.11                             "docker-entrypoint.s…"   55 seconds ago   Up 53 seconds   0.0.0.0:27017->27017/tcp   shortener-backend-mongo-1
a22a2143d2f1   soserdev/shortener-keygenerator:latest   "java -jar app.jar"      55 seconds ago   Up 53 seconds   0.0.0.0:8081->8080/tcp     shortener-backend-keygenerator-1
97a2e074cac8   redis:8.2.2-alpine3.22                   "docker-entrypoint.s…"   55 seconds ago   Up 54 seconds   0.0.0.0:6379->6379/tcp     shortener-backend-redis-1
```

Login to pod `mongo:7.0.11`:
```bash
docker exec -it c7ca37c87617 -- sh
```

Login to mongo:

```bash
mongosh -u root -p rootpw
```

Get databases:

```bash
show dbs
```

Use db `urlservice`:

```bash
use urlservice
```

Show the collections:

```bash
show collections
```

Find all urls:

```bash
db.urls.find()
```

Find last three inserted urls:

```bash
db.urls.find().sort({ created: -1 }).limit(3)
```

Find a short url `1fa`:

```bash
urlservice> db.urls.find({ shortUrl: "1fa" })
```

Result:

```bash
urlservice> db.urls.find({ shortUrl: "1fa" })
[
  {
    _id: ObjectId('68f8972acaba2b2af94d00cd'),
    shortUrl: '1fa',
    longUrl: 'https://www.manning.com/books/spring-in-action-sixth-edition',
    userid: 'guest',
    created: ISODate('2025-10-22T08:34:50.070Z'),
    updated: ISODate('2025-10-22T08:34:50.070Z'),
    _class: 'dev.smo.shortener.urlservice.model.UrlData'
  }
]
```