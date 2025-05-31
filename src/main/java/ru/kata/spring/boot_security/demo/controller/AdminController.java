package ru.kata.spring.boot_security.demo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.UserService;
import ru.kata.spring.boot_security.demo.dao.RoleDAO;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class AdminController {

    private final UserService userService;
    private final RoleDAO roleDAO;

    public AdminController(UserService userService, RoleDAO roleDAO) {
        this.userService = userService;
        this.roleDAO = roleDAO;
    }

    @GetMapping("/admin")
    public String adminPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        model.addAttribute("username", authentication.getName());
        model.addAttribute("roles", authentication.getAuthorities());

        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("newUser", new User());
        model.addAttribute("allRoles", roleDAO.findAll());

        return "admin";
    }

    @GetMapping("/admin/users")
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("username", authentication.getName());
        model.addAttribute("roles", authentication.getAuthorities());
        model.addAttribute("allRoles", roleDAO.findAll());
        model.addAttribute("newUser", new User());
        return "list-users";
    }

    @GetMapping("/admin/showNewUserForm")
    public String showFormForAdd(Model model) {
        User user = new User();
        model.addAttribute("user", user);
        model.addAttribute("allRoles", roleDAO.findAll());
        return "user-form";
    }

    @PostMapping("/admin/saveUser")
    public String saveUser(@ModelAttribute("user") User user,
                           @RequestParam(value = "selectedRoles", required = false) List<Long> selectedRolesIds) {

        Set<Role> roles = new HashSet<>();
        if (selectedRolesIds != null) {
            roles = selectedRolesIds.stream()
                    .map(roleDAO::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
        }
        user.setRoles(roles);

        if (user.getId() == null || user.getId() == 0) {
            userService.saveUser(user);
        } else {
            userService.updateUser(user);
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/admin/showFormForUpdate")
    public String showFormForUpdate(@RequestParam("userId") Long id, Model model) {
        User user = userService.getUser(id);
        model.addAttribute("user", user);
        model.addAttribute("allRoles", roleDAO.findAll());
        return "user-form";
    }

    @GetMapping("/admin/deleteUser")
    public String deleteUser(@RequestParam("userId") Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }
}