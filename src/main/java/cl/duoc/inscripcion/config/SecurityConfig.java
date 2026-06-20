package cl.duoc.inscripcion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Desactivar CSRF (API REST stateless no lo necesita)
            .csrf(csrf -> csrf.disable())

            // Sin sesiones HTTP - cada request debe traer su JWT
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Reglas de autorización
            .authorizeHttpRequests(auth -> auth
                // Endpoint de salud público (para verificar que la app levanta)
                .requestMatchers("/").permitAll()
                // Todo lo demás requiere JWT válido
                .anyRequest().authenticated()
            )

            // Validar tokens JWT usando el issuer configurado en application.properties
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt -> {})
            );

        return http.build();
    }
}