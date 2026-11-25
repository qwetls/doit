# do.it

Aplikasi pencatat uang yang ringan — money management untuk gaji Rp1.000.000/bulan.

**v1.9.0 — Tombol pensil di semua yang bisa diubah:**
- Pensil di tiap pos anggaran tetap (Belanja harian, Dana darurat, Tabungan) — ketuk, isi jumlah baru, simpan; form "Ubah anggaran" kini hanya untuk Pemasukan bulanan.
- Pensil di tiap kartu dompet (di samping tombol hapus) — membuka dialog ubah dompet.
- Pensil di tiap transaksi terbaru di Beranda — edit langsung tanpa buka Riwayat; hapus tetap hanya di Riwayat.
- Tanpa perubahan database (tetap v6).

**v1.7.0 — Samakan persis fitur + UI contoh FluxCash:**
- Lampiran Struk foto di Catat Transaksi (tombol "Unggah Foto Struk" + thumbnail, tersimpan internal, tampil juga saat ubah transaksi, ikut terhapus saat transaksi dihapus).
- + Tambah Pos Anggaran di bawah 3 pos tetap (Belanja/Darurat/Tabungan) — tambah/ubah/hapus, total overview ikut menghitung pos tambahan.
- Unduh Laporan Arus Kas (PDF) di Analisis — PDF asli dibagikan lewat FileProvider; ekspor CSV tetap ada.
- Pilih rentang tanggal sendiri di Analisis (tombol kalender + pemilih rentang tanggal).
- Kartu Smart Insights AI — judul + label efisiensi + persen nyata dari data (perubahan pengeluaran vs periode sebelumnya, tingkat tabungan).
- Migrasi aman ke database v5 (kolom struk + tabel pos anggaran). Pill "Alokasi Kas Otomatis" sengaja tidak dibuat (tidak ada perilaku jelas di baliknya).

**v1.6.0 — UI baru ala FluxCash (tema gelap emerald):**
- Tampilan dirombak total: tema gelap (#0E141C), aksen emerald, font Plus Jakarta Sans (judul/angka) + Inter (isi), kartu rounded-2xl, bottom bar baru (Beranda, Analisis, + di tengah, Anggaran, Akun).
- Beranda: Total Saldo Bersih + pemilih bulan + tombol mata + tren vs bulan lalu; kartu Pemasukan/Pengeluaran + % + label sehat/terkendali; grafik tren arus kas 7 hari + badge puncak; 4 aksi cepat (Transfer, Tarik Kas, Atur Pos, Ekspor); Transaksi Terbaru + nama dompet per baris.
- Analisis: filter Minggu Ini / Bulan Ini / 3 Bulan / Tahun Ini; Arus Kas Bersih + surplus % + bar proporsi; donut + legend; Insight dari pola catatan; kategori pengeluaran terbesar terurut; ekspor tetap CSV.
- Catat Transaksi: switcher Pengeluaran/Pemasukan/Transfer, nominal besar, grid kategori 3 kolom, insight mikro (% dari sisa anggaran).
- Anggaran: Sisa Anggaran Bulanan + progress bar, pos anggaran + status, grid dompet 2x2; semua fitur lama tetap ada. Tanpa perubahan skema DB (v4 tetap). Lampiran foto struk belum ada (butuh penyimpanan + kolom baru) — ditunda.

**v1.5.1 — Kembali ke desain v1.4.0 (Beranda lengkap):**
- Desain minimalis v1.5.0 dibatalkan — Beranda kembali lengkap seperti v1.4.0: grafik 7 hari / 6 bulan, donut per kategori, cincin anggaran, kartu target tabungan, kartu Perhatian, dan kartu ringkasan dompet. Semua fitur v1.4.0 tetap ada. Tanpa perubahan skema DB (v4 tetap).

**v1.5.0 — Minimalis (dibatalkan, diganti v1.5.1):**
- Beranda minimalis: saldo besar + Masuk/Keluar/Sisa + tombol + Pemasukan/Pengeluaran + baris tipis progres laptop + 5 transaksi terakhir; grafik/donut/Insight pindah ke Laporan. Dinilai kurang bagus, dikembalikan ke desain v1.4.0.

**v1.4.0 — Desain baru ala app bank + Rp & Dollar:**
- Beranda ala app bank: avatar profil paling kiri (ketuk → Profil & Pengaturan), sapaan "Halo, Adrien", total saldo besar dengan animasi, baris Masuk / Keluar / Sisa bulan ini, tombol besar **+ Pemasukan** (hijau) / **+ Pengeluaran** (merah) yang membuka sheet dengan jenis yang benar. Pemasukan universal (sumber tetap Gaji pokok, Lembur, Sampingan, Lain-lain); catatan opsional ada di pemasukan dan pengeluaran.
- Dompet multi + 2 mata uang saja (Rp & Dollar): tiap dompet punya mata uang; dompet lama default Rp. Ringkasan Tunai · E-money · Bank per mata uang, tidak dicampur. Transfer menampilkan arah eksplisit ("Tunai → GoPay"); transfer Rp ↔ $ tanpa kurs otomatis — jumlah diterima diisi manual.
- Paylater & Utang Piutang (bagian khusus di Rencana): jenis Utang / Piutang / Paylater + nama penyedia untuk Paylater (mis. GoPay Later, SPayLater). Paylater otomatis berarti kamu meminjam.
- Profil & Pengaturan (baru, dari avatar Beranda): nama tampilan, mata uang utama (Rp/$), dompet utama, pemasukan bulanan, jalan pintas ke Rencana/Laporan, versi aplikasi + cek update (self-update yang sama). Tanpa login, tanpa foto — avatar inisial.
- Migrasi aman ke database v4 (migrasi nyata v3→v4): dompet lama dapat kolom mata uang (default IDR), utang lama dapat jenis + penyedia — semua data dipertahankan.
- Batas yang disengaja: anggaran, target tabungan, tagihan, dan utang tetap dalam Rp. Kalau mata uang utama diganti ke Dollar, ringkasan memakai $, tetapi anggaran yang sudah diset tetap dibandingkan dalam Rp — set ulang anggaran setelah pindah mata uang kalau perlu. Jumlah Dollar ditampilkan tanpa sen (angka bulat).

**v1.3.0 — Restruktur (satu tab = satu tugas):**
- Tab "Keuangan" dibongkar — tiap fitur kini hidup di tepat satu tempat, tanpa duplikasi.
- Beranda (ringkasan saja): total saldo dompet, masuk/keluar bulan ini, grafik 7 hari / 6 bulan, donut per kategori, cincin anggaran, kartu target tabungan, kartu "Perhatian" (tagihan jatuh tempo ≤7 hari / terlambat + anggaran hampir/habis, ketuk → lompat ke Rencana). Pemasukan per sumber pindah sepenuhnya ke Laporan.
- Riwayat: semua transaksi + cari + filter bulan/kategori, ditambah filter dompet baru; ubah/hapus per baris.
- Dompet (baru): daftar dompet + saldo, tambah/ubah/hapus dompet, transfer antar dompet.
- Rencana (baru): Anggaran, Target tabungan, Transaksi berulang, Utang & piutang, Tagihan & pengingat — tiap bagian punya judul sendiri.
- Laporan (baru): arus kas, grafik 6 bulan, per kategori (donut), per sumber pemasukan, per dompet, ekspor CSV.
- Tanpa perubahan data: skema database tetap v3, tanpa migrasi.

**v1.2.0 — Financial app:**
- Dompet ganda: Tunai + SeaBank sebagai default (bisa tambah/ubah/hapus); saldo per dompet; pilih dompet saat mencatat; transfer antar dompet sebagai tipe sendiri (bukan pemasukan/pengeluaran).
- Gaji & pemasukan lain: sumber pemasukan Gaji pokok, Lembur, Sampingan, Lain-lain; ringkasan per sumber per bulan di Beranda dan Laporan.
- Transaksi berulang: jadwal bulanan/mingguan, on/off, dievaluasi tiap aplikasi dibuka (tanpa WorkManager).
- Utang & piutang: bayar sebagian sampai lunas.
- Tagihan & pengingat: jatuh tempo bulanan; Beranda menampilkan yang jatuh tempo ≤7 hari / terlambat; "Bayar" mencatat pengeluaran.
- Laporan + ekspor CSV: arus kas bulanan per kategori, per dompet, per sumber pemasukan; ekspor CSV bulan berjalan lewat share intent.
- Migrasi aman ke database v3 (migrasi nyata v1→v3 dan v2→v3) — data lama dipertahankan; `fallbackToDestructiveMigration` dihapus.

**v1.1.0 — Unicorn-grade MVP:**
- Dashboard baru: kartu hero saldo bulan ini (angka animasi), grafik batang pengeluaran 7 hari / 6 bulan, donut pengeluaran per kategori, cincin anggaran Belanja/Darurat/Tabungan dengan peringatan 80% (kuning) dan 100%+ (merah).
- Insight otomatis: pengeluaran dibanding bulan lalu, kategori terbesar, perkiraan sisa hari anggaran, proyeksi tanggal tercapai target tabungan.
- Onboarding sekali saat pertama buka: pemasukan bulanan, tiga anggaran (default 200rb/100rb/700rb), target laptop (default Rp3.000.000). Bisa dilewati.
- Quick add: bottom sheet dengan keypad kalkulator, kategori terakhir dipakai sebagai default, baris kategori yang sering dipakai.
- Riwayat: cari catatan, filter per kategori dan bulan, ubah catatan yang sudah ada.
- Target tabungan lebih dari satu: Laptop tetap target utama; tambah target sendiri, tambah tabungan manual per target, ubah dan hapus target tambahan.
- Widget layar utama: sisa bulan ini, sisa anggaran, progres tabungan.
- Anggaran bulanan: Belanja Rp200.000, Dana darurat Rp100.000, Tabungan laptop Rp700.000 — semua bisa diubah, termasuk pemasukan bulanan.
- Lokal saja (Room/SQLite di HP) — tanpa server, tanpa login, tanpa biaya. Grafik custom Canvas, tanpa library chart berat.

**v1.0.0 — Money management (rilis pertama):** catat pemasukan/pengeluaran, anggaran, progres tabungan laptop, riwayat.

**Catatan migrasi v1.4.0:** database naik ke versi 4 dengan migrasi nyata (v3→v4) — kolom mata uang di dompet, jenis + penyedia di utang, kolom jumlah-diterima di transaksi transfer; semua data lama dipertahankan; `fallbackToDestructiveMigration` tetap dihapus.

**Catatan migrasi v1.2.0:** database naik ke versi 3 dengan migrasi nyata (v1→v3 dan v2→v3) — data lama dipertahankan; `fallbackToDestructiveMigration` dihapus, upgrade selanjutnya tidak boleh menghapus data.

**Catatan migrasi v1.1.0:** database naik ke versi 2 (tabel target tabungan baru) dengan fallbackToDestructiveMigration — data v1.0.0 terhapus saat upgrade karena aplikasi baru berumur satu hari.

**Rencana v2:** shift kerja, muat mobil, ekspor/backup data, pengingat harian.

## Download

[Releases](https://github.com/qwetls/doit/releases) — unduh `doit-vX.Y.Z-release.apk` (bukan debug).

## Build

Android, Kotlin + Jetpack Compose, minSdk 26, targetSdk 34. Build lewat GitHub Actions (push ke `main` = rilis otomatis). Tidak ada build lokal di VM (tanpa Android SDK).

## Catatan

- Tabungan dihitung dari total pemasukan dikurangi total pengeluaran selama ini; perkiraan bulan tercapai memakai rata-rata nabung bersih per bulan.
- v1 belum dites di perangkat nyata — build CI sukses berarti terkompilasi, bukan berarti sudah dicoba di HP.
