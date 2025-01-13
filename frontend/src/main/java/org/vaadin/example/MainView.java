package org.vaadin.example;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.vaadin.example.models.Turismo; // For LocalDate

import com.google.gson.Gson; // For Button Variants (e.g., LUMO_ERROR)
import com.google.gson.reflect.TypeToken; // For Form Layout
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route("")
public class MainView extends VerticalLayout {

    private Grid<Turismo> grid = new Grid<>(Turismo.class); // Grid for CRUD functionality
    private Grid<Turismo> communityGrid = new Grid<>(Turismo.class); // Grid for Group by Community
    private List<Turismo> allRecords = new ArrayList<>(); // Store all records for filtering
    private List<String> communityCodes = new ArrayList<>(); // List of unique community codes

    public MainView() {
        add(new H1("Tourism Data Management"));

        // Create Tabs
        Tabs tabs = new Tabs();
        Tab crudTab = new Tab("CRUD Management");
        Tab groupByCommunityTab = new Tab("Group by Community");
        tabs.add(crudTab, groupByCommunityTab);

        // Layouts for each tab
        VerticalLayout crudLayout = new VerticalLayout();
        VerticalLayout groupByCommunityLayout = new VerticalLayout();

        // Configure tab switch functionality
        tabs.addSelectedChangeListener(event -> {
            removeAll();
            add(new H1("Tourism Data Management"), tabs);
            if (event.getSelectedTab().equals(crudTab)) {
                add(crudLayout);
            } else if (event.getSelectedTab().equals(groupByCommunityTab)) {
                add(groupByCommunityLayout);
            }
        });

        // Set up layouts for tabs
        setupCRUDLayout(crudLayout);
        setupCommunityTab(groupByCommunityLayout);

        // Add tabs and default layout
        add(tabs, crudLayout); // Default to CRUD tab
    }

    /**
     * Set up the layout for the CRUD functionality tab
     */
    private void setupCRUDLayout(VerticalLayout layout) {
        // Add control buttons for Create and filtering
        Button addButton = new Button("Add Record", e -> openAddRecordDialog());
        DatePicker datePicker = new DatePicker("Filter by Date");
        datePicker.addValueChangeListener(event -> filterByDate(event.getValue()));
        layout.add(new HorizontalLayout(addButton, datePicker));

        // Set up the grid to display data
        setupGrid();
        layout.add(grid);

        // Fetch initial data
        fetchData();
    }

    private void setupCommunityTab(VerticalLayout layout) {
        layout.add(new H1("Group by Community"));

        // Create ComboBox
        ComboBox<String> communityDropdown = new ComboBox<>("Select Community");
        communityDropdown.setPlaceholder("Choose a community");
        communityDropdown.setWidth("300px");

        // Fetch community codes and populate ComboBox
        fetchCommunityCodes();
        communityDropdown.setItems(communityCodes);

        // Add listener for ComboBox selection
        communityDropdown.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                fetchCommunityData(event.getValue());
            }
        });

        // Configure grid
        setupCommunityGrid();

        // Add ComboBox and Grid to layout
        layout.add(communityDropdown, communityGrid);
    }

    // Setup the grid to display records
    private void setupGrid() {
        grid.addColumn(Turismo::get_id).setHeader("ID");
        grid.addColumn(t -> t.getFrom().getComunidad()).setHeader("From Comunidad");
        grid.addColumn(t -> t.getFrom().getProvincia()).setHeader("From Provincia");
        grid.addColumn(t -> t.getTo().getComunidad()).setHeader("To Comunidad");
        grid.addColumn(t -> t.getTo().getProvincia()).setHeader("To Provincia");
        grid.addColumn(t -> t.getTimeRange().getFecha_inicio()).setHeader("Start Date");
        grid.addColumn(t -> t.getTimeRange().getFecha_fin()).setHeader("End Date");
        grid.addColumn(Turismo::getTotal).setHeader("Total");

        // Add double-click listener for updating
        grid.addItemDoubleClickListener(event -> openEditRecordDialog(event.getItem()));

        // Add Delete button to each row
        grid.addComponentColumn(turismo -> {
            Button deleteButton = new Button("Delete", e -> deleteRecordFromBackend(turismo));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            return deleteButton;
        }).setHeader("Actions");

        add(grid);
    }

    // Add control buttons for Create and filtering
    private void addControls() {
        Button addButton = new Button("Add Record", e -> openAddRecordDialog());
        DatePicker datePicker = new DatePicker("Filter by Date");
        datePicker.addValueChangeListener(event -> filterByDate(event.getValue()));
        add(new HorizontalLayout(addButton, datePicker));
    }

    // Fetch data from the backend and populate the grid
    private void fetchData() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://lab2-backend-085cd548673a.herokuapp.com/api/turismo"))
                //.uri(URI.create("http://localhost:8083/api/turismo"))
                .build();
    
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Gson gson = new Gson();
            
            // Use TypeToken for correct deserialization
            Type listType = new TypeToken<List<Turismo>>() {}.getType();
            allRecords = gson.fromJson(response.body(), listType); // Save all records for filtering
            
            grid.setItems(allRecords); // Populate grid with the deserialized records
        } catch (Exception e) {
            Notification.show("Failed to fetch data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Filter grid rows by a selected date
    private void filterByDate(LocalDate date) {
        if (date != null) {
            List<Turismo> filtered = allRecords.stream()
                .filter(record -> record.getTimeRange().getFecha_inicio().equals(date.toString()))
                .collect(Collectors.toList());
            if (filtered.isEmpty()) {
                Notification.show("No matching rows.");
            }
            grid.setItems(filtered);
        } else {
            grid.setItems(allRecords); // Reset grid if no date is selected
        }
    }

    // Add a new record to the backend
    private void openAddRecordDialog() {
        Dialog dialog = new Dialog();
        FormLayout form = new FormLayout();

        TextField fromComunidad = new TextField("From Comunidad");
        TextField fromProvincia = new TextField("From Provincia");
        TextField toComunidad = new TextField("To Comunidad");
        TextField toProvincia = new TextField("To Provincia");
        TextField startDate = new TextField("Start Date (YYYY-MM-DD)");
        TextField endDate = new TextField("End Date (YYYY-MM-DD)");
        TextField period = new TextField("Period");
        TextField total = new TextField("Total");

        Button saveButton = new Button("Save", e -> {
            Turismo newRecord = new Turismo();
            Turismo.FromTo from = new Turismo.FromTo();
            from.setComunidad(fromComunidad.getValue());
            from.setProvincia(fromProvincia.getValue());

            Turismo.FromTo to = new Turismo.FromTo();
            to.setComunidad(toComunidad.getValue());
            to.setProvincia(toProvincia.getValue());

            Turismo.TimeRange timeRange = new Turismo.TimeRange();
            timeRange.setFecha_inicio(startDate.getValue());
            timeRange.setFecha_fin(endDate.getValue());
            timeRange.setPeriod(period.getValue());

            newRecord.setFrom(from);
            newRecord.setTo(to);
            newRecord.setTimeRange(timeRange);
            newRecord.setTotal(Integer.parseInt(total.getValue()));

            addRecordToBackend(newRecord);
            dialog.close();
        });

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        form.add(fromComunidad, fromProvincia, toComunidad, toProvincia, startDate, endDate, period, total);
        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        dialog.add(form, buttons);
        dialog.open();
    }

    private void addRecordToBackend(Turismo turismo) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            Gson gson = new Gson();
            String jsonPayload = gson.toJson(turismo);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://lab2-backend-085cd548673a.herokuapp.com/api/turismo"))
                //.uri(URI.create("http://localhost:8083/api/turismo"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Notification.show("Record added: " + response.body());
            fetchData();
        } catch (Exception e) {
            Notification.show("Failed to add record: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Edit a record when double-clicking on a grid row
    private void openEditRecordDialog(Turismo turismo) {
        try {
            // Fetch the latest data for the selected record by its ID
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://lab2-backend-085cd548673a.herokuapp.com/api/turismo/" + turismo.get_id())) // Correct API call with ID
                //.uri(URI.create("http://localhost:8083/api/turismo/" + turismo.get_id())) // Correct API call with ID
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 404) {
                Notification.show("Record not found.");
                return;
            }

            if (response.statusCode() != 200) {
                Notification.show("Failed to fetch record: " + response.statusCode());
                return;
            }

            // Parse the fetched data
            Gson gson = new Gson();
            Turismo latestRecord = gson.fromJson(response.body(), Turismo.class);

            // Open a dialog to display the form with fetched data
            Dialog dialog = new Dialog();
            FormLayout form = new FormLayout();

            TextField fromComunidad = new TextField("From Comunidad", latestRecord.getFrom().getComunidad());
            TextField fromProvincia = new TextField("From Provincia", latestRecord.getFrom().getProvincia());
            TextField toComunidad = new TextField("To Comunidad", latestRecord.getTo().getComunidad());
            TextField toProvincia = new TextField("To Provincia", latestRecord.getTo().getProvincia());
            TextField startDate = new TextField("Start Date", latestRecord.getTimeRange().getFecha_inicio());
            TextField endDate = new TextField("End Date", latestRecord.getTimeRange().getFecha_fin());
            TextField period = new TextField("Period", latestRecord.getTimeRange().getPeriod());
            TextField total = new TextField("Total", String.valueOf(latestRecord.getTotal()));

            // Submit button
            Button saveButton = new Button("Submit", e -> {
                latestRecord.getFrom().setComunidad(fromComunidad.getValue());
                latestRecord.getFrom().setProvincia(fromProvincia.getValue());
                latestRecord.getTo().setComunidad(toComunidad.getValue());
                latestRecord.getTo().setProvincia(toProvincia.getValue());
                latestRecord.getTimeRange().setFecha_inicio(startDate.getValue());
                latestRecord.getTimeRange().setFecha_fin(endDate.getValue());
                latestRecord.getTimeRange().setPeriod(period.getValue());
                latestRecord.setTotal(Integer.parseInt(total.getValue()));

                // Update the record in the backend
                updateRecordInBackend(latestRecord);
                dialog.close();
            });

            // Cancel button
            Button cancelButton = new Button("Cancel", e -> dialog.close());

            form.add(fromComunidad, fromProvincia, toComunidad, toProvincia, startDate, endDate, period, total);
            HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
            dialog.add(form, buttons);
            dialog.open();

        } catch (Exception e) {
            Notification.show("Failed to fetch record: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Update the record in the backend
    private void updateRecordInBackend(Turismo turismo) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            Gson gson = new Gson();
            String jsonPayload = gson.toJson(turismo);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://lab2-backend-085cd548673a.herokuapp.com/api/turismo/" + turismo.get_id())) // API call for updating
                //.uri(URI.create("http://localhost:8083/api/turismo/" + turismo.get_id())) // API call for updating
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Notification.show("Record updated successfully.");
                fetchData(); // Refresh the grid with updated data
            } else {
                Notification.show("Failed to update record: " + response.body());
            }
        } catch (Exception e) {
            Notification.show("Failed to update record: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Delete a record
    private void deleteRecordFromBackend(Turismo turismo) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://lab2-backend-085cd548673a.herokuapp.com/api/turismo/" + turismo.get_id()))
                //.uri(URI.create("http://localhost:8083/api/turismo/" + turismo.get_id()))
                .DELETE()
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Notification.show("Record deleted: " + response.body());
            fetchData();
        } catch (Exception e) {
            Notification.show("Failed to delete record: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupCommunityGrid() {
        communityGrid.addColumn(Turismo::get_id).setHeader("ID");
        communityGrid.addColumn(t -> t.getFrom().getComunidad()).setHeader("From Comunidad");
        communityGrid.addColumn(t -> t.getTo().getComunidad()).setHeader("To Comunidad");
        communityGrid.addColumn(t -> t.getTimeRange().getFecha_inicio()).setHeader("Start Date");
        communityGrid.addColumn(t -> t.getTimeRange().getFecha_fin()).setHeader("End Date");
        communityGrid.addColumn(Turismo::getTotal).setHeader("Total");
    }

    private void fetchCommunityCodes() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://lab2-backend-085cd548673a.herokuapp.com/api/turismo")) // Adjust the backend endpoint as needed
                //.uri(URI.create("http://localhost:8083/api/turismo")) // Adjust the backend endpoint as needed
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Turismo>>() {}.getType();
            List<Turismo> records = gson.fromJson(response.body(), listType);

            // Extract unique community codes and sort alphabetically
            communityCodes = records.stream()
                .map(record -> record.getTo().getComunidad())
                .distinct()
                .sorted() // Sort alphabetically
                .collect(Collectors.toList());

        } catch (Exception e) {
            Notification.show("Failed to fetch community codes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void fetchCommunityData(String communityCode) {
        try {
            // URL-encode the community name
            String encodedCommunityCode = URLEncoder.encode(communityCode, StandardCharsets.UTF_8);
    
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://lab2-backend-085cd548673a.herokuapp.com/api/turismo/community/" + encodedCommunityCode))
                //.uri(URI.create("http://localhost:8083/api/turismo/community/" + encodedCommunityCode))
                .build();
    
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    
            if (response.statusCode() == 404) {
                Notification.show("No records found for community: " + communityCode);
                communityGrid.setItems(new ArrayList<>()); // Clear grid
                return;
            }
    
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Turismo>>() {}.getType();
            List<Turismo> communityRecords = gson.fromJson(response.body(), listType);
    
            communityGrid.setItems(communityRecords);
        } catch (Exception e) {
            Notification.show("Failed to fetch community data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
}
