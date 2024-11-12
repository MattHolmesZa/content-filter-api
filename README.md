# Content Filter API

This project is a content filter API designed to replace restricted words within input text with asterisks. It includes services for managing restricted words and sanitizing input text based on those words. The application is built using Kotlin, Spring Boot, and utilizes a repository for persistent storage of sensitive words.

## Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Building the Project](#building-the-project)
  - [Running the Application](#running-the-application)
  - [Running Tests](#running-tests)
- [Usage](#usage)
  - [RestrictedWordService](#restrictedwordservice)
  - [ContentFilterService](#contentfilterservice)
- [Trade-offs and Assumptions](#trade-offs-and-assumptions)
- [Scaling Considerations](#scaling-considerations)
- [Security Enhancements for Production](#security-enhancements-for-production)

## Overview

The Content Filter API provides functionalities to:
- Add, update, delete, and retrieve restricted words.
- Sanitize input text by replacing occurrences of restricted words with asterisks.

This project is intended to help applications filter out inappropriate or restricted content before displaying it to users or storing it.

## Features

- **Restricted Word Management**: CRUD operations for restricted words.
- **Word Sanitization**: Replaces restricted words in input text with asterisks, handling case insensitivity and special characters.
- **Caching**: Utilizes caching for efficient retrieval of restricted words.
- **Unit Tests**: Comprehensive unit tests for all services.

## Getting Started

### Prerequisites

- JDK 11 or higher
- Gradle
- Git (for cloning the repository)
- An IDE like IntelliJ IDEA or Eclipse (optional but recommended)

### Building the Project

Clone the repository:

```bash
git clone https://github.com/MattHolmesZa/content-filter-api.git
cd content-filter-api
```

Build the project using Gradle:

```bash
./gradlew build
```

### Running the Application

You can run the application using the Spring Boot plugin:

```bash
./gradlew bootRun
```

### Running Tests

Execute the test suite using:

```bash
./gradlew test
```

## Usage

### RestrictedWordService

This service manages the restricted words stored in the repository.

- **Get Words**: Retrieve all restricted words.
- **Add Word**: Add a new restricted word.
- **Update Word**: Update an existing restricted word.
- **Delete Word**: Delete a restricted word.

Example:

```kotlin
val restrictedWordService = RestrictedWordService(restrictedWordRepository)

// Add a new word
restrictedWordService.addWord("inappropriate")

// Get all words
val words = restrictedWordService.getWords()

// Update a word
restrictedWordService.updateWord("inappropriate", "offensive")

// Delete a word
restrictedWordService.deleteWord("offensive")
```

### ContentFilterService

This service sanitizes input text by replacing restricted words with asterisks.

Example:

```kotlin
val contentFilterService = ContentFilterService(restrictedWordService)

val inputText = "This is some inappropriate content."
val sanitizedText = contentFilterService.sanitizeWords(inputText)

// Output: "This is some ************* content."
```

## Trade-offs and Assumptions

### Trade-offs

- **Case Sensitivity Handling**: To simplify comparisons and ensure consistent behavior, all restricted words are stored in lowercase. Input text is matched against the restricted words in a case-insensitive manner using regex with the `IGNORE_CASE` option.
- **Performance vs. Complexity**: Sorting the restricted words by length in descending order before pattern compilation ensures that longer words are matched first, preventing partial matches. This adds a slight overhead during sanitization but improves accuracy.
- **Data Normalization**: The decision to normalize words to lowercase throughout the application reduces complexity but assumes that the input text does not require case preservation for the restricted words.

### Assumptions

- **Input Text Encoding**: It is assumed that the input text is UTF-8 encoded, allowing for proper handling of Unicode characters.
- **Consistent Data Storage**: All restricted words are stored in lowercase, and any new words added are converted to lowercase before storage.
- **Single Language Support**: The application assumes that the restricted words and input text are in the same language and does not account for language-specific nuances or multi-language support.

## Scaling Considerations

At scale, certain aspects of the application may need to be reconsidered to maintain performance and efficiency.

### Externalizing Caching

- **Current Approach**: The application uses in-memory caching for restricted words.
- **Limitation**: In-memory caching is limited to a single instance and does not scale horizontally.
- **Recommendation**: Use Distributed Caching: Implement external caching solutions like Redis, Memcached, or Hazelcast to store restricted words in a distributed cache.
    - **Benefits**:
        - **Scalability**: Supports horizontal scaling across multiple instances.
        - **Consistency**: Ensures all instances have consistent access to the latest restricted words.
        - **Performance**: Reduces database load by caching frequently accessed data.

### Efficient Text Sanitization

- **Current Approach**: Uses regex pattern matching to find and replace restricted words.
- **Limitation**:
    - **Performance Overhead**: As the list of restricted words grows, compiling large regex patterns becomes inefficient.
    - **Memory Consumption**: Large patterns can consume significant memory.
- **Recommendations**:
    1. **Use a Trie (Prefix Tree) Data Structure**
        - **Description**: A trie allows for efficient storage and retrieval of strings, enabling quick matching of multiple patterns.
        - **Benefits**:
            - **Efficiency**: Faster lookup times for large sets of words.
            - **Partial Matching**: Handles overlapping and nested words effectively.
        - **Implementation**: Implement the Aho-Corasick algorithm, which uses a trie for efficient multi-pattern searching in texts.
        - **Libraries**: Utilize existing libraries like `ahocorasick` for Java/Kotlin.
    2. **Streaming Text Processing**
        - **Description**: For very large texts, process the text in streams rather than loading it entirely into memory.
        - **Benefits**: Reduces memory usage by processing chunks of text.
        - **Implementation**: Use `InputStream` and `Reader` classes to read and process text incrementally.
    3. **Parallel Processing**
        - **Description**: Leverage multi-threading or reactive programming to process multiple parts of the text in parallel.
        - **Benefits**: Improves throughput by utilizing multiple CPU cores.
        - **Considerations**: Ensure that shared data structures are thread-safe.

### Asynchronous Processing

- **Description**: Use message queues or event-driven architectures to process text asynchronously.
- **Benefits**: Separates the sanitization process from user requests and processes messages at scale using consumers.
- **Implementation**: Use systems like RabbitMQ, Kafka, or AWS SQS.

### Microservices Architecture

- **Description**: Break down the application into smaller, independent services.
- **Benefits**: Scale individual services based on demand and allows for technology heterogeneity and independent deployments.
- **Considerations**: Introduces latency due to inter-service communication and requires robust service orchestration and monitoring.

## Security Enhancements for Production

Before taking this application to production, consider implementing the following security enhancements:

### Input Validation and Sanitization

- Ensure that all inputs (especially those from users) are properly validated and sanitized to prevent injection attacks or malicious input.
- Escape special characters in restricted words and handle any edge cases.

### Database Security

- **Prepared Statements**: Use prepared statements or parameterized queries to prevent SQL injection.
- **Encryption**: If restricted words are confidential, consider encrypting them in the database.
- **Rate Limiting**: Implement rate limiting on APIs that add or update restricted words to prevent abuse or denial-of-service attacks.

### Authentication and Authorization

- **Authentication**: Secure the APIs with authentication mechanisms (e.g., OAuth2, JWT).
- **Authorization**: Implement role-based access control to restrict restricted word management functionalities to authorized users only.

### Logging and Monitoring

- **Audit Logs**: Maintain audit logs for all CRUD operations on restricted words.
- **Monitoring**: Set up monitoring and alerting for unusual activities or errors.

### Exception Handling

- **Generic Error Messages**: Avoid exposing internal error details to clients. Provide generic error messages and log the details internally.
- **Graceful Degradation**: Ensure the application can handle failures gracefully without exposing sensitive information.

### Secure Configuration Management

- **Environment Variables**: Store sensitive configurations (like database credentials) securely, using environment variables or a secrets manager.
- **Configuration Profiles**: Use separate configuration profiles for development, testing, and production environments.

### HTTPS Enforcement

- **SSL/TLS**: Ensure all communication with the application is over HTTPS to protect data in transit.
- **Certificate Management**: Properly manage SSL certificates and renew them before expiration.

### Testing

- **Security Testing**: Perform security testing, including penetration testing and code reviews.
- **Automated Tests**: Implement automated security tests as part of the CI/CD pipeline.

### Internationalization and Localization

- **Language Support**: If the application will be used in multiple languages, ensure that restricted words are managed and matched appropriately for each language.

### Externalizing Configuration and Secrets

- **Secret Management**: Use secret management tools like Vault or AWS Secrets Manager to handle sensitive configuration data.