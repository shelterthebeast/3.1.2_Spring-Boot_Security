package ru.kata.spring.boot_security.demo.service;

import ru.kata.spring.boot_security.demo.dao.RoleDAO;
import ru.kata.spring.boot_security.demo.dao.UserDAO;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserDAO userDAO;
    private final RoleDAO roleDAO;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    public UserServiceImpl(UserDAO userDAO, RoleDAO roleDAO, PasswordEncoder passwordEncoder, JdbcTemplate jdbcTemplate) {
        this.userDAO = userDAO;
        this.roleDAO = roleDAO;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        if (roleDAO.findByName("ADMIN").isEmpty()) {
            Role adminRole = new Role("ADMIN");
            roleDAO.save(adminRole);
        }
        if (roleDAO.findByName("USER").isEmpty()) {
            Role userRole = new Role("USER");
            roleDAO.save(userRole);
        }

        if (userDAO.findByUsername("admin").isEmpty()) {
            Role adminRole = roleDAO.findByName("ADMIN").get();
            User admin = new User("admin", passwordEncoder.encode("admin"), "Admin", "admin@mail.com", 30, new HashSet<>());
            admin.getRoles().add(adminRole);
            userDAO.save(admin);
        }

        if (userDAO.findByUsername("user").isEmpty()) {
            Role userRole = roleDAO.findByName("USER").get();
            User user = new User("user", passwordEncoder.encode("user"), "User", "user@mail.com", 25, new HashSet<>());
            user.getRoles().add(userRole);
            userDAO.save(user);
        }
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
        user.setPassword(passwordEncoder.encode(user.getPassword()));
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
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
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

    @Override
    @Transactional
    public void saveOrUpdateUser(User user, Set<Long> selectedRoleIds) {
        Set<Role> roles = new HashSet<>();
        if (selectedRoleIds != null && !selectedRoleIds.isEmpty()) {
            roles = selectedRoleIds.stream()
                    .map(roleDAO::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
        }
        user.setRoles(roles);

        if (user.getId() == null || user.getId() == 0) {
            saveUser(user);
        } else {
            updateUser(user);
        }
    }
}