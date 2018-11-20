import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.*;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.*;

/** ChatPane is a panel for inputing and reading text from an IRC connection
 *  @see Connection
 **/
public class ChatPane extends JPanel {

  	private Connection connection;

	/** Background color of the elements */
  	static Color bgcolor = new Color(50,50,50);
	/** Text color */
	static Color fgcolor = new Color(255,255,255);
	/** Font family to use */
	static String fontfam = "Monospace";
	/** Font size for all text */
	static int fontsize = 16;

	/** Pane containing all the chat messages */
	JEditorPane chat;
	/** Attributes for styling the text */
	SimpleAttributeSet attrib;
	/** Input text box */
	JTextField t_input;

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
		con.gridx = 0;
		con.gridy = 1;
		con.weightx = 0;
		con.weighty = 0;
		this.add(t_input, con);
	}

	/**
	 * Returns the nick of the user represented in a string
	 * @param msg the message containing the nick
	 * @return the nick OR null if none was found
	 **/
	public String extractUser(String msg) {
		String user = null;

		try {
			user = msg.substring(1,msg.indexOf("!")-1); // extract user nick
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
		int startat = 0;
		if (msgparsed[0].equals(Connection.CMD_PING)) { // PING message we need to respond to
			connection.keepalive(msg);
		} else if (msgparsed[1].equals(Connection.CMD_PRIVMSG)) { // message in the channel
			String u = extractUser(msgparsed[0]); // extract user nick
			if (u != null) {
				incoming += u; // user nick
				msgparsed[2] = "\u2502"; // separator
			}
			msgparsed[3] = msgparsed[3].substring(1);
			startat = 2;
		} else if (msgparsed[1].equals(Connection.CMD_QUIT)) { // a user has quit
			incoming += extractUser(msgparsed[0])+" has quit ("+msgparsed[msgparsed.length-1].substring(1)+")";
			startat = 100;
		} else if (msgparsed[1].equals(Connection.CMD_PART)) { // a user has left
		  	incoming += extractUser(msgparsed[0])+" left ("+msgparsed[msgparsed.length-1].substring(1)+")";
		} else if (msgparsed[1].equals(Connection.CMD_JOIN)) { // a user has joined
			incoming += extractUser(msgparsed[0])+" joined";
			startat = 100;
		}
		for (int i = startat; i < msgparsed.length; i++) { // consolidate the constituent parts of the message
		  	if (msgparsed[i] != "" || msgparsed[i] != null) {
				incoming += " "+msgparsed[i];
			}
		}
		append(incoming); // show message
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
		try {
		  	doc.insertString(doc.getLength(),"\n"+Connection.getTime()+" \u2502 ",null);
			doc.insertString(doc.getLength(),msg,null);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		chat.setCaretPosition(chat.getDocument().getLength()); // scroll window down as messages come in
	}

}
