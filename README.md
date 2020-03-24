![Java CI with Maven](https://github.com/ninthnails/acme-hr/workflows/Java%20CI%20with%20Maven/badge.svg?branch=master)

## Hierarchy API

REST API for managing the employees at ACME .


### Build, Run and Test
Project is using [Maven](https://maven.apache.org/download.html), follow standard [installation](https://maven.apache.org/install.html) instructions.

For a quick demo, run with `dev` profile activated.
```
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

To compile and/or run unit and integration tests:
```
mvn compile
mvn test
```

### API Usage
For local access, the endpoint is on HTTPS with a self-signed certificate.
Basic authentication is enabled.

#### POST /api/v1/hierarchy
Saves provided JSON structure as the effective company hierarchy. Usage example:
```
curl -XPOST -sku admin:admin -H 'Content-Type: application/json' \
"https://localhost:8443/api/v1/hierarchy"  \
-d '{"Alice":"Charlie","Bob":"Charlie","Charlie":"Daisy","Daisy":"Elody"}'
```

#### GET /api/v1/hierarchy
Provides the current company hierarchy in a tree-like structure. Usage example:
```
curl -sku admin:admin "https://localhost:8443/api/v1/hierarchy" 
```

#### GET /api/v1/hierarchy/employees/{name}/supervisors
Provides the supervisor and the supervisor’s supervisor of a given employee. Usage example:
```
curl -sku admin:admin "https://localhost:8443/api/v1/hierarchy/employees/Alice/supervisors"
```
