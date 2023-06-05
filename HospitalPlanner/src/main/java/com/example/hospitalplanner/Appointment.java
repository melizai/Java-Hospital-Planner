package com.example.hospitalplanner;


import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Appointment {

    String numePacient;
    String nrTelefonPacient;

    public String specialitate;

    public String data;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getSpecialitate() {
        return specialitate;
    }

    public void setSpecialitate(String specialitate) {
        this.specialitate = specialitate;
    }

    public String getNumePacient() {
        return numePacient;
    }

    public void setNumePacient(String numePacient) {
        this.numePacient = numePacient;
    }

    public String getNrTelefonPacient() {
        return nrTelefonPacient;
    }

    public void setNrTelefonPacient(String nrTelefonPacient) {
        this.nrTelefonPacient = nrTelefonPacient;
    }

    public List<String> getSpecialties(){
        String url = "jdbc:mysql://localhost:3306/javaproject";
        String username = "root";
        String password = "parola";
        List<String> specialties = new ArrayList<>();

        MySQLDatabaseConnector connector = new MySQLDatabaseConnector(url, username, password);
        try {
            connector.connect();

            Connection connection = connector.getConnection();
            MyRepository repository = new MyRepository(connection);
            ResultSet resultSet = repository.select("SELECT DISTINCT specialty FROM doctor");

            //  Print the value of the variable

            while (resultSet.next()) {
                String specialty = resultSet.getString("specialty");
                specialties.add(specialty);
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return specialties;
    }

    public void printIncrementDateWithOne(Date date) {

        String url = "jdbc:mysql://localhost:3306/javaproject";
        String username = "root";
        String password = "parola";
        Date newDate = date;

        MySQLDatabaseConnector connector = new MySQLDatabaseConnector(url, username, password);
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
//            Calendar calendar = Calendar.getInstance();calendar.setTime(newDate);calendar.add(Calendar.DAY_OF_YEAR, 1); System.out.println("Updated date: " + calendar.getTime());
            LocalDate localDate = newDate.toLocalDate();
            LocalDate updatedDate = localDate.plusDays(1); // Incrementing the date by one day

            System.out.println("Updated date: " + updatedDate);
            connector.disconnect();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }  //merge

    public java.sql.Date incrementDateWithOne(java.sql.Date date) {
        String url = "jdbc:mysql://localhost:3306/javaproject";
        String username = "root";
        String password = "parola";

        MySQLDatabaseConnector connector = new MySQLDatabaseConnector(url, username, password);
        try (Connection ignored = DriverManager.getConnection(url, username, password)) {
            LocalDate localDate = date.toLocalDate();
            LocalDate updatedDate = localDate.plusDays(1); // Incrementing the date by one day

            connector.disconnect();

            return java.sql.Date.valueOf(updatedDate);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // Return null if an exception occurs
    }


    public List<String> getAvailabilityBySpecialtyAndDate(String specialty, java.sql.Date date) {  //trebuie modif ca sa returneze cv de genul:
        String url = "jdbc:mysql://localhost:3306/javaproject";                       //31.05.2023, Doctor 1: 10:00-11:00, 13:00-14:00, Doctor 2: 08:00-09:00
        String username = "root";
        String password = "parola";
        List<String> doctorNames = new ArrayList<>();

        MySQLDatabaseConnector connector = new MySQLDatabaseConnector(url, username, password);
        try (Connection conn = DriverManager.getConnection(url, username, password)) {

            String sql = "SELECT d.id AS doctor_id, d.name AS doctor_name, d.specialty as doctor_speciality,\n" +
                    "       da.day_of_week as doctor_availability_day_of_week, da.start_at as doctor_availability_start_at, da.end_at as doctor_availability_end_at,\n" +
                    "        a.id AS appointment_id, a.patient_name, a.patient_phone, a.date as appointment_date, a.start_at AS appointment_start, a.end_at AS appointment_end\n" +
                    "FROM doctor d\n" +
                    "JOIN doctor_availability da ON d.id = da.doctor_id \n" +
                    "LEFT JOIN appointment a ON d.id = a.doctor_id AND a.date = ? and da.start_at  = a.start_at\n" +
                    "WHERE da.day_of_week = DAYNAME(?) -- Checks availability for the current day\n" +
                    "  and d.specialty = ? -- Checks appointments for the current date\n" +
                    "ORDER BY d.name;";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setDate(1, date);
            statement.setDate(2, date);
            statement.setString(3, specialty);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int appointment_id = resultSet.getInt("appointment_id");
                String doctor_name = resultSet.getString("doctor_name");
                Time doctor_availability_start_at = resultSet.getTime("doctor_availability_start_at");
                Time doctor_availability_end_at = resultSet.getTime("doctor_availability_end_at");
                if(appointment_id == 0){doctorNames.add(doctor_name + ": " + doctor_availability_start_at + " - " + doctor_availability_end_at);
                }
            }
            connector.disconnect();
        }catch (SQLException e) {
            e.printStackTrace();
        }
//        if(!doctorNames.isEmpty())System.out.println(date + ": ");  //primesc sa vad daca si in interval. vedem dupa care i treaba
        return doctorNames;
    }

    public List<String> getAppointmentsForFirstAvailabileDate(String specialty, Date preferateDate){  //daca am o data care n are prog valabile imi arata urmatoare zi care o face
        List<String> listaProgramariValabile;
        Date dataPreferata = preferateDate;
        listaProgramariValabile = this.getAvailabilityBySpecialtyAndDate(specialty, dataPreferata);
        while(listaProgramariValabile.isEmpty()) {
            dataPreferata = this.incrementDateWithOne(dataPreferata);
            listaProgramariValabile = this.getAvailabilityBySpecialtyAndDate(specialty, dataPreferata);
        }
        return listaProgramariValabile;
    }

    public Date getFirstAvailabileDate(String specialty, Date preferateDate){  //daca am o data care n are prog valabile imi arata urmatoare zi care o face
        Appointment programare= new Appointment();
        List<String> listaProgramariValabile = new ArrayList<>();
        Date dataPreferata = preferateDate;
        String specialitate = specialty;
        listaProgramariValabile = programare.getAvailabilityBySpecialtyAndDate(specialitate, dataPreferata);
        while(listaProgramariValabile.isEmpty()) {
            dataPreferata = programare.incrementDateWithOne(dataPreferata);
            listaProgramariValabile = programare.getAvailabilityBySpecialtyAndDate(specialitate, dataPreferata);
        }
        return dataPreferata;
    }


    //elimin din ultimele 2 return ul la data, las sa fie doar lista cu  {doctor1: interval  orar, doctor2: interval  orar}
    public void createAppointment(){

    }

}
