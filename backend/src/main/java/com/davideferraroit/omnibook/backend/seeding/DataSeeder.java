package com.davideferraroit.omnibook.backend.seeding;

import com.davideferraroit.omnibook.backend.model.resource.Resource;
import com.davideferraroit.omnibook.backend.model.resource.ResourceRepository;
import com.davideferraroit.omnibook.backend.model.service.Service;
import com.davideferraroit.omnibook.backend.model.service.ServiceRepository;
import com.davideferraroit.omnibook.backend.model.tenant.Tenant;
import com.davideferraroit.omnibook.backend.model.tenant.TenantRepository;
import com.davideferraroit.omnibook.backend.model.tenant.config.DaySchedule;
import com.davideferraroit.omnibook.backend.model.tenant.config.TenantConfig;
import com.davideferraroit.omnibook.backend.model.tenant.config.Terminology;
import com.davideferraroit.omnibook.backend.model.auth.User;
import com.davideferraroit.omnibook.backend.model.auth.UserRepository;
import com.davideferraroit.omnibook.backend.model.auth.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final TenantRepository tenantRepository;
    private final ResourceRepository resourceRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByEmail("root@root.it")) {
            User rootUser = User.builder()
                    .email("root@root.it")
                    .password(passwordEncoder.encode("root"))
                    .role(Role.SUPER_ADMIN)
                    .tenantId(null)
                    .build();
            userRepository.save(rootUser);
            log.info("Utente root/root creato con successo.");
        }

        if (tenantRepository.count() > 0) {
            log.info("Database già popolato, skip seeding.");
            return;
        }

        log.info("Inizio seeding dati fittizi per l'ambiente di test...");

        List<DaySchedule> businessHours = List.of(
                new DaySchedule(DayOfWeek.MONDAY, false, null, null),
                new DaySchedule(DayOfWeek.TUESDAY, true, LocalTime.of(9, 0), LocalTime.of(18, 0)),
                new DaySchedule(DayOfWeek.WEDNESDAY, true, LocalTime.of(9, 0), LocalTime.of(18, 0)),
                new DaySchedule(DayOfWeek.THURSDAY, true, LocalTime.of(9, 0), LocalTime.of(18, 0)),
                new DaySchedule(DayOfWeek.FRIDAY, true, LocalTime.of(9, 0), LocalTime.of(18, 0)),
                new DaySchedule(DayOfWeek.SATURDAY, true, LocalTime.of(9, 0), LocalTime.of(13, 0)),
                new DaySchedule(DayOfWeek.SUNDAY, false, null, null)
        );

        TenantConfig barberConfig = new TenantConfig(
                "#1E3A8A", // Blue 900
                new Terminology("Barbiere", "Trattamento", "Appuntamento"),
                List.of(),
                Set.of(),
                null,
                businessHours,
                false, // allowAutoAssignment = false, il cliente deve scegliere il barbiere
                false  // autoAcceptBookings = false, l'admin deve accettarle a mano
        );

        Tenant barberia = Tenant.builder()
                .name("Barberia Da Marco")
                .slug("barberia-marco")
                .config(barberConfig)
                .build();
        tenantRepository.save(barberia);

        Resource marco = Resource.builder()
                .tenant(barberia)
                .name("Marco (Titolare)")
                .capacity(1)
                .build();

        Resource luca = Resource.builder()
                .tenant(barberia)
                .name("Luca (Apprendista)")
                .capacity(1)
                .build();
                
        resourceRepository.saveAll(List.of(marco, luca));

        Service taglio = Service.builder()
                .tenant(barberia)
                .name("Taglio Capelli")
                .durationMinutes(30)
                .allowedResources(Set.of(marco, luca))
                .build();

        Service barba = Service.builder()
                .tenant(barberia)
                .name("Regolazione Barba")
                .durationMinutes(20)
                .allowedResources(Set.of(marco)) // Solo Marco fa la barba
                .build();

        serviceRepository.saveAll(List.of(taglio, barba));

        log.info("Seeding completato. Slug tenant: barberia-marco");
    }
}
