package de.hhu.propra.chicken.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import java.util.HashSet;
import java.util.Set;

@Configuration
@EnableWebSecurity(debug = false)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {


    @Value("${chicken.roles.tutor}")
    private Set<String> tutors;

    @Value("${chicken.roles.organizer}")
    private Set<String> organizers;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests(a -> a
                        .antMatchers("/", "/error", "/css/**", "/img/**").permitAll()
                        .anyRequest().authenticated()
                )
                .csrf(c -> c.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .logout(l -> l.logoutSuccessUrl("/").permitAll())
                .oauth2Login();
    }

    @Bean
    OAuth2UserService<OAuth2UserRequest, OAuth2User> createUserService() {
        DefaultOAuth2UserService defaultService = new DefaultOAuth2UserService();
        return userRequest -> {
            OAuth2User oauth2User = defaultService.loadUser(userRequest);

            var attributes = oauth2User.getAttributes(); //keep existing attributes

            var authorities = new HashSet<GrantedAuthority>();

            String login = attributes.get("login").toString();

            if (organizers.contains(login)) {
                System.out.printf("GRANTING ORGANISATOR PRIVILEGES TO USER %s%n", login);
                authorities.add(new SimpleGrantedAuthority("ROLE_ORGANIZER"));
            }else if (tutors.contains(login)) {
                System.out.printf("GRANTING TUTOR PRIVILEGES TO USER %s%n", login);
                authorities.add(new SimpleGrantedAuthority("ROLE_TUTOR"));
            } else {
                System.out.printf("GRANTING STUDENT PRIVILEGES TO USER %s%n", login);
                authorities.add(new SimpleGrantedAuthority("ROLE_STUDENT"));
            }
            return new DefaultOAuth2User(authorities, attributes, "login");
        };
    }
}
