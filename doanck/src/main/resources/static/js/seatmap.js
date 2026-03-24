let selectedSeats = []

function createSeats(){

const rows=["A","B","C","D","E","F","G","H","I","J"]

let map=document.getElementById("seat-map")

rows.forEach(r=>{

let row=document.createElement("div")
row.className="row"

for(let i=1;i<=15;i++){

let seat=r+i

let btn=document.createElement("div")

btn.innerText=seat
btn.dataset.seat = seat

btn.className="seat"

btn.onclick=function(){

if(btn.classList.contains("booked")) return

btn.classList.toggle("selected")

if(selectedSeats.includes(seat)){

selectedSeats=selectedSeats.filter(s=>s!==seat)

}else{

selectedSeats.push(seat)

}

updateTotal()

}

row.appendChild(btn)

}

map.appendChild(row)

})

}

// =========================
// SET BOOKED SEATS
// =========================

function setBookedSeats(bookedSeats){

bookedSeats.forEach(ticket => {

let seat = document.querySelector(`[data-seat='${ticket.seatNumber}']`)

if(seat){
seat.classList.add("booked")
}

})

}

// =========================
// UPDATE TOTAL PRICE
// =========================

function updateTotal(){

let total = selectedSeats.length * price

document.getElementById("total").innerText = total

}

// =========================
// PAYMENT
// =========================

function bookSeats(){

if(selectedSeats.length===0){

alert("Please select seats")

return

}

let showtimeId=document.getElementById("showtimeId").value

window.location.href =
"/payment/create?seats="+selectedSeats.join(",")+
"&showtime="+showtimeId

}