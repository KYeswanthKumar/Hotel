const express = require('express');
const cors = require('cors');

const app = express();
app.use(cors());
app.use(express.json());

// In-memory data store for the demo (Note: Vercel resets this after ~15 mins of inactivity)
const rooms = [
    { roomNumber: "101", category: "Deluxe", bookings: [], basePrice: 150 },
    { roomNumber: "102", category: "Deluxe", bookings: [], basePrice: 150 },
    { roomNumber: "201", category: "Suite", bookings: [], basePrice: 300 },
    { roomNumber: "202", category: "Suite", bookings: [], basePrice: 300 },
    { roomNumber: "301", category: "Family", bookings: [], basePrice: 250 },
    { roomNumber: "302", category: "Family", bookings: [], basePrice: 250 },
    { roomNumber: "401", category: "Penthouse", bookings: [], basePrice: 800 },
    { roomNumber: "501", category: "Party Hall", bookings: [], basePrice: 1000 },
    { roomNumber: "601", category: "Banquet Hall", bookings: [], basePrice: 1500 },
    { roomNumber: "701", category: "Standard", bookings: [], basePrice: 50 },
    { roomNumber: "702", category: "Economy", bookings: [], basePrice: 40 },
];

function isAvailable(room, checkIn, checkOut) {
    const inDate = new Date(checkIn);
    const outDate = new Date(checkOut);
    for (let b of room.bookings) {
        const bIn = new Date(b.checkIn);
        const bOut = new Date(b.checkOut);
        if (inDate < bOut && outDate > bIn) {
            return false;
        }
    }
    return true;
}

app.get('/api/availability', (req, res) => {
    const { category, checkIn, checkOut } = req.query;
    if (!checkIn || !checkOut) {
        return res.status(400).json({ error: "Missing dates" });
    }
    
    let availableRooms = rooms.filter(r => isAvailable(r, checkIn, checkOut));
    
    if (category) {
        availableRooms = availableRooms.filter(r => r.category === category);
    }
    
    res.json({
        available: availableRooms.length > 0,
        rooms: availableRooms.map(r => ({ roomNumber: r.roomNumber, category: r.category }))
    });
});

app.post('/api/book', (req, res) => {
    const { category, checkIn, checkOut } = req.body;
    let availableRooms = rooms.filter(r => isAvailable(r, checkIn, checkOut));
    
    if (category) {
        availableRooms = availableRooms.filter(r => r.category === category);
    }
    
    if (availableRooms.length > 0) {
        const room = availableRooms[0];
        room.bookings.push({ checkIn, checkOut });
        
        const inDate = new Date(checkIn);
        const outDate = new Date(checkOut);
        const days = Math.ceil((outDate - inDate) / (1000 * 60 * 60 * 24)) || 1;
        const totalTariff = room.basePrice * days;
        
        const bookingId = "BKG-" + Math.floor(Math.random() * 1000000);
        
        res.json({
            success: true,
            bookingId: bookingId,
            roomNumber: room.roomNumber,
            totalTariff: totalTariff
        });
    } else {
        res.status(400).json({ success: false, error: "No rooms available in this category for these dates." });
    }
});

app.post('/api/send-otp', (req, res) => {
    const { phone } = req.body;
    res.json({ success: true, message: "OTP sent to " + phone });
});

// For local testing
if (require.main === module) {
    app.listen(8085, () => {
        console.log("Vercel Mock Backend running on http://localhost:8085");
    });
}

// Export for Vercel
module.exports = app;
