// Navbar scroll effect
window.addEventListener('scroll', () => {
    const navbar = document.querySelector('.navbar');
    if (window.scrollY > 50) {
        navbar.classList.add('scrolled');
    } else {
        navbar.classList.remove('scrolled');
    }
});

// Set default dates
document.addEventListener('DOMContentLoaded', () => {
    const today = new Date();
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);
    
    // Format to YYYY-MM-DD
    const formatDate = (date) => date.toISOString().split('T')[0];
    
    const checkinEl = document.getElementById('checkin');
    const checkoutEl = document.getElementById('checkout');
    if (checkinEl) checkinEl.value = formatDate(today);
    if (checkoutEl) checkoutEl.value = formatDate(tomorrow);
});

// Handle Booking Form (Availability Check)
const bookingForm = document.getElementById('booking-form');
if (bookingForm) {
    bookingForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const checkin = document.getElementById('checkin').value;
        const checkout = document.getElementById('checkout').value;
        const guests = document.getElementById('guests').value;
        const msgDiv = document.getElementById('booking-message');
        
        if(new Date(checkin) >= new Date(checkout)) {
            msgDiv.className = 'message-success';
            msgDiv.style.borderLeftColor = '#e74c3c';
            msgDiv.style.background = 'rgba(231, 76, 60, 0.2)';
            msgDiv.textContent = 'Check-out date must be after check-in date.';
            msgDiv.classList.remove('hidden');
            return;
        }

        const btn = e.target.querySelector('button');
        const origHtml = btn.innerHTML;
        btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Searching...';
        msgDiv.classList.add('hidden');

        // Redirect to results page
        setTimeout(() => {
            btn.innerHTML = origHtml;
            window.location.href = `results.html?checkin=${checkin}&checkout=${checkout}&guests=${guests}`;
        }, 800);
    });
}

// Handle room selection (Booking)
function bookRoom(type) {
    let checkin = null;
    let checkout = null;
    
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has('checkin') && urlParams.has('checkout')) {
        checkin = urlParams.get('checkin');
        checkout = urlParams.get('checkout');
    } else {
        const inEl = document.getElementById('checkin');
        const outEl = document.getElementById('checkout');
        if (inEl && outEl) {
            checkin = inEl.value;
            checkout = outEl.value;
        }
    }
    
    if(!checkin || !checkout) {
        alert("Please select check-in and check-out dates first.");
        return;
    }

    const btn = event.target;
    const origHtml = btn.innerHTML;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Loading...';

    setTimeout(() => {
        btn.innerHTML = origHtml;
        window.location.href = `payment.html?category=${type}&checkin=${checkin}&checkout=${checkout}`;
    }, 600);
}

// Currency Converter Logic
const basePrices = {
    'Deluxe': 200,
    'Suite': 500,
    'Family': 800,
    'Penthouse': 3500,
    'Party Hall': 5000,
    'Banquet Hall': 12000,
    'Standard': 50,
    'Economy': 40
};

function changeCurrency() {
    const selector = document.getElementById('currency-selector');
    if (!selector) return;
    
    const selectedOption = selector.options[selector.selectedIndex];
    const rate = parseFloat(selectedOption.getAttribute('data-rate'));
    const symbol = selectedOption.getAttribute('data-symbol');
    const code = selectedOption.value;
    
    localStorage.setItem('currencyCode', code);
    localStorage.setItem('currencySymbol', symbol);
    localStorage.setItem('currencyRate', rate);
    
    const priceElements = document.querySelectorAll('.room-price .price');
    if (priceElements.length >= 6) {
        priceElements[0].innerText = `${symbol}${(basePrices['Deluxe'] * rate).toFixed(0)}`;
        priceElements[1].innerText = `${symbol}${(basePrices['Suite'] * rate).toFixed(0)}`;
        priceElements[2].innerText = `${symbol}${(basePrices['Family'] * rate).toFixed(0)}`;
        priceElements[3].innerText = `${symbol}${(basePrices['Penthouse'] * rate).toFixed(0)}`;
        priceElements[4].innerText = `${symbol}${(basePrices['Standard'] * rate).toFixed(0)}`;
        priceElements[5].innerText = `${symbol}${(basePrices['Economy'] * rate).toFixed(0)}`;
    } else if (priceElements.length >= 4) {
        priceElements[0].innerText = `${symbol}${(basePrices['Deluxe'] * rate).toFixed(0)}`;
        priceElements[1].innerText = `${symbol}${(basePrices['Suite'] * rate).toFixed(0)}`;
        priceElements[2].innerText = `${symbol}${(basePrices['Family'] * rate).toFixed(0)}`;
        priceElements[3].innerText = `${symbol}${(basePrices['Penthouse'] * rate).toFixed(0)}`;
    } else if (priceElements.length >= 2) {
        priceElements[0].innerText = `${symbol}${(basePrices['Deluxe'] * rate).toFixed(0)}`;
        priceElements[1].innerText = `${symbol}${(basePrices['Suite'] * rate).toFixed(0)}`;
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const code = localStorage.getItem('currencyCode');
    if (code) {
        const selector = document.getElementById('currency-selector');
        if (selector) {
            selector.value = code;
            changeCurrency();
        }
    }
});
