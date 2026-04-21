# disys-energy-communities

FHTW DISYS SS2026, intermediate hand-in (Group C, Luka Condric).

Two independent Maven projects:

- `rest-api/`: Spring Boot 3, Java 21, port 8080. Returns sample data at `/energy/current` and `/energy/historical`.
- `gui/`: JavaFX 21 client that calls the REST API.

## Run

REST API:

```
cd rest-api
./mvnw spring-boot:run
```

GUI (needs the REST API running):

```
cd gui
./mvnw javafx:run
```

## Endpoints

```
curl http://localhost:8080/energy/current
curl "http://localhost:8080/energy/historical?start=2026-04-19T00:00:00&end=2026-04-20T23:59:59"
```
