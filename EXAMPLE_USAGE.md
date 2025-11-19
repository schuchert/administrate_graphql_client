# Example Usage of Administrate GraphQL Client

This document provides Spring Boot test examples of how to use the generated GraphQL client library.

## Prerequisites

1. Add the dependency to your project:
```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>administrate-graphql-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

2. Add Spring Boot Test dependencies:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

3. Configure the GraphQL endpoint in your `src/test/resources/application.properties` or `src/test/resources/application.yml`:
```properties
# The GraphQL endpoint URL
graphql.client.url=http://your-graphql-server.com/graphql
```

## Example 1: Query All Course Templates (Spring Boot Test)

```java
package com.example.test;

import com.fsi.tm2poc.graphql.client.CourseTemplate;
import com.fsi.tm2poc.graphql.client.CourseTemplateConnection;
import com.fsi.tm2poc.graphql.client.CourseTemplateEdge;
import com.fsi.tm2poc.graphql.client.Query;
import com.fsi.tm2poc.graphql.client.util.QueryExecutor;
import com.graphql_java_generator.exception.GraphQLRequestExecutionException;
import com.graphql_java_generator.exception.GraphQLRequestPreparationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CourseTemplateQueryTest {

    @Autowired
    private QueryExecutor queryExecutor;

    @Test
    void testGetAllCourseTemplates() 
            throws GraphQLRequestExecutionException, GraphQLRequestPreparationException {
        
        // Define the GraphQL query
        // Note: The query string omits the "query" keyword and starts with "{"
        String query = "{ courseTemplates { edges { node { id name description code title learningMode lifecycleState createdAt updatedAt } } pageInfo { hasNextPage hasPreviousPage } } }";
        
        // Execute the query (no parameters needed for this simple query)
        Query response = queryExecutor.execWithBindValues(query, new HashMap<>());
        
        // Assert that the response is not null
        assertNotNull(response);
        assertNotNull(response.getCourseTemplates());
        
        // Extract the course templates from the response
        CourseTemplateConnection connection = response.getCourseTemplates();
        assertNotNull(connection);
        
        List<CourseTemplate> templates = connection.getEdges() != null
                ? connection.getEdges().stream()
                        .map(CourseTemplateEdge::getNode)
                        .collect(Collectors.toList())
                : List.of();
        
        // Assert that we got results (or empty list if none exist)
        assertNotNull(templates);
        
        // Print results for debugging
        System.out.println("Found " + templates.size() + " course templates");
        templates.forEach(template -> 
            System.out.println("Template: " + template.getName() + " (ID: " + template.getId() + ")")
        );
    }

    @Test
    void testGetCourseTemplatesWithPagination() 
            throws GraphQLRequestExecutionException, GraphQLRequestPreparationException {
        
        // Define the GraphQL query with parameters
        String query = "{ courseTemplates(first: ?first, offset: ?offset) { edges { node { id name description code title learningMode lifecycleState } } pageInfo { hasNextPage hasPreviousPage startCursor endCursor } } }";
        
        // Set up parameters
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("first", 10);
        parameters.put("offset", 0);
        
        // Execute the query
        Query response = queryExecutor.execWithBindValues(query, parameters);
        
        // Assert response
        assertNotNull(response);
        assertNotNull(response.getCourseTemplates());
        
        // Extract results
        CourseTemplateConnection connection = response.getCourseTemplates();
        assertNotNull(connection);
        assertNotNull(connection.getPageInfo());
        
        List<CourseTemplate> templates = connection.getEdges() != null
                ? connection.getEdges().stream()
                        .map(CourseTemplateEdge::getNode)
                        .collect(Collectors.toList())
                : List.of();
        
        // Assert pagination info
        assertNotNull(connection.getPageInfo());
        assertNotNull(connection.getPageInfo().getHasNextPage());
        assertNotNull(connection.getPageInfo().getHasPreviousPage());
        
        // Assert we got at most 10 results
        assertTrue(templates.size() <= 10, "Should return at most 10 results");
        
        System.out.println("Retrieved " + templates.size() + " course templates with pagination");
    }
}
```

## Example 2: Create a Course Template (Mutation - Spring Boot Test)

```java
package com.example.test;

import com.fsi.tm2poc.graphql.client.CourseTemplate;
import com.fsi.tm2poc.graphql.client.CourseTemplateCreateInput;
import com.fsi.tm2poc.graphql.client.CourseTemplateMutateResponse;
import com.fsi.tm2poc.graphql.client.LearningMode;
import com.fsi.tm2poc.graphql.client.Mutation;
import com.fsi.tm2poc.graphql.client.util.MutationExecutor;
import com.graphql_java_generator.exception.GraphQLRequestExecutionException;
import com.graphql_java_generator.exception.GraphQLRequestPreparationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CourseTemplateMutationTest {

    @Autowired
    private MutationExecutor mutationExecutor;

    @Test
    void testCreateCourseTemplate() 
            throws GraphQLRequestExecutionException, GraphQLRequestPreparationException {
        
        // Build the input object using the builder pattern
        CourseTemplateCreateInput input = CourseTemplateCreateInput.builder()
                .withName("Test Course Template")
                .withDescription("A test course template created from Spring Boot test")
                .withCode("TEST-001")
                .withTitle("Test Course")
                .withLearningMode(LearningMode.LMS)
                .build();
        
        // Define the GraphQL mutation
        // Note: The mutation string omits the "mutation" keyword and starts with "{"
        String mutation = "{ createCourseTemplate(input: ?input) { courseTemplate { id name description code title learningMode lifecycleState createdAt updatedAt } errors { field message } } }";
        
        // Set up parameters
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("input", input);
        
        // Execute the mutation
        Mutation response = mutationExecutor.execWithBindValues(mutation, parameters);
        
        // Assert response is not null
        assertNotNull(response);
        
        // Extract the response
        CourseTemplateMutateResponse mutateResponse = response.getCreateCourseTemplate();
        assertNotNull(mutateResponse);
        
        // Check for errors
        if (mutateResponse.getErrors() != null && !mutateResponse.getErrors().isEmpty()) {
            // Handle errors
            StringBuilder errorMessage = new StringBuilder("Failed to create course template: ");
            mutateResponse.getErrors().forEach(error -> 
                errorMessage.append(error.getField())
                    .append(": ")
                    .append(error.getMessage())
                    .append("; ")
            );
            fail(errorMessage.toString());
        }
        
        // Assert the course template was created
        CourseTemplate createdTemplate = mutateResponse.getCourseTemplate();
        assertNotNull(createdTemplate, "Course template should be created");
        assertNotNull(createdTemplate.getId(), "Created template should have an ID");
        assertEquals("Test Course Template", createdTemplate.getName());
        assertEquals("A test course template created from Spring Boot test", createdTemplate.getDescription());
        assertEquals("TEST-001", createdTemplate.getCode());
        assertEquals(LearningMode.LMS, createdTemplate.getLearningMode());
        
        System.out.println("Successfully created course template with ID: " + createdTemplate.getId());
    }

    @Test
    void testCreateCourseTemplateWithRequiredFieldsOnly() 
            throws GraphQLRequestExecutionException, GraphQLRequestPreparationException {
        
        // Build input with only required fields
        CourseTemplateCreateInput input = CourseTemplateCreateInput.builder()
                .withName("Minimal Course Template")
                .build();
        
        String mutation = "{ createCourseTemplate(input: ?input) { courseTemplate { id name description code title learningMode lifecycleState } errors { field message } } }";
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("input", input);
        
        Mutation response = mutationExecutor.execWithBindValues(mutation, parameters);
        
        assertNotNull(response);
        CourseTemplateMutateResponse mutateResponse = response.getCreateCourseTemplate();
        assertNotNull(mutateResponse);
        
        // Check for errors
        if (mutateResponse.getErrors() != null && !mutateResponse.getErrors().isEmpty()) {
            mutateResponse.getErrors().forEach(error -> 
                System.err.println("Error: " + error.getField() + " - " + error.getMessage())
            );
            fail("Mutation should not have errors");
        }
        
        CourseTemplate createdTemplate = mutateResponse.getCourseTemplate();
        assertNotNull(createdTemplate);
        assertEquals("Minimal Course Template", createdTemplate.getName());
        
        System.out.println("Successfully created minimal course template with ID: " + createdTemplate.getId());
    }
}
```

## Example 3: Complete Test Class with Both Queries and Mutations

```java
package com.example.test;

import com.fsi.tm2poc.graphql.client.*;
import com.fsi.tm2poc.graphql.client.util.MutationExecutor;
import com.fsi.tm2poc.graphql.client.util.QueryExecutor;
import com.graphql_java_generator.exception.GraphQLRequestExecutionException;
import com.graphql_java_generator.exception.GraphQLRequestPreparationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CourseTemplateIntegrationTest {

    @Autowired
    private QueryExecutor queryExecutor;

    @Autowired
    private MutationExecutor mutationExecutor;

    @Test
    void testGetAllCourseTemplates() 
            throws GraphQLRequestExecutionException, GraphQLRequestPreparationException {
        String query = "{ courseTemplates { edges { node { id name description code title learningMode lifecycleState createdAt updatedAt } } } }";
        Query response = queryExecutor.execWithBindValues(query, new HashMap<>());
        
        assertNotNull(response);
        CourseTemplateConnection connection = response.getCourseTemplates();
        assertNotNull(connection);
        
        List<CourseTemplate> templates = connection.getEdges() != null
                ? connection.getEdges().stream()
                        .map(CourseTemplateEdge::getNode)
                        .collect(Collectors.toList())
                : List.of();
        
        assertNotNull(templates);
        System.out.println("Retrieved " + templates.size() + " course templates");
    }

    @Test
    void testGetCourseTemplateById() 
            throws GraphQLRequestExecutionException, GraphQLRequestPreparationException {
        // First, create a course template to get its ID
        CourseTemplate created = createTestCourseTemplate();
        assertNotNull(created);
        String templateId = created.getId();
        
        // Now query by ID
        String query = "{ courseTemplate(id: ?id) { id name description code title learningMode lifecycleState createdAt updatedAt } }";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("id", templateId);
        
        Query response = queryExecutor.execWithBindValues(query, parameters);
        
        assertNotNull(response);
        CourseTemplate template = response.getCourseTemplate();
        assertNotNull(template);
        assertEquals(templateId, template.getId());
        assertEquals(created.getName(), template.getName());
        
        System.out.println("Successfully retrieved course template by ID: " + templateId);
    }

    @Test
    void testCreateAndUpdateCourseTemplate() 
            throws GraphQLRequestExecutionException, GraphQLRequestPreparationException {
        
        // Create a course template
        CourseTemplate created = createTestCourseTemplate();
        assertNotNull(created);
        String templateId = created.getId();
        
        // Update the course template
        CourseTemplateUpdateInput updateInput = CourseTemplateUpdateInput.builder()
                .withId(templateId)
                .withName("Updated Course Template Name")
                .withDescription("Updated description")
                .withCode("UPDATED-001")
                .withTitle("Updated Title")
                .withLearningMode(LearningMode.BLENDED)
                .build();
        
        String mutation = "{ updateCourseTemplate(input: ?input) { courseTemplate { id name description code title learningMode lifecycleState createdAt updatedAt } errors { field message } } }";
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("input", updateInput);
        
        Mutation response = mutationExecutor.execWithBindValues(mutation, parameters);
        CourseTemplateMutateResponse mutateResponse = response.getUpdateCourseTemplate();
        
        assertNotNull(mutateResponse);
        
        // Check for errors
        if (mutateResponse.getErrors() != null && !mutateResponse.getErrors().isEmpty()) {
            mutateResponse.getErrors().forEach(error -> 
                System.err.println("Error: " + error.getField() + " - " + error.getMessage())
            );
            fail("Update should not have errors");
        }
        
        CourseTemplate updated = mutateResponse.getCourseTemplate();
        assertNotNull(updated);
        assertEquals(templateId, updated.getId());
        assertEquals("Updated Course Template Name", updated.getName());
        assertEquals("Updated description", updated.getDescription());
        assertEquals(LearningMode.BLENDED, updated.getLearningMode());
        
        System.out.println("Successfully updated course template: " + updated.getName());
    }

    @Test
    void testFullWorkflow() 
            throws GraphQLRequestExecutionException, GraphQLRequestPreparationException {
        
        // 1. Create a course template
        CourseTemplate created = createTestCourseTemplate();
        assertNotNull(created);
        String templateId = created.getId();
        System.out.println("Created template with ID: " + templateId);
        
        // 2. Query it by ID
        String query = "{ courseTemplate(id: ?id) { id name description code title learningMode lifecycleState } }";
        Map<String, Object> params = new HashMap<>();
        params.put("id", templateId);
        
        Query response = queryExecutor.execWithBindValues(query, params);
        CourseTemplate queried = response.getCourseTemplate();
        assertNotNull(queried);
        assertEquals(templateId, queried.getId());
        System.out.println("Queried template: " + queried.getName());
        
        // 3. Update it
        CourseTemplateUpdateInput updateInput = CourseTemplateUpdateInput.builder()
                .withId(templateId)
                .withName("Workflow Test Template")
                .build();
        
        String mutation = "{ updateCourseTemplate(input: ?input) { courseTemplate { id name } errors { field message } } }";
        Map<String, Object> mutationParams = new HashMap<>();
        mutationParams.put("input", updateInput);
        
        Mutation mutationResponse = mutationExecutor.execWithBindValues(mutation, mutationParams);
        CourseTemplateMutateResponse updateResponse = mutationResponse.getUpdateCourseTemplate();
        assertNotNull(updateResponse);
        assertNull(updateResponse.getErrors() || updateResponse.getErrors().isEmpty() ? null : updateResponse.getErrors());
        
        CourseTemplate updated = updateResponse.getCourseTemplate();
        assertNotNull(updated);
        assertEquals("Workflow Test Template", updated.getName());
        System.out.println("Updated template: " + updated.getName());
    }

    /**
     * Helper method to create a test course template
     */
    private CourseTemplate createTestCourseTemplate() 
            throws GraphQLRequestExecutionException, GraphQLRequestPreparationException {
        CourseTemplateCreateInput input = CourseTemplateCreateInput.builder()
                .withName("Test Course Template " + System.currentTimeMillis())
                .withDescription("A test course template")
                .withCode("TEST-" + System.currentTimeMillis())
                .withTitle("Test Course")
                .withLearningMode(LearningMode.LMS)
                .build();
        
        String mutation = "{ createCourseTemplate(input: ?input) { courseTemplate { id name description code title learningMode lifecycleState } errors { field message } } }";
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("input", input);
        
        Mutation response = mutationExecutor.execWithBindValues(mutation, parameters);
        CourseTemplateMutateResponse mutateResponse = response.getCreateCourseTemplate();
        
        if (mutateResponse.getErrors() != null && !mutateResponse.getErrors().isEmpty()) {
            fail("Failed to create test course template: " + mutateResponse.getErrors());
        }
        
        return mutateResponse.getCourseTemplate();
    }
}
```

## Example 4: Test Configuration

Create a test configuration file at `src/test/resources/application.properties`:

```properties
# GraphQL endpoint URL for testing
graphql.client.url=http://localhost:8080/graphql

# Optional: Configure logging for debugging
logging.level.com.fsi.tm2poc.graphql=DEBUG
logging.level.com.graphql_java_generator=DEBUG
```

Or using YAML format at `src/test/resources/application.yml`:

```yaml
graphql:
  client:
    url: http://localhost:8080/graphql

logging:
  level:
    com.fsi.tm2poc.graphql: DEBUG
    com.graphql_java_generator: DEBUG
```

## Example 5: Using @TestPropertySource for Test-Specific Configuration

```java
package com.example.test;

import com.fsi.tm2poc.graphql.client.Query;
import com.fsi.tm2poc.graphql.client.util.QueryExecutor;
import com.graphql_java_generator.exception.GraphQLRequestExecutionException;
import com.graphql_java_generator.exception.GraphQLRequestPreparationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@TestPropertySource(properties = {
    "graphql.client.url=http://localhost:8080/graphql"
})
class CourseTemplateWithCustomConfigTest {

    @Autowired
    private QueryExecutor queryExecutor;

    @Test
    void testQueryWithCustomConfig() 
            throws GraphQLRequestExecutionException, GraphQLRequestPreparationException {
        String query = "{ courseTemplates { edges { node { id name } } } }";
        Query response = queryExecutor.execWithBindValues(query, new HashMap<>());
        assertNotNull(response);
    }
}
```

## Notes

1. **Query/Mutation Strings**: The query and mutation strings passed to `execWithBindValues` should omit the `query` or `mutation` keyword and start directly with `{`.

2. **Parameters**: Use `?parameterName` in the query string for optional parameters and `&parameterName` for mandatory parameters. Then provide the values in the parameters Map.

3. **Error Handling**: Always check the `errors` field in mutation responses to handle validation errors gracefully. In tests, use `assertNull()` or `assertTrue(isEmpty())` to verify no errors occurred.

4. **Pagination**: The `courseTemplates` query returns a `CourseTemplateConnection` which uses a cursor-based pagination pattern with `edges` and `pageInfo`.

5. **Spring Auto-Configuration**: The library includes Spring Boot auto-configuration, so `QueryExecutor` and `MutationExecutor` will be automatically available as Spring beans when the library is on the classpath.

6. **Test Configuration**: Use `src/test/resources/application.properties` or `application.yml` to configure the GraphQL endpoint for tests, or use `@TestPropertySource` for test-specific configuration.

7. **Assertions**: Use JUnit 5 assertions (`assertNotNull`, `assertEquals`, `assertTrue`, etc.) to verify the results of your GraphQL operations.

8. **Test Isolation**: Consider using unique identifiers (like timestamps) in test data to avoid conflicts between test runs.

