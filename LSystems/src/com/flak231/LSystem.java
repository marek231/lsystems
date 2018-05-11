package com.flak231;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

import javax.swing.*;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

@SuppressWarnings("serial")
public class LSystem extends JFrame{
	private JButton generateButton, clearButton;
	private Image turtleImage = null;
	private boolean clearFlag = false;
	
	public LSystem(String name) {
		super(name);
	}
	
	public void addComponentsToPane(final Container pane) {
		JPanel controls = new JPanel();
		Box buttonBox = Box.createHorizontalBox();
		generateButton = makeGenButton("Generate");
		clearButton = makeClearButton("Clear");
		buttonBox.add(generateButton);
		buttonBox.add(Box.createHorizontalStrut(10));
		buttonBox.add(clearButton);
		controls.add(buttonBox);
		pane.add(controls, BorderLayout.NORTH);
		
		//Decode JSON to provide parameters
		JSONParser parser = new JSONParser();
		Object obj = null;
		String axiom = "";
		Number angle = null;
		Map<String, String> rules = new HashMap<>(); 
        try {
            obj = parser.parse(new FileReader("resources/tree.json"));
 
            JSONObject mainObject = (JSONObject) obj;
            JSONObject chosenObject = (JSONObject) mainObject.get("Koch Snowflake");
 
            axiom = (String) chosenObject.get("axiom");
            angle = (Number) chosenObject.get("angle");
            rules = (Map<String, String>) chosenObject.get("rules");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
		pane.add(new TreeDrawing(axiom, (double) angle, rules), BorderLayout.CENTER);
	}
	
	public JButton makeGenButton(String text) {
		JButton theButton = new JButton();
		theButton.setText(text);
		theButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				turtleImage = null;
				repaint();
			}
		});
		return theButton; 
	}
	
	public JButton makeClearButton(String text) {
		JButton theButton = new JButton();
		theButton.setText(text);
		theButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				clearFlag = true;
				turtleImage = null;
				repaint();
			}
		});
		return theButton;
	}
	
	private static void createAndShowGUI() {
		LSystem frame = new LSystem("L-Systems");
		frame.setSize(1000, 800);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.addComponentsToPane(frame.getContentPane());
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

	private class TreeDrawing extends JComponent{
		private String axiom;
		private String sentence;
		private double angle;
		private Map<String, String> rules;
		private int len;
		private int initialLength;
		private Stack<AffineTransform> transformStack;
		public TreeDrawing(String axiom, double angle, Map<String, String> rules) {
			this.axiom = axiom;
			this.sentence = axiom;
			this.angle = Math.toRadians(angle);
			this.rules = rules;
			initialLength = len = 200;
			transformStack = new Stack<>();
		}
		
		public void generate() {
			len /= 2;
			String nextSentence = "";
			for(int i = 0; i < sentence.length(); i++) {
				char current = sentence.charAt(i);
				boolean found = false;
				if(rules.containsKey(String.valueOf(current))) {
					found = true;
					nextSentence += rules.get(String.valueOf(current));
				}
				if(!found) {
					nextSentence += current;
				}
			}
			sentence = nextSentence;
		}
		
		private void turtleDraw(Graphics2D g2d) {
		    if(turtleImage == null) {
		        turtleImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
		        Graphics2D graphics = ((BufferedImage) turtleImage).createGraphics();
		        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		        graphics.setStroke(new BasicStroke(1.5f));
		        //if clear flag is on, reset everything to the default values and paint a rectangle with the current background color
		        if(clearFlag) {
					sentence = axiom;
					len = initialLength;
					graphics.setColor(this.getBackground());
					graphics.fillRect(0, 0, getWidth(), getHeight());
					clearFlag = false;
				//else proceed with the turtle graphics drawing
				} else {
			        graphics.setColor(new Color(137, 181, 150));
			        graphics.translate(getWidth() / 2, getHeight());
			        //graphics.rotate(Math.toRadians(90.0));
			        for(int i = 0; i < sentence.length(); i++) {
			            char current = sentence.charAt(i);
			            if(current == 'F') {
			                graphics.drawLine(0, 0, 0, -len);
			                graphics.translate(0, -len);
			            } else if(current == '+') {
			                graphics.rotate(angle);
			            } else if(current == '-') {
			                graphics.rotate(-angle);
			            } else if(current == '[') {
			                transformStack.push(graphics.getTransform());
			            } else if(current == ']') {
			                graphics.setTransform(transformStack.pop());
			            }
			        }
			        generate();
				}
		    }
		    g2d.drawImage(turtleImage, 0, 0, null);
		}
		
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g.create();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			turtleDraw(g2d);
			g2d.dispose();
		}
	}
}