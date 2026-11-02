package ru.tusman4ik.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.tusman4ik.taskimpl.t4.Generator_4;

import java.util.Random;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestPropertySource(properties = "logging.level.ru.tusman4ik=WARN")
class TaskControllerTest {

    private static final String ID = """
            "id":{"exam":"OGE","subject":"CS","number":4,"prototype":0}""";

    private MockMvc mvc;

    @BeforeEach
    void setup(@Autowired WebApplicationContext wac) {
        mvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    void genReturnsFullShape() throws Exception {
        mvc.perform(post("/v1/tasks/gen")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" + ID + ",\"seed\":2000}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id.exam").value("OGE"))
                .andExpect(jsonPath("$.id.subject").value("CS"))
                .andExpect(jsonPath("$.id.number").value(4))
                .andExpect(jsonPath("$.id.prototype").value(0))
                .andExpect(jsonPath("$.seed").value(2000))
                .andExpect(jsonPath("$.statement").isNotEmpty())
                .andExpect(jsonPath("$.data").isMap())
                .andExpect(jsonPath("$.inputForm.type").value("int-form-0"));
    }

    @Test
    void genWithoutSeedAssignsOne() throws Exception {
        mvc.perform(post("/v1/tasks/gen")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" + ID + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seed").isNumber());
    }

    @Test
    void genUnknownPrototypeIs404() throws Exception {
        mvc.perform(post("/v1/tasks/gen")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":{"exam":"OGE","subject":"CS","number":4,"prototype":99}}"""))
                .andExpect(status().isNotFound());
    }

    @Test
    void genOutOfRangeNumberIs400() throws Exception {
        mvc.perform(post("/v1/tasks/gen")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":{"exam":"OGE","subject":"CS","number":100,"prototype":0}}"""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void genMissingNumberIs400() throws Exception {
        mvc.perform(post("/v1/tasks/gen")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":{"exam":"OGE","subject":"CS","prototype":0}}"""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkRightAnswerScoresOne() throws Exception {
        long seed = 2001;
        int answer = Generator_4.build(new Random(seed), cfg()).answer();
        mvc.perform(post("/v1/tasks/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkBody(seed, "{\"answer\":" + answer + "}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void checkWrongAnswerScoresZero() throws Exception {
        long seed = 2001;
        int answer = Generator_4.build(new Random(seed), cfg()).answer();
        mvc.perform(post("/v1/tasks/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkBody(seed, "{\"answer\":" + (answer + 1000) + "}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void checkMalformedAnswerIs422() throws Exception {
        mvc.perform(post("/v1/tasks/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkBody(2001, "{\"wrong\":1}")))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void checkMissingSeedIs400() throws Exception {
        mvc.perform(post("/v1/tasks/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" + ID + ',' + "\"answer\":{\"answer\":1}}"))
                .andExpect(status().isBadRequest());
    }

    private static Generator_4.GraphConfig cfg() {
        return new Generator_4.GraphConfig(5, 2, 3, 1, 5, 2, 5);
    }

    private static String checkBody(long seed, String answerJson) {
        return """
                {%s,"seed":%d,"answer":%s}""".formatted(ID, seed, answerJson);
    }
}
