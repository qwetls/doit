# do.it

Aplikasi pencatat uang yang ringan — money management untuk gaji Rp1.000.000/bulan.

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
