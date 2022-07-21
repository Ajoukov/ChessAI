import javax.swing.JFrame;

public class ChessGame extends JFrame{
	public static void main(String[] args) {
		new ChessGame();
	}
	ChessGame(){
			
		this.add(new ChessPanel());
		this.setTitle("Chess");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);
		this.pack();
		this.setVisible(true);
		this.setLocationRelativeTo(null);
		
	}
}