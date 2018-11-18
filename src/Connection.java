import java.net.Socket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;

public class Connection {
	public String host;
	public int port;
	public String nick;
	public String realname;

	Socket soc;
	PrintWriter out;
	BufferedReader rec;

	private static String MODE_NONE = "0";
	private static String MODE_WALLOPS = "4";
	private static String MODE_INVISIBLE = "8";

	private static final String CMD_QUIT = "QUIT";
	private static final String CMD_JOIN = "JOIN";
	private static final String CMD_NICK = "NICK";
	private static final String CMD_USER = "USER";
	private static final String CMD_PART = "PART";
	private static final String CMD_MODE = "MODE";
	private static final String CMD_NAMES = "NAMES";
	private static final String CMD_PRIVMSG = "PRIVMSG";
	private static final String CMD_NOTICE = "NOTICE";
	private static final String CMD_TIME = "TIME";

	private static final String RPL_BOUNCE = "005";
	private static final String RPL_AWAY = "301";
	private static final String RPL_NOTOPIC = "331";
	private static final String RPL_TOPIC = "332";
	private static final String RPL_NAMREPLY = "353";
	private static final String RPL_ENDOFNAMES = "366";
	private static final String RPL_MOTDSTART = "375";
	private static final String RPL_MOTD = "372";
	private static final String RPL_ENDOFMOTD = "376";
	private static final String RPL_TIME = "391";

	private static final String ERR_NOMOTD = "422";

	public Connection(String host, int port, String nick, String realname) {
		this.host = host;
		this.port = port;
		this.nick = nick;
		this.realname = realname;
	}

	public Connection(String host, String nick, String realname) {
		this.host = host;
		this.port = 6667;
		this.nick = nick;
		this.realname = realname;
	}

	public void connect() {
	  	try {
		  	soc = new Socket(host,port);
			rec = new BufferedReader(new InputStreamReader(soc.getInputStream()));
			out = new PrintWriter(soc.getOutputStream(), true);
			// PASS <password>	optional
			// NICK <nickname>
			send(CMD_NICK,nick);
			// USER <user> <mode> * :<realname>
			send(CMD_USER, nick+" "+MODE_NONE+" *", realname);
	
		  	// JOIN <channel>
			//send(CMD_JOIN,"##programming");
			String in;
			/*
			while ((in = rec.readLine()) != null) {
				System.out.println(in);
			}
			*/
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void disconnect() {
	  	send(CMD_QUIT+" :Goodbye");
		try {
			soc.close();
		} catch(Exception e) {
			System.err.println("oops");
			e.printStackTrace();
		}
	}

	private void keepalive(String ping) {
		String pong = ping.replaceFirst("I","O");
		send(pong);
	}

	public void send(String message) {
	  	//System.out.println("\t|| Sending ["+message+"]");
	  	out.println(message);
	}

	private void send(String cmd, String msg) {
		send(cmd+" "+msg);
	}

	private void send(String cmd, String param, String msg) {
		send(cmd+" "+param+" :"+msg);
	}

	public static String[] parseMessage(String message) {
		boolean hasPrefix = false;

		// messages with a prefix start with a colon
		if (message.startsWith(":")) {
			hasPrefix = true;
		}
		String[] msg;
		if (hasPrefix) {
			msg = message.split(" ",4);
		} else {
			String[] temp = message.split(" ",3);
			msg = new String[4];
			msg[0] = ""; // prefix
			msg[1] = temp[0]; // command
			msg[2] = temp[1]; // parameters
		}
		return msg;
	}

	public static String getTime() {
	  	String dt = ZonedDateTime.now().toString();
		String time = dt.substring(dt.indexOf("T")+1, dt.indexOf("."));
		return time;
	}

	public static String getDate() {
		String dt = ZonedDateTime.now().toString();
		String date = dt.substring(0, dt.indexOf("T"));
		return date;
	}

	public static String getDateTime() {
		return getDate()+" "+getTime();
	}

	public static void main(String[] args) {
		Connection c = new Connection("chat.freenode.net",6667,"irctest","Kat");
		c.connect();
	}
}
