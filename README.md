# GitHub Code Snippet Search Engine

A Spring Boot 3.2 application for searching, storing, and analyzing code snippets from GitHub.

## Features

- Search GitHub code using the GitHub API with filtering by language, repository, and file extension
- Store search history and code snippets in MySQL database
- Full-text search on cached code snippets
- Resilience features including circuit breaker pattern and caching
- Comprehensive test coverage with JUnit 5 and Testcontainers
- API documentation with Swagger UI

## Architecture

The application follows a layered architecture:

- **Controller Layer**: Handles HTTP requests and responses
- **Service Layer**: Contains business logic and implements SOLID principles
- **Repository Layer**: Interacts with the database
- **Model Layer**: Domain entities
- **DTO Layer**: Data transfer objects for API interactions

## Technology Stack

- Spring Boot 3.2
- Spring Data JPA
- Spring WebFlux (for WebClient)
- MySQL Database
- Flyway for database migrations
- Resilience4j for circuit breaker pattern
- Caffeine for caching
- JUnit 5 and Testcontainers for testing
- Swagger UI for API documentation
- Docker and Docker Compose for containerization

## Getting Started

### Prerequisites

- Java 17+
- Maven
- Docker and Docker Compose (for running MySQL)

### Running the Application

1. Clone the repository
2. Start the MySQL database:

```bash
docker-compose up -d mysql
```

3. Run the application:

```bash
./mvnw spring-boot:run
```

4. Access the application at http://localhost:8080
5. Access the API documentation at http://localhost:8080/swagger-ui

### Building and Running with Docker

```bash
docker-compose up -d
```

## API Endpoints

- `POST /api/search` - Search for code on GitHub
- `GET /api/history` - Get paginated search history
- `GET /api/search/{id}` - Get detailed search results by ID
- `GET /api/snippets/search?q={query}` - Full-text search in code snippets

## Project Structure

```
com.codesearcher
├── config       # Configuration classes
├── controller   # REST controllers
├── dto          # Data Transfer Objects
├── model        # Domain entities
├── repository   # Data access layer
├── service      # Business logic
└── exception    # Exception handling
```

## Testing

Run tests with:

```bash
./mvnw test
```

Generate test coverage report:

```bash
./mvnw jacoco:report
```

The report will be available at `target/site/jacoco/index.html`

## Academic Deliverables

### Documentation

- ER Diagram: See `docs/er-diagram.png`
- Sequence Diagram: See `docs/sequence-diagram.png`
- SRS Template: See `docs/srs-template.md`

### Code Samples

- WebClient example: See `GithubApiServiceImpl.java`
- MySQL full-text search: See `CodeSnippetRepository.java`
- Testcontainers example: See `CodeSearchIntegrationTest.java`