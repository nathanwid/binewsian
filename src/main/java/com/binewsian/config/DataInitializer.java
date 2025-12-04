package com.binewsian.config;

import com.binewsian.enums.Role;
import com.binewsian.model.User;
import com.binewsian.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Cek apakah sudah ada data
        if (userRepository.count() == 0) {
            // Buat user dummy untuk testing
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@example.com");
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);

            User contributor = new User();
            contributor.setUsername("contributor");
            contributor.setPassword(passwordEncoder.encode("contributor123"));
            contributor.setEmail("contributor@example.com");
            contributor.setRole(Role.CONTRIBUTOR);
            userRepository.save(contributor);

            User user = new User();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setEmail("user@example.com");
            user.setRole(Role.USER);
            userRepository.save(user);

            log.info("Data dummy berhasil diinisialisasi!");
        }
    }
}
