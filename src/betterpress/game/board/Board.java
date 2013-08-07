package betterpress.game.board;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import betterpress.game.ai.Bot;
import betterpress.game.ai.Player;
import betterpress.game.ai.WordGetter;

public class Board {

	private char[][] letterBoard;
	private char[][] colorBoard;
	private Set<int[][]> playableWords;
	boolean verbose;

	// used for generating random boards
	private Random random;

	// all words that are allowed, initialized from dictfile
	private HashSet<String> dict;

	// words that have been played already
	private HashSet<String> usedWords;
	int pass = 0;

	// The two players, whether Bot or Human objects.
	private Player redPlayer;
	private Player bluePlayer;

	// Letter representing whose turn it is
	private char turn;

	// Constructor. Random characters, no players constructed yet. Blue goes
	// first.
	// Dictionary is read in in this constructor, exception currently unhandled.
	public Board(boolean verbose, boolean easyBoard) {
		InputStreamReader isr = new InputStreamReader(getClass()
				.getResourceAsStream("/resources/en.txt"));
		random = new Random();
		this.verbose = verbose;
		this.dict = new HashSet<String>(100000);
		this.usedWords = new HashSet<String>();
		BufferedReader br;
		try {
			br = new BufferedReader(isr);

			String nextline = "";

			while ((nextline = br.readLine()) != null) {
				dict.add(nextline);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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

		printLetters();

		char[][] letterBoardCopy = deepCopy5x5Array(this.letterBoard);
		this.playableWords = WordGetter.getPlays(letterBoardCopy, this.dict);
		System.out.println("[BOARD] there are " + this.playableWords.size()
				+ " legal moves");

		printLetters();

		// Blue goes first because I said so
		turn = 'b';
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
	 * @return the redPlayer
	 */
	public Player getRedPlayer() {
		return redPlayer;
	}

	/**
	 * @param redPlayer
	 *            the redPlayer to set
	 */
	public void setRedPlayer(Player redPlayer) {
		this.redPlayer = redPlayer;
	}

	/**
	 * @return the bluePlayer
	 */
	public Player getBluePlayer() {
		return bluePlayer;
	}

	/**
	 * @param bluePlayer
	 *            the bluePlayer to set
	 */
	public void setBluePlayer(Player bluePlayer) {
		this.bluePlayer = bluePlayer;
	}

	public HashSet<String> getDictionary() {
		return this.dict;
	}

	public void setDictionary(HashSet<String> dict) {
		this.dict = dict;
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

	/**
	 * @return the playableWords
	 */
	public Set<int[][]> getPlayableWords() {
		return playableWords;
	}

	public void betterPrint() {
		System.out.println("[Board] Board State: ");
		for (int i = 0; i < 5; ++i) {
			for (int j = 0; j < 5; ++j) {
				char letter = Character.isUpperCase(colorBoard[i][j]) ? Character
						.toUpperCase(letterBoard[i][j]) : letterBoard[i][j];
				if (colorBoard[i][j] == 'b' || colorBoard[i][j] == 'B') {
					System.out.print("[" + letter + "]");
				} else if (colorBoard[i][j] == 'r' || colorBoard[i][j] == 'R') {
					System.out.print("(" + letter + ")");
				} else {
					System.out.print(" " + letter + " ");
				}
			}
			System.out.println();
		}
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

	public boolean isValidWord(String word) {
		return (dict.contains(word) && !usedWords.contains(word));
	}

	// updates the board to reflect moves, if legal. else, prompts current
	// player.
	public char playWord(int[][] moves, char playerChar) {

		String word = whatWordDoesThisPlayMake(moves);

		if (isValidWord(word)) {
			usedWords.add(word);
			// Update colorboard to represent move

			this.colorBoard = colorTiles(this.colorBoard, moves, turn);

			switchTurns();
			if (verbose) {
				System.out.println("player " + turn + " played " + word);
				betterPrint();
			}

			return checkWinner();
		} else {
			System.out.println("[BOARD] Player " + turn
					+ " made an illegal move, and so loses.");
			switchTurns();
			return turn;
		}
	}

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

	// super simple. changes whose turn it is.
	private void switchTurns() {
		if (turn == 'r') {
			turn = 'b';
		} else {
			turn = 'r';
		}
	}

	// tell whoever's turn it is to provide a move, then plays that move.
	// char returned is the winner, as determined by checkWinner. Will be ' '
	// until the game is over.
	private char promptCurrentPlayer() {
		if (turn == 'r') {
			return playWord(redPlayer.provideMove(letterBoard, colorBoard,
					usedWords, turn), turn);
		} else {
			return playWord(bluePlayer.provideMove(letterBoard, colorBoard,
					usedWords, turn), turn);
		}
	}

	// returns the letter of who wins, or ' ' if no one has won yet.
	private char checkWinner() {
		
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
		
		if (blue + red == 25 || usedWords.size() == playableWords.size()) {
			if (red > blue) {
				return 'r';
			} else {
				return 'b';
			}
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

	// returns the char of the winner as soon as someone wins.
	public char playOneGame() {
		char winner;
		while ((winner = promptCurrentPlayer()) == ' ')
			;
		return winner;
	}

	public static void main(String[] args) {
		reasonableLetterBoard();
		Board board = new Board(true, false);
		board.printColors();
		board.printLetters();
		board.betterPrint();

		Bot bluePlayer = new Bot(board);
		Bot redPlayer = new Bot(board);
		board.bluePlayer = bluePlayer;
		board.redPlayer = redPlayer;

		// will currently fail because the Player objects are never initialized
		System.out.println(board.playOneGame());

	}
}