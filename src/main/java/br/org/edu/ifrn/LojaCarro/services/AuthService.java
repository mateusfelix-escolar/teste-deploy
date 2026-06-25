package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.dto.LoginRequest;
import br.org.edu.ifrn.LojaCarro.dto.LoginResponse;
import br.org.edu.ifrn.LojaCarro.dto.SignUpRequest;
import br.org.edu.ifrn.LojaCarro.model.Role;
import br.org.edu.ifrn.LojaCarro.model.User;
import br.org.edu.ifrn.LojaCarro.repository.RoleRepository;
import br.org.edu.ifrn.LojaCarro.repository.UserRepository;
import br.org.edu.ifrn.LojaCarro.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Transactional
    public LoginResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String token = jwtTokenProvider.generateToken(authentication);

        return new LoginResponse(token, user.getId(), user.getUsername(), user.getEmail(), user.getFullName());
    }

    @Transactional
    public LoginResponse registerUser(SignUpRequest signUpRequest) {
        // Validar se usuário já existe
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new RuntimeException("Erro: Nome de usuário já existe!");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new RuntimeException("Erro: Email já existe!");
        }

        // Criar novo usuário
        User user = new User(
                signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                passwordEncoder.encode(signUpRequest.getPassword()),
                signUpRequest.getFullName()
        );

        // Atribuir role padrão (VENDEDOR)
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(Role.RoleType.ROLE_VENDEDOR)
                .orElseThrow(() -> new RuntimeException("Role ROLE_VENDEDOR não encontrada"));
        roles.add(userRole);
        user.setRoles(roles);

        User registeredUser = userRepository.save(user);

        // Gerar token
        String token = jwtTokenProvider.generateTokenFromUsername(registeredUser.getUsername());

        return new LoginResponse(token, registeredUser.getId(), registeredUser.getUsername(),
                registeredUser.getEmail(), registeredUser.getFullName());
    }

    @Transactional
    public void registerUserWithRole(SignUpRequest signUpRequest, String roleName) {
        // Validar se usuário já existe
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new RuntimeException("Erro: Nome de usuário já existe!");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new RuntimeException("Erro: Email já existe!");
        }

        // Criar novo usuário
        User user = new User(
                signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                passwordEncoder.encode(signUpRequest.getPassword()),
                signUpRequest.getFullName()
        );

        // Atribuir role especificada
        Set<Role> roles = new HashSet<>();
        Role.RoleType roleType = Role.RoleType.valueOf(roleName);
        Role role = roleRepository.findByName(roleType)
                .orElseThrow(() -> new RuntimeException("Role " + roleName + " não encontrada"));
        roles.add(role);
        user.setRoles(roles);

        userRepository.save(user);
    }
}
