# GitHub Proxy App


## Requirements

- Java 25
- Gradle

## Running the application

```bash
cd github-proxy
./gradlew bootRun
```

In Windows PowerShell:
```bash
cd github-proxy
Invoke-RestMethod http://localhost:8080/api/users/octocat/repositories
Invoke-RestMethod http://localhost:8080/api/users/nieistniejacy/repositories
```

## Running tests

```bash
cd github-proxy
./gradlew test
```

## Configuration

| Property | Default | Description |
|---|---|---|
| `github.api.base-url` | `https://api.github.com` | Base URL of the GitHub API |

## Tech Stack

- Java 25
- Spring Boot 4
- Gradle with Kotlin DSL
- WireMock for integration tests
