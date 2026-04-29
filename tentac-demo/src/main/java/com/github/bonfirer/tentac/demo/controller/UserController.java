package com.github.bonfirer.tentac.demo.controller;

import com.github.bonfirer.tentac.annotation.MarkdownApi;
import com.github.bonfirer.tentac.annotation.MarkdownApiOperation;
import com.github.bonfirer.tentac.annotation.MarkdownApiParam;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Sample User REST controller with both Swagger2 and Tentac annotations.
 * Demonstrates compatibility with existing Swagger2 annotated controllers.
 */
@RestController
@RequestMapping("/api/users")
@Api(tags = "User Management", description = "Operations about users")
@MarkdownApi(tags = "User Management", description = "Operations about users")
public class UserController {

    /**
     * Get all users.
     */
    @GetMapping
    @ApiOperation(value = "Get all users", notes = "Returns a list of all registered users",
                  nickname = "getAllUsers")
    @MarkdownApiOperation(value = "Get all users",
                          notes = "Returns a list of all registered users")
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        users.add(new User(1L, "john_doe", "john@example.com"));
        users.add(new User(2L, "jane_smith", "jane@example.com"));
        return users;
    }

    /**
     * Get user by ID.
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "Get user by ID", notes = "Returns a single user by their unique ID",
                  nickname = "getUserById")
    @MarkdownApiOperation(value = "Get user by ID",
                          notes = "Returns a single user by their unique ID")
    public User getUserById(
            @PathVariable("id")
            @ApiParam(value = "User ID", required = true, example = "1")
            @MarkdownApiParam(name = "id", value = "User ID", required = true, example = "1",
                              in = "path")
            Long id) {
        return new User(id, "john_doe", "john@example.com");
    }

    /**
     * Create a new user.
     */
    @PostMapping
    @ApiOperation(value = "Create a new user", notes = "Creates a new user with the provided details",
                  nickname = "createUser")
    @MarkdownApiOperation(value = "Create a new user",
                          notes = "Creates a new user with the provided details")
    public User createUser(
            @RequestBody
            @ApiParam(value = "User object to create", required = true)
            User user) {
        user.setId(3L);
        return user;
    }

    /**
     * Update an existing user.
     */
    @PutMapping("/{id}")
    @ApiOperation(value = "Update user", notes = "Updates an existing user's information",
                  nickname = "updateUser")
    @MarkdownApiOperation(value = "Update user",
                          notes = "Updates an existing user's information")
    public User updateUser(
            @PathVariable("id")
            @ApiParam(value = "User ID to update", required = true, example = "1")
            @MarkdownApiParam(name = "id", value = "User ID to update", required = true,
                              example = "1", in = "path")
            Long id,
            @RequestBody
            @ApiParam(value = "Updated user object", required = true)
            User user) {
        user.setId(id);
        return user;
    }

    /**
     * Delete a user.
     */
    @DeleteMapping("/{id}")
    @ApiOperation(value = "Delete user", notes = "Deletes a user by their ID",
                  nickname = "deleteUser")
    @MarkdownApiOperation(value = "Delete user",
                          notes = "Deletes a user by their ID")
    public void deleteUser(
            @PathVariable("id")
            @ApiParam(value = "User ID to delete", required = true, example = "1")
            @MarkdownApiParam(name = "id", value = "User ID to delete", required = true,
                              example = "1", in = "path")
            Long id) {
        // Delete logic here
    }

    /**
     * Search users by keyword.
     */
    @GetMapping("/search")
    @ApiOperation(value = "Search users", notes = "Search users by keyword in name or email",
                  nickname = "searchUsers")
    @MarkdownApiOperation(value = "Search users",
                          notes = "Search users by keyword in name or email")
    public List<User> searchUsers(
            @RequestParam("keyword")
            @ApiParam(value = "Search keyword", required = true, example = "john")
            @MarkdownApiParam(name = "keyword", value = "Search keyword", required = true,
                              example = "john")
            String keyword,
            @RequestParam(value = "page", defaultValue = "1")
            @ApiParam(value = "Page number", defaultValue = "1")
            @MarkdownApiParam(name = "page", value = "Page number", defaultValue = "1")
            int page,
            @RequestParam(value = "size", defaultValue = "20")
            @ApiParam(value = "Page size", defaultValue = "20")
            @MarkdownApiParam(name = "size", value = "Page size", defaultValue = "20")
            int size) {
        List<User> users = new ArrayList<>();
        users.add(new User(1L, "john_doe", "john@example.com"));
        return users;
    }

    /**
     * User model.
     */
    @io.swagger.annotations.ApiModel(description = "User entity")
    public static class User {
        @io.swagger.annotations.ApiModelProperty(value = "User ID", example = "1")
        private Long id;

        @io.swagger.annotations.ApiModelProperty(value = "Username", example = "john_doe")
        private String username;

        @io.swagger.annotations.ApiModelProperty(value = "Email address", example = "john@example.com")
        private String email;

        public User() {
        }

        public User(Long id, String username, String email) {
            this.id = id;
            this.username = username;
            this.email = email;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
