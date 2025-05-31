package ru.kata.spring.boot_security.demo.service;

import ru.kata.spring.boot_security.demo.dao.RoleDAO;
import ru.kata.spring.boot_security.demo.dao.UserDAO;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private final UserDAO userDAO;
    private final RoleDAO roleDAO;

    public UserServiceImpl(UserDAO userDAO, RoleDAO roleDAO) {
        this.userDAO = userDAO;
        this.roleDAO = roleDAO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    @Override
    @Transactional
    public void saveUser(User user) {
        if (user.getRoles().isEmpty()) {
            Optional<Role> defaultRole = roleDAO.findByName("USER");
            defaultRole.ifPresent(role -> {
                Set<Role> roles = new HashSet<>();
                roles.add(role);
                user.setRoles(roles);
            });
        }
        userDAO.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUser(Long id) {
        Optional<User> result = userDAO.findById(id);
        return result.orElseThrow(() -> new RuntimeException("Did not find user id - " + id));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        userDAO.deleteById(id);
    }

    @Override
    @Transactional
    public void updateUser(User updatedUser) {
        User existingUser = userDAO.findById(updatedUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found for update with id: " + updatedUser.getId()));

        existingUser.setName(updatedUser.getName());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setAge(updatedUser.getAge());

        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            existingUser.setPassword(updatedUser.getPassword());
        }

        if (!updatedUser.getRoles().isEmpty()) {
            Set<Role> updatedRoles = new HashSet<>();
            for (Role role : updatedUser.getRoles()) {
                roleDAO.findByName(role.getName())
                        .ifPresent(updatedRoles::add);
            }
            existingUser.setRoles(updatedRoles);
        } else {
            existingUser.setRoles(existingUser.getRoles());
        }

        userDAO.save(existingUser);
    }
}