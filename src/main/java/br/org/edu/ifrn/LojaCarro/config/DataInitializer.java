package br.org.edu.ifrn.LojaCarro.config;

import br.org.edu.ifrn.LojaCarro.model.Role;
import br.org.edu.ifrn.LojaCarro.model.User;
import br.org.edu.ifrn.LojaCarro.repository.RoleRepository;
import br.org.edu.ifrn.LojaCarro.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Inicializar roles se não existirem
        if (roleRepository.findByName(Role.RoleType.ROLE_ADMIN).isEmpty()) {
            roleRepository.save(new Role(Role.RoleType.ROLE_ADMIN));
        }
        if (roleRepository.findByName(Role.RoleType.ROLE_GERENTE).isEmpty()) {
            roleRepository.save(new Role(Role.RoleType.ROLE_GERENTE));
        }
        if (roleRepository.findByName(Role.RoleType.ROLE_VENDEDOR).isEmpty()) {
            roleRepository.save(new Role(Role.RoleType.ROLE_VENDEDOR));
        }
        if (roleRepository.findByName(Role.RoleType.ROLE_CLIENTE).isEmpty()) {
            roleRepository.save(new Role(Role.RoleType.ROLE_CLIENTE));
        }

        // Criar usuários de teste se não existirem
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User("admin", "admin@example.com", 
                    passwordEncoder.encode("admin123"), "Administrador");
            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(roleRepository.findByName(Role.RoleType.ROLE_ADMIN).orElseThrow());
            admin.setRoles(adminRoles);
            userRepository.save(admin);
        }

        if (!userRepository.existsByUsername("gerente")) {
            User gerente = new User("gerente", "gerente@example.com", 
                    passwordEncoder.encode("gerente123"), "Gerente da Loja");
            Set<Role> gerenteRoles = new HashSet<>();
            gerenteRoles.add(roleRepository.findByName(Role.RoleType.ROLE_GERENTE).orElseThrow());
            gerente.setRoles(gerenteRoles);
            userRepository.save(gerente);
        }

        if (!userRepository.existsByUsername("vendedor")) {
            User vendedor = new User("vendedor", "vendedor@example.com", 
                    passwordEncoder.encode("vendedor123"), "Vendedor");
            Set<Role> vendedorRoles = new HashSet<>();
            vendedorRoles.add(roleRepository.findByName(Role.RoleType.ROLE_VENDEDOR).orElseThrow());
            vendedor.setRoles(vendedorRoles);
            userRepository.save(vendedor);
        }

        if (!userRepository.existsByUsername("cliente")) {
            User cliente = new User("cliente", "cliente@example.com", 
                    passwordEncoder.encode("cliente123"), "Cliente");
            Set<Role> clienteRoles = new HashSet<>();
            clienteRoles.add(roleRepository.findByName(Role.RoleType.ROLE_CLIENTE).orElseThrow());
            cliente.setRoles(clienteRoles);
            userRepository.save(cliente);
        }
    }
}
