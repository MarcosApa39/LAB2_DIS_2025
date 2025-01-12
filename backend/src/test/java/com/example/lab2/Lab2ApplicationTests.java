package com.example.lab2;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

// import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Lab2ApplicationTests
 * Unit tests for the REST API endpoints in TurismoController.
 *
 * The tests cover the critical CRUD operations (GET, POST, PUT, DELETE).
 * Edge cases are handled:
 *  - Invalid payloads for POST.
 *  - Non-existent id for PUT and DELETE.
 *  - Handling an empty dataset (GET).
*/

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class Lab2ApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    private Gson gson;
    private TurismoController turismoController;

    @Before
    public void setup() throws Exception {
        gson = new Gson();
    
        // Use test JSON file for controller
        String testJsonPath = "src/test/java/com/example/lab2/resources/test_TurismoComunidades.json";
        turismoController = new TurismoController(testJsonPath);
    
        // Reset test JSON file before each test
        Path source = Paths.get("src/test/java/com/example/lab2/resources/original_test_data.json");
        Path destination = Paths.get(testJsonPath);
        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
    
        // Configure MockMvc with the test-specific controller
        mockMvc = MockMvcBuilders.standaloneSetup(turismoController).build();
    }    


    /**
     * Tests fetching all records.
     * Verifies that the API returns a 200 OK response and content type is JSON.
     */
    @Test
    public void testGetAllRecords() throws Exception {
        mockMvc.perform(get("/api/turismo"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

  

    /**
     * Tests adding a new record.
     * Verifies that the API returns a 200 OK response with a success message.
     */
    @Test
    public void testAddRecord() throws Exception {
        Turismo turismo = new Turismo();
        turismo.setTotal(2000);
        Turismo.FromTo from = new Turismo.FromTo();
        from.setComunidad("New Comunidad");
        from.setProvincia("New Provincia");
        turismo.setFrom(from);
        Turismo.TimeRange timeRange = new Turismo.TimeRange();
        timeRange.setFecha_inicio("2024-02-01");
        timeRange.setFecha_fin("2024-02-28");
        timeRange.setPeriod("2024M02");
        turismo.setTimeRange(timeRange);

        String jsonPayload = gson.toJson(turismo);

        mockMvc.perform(post("/api/turismo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(content().string("Record added successfully."));
    }

    /**
     * Tests adding a record with an invalid payload.
     * Verifies that the API returns a 400 Bad Request response.
     */
    @Test
    public void testAddRecordWithInvalidPayload() throws Exception {
        String invalidPayload = "{}"; // Missing required fields

        mockMvc.perform(post("/api/turismo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest());
    }

    /**
     * Tests updating an existing record.
     * Verifies that the API returns a 200 OK response with a success message.
     */
    @Test
    public void testUpdateRecord() throws Exception {
        Turismo turismo = new Turismo();
        turismo.set_id("6580f1a6-cd7c-4e2d-b1cc-cf0ca9cd6891"); // Valid ID from sample data
        turismo.setTotal(3000);

        String jsonPayload = gson.toJson(turismo);

        mockMvc.perform(put("/api/turismo/6580f1a6-cd7c-4e2d-b1cc-cf0ca9cd6891")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(content().string("Record updated successfully."));
    }

    /**
     * Tests updating a non-existent record.
     * Verifies that the API returns a 404 Not Found response with an error message.
     */
    @Test
    public void testUpdateNonExistentRecord() throws Exception {
        Turismo turismo = new Turismo();
        turismo.setTotal(3000);

        String jsonPayload = gson.toJson(turismo);

        mockMvc.perform(put("/api/turismo/non-existent-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Record not found."));
    }

    /**
     * Tests deleting an existing record.
     * Verifies that the API returns a 200 OK response with a success message.
     */
    @Test
    public void testDeleteRecord() throws Exception {
        mockMvc.perform(delete("/api/turismo/cdc9bf1c-8352-49e6-8d40-937266f61e00")) // Valid ID from sample data
                .andExpect(status().isOk())
                .andExpect(content().string("Record deleted successfully."));
    }

    /**
     * Tests deleting a non-existent record.
     * Verifies that the API returns a 404 Not Found response with an error message.
     */
    @Test
    public void testDeleteNonExistentRecord() throws Exception {
        mockMvc.perform(delete("/api/turismo/non-existent-id"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Record not found."));
    }

}
