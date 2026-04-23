# SHORTENER BACKEND

## About

A simple Shortener Backend for a Url Shortener.

* Spring Boot
* Redis
* MongoDB
* Docker & Docker Compose
* Testcontainers
* GitHub Actions

### External Dependencies

This service depends on:

- [Shortener Urlservice](https://github.com/soserdev/shortener-urlservice)
- [Shortener Keygenerator](https://github.com/soserdev/shortener-keygenerator)

## Run the Application  🚀

## Docker Compose (Recommended)

Start only dependencies:

```bash
docker compose -f compose.yaml up
```

Start full stack (backend + dependencies):

```bash
docker compose -f docker-compose.yaml up
```

Stop containers:

```bash
docker compose stop
```

Remove containers:

```bash
docker compose down
```


### Build Docker Image (Backend)

```bash
docker build -t soserdev/shortener-backend:0.1.4 -f Dockerfile .
```


## Test the API (Quick Start)  🧪

### Create short URL

```bash
curl -v -H'Content-Type: application/json' \
-d'{"url": "https://www.manning.com/books/spring-in-action-sixth-edition"}' \
http://localhost:8080/shorturl
```

### Get short URL (no redirect)

```bash
curl -v http://localhost:8080/shorturl/1fa
```

### Redirect

```bash
curl -v http://localhost:8080/1fa
```

### Find all URLs

```bash
curl -s http://localhost:8080/shorturl | jq
{
  "content": [
    {
      "id": "69ea3e17b7ce664e554b5eb1",
      "url": "https://www.manning.com/books",
      "shortUrl": "1fa",
      "user": "default",
      "status": "active",
      "created": "2026-04-23T15:43:19.977",
      "updated": "2026-04-23T15:43:19.977"
    }
  ],
  "number": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true,
  "numberOfElements": 1,
  "empty": false
}
```

## 📚 API Documentation

Here’s a clean **API overview table** you can drop into your README. It summarizes everything from your controller in a compact, scannable way.

### 📊 API Overview

| Method | Endpoint               | Auth     | Description                 | Request                             | Response                |
| ------ | ---------------------- | -------- | --------------------------- | ----------------------------------- | ----------------------- |
| POST   | `/shorturl`            | Optional | Create a new short URL      | `{ "url": "https://..." }`          | `ResponseUrl`           |
| PUT    | `/shorturl/id/{id}`    | Required | Update URL status           | `{ "url": "...", "status": "..." }` | `ResponseUrl`           |
| GET    | `/shorturl`            | Required | Get paginated URLs for user | `page, size, sortBy, direction`     | `Page<ResponseUrl>`     |
| GET    | `/shorturl/{shortUrl}` | No       | Get URL metadata            | path variable                       | `ResponseUrl`           |
| GET    | `/{shortUrl}`          | No       | Redirect to long URL        | path variable                       | `302 Location redirect` |


### 🧠 Notes

* Pagination is **user-scoped** (`Authentication.getName()`)
* Default paging:

    * `page = 0`
    * `size = 10`
    * `sortBy = created`
    * `direction = desc`
* Redirect endpoint uses:

    * cache first (Redis)
    * fallback to URL service
* Blacklisted URLs are rejected during creation
* `shortUrl` format: alphanumeric, 3–6 chars

## 🧰 Dev with Docker Compose

Create a short URL:

```bash
curl -v -H'Content-Type: application/json' \
-d'{"url": "https://www.manning.com/books"}' \
http://localhost:8080/shorturl
```


### Check MongoDB data

List containers:

```bash
docker ps -a
```

Example output:

```bash
CONTAINER ID   IMAGE
0e220b5eae1d   shortener-urlservice
c7ca37c87617   mongo:7.0.11
a22a2143d2f1   shortener-keygenerator
97a2e074cac8   redis
```


## Enter Mongo container

```bash
docker exec -it c7ca37c87617 sh
```


## Connect to Mongo

```bash
mongosh -u root -p rootpw
```


## MongoDB commands

```bash
show dbs
use urlservice
show collections
```

Find all URLs:

```bash
db.urls.find()
```

Latest URLs:

```bash
db.urls.find().sort({ created: -1 }).limit(3)
```

Find specific short URL:

```bash
db.urls.find({ shortUrl: "1fa" })
```


## Example document

```json
{
  "_id": "68f8972acaba2b2af94d00cd",
  "shortUrl": "1fa",
  "longUrl": "https://www.manning.com/books/spring-in-action-sixth-edition",
  "user": "default",
  "created": "2025-10-22T08:34:50.070Z",
  "updated": "2025-10-22T08:34:50.070Z",
  "_class": "dev.smo.shortener.urlservice.model.UrlData"
}
```


## ⚙️ Architecture Notes

* Redis is used for caching short URL lookups
* MongoDB stores persistent URL mappings
* Keygenerator service generates unique short keys
* Urlservice handles paging, sorting, and filtering


## 🧠 Key Features

* URL validation before creation
* Blacklist protection (e.g. localhost blocking)
* Cached redirects for performance
* Per-user URL ownership
* Pagination support for large datasets
* JWT or forwarded-user authentication support


