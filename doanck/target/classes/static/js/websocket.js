let socket=new SockJS('/ws')

let stomp=Stomp.over(socket)

stomp.connect({},function(){

stomp.subscribe('/topic/seats',function(message){

let seat=message.body

document.querySelectorAll(".seat").forEach(btn=>{

if(btn.innerText===seat){

btn.classList.remove("available")
btn.classList.remove("selected")
btn.classList.add("booked")

}

})

})

})