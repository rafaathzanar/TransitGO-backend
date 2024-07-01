package bugBust.transitgo.auth;

import bugBust.transitgo.config.JwtService;
import bugBust.transitgo.exception.EmailAlreadyExistException;
import bugBust.transitgo.exception.InvalidEmailOrPasswordException;
import bugBust.transitgo.model.Role;
import bugBust.transitgo.model.User;
import bugBust.transitgo.repository.UserRepository;
import bugBust.transitgo.services.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository repository;
    @Autowired
    private EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;



    public AuthenticationResponse register(RegisterRequest request) throws EmailAlreadyExistException {

        if (repository.existsByEmail(request.getEmail())){
            throw new EmailAlreadyExistException("Email already in use");
        }

        if (repository.existsByBusid(request.getBusid())){
            throw new EmailAlreadyExistException("Bus already assigned");
        }

        // Get the user's role from the request
            String userRole = request.getType();
            Role role = null;
        switch (userRole.toLowerCase()) {
            case "admin" -> role = Role.admin;
            case "employee" -> role = Role.employee;
            default -> role = Role.passenger;
        }
            //Role role = Role.valueOf((userRole.equals("employee")) ?  "employee" : "passenger");

        var user = User.builder()
                .fname(request.getFname())
                .lname(request.getLname())
                .email(request.getEmail())
                .phone(request.getPhone())
                .uname(request.getUname())
                .busid(request.getBusid())
                .password(passwordEncoder.encode(request.getPassword()))
                .type(role)
                .enabled(false)//email.v
                .build();
        repository.save(user);

        if (role == Role.passenger){
            //generate verification token and send email
            String token = UUID.randomUUID().toString();
            user.setVerificationToken(token);
            repository.save(user);

            String confirmationURL = "http://localhost:3000/verify-email?token="+token;
            emailService.sendEmail(user.getEmail(),"Email Verification", "Click the link to verify your email :"+confirmationURL);
        }

        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) throws InvalidEmailOrPasswordException {
    try {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                ));
    }catch (BadCredentialsException e) {
        throw new InvalidEmailOrPasswordException("Invalid Email or Password");
    }
        var user = repository.findByEmail(request.getEmail())
                .orElseThrow(()->new InvalidEmailOrPasswordException("Invalid Email or Password"));

        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .user(user)
                .build();
    }

    public String validateVerificationToken(String token){
        User user = repository.findByVerificationToken(token).orElse(null);
        if (user == null){
            return "invalid";
        }

        user.setEnabled(true);
        user.setVerificationToken(null);
        repository.save(user);
        return "valid";
    }
}
