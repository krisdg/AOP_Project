package tcmis.mainpackage;

/**
 * Section 4.1, Page 51
 * Creating a JADE agent is as simple as defining a class that extends the jade.core.Agent 
 * class and implementing the setup() method as exemplified in the code below.
 **/

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import jade.core.Agent;
import jade.core.AID;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.*;

public class MonitorGUI extends JFrame {

	Monitor parentAgent;
	Overview canvas = new Overview();
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	MonitorGUI(Monitor parentAgent) {
		this.parentAgent = parentAgent;

        this.setSize(1000, 500);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setContentPane(canvas.view);
        this.pack();
        this.setLocationByPlatform(true);
        this.setVisible(true);
	}

	@Override
	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);

		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			parentAgent.doDelete();
		}
	}
	
	public void addCar(int curX, int curY, int desX, int desY, Color color, String name) {
		canvas.addCar(curX, curY, desX, desY, color, name);
	}
	
	public void addStation(int statX, int statY, Color color, String name) {
		canvas.addStation(statX, statY, color, name);
	}

	public void clearOverview() {
		canvas.initialize();
	}

}

class Overview
{
    JLabel view;
    BufferedImage surface;
    Random random = new Random();

    public Overview()
    {
        surface = new BufferedImage(1000,500,BufferedImage.TYPE_INT_RGB);
        view = new JLabel(new ImageIcon(surface));
        initialize();
    }
    
    public void initialize() {
    	Graphics g = surface.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0,0,1000,500);
        
        //DRAW GARAGE
        g.setColor(Color.BLACK);
        g.drawString("GARAGE", 4, 16);
        g.drawRect(-1, -1, 60, 25);
        g.drawRect(-1, -1, 59, 24);
        
        g.dispose();
    }

    public void addCar(int curX, int curY, int desX, int desY, Color color, String name) {
        Graphics g = surface.getGraphics();
        
        //DRAW DESTINATION
        g.setColor(new Color(190, 190, 190));
        g.drawLine(curX, curY, desX, desY);
        
        //DRAW CAR
        g.setColor(color);
        g.drawOval(curX - 7, curY - 7, 14, 14);
        g.drawLine(curX - 11, curY, curX + 11, curY);
        g.drawLine(curX, curY - 11, curX, curY + 11);
        
        //DRAW NAME
        g.setColor(new Color(110, 110, 110));
        g.drawString(name, curX + 14, curY + 14);
        
        g.dispose();
        view.repaint();
    }

    public void addStation(int statX, int statY, Color color, String name) {
        Graphics g = surface.getGraphics();
        
        //DRAW CAR
        g.setColor(color);
        g.drawOval(statX - 10, statY - 10, 20, 20);
        g.drawOval(statX - 9, statY - 9, 18, 18);
        
        //DRAW NAME
        g.setColor(new Color(0, 0, 0));
        g.drawString(name, statX - 4, statY + 4);
        
        g.dispose();
        view.repaint();
    }
}