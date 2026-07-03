package com.hotelbooking.models;

import com.hotelbooking.exceptions.InvalidCancellationException;

import java.time.LocalDate;
import java.util.UUID;

public class Booking {
    private final String bookingId;
    private final Room room;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final double finalTariff;
    private boolean isCancelled;

    public Booking(Room room, LocalDate checkIn, LocalDate checkOut, double finalTariff) {
        this.bookingId = UUID.randomUUID().toString();
        this.room = room;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.finalTariff = finalTariff;
        this.isCancelled = false;
    }

    public String getBookingId() {
        return bookingId;
    }

    public Room getRoom() {
        return room;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public double getFinalTariff() {
        return finalTariff;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void cancel() throws InvalidCancellationException {
        if (isCancelled) {
            throw new InvalidCancellationException("Booking " + bookingId + " is already cancelled.");
        }
        room.cancel(checkIn, checkOut);
        isCancelled = true;
    }

    @Override
    public String toString() {
        String status = isCancelled ? "Cancelled" : "Active";
        return String.format("Booking{id='%s', room=%s, status=%s, total=%.2f}", 
                bookingId.substring(0, 8), room.getRoomNumber(), status, finalTariff);
    }
}
