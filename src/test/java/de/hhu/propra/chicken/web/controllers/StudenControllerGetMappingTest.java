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
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudentController.class)
@Import({MethodSecurityConfiguration.class})
@TestPropertySource(properties = {"spring.config.location=classpath:application-test.yml"})
public class StudenControllerGetMappingTest {

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
        studentSession.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                new SecurityContextImpl(student)
        );
    }

    @Test
    @DisplayName("User wird auf GithubLogin weitergeleitet")
    void test1() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/oauth2/authorization/github"));
    }

    @Test
    @DisplayName("Studentenseite erreichbar")
    void test2() throws Exception {
        mockMvc.perform(get("/student").session(studentSession).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Urlaubanlegeseite erreichbar")
    void test3() throws Exception {
        mockMvc.perform(get("/vacationRegistration").session(studentSession).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Klausuranlegeseite erreichbar")
    void test4() throws Exception {
        mockMvc.perform(get("/createExam").session(studentSession).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Klausurregistierungsseite erreichbar")
    void test5() throws Exception {
        mockMvc.perform(get("/examRegistration").session(studentSession).with(csrf()))
                .andExpect(status().isOk());
    }
}
