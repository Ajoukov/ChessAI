import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Random;
import java.io.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ChessPanel extends JPanel implements MouseListener {

	static final int SCREEN_WIDTH = 512;
	static final int SCREEN_HEIGHT = 512;
	static final int UNIT_SIZE = 64;
	boolean selected = false;
	int[] selectedSquare = {-1,-1};
	int movesCount = 0;
	int movesCountFifty = 0;
	boolean whiteMove = true;

	String moves = "";

	private int[][][] bNImg;
	private int[][][] bKImg;
	private int[][][] bQImg;
	private int[][][] bPImg;
	private int[][][] bBImg;
	private int[][][] bRImg;
	private int[][][] wRImg;
	private int[][][] wNImg;
	private int[][][] wBImg;
	private int[][][] wKImg;
	private int[][][] wQImg;
	private int[][][] wPImg;

	//positions of pieces
	String[][] squares = {
	{"bR","bN","bB","bQ","bK","bB","bN","bR"},
	{"bP","bP","bP","bP","bP","bP","bP","bP"},
	{"","","","","","","",""},
	{"","","","","","","",""},
	{"","","","","","","",""},
	{"","","","","","","",""},
	{"wP","wP","wP","wP","wP","wP","wP","wP"},
	{"wR","wN","wB","wQ","wK","wB","wN","wR"}};

	boolean BcastleQueen = true;
	boolean BcastleKing = true;
	boolean WcastleQueen = true;
	boolean WcastleKing = true;
//	Random random;
	
	ChessPanel() {
		addMouseListener(this);
		loadImages();
//		random = new Random();
		this.setPreferredSize(new Dimension(SCREEN_WIDTH,SCREEN_HEIGHT));
		this.setBackground(new Color(211, 217, 199));
		this.setFocusable(false);
	}
		@Override public void mouseReleased(MouseEvent mouse) {}
		@Override public void mouseClicked(MouseEvent mouse) {}
		@Override public void mouseEntered(MouseEvent mouse) {}
		@Override public void mouseExited(MouseEvent mouse) {}
	@Override public void mousePressed(MouseEvent mouse) {
		double x = (double) mouse.getX();
		double y = (double) mouse.getY();
		if (mouse.getButton() == MouseEvent.BUTTON1) {
			select((int)x/64,(int)y/64, true);
		}
	}

	private boolean select(int x1, int y1, boolean human) {
		int x = x1;
		int y = y1;
		if (selectedSquare[0] == -1 && !squares[y][x].equals("")) {
			selectedSquare[0] = x;
			selectedSquare[1] = y;
			this.repaint();
		}
		else if ((x != selectedSquare[0] || y != selectedSquare[1]) && selectedSquare[0] != -1) {
			move(x,y, human, false);
		} else {
			selectedSquare[0] = -1;
			selectedSquare[1] = -1;
			return false;
		}
		return true;
	}

	public void draw(Graphics g) {
		//Draw Grid
		g.setColor(new Color(93, 128, 56));
		for(int x = 0;x < 8;x++) {
			for (int y = 0; y < 8;y++) {
				g.setColor(new Color(93, 128, 56));
				if (x % 2 != y % 2) g.fillRect(y*UNIT_SIZE, x*UNIT_SIZE, UNIT_SIZE, UNIT_SIZE);
				g.setColor(new Color(255, 0, 0));
				if (x == selectedSquare[1] && y == selectedSquare[0] ) g.fillRect(y*UNIT_SIZE, x*UNIT_SIZE, UNIT_SIZE, UNIT_SIZE);
				String piece = squares[x][y];
				if (piece.equals("bN")) paintPiece(g, bNImg, x, y);
				if (piece.equals("wN")) paintPiece(g, wNImg, x, y);
				if (piece.equals("bK")) paintPiece(g, bKImg, x, y);
				if (piece.equals("wK")) paintPiece(g, wKImg, x, y);
				if (piece.equals("bQ")) paintPiece(g, bQImg, x, y);
				if (piece.equals("wQ")) paintPiece(g, wQImg, x, y);
				if (piece.equals("wR")) paintPiece(g, wRImg, x, y);
				if (piece.equals("bR")) paintPiece(g, bRImg, x, y);
				if (piece.equals("bB")) paintPiece(g, bBImg, x, y);
				if (piece.equals("wB")) paintPiece(g, wBImg, x, y);
				if (piece.equals("bP")) paintPiece(g, bPImg, x, y);
				if (piece.equals("wP")) paintPiece(g, wPImg, x, y);
			}
		}
		g.setColor(Color.BLACK);
		g.setFont(new Font("",Font.BOLD, 20));
		FontMetrics metrics = getFontMetrics(g.getFont());
		g.drawString("Moves: " + movesCount, (SCREEN_WIDTH - metrics.stringWidth("Moves: " + movesCount))/2, SCREEN_HEIGHT/2);
		//printBoard();
	}

	public int move(int x1, int y1, boolean human, boolean test) {
		int damage = 0;
		int x = x1;
		int y = y1;
		boolean valid = true;
		boolean takes = false;

		
		//Selected square x,y
		int x2 = selectedSquare[0];
		int y2 = selectedSquare[1];
		
		//Upper and lower values
		int c = Math.max(x,x2);
		int b = Math.min(x,x2);
		int e = Math.max(y,y2);
		int d = Math.min(y,y2);

		//If same color switch to other piece
		String selectedPiece = squares[y2][x2];
		if (!(squares[y][x].equals("") || !squares[y][x].substring(0,1).equals(selectedPiece.substring(0,1)))) {
			selectedSquare[0] = x;
			selectedSquare[1] = y; 
			//CHECK FOR CHECKS/MATE
			this.repaint();
			return -2;
		}
		//If opposite color then takes = true
		if (!squares[y][x].equals("") && !squares[y][x].substring(0,1).equals(selectedPiece.substring(0,1))) {
			takes = true;
		}

		//If not opposite color as last move then valid = false
		if (selectedPiece.substring(0,1).equals("w")) {
			if (!whiteMove) valid = false;
		} else if (whiteMove) valid = false;

		//ROOK
		if (selectedPiece.substring(1,2).equals("R")) {
			if (x != x2 && y != y2) valid = false;
			boolean horizontal = x != x2;
			if (horizontal) {
				for (int i = b+1; i<c; i++) {
					if (!squares[y][i].equals("")) valid = false;
				}
			}
			else {
				for (int i = d+1; i<e; i++) {
					if (!squares[i][x].equals("")) valid = false;
				}
			}
		}
		//PAWN TODO: EN PASSANT / QUEENING
		if (selectedPiece.substring(1,2).equals("P")) {
			//TODO: ADD EN PASSANT
			if (whiteMove) {
				//move by 2
				if (y2-y == 2) {
					if (c-b != 0) valid = false;
					if (y2 != 6) valid = false;
					if (!squares[y+1][x].equals("")) valid = false;
					if (!squares[y][x].equals("")) valid = false;
				}
				//move by 1
				else if (y2-y == 1 && c-b == 0) {
					if (!squares[y][x].equals("")) valid = false;
				} 
				//takes
				else if (y2-y == 1 && c-b == 1) {
					if (!takes) valid = false;
				} else {
					valid = false;
				}
			} else {
				//move by 2
				if (y-y2 == 2) {
					if (c-b != 0) valid = false;
					if (y2 != 1) valid = false;
					if (!squares[y-1][x].equals("")) valid = false;
					if (!squares[y][x].equals("")) valid = false;
				}
				//move by 1
				else if (y-y2 == 1 && c-b == 0) {
					if (!squares[y][x].equals("")) valid = false;
				} 
				//takes
				else if (y-y2 == 1 && c-b == 1) {
					if (!takes) valid = false;
				} else {
					valid = false;
				}

			}
		}
		//BISHOP
		if (selectedPiece.substring(1,2).equals("B")) {
			if (c-b != e-d) valid = false;
			int moveHor = (y > y2) ? 1 : -1;
			int moveVer = (x > x2) ? 1 : -1;
			if (!(c-b == 1 && e-d == 1)) {
				for (int i = 1; i < c-b; i++) {
					if (y2+(i*moveHor) < 0 || x2+(i*moveVer) < 0 || y2+(i*moveHor) > 8 || x2+(i*moveVer) > 8) {
						valid = false;
						break;
					}
				if (squares[y2+(i*moveHor)][x2+(i*moveVer)] != "") valid = false;
			}
			}
		}
		//KNIGHT
		if (selectedPiece.substring(1,2).equals("N")) {
			if (!(c-b == 2 && e-d == 1) ^ (e-d == 2 && c-b == 1)) valid = false;
			;
		}
		//KING
		if (selectedPiece.substring(1,2).equals("K")) {
			if (e-d > 1 || c-b > 1) valid = false;
			;
		}
		//QUEEN TODO!
		if (selectedPiece.substring(1,2).equals("Q")) {
			if (c-b == e-d) {	
				int moveHor = (y > y2) ? 1 : -1;
				int moveVer = (x > x2) ? 1 : -1;
				for (int i = 1; i < c-b; i++) {	
					if (y2+(i*moveHor) < 0 || x2+(i*moveVer) < 0 || y2+(i*moveHor) > 8 || x2+(i*moveVer) > 8) {
						valid = false;
						break;
					}
					if (squares[y2+(i*moveHor)][x2+(i*moveVer)] != "") valid = false;
				}
			}
			else if ((c-b != 0)  ^ (e-d != 0)) {
				boolean horizontal = x != x2;
				if (horizontal) {
					for (int i = b+1; i<c; i++) {
						if (!squares[y][i].equals("")) valid = false;
					}
				}
				else {
					for (int i = d+1; i<e; i++) {
						if (!squares[i][x].equals("")) valid = false;
					}
				}

			} else valid = false;
		}

		//Check if move into check etc.
		// if (checkPosition(x,y,x2,y2)) valid = false;
		if (valid) {
			if (takes) {
				String taken = squares[y][x].substring(1,2);
				switch (taken) {
					case "P":
						damage = 1;
					case "B":
						damage = 3;
					case "N":
						damage = 2;
					case "K":
						damage = 100;
						gameOver(0);
						break;
					case "Q":
						damage = 8;
					case "R":
						damage = 5;
				}
			}
			if (test) {
				return damage;
			}
			squares[y][x] = squares[y2][x2];
			squares[y2][x2] = "";
			System.out.println(selectedSquare[0] +","+ selectedSquare[1] +" to " +x + "," + y);




			//if white ++ movecount
			if (selectedPiece.substring(0,1).equals("w")) {
				movesCount++;
				moves += movesCount + ". ";
			}
			//TODO: CHECK IF MULTIPLE OF THE SAME PIECE CAN GO TO THE SAME SQUARE
			//Moves string += move (Ex. "Ng5")
			boolean isPawn = selectedPiece.substring(1,2).equals("P");
			if (!isPawn) {
				moves += selectedPiece.substring(1,2);
				if (takes) moves += "x";
			}
			if (isPawn && takes) moves += getPos(x2,y2).substring(0,1) + "x"; 
			moves+=getPos(x,y);

			
			moves+=" ";

			//Moves string += check/mate
			moves+=checkGame();

			//Fifty move count
			movesCountFifty++;
			System.out.println(moves);
			if (movesCountFifty >= 100) {
				gameOver(1);
				return -2;
			}
			if (damage == 100) {
				gameOver(0);
			}
			whiteMove = !whiteMove;
		}
		selectedSquare[0] = -1;
		selectedSquare[1] = -1;
		this.repaint();





		if (human) {
			botMove();
			return -2;
		}
		else {
			if (!valid) return -2;
			return damage;
		}

	}


	private void botMove() {
		int maxDamage = -1;
		int x1 = -1;
		int x2 = -1;
		int y1 = -1;
		int y2 = -1;
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				// if (squares[i][j].equals("") || squares[i][j].substring(0,1).equals("w")) {
				// 	j++;
				// 	continue;
				// }
				for (int k = 0; k < 8; k++) {
					for (int l = 0; l < 8; l++) {
						if (squares[i][j].equals("") || squares[i][j].substring(0,1).equals("w")) continue;
						selectedSquare[0] = -1;
						selectedSquare[1] = -1;
						if (!select(j, i, false)) continue;
						// System.out.println(i + " " + j + " " + k + " " + l);
						int damage = move(l, k, false, true);
						if (damage > maxDamage) {
							maxDamage = damage;
							x1 = i;
							x2 = j;
							y1 = k;
							y2 = l;
						}
					// 	if (move(l, k, false, false) != -1) {
					// 		System.out.println("GGGGGGGGGGGGG"); 
					// 		return;
					// 	}
					}					
				}
			}
		}
		selectedSquare[0] = x2;
		selectedSquare[1] = x1;
		move(y2, y1, false, false);
	}




	//TODO: CHECK IF SM IS IN CHECK
	// - means black in danger
	// public boolean checkPosition(int x, int y, int x2, int y2) {
	// 	//Copy of squares with move
		
	// 	String[][] squaresCopy = squares;
	// 	squaresCopy[y][x] = squaresCopy[y2][x2];
	// 	squaresCopy[y2][x2] = "";
	// 	//possible king moves DEFAULT = false
	// 	boolean[][] KingMoves = new boolean[8][8];
	// 	for (int i = 0; i < 8; i++) {
	// 		for (int j = 0; j < 8; j++) {
	// 			String here = squaresCopy[j][i];
	// 			if (!here.equals("") && ( ( here.substring(0,1).equals("w") && !whiteMove) || here.substring(0,1).equals("b") && whiteMove)) {
	// 				switch (here.substring(1,2)) {
	// 					case "R":
	// 						//horizontal
	// 						// for (int k = 0; k < i || squaresCopy[j][i+k]; k++) {
	// 							// if ()
	// 						// }
	// 						break;
	// 					case "Q":
	// 						break;
	// 					case "K":
	// 						break;
	// 					case "P":
	// 						break;
	// 					case "B":
	// 						break;
	// 				}
	// 			}
	// 		}
	// 	}
	// 	return false;
	// }
	public String getPos(int x, int y) {
		String returnString = "";
		switch (x) {
				case 0:
					returnString+="a";
					break;
				case 1:
					returnString+="b";
					break;
				case 2:
					returnString+="c";
					break;
				case 3:
					returnString+="d";
					break;
				case 4:
					returnString+="e";
					break;
				case 5:
					returnString+="f";
					break;
				case 6:
					returnString+="g";
					break;
				case 7:
					returnString+="h";
					break;
			}
			switch (y) {
				case 0:
					returnString+="8";
					break;
				case 1:
					returnString+="7";
					break;
				case 2:
					returnString+="6";
					break;
				case 3:
					returnString+="5";
					break;
				case 4:
					returnString+="4";
					break;
				case 5:
					returnString+="3";
					break;
				case 6:
					returnString+="2";
					break;
				case 7:
					returnString+="1";
					break;
			}
			return returnString;
	}
//TODO
	public String checkGame() {
		return "";
	}

	public void gameOver(int n) {
		if (n == 1) {
			System.out.println("Game End - 100 moves");
		}
		if (n == 0) {
			System.out.println("Game End - King taken");
			 		
		}
	}
	public void actionPerformed(ActionEvent e) {
		repaint();
	}

	private void loadImages() {
		bNImg = imageToByteArray("bN","png");
		wNImg = imageToByteArray("wN","png");
		bKImg = imageToByteArray("bK","png");
		wKImg = imageToByteArray("wK","png");
		bBImg = imageToByteArray("bB","png");
		wBImg = imageToByteArray("wB","png");
		bPImg = imageToByteArray("bP","png");
		wPImg = imageToByteArray("wP","png");
		bRImg = imageToByteArray("bR","png");
		wRImg = imageToByteArray("wR","png");
		bQImg = imageToByteArray("bQ","png");
		wQImg = imageToByteArray("wQ","png");
	}


	public void paintPiece(Graphics g, int[][][] piece, int gridX, int gridY) {
    	int numPixelsInRow = piece[0].length;
    	int numPixelsInCol = piece.length;

    	for(int x = 0; x < numPixelsInCol; x++) {
      		for(int y = 0; y < numPixelsInRow; y++) {
        		int r2 = piece[x][y][0];
	        	int g2 = piece[x][y][1];
        		int b2 = piece[x][y][2];
        		if (!(r2 == 255 && g2 == 0)) {
        			g.setColor(new Color(r2,g2,b2));
        			g.fillRect(1+gridY*(64)+y,1+gridX*(64)+x,1,1);
        		}
      		}
    	}
  	}


	public static int[][][] imageToByteArray(String a, String b){
    File file = new File(a + "." + b);
    BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    try {
        image = ImageIO.read(file);
    } catch (IOException e) {
        e.printStackTrace();
    }
    int p = image.getWidth();          
    int q = image.getHeight();      
    int r = 3;
    int[][][] data = new int[q][p][r];
    for(int y = 0; y < image.getHeight(); y++){
      for(int x = 0; x < image.getWidth(); x++){
        int px = image.getRGB(x,y);
        int red = (px >> 16) & 0xFF;
        int green = (px >> 8) & 0xFF;
        int blue = px & 0xFF;
        data[y][x][0] = red;
        data[y][x][1] = green;
        data[y][x][2] = blue;
      }
    }
    return data;
  }

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		draw(g);
	}

	public void printBoard() {
		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				System.out.print(squares[x][y] + " ");
			}
			System.out.print("\n");
		}
	}
}