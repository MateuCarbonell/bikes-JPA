package org.example;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.lang.Object;
import javax.xml.namespace.QName;
import java.util.Arrays;
import java.sql.*;
import java.sql.DriverManager;
import com.zaxxer.hikari.HikariDataSource;


public class Application {
    private static JpaService jpaService = JpaService.getInstance(); //JPA
    private static Connection connection; //JDBC
    private static HikariDataSource dataSource; //Pool


    public static void main(String[] args) throws SQLException {


        try {
            openDatabaseConnection();
            initDatabaseConnectionPool();

            insertBikesTable();
            insertPlacesTable();
            insertBookingsTable();

            printBikes();
            System.out.println("");
            System.out.println("Actualizar una linea");
            updateDataBikes("Orbea", "NuevaOrbea");
            printBikes();
            System.out.println("");
            printPlaces();
            System.out.println("Borrar 1 linea");
            deleteDataPool(1);

            System.out.println("");

            printPlaces();
            System.out.println("");
            readDataBookings();
        } finally {
            closeDatabaseConnection();
            closeDatabaseConnectionPool();
        }
    }


    private static void insertPlacesTable() { //JPA
        jpaService.runInTransaction((EntityManager entityManager) -> {
            List<Places> places = new ArrayList<>();
            places.add(new Places("Inca ", 94132, "Mallorca"));
            places.add(new Places("Villas ", 12001, "Madrid"));
            places.add(new Places("Palma ", 91001, "Valencia"));

            places.forEach(entityManager::persist);

            return null;
        });
    }

    public static void insertBikesTable() { // JPA
        jpaService.runInTransaction((EntityManager entityManager) -> {
            List<Bikes> bikes = new ArrayList<>();
            bikes.add(new Bikes("Orbea", 2, 2));
            bikes.add(new Bikes("Giant", 1, 1));
            bikes.add(new Bikes("LOV", 3, 3));
            bikes.add(new Bikes("BTWIN", 4, 4));

            bikes.forEach(entityManager::persist);

            return null;
        });
    }

    private static void printBikes() { // JPA
        List<Bikes> bikes = jpaService.runInTransaction(entityManager -> {
            return entityManager.createQuery(
                    "select b from Bikes b",
                    Bikes.class
            ).getResultList();
        });

        System.out.println("BIKES TABLE");
        System.out.println("+---------+----------+--------------+-------------+");
        System.out.printf("| %7s | %8s | %12s | %11s |\n", "BIKE_ID", "PLACE_ID", "BIKE_MODEL", "ASSIGNED_ID");
        System.out.println("+---------+----------+--------------+-------------+");

        for (Bikes bike : bikes) {
            System.out.format("| %-7s | %-8s | %-12s | %-11s |\n", bike.getId(), bike.getPlace(), bike.getModel(), bike.getAssigned_id());

        }
        System.out.println("+---------+----------+--------------+-------------+");
    }

    private static void readDataBookings() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
            SELECT bookings_id, nombre, surname, age, email, village, bike_id
            FROM bookings
            ORDER BY bookings_id DESC
        """)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                boolean empty = true;
                System.out.println("BOOKINGS TABLE");
                System.out.println("+-------------+------------+------------+-----+--------------------------+----------------------+----------+");
                System.out.printf("| %11s | %10s | %10s | %3s | %25s | %20s | %8s |\n", "BOOKINGS_ID", "NOMBRE", "SURNAME", "AGE", "EMAIL", "VILLAGE", "BIKE_ID");
                System.out.println("+-------------+------------+------------+-----+--------------------------+----------------------+----------+");
                while (resultSet.next()) {
                    empty = false;
                    int bookings_id = resultSet.getInt("bookings_id");
                    String nombre = resultSet.getString("nombre");
                    String surname = resultSet.getString("surname");
                    int age = resultSet.getInt("age");
                    String email = resultSet.getString("email");
                    String village = resultSet.getString("village");
                    int bike_id = resultSet.getInt("bike_id");
                    System.out.printf("| %11d | %10s | %10s | %3d | %25s | %20s | %8d |\n", bookings_id, nombre, surname, age, email, village, bike_id);
                }
                if (empty) {
                    System.out.println("\t (no data)");
                }
                System.out.println("+-------------+------------+------------+-----+--------------------------+----------------------+----------+");
                System.out.println("");
            }
        }


    }

    private static void printPlaces() {
        List<Places> places = jpaService.runInTransaction(entityManager -> {
            return entityManager.createQuery(
                    "select p from Places p",
                    Places.class
            ).getResultList();
        });

        // Imprimir encabezado de la tabla
        System.out.println("PLACES TABLE");
        System.out.println("+----------------+----------------------+--------+");
        System.out.println("| Location       | Village              | CP     |");
        System.out.println("+----------------+----------------------+--------+");

        // Imprimir cada fila de la tabla
        for (Places place : places) {
            System.out.println(String.format(
                    "| %-15s | %-20s | %-6s|",
                    place.getLocation(),
                    place.getVillage(),
                    place.getCp()
            ));
        }
        System.out.println("+----------------+----------------------+--------+");

    }


    private static void updateDataBikes (String oldModel, String newModel) throws SQLException { //JDBC
            System.out.println("Updating DATA");
            try (PreparedStatement statement = connection.prepareStatement("""
                        UPDATE bikes
                        SET bike_model = ?
                        WHERE bike_model = ?
            """)) {
            statement.setString(1, newModel);
            statement.setString(2, oldModel);
            int rowsUpdated = statement.executeUpdate();
            System.out.println("rows updated --> " + rowsUpdated);
        }
    }

    private static void insertBookingsTable() { // JPA
        jpaService.runInTransaction((EntityManager entityManager) -> {
            List<Bookings> bookings = new ArrayList<>();
            bookings.add(new Bookings("Joan", "Calero", 23, "jcalero@gmail.com", "Inca", 1));
            bookings.add(new Bookings("María", "Martínez", 27, "mmartinez@gmail.com", "Valencia", 2));
            bookings.add(new Bookings("Carla", "López", 29, "clopez@hotmail.com", "Bilbao", 3));
            bookings.add(new Bookings("Juan", "Sánchez", 35, "jsanchez@yahoo.com", "Sevilla", 4));
            bookings.forEach(entityManager::persist);

            return null;
        });

    }

    private static void deleteDataPool(Integer idExpression) throws SQLException { // POOL!
        System.out.println("rows deleted!!!");
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("""
                            DELETE FROM places
                            WHERE place_id = ?
                    """)) {
                statement.setInt(1,idExpression);
                int rowsDeleted = statement.executeUpdate();
                System.out.println("rows deleted : " + rowsDeleted);
            }
        }
    }


    private static void readDataBikes() throws SQLException { //JDBC
        System.out.println("Reading data: ");
        try (PreparedStatement statement = connection.prepareStatement("""
            SELECT bike_id,bike_model,place_id,assigned_id FROM bikes ORDER BY bike_id DESC
""")){
            ResultSet resultSet = statement.executeQuery();
            boolean empty = true;
            while(resultSet.next()){
                String bike_model= resultSet.getString(1);
                int bike_id= resultSet.getInt(2);
                System.out.println(bike_model + ": "+ bike_id);
                empty= false;
            }
            if(empty){
                System.out.println("no data!");
            }
        }
    }
    private static void openDatabaseConnection() throws SQLException { //JDBC
        System.out.println("Connecting to the DB");
        connection = DriverManager.getConnection(
                "jdbc:mariadb://localhost:3306/bikes",
                "root","mateu"

        );
        System.out.println("Connection valid: "+ connection.isValid(5));
    }

    private static void closeDatabaseConnection() throws SQLException{ //JDBC
        System.out.println("closing database connection");
        connection.close();
        System.out.println("connection is valid: " + connection.isValid(5));
    }


    private static void initDatabaseConnectionPool(){
        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mariadb://localhost:3306/bikes");
        dataSource.setUsername("root");
        dataSource.setPassword("mateu");

    }

    private static void closeDatabaseConnectionPool(){
        dataSource.close();

    }
}
