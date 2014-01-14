package ch.blinkenlights.android.vanilla;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.junit.Test;

public class MediaUtilsTest {

	@Test
	public void testShuffleListOfSongBooleanForEvenResultDistribution() {
		Random rand = new Random();
		final int nAlbums = 20;
		final int maxAlbumTracks = 20;
		final int iterations = 20000;
		int[][] resultGrid = new int[nAlbums][nAlbums];

		for (int runNo = 0; runNo < iterations; runNo++) {
			// Generate list of random length albums with all tracks in a random order
			List<Song> songs = new ArrayList<Song>(nAlbums * maxAlbumTracks);
			long songId = 0;
			for (int albumNo = 0; albumNo < nAlbums; albumNo++) {
				int albumTracks = rand.nextInt(maxAlbumTracks - 1) + 1;
				for (int trackNo = 0; trackNo < albumTracks; trackNo++) {
					Song song = new Song(songId);
					song.albumId = albumNo;
					songs.add(song);
					songId++;
				}
			}

			// Call method
			MediaUtils.shuffle(songs, true);

			// Extract album order and add to result grid
			long previousAlbumId = songs.get(0).albumId;
			List<Long> albumIds = new ArrayList<Long>();
			albumIds.add(previousAlbumId);
			for (int i = 0; i < songs.size(); i++) {
				long currentAlbumId = songs.get(i).albumId;
				if (currentAlbumId != previousAlbumId) {
					albumIds.add(currentAlbumId);
					previousAlbumId = currentAlbumId;
				}
			}
			for (int albumNo = 0; albumNo < nAlbums; albumNo++) {
				resultGrid[albumIds.get(albumNo).intValue()][albumNo]++;
			}
		}

		// Uncomment to print table of how often albums appear in each position to stdout
		// printOccurenceMatrix(nAlbums, resultGrid);

		// Analyse result grid
		double sumSquares = 0.0;
		final double mean = ((double) iterations) / nAlbums;
		for (int i = 0; i < nAlbums; i++) {
			for (int j = 0; j < nAlbums; j++) {
				int result = resultGrid[i][j];
				double diff = result - mean;
				sumSquares += diff * diff;
			}
		}
		Double measureOfDistribution = sumSquares / (iterations * nAlbums);
		org.junit.Assert.assertEquals(
				"Measure of even distribution should come out close to 1 for even distribution.",
				1.0, measureOfDistribution, 0.3);
	}

	@SuppressWarnings("unused")
	private void printOccurenceMatrix(final int nAlbums, int[][] resultGrid) {
		for (int i = 0; i < nAlbums; i++) {
			for (int j = 0; j < nAlbums; j++) {
				System.out.print(String.format("%04d ", resultGrid[i][j]));
			}
			System.out.println();
		}
	}

	@Test
	public void testPerformance() {
		Random rand = new Random();
		final int nAlbums = 20;
		final int maxAlbumTracks = 20;
		final int iterations = 1000000;
		
		final long start = new Date().getTime();
		for (int runNo = 0; runNo < iterations; runNo++) {
			List<Song> songs = new ArrayList<Song>(nAlbums * maxAlbumTracks);
			long songId = 0;
			for (int albumNo = 0; albumNo < nAlbums; albumNo++) {
				int albumTracks = rand.nextInt(maxAlbumTracks - 1) + 1;
				for (int trackNo = 0; trackNo < albumTracks; trackNo++) {
					Song song = new Song(songId);
					song.albumId = albumNo;
					songs.add(song);
					songId++;
				}
			}
			oldShuffle(songs, true);
		}
		final long mid = new Date().getTime();
		for (int runNo = 0; runNo < iterations; runNo++) {
			List<Song> songs = new ArrayList<Song>(nAlbums * maxAlbumTracks);
			long songId = 0;
			for (int albumNo = 0; albumNo < nAlbums; albumNo++) {
				int albumTracks = rand.nextInt(maxAlbumTracks - 1) + 1;
				for (int trackNo = 0; trackNo < albumTracks; trackNo++) {
					Song song = new Song(songId);
					song.albumId = albumNo;
					songs.add(song);
					songId++;
				}
			}
			MediaUtils.shuffle(songs, true);
		}
		final long end = new Date().getTime();
		
		System.out.println("Old shuffle time = " + (mid - start));
		System.out.println("New shuffle time = " + (end - mid));
				
	}
	
	/**
	 * Shuffle a Song list using Fisher-Yates algorithm.
	 *
	 * @param albumShuffle If true, preserve the order of tracks inside albums.
	 */
	private void oldShuffle(List<Song> list, boolean albumShuffle)
	{
		int size = list.size();
		if (size < 2)
			return;

		Random random = MediaUtils.getRandom();
		if (albumShuffle) {
			Song[] songs = list.toArray(new Song[size]);
			Song[] temp = new Song[size];

			// Make sure the albums are in order
			Arrays.sort(songs);

			// This is Fisher-Yates algorithm, but it swaps albums instead of
			// single elements.
			for (int i = size; --i != -1; ) {
				Song songI = songs[i];
				if (i > 0 && songs[i - 1].albumId == songI.albumId)
					// This index is not the start of an album. Skip it.
					continue;

				int j = random.nextInt(i + 1);
				while (j > 0 && songs[j - 1].albumId == songs[j].albumId)
					// This index is not the start of an album. Find the start.
					j -= 1;

				int lowerStart = Math.min(i, j);
				int upperStart = Math.max(i, j);

				if (lowerStart == upperStart)
					// Swap with ourself. That was easy!
					continue;

				long lowerAlbum = songs[lowerStart].albumId;
				int lowerEnd = lowerStart;
				while (lowerEnd + 1 < size && songs[lowerEnd + 1].albumId == lowerAlbum)
					lowerEnd += 1;

				long upperAlbum = songs[upperStart].albumId;
				int upperEnd = upperStart;
				while (upperEnd + 1 < size && songs[upperEnd + 1].albumId == upperAlbum)
					upperEnd += 1;

				int lowerSize = lowerEnd - lowerStart + 1;
				int upperSize = upperEnd - upperStart + 1;

				if (lowerSize == 1 && upperSize == 1) {
					// Easy, single element swap
					Song tempSong = songs[lowerStart];
					songs[lowerStart] = songs[upperStart];
					songs[upperStart] = tempSong;
				} else {
					// Slow multi-element swap. Copy to a new array in the
					// swapped order.
					System.arraycopy(songs, 0, temp, 0, lowerStart); // copy elements before lower
					System.arraycopy(songs, upperStart, temp, lowerStart, upperSize); // copy upper elements to lower spot
					System.arraycopy(songs, lowerEnd + 1, temp, lowerStart + upperSize, upperStart - lowerEnd - 1); // copy elements between upper and lower
					System.arraycopy(songs, lowerStart, temp, lowerStart + upperEnd - lowerEnd, lowerSize); // copy lower elements to upper spot
					System.arraycopy(songs, upperEnd + 1, temp, upperEnd + 1, size - upperEnd - 1); // copy elements remaining elements after upper

					// New array is finished. Use the old array as temp for the
					// next iteration.
					Song[] tempTemp = songs;
					songs = temp;
					temp = tempTemp;
				}
			}

			list.clear();
			list.addAll(Arrays.asList(songs));
		} else {
			Collections.shuffle(list, random);
		}
	}


	
}
