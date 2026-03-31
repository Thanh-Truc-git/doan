const THEME_KEY = "bhtnex_theme";
const FAV_KEY = "bhtnex_favourites_v1";

function getSavedTheme() {
  return localStorage.getItem(THEME_KEY) === "light" ? "light" : "dark";
}

function applyTheme(theme) {
  const finalTheme = theme === "light" ? "light" : "dark";
  document.documentElement.setAttribute("data-theme", finalTheme);
  localStorage.setItem(THEME_KEY, finalTheme);
}

function escapeHtml(value) {
  return String(value || "").replace(/[&<>"']/g, (char) => (
    { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[char]
  ));
}

function readFavourites() {
  try {
    const raw = localStorage.getItem(FAV_KEY);
    const parsed = raw ? JSON.parse(raw) : [];
    return Array.isArray(parsed) ? parsed : [];
  } catch (_) {
    return [];
  }
}

function saveFavourites(items) {
  localStorage.setItem(FAV_KEY, JSON.stringify(items));
}

applyTheme(getSavedTheme());

document.addEventListener("DOMContentLoaded", () => {
  const sidebar = document.querySelector(".sidebar");
  const collapseBtn = document.getElementById("collapseBtn");
  const menuBtn = document.getElementById("btnMenu");
  const profileBtn = document.getElementById("profileBtn");
  const profileMenu = document.getElementById("profileMenu");
  const heroTitle = document.getElementById("heroTitle");
  const heroMeta = document.getElementById("heroMeta");
  const heroShowtime = document.getElementById("heroShowtime");
  const heroImg = document.getElementById("heroImg");
  const heroTrailerBtn = document.getElementById("heroTrailerBtn");
  const heroBookBtn = document.getElementById("heroBookBtn");
  const topBookBtn = document.getElementById("topBookBtn");
  const heroImageWrap = document.querySelector(".hero-image");
  const movieCards = document.querySelectorAll(".movie-card");
  const favouritesList = document.getElementById("favouritesList");
  const reveals = document.querySelectorAll("[data-reveal]");

  function syncMenuButton() {
    if (!menuBtn || !sidebar) return;
    menuBtn.setAttribute("aria-expanded", String(sidebar.classList.contains("open")));
  }

  function updateActiveNavByPath() {
    const currentPath = window.location.pathname;
    document.querySelectorAll(".nav-item").forEach((item) => {
      const href = item.getAttribute("href");
      item.classList.toggle("active", href === currentPath || (href !== "/" && currentPath.startsWith(href)));
    });
  }

  if (collapseBtn && sidebar) {
    collapseBtn.addEventListener("click", () => {
      if (window.innerWidth <= 980) {
        sidebar.classList.toggle("open");
        syncMenuButton();
        return;
      }

      const collapsed = sidebar.classList.toggle("collapsed");
      collapseBtn.setAttribute("aria-pressed", String(collapsed));
      localStorage.setItem("sidebarCollapsed", collapsed ? "1" : "0");
    });

    if (window.innerWidth > 980 && localStorage.getItem("sidebarCollapsed") === "1") {
      sidebar.classList.add("collapsed");
      collapseBtn.setAttribute("aria-pressed", "true");
    }
  }

  if (menuBtn && sidebar) {
    menuBtn.addEventListener("click", () => {
      sidebar.classList.toggle("open");
      syncMenuButton();
    });
  }

  window.addEventListener("resize", () => {
    if (!sidebar) return;

    if (window.innerWidth > 980) {
      sidebar.classList.remove("open");
      syncMenuButton();
      return;
    }

    sidebar.classList.remove("collapsed");
    if (collapseBtn) collapseBtn.setAttribute("aria-pressed", "false");
  });

  if (profileBtn && profileMenu) {
    profileBtn.addEventListener("click", (event) => {
      event.stopPropagation();
      const open = profileMenu.getAttribute("aria-hidden") === "false";
      profileMenu.setAttribute("aria-hidden", String(!open));
      profileBtn.setAttribute("aria-expanded", String(!open));
    });

    document.addEventListener("click", (event) => {
      if (profileBtn.contains(event.target) || profileMenu.contains(event.target)) return;
      profileMenu.setAttribute("aria-hidden", "true");
      profileBtn.setAttribute("aria-expanded", "false");
    });
  }

  if (reveals.length) {
    const observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (!entry.isIntersecting) return;
        entry.target.classList.add("is-visible");
        observer.unobserve(entry.target);
      });
    }, { threshold: 0.12 });

    reveals.forEach((item) => observer.observe(item));
  }

  function isFavourited(movieId) {
    return readFavourites().some((item) => String(item.id) === String(movieId));
  }

  function setFavButtonState(button, active) {
    button.setAttribute("aria-pressed", String(active));
    button.classList.toggle("active", active);
    button.textContent = active ? "♥" : "♡";
  }

  function extractMovieFromCard(card) {
    return {
      id: card.dataset.id,
      title: card.dataset.title || "Unknown",
      genre: card.dataset.genre || "",
      duration: card.dataset.duration || "",
      poster: card.dataset.poster || "/assets/img/hero.jpg"
    };
  }

  function renderFavouritesPage() {
    if (!favouritesList) return;

    const items = readFavourites();
    if (items.length === 0) {
      favouritesList.innerHTML = '<p class="empty">Ban chua co phim yeu thich nao.</p>';
      return;
    }

    favouritesList.innerHTML = items.map((item) => `
      <article class="fav-card" data-reveal>
        <div class="fav-poster">
          <img src="${escapeHtml(item.poster)}" alt="${escapeHtml(item.title)}">
        </div>
        <div class="fav-content">
          <h3>${escapeHtml(item.title)}</h3>
          <p>${escapeHtml(item.duration ? `${item.duration} phut • ` : "")}${escapeHtml(item.genre || "Dang cap nhat")}</p>
          <div class="fav-actions">
            <a class="btn-book" href="/seat-map/${encodeURIComponent(item.id)}">Dat ve</a>
            <button type="button" class="btn-fav-remove" data-id="${escapeHtml(item.id)}">Bo yeu thich</button>
          </div>
        </div>
      </article>
    `).join("");
  }

  function buildMeta(card) {
    const duration = card.dataset.duration ? `${card.dataset.duration} phut` : "Dang cap nhat";
    const genre = card.dataset.genre || "The loai dang cap nhat";
    return `${duration} • ${genre}`;
  }

  function buildShowtime(card) {
    return card.dataset.showtime ? `Lich chieu: ${card.dataset.showtime}` : "Chua co lich chieu";
  }

  function getCardShowtimeMs(card) {
    const iso = card.dataset.showtimeIso;
    if (!iso) return null;
    const parsed = Date.parse(iso);
    return Number.isNaN(parsed) ? null : parsed;
  }

  function scheduleMovieRefresh() {
    if (!movieCards.length) return;

    const now = Date.now();
    const upcomingTimes = Array.from(movieCards)
      .map((card) => getCardShowtimeMs(card))
      .filter((time) => time !== null && time > now);

    if (!upcomingTimes.length) return;

    const nextExpiry = Math.min(...upcomingTimes);
    const delay = Math.max(nextExpiry - now + 1000, 1000);
    window.setTimeout(() => window.location.reload(), delay);
  }

  function reloadWhenAnyMovieExpires() {
    const hasExpiredMovie = Array.from(movieCards).some((card) => {
      const showtimeMs = getCardShowtimeMs(card);
      return showtimeMs !== null && showtimeMs <= Date.now();
    });

    if (hasExpiredMovie) {
      window.location.reload();
    }
  }

  function getActiveMovieId() {
    const activeCard = document.querySelector(".movie-card.active") || movieCards[0];
    return activeCard ? activeCard.dataset.id : null;
  }

  function updateHero(card, shouldScroll) {
    if (!card) return;

    const title = card.dataset.title || "Chon bo phim ban muon xem";
    const poster = card.dataset.poster || "/assets/img/hero.jpg";
    const trailer = card.dataset.trailer || "";
    const movieId = card.dataset.id;

    movieCards.forEach((item) => item.classList.remove("active"));
    card.classList.add("active");

    if (heroImg) heroImg.style.opacity = "0";

    window.setTimeout(() => {
      if (heroTitle) heroTitle.textContent = title;
      if (heroMeta) heroMeta.textContent = buildMeta(card);
      if (heroShowtime) heroShowtime.textContent = buildShowtime(card);
      if (heroImg) {
        heroImg.src = poster;
        heroImg.style.opacity = "1";
      }
      if (heroImageWrap) {
        heroImageWrap.style.setProperty("--hero-image", `url("${poster}")`);
      }
      if (heroBookBtn && movieId) heroBookBtn.setAttribute("href", `/seat-map/${movieId}`);
      if (topBookBtn && movieId) topBookBtn.setAttribute("href", `/seat-map/${movieId}`);
      if (heroTrailerBtn) {
        if (trailer) {
          heroTrailerBtn.setAttribute("href", trailer);
          heroTrailerBtn.removeAttribute("aria-disabled");
          heroTrailerBtn.style.pointerEvents = "auto";
          heroTrailerBtn.style.opacity = "1";
        } else {
          heroTrailerBtn.setAttribute("href", "#");
          heroTrailerBtn.setAttribute("aria-disabled", "true");
          heroTrailerBtn.style.pointerEvents = "none";
          heroTrailerBtn.style.opacity = "0.58";
        }
      }
    }, 120);

    if (shouldScroll) {
      window.scrollTo({ top: 0, behavior: "smooth" });
    }
  }

  if (heroTrailerBtn) {
    heroTrailerBtn.addEventListener("click", (event) => {
      if (heroTrailerBtn.getAttribute("aria-disabled") !== "true") return;
      event.preventDefault();
      alert("Phim nay hien chua co trailer.");
    });
  }

  movieCards.forEach((card) => {
    const favBtn = card.querySelector(".btn-fav");
    if (favBtn) {
      setFavButtonState(favBtn, isFavourited(card.dataset.id));
      favBtn.addEventListener("click", (event) => {
        event.preventDefault();
        event.stopPropagation();

        const movie = extractMovieFromCard(card);
        const items = readFavourites();
        const existingIndex = items.findIndex((item) => String(item.id) === String(movie.id));

        if (existingIndex >= 0) {
          items.splice(existingIndex, 1);
          setFavButtonState(favBtn, false);
        } else {
          items.push(movie);
          setFavButtonState(favBtn, true);
        }

        saveFavourites(items);
        renderFavouritesPage();
      });
    }

    card.addEventListener("click", (event) => {
      if (event.target.closest(".btn-book") || event.target.closest(".btn-fav")) return;
      updateHero(card, true);
    });
  });

  document.addEventListener("click", (event) => {
    const removeBtn = event.target.closest(".btn-fav-remove");
    if (!removeBtn) return;

    const movieId = removeBtn.dataset.id;
    const items = readFavourites().filter((item) => String(item.id) !== String(movieId));
    saveFavourites(items);
    renderFavouritesPage();

    movieCards.forEach((card) => {
      if (String(card.dataset.id) !== String(movieId)) return;
      const favBtn = card.querySelector(".btn-fav");
      if (favBtn) setFavButtonState(favBtn, false);
    });
  });

  if (heroBookBtn) {
    heroBookBtn.addEventListener("click", (event) => {
      const activeMovieId = getActiveMovieId();
      if (activeMovieId && /^\d+$/.test(String(activeMovieId))) return;
      event.preventDefault();
      alert("Khong tim thay phim hop le de dat ve.");
    });
  }

  if (movieCards.length) {
    const selectedCard = document.querySelector(".movie-card.active") || movieCards[0];
    updateHero(selectedCard, false);
    reloadWhenAnyMovieExpires();
    scheduleMovieRefresh();
  }

  renderFavouritesPage();
  updateActiveNavByPath();
});
