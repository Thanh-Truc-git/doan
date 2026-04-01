let selectedSeats=[]

// lấy showtime từ URL
const urlParams = new URLSearchParams(window.location.search)
let showtimeId = urlParams.get("showtime")

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

btn.className="seat available"

btn.onclick=function(){

if(btn.classList.contains("booked")) return

btn.classList.toggle("selected")

if(selectedSeats.includes(seat)){

selectedSeats=selectedSeats.filter(s=>s!==seat)

}else{

selectedSeats.push(seat)

}

}

row.appendChild(btn)

}

map.appendChild(row)

})

}

createSeats()

function bookSeats() {

    let selectedSeats = [];

    document.querySelectorAll(".seat.selected").forEach(seat => {
        selectedSeats.push(seat.dataset.seat);
    });

    let showtimeId = document.getElementById("showtimeId").value;

    window.location.href =
        "/payment/create?seats=" + selectedSeats.join(",") +
        "&showtime=" + showtimeId;
}