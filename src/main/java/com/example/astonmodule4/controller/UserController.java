package com.example.astonmodule4.controller;

import com.example.astonmodule4.model.dto.request.CreateUserRequest;
import com.example.astonmodule4.model.dto.request.UpdateUserRequest;
import com.example.astonmodule4.model.dto.response.UserResponse;
import com.example.astonmodule4.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "API для управления пользователями")
public class UserController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @Operation(summary = "Получить всех пользователей", description = "Возвращает список всех пользователей")
    @ApiResponse(responseCode = "200", description = "Успешное получение списка пользователей")
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<UserResponse>>> getAllUsers() {
        LOGGER.info("Getting all users process");
        List<UserResponse> users = userService.getAllUsers();
        LOGGER.debug("Getting count of users: {}", users.size());

        List<EntityModel<UserResponse>> userModels = users.stream()
                .map(user -> EntityModel.of(user)
                        .add(linkTo(methodOn(UserController.class).getUser(user.getId())).withSelfRel())
                        .add(linkTo(methodOn(UserController.class).updateUser(user.getId(), null)).withRel("update"))
                        .add(linkTo(methodOn(UserController.class).deleteUser(user.getId())).withRel("delete")))
                .collect(Collectors.toList());

        CollectionModel<EntityModel<UserResponse>> collectionModel = CollectionModel.of(userModels);
        collectionModel.add(linkTo(methodOn(UserController.class).getAllUsers()).withSelfRel());
        collectionModel.add(linkTo(methodOn(UserController.class).createUser(null)).withRel("create"));

        return ResponseEntity.ok(collectionModel);
    }
    @Operation(summary = "Создать пользователя", description = "Создает нового пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно создан"),
            @ApiResponse(responseCode = "400", description = "Неверные входные данные", content = @Content)
    })
    @PostMapping
    public ResponseEntity<EntityModel<UserResponse>> createUser(
            @Parameter(description = "Данные для создания пользователя", required = true)
            @RequestBody @Valid CreateUserRequest userRequest) {

        LOGGER.info("Creating user process: email={}", userRequest.getEmail());
        UserResponse createdUser = userService.createUser(userRequest);
        LOGGER.info("User created: ID={}, email={}", createdUser.getId(), createdUser.getEmail());

        EntityModel<UserResponse> model = EntityModel.of(createdUser);
        model.add(linkTo(methodOn(UserController.class).getUser(createdUser.getId())).withSelfRel());
        model.add(linkTo(methodOn(UserController.class).updateUser(createdUser.getId(), null)).withRel("update"));
        model.add(linkTo(methodOn(UserController.class).deleteUser(createdUser.getId())).withRel("delete"));
        model.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"));

        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Обновить пользователя", description = "Обновляет данные пользователя по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно обновлен"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content),
            @ApiResponse(responseCode = "400", description = "Неверные входные данные", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<UserResponse>> updateUser(
            @Parameter(description = "ID пользователя", required = true)
            @PathVariable Long id,

            @Parameter(description = "Данные для обновления пользователя", required = true)
            @RequestBody @Valid UpdateUserRequest userRequest) {

        LOGGER.info("Updating user process: id={}", id);
        UserResponse updatedUser = userService.updateUser(id, userRequest);
        LOGGER.info("User updated: ID={}, name={}, email={}",
                updatedUser.getId(), updatedUser.getName(), updatedUser.getEmail());

        EntityModel<UserResponse> model = EntityModel.of(updatedUser);
        model.add(linkTo(methodOn(UserController.class).getUser(id)).withSelfRel());
        model.add(linkTo(methodOn(UserController.class).deleteUser(id)).withRel("delete"));
        model.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"));

        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Получить пользователя по ID", description = "Возвращает пользователя по указанному ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь найден"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<UserResponse>> getUser(
            @Parameter(description = "ID пользователя", required = true)
            @PathVariable Long id) {

        LOGGER.info("Getting user process: id={}", id);
        UserResponse gotUser = userService.getUserById(id);
        LOGGER.info("User got: ID={}, name={}, email={}",
                gotUser.getId(), gotUser.getName(), gotUser.getEmail());

        EntityModel<UserResponse> model = EntityModel.of(gotUser);
        model.add(linkTo(methodOn(UserController.class).getUser(id)).withSelfRel());
        model.add(linkTo(methodOn(UserController.class).updateUser(id, null)).withRel("update"));
        model.add(linkTo(methodOn(UserController.class).deleteUser(id)).withRel("delete"));
        model.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"));

        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Удалить пользователя", description = "Удаляет пользователя по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно удален"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID пользователя", required = true)
            @PathVariable Long id) {

        LOGGER.info("Deleting user process: id={}", id);
        userService.deleteUser(id);
        LOGGER.info("User deleted: ID={}", id);

        return ResponseEntity.ok().build();
    }
}