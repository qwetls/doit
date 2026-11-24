# do.it

Aplikasi pencatat uang yang ringan — money management untuk gaji Rp1.000.000/bulan.

**v1.0.0 — Money management:**
- Catat pemasukan (Gaji, Lain-lain) dan pengeluaran (Belanja harian, Makan, Transport, Darurat, Lain-lain) — jumlah, kategori, tanggal, catatan.
- Anggaran bulanan: Belanja Rp200.000, Dana darurat Rp100.000, Tabungan laptop Rp700.000 — semua bisa diubah.
- Beranda: total masuk/keluar/sisa bulan ini, pemakaian anggaran, progres tabungan laptop, catatan terakhir.
- Tabungan laptop: target default Rp3.000.000 (bisa diubah), terkumpul, sisa, perkiraan bulan tercapai.
- Riwayat: semua catatan per bulan, filter per kategori, hapus catatan.
- Lokal saja (Room/SQLite di HP) — tanpa server, tanpa login, tanpa biaya.

**Rencana v2:** shift kerja, muat mobil, ekspor/backup data, pengingat harian.

## Download

[Releases](https://github.com/qwetls/doit/releases) — unduh `doit-vX.Y.Z-release.apk` (bukan debug).

## Build

Android, Kotlin + Jetpack Compose, minSdk 26, targetSdk 34. Build lewat GitHub Actions (push ke `main` = rilis otomatis). Tidak ada build lokal di VM (tanpa Android SDK).

## Catatan

- Tabungan dihitung dari total pemasukan dikurangi total pengeluaran selama ini; perkiraan bulan tercapai memakai rata-rata nabung bersih per bulan.
- v1 belum dites di perangkat nyata — build CI sukses berarti terkompilasi, bukan berarti sudah dicoba di HP.
