package com.hotelbooking.models;

import com.hotelbooking.exceptions.InvalidCancellationException;
import com.hotelbooking.exceptions.RoomNotAvailableException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Room {
    private final String roomNumber;
    private final RoomCategory category;
    
    // Internal class to manage booked ranges
    private static class DateRange {
        LocalDate checkIn;
        LocalDate checkOut;
        
        DateRange(LocalDate checkIn, LocalDate checkOut) {
            this.checkIn = checkIn;
            this.checkOut = checkOut;
        }
    }
    
    private final List<DateRange> bookedRanges;

    public Room(String roomNumber, RoomCategory category) {
        this.roomNumber = roomNumber;
        this.category = category;
        this.bookedRanges = new ArrayList<>();
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public RoomCategory getCategory() {
        return category;
    }

    public boolean isAvailable(LocalDate checkIn, LocalDate checkOut) {
        for (DateRange range : bookedRanges) {
            // Overlap condition: max(start1, start2) < min(end1, end2)
            LocalDate maxStart = checkIn.isAfter(range.checkIn) ? checkIn : range.checkIn;
            LocalDate minEnd = checkOut.isBefore(range.checkOut) ? checkOut : range.checkOut;
            if (maxStart.isBefore(minEnd)) {
                return false; // Overlap detected
            }
        }
        return true;
    }

    public void book(LocalDate checkIn, LocalDate checkOut) throws RoomNotAvailableException {
        if (!isAvailable(checkIn, checkOut)) {
            throw new RoomNotAvailableException("Room " + roomNumber + " is already booked for these dates.");
        }
        bookedRanges.add(new DateRange(checkIn, checkOut));
        // Keep the ranges sorted for better management
        bookedRanges.sort(Comparator.comparing(r -> r.checkIn));
    }

    public void cancel(LocalDate checkIn, LocalDate checkOut) throws InvalidCancellationException {
        boolean removed = bookedRanges.removeIf(r -> r.checkIn.equals(checkIn) && r.checkOut.equals(checkOut));
        if (!removed) {
            throw new InvalidCancellationException("Booking for room " + roomNumber + " from " + checkIn + " to " + checkOut + " not found.");
        }
    }

    @Override
    public String toString() {
        return "Room{" + roomNumber + ", " + category.getName() + "}";
    }
}
