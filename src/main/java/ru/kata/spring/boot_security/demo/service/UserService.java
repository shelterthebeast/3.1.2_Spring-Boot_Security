package ru.kata.spring.boot_security.demo.service;

import ru.kata.spring.boot_security.demo.model.User;
import java.util.List;

public interface UserService {
    List<User> getAllUsers();
    void saveUser(User user);
    User getUser(Long id);
    void deleteUser(Long id);
    void updateUser(User user);
}