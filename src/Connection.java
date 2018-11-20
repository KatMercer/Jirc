import java.net.Socket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;

/** 
 * An IRC communication socket
 **/
public class Connection {
  	/** Hostname of the server */
	public String host;
	/** Port number of the server */
	public int port;
	/** Nickname of the user */
	public String nick;
	/** Real name of the user */
	public String realname;
	/** Channel the user is in */
	public String channel;

	/** Socket connection to the server */
	Socket soc;
	/** Writer to send messages to the server */
	PrintWriter out;
	/** Reader to read messages received from the server */
	BufferedReader rec;

	/* User modes */
	public static String MODE_NONE = "0";
	public static String MODE_WALLOPS = "4";
	public static String MODE_INVISIBLE = "8";

	/* Command codes */
	public static final String CMD_QUIT = "QUIT";
	public static final String CMD_JOIN = "JOIN";
	public static final String CMD_NICK = "NICK";
	public static final String CMD_USER = "USER";
	public static final String CMD_PART = "PART";
	public static final String CMD_MODE = "MODE";
	public static final String CMD_NAMES = "NAMES";
	public static final String CMD_PRIVMSG = "PRIVMSG";
	public static final String CMD_NOTICE = "NOTICE";
	public static final String CMD_TIME = "TIME";
	public static final String CMD_PING = "PING";

	/* Reply codes */
	public static final String RPL_BOUNCE = "005";
	public static final String RPL_AWAY = "301";
	public static final String RPL_NOTOPIC = "331";
	public static final String RPL_TOPIC = "332";
	public static final String RPL_NAMREPLY = "353";
	public static final String RPL_ENDOFNAMES = "366";
	public static final String RPL_MOTDSTART = "375";
	public static final String RPL_MOTD = "372";
	public static final String RPL_ENDOFMOTD = "376";
	public static final String RPL_TIME = "391";

	/* Error codes */
	public static final String ERR_NOMOTD = "422";

	/**
	 * Constructor for an IRC connection
	 * @param host the hostname of the server
	 * @param port the port number
	 * @param nick nickname of the user
	 * @param realname the user's real name
	 **/
	public Connection(String host, int port, String nick, String realname) {
		this.host = host;
		this.port = port;
		this.nick = nick;
		this.realname = realname;
		this.channel = "";
	}

	/**
	 * Constructor for an IRC connection
	 * The default port number 6667 is used
	 * @param host the hostname of the server
	 * @param nick nickname of the user
	 * @param realname the user's real name
	 **/
	public Connection(String host, String nick, String realname) {
		this.host = host;
		this.port = 6667;
		this.nick = nick;
		this.realname = realname;
		this.channel = "";
	}

	/**
	 * Connects to the host
	 **/
	public void connect() {
	  	try {
		  	soc = new Socket(host,port);
			rec = new BufferedReader(new InputStreamReader(soc.getInputStream()));
			out = new PrintWriter(soc.getOutputStream(), true);
			// PASS <password>	optional
			// NICK <nickname>
			send("/"+CMD_NICK+" "+nick);
			// USER <user> <mode> * :<realname>
			send("/"+CMD_USER+" "+nick+" "+MODE_NONE+" * :"+realname);
	
		  	// JOIN <channel>
			//send(CMD_JOIN,"##programming");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Disconnects from the host by quitting and closing the socket
	 **/
	public void disconnect() {
	  	send("/"+CMD_QUIT+" :Goodbye");
		try {
			soc.close();
		} catch(Exception e) {
			System.err.println("oops");
			e.printStackTrace();
		}
	}

	/**
	 * Sends a message to the server to prove we're still connected
	 * @param ping the PING message received from the server
	 **/
	public void keepalive(String ping) {
		String pong = ping.replaceFirst("I","O");
		send("/"+pong);
	}

	/**
	 * Sends a message to the server. 
	 * Messages beginning with a forward slash are interpreted as containing a command, otherwise it is sent as a chat message.
	 * @param message the message to send
	 **/
	public void send(String message) {
	  	//System.out.println("\t|| Sending ["+message+"]");
		message = message.trim();
		if (message.startsWith("/")) {
			if (message.substring(1).toUpperCase().startsWith(CMD_JOIN)) {
				channel = message.substring(1).split(" ")[1];
			} else if (message.substring(1).toUpperCase().startsWith(CMD_PART)) {
				String msg = message.substring(1).trim();
				if (msg.toUpperCase().equals(CMD_PART)) {
					message = "/"+CMD_PART+" "+channel;
				}
			}
			out.println(message.substring(1));
			//System.out.println(message.substring(1));//DEBUG
		} else {
			out.println(CMD_PRIVMSG+" "+channel+" :"+message);
			//System.out.println(CMD_PRIVMSG+" "+channel+" :"+message);//DEBUG
		}
	}

	/**
	 * Parses a message into it's constituent parts. 
	 * @param message the message to parse
	 * @return an array representing the different parts of the message
	 **/
	public static String[] parseMessage(String message) {
		boolean hasPrefix = false;

		// messages with a prefix start with a colon
		if (message.startsWith(":")) {
			hasPrefix = true;
		}
		String[] msg;
		msg = message.split(" ");

		int startat = 0;
		if (hasPrefix) {
			startat = 1;
		} /*else {
			String[] temp = message.split(" ",3);
			msg = new String[4];
			msg[0] = ""; // prefix
			msg[1] = temp[0]; // command
			msg[2] = temp[1]; // parameters
		}*/
		for (int i = startat; i < msg.length; i++) {
			if (msg[i].startsWith(":")) {
				for (int j = i+1; j < msg.length; j++) {
					msg[i] += " "+msg[j];
				}
				String[] tmp = new String[i+1];
				for (int j = 0; j < tmp.length; j++) {
					tmp[j] = msg[j];
				}
				msg = tmp;
				break;
			}
		}
		return msg;
	}

	/**
	 * Gets the current time as reported by the client system
	 * @return the current time
	 **/
	public static String getTime() {
	  	String dt = ZonedDateTime.now().toString();
		String time = "";
		try {
			time = dt.substring(dt.indexOf("T")+1, dt.indexOf("."));
		} catch (StringIndexOutOfBoundsException e) {
		  	System.err.println("Error getting ZonedDateTime.now");
			e.printStackTrace();
		}
		return time;
	}

	/**
	 * Gets the current date as reported by the client system
	 * @return the current date
	 **/
	public static String getDate() {
		String dt = ZonedDateTime.now().toString();
		String date = dt.substring(0, dt.indexOf("T"));
		return date;
	}

	/**
	 * Gets the current date and time as reported by the client system
	 * @return the current date and time
	 **/
	public static String getDateTime() {
		return getDate()+" "+getTime();
	}
}
