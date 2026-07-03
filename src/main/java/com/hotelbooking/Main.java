package com.hotelbooking;

import com.hotelbooking.models.RoomCategory;
import com.hotelbooking.models.Room;
import com.hotelbooking.models.Booking;
import com.hotelbooking.services.Hotel;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        try {
            // 1. Initialize Hotel
            Hotel myHotel = new Hotel("The Grand Boutique");

            // 2. Define Room Categories
            RoomCategory deluxe = new RoomCategory("Deluxe", 200.0);
            RoomCategory suite = new RoomCategory("Suite", 500.0);

            // 3. Add Rooms to Hotel
            myHotel.addRoom(new Room("101", deluxe));
            myHotel.addRoom(new Room("102", deluxe));
            myHotel.addRoom(new Room("201", suite));

            System.out.println("--- Welcome to " + myHotel.getName() + " ---");

            // Dates
            LocalDate checkIn = LocalDate.of(2024, 6, 1); // June 1st (Summer Season)
            LocalDate checkOut = LocalDate.of(2024, 6, 5); // 4 nights

            // 4. Check Availability
            List<Room> availableDeluxe = myHotel.checkAvailability(deluxe, checkIn, checkOut);
            System.out.println("Available Deluxe Rooms: " + 
                availableDeluxe.stream().map(Room::getRoomNumber).collect(Collectors.toList()));

            // 5. Book a Room
            System.out.println("\nAttempting to book a Deluxe room...");
            Booking booking1 = myHotel.bookRoom(deluxe, checkIn, checkOut);
            System.out.println("Success! " + booking1);

            // Check availability again
            List<Room> availableDeluxeAfter = myHotel.checkAvailability(deluxe, checkIn, checkOut);
            System.out.println("Available Deluxe Rooms after booking: " + 
                availableDeluxeAfter.stream().map(Room::getRoomNumber).collect(Collectors.toList()));

            // Book the remaining Deluxe room
            System.out.println("\nAttempting to book another Deluxe room...");
            Booking booking2 = myHotel.bookRoom(deluxe, checkIn, checkOut);
            System.out.println("Success! " + booking2);

            // Attempt to book a third Deluxe room (Should fail due to no availability)
            System.out.println("\nAttempting to book a third Deluxe room...");
            try {
                myHotel.bookRoom(deluxe, checkIn, checkOut);
            } catch (Exception e) {
                System.out.println("Booking Failed as expected: " + e.getMessage());
            }

            // 6. Cancel a Booking
            System.out.println("\nCancelling the first booking...");
            myHotel.cancelBooking(booking1);
            System.out.println("Booking 1 Status: " + booking1);

            // Check availability after cancellation
            List<Room> availableDeluxeFinal = myHotel.checkAvailability(deluxe, checkIn, checkOut);
            System.out.println("Available Deluxe Rooms after cancellation: " + 
                availableDeluxeFinal.stream().map(Room::getRoomNumber).collect(Collectors.toList()));
                
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
