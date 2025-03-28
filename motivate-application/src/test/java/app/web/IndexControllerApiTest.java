package app.web;

import app.comment.service.CommentService;
import app.post.service.PostService;
import app.security.AuthenticationMetaData;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static app.web.TestBuilder.aRandomUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IndexController.class)
@TestPropertySource(properties = {
        "logging.level.org.springframework=WARN",
        "logging.level.org.springframework.boot.autoconfigure=WARN"
})
class IndexControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private CommentService commentService;

    @Test
    void getIndex_ShouldReturnIndexView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpectAll(
                        status().isOk(),
                        view().name("index")
                );
    }

    @Test
    void getRequestToLoginEndpoint_ShouldReturnLoginView() throws Exception {

        MockHttpServletRequestBuilder request = get("/login");

                mockMvc.perform(request)
                        .andExpect(status().isOk())
                        .andExpect(view().name("login"))
                        .andExpect(model().attributeExists("loginRequest"));
    }

    @Test
    void getRequestToLoginEndpointWithErrorParameter_ShouldReturnLoginViewAndErrorMessageAttribute() throws Exception {
        MockHttpServletRequestBuilder request = get("/login").param("error", "");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginRequest", "errorMessage"));
    }

    @Test
    void postRequestToRegisterEndpoint_HappyPath() throws Exception {
        MockHttpServletRequestBuilder request = post("/register")
                .formField("username", "Hris123")
                .formField("password", "123456")
                .formField("country", "BULGARIA")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(userService, times(1)).register(any());
    }

    @Test
    void postRequestToRegisterEndpointWithInvalidData_returnRegisterView() throws Exception {
        MockHttpServletRequestBuilder request = post("/register")
                .formField("username", "")
                .formField("password", "")
                .formField("country", "BULGARIA")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("register"));

        verify(userService, never()).register(any());
    }

    @Test
    void getAuthenticatedRequestToHome_returnsHomeView() throws Exception {

        when(userService.getById(any(UUID.class))).thenReturn(aRandomUser());

        AuthenticationMetaData principal = new AuthenticationMetaData(UUID.randomUUID(), "User123", "123123", UserRole.USER, true);
        MockHttpServletRequestBuilder request = get("/home")
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("user"));

    }

    @Test
    void getAuthenticatedRequestToHome_redirectToLogin() throws Exception {
        MockHttpServletRequestBuilder request = get("/home");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection());
        verify(userService, never()).register(any());
    }

    @Test
    public void getRegisterPage_ShouldReturnRegisterView() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registerRequest"));
    }

    @Test
    public void getRegisterPage_ShouldContainRegisterRequestObject() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(model().attribute("registerRequest",
                        org.hamcrest.Matchers.instanceOf(RegisterRequest.class)));
    }

    @Test
    public void getRegisterPage_ShouldNotHaveErrors() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(model().hasNoErrors());
    }

    @Test
    public void getRegisterPage_ShouldHaveCorrectContentType() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(content().contentTypeCompatibleWith("text/html"));
    }

    @Test
    @WithAnonymousUser
    public void getRegisterPage_ShouldBeAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void getRegisterPage_ShouldBeAccessibleWithAuthentication() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk());
    }
}