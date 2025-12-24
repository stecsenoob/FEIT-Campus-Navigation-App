# FEIT Places (Android App)

Android апликација за преглед на локации/објекти на ФЕИТ (библиотека, амфитеатри, лаборатории, кантина, служби), пребарување, филтрирање, мапа со маркери и „Favorites“ функционалност.

---

## 1. Архитектура (System Architecture)

Апликацијата е организирана во 3 слоја:

### A) UI Layer (Presentation)
- **Activities/Fragments**
    - `HomeFragment` – листа на места + Search + Filter + додавање/тргање Favorites (❤️)
    - `PlaceDetailsFragment` – детална страница за избрано место
    - `MapFragment` – Google Map со маркери за сите места + инфо-попап
    - `FavoritesFragment` – листа на омилени места + можност за отстранување (❤️)
- **Adapters**
    - `PlaceAdapter` – RecyclerView адаптер за Home листата (со срце)
    - `FavoritesAdapter` – RecyclerView адаптер за Favorites листата (со срце)

### B) Data Layer (Persistence / Local DB)
- **Room Database**
    - `AppDatabase` – Room DB singleton (`places_db`)
    - `PlaceEntity` – ентитет/табела `places`
    - `PlaceDao` – SQL query методи (getAll, getByTitle, getFavorites, setFavorite…)

### C) External Services
- **Google Maps SDK** – мапа и маркери (MapFragment)
- **(Optional) Google Directions API** – ако е имплементирано цртање рута со polyline

---

## 2. Технологии
- Java (Android)
- Room (SQLite persistence)
- RecyclerView + Adapters
- Google Maps SDK
- Material Components UI

---

## 3. Инсталација и Setup

### 3.1 Предуслови
- Android Studio (препорачано: најнова стабилна верзија)
- Android SDK (API 24+ препорачано)
- Google Maps API key (за мапата)

### 3.2 Клонирање на проектот
```bash
git clone <YOUR_GITHUB_REPO_URL>
