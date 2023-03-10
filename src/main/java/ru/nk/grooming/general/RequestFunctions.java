package ru.nk.grooming.general;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.nk.grooming.authentication.routes.components.AuthService;
import ru.nk.grooming.types.ResponseWithStatus;
import ru.nk.grooming.types.StatusCode;

import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class RequestFunctions {
    private final AuthService authService;

    private boolean isNotAdmin(HttpServletRequest request) {
        return authService.isNotAdmin(request);
    }

    public <ObjectType, PropType> ResponseWithStatus<ObjectType> findBy(
            PropType property,
            Function<PropType, Optional<ObjectType>> findFunction
    ) {
        ObjectType object = findFunction.apply(property).orElse(null);
        if (object == null) {
            return ResponseWithStatus.<ObjectType>builder()
                    .status(404)
                    .data(null)
                    .build();
        }

        return ResponseWithStatus.<ObjectType>builder()
                .status(200)
                .data(object)
                .build();
    }

    public <ObjectType, PropType> ResponseWithStatus<ObjectType> findByWithAuth(
            PropType property,
            Function<PropType, Optional<ObjectType>> findFunction,
            HttpServletRequest request
    ) {
        if (isNotAdmin(request)) {
            return ResponseWithStatus.<ObjectType>builder()
                    .status(403)
                    .data(null)
                    .build();
        }

        return findBy(property, findFunction);
    }
    public <ObjectType> Iterable<ObjectType> findAllWithAuth(
            FindAll<ObjectType> findAll,
            HttpServletRequest request
    ) {
        if (isNotAdmin(request)) {
            return null;
        }

        return findAll.apply();
    }
    public <ObjectType, PropType> Iterable<ObjectType> findAllByWithAuth(
            PropType property,
            Function<PropType, Iterable<ObjectType>> findAllBy,
            HttpServletRequest request
    ) {
        if (isNotAdmin(request)) {
            return null;
        }

        return findAllBy.apply(property);
    }

    public <ObjectType, PropType> StatusCode save(
            ObjectType object,
            PropType property,
            Function<PropType, Optional<ObjectType>> findFunction,
            Function<ObjectType, ObjectType> saveFunction,
            HttpServletRequest request
    ) {
        if (isNotAdmin(request)) {
            return StatusCode.create(403);
        }

        if (findBy(property, findFunction).getStatus() == 404) {
            saveFunction.apply(object);
            return StatusCode.create(200);
        }

        return StatusCode.create(409);
    }

    public <ObjectType, PropType> StatusCode deleteBy(
            PropType property,
            Function<PropType, Optional<ObjectType>> findFunction,
            Runnable deleteFunction,
            HttpServletRequest request
    ) {
        if (isNotAdmin(request)) {
            return StatusCode.create(403);
        }

        if (findFunction.apply(property).orElse(null) == null) {
            return StatusCode.create(404);
        }

        deleteFunction.run();
        return StatusCode.create(200);
    }
}
