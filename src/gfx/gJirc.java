import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JButton;

public class gJirc extends JFrame {

  	Connection connection;
  	ChatPane chatpane;

	String host;
	int port;
	String nick;
	String realname;

  	public gJirc() {
	  	this.host = "chat.freenode.net";//DEBUG
		this.port = 6667;//DEBUG
	  	this.nick = "irctest";//DEBUG
		this.realname = "Kat";//DEBUG

		this.setTitle("JIRC");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.getContentPane().setBackground(Color.BLACK);
		this.getContentPane().setLayout(new BorderLayout());

		connection = new Connection(host,port,nick,realname);


		chatpane = new ChatPane(connection);
		this.add(chatpane, BorderLayout.CENTER);

		this.setSize(500,500);
		this.setMinimumSize(new Dimension(100,100));
//		this.pack();
		this.setVisible(true);
	}

	public void run() {
	  	//connection.connect();
		String in;
		try {
		  	while ((in = connection.rec.readLine()) != null) {
				chatpane.msgIn(in);
//				System.out.println(in);//DEBUG
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

  	public static void main(String[] args) {
		//System.out.println("sorry, nothing.");//DEBUG
		gJirc app = new gJirc();
		app.run();
	}
  	
}
