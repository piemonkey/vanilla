package ch.blinkenlights.android.vanilla;

import java.util.ArrayList;
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

}
