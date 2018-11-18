import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.*;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ChatPane extends JPanel {

  	private Connection connection;

  	static Color bgcolor = new Color(50,50,50);
	static Color fgcolor = new Color(255,255,255);

	JTextArea chat;
	JTextField t_input;

	public ChatPane(Connection connection) {
	  	this.connection = connection;
		this.connection.connect();

	  	this.setOpaque(false);
	  	GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints con = new GridBagConstraints();

		con.fill = GridBagConstraints.BOTH;
		this.setLayout(gridbag);

		// Chat view
		chat = new JTextArea();
		chat.setEditable(false);
		chat.setLineWrap(true);
		chat.setWrapStyleWord(true);
		chat.setBackground(bgcolor);
		chat.setForeground(fgcolor);
		JScrollPane chatscroll = new JScrollPane(chat);
		con.gridx = 0;
		con.gridy = 0;
		con.weightx = 1;
		con.weighty = 1;
		this.add(chatscroll, con);

		// Text input field
		t_input = new JTextField("", 20);
		t_input.setBackground(bgcolor);
		t_input.setForeground(fgcolor);
		t_input.setCaretColor(fgcolor);
		t_input.addActionListener(new ActionListener() {
			 public void actionPerformed(ActionEvent e) {
//				System.out.println(t_input.getText());//DEBUG
				addMessage(connection.nick,t_input.getText());
				connection.send(t_input.getText());
				t_input.setText("");
				chat.setCaretPosition(chat.getDocument().getLength());
			 }
			 });
		con.gridx = 0;
		con.gridy = 1;
		con.weightx = 0;
		con.weighty = 0;
		this.add(t_input, con);

		/*
		String in;
		try {
		  	while (this.connection.rec != null) {
				while ((in = this.connection.rec.readLine()) != null) {
					chat.append(in);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/
	}

	public void msgIn(String msg) {
	  	String[] msgparsed = Connection.parseMessage(msg);
		chat.append("\n"+Connection.getDateTime());
		for (String s : msgparsed) {
			chat.append(" "+s);
		}
		chat.setCaretPosition(chat.getDocument().getLength());
	}

	public void addMessage(String prepend, String usr, String msg) {
		chat.append("["+prepend+"]"+usr+": "+msg);
	}

	public void addMessage(String usr, String msg) {
	  	if (chat.getText().length() != 0) {
			chat.append("\n"+usr+": "+msg);
		} else {
			chat.append(usr+": "+msg);
		}
	}

}
