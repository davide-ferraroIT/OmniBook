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
                    .firstName("Amministratore")
                    .lastName("Root")
                    .phone("+39000000000")
                    .email("root@root.it")
                    .password(passwordEncoder.encode("root"))
                    .role(Role.ADMIN)
                    .tenant(null)
                    .build();
            userRepository.save(rootUser);
            log.info("Utente root/root creato con successo.");
        }

        if (!userRepository.existsByEmail("davide@example.it")) {
            User davideUser = User.builder()
                    .firstName("Davide")
                    .lastName("Ferraro")
                    .phone("+39111111111")
                    .email("davide@example.it")
                    .password(passwordEncoder.encode("davide"))
                    .role(Role.ADMIN)
                    .tenant(null)
                    .build();
            userRepository.save(davideUser);
            log.info("Utente davide/davide creato con successo.");
        }

        if (tenantRepository.count() > 0) {
            log.info("Database già popolato, skip seeding.");
            return;
        }


        log.info("Inizio seeding dati fittizi per l'ambiente di test...");

        List<DaySchedule> businessHours = List.of(
                new DaySchedule(DayOfWeek.MONDAY, false, List.of()),
                new DaySchedule(DayOfWeek.TUESDAY, true, List.of(new com.davideferraroit.omnibook.backend.model.tenant.config.TimeSlot(LocalTime.of(9, 0), LocalTime.of(13, 0)), new com.davideferraroit.omnibook.backend.model.tenant.config.TimeSlot(LocalTime.of(15, 0), LocalTime.of(19, 0)))),
                new DaySchedule(DayOfWeek.WEDNESDAY, true, List.of(new com.davideferraroit.omnibook.backend.model.tenant.config.TimeSlot(LocalTime.of(9, 0), LocalTime.of(13, 0)), new com.davideferraroit.omnibook.backend.model.tenant.config.TimeSlot(LocalTime.of(15, 0), LocalTime.of(19, 0)))),
                new DaySchedule(DayOfWeek.THURSDAY, true, List.of(new com.davideferraroit.omnibook.backend.model.tenant.config.TimeSlot(LocalTime.of(9, 0), LocalTime.of(13, 0)), new com.davideferraroit.omnibook.backend.model.tenant.config.TimeSlot(LocalTime.of(15, 0), LocalTime.of(19, 0)))),
                new DaySchedule(DayOfWeek.FRIDAY, true, List.of(new com.davideferraroit.omnibook.backend.model.tenant.config.TimeSlot(LocalTime.of(9, 0), LocalTime.of(19, 0)))),
                new DaySchedule(DayOfWeek.SATURDAY, true, List.of(new com.davideferraroit.omnibook.backend.model.tenant.config.TimeSlot(LocalTime.of(9, 0), LocalTime.of(13, 0)))),
                new DaySchedule(DayOfWeek.SUNDAY, false, List.of())
        );

        List<DaySchedule> gommistaHours = List.of(
                new DaySchedule(DayOfWeek.MONDAY, true, List.of(new com.davideferraroit.omnibook.backend.model.tenant.config.TimeSlot(LocalTime.of(8, 0), LocalTime.of(12, 30)), new com.davideferraroit.omnibook.backend.model.tenant.config.TimeSlot(LocalTime.of(14, 0), LocalTime.of(18, 0)))),
                new DaySchedule(DayOfWeek.TUESDAY, true, List.of(new com.davideferraroit.omnibook.backend.model.tenant.config.TimeSlot(LocalTime.of(8, 0), LocalTime.of(12, 30)), new com.davideferraroit.omnibook.backend.model.tenant.config.TimeSlot(LocalTime.of(14, 0), LocalTime.of(18, 0)))),
                new DaySchedule(DayOfWeek.WEDNESDAY, true, List.of(new com.davideferraroit.omnibook.backend.model.tenant.config.TimeSlot(LocalTime.of(8, 0), LocalTime.of(12, 30)), new com.davideferraroit.omnibook.backend.model.tenant.config.TimeSlot(LocalTime.of(14, 0), LocalTime.of(18, 0)))),
                new DaySchedule(DayOfWeek.THURSDAY, true, List.of(new com.davideferraroit.omnibook.backend.model.tenant.config.TimeSlot(LocalTime.of(8, 0), LocalTime.of(12, 30)), new com.davideferraroit.omnibook.backend.model.tenant.config.TimeSlot(LocalTime.of(14, 0), LocalTime.of(18, 0)))),
                new DaySchedule(DayOfWeek.FRIDAY, true, List.of(new com.davideferraroit.omnibook.backend.model.tenant.config.TimeSlot(LocalTime.of(8, 0), LocalTime.of(12, 30)), new com.davideferraroit.omnibook.backend.model.tenant.config.TimeSlot(LocalTime.of(14, 0), LocalTime.of(18, 0)))),
                new DaySchedule(DayOfWeek.SATURDAY, true, List.of(new com.davideferraroit.omnibook.backend.model.tenant.config.TimeSlot(LocalTime.of(8, 0), LocalTime.of(12, 0)))),
                new DaySchedule(DayOfWeek.SUNDAY, false, List.of())
        );

        TenantConfig gommistaConfig = new TenantConfig(
                "#DC2626", // Red 600
                new Terminology("Gommista", "Intervento", "Prenotazione"),
                List.of(),
                Set.of(),
                null,
                gommistaHours,
                true,  // allowAutoAssignment = true, auto-assegna il primo ponte libero
                true,   // autoAcceptBookings = true
                List.of() // holidays
        );

        TenantConfig barberConfig = new TenantConfig(
                "#1E3A8A", // Blue 900
                new Terminology("Barbiere", "Trattamento", "Appuntamento"),
                List.of(),
                Set.of(),
                null,
                businessHours,
                false, // allowAutoAssignment = false, il cliente deve scegliere il barbiere
                false, // autoAcceptBookings = false, l'admin deve accettarle a mano
                List.of() // holidays
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

        Tenant gommista = Tenant.builder()
                .name("Gommista Rossi")
                .slug("gommista-rossi")
                .config(gommistaConfig)
                .build();
        tenantRepository.save(gommista);

        Resource ponte1 = Resource.builder()
                .tenant(gommista)
                .name("Ponte Sollevatore 1")
                .capacity(1)
                .build();

        Resource ponte2 = Resource.builder()
                .tenant(gommista)
                .name("Ponte Sollevatore 2")
                .capacity(1)
                .build();
                
        resourceRepository.saveAll(List.of(ponte1, ponte2));

        Service cambioGomme = Service.builder()
                .tenant(gommista)
                .name("Cambio Gomme")
                .durationMinutes(45)
                .allowedResources(Set.of(ponte1, ponte2))
                .build();

        Service equilibratura = Service.builder()
                .tenant(gommista)
                .name("Equilibratura e Convergenza")
                .durationMinutes(30)
                .allowedResources(Set.of(ponte1, ponte2))
                .build();

        serviceRepository.saveAll(List.of(cambioGomme, equilibratura));

        User barberAdmin = User.builder()
                .firstName("Marco")
                .lastName("Rossi")
                .phone("+393331234567")
                .email("barbiere@example.it")
                .password(passwordEncoder.encode("password"))
                .role(Role.SHOP)
                .tenant(barberia)
                .build();

        User gommistaAdmin = User.builder()
                .firstName("Luigi")
                .lastName("Bianchi")
                .phone("+393339876543")
                .email("gommista@example.it")
                .password(passwordEncoder.encode("password"))
                .role(Role.SHOP)
                .tenant(gommista)
                .build();

        User customer = User.builder()
                .firstName("Mario")
                .lastName("Verdi")
                .phone("+393331122334")
                .email("cliente@example.it")
                .password(passwordEncoder.encode("password"))
                .role(Role.CUSTOMER)
                .tenant(gommista)
                .build();

        userRepository.saveAll(List.of(barberAdmin, gommistaAdmin, customer));

        log.info("Seeding completato. Slug creati: barberia-marco, gommista-rossi");
    }
}
