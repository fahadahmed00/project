# Software Requirements Specification (SRS)

## 1. Introduction

### 1.1 Purpose
This document specifies the requirements for the GitHub Code Snippet Search Engine application. It defines the functional and non-functional requirements, constraints, and system interfaces.

### 1.2 Scope
The application is a code search engine that allows users to search for code snippets on GitHub, track search history, and perform full-text search on cached code snippets.

### 1.3 Definitions, Acronyms, and Abbreviations
- **API**: Application Programming Interface
- **REST**: Representational State Transfer
- **JPA**: Java Persistence API
- **SRS**: Software Requirements Specification
- **SOLID**: Single responsibility, Open-closed, Liskov substitution, Interface segregation, Dependency inversion

## 2. Overall Description

### 2.1 Product Perspective
The system integrates with GitHub's code search API to retrieve code snippets, stores them locally for analysis, and provides a web interface for users to search and view results.

### 2.2 Product Functions
- Search for code on GitHub with various filters
- Track and display search history
- Cache code snippets for offline access
- Perform full-text search on cached snippets

### 2.3 User Classes and Characteristics
- **Anonymous Users**: Can search for code, view search history, and perform full-text search without creating an account

### 2.4 Operating Environment
- Java 17 or higher
- MySQL 8.0 or higher
- Web browser with JavaScript enabled

### 2.5 Design and Implementation Constraints
- Must use Spring Boot 3.2
- Must implement a layered architecture
- Must use Flyway for database migrations
- Must achieve 70%+ test coverage

### 2.6 User Documentation
- Swagger UI for API documentation
- README file with setup instructions

## 3. Specific Requirements

### 3.1 External Interface Requirements

#### 3.1.1 User Interfaces
- Search form with fields for query, language, repository, and file extension
- Results display with syntax highlighting for code snippets
- History view with pagination
- Full-text search interface for cached snippets

#### 3.1.2 Software Interfaces
- GitHub API: Integration with GitHub's code search API
- MySQL: Database for storing search history and code snippets

### 3.2 Functional Requirements

#### 3.2.1 Search Code
- Users shall be able to search for code on GitHub
- Users shall be able to filter by language, repository, and file extension
- System shall retrieve and display code snippets matching the search criteria

#### 3.2.2 Track Search History
- System shall track all searches performed
- System shall associate searches with anonymous session IDs
- Users shall be able to view their search history
- Users shall be able to re-run previous searches

#### 3.2.3 Cache Code Snippets
- System shall cache code snippets from search results
- System shall store code content, metadata, and source information

#### 3.2.4 Full-Text Search
- Users shall be able to search within cached code snippets
- System shall use MySQL's full-text search capabilities

### 3.3 Non-Functional Requirements

#### 3.3.1 Performance
- API requests shall have a circuit breaker to fail fast
- Cached results shall be returned in less than 500ms

#### 3.3.2 Security
- No authentication required for anonymous use
- Session tracking shall not collect personal information

#### 3.3.3 Reliability
- System shall handle GitHub API failures gracefully
- System shall provide meaningful error messages

#### 3.3.4 Maintainability
- Code shall follow SOLID principles
- System shall use a layered architecture
- Database schema shall be managed with migrations

#### 3.3.5 Testability
- System shall have 70%+ test coverage
- Integration tests shall use Testcontainers

## 4. Analysis Models

### 4.1 Data Model
See ER Diagram in project documentation

### 4.2 Interaction Model
See Sequence Diagram in project documentation

## 5. Appendices

### 5.1 Assumptions and Dependencies
- GitHub API rate limits may affect search capabilities
- MySQL full-text search has limitations for certain languages

### 5.2 Acronyms and Abbreviations
- See section 1.3