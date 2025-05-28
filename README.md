# Branch Coding Exercise
This Java 21 project is a simple Spring Boot application consisting of a `RestController`, a service what wraps calls to the GitHub API with `@Cacheable`, and another service that orchestrates and maps. 

## Getting Started/Running Locally

```bash
git clone git@github.com:andsamp/branch-assignment.git
```
```bash
cd branch-assignment
```
```bash
./gradlew bootRun
```

The applications should now be running on port `8080`. You can test by navigating to http://localhost:8080/users/octocat.

## Endpoints
| Method | Path | Description                                |
|--------|------|--------------------------------------------|
| `GET` | `/users/{username}` | Get specified GitHub User and Repositories |
