# Exercise1

## Brief

- Developed & tested with JDK 12.
- `Guice` for light and easy IoC & DI.
- `Spark` for light and easy REST.
- `SLF4j` as `Spark` expects SLF4j implementation provided.
- `JUnit` and `Mockito` for unit & integration tests.

## Running

### With maven
1. Clone repository
2. `cd Exercise1`
3. `mvn compile`
4. `mvn exec:java`

### With java
1. Clone repository
2. `cd Exercise1`
3. `mvn package`
4. `java -jar target\exercise1-1.0.0-jar-with-dependencies.jar`

## Running tests

3. `mvn test`

## REST API

- GET http://localhost:8000/transfer
- GET http://localhost:8000/transfer/{ID}
- POST http://localhost:8000/transfer
- PUT http://localhost:8000/transfer/{ID}
- PATCH http://localhost:8000/transfer/{ID}
- DELETE http://localhost:8000/transfer/{ID}
- OPTIONS http://localhost:8000/transfer
- HEAD http://localhost:8000/transfer

### Transfer Order entity layout:
    {
      "$schema": "http://json-schema.org/draft-04/schema#",
      "type": "object",
      "properties": {
        "id": {
          "type": "integer"
        },
        "sourceAccount": {
          "type": "string"
        },
        "targetAccount": {
          "type": "string"
        },
        "amount": {
          "type": "number"
        },
        "status": {
          "type": "string"
        }
      },
      "required": []
    }

Example:

    {
      "id": 0,
      "sourceAccount": "12345",
      "targetAccount": "09876",
      "amount": 123.45,
      "status": "PLANNED"
    }

There's also a Postman examples file (you can import it in Postman) provided in the `postman` directory.

