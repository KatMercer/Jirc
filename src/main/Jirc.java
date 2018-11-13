import java.io.*;
import java.net.*;
import java.time.*;
import java.util.*;

public class Jirc {

		Socket soc;
		PrintWriter out; // write to server
		BufferedReader in; // read from server
		BufferedReader stdin; // read from cli

	  	// codes based on IRC specification
	  	static final String JOIN = "JOIN";
	  	static final String QUIT = "QUIT";
	  	static final String PRIVMSG = "PRIVMSG";
	  	static final String NAMEREQ = "333";
	  	static final String NAMELIST = "353";
	  	static final String NAMEEND = "366";
	  	static final String MOTDREQ = "375";
	  	static final String MOTD = "372";
	  	static final String MOTDEND = "376";

	public Jirc() {

		String hostname = "chat.freenode.net";
		int portNumber = 6667;
		String nick = "irctest";
		String channel = "##programming";//or ##chat
		String message = "test";
		ArrayList<String> mutelist = new ArrayList<String>();

		// codes to avoid printing
		mutelist.add(NAMEREQ); // names list request
		mutelist.add(NAMELIST); // names list
		mutelist.add(NAMEEND); // end names list
		mutelist.add(MOTDREQ); // motd request
//		mutelist.add(MOTD); // motd reply
		mutelist.add(MOTDEND); // motd end
//		mutelist.add(QUIT); // quit messages
//		mutelist.add(JOIN); // join messages
		// end muting codes


		System.out.println("|| Connecting to "+hostname+":"+portNumber);
		try {
		  	// connection
			soc = new Socket(hostname, portNumber);
			System.out.println("|| Connected to "+hostname+":"+portNumber);
			// to server
			out = new PrintWriter(soc.getOutputStream(), true);
			System.out.println("|| Opened write");
			// from server
			in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
			System.out.println("|| Opened read");
			// input from cli
			stdin = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("|| Opened cli reader");
			//System.out.print("<< ");
			/*
			while ((userInput = stdin.readLine()) != null) {
				out.println(userInput);
				System.out.println(">> " + in.readLine());
				System.out.print("<< ");
			}
			*/
			System.out.println("|| sending nick");
		  	send(out, "NICK "+nick);
			System.out.println("|| sending user");
			send(out, "USER "+nick+" i * :Kat");
			System.out.println("|| joining "+channel);
			send(out, "JOIN "+channel);
			
			String sender = "";
			String receive = "";
			while (true) {
				receive = in.readLine();
				if (receive == null) {
					break;
				}
				// serv, command, msg
				String[] resp = receive.split(" ",3);
//				System.out.println(">>"+receive);
				String origin = resp[0];
				String code = resp[1];
				String msg = "";
				String chan = "";
				if (resp.length > 2) {
					msg = resp[2];
				}

				if (!mutelist.contains(code)) { // only show what we want

					String time = Instant.now().toString();
					System.out.print(time.substring(time.indexOf("T")+1, time.indexOf("."))+" | "); // timestamp everything

					if (origin.contains("!~")) {
						sender = origin.substring(1,origin.indexOf("!"));
						try {
						chan = msg.substring(0,msg.indexOf(":")-1);
						} catch (StringIndexOutOfBoundsException e) {
							chan = "";
						}
						msg = msg.substring(msg.indexOf(":")+1);
						//System.out.println("|| message from "+sender);
					} else {
						sender = "";
					}

					if (code.equals(PRIVMSG)) {
					  	System.out.println(sender+"@"+chan+": "+msg);//DEBUG test
//						System.out.print("UM  ++ "+sender); // show who is sending the message
					} else if (code.equals(JOIN)) {
					  	System.out.println(sender+" joined "+msg);
					} else if (code.equals(QUIT)) {
					  	System.out.println(sender+" quit "+msg);
					} else if (code.equals(MOTD)) {
					  	System.out.println(msg);
					} else if (origin.equals("PING")) { // keepalive
					  	System.out.println(receive);
					  	String pong = receive.replaceFirst("I","O");
						out.println(pong);
						System.out.println(pong);
					} else {
					  	System.out.println(receive);//DEBUG
//						System.out.print("NUM ++ "+resp[0]+" || "+resp[1]); // show any message not from users
					}
//					if (resp.length > 2) {
//						System.out.println(" || "+resp[2]); // contents of the message
//					} else {
//						System.out.print("\n");
//					}
				} else {
//					System.out.println("|| code "+resp[1]+" muted");//DEBUG
				}
//				if (resp[1].equals("366")) {
//					lock = false;
//				} else if (resp[1].equals("333")) {
//					lock = true;
//				}
			}

			out.close();
			in.close();
			soc.close();
			
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host "+hostname);
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to "+hostname);
			System.exit(1);
		}
	}

	/*
	public void start() {
		stdin = new BufferedReader(InputStreamReader(System.in));
		out = new PrintWriter(soc.getOutputStream(), true);
	}
	*/

	public void send(PrintWriter out, String message) {
		out.println(message);
	}
	
  	public static void main(String[] args) {
//		Jirc client = new Jirc(args);
		Jirc client = new Jirc();
	}

}
