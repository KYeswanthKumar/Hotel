package com.hotelbooking.api;

import com.hotelbooking.models.Booking;
import com.hotelbooking.models.RoomCategory;
import com.hotelbooking.models.Room;
import com.hotelbooking.services.Hotel;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.LocalDate;
import java.util.List;

public class Server {
    private static Hotel myHotel;
    private static RoomCategory deluxe = new RoomCategory("Deluxe", 200.0);
    private static RoomCategory suite = new RoomCategory("Suite", 500.0);
    private static RoomCategory family = new RoomCategory("Family", 800.0);
    private static RoomCategory penthouse = new RoomCategory("Penthouse", 3500.0);
    private static RoomCategory partyHall = new RoomCategory("Party Hall", 5000.0);
    private static RoomCategory banquetHall = new RoomCategory("Banquet Hall", 12000.0);
    private static RoomCategory standard = new RoomCategory("Standard", 50.0);
    private static RoomCategory economy = new RoomCategory("Economy", 40.0);

    public static void main(String[] args) throws Exception {
        // Initialize Data
        myHotel = new Hotel("The Grand Boutique");
        myHotel.addRoom(new Room("101", deluxe));
        myHotel.addRoom(new Room("102", deluxe));
        myHotel.addRoom(new Room("201", suite));
        myHotel.addRoom(new Room("202", suite));
        myHotel.addRoom(new Room("301", family));
        myHotel.addRoom(new Room("302", family));
        myHotel.addRoom(new Room("401", penthouse));
        
        // Event Halls
        myHotel.addRoom(new Room("501", partyHall));
        myHotel.addRoom(new Room("601", banquetHall));
        
        // Budget rooms
        myHotel.addRoom(new Room("701", standard));
        myHotel.addRoom(new Room("702", economy));

        HttpServer server = HttpServer.create(new InetSocketAddress(8085), 0);
        server.createContext("/api/availability", new AvailabilityHandler());
        server.createContext("/api/book", new BookingHandler());
        server.createContext("/api/send-otp", new OtpHandler());
        
        // Handle CORS
        server.createContext("/", exchange -> {
            setCORS(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
            } else {
                exchange.sendResponseHeaders(404, -1);
            }
        });

        server.setExecutor(null); 
        System.out.println("Backend server running on http://localhost:8085/");
        server.start();
    }

    // Helper for CORS
    private static void setCORS(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    static class AvailabilityHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            setCORS(t);
            if (t.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                t.sendResponseHeaders(204, -1);
                return;
            }

            try {
                // Parse query params manually: ?category=Deluxe&checkIn=2024-06-01&checkOut=2024-06-05
                String query = t.getRequestURI().getQuery();
                String catStr = extractParam(query, "category");
                String checkInStr = extractParam(query, "checkIn");
                String checkOutStr = extractParam(query, "checkOut");

                LocalDate checkIn = LocalDate.parse(checkInStr);
                LocalDate checkOut = LocalDate.parse(checkOutStr);

                List<Room> allRooms = myHotel.getAllRooms();
                
                // Construct JSON array of available rooms
                StringBuilder sb = new StringBuilder();
                sb.append("[");
                boolean first = true;
                for (Room r : allRooms) {
                    if (r.isAvailable(checkIn, checkOut)) {
                        if (!first) sb.append(",");
                        first = false;
                        sb.append("{")
                          .append("\"roomNumber\":\"").append(r.getRoomNumber()).append("\",")
                          .append("\"category\":\"").append(r.getCategory().getName()).append("\"")
                          .append("}");
                    }
                }
                sb.append("]");
                
                String response = "{\"available\": true, \"rooms\": " + sb.toString() + "}";
                t.getResponseHeaders().set("Content-Type", "application/json");
                t.sendResponseHeaders(200, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } catch (Exception e) {
                String response = "{\"error\": \"" + e.getMessage() + "\"}";
                t.getResponseHeaders().set("Content-Type", "application/json");
                t.sendResponseHeaders(400, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }

    static class BookingHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            setCORS(t);
            if (t.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                t.sendResponseHeaders(204, -1);
                return;
            }

            try {
                InputStream is = t.getRequestBody();
                String body = new String(is.readAllBytes());
                
                // Very basic JSON parsing
                String catStr = extractJsonString(body, "category");
                String checkInStr = extractJsonString(body, "checkIn");
                String checkOutStr = extractJsonString(body, "checkOut");

                RoomCategory cat = deluxe;
                if ("Penthouse".equalsIgnoreCase(catStr)) cat = penthouse;
                else if ("Family".equalsIgnoreCase(catStr)) cat = family;
                else if ("Suite".equalsIgnoreCase(catStr)) cat = suite;
                else if ("Party Hall".equalsIgnoreCase(catStr)) cat = partyHall;
                else if ("Banquet Hall".equalsIgnoreCase(catStr)) cat = banquetHall;
                else if ("Standard".equalsIgnoreCase(catStr)) cat = standard;
                else if ("Economy".equalsIgnoreCase(catStr)) cat = economy;

                LocalDate checkIn = LocalDate.parse(checkInStr);
                LocalDate checkOut = LocalDate.parse(checkOutStr);

                Booking booking = myHotel.bookRoom(cat, checkIn, checkOut);
                
                String response = String.format(
                    "{\"success\": true, \"bookingId\": \"%s\", \"roomNumber\": \"%s\", \"totalTariff\": %.2f}",
                    booking.getBookingId(), booking.getRoom().getRoomNumber(), booking.getFinalTariff()
                );

                t.getResponseHeaders().set("Content-Type", "application/json");
                t.sendResponseHeaders(200, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();

            } catch (Exception e) {
                String response = "{\"success\": false, \"error\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}";
                t.getResponseHeaders().set("Content-Type", "application/json");
                t.sendResponseHeaders(400, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }

    static class OtpHandler implements HttpHandler {
        // REPLACE THESE WITH REAL TWILIO CREDENTIALS
        private static final String TWILIO_ACCOUNT_SID = "YOUR_TWILIO_ACCOUNT_SID";
        private static final String TWILIO_AUTH_TOKEN = "YOUR_TWILIO_AUTH_TOKEN";
        private static final String TWILIO_PHONE_NUMBER = "YOUR_TWILIO_PHONE_NUMBER";

        @Override
        public void handle(HttpExchange t) throws IOException {
            setCORS(t);
            if (t.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                t.sendResponseHeaders(204, -1);
                return;
            }

            try {
                InputStream is = t.getRequestBody();
                String body = new String(is.readAllBytes());
                String phone = extractJsonString(body, "phone");
                
                if (phone == null || phone.isEmpty()) {
                    throw new Exception("Phone number is required");
                }
                
                String otp = String.valueOf((int)(100000 + Math.random() * 900000));
                
                if (TWILIO_ACCOUNT_SID.startsWith("YOUR_")) {
                    throw new Exception("Twilio credentials not configured in backend Server.java");
                }
                
                String url = "https://api.twilio.com/2010-04-01/Accounts/" + TWILIO_ACCOUNT_SID + "/Messages.json";
                String auth = TWILIO_ACCOUNT_SID + ":" + TWILIO_AUTH_TOKEN;
                String authHeader = "Basic " + java.util.Base64.getEncoder().encodeToString(auth.getBytes());
                
                String formData = "To=" + java.net.URLEncoder.encode(phone, "UTF-8") +
                                  "&From=" + java.net.URLEncoder.encode(TWILIO_PHONE_NUMBER, "UTF-8") +
                                  "&Body=" + java.net.URLEncoder.encode("Your Grand Hotel verification OTP is: " + otp, "UTF-8");
                                  
                java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .header("Authorization", authHeader)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(formData))
                    .build();
                    
                java.net.http.HttpResponse<String> response = java.net.http.HttpClient.newHttpClient()
                    .send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
                    
                if (response.statusCode() != 201) {
                    throw new Exception("Twilio API Error");
                }
                
                String jsonResponse = "{\"success\": true, \"otp\": \"" + otp + "\"}";
                t.getResponseHeaders().set("Content-Type", "application/json");
                t.sendResponseHeaders(200, jsonResponse.length());
                OutputStream os = t.getResponseBody();
                os.write(jsonResponse.getBytes());
                os.close();

            } catch (Exception e) {
                String response = "{\"success\": false, \"error\": \"" + e.getMessage().replace("\"", "\\\"").replace("\n", " ") + "\"}";
                t.getResponseHeaders().set("Content-Type", "application/json");
                t.sendResponseHeaders(400, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }

    private static String extractParam(String query, String param) {
        if (query == null) return null;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=");
            if (kv.length == 2 && kv[0].equals(param)) {
                return kv[1];
            }
        }
        return null;
    }
    
    private static String extractJsonString(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start == -1) {
            search = "\"" + key + "\": \"";
            start = json.indexOf(search);
        }
        if (start == -1) return null;
        start += search.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }
}
