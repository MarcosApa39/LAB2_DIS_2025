package com.example.lab2;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@RestController
@RequestMapping("/api/turismo")
public class TurismoController {

    private final String jsonFilePath;
    private final String groupedJsonFilePath = "src/main/resources/Comunidades_Agrupadas.json";
    private final Gson gson = new Gson();

    // Constructor para inicializar jsonFilePath con un valor predeterminado
    public TurismoController() {
        this.jsonFilePath = "src/main/resources/TurismoComunidades.json";
    }

    // Constructor adicional para pruebas (testJsonPath)
    public TurismoController(String testJsonPath) {
        this.jsonFilePath = testJsonPath;
    }

    /**
     * Fetch all records or a paginated subset of records.
     */
    @GetMapping
    public ResponseEntity<List<Turismo>> getAllOrPaginatedRecords(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        try {
            List<Turismo> records = loadRecords();
            if (page == null || size == null) {
                return ResponseEntity.ok(records);
            }

            int start = Math.min(page * size, records.size());
            int end = Math.min(start + size, records.size());
            if (start > end) {
                return ResponseEntity.badRequest().body(null);
            }

            List<Turismo> paginatedRecords = records.subList(start, end);
            return ResponseEntity.ok(paginatedRecords);
        } catch (IOException e) {
            System.err.println("Error fetching records: " + e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Add a new record.
     */
    @PostMapping
    public ResponseEntity<String> addRecord(@RequestBody Turismo turismo) {
        if (turismo.getFrom() == null || turismo.getTimeRange() == null) {
            return ResponseEntity.badRequest().body("Invalid payload: Missing required fields.");
        }
        try {
            List<Turismo> records = loadRecords();
            turismo.set_id(UUID.randomUUID().toString());
            records.add(turismo);
            saveRecords(records);
            return ResponseEntity.ok("Record added successfully.");
        } catch (IOException e) {
            System.err.println("Error saving record: " + e.getMessage());
            return ResponseEntity.status(500).body("Error saving record.");
        }
    }

    /**
     * Update an existing record.
     */
    @PutMapping("/{id}")
    public ResponseEntity<String> updateRecord(@PathVariable String id, @RequestBody Turismo updatedTurismo) {
        try {
            List<Turismo> records = loadRecords();
            boolean found = false;

            for (Turismo turismo : records) {
                if (turismo.get_id().equals(id)) {
                    turismo.setFrom(updatedTurismo.getFrom());
                    turismo.setTo(updatedTurismo.getTo());
                    turismo.setTimeRange(updatedTurismo.getTimeRange());
                    turismo.setTotal(updatedTurismo.getTotal());
                    found = true;
                    break;
                }
            }

            if (!found) {
                return ResponseEntity.status(404).body("Record not found.");
            }

            saveRecords(records);
            return ResponseEntity.ok("Record updated successfully.");
        } catch (IOException e) {
            System.err.println("Error updating record: " + e.getMessage());
            return ResponseEntity.status(500).body("Error updating record.");
        }
    }

    /**
     * Delete a record by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRecord(@PathVariable String id) {
        try {
            List<Turismo> records = loadRecords();
            boolean removed = records.removeIf(turismo -> turismo.get_id().equals(id));

            if (!removed) {
                return ResponseEntity.status(404).body("Record not found.");
            }

            saveRecords(records);
            return ResponseEntity.ok("Record deleted successfully.");
        } catch (IOException e) {
            System.err.println("Error deleting record: " + e.getMessage());
            return ResponseEntity.status(500).body("Error deleting record.");
        }
    }

    /**
     * Get a record by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Turismo> getRecordById(@PathVariable String id) {
        try {
            List<Turismo> records = loadRecords();

            for (Turismo turismo : records) {
                if (turismo.get_id() != null && turismo.get_id().equals(id)) {
                    return ResponseEntity.ok(turismo);
                }
            }

            System.err.println("Record with ID " + id + " not found.");
            return ResponseEntity.status(404).body(null);
        } catch (IOException e) {
            System.err.println("Error fetching record by ID: " + e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Get records by community code.
     */
    @GetMapping("/community/{community}")
    public ResponseEntity<List<Turismo>> getRecordsByCommunity(@PathVariable String community) {
        try {
            String decodedCommunity = java.net.URLDecoder.decode(community, StandardCharsets.UTF_8);

            Map<String, List<Turismo>> groupedRecords = loadGroupedRecords();
            List<Turismo> filteredRecords = groupedRecords.getOrDefault(decodedCommunity, new ArrayList<>());

            if (filteredRecords.isEmpty()) {
                return ResponseEntity.status(404).body(null);
            }

            return ResponseEntity.ok(filteredRecords);
        } catch (IOException e) {
            System.err.println("Error loading grouped records: " + e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Load records from the JSON file.
     */
    private List<Turismo> loadRecords() throws IOException {
        try (FileReader reader = new FileReader(jsonFilePath)) {
            Type listType = new TypeToken<List<Turismo>>() {}.getType();
            return gson.fromJson(reader, listType);
        } catch (IOException e) {
            System.err.println("Error reading records file: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Save records to the JSON file.
     */
    private void saveRecords(List<Turismo> records) throws IOException {
        try (FileWriter writer = new FileWriter(jsonFilePath)) {
            gson.toJson(records, writer);
        }
    }

    /**
     * Load grouped records from the JSON file.
     */
    private Map<String, List<Turismo>> loadGroupedRecords() throws IOException {
        try (FileReader reader = new FileReader(groupedJsonFilePath)) {
            Type mapType = new TypeToken<Map<String, List<Turismo>>>() {}.getType();
            return gson.fromJson(reader, mapType);
        }
    }
}
