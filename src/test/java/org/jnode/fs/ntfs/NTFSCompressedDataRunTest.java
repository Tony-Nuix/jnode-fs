package org.jnode.fs.ntfs;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.jnode.fs.ntfs.datarun.CompressedDataRun;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.jnode.fs.FileSystemTestUtils.*;

/**
 * Tests for {@link org.jnode.fs.ntfs.datarun.CompressedDataRun}.
 */
public class NTFSCompressedDataRunTest {

    @Test
    public void testDecompression_ofSingleChunk_isCorrect() throws IOException {
        // Arrange
        String chunk =
            "23 B7 00 25 21 50 53 2D 41 64 6F 00 62 65 2D 33\n" +
            "2E 30 20 45 08 50 53 46 01 40 0D 0A 25 25 00 43\n" +
            "72 65 61 74 6F 72 3A 02 20 02 78 20 49 6C 6C 75\n" +
            "73 04 74 72 01 48 28 54 4D 29 20 00 66 6F 72 20\n" +
            "57 69 6E 64 00 6F 77 73 2C 20 76 65 72 00 73 69\n" +
            "6F 6E 20 34 2E 30 05 01 74 46 01 6C 28 52 6F 62\n" +
            "20 20 53 61 62 6F 20 1C 00 29 20 00 28 49 6E 73\n" +
            "6F 20 43 6F B8 72 70 6F 00 60 01 48 14 32 29 01\n" +
            "5D 00 54 69 74 6C 65 3A 20 28 00 4C 41 59 45 52\n" +
            "53 2E 41 8E 49 02 15 02 AE 00 3A 44 61 74 01 1C\n" +
            "40 31 2F 36 2F 39 37 00 5A 31 40 3A 30 36 20 50\n" +
            "4D 02 23 42 00 6F 75 6E 64 69 6E 67 42 00 6F 78\n" +
            "3A 20 31 34 20 31 00 36 20 36 34 33 20 37 37 02\n" +
            "35 81 2B 44 6F 63 75 6D 65 00 6E 74 50 72 6F 63\n" +
            "65 73 00 73 43 6F 6C 6F 72 73 3A 00 20 43 79 61\n" +
            "6E 20 4D 61 02 67 80 0C 61 20 59 65 6C 6C 00 6F\n" +
            "77 20 42 6C 61 63 6B 01 89 19 53 75 70 70 6C 69\n" +
            "65 80 64 52 65 73 6F 75 72 00 1F 88 3A 20 70 00\n" +
            "23 73 65 74 03 9F 04 5F 70 00 18 65 64 61 72 72\n" +
            "B0 61 79 20 32 80 B4 82 93 2B 0C 12 20 63 6D 79\n" +
            "6B 63 01 3B 20 31 C8 2E 31 20 13 11 73 68 80 3F\n" +
            "17 0F 69 80 D1 6F 6D 85 21 30 93 21 08 E5 41 70\n" +
            "5F 41 49 33 40 1B 43 0A C0 02 5F F9 82 3D 55 73\n" +
            "C0 3B 80 3E C1 22 80 39 C1 05 60 54 65 6D 70 6C\n" +
            "C0 56 C2 4E 33 80 30 35 2E 35 20 33 39 42 01 23\n" +
            "C7 02 85 0A 69 6C 65 83 58 37 20 A0 31 37 20 35\n" +
            "39 81 58 36 04 07 01 47 59 65 76 69 65 77 3A 20\n" +
            "40 48 65 61 64 65 72 40 07 25 15 85 18 3A 01 03\n" +
            "50 40 20 4F 72 69 90 67 69 6E 3A C0 10 31 37 C1\n" +
            "04 01 C1 21 50 61 70 65 72 52 65 00 63 74 3A 2D\n" +
            "31 37 2E 39 02 39 02 17 33 2E 39 36 38 35 81 41\n" +
            "1A 2E 39 37 35 38 20 85 06 31 05 0D 4D 61 72 03\n" +
            "12 03 0C 2D 31 C0 35 2E 31 31 39 34 C6 09 80 29\n" +
            "03 42 06 C1 0B 45 6E 64 43 6F 6D 65 01 82 73 81\n" +
            "03 42 65 40 0E 80 84 6C DC 6F 67 86 03 45 79 1D\n" +
            "79 31 41 0C 85 AB D2 50 42 80 20 41 82 80 4F 00\n" +
            "31 81 D2 EA 73 C2 A5 56 83 CF 3A 42 86 C1 0C 0C\n" +
            "B3 C0 38 2F 32 2F 39 30 00 B3 42 0B 90 43 6F 70\n" +
            "79 00 46 68 74 C0 C0 00 28 43 29 20 31 39 38 37\n" +
            "20 2D 31 39 39 32 44 EA 53 79 00 73 74 65 6D 73\n" +
            "20 49 6E 04 63 6F 43 D4 65 64 20 41 6C 12 6C C2\n" +
            "0C 73 20 00 2F 65 72 76 04 65 64 C0 12 0D 0A 75\n" +
            "73 65 80 72 64 69 63 74 20 2F A2 7C 09 CA 54 35\n" +
            "20 22 03 64 75 70 20 02 62 81 20 20 70 75 74 0D\n" +
            "0A 00 0D 0A 2F 69 6E 69 74 69 00 61 6C 69 7A 65\n" +
            "09 09 09 10 25 20 2D 20 07 02 20 2D 0D E0 0A 7B\n" +
            "0D 0A 09 AF 09 42 08 31 03 15 60 02 09 41 06 09\n" +
            "01 0D 78 63 68 AE 65 21 6B 00 0B 60 02 25 C0 00\n" +
            "62 60 8C C1 41 04 09 7D 20 69 66 01 01 46 16 41\n" +
            "E0 56 20 72 6F 6C 6C E3 12 09 23 00 04 60 92 61\n" +
            "6C 6C 80 04 65 6E 80 64 0D 0A 7D 20 64 65 20 06\n" +
            "81 00 16 74 65 72 6D 69 6E 80 57 FF E0 09 E1 15\n" +
            "E6 01 C4 15 27 05 C8 74 41 1B C0 46 91 42 82 73\n" +
            "20 63 40 87 74 20 A9 78 27 62 79 A3 1D C3 7A 61\n" +
            "73 C0 A5 65 20 C1 C0 A6 64 6F 6E 6C 79 09 10 E0\n" +
            "80 07 41 07 40 8F 22 10 62 6F 6F 6C 65 47 A0 8A\n" +
            "C7 02 E5 26 70 6F 70 E9 06 63 F8 75 72 72 E0 8E\n" +
            "66 07 81 17 88 06 E4 08 C1 83 10 66 61 6C 73 65\n" +
            "88 07 64 07 EB 62 38 65 12 20 E0 0B 20 82 23 A1\n" +
            "4C 20 5D 99 25 59 0D 0A 2F 36 E8 3C 20 67 00 99\n" +
            "F0 65 78 65 63 C1 06 22 62 05 07 4D 60 83 50 98\n" +
            "05 60 43 4D 59 4B 20 C2 8B 1E 20 D4 5F C1 9A 21\n" +
            "0C 6E B9 32 33 2F 14 38 39 FD 5F 30 FC 5F 20 52\n" +
            "69 18 67 68 74 0C 60 2B 2C 20 74 72 B8 75 65 20\n" +
            "27 2C 0F 64 E7 1B 34 D2 63 19 9F 63 0A 2F E0 0B\n" +
            "C7 08 77 68 65 CE 72 C0 36 42 60 A2 3F 09 7D 00\n" +
            "58 01 62 03 6D 73 66 06 5F 76 61 72 73 20 22 32\n" +
            "F2 0F 09 2F 5F C0 0B 72 67 36 62 62 C0 20 09 09\n" +
            "C1 0D E5 01 20 6C F8 6F 61 64 C3 62 00 05 64 1F\n" +
            "CA 02 3C 01 FF 09 03 13 22 97 0B 1B 3D 76 01 31\n" +
            "07 E1 0B FF 3C E2 0A FF 3C 0A 09 09 21 29 83 0F\n" +
            "1E 3C 5E 09 A2 2A C4 44 8F 07 11 41 65 49 2F 2F\n" +
            "3F 8F 3E 84 3F F4 35 D4 0D 32 31 ED 0C 65 71 7F\n" +
            "90 0B 81 0B 43 07 E3 46 94 41 2A 1B 81 41 63 05\n" +
            "41 81 6D 20 6A 6E 74 61 20 79 3D 43 81 62 41 81\n" +
            "C1 23 97 0A D3 50 31 20 30 73 75 62 20 E0 86 E2\n" +
            "4A 0D 0A 0A 33 84 06 33 80 53 64 65 78 20 00 61\n" +
            "64 64 20 6E 65 67 20 D1 01 50 30 20 6C 40 56 09\n" +
            "13 51 A1 11 5F 50 80 A1 4D B2 4F 15 4F F0 03 7D\n" +
            "70 46 70 FF 00 9A 85 3B 36 07 C3 21 5A 20 D8 3B\n" +
            "82 16 42 0D BE 20 70 51 E4 10 B6 03 C2 5C 4D 01\n" +
            "20 BF 0E FC 77 20 B2 0E 72 47 6C 14 E5 07 54 04\n" +
            "86 27 FF 07 44 D5 0F 53 11 71 0F 6F 0D 20 10 70\n" +
            "0F C4 17 3F A4 04 8F 4C 82 1A D0 2E E4 51 B1 69\n" +
            "25 25 69 4A 4D 25 25 CF 4A 63 10 03 93 42 5F 36\n" +
            "63 C2 94 E1 98 38 61 47 85 4A 57 72 F1 F0 6D 6E\n" +
            "67 20 83 74 CF 4A A1 AE BF 7A FF 86 A7 CF 4A 71\n" +
            "47 C6 7A 7F 7A 7C 7A 7F 4A 40 77 7F 44 10 A4 0E\n" +
            "7F 4A CE 42 73 A2 4F 42 33 7C 25 09 37 7B 61 74\n" +
            "01 B7 61 6E 64 20 03 D5 33 F1 00 6F 66 20 74 68\n" +
            "65 E5 F2 A9 53 B0 5F 28 32 A3 AA 42 83 3F 4E FF\n" +
            "0F 80 64 21 52 08 8C 42 31 01 14 30 3C 42 D0 37\n" +
            "F7 29 42 23 38 19 42 7D 61 C7 28 7E A5 41 69 06\n" +
            "FF 54 23 6C 7F A2 2D E6 41 EF 40 1A 21 AF 40 70\n" +
            "84 3D 76 05 25 10 74 50 81 95 CA 71 BE 65 64 30\n" +
            "75 72 65 73 32 D1 B0 97 70 70 03 80 7E E0 13 72\n" +
            "6F 75 67 68 20 00 65 61 63 68 20 63 68 61 18 72\n" +
            "61 63 F0 07 70 17 20 61 20 D3 80 D4 21 02 28 33\n" +
            "59 15 63 E3 8B 10 82 77 C0 0A 14 02 C1 05 20 84\n" +
            "01 44 0B 80 5B 6C 00 6F 62 79 74 65 20 30 20 A6\n" +
            "64 12 8E 10 01 68 69 1D 01 63 A2 03 06 65 00 95\n" +
            "57 02 63 73 63 72 69 88 70 74 20 C4 24 66 6F 6E\n" +
            "80 9F 0A 46 50 00 53 73 01 6B 6E 6F 77 B8 6E 20\n" +
            "7B FF 01 F4 01 41 3C 7D F0 01 3C 30 20 A1 19 A1\n" +
            "57 B4 05 E5 1C 2F 5F B3 A4 09 79 07 09 5F D6 09\n" +
            "82 15 09 70 37 19 F3 06 31 20 04 01 54 03 31 32\n" +
            "39 05 B0 4A 20 45 04 31 35 39 20 6C 2C 65 20 A0\n" +
            "29 F0 05 09 55 01 32 32 22 34 49 02 32 35 32 44\n" +
            "02 20 6F 96 72 B6 02 95 0E 32 1D 06 36 31 CB 03\n" +
            "EE 34 C5 03 55 06 95 03 33 9F 03 9F 03 F1 06 EF\n" +
            "35 07 D0 07 07 08 71 64 09 77 19 B5 0B F4 1A 07\n" +
            "22 7E 23 5E C6 11 32 35 36 20 6D FC 75 6C C6 0F\n" +
            "40 61 51 0F C0 12 81 1C A2 00 7E 2F 87 14 86 04\n" +
            "40 B1 23 70 C8 73 17 30 63 37 63 24 23 25 31 28\n" +
            "20 14 01 14 28 0D DA B7 00 0A 7B 0D 0A 09 64 75\n" +
            "70 80 20 32 35 36 20 6C 74 00 C0 41 01 80 09 28\n" +
            "73 29 20 01 A8 30 00 20 34 20 33 20 72 6F 6C D0\n" +
            "6C 20 70 75 01 74 7D 05 84 05 CC 00 69 64 69 76\n" +
            "20 65 78 63 22 68 02 82 6D 6F 64 00 40 09 28 8C\n" +
            "68 6C 03 7E 03 86 36 20 35 06 86 04 20 31 0E A4\n" +
            "20 69 66 65 6C 00 73 65 0D 0A 7D 20 64 65 00 66\n" +
            "0D 0A 0D 0A 2F 63 6C 40 65 6E 67 74 68 09 00 00\n" +
            "25 00 20 73 74 72 69 6E 67 20 D5 04 13 20 03 1A\n" +
            "0D 02 B9 30 02 7A 01 93 29 04 C0 20 7B 00 60 7D\n" +
            "00 05 32 20 42 7D 04 57 20 61 64 64 00 0C 63 40\n" +
            "66 6F 72 61 6C 6C 08 65 25 00 20 67 72 61 70 68\n" +
            "69 63 00 20 70 72 6F 63 65 64 75 10 72 65 73 20\n" +
            "80 12 20 64 72 14 61 77 81 38 61 05 3D 68 6F 72\n" +
            "20 69 7A 6F 6E 74 80 1F 79 20 00 6F 72 20 76 65\n" +
            "72 74 69 02 63 81 06 2C 20 77 69 74 68 22 2D 00\n" +
            "51 65 61 6D 01 0D 63 72 48 6F 73 73 85 07 28 32\n" +
            "02 2D 73 0A 29 02 6A 68 80 2A 64 74 68 73 98 68\n" +
            "6F 77 89 6B 08 0B 20 2D 03 6B 7B 82 C1 81 9D 63\n" +
            "83 7D 81 A9 01 1B 81 03 5F 20 68 76 61 78 20 81\n" +
            "02 79 20 80 72 6D 6F 76 65 74 6F 04 0B 60 77 62\n" +
            "20 65 71 00 7D 80 0D 63 9D 82 10 63 86 10 02 86\n" +
            "00 13 7D 20 0F 83 1C 2F 76 D3 25 88 05 CE 25 32\n" +
            "35 35 03 C0 61 00 14 09 5F 63 68 61 72 85 80 47\n" +
            "65 40 47 74 69 6F 6E 80 5F 64 65 71 01 06 61 6E\n" +
            "82 82 82 32 09 88 2D 39 30 40 80 74 61 74 40 79\n" +
            "21 C0 3E 30 20 5F 66 80 53 41 64 50 6A 75 73 74\n" +
            "49 31 09 08 3C 09 1F C9 31 C0 02 80 37 40 01 C6\n" +
            "3E 36 20 2D 2E 31 43 95 87 2F C1 1D 09 0B 17 6E\n" +
            "65 2E 67 0A 18 0A 22 41 B3 09 43 28 63 75 84 72\n" +
            "72 C0 30 70 6F 69 6E C1 B9 04 09 09 42 28 48 65\n" +
            "69 67 68 E0 74 20 73 75 62 82 16 C2 B9 01 21 1E\n" +
            "79 81 04 83 BD C3 24 45 08 32 20 69 F8 6E 64 65\n" +
            "42 5F 85 63 05 0D 40 63 40 09 3F 06 0D 80 68 81\n" +
            "03 41 A9 41 87 40 45 33 20 5E 32 02 34 82 1A 09\n" +
            "42 41 82 73 02 C1 77 A2 69 00 90 20 70 6F C0 EB\n" +
            "20 01 DE D3 41 37 83 52 73 63 40 2F 20 4D 3B 05\n" +
            "8D 6E 09 07 8A 85 C5 40 19 7D 50 83 9F 62 20 03\n" +
            "43 11 00 1D 20 75 6E 6B 6E 6F 54 77 6E E0 5E 69\n" +
            "A0 0F 61 E2 3F 28 BE 34 6A 5E 8D 4B C0 1E 00 21\n" +
            "81 46 20 40 39 F6 61 00 29 02 1B 20 A0 5A 82 1B\n" +
            "21 16 C5 4D 6E 36 80 49 61 8F 40 15 2F A3 2E 62\n" +
            "29 64 F5 A2 84 09 42 02 78 E3 29 48 02 C1 2E C1\n" +
            "91 3F 48 02 00 0E 0D 07 C0 10 48 02 00 0C 5F 6C\n" +
            "F8 69 6E 65 E9 58 40 8B 81 69 A9 73 E1 8A 67 A9\n" +
            "62 47 29 27 97 68 76 C6 18 40 3A 25 1E 20 E8 1D\n" +
            "E4 26 28 04 05 1D 30 20 30 7F C0 AC E5 59 49 21\n" +
            "89 A2 E0 02 A1 23 C0 0A 09 CE 25 8E 27 E9 26 C1\n" +
            "09 30 20 00 64 44 B7 FF 17 0A E5 09 00 0A A6 13\n" +
            "2F 09 E1 09 C7 6D B4 09 11 04 68 64 69 63 20 7A\n" +
            "65 61 64 50 6F 6E 6C 79 22 55 65 01 81 73 C0 65\n" +
            "74 70 61 63 6B A0 AA 81 5D 00 25 25 45 6E 64 52\n" +
            "65 73 10 6F 75 72 63 20 82 25 25 42 50 65 67 69\n" +
            "6E 05 02 3A 03 48 65 00 74 20 41 64 6F 62 65 5F\n" +
            "82 63 80 84 6F 6D 63 6F 6C 20 AF 04 31 2E 80 11\n" +
            "0D 0A 25 25 54 00 69 74 6C 65 3A 20 28 43 09 82\n" +
            "03 20 43 A2 03 4F 70 65 72 10 61 74 6F 72 01 AE\n" +
            "25 25 56 C8 65 72 73 20 92 3A 20 81 06 61 06 68\n" +
            "43 72 65 62 54 44 E0 90 40 07 35 00 2F 39 2F 38\n" +
            "38 29 20 28 01 C0 B3 25 25 43 6F 70 79 72 03 A1\n" +
            "7D 20 03 28 43 29 20 31 39 60 38 37 2D 31 39 E0\n" +
            "85 A2 10 20 00 53 79 73 74 65 6D 73 20 40 49 6E\n" +
            "63 6F 72 70 60 C9 74 50 65 64 20 41 00 E3 52 81\n" +
            "06 73 82 20 A0 17 65 72 76 65 64 62 BD 03 A5 89\n" +
            "43 1E 20 74 72 75 65 20 A1 49 20 75 73 65 72 22\n" +
            "25 2F 4F 1C 6E 35 20 7B 60 28 81 7E 62 C1 22 23\n" +
            "EF 2F 00 69 6E 69 74 69 61 6C 69 74 7A 65 E2 4A\n" +
            "2D E0 6F 05 02 C4 48 2F C3 20 0C 69 25 77 68 65\n" +
            "72 E1 AD 01 9C FF 40 86 80 5C A5 F7 8F 0E 92 06\n" +
            "10 02 9E 01 30 01 C7 13 52 F1 08 70 35 65 63 6B\n" +
            "21 01 31 01 FA 25 80 08 62 E0 4E F2 00 B1 30 81\n" +
            "00 B1 49 79 C3 06 09 7D C1 74 40 72 60 01 32 20\n" +
            "09 07 6F 07 65 07 9E 34 2F 74 65 72 6D FC 69 6E\n" +
            "80 1A D4 0E F6 00 C4 0E E9 26 7F 05 9F 22 68 71\n" +
            "0F 83 07 C3 09 44 3A 2F 66 30 0B 30 63 6D 79 6B\n" +
            "B8 12 F0 2F 63 79 40 61 6E 20 6D 61 67 81 4C 20\n" +
            "00 79 65 6C 6C 6F 77 20 62 02 6C 00 1C 20 6E 61\n" +
            "6D 65 20 01 4F 03 72 20 6F 62 6A 65 63 13 D0 62\n" +
            "80 06 35 20 E1 2D 65 64 61 70 72 72 61 79 41 3B\n" +
            "A3 8C 2C 19 09 F3 70 06 43 03 20 74 70 66 21 21\n" +
            "09 1B D3 7F 01 71 49 0D 0A 61 6C 6F 61 64 DB F2\n" +
            "33 D2 14 34 40 14 F1 0C 34 60 1F F1 65 38 6D 75\n" +
            "6C 70 94 03 70 A0 01 7D 20 88 72 65 70 C0 2D 0D\n" +
            "0A 35 66 71 7F 72 03 21 21 A1 0E E1 31 B6 44 51\n" +
            "09 80 83 72 0A 70 70 95 74 31 09 62 6F 6F 6C FE\n" +
            "65 E0 0F D0 02 76 01 74 16 52 04 94 13 39 17 3F\n" +
            "A0 34 2F 3E 2F 3E 2F 3E 32 3F 2D 3E 49 6C 06 6C\n" +
            "70 3C B2 3B 41 5F 41 49 33 61 20 3B 32 20 31 31\n" +
            "89 3E 42 1E 20 41 88 02 20 28 52 29 20 34 3E 20\n" +
            "02 33 70 41 41 62 62 72 65 76 22 69 30 23 64 20\n" +
            "50 70 63 6F 67 13 82 3D 68 40 32 20 6F 40 28 37\n" +
            "2F 20 32 32 2F 38 39 7F 40 20 28 FD 79 40 32 D3\n" +
            "48 7F 40 7F 40 7F 40 7F 40 A3 50 F3 7D 40 7E 10\n" +
            "36 37 DF 40 10 27 60 0C F7 3F AF 32 4D 40 B3 1A\n" +
            "42 90 26 2D 27 02 65 D4 1C 00 25 20 36 36 20 76\n" +
            "61 72 80 73 2C 20 62 75 74 20 40 1F 86 76 80 49\n" +
            "52 2E 6F 66 20 31 30 6F 0E 6E E0 78 64 B0 A3 29\n" +
            "20 50 6F 73 00 74 73 63 72 69 70 74 20 20 66 72\n" +
            "61 67 6D 00 32 73 0D 16 0A 9F 0B 1B 1C 5F 51 06\n" +
            "20 37 36 53 EF 0B E4 0B 70 61 11 30 6F 11 5B 6E\n" +
            "02 64 00 05 2F 5F 6C 70 20 2F 10 6E 6F 6E 65 A4\n" +
            "33 5F 70 66 C4 20 7B 35 3B 5F 70 73 CA 00 AB 01\n" +
            "49 BA 01 6A 73 CA 01 6A 73 DA 01 6F 0C 6C 61 C0\n" +
            "76 12 84 2F 5F 64 6F F0 43 6C 69 70 F5 00 41 15\n" +
            "58 09 71 64 09 A1 C8 66 09 74 2F 66 6C 61 74 23\n" +
            "B1 09 A2 15 63 66 20 F1 00 6E 65 06 73 B0 0B 91\n" +
            "03 74 79 70 6F 67 BF F1 C4 10 C2 49 0D 6E 85 B4\n" +
            "07 7D B3 30 65 0F 6C 74 6D 60 48 50 18 78 05 01\n" +
            "70 A9 64 00 65 72 53 74 61 72 74 20 80 5B 2F 65\n" +
            "30 20 2F 72 30 00 AA 61 30 00 6F 30 00 69 3A 00\n" +
            "5D 41 03 19 50 2E 2F 5F 53 03 70 76 20 5B 6E C6\n" +
            "75 60 6A 4C 00 2F 65 31 70 04 30 00 FA 61 30 00\n" +
            "63 50 10 92 03 F0 0E 84 03 61 44 E1 94 0A 73 68\n" +
            "69 66 90 07 80 77 36 02 FF A0 9B F6 0A 30 9C B6\n" +
            "00 C0 97 B6 00 90 8F B7 00 56 79 47 17 40 27 64\n" +
            "D1 D5 5B 09 05 63 11 5E 0F 6D 74 78 7C 10 73 70\n" +
            "20 20 31 36 23 30 32 56 05 68 79 80 70 68 65 6E\n" +
            "20 28 2D C0 E6 F5 81 E3 2F A2 B0 53 20 72 D7 06\n" +
            "72 C6 C3 B1 D3 76 03 31 01 44 65 4F 01 74 E4 BE\n" +
            "8A 02 A3 44 CA 36 01 63 6E 74 D7 05 73 40 E0 C0\n" +
            "65 20 5B 31 20 31 A6 11 30 6B F0 69 76 65 45 50\n" +
            "82 52 0E 76 03 00 33 A6 4E 90 3A AF 01 2F 5F 70\n" +
            "85 70 02 03 9A 65 97 05 70 80 38 D7 00 74 44 61\n" +
            "95 26 32 C4 34 B3 0E 68 66 82 69 31 30 0C 30 20\n" +
            "B4 9B F4 1A 68 66 66 6F C0 75 6E 64 20 66 61 50\n" +
            "F5 43 24 81 01 2A 62 69 74 6D 61 70 30 79 03 00\n" +
            "0C 66 37 10 B6 00 0A 2F 5F 62 69 74 66 6F 00 6E\n" +
            "74 20 6E 75 6C 6C 20 10 64 65 66 0D 03 98 6C 6F\n" +
            "62 20 79 74 65 20 30 08 48 68 69 11 0E 48 6B 65\n" +
            "79 0D 70 73 74 72 48 69 6E 67 0D 2A 6D 65 00 2C\n" +
            "63 01 05 9C 0D 0A 25 20 74 79 70 00 6F 67 72 61\n" +
            "70 68 79 20 00 6F 70 65 72 61 74 6F 72 82 73 00\n" +
            "7B 54 78 20 7B 7D 04 74 0C 54 6A 06 0B 01 31 63\n" +
            "6F 6D 70 00 6F 75 6E 64 20 70 61 74 02 68 0A 34\n" +
            "43 52 65 6E 64 65 6A 72 0A 2D 70 00 86 74 00 89\n" +
            "01 CF 41 00 49 33 5F 73 61 76 65 70 88 61 67 65\n" +
            "0C 52 6C 6F 72 03 25 44 6E 64 01 3F 5F 67 66 0A\n" +
            "5D 63 00 66 20 34 20 61 72 72 61 0A 79 05 84 69\n" +
            "0B 10 6F 66 20 66 90 61 6C 73 65 05 0F 66 63 06\n" +
            "30 D0 2F 5F 67 73 0B 26 73 0E 26 0B 10 D4 6F 73\n" +
            "0B 26 73 09 26 69 88 25 01 87 08 77 72 69 81 6F\n" +
            "20 73 79 73 38 74 65 6D 0A 62 0D DE 8D DC 63 70\n" +
            "08 72 6F 63 0C 4D 63 72 69 70 42 74 C7 7B 68 76\n" +
            "61 78 4A 03 79 A9 49 03 77 62 49 03 63 4A 0A 63\n" +
            "46 0A 00 0D 0A 41 64 6F 62 65 5F 30 49 6C 6C 75\n" +
            "80 85 81 78 41 5F 81 C0 62 20 62 65 67 69 6E 55\n" +
            "07 10 0D 0A 09 7B C0 00 64 75 70 80 20 78 63 68\n" +
            "65 63 6B 00 03 01 41 04 25 09 09 62 69 6E 64 11\n" +
            "41 03 7D 20 69 C0 A9 09 70 6F 0C 70 20 C0 00 40\n" +
            "04 7D 20 66 6F C0 72 61 6C 6C 0D 0A 00 82 04 01\n" +
            "41 F1 1E 5F 76 61 72 73 85 27 6E 64 65 77 41 9A\n" +
            "0D 0A C4 A3 40 92 74 00 65 72 6D 69 6E 61 74 65\n" +
            "42 09 00 00 25 20 2D 20 06 04 20 B8 2D 0D 0A 80\n" +
            "2D 87 1F 06 0D 25 81 86 E4 69 6E 00 6B 6F 6E 43\n" +
            "69 83 BA 01 A6 95 81 11 09 C1 11 5F 42 63 0D 0A\n" +
            "47 E1 4D 80 07 64 80 E3 41 08 25 20 C1 D9 76 F8\n" +
            "61 6C 75 80 99 C0 04 C4 1A 13 38 83 30 40 33 20\n" +
            "31 20 72 6F 40 F4 70 34 75 74 09 30 78 C0 03 8D\n" +
            "14 6C 69 71 A0 18 61 6C 20 61 03 44 0B 81 2F 6C\n" +
            "10 6F 61 64 20 02 01 65 6E 67 05 00 6E 65 A0 31\n" +
            "20 6D 61 78 6C 7D C4 01 71 E0 2E 05 35 E9 05 C0\n" +
            "02 44 04 32 08 20 6D 75 60 8A 69 63 74 20 E8 63\n" +
            "6F 70 24 68 09 83 37 A2 0B E2 40 6D C1 23 20 E2\n" +
            "26 E7 2C 6E C0 3A C3 14 69 F0 6E 74 65 67 40 7B\n" +
            "21 02 84 13 C2 43 41 E5 3E 72 65 70 65 61 49 1C\n" +
            "25 39 60 14 72 6B 41 90 C8 8A 80 29 68 73 1C 77\n" +
            "6A 43 0A 24 94 01 02 20 78 20 1A 79 E2 36 09 21\n" +
            "18 03 03 77 69 64 31 A0 1D 33 20 32 02 28 84 1C\n" +
            "09 5F 04 68 76 80 5E 65 71 20 7B 20 47 02 21 E0\n" +
            "01 C0 5E 61 64 64 C7 01 79 F6 20 C1 01 64 53 09\n" +
            "A5 04 C0 68 A9 04 00 89 63 C0 01 A0 0A 7D 20 63\n" +
            "65 56 E7 1E 76 17 8D 14 01 02 87 14 30 A0 6E 33\n" +
            "20 2D 0F 43 3B 60 08 C1 2F 22 17 32 35 35 20 04\n" +
            "6C 65 20 02 09 5F 63 68 61 00 72 6F 72 69 65 6E\n" +
            "74 61 44 74 69 20 4E 31 20 65 21 35 09 6E 61 43\n" +
            "67 42 06 62 06 63 E4 20 A9 1E 35 07 A6 1E 61 09\n" +
            "92 1E 79 20 73 75 62 7F 69 20 C1 01 85 1E A7 1E\n" +
            "CB 04 80 20 C0 04 0D FA 0A A2 0A 34 25 18 26 09\n" +
            "C2 02 06 1B CA 02 DE 7D 61 03 83 16 51 30 BF 11\n" +
            "20 C0 06 80 2E 17 A1 14 48 32 A7 11 5F A1 DF 48\n" +
            "65 69 B8 67 68 74 01 0F C1 0D A1 19 65 60 BA 3D\n" +
            "60 01 7D B1 34 86 34 20 40 A0 9D 66 69 F4 6C 6C\n" +
            "A1 2E 20 41 3D 00 12 20 DD 00 E0 DB 80 04 07 37\n" +
            "36 C4 71 20 0A 2F E3 11 62 11 FB 01 7C 45 02 78\n" +
            "63 2A 48 02 E1 4D 61 6E 48 02 1F A0 0D 0D 07 60\n" +
            "10 48 02 A0 09 5F 6C 69 74 6E 65 29 40 30 20 40\n" +
            "C0 56 E2 5D 7D CF 00 01 62 4A 67 1C D7 27 73 77\n" +
            "01 32 F0 48 83 8C 0C 0B 28 30 20 36 20 33 C3 45\n" +
            "C3 30 0E C9 45 76 6A 73 73 23 36 8F 11 93 21 18\n" +
            "83 11 6D 61 70 00 78 20 C1 02 95 05 3D 34 E7 11\n" +
            "7B 0F 04 61 72 20 3D 57 32 03 73 09 B2 16 7B 21\n" +
            "6C 7D 01 04 09 7F D9 2C 23 26 E2 48 63 4A E5 2F\n" +
            "7F 31 7F 31 09 F0 09 2D 39 30 90 0D 90 33 B0 34\n" +
            "A0 14 40 09 09 63 75 72 72 A0 34 70 8E 6F 70 48\n" +
            "02 01 E2 22 41 64 6A 00 6D 03 E1 3E 32 01 6D 6F\n" +
            "76 65 74 6F ED D4 2D 67 81 86 62 01 66 72 81 D1\n" +
            "0A 02 8B 09 AE 04 35 20 E0 6C 65 78 20 73 04 65\n" +
            "74 34 11 73 74 72 6F 6B C1 62 3C 09 67 72 65 73\n" +
            "70 62 75 08 1F 69 07 B2 2B 60 12 65 07 A0 00 5F\n" +
            "73 70 DF E4 0C C2 51 10 01 C5 05 75 00 72 D8 02\n" +
            "B1 1F 3D 12 0A 32 73 59 B6 01 42 01 28 10 09 09\n" +
            "B7 E8 37 AF 10 9F 33 09 E5 06 35 0A 33 34 0E E5\n" +
            "0F 0A 39 D5 0F 75 62 62 07 44 09 B2 00 9B AE 06\n" +
            "12 2E 34 15 03 B0 20 6E 67 63 5A E3 60 60 20 4B\n" +
            "64 69 76 B5 3B 14 48 A1 09 F8 41 73 63 A0 1B 86\n" +
            "01 06 10 22 06 67 1A 6E 32 64 05 EB 1A 42 02 36\n" +
            "15 07 1F 1A 09 FD 18 1A 09 C2 02 F3 05 01 89 44\n" +
            "89 92 6E 7F 44 D8 09 36 20 C1 6C 89 33 68 8F 33\n" +
            "1F 45 EB 45 30 C1 02 20 D3 86 09 8F 33 9C 37 84\n" +
            "25 B7 8F 33 00 04 8F 33 09 56 11 CB 10 20 0D 19\n" +
            "EF 66 1E 8F 11 9C 2B 56 17 09 27 20 E3 5C 35 04\n" +
            "7F 75 00 18 2A 05 2A FD 29 10 15 46 73 6F 14 2F\n" +
            "FF 5F 14 5F 14 55 14 B0 02 45 14 0F 53 B0 34 01\n" +
            "53 E7 31 02 02 53 83 00 69 66 C3 1C 08 53 E2 4E\n" +
            "1B 0D 53 C5 3C 73 05 06 61 7B 30 20 37 77 55 53\n" +
            "D0 04 C8 1F 25 EE CC C0 8A C0 51 70 C7 73 89 1F\n" +
            "1E 1F 1E 72 69 78 B4 67 AF 51 FF 50 B4 84 7E AF\n" +
            "51 24 43 AF 51 84 01 2A 83 AF 51 2F AF 51 70 E4\n" +
            "AF 51 A7 51 66 0A 51 0D 0A 9E 20 08 00 1B 26 02\n" +
            "35 3F 4F 09 09 E7 24 FF F8 24 3F 4F 83 55 18 25\n" +
            "B2 38 52 05 3F 4F D0 52 F7 3F 4F 33 2A 7D 2F 09\n" +
            "32 09 DB 82 3F 4F 54 43 FF 3F 4F 3F 4F 72 CC 72\n" +
            "09 AE 06 3F 4F 3F 4F B4 53 3F A2 02 E3 12 3F 4F\n" +
            "91 69 13 4A 9F 4E CD B6 80 70 61 74 68 0D 0A 09\n" +
            "00 20 00 09 09 6D 6F 76 65 74 6F 30 20 70 6F 70\n" +
            "01 18 01 90 7D 20 40 69 66 65 6C 73 65 00 2C 7D\n" +
            "00 20 63 66 6F 72 61 6C 6C 11 00 2C 36 20 6E 00\n" +
            "90 0D 0A 7D 00 20 64 65 66 0D 0A 0D 0A 00 2F 68\n" +
            "6A 73 70 0D 0A 7B 08 0D 0A 20 00 00 34 20 31 20\n" +
            "3C 72 6F 01 52 01 1A 04 28 01 14 64 75 00 70 20\n" +
            "63 73 74 72 69 6E 26 67 03 52 01 28 66 61 00 60\n" +
            "20 63 38 68 61 72 03 8B 01 13 01 03 5F 73 70 70\n" +
            "20 65 71 07 27 08 47 01 1E 35 80 20 69 6E 64 65\n" +
            "78 20 05 07 1E 72 03 BD 07 2F 01 BB 07 0D 32 20\n" +
            "63 F0 6F 70 79 20 0A 25 08 D2 81 28 8F 6A 05 00\n" +
            "6A 09 01 00 25 20 63 78 20 80 63 79 20 66 69 6C\n" +
            "6C 01 54 80 20 61 78 20 61 79 20 83 64 12 20 80\n" +
            "12 20 2D 02 7E 09 6D 61 03 00 6E 00 15 75 72 72\n" +
            "65 6E 74 03 83 06 83 41 5F 6C 69 6E 65 6F 04 72\n" +
            "69 00 0B 61 74 69 6F 6E 24 20 30 00 6E 20 7B 81\n" +
            "9B 7D 20 9C 7B 76 80 21 87 B5 07 AB 73 70 84 3F\n" +
            "5F 0A 38 00 8C 83 37 81 5A 17 39 30 80 31 30 30\n" +
            "20 37 20 33 08 C6 32 43 25 20 4B 41 66 00 50 6E\n" +
            "40 3C 75 63 82 2F 6F 00 70 65 72 61 74 6F 72 73\n" +
            "09 82 7F 70 6C 83 49 78 20 79 20 18 70 6C 20 80\n" +
            "01 C2 43 74 72 61 04 6E 73 40 8D 6D 0D 0A 30 2E\n" +
            "40 32 35 20 73 75 62 C0 22 75 08 6E 64 20 82 03\n" +
            "61 64 64 20 D8 65 78 63 C0 9F 5B 07 69 C8 11 C4\n" +
            "46 10 2F 73 65 74 00 23 6F 6B 65 00 61 64 6A 75\n" +
            "73 74 20 77 58 68 65 72 81 A9 40 89 09 01 B0 74\n" +
            "30 72 75 65 20 0C 09 40 AE 2F 63 01 04 2A 31 20\n" +
            "79 31 20 78 32 00 20 79 32 20 78 33 20 79 28 33\n" +
            "20 63 C1 70 09 81 0F 63 75 8E 72 C1 C1 40 0B 04\n" +
            "1B 09 2F 43 82 0E 20 20 6C 6F 61 64 83 BC 09 2F\n" +
            "36 76 04 12 88 10 76 89 10 01 80 70 6F 80 69 6E\n" +
            "74 20 36 20 32 42 60 AA 20 10 16 56 01 16 76 0A\n" +
            "16 79 4F 28 AE 79 06 16 84 B1 50 12 59 41 12 79\n" +
            "4A 12 CD 08 65 6C 46 10 C1 A1 74 6F 00 0B 06 37\n" +
            "6A 4C 21 07 6C 2A 07 6D 64 10 C0 39 6D 2B 26 07\n" +
            "05 6C 09 25 07 7D 00 07 7B 25 F9 E4 83 2F 63 A1\n" +
            "01 60 00 E0 3F D0 13 31 28 FF 04 06 A0 05 90 25\n" +
            "D3 08 F1 25 C4 08 00 06 BF 23 1F 08 38 01 99 C1\n" +
            "3D E0 06 7F 22 0A 09 2F 1F 80 58 C2 43 E0 05 2F\n" +
            "21 C5 A4 25 20 67 40 72 61 70 68 69 63 80 83 61\n" +
            "48 74 65 20 C8 64 2F 64 81 2A 09 20 25 20 61 72\n" +
            "72 80 87 70 68 E0 61 73 65 20 64 A1 2B A0 10 20\n" +
            "55 18 64 61 73 20 61 64 2B 2F 63 66 8C 09 7B 62\n" +
            "01 80 06 25 20 2D 80 AF 00 20 66 6C 61 74 6E 65\n" +
            "73 99 20 6E 2F 69 44 09 45 02 20 69 E4 08 6F 21\n" +
            "AC 21 8B 65 22 40 B9 63 20 B4 40 36 69 CF C0 00\n" +
            "A0 0C 21 06 06 B7 2F 6A E4 08 01 20 72 6A 80 55\n" +
            "20 6A E7 11 65 02 A7 05 4A C1 A8 05 63 61 70 20\n" +
            "4A 8B 05 40 02 05 67 05 4D 64 05 6D 69 74 65 72\n" +
            "E4 6C 69 C0 00 20 4D C7 05 A7 02 27 06 02 77 A8\n" +
            "0B 77 69 64 74 68 20 9E 77 EB 0B 82 02 E6 05 84\n" +
            "92 70 61 40 6C 8B E0 C9 2A 2D 48 44 09 2D 20 48\n" +
            "42 08 15 85 2A 68 E6 02 68 44 0B 63 6C 6F AC 73\n" +
            "65 E3 CE 85 04 4E 86 04 4E 84 04 60 5F 70 6F 6C\n" +
            "61 03 B6 E4 2A 5F 40 64 6F 43 6C 69 70 E0 DC 65\n" +
            "50 71 20 7B 63 41 01 2F 65 02 30 2C 20 64 A0 E2\n" +
            "A1 B9 20 A0 05 6E 65 0E 77 C3 0B 40 30 84 07 2F\n" +
            "43 52 65 61 40 D7 72 20 7B 4E 60 0E 22 E8 09 D6\n" +
            "7D 05 48 A5 10 6E A6 10 6E A2 10 41 05 E9 61 ED\n" +
            "2F 46 06 03 46 DF 13 61 CC A0 0E 01 E2 56 09 67\n" +
            "73 61 76 65 20 48 5F 70 66 60 54 65 73 60 B7 65\n" +
            "06 20 22 17 24 0A 20 2F 5F 6C 70 60 20 2F 6E 6F\n" +
            "6E 60 29 50 09 20 E8 5F 66 63 11 0C 09 9C 0D F1\n" +
            "04 91 3F 1F 43 05 E0 04 02 01 F5 0B 6F 0E 20 7B\n" +
            "46 6B 6F 0E 65 0E 66 D6 0C 66 D4 0C 18 19 46 F5\n" +
            "C7 21 53 76 02 53 5F 0F 00 08 81 3C 4A 19 77 41\n" +
            "09 41 01 56 0F 73 5F 0F 81 19 5F 0F 73 DB 5F 0F\n" +
            "33 1A 09 59 0F 50 38 09 57 1B 5F 0F 58 20 7B 53\n" +
            "5F 0F 55 0F 73 D6 0C 73 D5 5F 0F 53 57 0F 42 76\n" +
            "02 42 5F 0F 5F 0F 14 71 20 D0 45 46 40 1D 65 61\n" +
            "72 DC 73 20 E4 10 60 02 83 1F 46 07 10 30 01 0D\n" +
            "29 21 53 AF 11 AF 11 65 66 20 5F FE 73 31 5E FF\n" +
            "20 80 10 82 22 F2 16 E0 0B 8A 11 B7 B0 06 71 01\n" +
            "47 2F 42 7F 11 75 11 62 F6 0E AA 62 7F 11 42 77\n" +
            "11 57 76 02 57 74 02 AD 36 2A 31 D2 37 A7 02 2A\n" +
            "A6 02 5B F0 82 91 80 42 5D 20 2A B5 05 6F 75 D0\n" +
            "79 1C 30 20 F0 2E 44 0A D1 53 74 79 70 D8 65 20\n" +
            "28 B3 02 B1 00 29 72 9A 20 85 EF 11 3D 70 02 84\n" +
            "3D 38 41 7B 04 34 03 02 B4 0C AD 81 60 6F E0 AE\n" +
            "09 60 75 B6 08 75 22 08 55 65 0F 55 76 01 55 7A\n" +
            "01 71 76 01 71 39 72 01 0D 0A 59 07 52 1D 3A 07\n" +
            "2F 51 C5 06 03 51 0F 03 7B 67 72 53 3F 3B 03 9A\n" +
            "2A 57 09 2A 63 09 65 06 31 20 01 9A DE 2F 33 07\n" +
            "71 9A AD 15 77 0B 2A 83 0B 97 03 7E 73 90 9E 9D\n" +
            "03 30 22 F9 0B 54 50 DB 08 44 03 14 0C 11 57 72\n" +
            "69 7A 65 64 20 4E 44 92 0C F2 17 62 38 2F 2A 85\n" +
            "62 2D AC 20 2A 13 62 75 14 2A 77 21 2A 83 21 A3\n" +
            "94 01 B0 62 6C 61 63 DB 78 60 94 06 01 F4 B3 6C\n" +
            "6C 78 20 6C 6C 79 B0 20 75 72 78 30 00 B6 BD 60\n" +
            "74 14 30 2F 5F 69 20 E2 53 B1 5D 0D 0A 12 36 14\n" +
            "CE 20 34 94 D0 63 6F 6E 28 63 61 74 63 D3 35 10\n" +
            "60 69 63 80 74 20 62 65 67 69 6E B0 D1 88 73 68\n" +
            "6F 30 60 67 65 20 03 7D 77 B0 B0 81 A9 70 82 79\n" +
            "A4 00 91 76 C2 73 31 37 81 01 D1 00 A4 6E 30 F5\n" +
            "00 B1 79 0D 0A E6 31 F2 00 B9 73 5B 5D 60 69 B6\n" +
            "83 74 28 9D 0A 06 66 D2 D5 20 02 B0 DD 72 70 90\n" +
            "D7 8A 74 B7 31 7E 24 10 2D 20 7E 64 0E 73 70 69\n" +
            "30 1C 69 20 94 22 A6 02 00 D0 6F 68 6C 6F 72 4A\n" +
            "76 4F D4 03 10 85 67 1C 20 4F 04 04 D1 34 20 0F\n" +
            "5F 6F 66 AF 92 BD 14 23 9C 65 17 08 52 39 04 52\n" +
            "3D 04 6E 73 3F 04 D6 69 37 04 67 34 04 01 15 20\n" +
            "A2 67 34 04 2F 5F 67 1C 08 66 B0 4B FE 7B 21 28\n" +
            "41 6E 81 DC 70 3F 64 3F D1 0A 1B 12 FE 09 E1 03\n" +
            "20 01 43 1A 10 4F 46 03 F3 1F F0 5E 3F F1 94 E0\n" +
            "25 33 29 52 70 60 4E 20 73 0D 0A 37 C1 05 E2 09\n" +
            "B5 01 73 A0 97 C5 01 68 76 E9 60 9E 6F 77 FA 01\n" +
            "6A 0C 02 D2 1F 51 23 A7 59 02 11 08 DF 0F 2F 47\n" +
            "D9 0F 47 D7 0F 1F BC 13 91 5B 21 08 51 04 B0 4D\n" +
            "6F 6B 65 01 E4 0F 00 00 10 72 F5 8B 82 F9 FF FF\n" +
            "A9 EB F6 8B 82 F9 FF FF F1 E8 F6 8B 82 F9 FF FF\n" +
            "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";
        byte[] compressed = toByteArray(chunk);
        byte[] uncompressed = new byte[0x20000];

        // Act
        new CompressedDataRun(null, 16).decompressUnit(compressed, uncompressed);

        // Assert
        String uncompressedContent = new String(uncompressed, StandardCharsets.US_ASCII);
        assertThat(uncompressedContent.startsWith("%!PS-Adobe-3.0 EPSF-3.0\r\n%%Creator: Adobe Illustrator(TM)"),
            is(true));
        assertThat(uncompressedContent, containsString("% matrix llx lly urx ury string")); // Near the end of the chunk
    }
}
