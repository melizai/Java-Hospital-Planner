package com.example.hospitalplanner;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import java.io.*;
import java.net.URL;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class HelloController implements Initializable {

    private Stage stage;
    private Scene scene;
    private Parent root;

    public void switchToHome(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("home.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToProgramare(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("programare.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToCadreMedicale(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("cadre-medicale.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void informatii(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("informatii.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

//    public void switchToProgramare2(ActionEvent event) throws IOException {
//        Parent root = FXMLLoader.load(getClass().getResource("programare2.fxml"));
//        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
//        scene = new Scene(root);
//        stage.setScene(scene);
//        stage.show();
//    }

    Appointment programare = new Appointment();
    @FXML
    public TextField numePrenume = new TextField();
    public String numeIntrodus;
    @FXML
    public TextField numar = new TextField();
    public String numarIntrodus;
    @FXML
    public ChoiceBox<String> choiceSpecialitati = new ChoiceBox<>();
    public List<String> specialties = programare.getSpecialties();
    public String specialitateAleasa;
    @FXML
    private Label textFormular;  //ala care e pe ecran la inceput si dupa inlocuit cu textPozitiv si textNegativ
    public String textPozitiv = "Data selectată are locuri disponibile, selectați mai jos";
    public String textNegativ = "Nu există locuri libere pentru data selectată. Următoarea dată disponibilă:";
    @FXML
    private DatePicker calendar;
    @FXML
    private Label dataCalendar;
    public Date dataAleasa;
    public String dataValabila;
    @FXML
    private ChoiceBox<String> choiceProgramari = new ChoiceBox<>();
    public List<String> programari;
    public String programareAleasa;
    private static final String COUNTER_FILE = "counter.txt";

    public static int getIdForAppointment() {
        int nextId = 1;

        try {
            File file = new File(COUNTER_FILE);

            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                nextId = Integer.parseInt(reader.readLine()) + 1;
                reader.close();
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(String.valueOf(nextId));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return nextId;
    }
    public void adaugaProgramare(int id, int idDoctor, String nume, String numar, Date data, Time start, Time end){
        String url = "jdbc:mysql://localhost:3306/javaproject";
        String username = "root";
        String password = "parola";

        try (Connection conn = DriverManager.getConnection(url, username, password)) {

            String sql = "INSERT INTO `javaproject`.`appointment` (`id`, `doctor_id`, `patient_name`, `patient_phone`, `date`, `start_at`, `end_at`) VALUES (?,?,?,?,?,?,?);";

            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setInt(1, id);
                statement.setInt(2, idDoctor);
                statement.setString(3, nume);
                statement.setString(4, numar);
                statement.setDate(5, data);
                statement.setTime(6, start);
                statement.setTime(7, end);

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void specialitateDinChoiceBox(ActionEvent event){
        specialitateAleasa = choiceSpecialitati.getValue();
        System.out.println(specialitateAleasa);
    }
    public void programareDinChoiceBox(ActionEvent event){
        programareAleasa = choiceProgramari.getValue();
        System.out.println(programareAleasa);
    }


    @FXML
    public void getDataCalendar(ActionEvent event) {

        LocalDate dataSelectata = calendar.getValue();
        this.dataAleasa = Date.valueOf(calendar.getValue());
        String dateFormat = dataSelectata.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        this.dataValabila = String.valueOf(programare.getFirstAvailabileDate(specialitateAleasa, dataAleasa));  //same ca aia care returneaza lista cu tot, fac sa returneze doar data
        if (dataValabila.equals(dateFormat)) {
            textFormular.setText(textPozitiv);
        } else {
            textFormular.setText(textNegativ);
        }
        this.dataAleasa = Date.valueOf(dataValabila);

        dataCalendar.setText(String.valueOf(dataAleasa));  //o sa pun dataAleasa in loc de dateFormat
        System.out.println(dataAleasa);
        System.out.println(dataValabila);

        if(this.specialitateAleasa != null){
            this.choiceProgramari.getItems().clear();
            this.programari = programare.getAppointmentsForFirstAvailabileDate(this.specialitateAleasa, this.dataAleasa);
            this.choiceProgramari.getItems().addAll(this.programari);
        }

        this.choiceProgramari.setOnAction(this::programareDinChoiceBox);
        dataValabila = String.valueOf(this.dataAleasa);
    }

    public String extractDoctor(String input) {
        int index = input.indexOf(':');
        if (index != -1) {
            return input.substring(0, index);
        }
        return input;
    }

    public String extractStartTime(String input, String startDelimiter, String endDelimiter) {
        int startIndex = input.indexOf(startDelimiter);
        int endIndex = input.indexOf(endDelimiter);
        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            return input.substring(startIndex + startDelimiter.length(), endIndex);
        }
        return "";
    }

    public String extractEndTime(String input, String startDelimiter) {
        int startIndex = input.indexOf(startDelimiter);
        if (startIndex != -1) {
            return input.substring(startIndex + startDelimiter.length());
        }
        return "";  // Return an empty string if the start delimiter is not found
    }

    public int idDoctorFromName(String nume){
        String url = "jdbc:mysql://localhost:3306/javaproject";
        String username = "root";
        String password = "parola";
        int doctorId = 0;

        MySQLDatabaseConnector connector = new MySQLDatabaseConnector(url, username, password);
        try (Connection conn = DriverManager.getConnection(url, username, password)) {

            String sql = "SELECT id FROM javaproject.doctor where name = ?;";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, nume);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                doctorId = resultSet.getInt("id");
            }

            connector.disconnect();
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return doctorId;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        this.choiceSpecialitati.getItems().addAll(this.specialties);
        this.choiceSpecialitati.setOnAction(this::specialitateDinChoiceBox);

    }

    @FXML
    public void trySubmit(ActionEvent event) throws IOException {

        numeIntrodus = numePrenume.getText();
        numarIntrodus = numar.getText();

        System.out.println(numeIntrodus + "\n" + numarIntrodus);  //--am verificat sa vad ca le ia pe valori
        System.out.println(specialitateAleasa + " " + dataAleasa);
        System.out.println(programareAleasa);

        String doctor = extractDoctor(programareAleasa);

        String startDelimiter1 = ": ";
        String endDelimiter1 = " - ";
        Time start = Time.valueOf(extractStartTime(programareAleasa, startDelimiter1, endDelimiter1));

        String startDelimiter2 = " - ";
        Time end = Time.valueOf(extractEndTime(programareAleasa, startDelimiter2));

        int idDoctor = idDoctorFromName(doctor);

        int idProgramare = getIdForAppointment();

        adaugaProgramare(idProgramare, idDoctor, numeIntrodus, numarIntrodus, dataAleasa, start, end);

        switchToHome(event);
    }

}