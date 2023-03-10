package ru.nk.grooming.users;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.nk.grooming.authentication.routes.components.AuthService;
import ru.nk.grooming.general.RequestFunctions;
import ru.nk.grooming.types.ResponseWithStatus;
import ru.nk.grooming.types.StatusCode;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepo userRepo;
    private final AuthService authService;
    private final RequestFunctions requestFunctions;

    public StatusCode change(User user, HttpServletRequest request) {
        User dbUser = authService.getUserByHttpRequest(request);

        if (dbUser == null) {
            return StatusCode.create(403);
        }

        dbUser.mergeUser(user);
        userRepo.save(dbUser);
        return StatusCode.create(200);
    }

    public StatusCode deleteById(HttpServletRequest request) {
        User dbUser = authService.getUserByHttpRequest(request);

        if (dbUser == null) {
            return StatusCode.create(403);
        }

        userRepo.deleteById(dbUser.getId());
        return StatusCode.create(200);
    }

    public ResponseWithStatus<User> findById(Long id, HttpServletRequest request) {
        return requestFunctions.findByWithAuth(id, userRepo::findById, request);
    }

    public ResponseWithStatus<User> findByEmail(String email, HttpServletRequest request) {
        return requestFunctions.findByWithAuth(email, userRepo::findByEmail, request);
    }

    public Iterable<User> findAll(HttpServletRequest request) {
        return requestFunctions.findAllWithAuth(userRepo::findAll, request);
    }
}
