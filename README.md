Mandiri News (Android)

Aplikasi berita sederhana berbasis Jetpack Compose + Paging 3 yang menampilkan Headlines dan Top Stories dari NewsAPI. UI dirancang vertikal (feed) dengan kartu berita, featured carousel, dan brand top bar.


Fitur
- Featured Carousel: slide berita unggulan (Headlines).
- Feed Vertikal (Top Stories): daftar berita endless-scroll dengan Paging 3.
- Kartu Berita Rapi: gambar kiri, judul + deskripsi + meta (sumber • tanggal terbit).
- Brand Top Bar: logo bulat + teks “Mandiri News”.
- Pencarian (via EverythingPagingSource) — query default “indonesia”.
- Handling State: loading, error, retry.
- Rate-limit aware: penjadwalan request untuk mengurangi HTTP 429.

Teknologi
- Kotlin, Coroutines/Flow
- Jetpack Compose (Material 3)
- Paging 3 (Compose integration)
- Retrofit 2 + OkHttp 3
- Coil (pemanggilan gambar)
- NewsAPI (sumber data)
