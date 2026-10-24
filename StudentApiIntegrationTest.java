package com.example.studentmanagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StudentApiIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String REGISTER = """
            {"name":"Rishi","email":"rishi@example.com","password":"Password@123"}""";
    private static final String LOGIN = """
            {"email":"rishi@example.com","password":"Password@123"}""";
    private static final String STUDENT = """
            {"name":"Aarav Sharma","email":"aarav@example.com","branch":"IT","year":2}""";
    private static final String STUDENT_UPDATED = """
            {"name":"Aarav Sharma","email":"aarav@example.com","branch":"Computer Engineering","year":3}""";

    @Test
    void fullFlow() throws Exception {
        // register + duplicate
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(REGISTER))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("rishi@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(REGISTER))
                .andExpect(status().isConflict());

        // wrong password
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"rishi@example.com\",\"password\":\"wrong-pass\"}"))
                .andExpect(status().isUnauthorized());

        // login
        String loginBody = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(loginBody).get("token").asText();
        String bearer = "Bearer " + token;

        // protected endpoints reject missing / bad tokens
        mvc.perform(get("/api/students")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/students").header("Authorization", "Bearer not.a.jwt"))
                .andExpect(status().isUnauthorized());

        // create (+ validation + duplicate)
        String created = mvc.perform(post("/api/students").header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON).content(STUDENT))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(created).get("id").asLong();

        mvc.perform(post("/api/students").header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON).content(STUDENT))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/students").header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"email\":\"bad\",\"branch\":\"IT\",\"year\":9}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.email").exists());

        // read
        mvc.perform(get("/api/students").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("aarav@example.com"));
        mvc.perform(get("/api/students/" + id).header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.branch").value("IT"));

        // update
        mvc.perform(put("/api/students/" + id).header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON).content(STUDENT_UPDATED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(3));

        // delete + not found
        mvc.perform(delete("/api/students/" + id).header("Authorization", bearer))
                .andExpect(status().isOk());
        mvc.perform(get("/api/students/" + id).header("Authorization", bearer))
                .andExpect(status().isNotFound());
    }
}
