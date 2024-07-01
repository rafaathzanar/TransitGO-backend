package bugBust.transitgo.auth;

import bugBust.transitgo.exception.EmailAlreadyExistException;
import bugBust.transitgo.exception.InvalidEmailOrPasswordException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000","http://192.168.8.156:8081"})
public class AuthenticationController {

    private final AuthenticationService service;



    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
        @RequestBody RegisterRequest request
    ) throws EmailAlreadyExistException {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/authentication")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        //try {
        try {
            return ResponseEntity.ok(service.authenticate(request));
        } catch (InvalidEmailOrPasswordException e) {
            throw new RuntimeException(e);
        }
        // } catch (InvalidEmailOrPasswordException e) {
           // return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthenticationResponse(e.getMessage()));
        //}
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam("token")String token){
        String result = service.validateVerificationToken(token);
        if (result.equals("valid")){
            return ResponseEntity.ok("Verified");
        }else {
            return ResponseEntity.ok("Verify-Email");
        }
    }
}
