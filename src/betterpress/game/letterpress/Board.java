package betterpress.game.letterpress;
// comment
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Board {

	private char[][] letterBoard;
	private char[][] colorBoard;

	// used for generating random boards
	private Random random;

	// all words that are allowed, initialized from dictfile

	// Constructor. Random characters, no players constructed yet. Blue goes
	// first.
	// Dictionary is read in in this constructor, exception currently unhandled.
	public Board(boolean verbose, boolean easyBoard) {
		this.random = new Random();

		// Initialize with random
		this.letterBoard = new char[5][5];
		this.colorBoard = new char[5][5];

		for (int i = 0; i < 5; ++i) {
			for (int j = 0; j < 5; ++j) {
				if (easyBoard) {
					this.letterBoard[i][j] = ((char) (5 * i + j + 'a'));
				} else {
					this.letterBoard[i][j] = ((char) (random.nextInt(26) + 'a'));
				}
				this.colorBoard[i][j] = '_';
			}
		}
	}
	
	private static char[][] reasonableLetterBoard() {
		Random random = new Random();
		String consonants = "bcdfghjklmnprstvwxyz";
		String vowels = "aeiou";
		List<Character> charSet = new ArrayList<Character>(25);
		int writeLoc = 0;
		int numVowels = random.nextInt(4) + 3;
		for ( ; writeLoc < numVowels; ++writeLoc) {
			charSet.add(vowels.charAt(random.nextInt(vowels.length())));
		}
		if (charSet.contains("u") || charSet.contains("i")) {
			if (random.nextInt(3) == 0) {
				++writeLoc;
				charSet.add('q');
			}
		}
		for ( ; writeLoc < 25; ++writeLoc) {
			charSet.add(consonants.charAt(random.nextInt(consonants.length())));
		}
		char[][] letterArray = new char[5][5];
		for (int i = 0; i < 5; ++i) {
			for (int j = 0; j < 5; ++j) {
				letterArray[i][j] = charSet.remove(random.nextInt(charSet.size()));
			}
		}
		return letterArray;
	}


	/**
	 * @return the letterBoard
	 */
	public char[][] getLetterBoard() {
		return letterBoard;
	}

	public static char[][] deepCopy5x5Array(char[][] array) {
		char[][] copiedArray = new char[5][5];
		for (int i = 0; i < 5; ++i) {
			for (int j = 0; j < 5; ++j) {
				copiedArray[i][j] = array[i][j];
			}
		}
		return copiedArray;
	}

	/**
	 * @return the colorBoard
	 */
	public char[][] getColorBoard() {
		return colorBoard;
	}


	public String betterPrint() {
		StringBuffer buf = new StringBuffer();
		buf.append("[Board] Board State: \n");
		for (int i = 0; i < 5; ++i) {
			for (int j = 0; j < 5; ++j) {
				char letter = Character.isUpperCase(colorBoard[i][j]) ? Character
						.toUpperCase(letterBoard[i][j]) : letterBoard[i][j];
				if (colorBoard[i][j] == 'b' || colorBoard[i][j] == 'B') {
					buf.append("[" + letter + "]");
				} else if (colorBoard[i][j] == 'r' || colorBoard[i][j] == 'R') {
					buf.append("(" + letter + ")");
				} else {
					buf.append(" " + letter + " ");
				}
			}
			buf.append("\n");
		}
		return buf.toString();
	}

	public void printLetters() {
		System.out.println("[Board] Board Letters: ");
		for (char[] row : this.letterBoard) {
			for (char letter : row) {
				System.out.print(letter + " ");
			}
			System.out.println();
		}
	}

	public void printColors() {
		System.out.println("[Board] Board Colors: ");
		for (char[] row : this.colorBoard) {
			for (char letter : row) {
				System.out.print(letter + " ");
			}
			System.out.println();
		}
	}

	// updates the board to reflect moves, if legal. else, prompts current
	// player.
	

	public static char[][] colorTiles(char[][] colorBoard, int[][] moves,
			char playerChar) {
		char[][] copyBoard = deepCopy5x5Array(colorBoard);
		for (int[] loc : moves) {
			char target = copyBoard[loc[0]][loc[1]];
			if (!Character.isUpperCase(target)) {
				copyBoard[loc[0]][loc[1]] = playerChar;
			}
		}
		return updateLocked(copyBoard);
	}

	

	// tell whoever's turn it is to provide a move, then plays that move.
	// char returned is the winner, as determined by checkWinner. Will be ' '
	// until the game is over.
	

	// returns the letter of who wins, or ' ' if no one has won yet.
	public char checkWinner() {
		
		int blue = 0;
		int red = 0;
		for (char[] row : colorBoard) {
			for (char color : row) {
				if (color == 'r' || color == 'R') {
					++red;
				}
				if (color == 'b' || color == 'B') {
					++blue;
				}
			}
		}
		
		if (blue + red == 25) {
			if (red > blue) {
				return 'r';
			} else {
				return 'b';
			}
		} else {
			return ' ';
		}
	}
	
	public char boardLeader() {
		int blue = 0;
		int red = 0;
		for (char[] row : colorBoard) {
			for (char color : row) {
				if (color == 'r' || color == 'R') {
					++red;
				}
				if (color == 'b' || color == 'B') {
					++blue;
				}
			}
		}
		if (blue > red) {
			return 'b';
		} else if (red > blue) {
			return 'r';
		} else {
			return ' ';
		}
	}

	// This function is ugly. Do not look at it.
	public static char[][] updateLocked(char[][] colorBoard) {
		char[][] newColorBoard = new char[5][5];
		for (int i = 0; i < 5; ++i) {
			for (int j = 0; j < 5; ++j) {
				// check whether [i][j] should be locked

				// assume locked
				newColorBoard[i][j] = Character.toUpperCase(colorBoard[i][j]);

				// for all 5 surrounding squares...
				for (int x = -1; x < 2; ++x) {
					// (but skip null pointer exceptions)
					if (i + x < 0 || i + x > 4) {
						continue;
					}
					for (int y = -1; y < 2; ++y) {
						// (but skip null pointer exceptions)
						if (j + y < 0 || j + y > 4) {
							continue;
						}

						// Also skip the corners.
						if (x * y != 0) {
							continue;
						}
						// if any don't match, don't lock it
						if (!charMatch(colorBoard[i][j], colorBoard[i + x][j
								+ y])) {
							newColorBoard[i][j] = Character
									.toLowerCase(colorBoard[i][j]);
						}
					}
				}
			}
		}
		return newColorBoard;
	}

	public static boolean charMatch(char char1, char char2) {
		if (Character.toLowerCase(char1) == Character.toLowerCase(char2)) {
			return true;
		} else {
			return false;
		}
	}

	public String whatWordDoesThisPlayMake(int[][] play) {
		char[] wordarray = new char[play.length];
		for (int i = 0; i < play.length; ++i) {
			wordarray[i] = letterBoard[play[i][0]][play[i][1]];
		}
		return new String(wordarray);
	}

	public void setColorBoard(char[][] colorTiles) {
		this.colorBoard = colorTiles;
	}
	
}
