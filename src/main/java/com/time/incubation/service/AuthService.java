package com.time.incubation.service;

import com.time.incubation.exception.DataBaseException;
import com.time.incubation.exception.DuplicateIdException;
import com.time.incubation.exception.InvalidInputException;
import com.time.incubation.model.User;
import com.time.incubation.model.UserRole;
import com.time.incubation.repository.UserRepository;
import com.time.incubation.util.PasswordUtils;
import com.time.incubation.util.ValidationUtils;

import java.sql.SQLException;

public class AuthService {

    private final UserRepository repository;

    public AuthService(UserRepository repository) {
        this.repository = repository;
    }

    /**
     * Returns the User if username + plainPassword match and the account is
     * active, otherwise null.
     */
    public User login(String username, String plainPassword) throws DataBaseException {
        if (ValidationUtils.isNullOrEmpty(username) || ValidationUtils.isNullOrEmpty(plainPassword)) {
            return null;
        }

        try {
            User user = repository.findByUsername(username);
            if (user == null) return null;
            if (!user.isActive()) return null;

            if (PasswordUtils.matches(plainPassword, user.getPassword())) {
                return user;
            }
            return null;

        } catch (SQLException e) {
            throw new DataBaseException("Login failed due to a database error.", e);
        }
    }

    public void registerUser(String userId, String username, String plainPassword,
                             UserRole role, String fullName, String email)
            throws InvalidInputException, DuplicateIdException, DataBaseException {

        if (ValidationUtils.isNullOrEmpty(userId)) {
            throw new InvalidInputException("User ID is required.");
        }
        if (ValidationUtils.isNullOrEmpty(username)) {
            throw new InvalidInputException("Username is required.");
        }
        if (ValidationUtils.isNullOrEmpty(plainPassword) || plainPassword.length() < 6) {
            throw new InvalidInputException("Password must be at least 6 characters.");
        }
        if (role == null) {
            throw new InvalidInputException("User role is required.");
        }
        if (email != null && !email.isEmpty() && !ValidationUtils.isValidEmail(email)) {
            throw new InvalidInputException("Invalid email: " + email);
        }

        try {
            if (repository.findById(userId) != null) {
                throw new DuplicateIdException("User ID already exists: " + userId);
            }
            if (repository.findByUsername(username) != null) {
                throw new DuplicateIdException("Username already taken: " + username);
            }

            User u = new User(userId, username,
                    PasswordUtils.hash(plainPassword),
                    role, fullName, email, true);

            repository.save(u);

        } catch (SQLException e) {
            throw new DataBaseException("Failed to register user.", e);
        }
    }

    public void changePassword(String userId, String newPlainPassword)
            throws InvalidInputException, DataBaseException {
        if (ValidationUtils.isNullOrEmpty(newPlainPassword) || newPlainPassword.length() < 6) {
            throw new InvalidInputException("Password must be at least 6 characters.");
        }
        try {
            User u = repository.findById(userId);
            if (u == null) {
                throw new InvalidInputException("User not found: " + userId);
            }
            u.setPassword(PasswordUtils.hash(newPlainPassword));
            repository.save(u);
        } catch (SQLException e) {
            throw new DataBaseException("Failed to change password.", e);
        }
    }
}