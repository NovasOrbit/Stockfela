package com.application.stockfela.config;

import com.application.stockfela.entity.Role;
import com.application.stockfela.entity.Role.RoleName;
import com.application.stockfela.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoleSeeder {

    @Bean
    CommandLineRunner seedRoles(RoleRepository roleRepository) {
        return args -> {

            if (roleRepository.findByName(RoleName.ROLE_USER).isEmpty()) {
                roleRepository.save(new Role(RoleName.ROLE_USER));
            }

            if (roleRepository.findByName(RoleName.ROLE_ADMIN).isEmpty()) {
                roleRepository.save(new Role(RoleName.ROLE_ADMIN));
            }
        };
    }
}