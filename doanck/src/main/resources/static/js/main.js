// Dữ liệu mẫu khởi tạo nếu localStorage rỗng
const SAMPLE_NOW = [
  {id:1, title:"Inside Out", info:"10:00 | 102 phút", poster:"assets/img/movie-01.webp"},
  {id:2, title:"Happy Feet", info:"12:30 | 95 phút", poster:"assets/img/movie-02.webp"}
];
const SAMPLE_SOON = [
  {id:11, title:"Harry Potter 1", info:"Sắp chiếu", poster:"assets/img/movie-05.webp"},
  {id:12, title:"Harry Potter 2", info:"Sắp chiếu", poster:"assets/img/movie-06.webp"}
];

const LS_KEY = 'bhtnex_movies_v1';

// Lấy dữ liệu từ localStorage hoặc khởi tạo
function loadMovies(){
  const raw = localStorage.getItem(LS_KEY);
  if(!raw){
    const init = {now: SAMPLE_NOW, soon: SAMPLE_SOON};
    localStorage.setItem(LS_KEY, JSON.stringify(init));
    return init;
  }
  try { return JSON.parse(raw); } catch(e){ return {now:[], soon:[]}; }
}

// Lưu dữ liệu
function saveMovies(data){
  localStorage.setItem(LS_KEY, JSON.stringify(data));
}

// Tạo card DOM
function createCard(m){
  const el = document.createElement('article');
  el.className = 'card';
  el.innerHTML = `
    <div class="thumb" style="background-image:url('${m.poster}')"></div>
    <div class="card-body">
      <div class="card-title">${escapeHtml(m.title)}</div>
      <div class="card-info">${escapeHtml(m.info)}</div>
      <div class="card-actions">
        <button class="btn primary btn-book" data-title="${escapeHtmlAttr(m.title)}">Đặt vé</button>
        <button class="btn btn-remove" data-id="${m.id}">Xóa</button>
      </div>
    </div>
  `;
  return el;
}

// Render cả 2 section
function renderAll(){
  const data = loadMovies();
  const nowEl = document.getElementById('nowShowing');
  const soonEl = document.getElementById('comingSoon');
  nowEl.innerHTML = '';
  soonEl.innerHTML = '';
  data.now.forEach(m => nowEl.appendChild(createCard(m)));
  data.soon.forEach(m => soonEl.appendChild(createCard(m)));
}

// Escape để an toàn khi chèn text
function escapeHtml(str){ return String(str).replace(/[&<>"']/g, s => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[s])); }
function escapeHtmlAttr(str){ return escapeHtml(str).replace(/"/g,'&quot;'); }

// Thêm phim mới từ form
document.getElementById('addMovieForm').addEventListener('submit', (e)=>{
  e.preventDefault();
  const title = document.getElementById('mTitle').value.trim();
  const info = document.getElementById('mInfo').value.trim();
  const poster = document.getElementById('mPoster').value.trim();
  const section = document.getElementById('mSection').value;
  if(!title || !poster){ alert('Vui lòng nhập tên phim và đường dẫn poster'); return; }
  const data = loadMovies();
  const id = Date.now();
  const movie = {id, title, info, poster};
  if(section === 'now') data.now.unshift(movie); else data.soon.unshift(movie);
  saveMovies(data);
  renderAll();
  e.target.reset();
});

// Xóa tất cả
document.getElementById('clearAll').addEventListener('click', ()=>{
  if(!confirm('Xóa tất cả phim trong localStorage?')) return;
  localStorage.removeItem(LS_KEY);
  renderAll();
});

// Event delegation cho Đặt vé và Xóa
document.addEventListener('click', (e)=>{
  if(e.target.matches('.btn-book')){
    const title = e.target.dataset.title;
    openModal(title);
  }
  if(e.target.matches('.btn-remove')){
    const id = Number(e.target.dataset.id);
    removeMovieById(id);
  }
});

// Xóa phim theo id
function removeMovieById(id){
  const data = loadMovies();
  data.now = data.now.filter(m => m.id !== id);
  data.soon = data.soon.filter(m => m.id !== id);
  saveMovies(data);
  renderAll();
}

// Modal đơn giản
const modal = document.getElementById('modal');
const movieName = document.getElementById('movieName');
function openModal(title){
  movieName.value = title;
  modal.setAttribute('aria-hidden','false');
  document.body.style.overflow = 'hidden';
  document.getElementById('ticketCount').focus();
}
function closeModal(){
  modal.setAttribute('aria-hidden','true');
  document.body.style.overflow = '';
}
document.getElementById('modalClose').addEventListener('click', closeModal);
document.getElementById('modalCancel').addEventListener('click', closeModal);
document.getElementById('bookingForm').addEventListener('submit', (ev)=>{
  ev.preventDefault();
  const movie = movieName.value;
  const count = document.getElementById('ticketCount').value;
  alert(`Đặt ${count} vé cho ${movie} (demo).`);
  closeModal();
});

// Sidebar interactions (giữ nguyên)
document.addEventListener('click', (e)=>{
  const btn = e.target.closest('.nav-item');
  if(btn){
    document.querySelectorAll('.nav-item').forEach(n=>n.classList.remove('active'));
    btn.classList.add('active');
  }
});
const collapseBtn = document.getElementById('collapseBtn');
const sidebar = document.querySelector('.sidebar');
collapseBtn.addEventListener('click', ()=>{
  const collapsed = sidebar.classList.toggle('collapsed');
  collapseBtn.setAttribute('aria-pressed', String(collapsed));
});

// Hamburger for mobile
document.getElementById('btnMenu').addEventListener('click', ()=>{
  if(sidebar.style.display === 'block'){ sidebar.style.display = ''; }
  else { sidebar.style.display = 'block'; sidebar.style.position='absolute'; sidebar.style.zIndex=60; sidebar.style.left=0; sidebar.style.top=0; sidebar.style.height='100%'; }
});

document.addEventListener('DOMContentLoaded', () => {
  const video = document.getElementById('heroVideo');
  const playBtn = document.getElementById('videoPlayBtn');
  const muteBtn = document.getElementById('videoMuteBtn');
  const overlay = document.getElementById('videoOverlay');

  // Thử autoplay muted
  if(video){
    video.muted = true;
    video.loop = true;
    video.play().catch(()=> {
      // autoplay bị chặn, hiển thị overlay để user click
      overlay.style.display = 'flex';
    });
  }

  // Khi user nhấn Play: bật play và bật âm thanh (tùy chọn)
  playBtn?.addEventListener('click', async () => {
    try {
      video.muted = false; // bật tiếng
      await video.play();
      overlay.style.display = 'none';
    } catch(e){
      // nếu vẫn bị chặn, bật muted play rồi yêu cầu tương tác khác
      console.warn('Play failed', e);
    }
  });

  // Nút bật/tắt âm
  muteBtn?.addEventListener('click', () => {
    if(!video) return;
    video.muted = !video.muted;
    muteBtn.textContent = video.muted ? 'Bật âm' : 'Tắt âm';
  });

  // Khi user scroll ra khỏi view, tạm dừng (tối ưu UX)
  const io = new IntersectionObserver(entries => {
    entries.forEach(entry => {
      if(entry.target === video){
        if(!entry.isIntersecting) video.pause();
        else if(video.paused && video.getAttribute('data-autoplay') === '1') video.play();
      }
    });
  }, {threshold: 0.5});
  io.observe(video);
});


// Init render
renderAll();
