package com.hotelbooking.services;

import com.hotelbooking.exceptions.InvalidDateRangeException;
import com.hotelbooking.exceptions.RoomNotAvailableException;
import com.hotelbooking.models.Booking;
import com.hotelbooking.models.Room;
import com.hotelbooking.models.RoomCategory;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Hotel {
    private final String name;
    private final List<Room> rooms;
    private final List<Booking> bookings;

    public Hotel(String name) {
        this.name = name;
        this.rooms = new ArrayList<>();
        this.bookings = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<Room> getAllRooms() {
        return rooms;
    }

    public void addRoom(Room room) {
        rooms.add(room);
    }

    public List<Room> getRoomsByCategory(RoomCategory category) {
        return rooms.stream()
                .filter(r -> r.getCategory().equals(category))
                .collect(Collectors.toList());
    }

    public List<Room> checkAvailability(RoomCategory category, LocalDate checkIn, LocalDate checkOut) throws InvalidDateRangeException {
        if (!checkIn.isBefore(checkOut)) {
            throw new InvalidDateRangeException("Check-out date must be after check-in date.");
        }
        
        return getRoomsByCategory(category).stream()
                .filter(r -> r.isAvailable(checkIn, checkOut))
                .collect(Collectors.toList());
    }

    private double getOccupancyRate(LocalDate targetDate) {
        if (rooms.isEmpty()) {
            return 0.0;
        }
        LocalDate nextDay = targetDate.plusDays(1);
        long occupiedCount = rooms.stream()
                .filter(r -> !r.isAvailable(targetDate, nextDay))
                .count();
        return (double) occupiedCount / rooms.size();
    }

    public Booking bookRoom(RoomCategory category, LocalDate checkIn, LocalDate checkOut) throws InvalidDateRangeException, RoomNotAvailableException {
        List<Room> availableRooms = checkAvailability(category, checkIn, checkOut);
        
        if (availableRooms.isEmpty()) {
            throw new RoomNotAvailableException("No available rooms in category " + category.getName() + " for the given dates.");
        }
        
        Room roomToBook = availableRooms.get(0);
        
        double totalTariff = 0.0;
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        
        for (long i = 0; i < nights; i++) {
            LocalDate currentDate = checkIn.plusDays(i);
            double occupancy = getOccupancyRate(currentDate);
            totalTariff += PricingEngine.calculateDailyPrice(category.getBasePrice(), currentDate, occupancy);
        }
        
        roomToBook.book(checkIn, checkOut);
        
        Booking booking = new Booking(roomToBook, checkIn, checkOut, totalTariff);
        bookings.add(booking);
        
        return booking;
    }

    public void cancelBooking(Booking booking) {
        if (bookings.contains(booking) && !booking.isCancelled()) {
            try {
                booking.cancel();
            } catch (Exception e) {
                // Handle cancellation errors safely
                System.err.println("Failed to cancel booking: " + e.getMessage());
            }
        }
    }
}
