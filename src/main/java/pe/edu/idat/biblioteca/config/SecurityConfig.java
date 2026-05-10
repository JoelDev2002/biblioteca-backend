package pe.edu.idat.biblioteca.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pe.edu.idat.biblioteca.constants.ApiConstants;
import pe.edu.idat.biblioteca.security.CustomAccessDeniedHandler;
import pe.edu.idat.biblioteca.security.JwtAuthEntryPoint;
import pe.edu.idat.biblioteca.security.JwtAuthFilter;
import pe.edu.idat.biblioteca.service.impl.UserDetailServiceImpl;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthEntryPoint jwtAuthEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final UserDetailServiceImpl userDetailsService;
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }


    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;

    }


    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity)throws Exception{
        httpSecurity
                .csrf(csrf->csrf.disable())
                .authorizeHttpRequests(auth->auth
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/v1/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/usuario").permitAll()
                        .requestMatchers("/v1/rol/**").hasRole(ApiConstants.ADMIN)
                        .requestMatchers(HttpMethod.POST,"/v1/libro","/v1/prestamo").hasRole(ApiConstants.ADMIN)
                        .requestMatchers(HttpMethod.GET,"/v1/prestamo","/v1/prestamo/{id}","/v1/prestamo/devolucion/{id}").hasRole(ApiConstants.ADMIN)
                        .requestMatchers(HttpMethod.GET,ApiConstants.LIBRO_BY_ID,ApiConstants.USUARIO_BY_ID,"/v1/prestamo/usuario/{idUsuario}").hasAnyRole(ApiConstants.ADMIN,ApiConstants.ADMIN)
                        .requestMatchers(HttpMethod.PUT,ApiConstants.LIBRO_BY_ID,ApiConstants.USUARIO_BY_ID).hasAnyRole(ApiConstants.ADMIN,ApiConstants.USER)
                        .requestMatchers(HttpMethod.DELETE,ApiConstants.LIBRO_BY_ID,ApiConstants.USUARIO_BY_ID).hasRole(ApiConstants.ADMIN)
                        .anyRequest().authenticated())
                        .exceptionHandling(ex->ex
                                .accessDeniedHandler(accessDeniedHandler)
                                .authenticationEntryPoint(jwtAuthEntryPoint)
                        );
        httpSecurity.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        httpSecurity.formLogin(form->form.disable());

        return httpSecurity.build();
    }
}
