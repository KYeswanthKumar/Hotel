package com.hotelbooking.services;

import java.time.LocalDate;
import java.time.Month;

public class PricingEngine {
    
    public static double calculateDailyPrice(double basePrice, LocalDate currentDate, double occupancyRate) {
        double dailyPrice = basePrice;
        
        // Seasonal surcharge: Peak summer months (June, July, August) get 20% surge
        Month month = currentDate.getMonth();
        if (month == Month.JUNE || month == Month.JULY || month == Month.AUGUST) {
            dailyPrice *= 1.20;
        }
        
        // Occupancy surcharge: if occupancy > 80%, 10% surge
        if (occupancyRate > 0.80) {
            dailyPrice *= 1.10;
        }
        
        return dailyPrice;
    }
}
