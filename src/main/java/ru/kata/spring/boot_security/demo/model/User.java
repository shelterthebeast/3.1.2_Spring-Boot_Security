package ru.kata.spring.boot_security.demo.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name")
    private String name;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "age")
    private int age;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    public User() {}

    public User(String username, String password, String name, String email, int age, Set<Role> roles) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
        this.age = age;
        this.roles = roles;
    }

    public User(Long id, String username, String password, String name, String email, int age, Set<Role> roles) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
        this.age = age;
        this.roles = roles;
    }

    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    @Override
    public String getUsername() {return username;}

    public void setUsername(String username) {this.username = username;}

    @Override
    public String getPassword() {return password;}

    public void setPassword(String password) {this.password = password;}

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public String getEmail() {return email;}

    public void setEmail(String email) {this.email = email;}

    public int getAge() {return age;}

    public void setAge(int age) {this.age = age;}

    public Set<Role> getRoles() {return roles;}

    public void setRoles(Set<Role> roles) {this.roles = roles;}

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {return getRoles();}

    @Override
    public boolean isAccountNonExpired() {return true;}

    @Override
    public boolean isAccountNonLocked() {return true;}

    @Override
    public boolean isCredentialsNonExpired() {return true;}

    @Override
    public boolean isEnabled() {return true;}
}