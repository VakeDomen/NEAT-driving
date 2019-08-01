

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JFrame;
import javax.swing.JPanel;

import helpers.Config;

public class Gfx extends JPanel {

	
	
	
	private JFrame f;
	private SimulationHandler sh;
	
	public Gfx(SimulationHandler sh) {
		
		this.sh = sh;
		
		
		initFrame();
		initGui();
		
	}
	
	
	/*
	 * create and setup the frame of graphics
	 */
	private void initFrame() {
		f = new JFrame(Config.FRAME_TITLE);
		f.setPreferredSize(new Dimension(Config.FRAME_WIDTH, Config.FRAME_HEIGHT));
		f.pack();
		f.setResizable(false);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.add(this);
		f.setVisible(true);
	}
	
	/*
	 * sets initial values of graphics (Panel)
	 */
	private void initGui() {
		this.setBackground(Color.BLACK);
	}
	
	
	

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2d = (Graphics2D) g;
	
		
		sh.draw(g2d);
		
		
		super.repaint();
	}
	
	
	
	
	
	
}
