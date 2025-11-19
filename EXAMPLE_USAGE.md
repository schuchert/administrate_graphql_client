# Example Usage of Administrate GraphQL Client

This document provides examples of how to use the generated GraphQL client library.

## Prerequisites

1. Add the dependency to your project:
```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>administrate-graphql-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

2. Configure the GraphQL endpoint in your `application.properties` or `application.yml`:
```properties
# The GraphQL endpoint URL
graphql.client.url=http://your-graphql-server.com/graphql
```

## Example 1: Query All Course Templates

```java
package com.example.service;

import com.fsi.tm2poc.graphql.client.CourseTemplate;
import com.fsi.tm2poc.graphql.client.CourseTemplateConnection;
import com.fsi.tm2poc.graphql.client.CourseTemplateEdge;
import com.fsi.tm2poc.graphql.client.Query;
import com.fsi.tm2poc.graphql.client.util.QueryExecutor;
import com.graphql_java_generator.exception.GraphQLRequestExecutionException;
import com.graphql_java_generator.exception.GraphQLRequestPreparationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CourseTemplateService {

    @Autowired
    private QueryExecutor queryExecutor;

    /**
     * Retrieve all course templates
     * @return List of all course templates
     */
    public List<CourseTemplate> getAllCourseTemplates() 
            throws GraphQLRequestExecutionException, GraphQLRequestPreparationException {
        
        // Define the GraphQL query
        // Note: The query string omits the "query" keyword and starts with "{"
        String query = "{ courseTemplates { edges { node { id name description code title learningMode lifecycleState createdAt updatedAt } } pageInfo { hasNextPage hasPreviousPage } } }";
        
        // Execute the query (no parameters needed for this simple query)
        Query response = queryExecutor.execWithBindValues(query, new HashMap<>());
        
        // Extract the course templates from the response
        CourseTemplateConnection connection = response.getCourseTemplates();
        if (connection == null || connection.getEdges() == null) {
            return List.of();
        }
        
        return connection.getEdges().stream()
                .map(CourseTemplateEdge::getNode)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve course templates with pagination
     * @param first Maximum number of results to return
     * @param offset Number of results to skip
     * @return List of course templates
     */
    public List<CourseTemplate> getCourseTemplates(int first, int offset) 
            throws GraphQLRequestExecutionException, GraphQLRequestPreparationException {
        
        // Define the GraphQL query with parameters
        String query = "{ courseTemplates(first: ?first, offset: ?offset) { edges { node { id name description code title learningMode lifecycleState } } pageInfo { hasNextPage hasPreviousPage startCursor endCursor } } }";
        
        // Set up parameters
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("first", first);
        parameters.put("offset", offset);
        
        // Execute the query
        Query response = queryExecutor.execWithBindValues(query, parameters);
        
        // Extract results
        CourseTemplateConnection connection = response.getCourseTemplates();
        if (connection == null || connection.getEdges() == null) {
            return List.of();
        }
        
        return connection.getEdges().stream()
                .map(CourseTemplateEdge::getNode)
                .collect(Collectors.toList());
    }
}
```

## Example 2: Create a Course Template (Mutation)

```java
package com.example.service;

import com.fsi.tm2poc.graphql.client.CourseTemplate;
import com.fsi.tm2poc.graphql.client.CourseTemplateCreateInput;
import com.fsi.tm2poc.graphql.client.CourseTemplateMutateResponse;
import com.fsi.tm2poc.graphql.client.LearningMode;
import com.fsi.tm2poc.graphql.client.Mutation;
import com.fsi.tm2poc.graphql.client.util.MutationExecutor;
import com.graphql_java_generator.exception.GraphQLRequestExecutionException;
import com.graphql_java_generator.exception.GraphQLRequestPreparationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CourseTemplateMutationService {

    @Autowired
    private MutationExecutor mutationExecutor;

    /**
     * Create a new course template
     * @param name Required course template name
     * @param description Optional description
     * @param code Optional code
     * @param title Optional title
     * @param learningMode Optional learning mode (CLASSROOM, LMS, BLENDED, VIRTUAL)
     * @return The created course template, or null if there were errors
     * @throws RuntimeException if there are validation errors
     */
    public CourseTemplate createCourseTemplate(
            String name,
            String description,
            String code,
            String title,
            LearningMode learningMode) 
            throws GraphQLRequestExecutionException, GraphQLRequestPreparationException {
        
        // Build the input object using the builder pattern
        CourseTemplateCreateInput input = CourseTemplateCreateInput.builder()
                .withName(name)
                .withDescription(description)
                .withCode(code)
                .withTitle(title)
                .withLearningMode(learningMode)
                .build();
        
        // Define the GraphQL mutation
        // Note: The mutation string omits the "mutation" keyword and starts with "{"
        String mutation = "{ createCourseTemplate(input: ?input) { courseTemplate { id name description code title learningMode lifecycleState createdAt updatedAt } errors { field message } } }";
        
        // Set up parameters
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("input", input);
        
        // Execute the mutation
        Mutation response = mutationExecutor.execWithBindValues(mutation, parameters);
        
        // Extract the response
        CourseTemplateMutateResponse mutateResponse = response.getCreateCourseTemplate();
        
        // Check for errors
        if (mutateResponse != null && mutateResponse.getErrors() != null && !mutateResponse.getErrors().isEmpty()) {
            // Handle errors
            StringBuilder errorMessage = new StringBuilder("Failed to create course template: ");
            mutateResponse.getErrors().forEach(error -> 
                errorMessage.append(error.getField())
                    .append(": ")
                    .append(error.getMessage())
                    .append("; ")
            );
            throw new RuntimeException(errorMessage.toString());
        }
        
        // Return the created course template
        return mutateResponse != null ? mutateResponse.getCourseTemplate() : null;
    }

    /**
     * Simplified version using only required fields
     */
    public CourseTemplate createCourseTemplate(String name) 
            throws GraphQLRequestExecutionException, GraphQLRequestPreparationException {
        return createCourseTemplate(name, null, null, null, null);
    }
}
```

## Example 3: Complete Service Class with Both Queries and Mutations

```java
package com.example.service;

import com.fsi.tm2poc.graphql.client.*;
import com.fsi.tm2poc.graphql.client.util.MutationExecutor;
import com.fsi.tm2poc.graphql.client.util.QueryExecutor;
import com.graphql_java_generator.exception.GraphQLRequestExecutionException;
import com.graphql_java_generator.exception.GraphQLRequestPreparationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CourseTemplateService {

    @Autowired
    private QueryExecutor queryExecutor;

    @Autowired
    private MutationExecutor mutationExecutor;

    /**
     * Get all course templates
     */
    public List<CourseTemplate> getAllCourseTemplates() 
            throws GraphQLRequestExecutionException, GraphQLRequestPreparationException {
        String query = "{ courseTemplates { edges { node { id name description code title learningMode lifecycleState createdAt updatedAt } } } }";
        Query response = queryExecutor.execWithBindValues(query, new HashMap<>());
        CourseTemplateConnection connection = response.getCourseTemplates();
        if (connection == null || connection.getEdges() == null) {
            return List.of();
        }
        return connection.getEdges().stream()
                .map(CourseTemplateEdge::getNode)
                .collect(Collectors.toList());
    }

    /**
     * Get a single course template by ID
     */
    public CourseTemplate getCourseTemplateById(String id) 
            throws GraphQLRequestExecutionException, GraphQLRequestPreparationException {
        String query = "{ courseTemplate(id: ?id) { id name description code title learningMode lifecycleState createdAt updatedAt } }";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("id", id);
        Query response = queryExecutor.execWithBindValues(query, parameters);
        return response.getCourseTemplate();
    }

    /**
     * Create a new course template
     */
    public CourseTemplate createCourseTemplate(
            String name,
            String description,
            String code,
            String title,
            LearningMode learningMode) 
            throws GraphQLRequestExecutionException, GraphQLRequestPreparationException {
        
        CourseTemplateCreateInput input = CourseTemplateCreateInput.builder()
                .withName(name)
                .withDescription(description)
                .withCode(code)
                .withTitle(title)
                .withLearningMode(learningMode)
                .build();
        
        String mutation = "{ createCourseTemplate(input: ?input) { courseTemplate { id name description code title learningMode lifecycleState createdAt updatedAt } errors { field message } } }";
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("input", input);
        
        Mutation response = mutationExecutor.execWithBindValues(mutation, parameters);
        CourseTemplateMutateResponse mutateResponse = response.getCreateCourseTemplate();
        
        // Check for errors
        if (mutateResponse != null && mutateResponse.getErrors() != null && !mutateResponse.getErrors().isEmpty()) {
            StringBuilder errorMessage = new StringBuilder("Failed to create course template: ");
            mutateResponse.getErrors().forEach(error -> 
                errorMessage.append(error.getField())
                    .append(": ")
                    .append(error.getMessage())
                    .append("; ")
            );
            throw new RuntimeException(errorMessage.toString());
        }
        
        return mutateResponse != null ? mutateResponse.getCourseTemplate() : null;
    }

    /**
     * Update an existing course template
     */
    public CourseTemplate updateCourseTemplate(
            String id,
            String name,
            String description,
            String code,
            String title,
            LearningMode learningMode) 
            throws GraphQLRequestExecutionException, GraphQLRequestPreparationException {
        
        CourseTemplateUpdateInput input = CourseTemplateUpdateInput.builder()
                .withId(id)
                .withName(name)
                .withDescription(description)
                .withCode(code)
                .withTitle(title)
                .withLearningMode(learningMode)
                .build();
        
        String mutation = "{ updateCourseTemplate(input: ?input) { courseTemplate { id name description code title learningMode lifecycleState createdAt updatedAt } errors { field message } } }";
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("input", input);
        
        Mutation response = mutationExecutor.execWithBindValues(mutation, parameters);
        CourseTemplateMutateResponse mutateResponse = response.getUpdateCourseTemplate();
        
        // Check for errors
        if (mutateResponse != null && mutateResponse.getErrors() != null && !mutateResponse.getErrors().isEmpty()) {
            StringBuilder errorMessage = new StringBuilder("Failed to update course template: ");
            mutateResponse.getErrors().forEach(error -> 
                errorMessage.append(error.getField())
                    .append(": ")
                    .append(error.getMessage())
                    .append("; ")
            );
            throw new RuntimeException(errorMessage.toString());
        }
        
        return mutateResponse != null ? mutateResponse.getCourseTemplate() : null;
    }
}
```

## Example 4: Using in a REST Controller

```java
package com.example.controller;

import com.example.service.CourseTemplateService;
import com.fsi.tm2poc.graphql.client.CourseTemplate;
import com.fsi.tm2poc.graphql.client.LearningMode;
import com.graphql_java_generator.exception.GraphQLRequestExecutionException;
import com.graphql_java_generator.exception.GraphQLRequestPreparationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course-templates")
public class CourseTemplateController {

    @Autowired
    private CourseTemplateService courseTemplateService;

    @GetMapping
    public ResponseEntity<List<CourseTemplate>> getAllCourseTemplates() {
        try {
            List<CourseTemplate> templates = courseTemplateService.getAllCourseTemplates();
            return ResponseEntity.ok(templates);
        } catch (GraphQLRequestExecutionException | GraphQLRequestPreparationException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<CourseTemplate> createCourseTemplate(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) LearningMode learningMode) {
        try {
            CourseTemplate template = courseTemplateService.createCourseTemplate(
                    name, description, code, title, learningMode);
            return ResponseEntity.ok(template);
        } catch (GraphQLRequestExecutionException | GraphQLRequestPreparationException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
```

## Notes

1. **Query/Mutation Strings**: The query and mutation strings passed to `execWithBindValues` should omit the `query` or `mutation` keyword and start directly with `{`.

2. **Parameters**: Use `?parameterName` in the query string for optional parameters and `&parameterName` for mandatory parameters. Then provide the values in the parameters Map.

3. **Error Handling**: Always check the `errors` field in mutation responses to handle validation errors gracefully.

4. **Pagination**: The `courseTemplates` query returns a `CourseTemplateConnection` which uses a cursor-based pagination pattern with `edges` and `pageInfo`.

5. **Spring Auto-Configuration**: The library includes Spring Boot auto-configuration, so `QueryExecutor` and `MutationExecutor` will be automatically available as Spring beans when the library is on the classpath.

