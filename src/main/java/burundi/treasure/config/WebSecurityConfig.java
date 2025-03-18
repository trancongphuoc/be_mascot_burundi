package burundi.treasure.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import burundi.treasure.jwt.JwtFilter;
import burundi.treasure.service.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {


//    @Autowired
    private UserDetailsServiceImpl userService;

    @Autowired
    public WebSecurityConfig(UserDetailsServiceImpl userService) {
        this.userService = userService;
    }

    @Bean
    public JwtFilter jwtAuthenticationFilter() {
        return new JwtFilter();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Password encoder, để Spring Security sử dụng mã hóa mật khẩu người dùng
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Cấu hình CORS
                .csrf(AbstractHttpConfigurer::disable) // Tắt CSRF
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/","/log","/api/auth/*", "/Register", "/Cancel", "/Renew", "/Reward", "/PlayOneTime", "/api/admin/*", "/actuator/*","/api/user/*","/api/mascot/*","api/sp_app/*").permitAll() // Cho phép các URL này truy cập tự do

                        .anyRequest().authenticated() // Các URL khác yêu cầu xác thực
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))// Không sử dụng session cho việc lưu trữ trạng thái

                .formLogin(formLogin -> formLogin
                        .defaultSuccessUrl("/cms", true)
                        .failureUrl("/login?error=true")
                        .permitAll() // Cho phép tất cả mọi người truy cập vào trang login
                )
//                .logout(LogoutConfigurer::permitAll); // Cho phép tất cả mọi người truy cập vào trang logout
                .logout(logout -> logout
                        .logoutUrl("/api/logout") // Định nghĩa URL logout
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(200);
                            response.getWriter().write("Logout successful!");
                        }) // Xử lý khi logout thành công
                        .invalidateHttpSession(true) // Hủy session
                        .deleteCookies("JSESSIONID") // Xóa cookie
                );

        // Thêm Filter kiểm tra JWT trước khi xử lý authentication
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.applyPermitDefaultValues();
        source.registerCorsConfiguration("/**", corsConfig);
        return source;
    }
}
