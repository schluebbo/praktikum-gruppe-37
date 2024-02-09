package de.hhu.propra.chicken.web.controllers;

import de.hhu.propra.chicken.configuration.MethodSecurityConfiguration;
import de.hhu.propra.chicken.services.ExamService;
import de.hhu.propra.chicken.services.LogService;
import de.hhu.propra.chicken.services.StudentService;
import de.hhu.propra.chicken.services.values.PropertiesValues;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Calendar.MONDAY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import({MethodSecurityConfiguration.class})
public class SecurityTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    PropertiesValues mockedPropertiesValues;

    @MockBean
    ExamService examService;
    @MockBean
    StudentService studentService;
    @MockBean
    LogService logService;

    MockHttpSession studentSession = new MockHttpSession();
    MockHttpSession tutorSession = new MockHttpSession();
    MockHttpSession organizerSession = new MockHttpSession();
    MockHttpSession unknownSession = new MockHttpSession();

    public static OAuth2AuthenticationToken buildPrincipal(String name, Long githubID, String... roles) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("login", name);
        attributes.put("id", githubID);
        List<GrantedAuthority> authorities = Arrays.stream(roles)
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.toUpperCase()))
                .collect(Collectors.toList());
        OAuth2User user = new DefaultOAuth2User(authorities, attributes, "login");
        return new OAuth2AuthenticationToken(user, authorities, "whatever");
    }

    @BeforeEach
    void init(){
        OAuth2AuthenticationToken student = buildPrincipal("Simon",31514L ,"student");
        OAuth2AuthenticationToken tutor = buildPrincipal("Simon",31514L ,"tutor");
        OAuth2AuthenticationToken organizer = buildPrincipal("Simon",31514L ,"organizer");
        studentSession.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                new SecurityContextImpl(student)
        );
        tutorSession.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                new SecurityContextImpl(tutor)
        );
        organizerSession.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                new SecurityContextImpl(organizer)
        );
    }

    @Test
    @DisplayName("User wird auf GithubLogin weitergeleitet")
    void test1() throws Exception {
        mockMvc.perform(get("/").session(unknownSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/oauth2/authorization/github"));
    }

    @Test
    @DisplayName("Student wird auf /student weitergeleitet")
    void test2() throws Exception {
        mockMvc.perform(get("/").session(studentSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/student"));
    }

    @Test
    @DisplayName("Tutor wird auf /tutor weitergeleitet")
    void test3() throws Exception {
        mockMvc.perform(get("/").session(tutorSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tutor"));
    }

    @Test
    @DisplayName("Organisator wird auf /organizer weitergeleitet")
    void test4() throws Exception {
        mockMvc.perform(get("/").session(organizerSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/organizer"));
    }

    @Test
    @DisplayName("User darf auf keine Seite zugreifen")
    void test5() throws Exception {
        mockMvc.perform(get("/organizer").session(unknownSession))
                .andExpect(status().isFound());
    }

    @Test
    @DisplayName("User darf auf keine Seite zugreifen")
    void test6() throws Exception {
        mockMvc.perform(get("/tutor").session(unknownSession))
                .andExpect(status().isFound());
    }

    @Test
    @DisplayName("User darf auf keine Seite zugreifen")
    void test7() throws Exception {
        mockMvc.perform(get("/student").session(unknownSession))
                .andExpect(status().isFound());
    }

    @Test
    @DisplayName("Student wird nicht auf /tutor weitergeleitet")
    void test8() throws Exception {
        mockMvc.perform(get("/tutor").session(studentSession))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Student wird nicht auf /organizer weitergeleitet")
    void test9() throws Exception {
        mockMvc.perform(get("/organizer").session(studentSession))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Tutor wird nicht auf /organizer weitergeleitet")
    void test10() throws Exception {
        mockMvc.perform(get("/organizer").session(tutorSession))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Organisator wird nicht auf /tutor weitergeleitet")
    void test11() throws Exception {
        mockMvc.perform(get("/tutor").session(organizerSession))
                .andExpect(status().isForbidden());
    }
}
