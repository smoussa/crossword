import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class CrosswordApp {

	public static void main(String[] arg) {
		
		// Run application on the event-dispatch thread.
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new CrosswordFrame("Crossword Application");
			}
		});
		
	}

}

class CrosswordIO {
	
	private Document document;
	
	/**
	 * Reads a puzzle into the application from an XML DOM formated file.
	 * The file may be a new crossword puzzle or one that has been saved previously.
	 * @return the crossword imported
	 */
	public Crossword readPuzzle() {
		
		Crossword crossword = null;
		
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int result = fileChooser.showOpenDialog(null);
		
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			if (file.exists()) {
				try {
					crossword = loadXMLDocument(file);
				} catch (Exception e) {
					System.err.println("Failed to read crossword puzzle. Try again.");
				}
			} else
				JOptionPane.showMessageDialog(null, "The file does not exist.", "Error", JOptionPane.ERROR_MESSAGE);
		}
		
		return crossword;
	}
	
	/**
	 * Loads the XML data from the specified file.
	 * @param file - The file to load from.
	 * @return the newly created crossword object
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private Crossword loadXMLDocument(File file) throws ParserConfigurationException, SAXException, IOException {
		
		ArrayList<Clue> acrossClues = new ArrayList<Clue>();
		ArrayList<Clue> downClues = new ArrayList<Clue>();
		
		DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = documentFactory.newDocumentBuilder();
		document = builder.parse(file);

		Node crosswordNode = document.getFirstChild();
		String title = crosswordNode.getAttributes().getNamedItem("title").getTextContent();
		int size = Integer.parseInt(crosswordNode.getAttributes().getNamedItem("size").getTextContent());
		
		NodeList crosswordNodeList = crosswordNode.getChildNodes();
		Node acrossNode = crosswordNodeList.item(1);
		if (acrossNode.getNodeType() == Node.ELEMENT_NODE) {
			
			NodeList acrossNodeList = acrossNode.getChildNodes();
			for (int i = 1; i < acrossNodeList.getLength(); i += 2) {
				
				Node clueNode = acrossNodeList.item(i);
				NodeList clueNodeList = clueNode.getChildNodes();
				
				int number = Integer.parseInt(clueNodeList.item(1).getTextContent());
				int x = Integer.parseInt(clueNodeList.item(3).getTextContent());
				int y = Integer.parseInt(clueNodeList.item(5).getTextContent());
				String cluePhrase = clueNodeList.item(7).getTextContent();
				String answer = clueNodeList.item(9).getTextContent();
				String user = clueNodeList.item(11).getTextContent();
				String time = clueNodeList.item(13).getTextContent();
				String userAnswer = clueNodeList.item(15).getTextContent();
				
				Clue clue = new Clue(number, x, y, cluePhrase, answer);
				clue.setUser(user);
				clue.setSolvedTime(time);
				clue.setUserAnswer(userAnswer);
				
				acrossClues.add(clue);
				
			}
			
		}
		
		Node downNode = crosswordNodeList.item(3);
		if (downNode.getNodeType() == Node.ELEMENT_NODE) {
			
			NodeList downNodeList = downNode.getChildNodes();
			for (int i = 1; i < downNodeList.getLength(); i += 2) {
				
				Node clueNode = downNodeList.item(i);
				NodeList clueNodeList = clueNode.getChildNodes();
				
				int number = Integer.parseInt(clueNodeList.item(1).getTextContent());
				int x = Integer.parseInt(clueNodeList.item(3).getTextContent());
				int y = Integer.parseInt(clueNodeList.item(5).getTextContent());
				String cluePhrase = clueNodeList.item(7).getTextContent();
				String answer = clueNodeList.item(9).getTextContent();
				String user = clueNodeList.item(11).getTextContent();
				String time = clueNodeList.item(13).getTextContent();
				String userAnswer = clueNodeList.item(15).getTextContent();
				
				Clue clue = new Clue(number, x, y, cluePhrase, answer);
				clue.setUser(user);
				clue.setSolvedTime(time);
				clue.setUserAnswer(userAnswer);
				
				downClues.add(clue);
				
			}
			
		}
		
		return new Crossword(title, size, acrossClues, downClues);
	}
	
	/**
	 * Saves the current crossword and writes it to a new file in an XML DOM format.
	 * @param crossword - The crossword to save.
	 */
	public void writePuzzle(Crossword crossword) {
		
		saveCurrentState(crossword);
		
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int result = fileChooser.showSaveDialog(null);

		if (result == JFileChooser.APPROVE_OPTION) {
			
			File file = fileChooser.getSelectedFile();
			String fileName = file.getAbsolutePath();
			
			if (validate(file)) {
				if (!fileName.endsWith(".xml"))
					fileName = fileName.concat(".xml");
				file = new File(fileName);
				try {
					createXMLDocument(file, crossword);
				} catch (Exception e) {
					System.err.println("Failed to write crossword puzzle. Try again.");
				}
			}
			
		}
		
	}
	
	/**
	 * Tests to see whether the selected file is valid by checking empty names and existing files of the same name.
	 * @param file - The file to test.
	 * @return whether the file is valid
	 */
	private boolean validate(File file) {
		
		boolean write = false;
		String fileName = file.getAbsolutePath();
		
		if (fileName.equals(""))
			JOptionPane.showMessageDialog(null, "The file name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
		else if (file.exists()) {
			int result = JOptionPane.showConfirmDialog(null, "This file already exists, would you like to overwrite it?");
			if (result == JOptionPane.YES_OPTION)
				write = true;
		} else
			write = true;
		
		return write;
	}
	
	/**
	 * Saves the current state of the specified crossword.
	 * @param crossword - The crossword to save.
	 */
	private void saveCurrentState(Crossword crossword) {
		
		for (Clue clue : crossword.acrossClues)
			clue.getGroup().saveUserAnswer();
		
		for (Clue clue : crossword.downClues)
			clue.getGroup().saveUserAnswer();
		
	}
	
	/**
	 * Creates a new XML document by converting the crossword object into XML nodes.
	 * @param file - The file to write to.
	 * @param crossword - The crossword to get the data from.
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 */
	private void createXMLDocument(File file, Crossword crossword) throws ParserConfigurationException, TransformerException {
		
		DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = documentFactory.newDocumentBuilder();
		document = builder.newDocument();
		
		Element crosswordNode = document.createElement("crossword");
		crosswordNode.setAttribute("title", crossword.title);
		crosswordNode.setAttribute("size", String.valueOf(crossword.size));
		document.appendChild(crosswordNode);
		
		Element acrossNode = document.createElement("across");
		crosswordNode.appendChild(acrossNode);
		
		Element downNode = document.createElement("down");
		crosswordNode.appendChild(downNode);
		
		for (Clue clue : crossword.acrossClues) {
			Element clueNode = document.createElement("clue");
			acrossNode.appendChild(clueNode);
			appendClueChildren(clueNode, clue);
		}
		
		for (Clue clue : crossword.downClues) {
			Element clueNode = document.createElement("clue");
			downNode.appendChild(clueNode);
			appendClueChildren(clueNode, clue);
		}
		
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
		
		DOMSource source = new DOMSource(document);
		StreamResult streamResult = new StreamResult(file);
		transformer.transform(source, streamResult);
		
	}
	
	/**
	 * Adds children nodes to each clue in the crossword.
	 * @param clueNode - The clue node to add children nodes to.
	 * @param clue - The clue object.
	 */
	private void appendClueChildren(Element clueNode, Clue clue) {
		
		Element numberNode = document.createElement("number");
		numberNode.setTextContent(String.valueOf(clue.number));
		clueNode.appendChild(numberNode);
		
		Element xNode = document.createElement("x_position");
		xNode.setTextContent(String.valueOf(clue.x));
		clueNode.appendChild(xNode);
		
		Element yNode = document.createElement("y_position");
		yNode.setTextContent(String.valueOf(clue.y));
		clueNode.appendChild(yNode);
		
		Element cluePhraseNode = document.createElement("clue_phrase");
		cluePhraseNode.setTextContent(clue.clue);
		clueNode.appendChild(cluePhraseNode);
		
		Element answerNode = document.createElement("answer");
		answerNode.setTextContent(clue.answer);
		clueNode.appendChild(answerNode);
		
		Element userNode = document.createElement("user");
		userNode.setTextContent(clue.getUser());
		clueNode.appendChild(userNode);
		
		Element timeNode = document.createElement("time");
		timeNode.setTextContent(clue.getSolvedTime());
		clueNode.appendChild(timeNode);
		
		Element userAnswerNode = document.createElement("user_answer");
		userAnswerNode.setTextContent(clue.getUserAnswer());
		clueNode.appendChild(userAnswerNode);
		
	}
	
}

class CrosswordExample {
	
	/**
	 * Gets the given default crossword example.
	 * @return the crossword object.
	 */
	public Crossword getPuzzle() {

		String title = "An example puzzle";
		int size = 11;
		ArrayList<Clue> acrossClues = new ArrayList<Clue>();
		ArrayList<Clue> downClues = new ArrayList<Clue>();

		acrossClues.add( new Clue( 1, 1, 0, "Eager Involvement", "enthusiasm") );
		acrossClues.add( new Clue( 8, 0, 2, "Stream of water", "river") );
		acrossClues.add( new Clue( 9, 6, 2, "Take as one's own", "adopt") );
		acrossClues.add( new Clue( 10, 0, 4, "Ball game", "golf") );
		acrossClues.add( new Clue( 12, 5, 4, "Guard", "sentry") );
		acrossClues.add( new Clue( 14, 0, 6, "Language communication", "speech") );
		acrossClues.add( new Clue( 17, 7, 6, "Fruit", "plum") );
		acrossClues.add( new Clue( 21, 0, 8, "In addition", "extra") );
		acrossClues.add( new Clue( 22, 6, 8, "Boundary", "limit") );
		acrossClues.add( new Clue( 23, 0, 10, "Executives", "management") );

		downClues.add( new Clue( 2, 2, 0, "Pertaining to warships", "naval") );
		downClues.add( new Clue( 3, 4, 0, "Solid", "hard") );
		downClues.add( new Clue( 4, 6, 0, "Apportion", "share") );
		downClues.add( new Clue( 5, 8, 0, "Concerning", "about") );
		downClues.add( new Clue( 6, 10, 0, "Friendly", "matey") );
		downClues.add( new Clue( 7, 0, 1, "Boast", "brag") );
		downClues.add( new Clue( 11, 3, 4, "Enemy", "foe") );
		downClues.add( new Clue( 13, 7, 4, "Doze", "nap") );
		downClues.add( new Clue( 14, 0, 6, "Water vapour", "steam") );
		downClues.add( new Clue( 15, 2, 6, "Consumed", "eaten") );
		downClues.add( new Clue( 16, 4, 6, "Loud, resonant sound", "clang") );
		downClues.add( new Clue( 18, 8, 6, "Yellowish, citrus fruit", "lemon") );
		downClues.add( new Clue( 19, 10, 6 , "Mongrel dog", "mutt") );
		downClues.add( new Clue( 20, 6, 7, "Shut with force", "slam") );

		return new Crossword(title, size, acrossClues, downClues);

	}

}

class Crossword {

	final ArrayList<Clue> acrossClues, downClues;
	final String title;
	final int size;
	
	/**
	 * Constructs a new crossword object.
	 * @param title - The crossword title.
	 * @param size - The number of cells across (or down).
	 * @param acrossClues - The ArrayList of clues going across.
	 * @param downClues - The ArrayList of clues going down.
	 */
	public Crossword (String title, int size, ArrayList<Clue> acrossClues, ArrayList<Clue> downClues) {
		
		this.title = title;
		this.size = size;
		this.acrossClues = acrossClues;
		this.downClues = downClues;
		
	}

}

class Clue {

	final int number, x, y;
	final String clue, answer;
	
	private String user;
	private String time;
	private String userAnswer;
	private boolean solved;
	
	private CellGroup group;
	
	/**
	 * Constructs a new crossword clue object.
	 * @param number - The clue number.
	 * @param x - The x position of the first clue character cell.
	 * @param y - The y position of the first clue character cell.
	 * @param clue - The clue help phrase.
	 * @param answer - The answer solution to the clue.
	 */
	public Clue (int number, int x, int y, String clue, String answer) {
		
		this.number = number;
		this.x = x;
		this.y = y;
		this.clue = clue;
		this.answer = answer;
		
		user = "";
		time = "";
		userAnswer = "";
		solved = false;
		
	}
	
	/**
	 * Adds the group of cells corresponding to that clue.
	 * @param group - The group of cells.
	 */
	public void addGroup(CellGroup group) {
		this.group = group;
	}
	
	/**
	 * Gets the clue's group.
	 * @return the clue's group
	 */
	public CellGroup getGroup() {
		return group;
	}
	
	/**
	 * Sets the user name entered for that clue.
	 * @param username - The user name.
	 */
	public void setUser(String username) {
		user = username;
	}
	
	/**
	 * Gets the user name.
	 * @return the user name saved
	 */
	public String getUser() {
		return user;
	}
	
	/**
	 * Sets the solved time of the clue.
	 * @param time - The time solved.
	 */
	public void setSolvedTime(String time) {
		this.time = time;
	}
	
	/**
	 * Gets the time solved.
	 * @return the time solved
	 */
	public String getSolvedTime() {
		return time;
	}
	
	/**
	 * Sets the user answer to the current answer entered for that crossword clue.
	 * @param answer - The answer entered.
	 */
	public void setUserAnswer(String answer) {
		userAnswer = answer;
	}
	
	/**
	 * Gets the user answer.
	 * @return the user answer
	 */
	public String getUserAnswer() {
		return userAnswer;
	}
	
	/**
	 * Sets whether the clue has been solved or not.
	 * @param solved - Whether the clue is solved.
	 */
	public void setSolved(boolean solved) {
		
		if (!solved)
			this.solved = false;
		else
			this.solved = true;
		DisplayPanel.SupportPanel.refresh();
		
	}
	
	/**
	 * Checks whether the clue has been solved.
	 * @return true if the clue has been solved
	 */
	public boolean sovled() {
		return solved;
	}
	
}

class Clock {
	
	private static final String DATE_TIME_FORMAT = "HH:mm dd/MM/yy";
	
	/**
	 * Gets the current date and time.
	 * @return the date and time String
	 */
	public String getDateAndTime() {
		
		Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT);
	    
	    return sdf.format(cal.getTime());
	}
	
}

class CrosswordFrame extends JFrame {
	
	private static Crossword crossword;
	private DisplayPanel displayPanel;
	
	/**
	 * Constructs the application frame.
	 * @param title - The title of the frame window.
	 */
	public CrosswordFrame(String title) {
		super(title);
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.out.println("Failed to load system LookAndFeel.");
		}
		
		displayPanel = new DisplayPanel();
		add(displayPanel);

		JMenuBar menuBar = new JMenuBar() {
			
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setColor(new Color(35, 35, 35));
				g.fillRect(0, 0, getWidth(), getHeight());
			}
			
		};
		menuBar.setPreferredSize(new Dimension(getWidth(), 30));
		menuBar.setBorder(null);
		setJMenuBar(menuBar);
		
		JMenu openMenu = new JMenu("  Open  ");
		openMenu.setFont(new Font(null, Font.PLAIN, 14));
		openMenu.setForeground(Color.white);
		
		JMenu saveMenu = new JMenu("  Save  ");
		saveMenu.setFont(new Font(null, Font.PLAIN, 14));
		saveMenu.setForeground(Color.white);
		
		JMenu exitMenu = new JMenu("  Exit  ");
		exitMenu.setFont(new Font(null, Font.PLAIN, 14));
		exitMenu.setForeground(Color.white);
		
		menuBar.add(openMenu);
		menuBar.add(saveMenu);
		menuBar.add(exitMenu);
		
		openMenu.addMouseListener(new MouseAdapter() { // Opens a new crossword
			@Override
			public void mousePressed(MouseEvent e) {
				
				CrosswordIO io = new CrosswordIO();
				crossword = io.readPuzzle();
				
				if (crossword != null) {
					remove(displayPanel);
					
					displayPanel = new DisplayPanel(crossword);
					add(displayPanel);
					validate();
					
					displayPanel.loadSavedState();
				}
				
			}
		});
		
		saveMenu.addMouseListener(new MouseAdapter() { // Saves the current crossword
			@Override
			public void mousePressed(MouseEvent e) {
				
				CrosswordIO io = new CrosswordIO();
				io.writePuzzle(DisplayPanel.crossword);
				
			}
		});
		
		exitMenu.addMouseListener(new MouseAdapter() { // Exits the application
			@Override
			public void mousePressed(MouseEvent e) {
				System.exit(0);
			}
		});
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		int frameWidth = 1000;
		int frameHeight = 700;
		setBounds((int) (dim.getWidth() - frameWidth) / 2,
				(int) (dim.getHeight() - frameHeight) / 2,
				frameWidth, frameHeight);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);

	}
	
}

class DisplayPanel extends JPanel {

	private static CrosswordPanel crosswordPanel;
	private static UserPanel userPanel;
	private static DisplayPanel.CluesPanel.AcrossCluesPanel acrossCluesPanel;
	private static DisplayPanel.CluesPanel.DownCluesPanel downCluesPanel;
	private static SupportPanel supportPanel;
	
	private static Color mainContentColor = Color.white;
	private static Color mainBorderColor = new Color(50, 120, 190); // (35, 35, 35)
	private static Font panelTitleFont = new Font(null, Font.PLAIN, 15);
	private static Font panelContentFont = new Font(null, Font.PLAIN, 14);
	
	public static Crossword crossword;
	
	/**
	 * Constructs a new display panel to hold all the components and sets crossword imported.
	 * @param cw - The crossword object loaded from a file.
	 */
	public DisplayPanel(Crossword cw) {
		
		crossword = cw;
		initLayout();
		display();
		
	}
	
	/**
	 * Constructs a new default display panel to hold all the components and
	 * sets up a new crossword game using the default crossword example.
	 */
	public DisplayPanel() {
		
		CrosswordExample example = new CrosswordExample();
		crossword = example.getPuzzle();
		initLayout();
		display();
		
	}
	
	/**
	 * Initialises other classes and creates other sub panels.
	 */
	private void initLayout() {
		
		crosswordPanel = new CrosswordPanel(crossword.size);
		userPanel = new UserPanel("Player One");
		acrossCluesPanel = new DisplayPanel.CluesPanel.AcrossCluesPanel("Across", crossword.acrossClues.size());
		downCluesPanel = new DisplayPanel.CluesPanel.DownCluesPanel("Down", crossword.downClues.size());
		supportPanel = new SupportPanel();
		
	}
	
	/**
	 * Displays the application interface on to the screen.
	 */
	private void display() {
		
		JLabel puzzleTitle = new JLabel(crossword.title.toUpperCase());
		puzzleTitle.setFont(new Font(null, Font.PLAIN, 17));
		puzzleTitle.setForeground(mainContentColor);
		
		SpringLayout layout = new SpringLayout();
		setLayout(layout);
		setBackground(new Color(0, 82, 140));
		
		layout.putConstraint(SpringLayout.NORTH, puzzleTitle, 10, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.WEST, puzzleTitle, 10, SpringLayout.WEST, this);
		
		layout.putConstraint(SpringLayout.NORTH, crosswordPanel, 5, SpringLayout.SOUTH, puzzleTitle);
		layout.putConstraint(SpringLayout.WEST, crosswordPanel, 0, SpringLayout.WEST, puzzleTitle);
		
		layout.putConstraint(SpringLayout.NORTH, acrossCluesPanel, 5, SpringLayout.SOUTH, userPanel);
		layout.putConstraint(SpringLayout.WEST, acrossCluesPanel, 10, SpringLayout.EAST, crosswordPanel);
		layout.putConstraint(SpringLayout.SOUTH, acrossCluesPanel, 0, SpringLayout.SOUTH, crosswordPanel);
		
		layout.putConstraint(SpringLayout.NORTH, downCluesPanel, 0, SpringLayout.NORTH, acrossCluesPanel);
		layout.putConstraint(SpringLayout.WEST, downCluesPanel, 10, SpringLayout.EAST, acrossCluesPanel);
		layout.putConstraint(SpringLayout.SOUTH, downCluesPanel, 0, SpringLayout.SOUTH, crosswordPanel);
		
		layout.putConstraint(SpringLayout.NORTH, userPanel, 0, SpringLayout.NORTH, crosswordPanel);
		layout.putConstraint(SpringLayout.WEST, userPanel, 10, SpringLayout.EAST, crosswordPanel);
		
		layout.putConstraint(SpringLayout.NORTH, supportPanel, 5, SpringLayout.SOUTH, crosswordPanel);
		layout.putConstraint(SpringLayout.WEST, supportPanel, 0, SpringLayout.WEST, crosswordPanel);
		layout.putConstraint(SpringLayout.EAST, supportPanel, 0, SpringLayout.EAST, downCluesPanel);
		layout.putConstraint(SpringLayout.SOUTH, supportPanel, -10, SpringLayout.SOUTH, this);
		
		add(puzzleTitle);
		add(crosswordPanel);
		add(userPanel);
		add(acrossCluesPanel);
		add(downCluesPanel);
		add(supportPanel);
		
	}
	
	/**
	 * Loads the saved state of the imported crossword object by adding the user's answers to the crossword.
	 */
	public void loadSavedState() {
		
		for (Clue clue : crossword.acrossClues) {
			String userAnswer = clue.getUserAnswer();
			clue.getGroup().loadUserAnswer(userAnswer);
			if (userAnswer.equals(clue.answer))
				clue.setSolved(true);
		}
		
		for (Clue clue : crossword.downClues) {
			String userAnswer = clue.getUserAnswer();
			clue.getGroup().loadUserAnswer(userAnswer);
			if (userAnswer.equals(clue.answer))
				clue.setSolved(true);
		}
		
	}
	
	class CrosswordPanel extends JPanel {
		
		private Cell[][] cellArray;
		
		/**
		 * Constructs a new crossword panel to hold the crossword game.
		 * @param size - The size of the crossword.
		 */
		public CrosswordPanel(int size) {
			
			cellArray = new Cell[size][size];
			
			Dimension dimension = new Dimension(400, 400);
			setPreferredSize(dimension);
			setBackground(Color.black);
			setLayout(new GridLayout(size, size, 1, 1));
			setBorder(new EmptyBorder(2, 2, 0, 0));
			
			addClues();
			addSolidCells();
			buildGrid();
			
		}
		
		/**
		 * Adds the across and down clues to the crossword and positions them accordingly.
		 */
		private void addClues() {
			
			for (int c = 0; c < crossword.acrossClues.size(); c++) {
				Clue clue = crossword.acrossClues.get(c);
				position(clue, true);
			}
			
			for (int c = 0; c < crossword.downClues.size(); c++) {
				Clue clue = crossword.downClues.get(c);
				position(clue, false);
			}
			
		}
		
		/**
		 * Positions the clue onto the crossword by making a new clue group to hold clue cells, one for each character.
		 * The idea is to create a group for each clue. A clue will have a single group. Each group will have a set number
		 * of cells, one for each character of the clue answer. A cell can be in two groups only if that character cell
		 * will be in two clues (a double cell).
		 * @param clue - The clue to position.
		 * @param across - Whether the clue is an across clue.
		 */
		private void position(Clue clue, boolean across) {
			
			String answer = clue.answer;
			int length = answer.length();
			int x = clue.x;
			int y = clue.y;
			
			CellGroup group = new CellGroup(clue, across); // New cell group.
			
			// Add the first clue cell with the clue number label.
			Cell newCell = cellArray[x][y];
			if (newCell == null) {
				newCell = cellArray[x][y] = new Cell(clue.number, answer.charAt(0));
				group.cells.add(newCell);
			} else {
				Cell oldCell = cellArray[x][y];
				CellGroup firstGroup = oldCell.getGroup(0);
				int index = firstGroup.cells.indexOf(oldCell);
				cellArray[x][y] = new Cell(clue.number, answer.charAt(0));
				newCell = cellArray[x][y];
				newCell.setAsDoubleCell();
				firstGroup.cells.remove(index);
				firstGroup.cells.add(index, newCell);
				group.cells.add(newCell);
				newCell.addGroup(firstGroup);
			}
			
			newCell.addGroup(group);
			
			// Add the rest of the cells.
			for (int c = 0; c < length - 1; c++) {
				if (across)
					x++;
				else
					y++;
				Cell cell = cellArray[x][y];
				if (cell == null) {
					cell = cellArray[x][y] = new Cell(answer.charAt(c + 1));
					group.cells.add(cell);
				} else {
					cell.setAsDoubleCell();
					group.cells.add(cell);
				}
				cell.addGroup(group);
			}
			
			clue.addGroup(group);
			
		}
		
		/**
		 * Adds the solid black cells to the empty spaces of the array.
		 */
		private void addSolidCells() {
			
			for (int i = 0; i < crossword.size; i++)
				for (int j = 0; j < crossword.size; j++)
					if (cellArray[i][j] == null)
						cellArray[i][j] = new Cell();
			
		}
		
		/**
		 * Adds the clue cells in the array to the grid layout of the crossword panel.
		 */
		private void buildGrid() {
			
			for (int i = 0; i < crossword.size; i++)
				for (int j = 0; j < crossword.size; j++)
					add(cellArray[j][i]);
			
		}
		
	}
	
	static class UserPanel extends JPanel {
		
		public static String username;
		private static final JTextField textField = new JTextField(20);
		private static final JButton saveButton = new JButton("Saved");
		
		/**
		 * Constructs a new user panel to store the user name entered.
		 * @param name
		 */
		public UserPanel(String name) {
			
			username = name;
			
			setLayout(new FlowLayout(FlowLayout.LEADING));
			setBackground(null);
			
			Border raisedBorder = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
			TitledBorder title = BorderFactory.createTitledBorder(raisedBorder, "  Username  ");
			title.setBorder(BorderFactory.createLineBorder(mainBorderColor));
			title.setTitleColor(mainContentColor);
			title.setTitleFont(panelTitleFont);
			setBorder(title);
			
			textField.setText(username);
			textField.setFont(panelContentFont);
			textField.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					if (!saveButton.isEnabled()) {
						saveButton.setEnabled(true);
						saveButton.setText("Save");
					}
				}
			});
			textField.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					char c = e.getKeyChar();
					if (c == KeyEvent.VK_ENTER)
						save();
					else
						if (!saveButton.isEnabled()) {
							saveButton.setEnabled(true);
							saveButton.setText("Save");
						}
				}
			});
			
			saveButton.setFont(panelContentFont);
			saveButton.setBackground(null);
			saveButton.setEnabled(false);
			saveButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					save();
				}
			});
		    
		    add(textField);
		    add(saveButton);
			
		}
		
		/**
		 * Saves the user name written to the text field.
		 */
		private void save() {
			
			String text = textField.getText();
			if (!text.equals("")) {
				username = text;
				saveButton.setEnabled(false);
				saveButton.setText("Saved");
			} else
				JOptionPane.showMessageDialog(null, "Username field is empty.");
			
		}

	}
	
	abstract static class CluesPanel extends JPanel {
		
		public static JList acrossList;
		public static JList downList;
		
		/**
		 * Constructs a new panel to store the list of across and down clues.
		 * @param title - The title of the panel.
		 * @param size - The number of clues in that panel.
		 */
		public CluesPanel(String title, int size) {
			
			setLayout(new FlowLayout(FlowLayout.LEADING));
			setBackground(null);
			
			Border raisedBorder = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
			TitledBorder borderTitle = BorderFactory.createTitledBorder(raisedBorder, "  " + title + "  ");
			borderTitle.setBorder(BorderFactory.createLineBorder(mainBorderColor));
			borderTitle.setTitleColor(mainContentColor);
			borderTitle.setTitleFont(panelTitleFont);
			setBorder(borderTitle);
			
		}
		
		/**
		 * Gets the number of characters in the specified clue answer.
		 * @param answer - The clue answer.
		 * @return the string representation of the number of the characters in the clue answer.
		 */
		protected String getClueLength(String answer) {
			
			String dash = "-";
			String space = " ";
			
			String[] words = null;
			if (answer.contains(dash)) {
				words = answer.split(dash);
				String size = String.valueOf(words[0].length());
				int i = 1;
				while (i < words.length) {
					size = size.concat(dash + words[i].length());
					i++;
				}
				return size;
			} else if (answer.contains(space)) {
				words = answer.split(space);
				String size = String.valueOf(words[0].length());
				int i = 1;
				while (i < words.length) {
					size = size.concat("," + words[i].length());
					i++;
				}
				return size;
			} else
				return String.valueOf(answer.length());
			
		}
		
		static class AcrossCluesPanel extends CluesPanel {
			
			private String[] acrossArray;
			
			/**
			 * Constructs a new panel to hold the list of across clues.
			 * @param title - The title of the panel.
			 * @param size - The number of clues in the panel.
			 */
			public AcrossCluesPanel(String title, int size) {
				super(title, size);
				
				acrossArray = new String[size];
				addAcrossClues();
				setListPrefs(new JList(acrossArray));
			}
			
			/**
			 * Adds the across clues to the array of across clues.
			 */
			private void addAcrossClues() {
				
				for (int c = 0; c < crossword.acrossClues.size(); c++) {
					Clue clue = crossword.acrossClues.get(c);
					acrossArray[c] = clue.number + ". " + clue.clue + " (" + getClueLength(clue.answer) + ")";
				}
				
			}
			
			/**
			 * Customises the layout of the JList and adds the list of data to the scroll pane.
			 * @param list - The list of data.
			 */
			private void setListPrefs(JList list) {
				
				acrossList = list;
				
				acrossList.setVisibleRowCount(12);
				acrossList.setBorder(new EmptyBorder(5, 5, 5, 5));
				acrossList.setFont(panelContentFont);
				acrossList.setBackground(null);
				acrossList.setForeground(mainContentColor);
				acrossList.addMouseListener(new MouseAdapter() {
					@Override
					public void mousePressed(MouseEvent e) {
						downList.clearSelection();
						crossword.acrossClues.get(acrossList.getSelectedIndex()).getGroup().highlight();
					}
				});
				
				JScrollPane scrollPane = new JScrollPane(acrossList);
				scrollPane.getViewport().setBackground(null);
				scrollPane.setBorder(null);
				scrollPane.setBackground(null);
				
				add(scrollPane);
				
			}
			
			/**
			 * This method is called when a clue cell has been clicked. This highlights only that clue that has been selected.
			 * @param clue - The clue reference to be highlighted.
			 */
			public static void selectClue(Clue clue) {
				
				downList.clearSelection();
				int index = crossword.acrossClues.indexOf(clue);
				acrossList.ensureIndexIsVisible(index);
				acrossList.setSelectedIndex(index);
				
			}
			
		}
		
		static class DownCluesPanel extends CluesPanel {
			
			private String[] downArray;
			
			/**
			 * Constructs a new panel to hold the list of down clues.
			 * @param title - The title of the panel.
			 * @param size - The number of clues in the panel.
			 */
			public DownCluesPanel(String title, int size) {
				super(title, size);
				
				downArray = new String[size];
				addDownClues();
				setListPrefs(new JList(downArray));
			}
			
			/**
			 * Adds the down clues to the array of down clues.
			 */
			private void addDownClues() {
				
				for (int c = 0; c < crossword.downClues.size(); c++) {
					Clue clue = crossword.downClues.get(c);
					downArray[c] = clue.number + ". " + clue.clue + " (" + getClueLength(clue.answer) + ")";
				}
				
			}
			
			/**
			 * Customises the layout of the JList and adds the list of data to the scroll pane.
			 * @param list - The list of data.
			 */
			private void setListPrefs(JList list) {
				
				downList = list;
				
				downList.setVisibleRowCount(12);
				downList.setBorder(new EmptyBorder(5, 5, 5, 5));
				downList.setFont(panelContentFont);
				downList.setBackground(null);
				downList.setForeground(mainContentColor);
				downList.addMouseListener(new MouseAdapter() {
					@Override
					public void mousePressed(MouseEvent e) {
						acrossList.clearSelection();
						crossword.downClues.get(downList.getSelectedIndex()).getGroup().highlight();
					}
				});
				
				JScrollPane scrollPane = new JScrollPane(downList);
				scrollPane.getViewport().setBackground(null);
				scrollPane.setBorder(null);
				scrollPane.setBackground(null);
				
				add(scrollPane);
				
			}
			
			/**
			 * This method is called when a clue cell has been clicked. This highlights only that clue that has been selected.
			 * @param clue - The clue reference to be highlighted.
			 */
			public static void selectClue(Clue clue) {
				
				acrossList.clearSelection();
				int index = crossword.downClues.indexOf(clue);
				downList.ensureIndexIsVisible(index);
				downList.setSelectedIndex(index);
				
			}
			
		}

	}
	
	static class SupportPanel extends JPanel {
		
		private static boolean support;
		public static JTextArea textArea;
		private static JScrollPane scrollPane;
		
		/**
		 * Constructs a new panel to allow for the support feature.
		 */
		public SupportPanel() {
			
			support = false;
			
			SpringLayout layout = new SpringLayout();
			setLayout(layout);
			setBackground(null);
			
			Border raisedBorder = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
			TitledBorder title = BorderFactory.createTitledBorder(raisedBorder, "  Solved Clues Support  ");
			title.setBorder(BorderFactory.createLineBorder(mainBorderColor));
			title.setTitleColor(mainContentColor);
			title.setTitleFont(panelTitleFont);
			setBorder(title);
			
			textArea = new JTextArea(5, 20);
			textArea.setEditable(false);
			textArea.setFont(new Font(null, Font.PLAIN, 10));
			scrollPane = new JScrollPane(textArea);
			scrollPane.setVisible(false);
			
			final JButton supportButton = new JButton("Turn support on");
			supportButton.setFont(panelContentFont);
			supportButton.setBackground(null);
			supportButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					
					support = !support;
					if (support) {
						scrollPane.setVisible(true);
						supportButton.setText("Turn support off");
					} else {
						scrollPane.setVisible(false);
						supportButton.setText("Turn support on");
					}
					
				}
				
			});
			
			layout.putConstraint(SpringLayout.NORTH, supportButton, 0, SpringLayout.NORTH, this);
			layout.putConstraint(SpringLayout.WEST, supportButton, 10, SpringLayout.WEST, this);
			
			layout.putConstraint(SpringLayout.NORTH, scrollPane, 0, SpringLayout.NORTH, supportButton);
			layout.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.EAST, supportButton);
			layout.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.EAST, this);
			layout.putConstraint(SpringLayout.SOUTH, scrollPane, -10, SpringLayout.SOUTH, this);
			
			add(supportButton);
			add(scrollPane);
			
		}
		
		/**
		 * Refreshes the text area to test for changes in the crossword answers and logs the user's information.
		 */
		public static void refresh() {
			
			textArea.setText("");
			for (Clue clue : crossword.acrossClues) {
				if (clue.sovled()) {
					textArea.append(
							"Number: " + clue.number + "     Clue: " + clue.clue + "     Answer: " + clue.answer + "     " +
									"Solved by: " + clue.getUser() + "     Time and Date: " + clue.getSolvedTime() + "\n");
				}
			}
			for (Clue clue : crossword.downClues) {
				if (clue.sovled()) {
					textArea.append(
							"Number: " + clue.number + "     Clue: " + clue.clue + "     Answer: " + clue.answer + "     " +
									"Solved by: " + clue.getUser() + "     Time and Date: " + clue.getSolvedTime() + "\n");
				}
			}
			
		}

	}

}

class CellGroup {
	
	private Clue clue;
	public boolean across;
	public ArrayList<Cell> cells;
	
	private static ArrayList<Cell> lastHighlighted;
	public int focusedCellNum;
	
	/**
	 * Constructs a new cell group object to hold clue character cells.
	 * @param clue - The clue to represent.
	 * @param across - Whether the clue is across or down.
	 */
	public CellGroup(Clue clue, boolean across) {
		
		this.clue = clue;
		this.across = across;
		cells = new ArrayList<Cell>();
		lastHighlighted = new ArrayList<Cell>(0);
		focusedCellNum = 0;
		
	}
	
	/**
	 * Highlights this group of cells.
	 */
	public void highlight() {
		
		for (Cell c : lastHighlighted) { // Remove highlight from old cells.
			c.highlighted = false;
			c.setBorder(BorderFactory.createLineBorder(Color.white));
			c.repaint();
		}
		
		lastHighlighted = cells; // Highlight new cells.
		for (Cell c : cells) {
			c.highlighted = true;
			c.currentGroup = this;
			c.repaint();
		}
		
		if (across)
			DisplayPanel.CluesPanel.AcrossCluesPanel.selectClue(clue);
		else
			DisplayPanel.CluesPanel.DownCluesPanel.selectClue(clue);
		
	}
	
	/**
	 * Shifts the focus of the cell to the next one in the group.
	 */
	public void moveToNextCell() {
		
		if (focusedCellNum != cells.size() - 1) {
			cells.get(focusedCellNum).focused = false;
			cells.get(focusedCellNum).setBorder(BorderFactory.createLineBorder(Color.white));
			focusedCellNum += 1;
			cells.get(focusedCellNum).focus();
		}
		
	}
	
	/**
	 * Shifts the focus of the cell to the previous one in the group.
	 */
	public void moveToPreviousCell() {
		
		if (focusedCellNum != 0) {
			cells.get(focusedCellNum).focused = false;
			cells.get(focusedCellNum).setBorder(BorderFactory.createLineBorder(Color.white));
			focusedCellNum -= 1;
			cells.get(focusedCellNum).focus();
		}
		
	}
	
	/**
	 * Tests whether the solution is correct.
	 */
	public void testAnswer() {
		
		int correct = 0;
		
		for (int i = 0; i < cells.size(); i++) {
			if (cells.get(i).isCorrect())
				correct++;
		}
		if (correct == cells.size())
			save(true);
		else
			save(false);
		
	}
	
	/**
	 * Saves the user information when the clue is solved.
	 * @param solved - True if the clue is solved.
	 */
	private void save(boolean solved) {
		
		clue.setUser(DisplayPanel.UserPanel.username);
		clue.setSolvedTime(new Clock().getDateAndTime());
		clue.setSolved(solved);
		
	}
	
	/**
	 * Saves the user answer. This method is used when saving the user information to an XML file.
	 */
	public void saveUserAnswer() {
		
		String answer = "";
		
		for (int i = 0; i < cells.size(); i++) {
			char character = cells.get(i).enteredCharacter;
			if (character == 0)
				answer = answer.concat(".");
			else
				answer = answer.concat(String.valueOf(character));
		}
		
		clue.setUserAnswer(answer);
		
	}
	
	/**
	 * Shows the user answer in the crossword when loaded from the XML file.
	 * @param answer - The user answer to show.
	 */
	public void loadUserAnswer(String answer) {
		
		for (int i = 0; i < cells.size(); i++) {
			Cell cell = cells.get(i);
			char character = answer.charAt(i);
			if (character == '.')
				cell.enteredCharacter = 0;
			else
				cell.enteredCharacter = character;
			cell.repaint();
		}
		
	}
	
}

class Cell extends JPanel {
	
	private ArrayList<CellGroup> groups;
	public CellGroup currentGroup;
	
	public boolean highlighted;
	public boolean focused;
	private boolean doubleCell;
	private static int clickCount;
	
	private JLabel numberLabel;
	public char enteredCharacter;
	private char character;
	
	private static final Color focusBorderColor = new Color(255, 50, 0);
	private static final Color hoverBorderColor = new Color(150, 150, 150);
	
	private boolean solidCell;
	
	/**
	 * Constructs a new labelled clue character cell object.
	 * @param clueNumber - The clue number.
	 * @param character - The correct character.
	 */
	public Cell(int clueNumber, char character) {
		
		init(character);
		
		Font smallFont = new Font(null, Font.PLAIN, 8);
		numberLabel = new JLabel(String.valueOf(clueNumber));
		numberLabel.setFont(smallFont);
		
		SpringLayout layout = new SpringLayout();
		setLayout(layout);
		
		layout.putConstraint(SpringLayout.NORTH, numberLabel, 2, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.WEST, numberLabel, 2, SpringLayout.WEST, this);
		
		add(numberLabel);
		
	}
	
	/**
	 * Constructs a new empty cell object.
	 * @param character - The correct character.
	 */
	public Cell(char character) {
		init(character);
	}
	
	/**
	 * Constructs an empty solid black cell.
	 */
	public Cell() {
		solidCell = true;
		setBackground(Color.black);
	}
	
	/**
	 * Initialises variables and the cells layout.
	 * @param character - The correct character.
	 */
	private void init(char character) {
		
		this.character = character;
		enteredCharacter = 0;
		groups = new ArrayList<CellGroup>(0);
		highlighted = false;
		doubleCell = false;
		clickCount = 0;
		setBorder(BorderFactory.createLineBorder(Color.white));
		addListeners();
		
	}
	
	/**
	 * Tests to see whether the character is correct.
	 * @return true if the character is correct. False otherwise.
	 */
	public boolean isCorrect() {
		if (character == enteredCharacter)
			return true;
		else
			return false;
	}
	
	/**
	 * Sets this cell as a double cell. This means it is in two clue groups and is both across and down.
	 */
	public void setAsDoubleCell() {
		doubleCell = true;
	}
	
	/**
	 * Adds a group to the cell.
	 * @param group - The group this cell is in.
	 */
	public void addGroup(CellGroup group) {
		groups.add(group);
		currentGroup = group;
	}
	
	/**
	 * Gets the group this cell is in.
	 * @param index - The index of the group. A double cell will have two groups.
	 * @return the cell group
	 */
	public CellGroup getGroup(int index) {
		return groups.get(index);
	}
	
	/**
	 * This method is called only by a double cell. It changes the group it will be corresponding to.
	 */
	private void changeGroup() {
		
		if (currentGroup.equals(groups.get(0)))
			currentGroup = groups.get(1);
		else
			currentGroup = groups.get(0);
		
	}
	
	/**
	 * This method is called only by a double cell. Gets the other group the cell is in.
	 * @return the other group
	 */
	private CellGroup getOtherGroup() {
		
		if (currentGroup.equals(groups.get(0)))
			return groups.get(1);
		else
			return groups.get(0);
		
	}
	
	/**
	 * Highlights the current group of cells.
	 */
	private void highlight() {
		
		if (doubleCell) {
			clickCount++;
			if (clickCount >= 2)
				changeGroup();
		} else
			clickCount = 0;
		
		currentGroup.highlight();
		
	}
	
	/**
	 * The cell that calls this method claims the focus of the crossword.
	 */
	public void focus() {
		
		currentGroup.focusedCellNum = currentGroup.cells.indexOf(this);
		setBorder(BorderFactory.createLineBorder(focusBorderColor, 2));
		focused = true;
		requestFocus();
		
	}
	
	/**
	 * Sets the character entered by the user into this cell.
	 * @param enteredCharacter - The character entered.
	 */
	public void setChar(char enteredCharacter) {
		this.enteredCharacter = String.valueOf(enteredCharacter).toLowerCase().charAt(0);
		repaint();
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if (!solidCell) { // Adds a linear gradient colour for effect to normal cells.
			Graphics2D g2d = (Graphics2D) g;
			GradientPaint gp = new GradientPaint(0, 0, Color.white, 0, getHeight(), new Color(240, 240, 240));
			g2d.setPaint(gp);
			g2d.fillRect(0, 0, getWidth(), getHeight());
		}
		
		if (highlighted) { // Adds a linear gradient colour for effect to highlighted cells.
			Graphics2D g2d = (Graphics2D) g;
			GradientPaint gp = new GradientPaint(0, 0, new Color(255, 255, 150), 0, getHeight(), new Color(255, 219, 100));
			g2d.setPaint(gp);
			g2d.fillRect(0, 0, getWidth(), getHeight());
		}
        
		g.setColor(Color.BLACK);
		g.setFont(new Font(null, Font.BOLD, 14));
		g.drawString(String.valueOf(enteredCharacter).toUpperCase(), 12, 23);
	    
	}
	
	/**
	 * Adds mouse and key listeners to the cell.
	 */
	private void addListeners() {
		
		addMouseListener(new MouseAdapter() {
			
			@Override
			public void mousePressed(MouseEvent e) {
				highlight();
				focus();
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				if (!focused)
					setBorder(BorderFactory.createLineBorder(hoverBorderColor, 2));
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				if (!focused)
					setBorder(BorderFactory.createLineBorder(Color.white));
			}
			
		});
		
		addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyPressed(KeyEvent e) {
				
				char c = e.getKeyChar();
				int k = e.getKeyCode();
				
				if (Character.isLetter(c) || c == KeyEvent.VK_MINUS || c == KeyEvent.VK_SPACE) {
					setChar(c);
					currentGroup.testAnswer();
					if (doubleCell == true)
						getOtherGroup().testAnswer();
					currentGroup.moveToNextCell();
				} else if (c == KeyEvent.VK_BACK_SPACE) {
					enteredCharacter = 0;
					currentGroup.testAnswer();
					if (doubleCell == true)
						getOtherGroup().testAnswer();
					currentGroup.moveToPreviousCell();
					repaint();
				} else if (c == KeyEvent.VK_DELETE) {
					enteredCharacter = 0;
					currentGroup.testAnswer();
					if (doubleCell == true)
						getOtherGroup().testAnswer();
					repaint();
				} else if (currentGroup.across && k == KeyEvent.VK_LEFT) // Arrow key listeners for navigation
					currentGroup.moveToPreviousCell();
				else if (currentGroup.across && k == KeyEvent.VK_RIGHT)
					currentGroup.moveToNextCell();
				else if (!currentGroup.across && k == KeyEvent.VK_UP)
					currentGroup.moveToPreviousCell();
				else if (!currentGroup.across && k == KeyEvent.VK_DOWN)
					currentGroup.moveToNextCell();
				else if (currentGroup.across && k == KeyEvent.VK_UP) {
					currentGroup.moveToPreviousCell();
					highlight();
					focus();
				} else if (currentGroup.across && k == KeyEvent.VK_DOWN) {
					currentGroup.moveToNextCell();
					highlight();
					focus();
				} else if (!currentGroup.across && k == KeyEvent.VK_LEFT) {
					currentGroup.moveToPreviousCell();
					highlight();
					focus();
				} else if (!currentGroup.across && k == KeyEvent.VK_RIGHT) {
					currentGroup.moveToNextCell();
					highlight();
					focus();
				}
				
			}
			
		});
		
	}
	
}
