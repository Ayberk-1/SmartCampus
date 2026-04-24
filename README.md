# Coursework Report

**Author:** Ayberk Beden (w2134939)

**demo vide:** https://drive.google.com/file/d/14qPQkDlur86Lsm9VMyrmztRoIcTLZULL/view?usp=sharing
---

## Part 1: Service Architecture & Setup

### 1. JAX-RS Resource Lifecycle
JAX-RS resource classes are, by default, Request-Scoped, meaning a new instance of the resource class is created for each incoming HTTP request. This lifecycle aligns closely with the REST principle of statelessness, where each request is treated independently and no client-specific state is stored within the resource instance. As a result, instance variables are not shared between requests, which reduces the risk of thread interference at the object level.

However, this per-request model does not guarantee overall thread safety for the application. In practice, application data (such as collections of rooms or sensors) must persist across multiple requests. This introduces shared mutable state, typically managed through static fields or external service classes. Since these shared structures are accessed concurrently by multiple threads, they become a source of potential concurrency issues.

A key risk arises from non-atomic operations when using standard collections such as ArrayList or HashMap. For example, adding an element to an ArrayList involves multiple internal steps, and if two threads perform this operation simultaneously without proper synchronization, it may result in lost updates or inconsistent data. Such race conditions can compromise the integrity of the system.

To mitigate these issues, thread-safe mechanisms must be applied. This includes the use of concurrent data structures such as ConcurrentHashMap, as well as synchronization techniques (e.g., synchronized blocks or locks) to control access to shared resources.

A cleaner and more maintainable design is to encapsulate shared data within a thread-safe singleton service, rather than relying on static fields directly within resource classes. This approach separates concerns, centralizes state management, and allows concurrency control to be handled in a dedicated layer.

Although JAX-RS resources can be configured as singletons, doing so introduces additional risks by placing shared mutable state directly within the resource itself, making thread safety more difficult to manage. For this reason, the default Request-Scoped lifecycle is generally preferred, with shared state handled explicitly and safely in well-designed service components.

### 2. HATEOAS (Hypermedia)
HATEOAS (Hypermedia as the Engine of Application State) is a key constraint of REST architecture in which API responses include hypermedia links that guide clients on how to interact with related resources. Instead of relying on hardcoded endpoints, clients dynamically discover available actions through links embedded within each response.

This approach directly supports the REST principle of loose coupling between client and server. The client does not need prior knowledge of the API structure beyond the initial entry point, as all possible actions (e.g., retrieving related resources, updating data, or deleting entities) are communicated through hypermedia controls.

For example, a response representing a room resource may include links to:
* Retrieve all sensors within that room
* Add a new sensor
* Delete or update the room

By following these links, the client can navigate the API dynamically, similar to how users navigate web pages via hyperlinks.

The primary benefit of HATEOAS for client developers is that it reduces dependency on external documentation. Instead of manually constructing URLs, clients can rely on the API to provide valid and up-to-date navigation paths. This makes the system more resilient to change, as modifications to endpoint structures do not necessarily break existing clients, provided the hypermedia links are maintained.

Furthermore, HATEOAS improves discoverability and evolvability. New features or resources can be introduced by simply adding new links to responses, allowing clients to adopt new functionality without requiring immediate updates.

In contrast, APIs that do not implement HATEOAS require clients to hardcode endpoint structures, leading to tighter coupling and increased maintenance effort when the API evolves.

Overall, HATEOAS enhances flexibility, reduces client complexity, and promotes a more robust and scalable API design, aligning closely with the core principles of RESTful architecture.

---

## Part 2: Room Management

### 1. Returning IDs vs Full Objects
When designing API responses for a collection of resources such as rooms, there is an important trade-off between returning only resource identifiers and returning full object representations.

Returning only IDs results in a significantly smaller payload, which reduces network bandwidth usage and improves response times, particularly when dealing with large datasets. This approach also aligns well with the principle of minimising over-fetching, as clients can request additional details only when necessary. However, it introduces additional round trips, since the client must make further requests to retrieve full resource details.

In contrast, returning full objects provides a richer and more convenient response, allowing clients to access all relevant data in a single request. This reduces the number of API calls required and simplifies client-side logic. However, it increases payload size and processing overhead, which may negatively impact performance and scalability, especially in high-load systems.

A balanced approach is often preferred in practice. For example, APIs may return partial representations (summarised objects) along with hypermedia links (HATEOAS) to detailed resources. This allows clients to remain efficient while still having access to full data when required.

Therefore, the choice depends on the use case: lightweight responses are preferable for large collections, while full representations are more suitable when detailed information is immediately needed.

### 2. Idempotency of DELETE
The HTTP DELETE method is defined as idempotent, meaning that multiple identical requests should result in the same final state of the system.

In this implementation, when a client sends a DELETE request for a specific room:
1. The first request successfully removes the room from the system.
2. Any subsequent DELETE requests for the same room will not alter the system state further, as the resource no longer exists.

Although the response may differ (for example, returning 404 Not Found after the resource has already been deleted), the key property of idempotency is preserved because the state of the system remains unchanged after the initial deletion.

It is also important to consider business constraints. In this system, a room cannot be deleted if it still has sensors assigned to it. In such cases, the API returns an error (e.g., 409 Conflict), and no deletion occurs. Repeating the same DELETE request will consistently result in the same outcome, further reinforcing idempotent behaviour.

Therefore, despite variations in response codes, the DELETE operation remains idempotent because repeated requests do not produce additional side effects beyond the initial state change.

---

## Part 3: Sensor Operations & Linking

### 1. @Consumes Annotation and Media Type Mismatch
The @Consumes(MediaType.APPLICATION_JSON) annotation explicitly defines that the endpoint only accepts requests with a Content-Type of application/json. This establishes a strict contract between the client and the server regarding the expected data format.

If a client sends a request with an unsupported media type, such as text/plain or application/xml, the JAX-RS runtime performs content negotiation and determines that no suitable message body reader is available to process the request. As a result, the request is automatically rejected, and the server responds with an HTTP 415 Unsupported Media Type status.

This behaviour is important for maintaining data integrity and consistency, as it prevents the server from attempting to process data in an unexpected or incompatible format. It also enforces clear API boundaries, ensuring that clients adhere to the defined contract.

From a design perspective, this mechanism simplifies server-side logic, as developers do not need to manually validate content types within each method. Instead, JAX-RS handles this validation at the framework level, contributing to cleaner and more maintainable code.

### 2. Query Parameters vs Path Parameters for Filtering
When designing RESTful APIs, query parameters are generally preferred for filtering and searching operations on collections, while path parameters are used to uniquely identify specific resources.

Using a query parameter such as:
`/api/v1/sensors?type=CO2`
clearly indicates that the client is requesting a filtered view of the sensors collection. Query parameters are inherently optional and can be combined, allowing flexible queries such as:
`/api/v1/sensors?type=CO2&status=ACTIVE`

This approach supports extensibility and aligns with REST principles by treating filters as modifiers of a resource representation, rather than as part of the resource identity itself.

In contrast, embedding the filter in the path:
`/api/v1/sensors/type/CO2`
implies a hierarchical relationship and suggests that "CO2 sensors" are a distinct sub-resource. This can lead to rigid API structures and makes it difficult to support multiple or optional filtering criteria without significantly complicating the URI design.

Furthermore, query parameters improve readability and are widely supported by caching mechanisms and web standards for representing search queries.

Therefore, query parameters are considered superior for filtering because they provide flexibility, scalability, and clearer semantic meaning, whereas path parameters are better suited for identifying specific, uniquely addressable resources (e.g., /sensors/{id}).

---

## Part 4: Sub-Resources

### 1. Sub-Resource Locator Pattern
The Sub-Resource Locator pattern is used in JAX-RS to delegate the handling of nested resource paths to separate classes. Instead of defining all endpoints within a single resource class, a parent resource exposes a method that returns another resource responsible for handling a specific sub-path.

For example, a SensorResource may define a method for the path /sensors/{sensorId}/readings, which returns an instance of SensorReadingResource. This delegates all logic related to sensor readings to a dedicated class.

This pattern provides several architectural benefits:
* **Separation of Concerns:** Each resource class is responsible for a specific domain (e.g., sensors vs readings), improving clarity and maintainability.
* **Improved Modularity:** Logic is decomposed into smaller, focused components that can be developed and tested independently.
* **Scalability of Codebase:** As the API grows, new sub-resources can be added without increasing the complexity of existing classes.
* **Avoidance of “God Classes”:** Without this pattern, a single resource class would become excessively large and difficult to manage, containing multiple levels of routing and business logic.

In contrast, defining all nested endpoints (e.g., /sensors/{id}/readings/{rid}) within one class leads to tightly coupled and less maintainable code. The Sub-Resource Locator pattern promotes a cleaner and more extensible architecture, which is essential for large-scale RESTful systems.

### 2. Historical Data Management and Side Effects
In this system, each sensor maintains a historical log of readings, while also exposing a currentValue field representing the most recent measurement. When a new reading is submitted via a POST request, two operations occur:
1. The reading is added to the sensor’s historical collection.
2. The parent sensor’s currentValue is updated to reflect the latest value.

This introduces a controlled side effect, where an operation on a sub-resource (sensor readings) modifies the state of its parent resource (sensor).

From a design perspective, this is both necessary and beneficial. It ensures data consistency across the API, as clients can retrieve the most recent sensor value directly without needing to process the full history. This reduces client-side computation and improves performance.

However, this also introduces potential concurrency challenges. If multiple readings are submitted simultaneously, there is a risk that updates to currentValue may become inconsistent if not handled correctly. Therefore, appropriate synchronization or thread-safe data structures must be used to ensure that both the historical data and the derived currentValue remain accurate.

Additionally, this design reflects a common real-world pattern where derived state (currentValue) is maintained alongside raw data (historical readings) for efficiency. By updating the parent resource as part of the POST operation, the system avoids repeated computation and ensures that all endpoints provide consistent and up-to-date information.

---

## Part 5: Error Handling & Logging

### 1. Resource Conflict (409 Conflict)
When attempting to delete a room that still contains assigned sensors, the request violates a business rule: removing the room would leave orphaned sensor resources. In this case, throwing a custom RoomNotEmptyException and mapping it to HTTP 409 Conflict is appropriate.

The 409 status code indicates that the request could not be completed due to a conflict with the current state of the resource. Here, the room exists, but its state (having active sensors) prevents deletion.

This approach provides clear semantic meaning to the client and ensures that business constraints are explicitly enforced at the API level.

### 2. Dependency Validation (422 Unprocessable Entity)
When a client attempts to create a sensor with a roomId that does not exist, the request is syntactically valid but semantically incorrect. The JSON structure is well-formed, but it contains an invalid reference.

In this scenario, returning HTTP 422 Unprocessable Entity is more precise than 404 Not Found. A 404 response would imply that the requested endpoint or resource itself does not exist, which is misleading. Instead, 422 indicates that the server understands the request but cannot process it due to invalid data.

This distinction improves API clarity and helps clients diagnose errors more effectively, as it clearly communicates that the issue lies within the request payload rather than the endpoint.

### 4. Global Exception Handling (500 Internal Server Error)
A global ExceptionMapper<Throwable> acts as a safety net for any unhandled runtime exceptions, such as NullPointerException or IndexOutOfBoundsException. These errors indicate unexpected failures within the server.

Returning a generic HTTP 500 Internal Server Error ensures that clients receive a consistent and controlled response, rather than a raw stack trace or server-generated error page.

From a cybersecurity perspective, exposing internal stack traces poses significant risks. Stack traces can reveal:
* Internal class names and package structures
* Frameworks and library versions
* Application logic and execution flow

Attackers can use this information to identify vulnerabilities and craft targeted exploits, such as injection attacks or framework-specific attacks.

Therefore, best practice is to return a sanitised, generic error response to the client, while logging detailed diagnostic information internally for debugging purposes.

### 5. Logging with JAX-RS Filters
Implementing logging using ContainerRequestFilter and ContainerResponseFilter provides a clean and scalable solution for handling cross-cutting concerns.

By intercepting all incoming requests and outgoing responses, filters allow centralised logging of:
* HTTP method and request URI
* Response status codes

This approach offers several advantages:
* **Separation of Concerns:** Logging logic is kept separate from business logic, resulting in cleaner resource classes.
* **Consistency:** All requests and responses are logged uniformly, reducing the risk of missing logs.
* **Maintainability:** Changes to logging behaviour can be made in one place rather than across multiple resource methods.
* **Scalability:** As the API grows, logging automatically applies to new endpoints without additional code.

In contrast, manually adding logging statements within each resource method leads to code duplication, increased complexity, and a higher risk of inconsistencies.
