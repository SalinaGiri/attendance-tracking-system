package be.ucll.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.ucll.model.User;
import be.ucll.service.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/players")
@CrossOrigin(origins = "${FRONTEND_URL:http://localhost:8000}")
public class UserRestController {

    private UserService userService;

    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping()
    public User addUser(@Valid @RequestBody User user) {
        return userService.addUser(user);
    }
}
