package gfx;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.*;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.*;
import net.*;

/** ChatPane is a panel for inputing and reading text from an IRC connection
 *  @see Connection
 **/
public class ChatPane extends JPanel {

  	private Connection connection;

	/** Background color of the elements */
  	static Color bgcolor = new Color(0,0,0);
	/** Text color */
	static Color fgcolor = new Color(255,255,255);
	/** Font family to use */
	static String fontfam = "Monospace";
	/** Font size for all text */
	static int fontsize = 12;
	private boolean linecolored = false;

	/** Pane containing all the chat messages */
	private JEditorPane chat;
	//JList chat;
	/** Attributes for styling the text */
	private SimpleAttributeSet attrib;
	/** Input text box */
	public JTextField t_input;
	/** Nick display */
	public JLabel l_nick;

	/**
	 * Constructor for a ChatPane communicating over a Connection
	 * @param connection the connection to use
	 **/
	public ChatPane(Connection connection) {
	  	this.connection = connection;
		this.connection.connect();

	  	this.setOpaque(false);
	  	GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints con = new GridBagConstraints();

		con.fill = GridBagConstraints.BOTH;
		this.setLayout(gridbag);

		/* Chat view */
		chat = new JEditorPane("text/html","Welcome to Jirc!\n\n");
		// make hanging indent
		StyledDocument doc = (StyledDocument)chat.getDocument();
		attrib = new SimpleAttributeSet();
		StyleConstants.setFirstLineIndent(attrib, -(fontsize*10*0.7f));
		StyleConstants.setLeftIndent(attrib, (fontsize*10*0.7f));
		// edit other attributes 
		StyleConstants.setBackground(attrib, bgcolor);
		StyleConstants.setForeground(attrib, fgcolor);
		StyleConstants.setFontFamily(attrib, fontfam);
		StyleConstants.setFontSize(attrib, fontsize);
		doc.setParagraphAttributes(0, doc.getLength(), attrib, false);

		chat.setEditable(false);
		chat.setBackground(bgcolor); //didn't think this was needed but it definitely is
		// scroll pane so we can see chat history
		JScrollPane chatscroll = new JScrollPane(chat);
		con.gridx = 0;
		con.gridy = 0;
		con.gridwidth = 2;
		con.weightx = 1;
		con.weighty = 1;
		this.add(chatscroll, con);

		/* Text input field */
		t_input = new JTextField("", 20);
		t_input.setBackground(bgcolor);
		t_input.setForeground(fgcolor);
		t_input.setCaretColor(fgcolor);
		t_input.setFont(new Font(fontfam, Font.PLAIN, fontsize));
		t_input.addActionListener(new ActionListener() {
			 public void actionPerformed(ActionEvent e) {
				msgOut(t_input.getText());
				t_input.setText("");
			 }
			 });
		con.gridx = 1;
		con.gridy = 1;
		con.gridwidth = 1;
		con.weightx = 0;
		con.weighty = 0;
		this.add(t_input, con);

		/* Nick display */
		l_nick = new JLabel(connection.nick);
		l_nick.setBackground(bgcolor);
		l_nick.setForeground(fgcolor);
		l_nick.setFont(new Font(fontfam, Font.PLAIN, fontsize));
		con.gridx = 0;
		con.gridy = 1;
		con.insets = new Insets(0, fontsize/2, 0, fontsize/2);
		con.weightx = 0;
		con.weighty = 0;
		this.add(l_nick, con);
	}

	/**
	 * Returns the nick of the user represented in a string
	 * @param msg the message containing the nick
	 * @return the nick OR null if none was found
	 **/
	public String extractUser(String msg) {
		String user = null;

		try {
			user = msg.substring(1,msg.indexOf("!")); // extract user nick
		} catch (StringIndexOutOfBoundsException e) {
		  	user = null;
		}
		return user;
	}

	/**
	 * Incoming messages are parsed and operated on before being displayed
	 * @param msg the incoming message
	 **/
	public void msgIn(String msg) {
	  	String[] msgparsed = Connection.parseMessage(msg);
		String incoming = "";
		StyleContext style = StyleContext.getDefaultStyleContext();
		int startat = 0;
		if (msgparsed[0].equals(Connection.CMD_PING)) { // PING message we need to respond to
			connection.keepalive(msg);
		} else {
		  	if (msgparsed[1].equals(Connection.CMD_PRIVMSG)) { // message in the channel
				String u = extractUser(msgparsed[0]); // extract user nick
				if (u != null) {
					incoming += u; // user nick
					msgparsed[2] = "\u2502"; // separator
				}
				msgparsed[3] = msgparsed[3].substring(1);
				startat = 2;
				StyleConstants.setForeground(attrib, fgcolor);
			} else if (msgparsed[1].equals(Connection.CMD_QUIT)) { // a user has quit
				incoming += extractUser(msgparsed[0])+" has quit ("+msgparsed[msgparsed.length-1].substring(1)+")";
				startat = 100;
				StyleConstants.setForeground(attrib, new Color(235, 185, 70));
			} else if (msgparsed[1].equals(Connection.CMD_PART)) { // a user has left
			  	incoming += extractUser(msgparsed[0])+" left ("+msgparsed[msgparsed.length-1].substring(1)+")";
				StyleConstants.setForeground(attrib, new Color(235, 185, 70));
			} else if (msgparsed[1].equals(Connection.CMD_JOIN)) { // a user has joined
				incoming += extractUser(msgparsed[0])+" joined";
				startat = 100;
				StyleConstants.setForeground(attrib, new Color(235, 185, 70));
			} else if (msgparsed[1].equals(Connection.CMD_NOTICE) ||
				 msgparsed[1].equals(Connection.RPL_MOTD) ||
				 msgparsed[1].equals(Connection.RPL_MOTDSTART) ||
				 msgparsed[1].equals(Connection.RPL_ENDOFMOTD) ||
				 msgparsed[1].equals(Connection.RPL_TOPIC) ||
				 msgparsed[1].equals(Connection.RPL_NAMEREPLY) ||
				 msgparsed[1].equals(Connection.RPL_ENDOFNAMES) ||
				 msgparsed[1].equals(Connection.CMD_MODE) ||
				 msgparsed[1].equals(Connection.RPL_WELCOME) ||
				 msgparsed[1].equals(Connection.RPL_YOURHOST) ||
				 msgparsed[1].equals(Connection.RPL_CREATED) ||
				 msgparsed[1].equals(Connection.RPL_MYINFO)) {
				StyleConstants.setForeground(attrib, new Color(70, 185, 235));
			} else {
				StyleConstants.setForeground(attrib, fgcolor);
			}
			for (int i = startat; i < msgparsed.length; i++) { // consolidate the constituent parts of the message
			  	if (msgparsed[i] != "" || msgparsed[i] != null) {
					incoming += " "+msgparsed[i];
				}
			}
			append(incoming); // show message
		}
	}

	/**
	 * Outgoing messages are sent and modified before being displayed
	 * @param msg the outgoing message
	 **/
	public void msgOut(String msg) {
		connection.send(msg);

	  	String outgoing = "";
		if (msg.startsWith("/")) { // message is a command
			outgoing += msg;
		} else {
			outgoing += connection.nick+" \u2502 "+msg;
		}
		append(outgoing);
	}

	/**
	 * Append a message to the chat view
	 * @param msg the message to append
	 **/
	public void append(String msg) {
	  	Document doc = chat.getDocument();
		linecolored = !linecolored;
		Color col = new Color(255,255,255);
		StyleContext style = StyleContext.getDefaultStyleContext();
		style.addAttribute(attrib, StyleConstants.Background, col);
		try {
			if (linecolored) {
		  		int r = bgcolor.getRed();
				int g = bgcolor.getGreen();
				int b = bgcolor.getBlue();
				int uo = 30; // uniform offset for the color
				col = new Color(r+uo,g+uo,b+uo);
			} else {
			  	col = bgcolor;
			}
			StyleConstants.setBackground(attrib, col);
			if (msg.contains(connection.nick)) {
				StyleConstants.setBackground(attrib, new Color(100, 0, 0));
			} else {
				StyleConstants.setBackground(attrib, col);
			}

		  	doc.insertString(doc.getLength(),"\n"+Connection.getTime()+" \u2502 ",null);
			doc.insertString(doc.getLength(),msg,null);
			((StyledDocument)doc).setCharacterAttributes(doc.getText(0,doc.getLength()).lastIndexOf("\n"), doc.getLength(), attrib, false);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		chat.setCaretPosition(chat.getDocument().getLength()); // scroll window down as messages come in
	}

}
