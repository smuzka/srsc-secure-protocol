

import java.net.InetAddress;
import java.net.UnknownHostException;

import SHP.SHPClient;

class UseException extends Exception {
	public UseException() {
		super();
	}

	public UseException(String s) {
		super(s);
	}
}

public class TFTPClient {
	public static void main(String argv[]) throws TftpException, UseException {
		String host = "";
		String fileName = "";
		String mode="octet"; //default mode
		String type="";
		try {
			// Process command line
			if (argv.length == 0)
				throw new UseException("--Usage-- \nocter mode:  TFTPClient [host] [Type(R/W?)] [filename] \nother mode:  TFTPClient [host] [Type(R/W?)] [filename] [mode]" );
			//use default mode(octet)
			if(argv.length == 5){
				host =argv[2];
			    type = argv[3];
			    fileName = argv[4];}
			//use other modes
			else if(argv.length == 6){
				host = argv[0];
				mode =argv[5];
				type = argv[3];
				fileName = argv[4];
			}
			else throw new UseException("wrong command. \n--Usage-- \nocter mode:  TFTPClient [host] [Type(R/W?)] [filename] \nother mode:  TFTPClient [host] [Type(R/W?)] [filename] [mode]");
			
			
			InetAddress server = InetAddress.getByName(host);

			SHPClient.initConnection(
					"../../../srscProject/src/main/resources/",
					"127.0.0.1",
					12345,
					9999,
					argv[0],
					argv[1],
					argv[4]
			);

			//process read request
			if(type.matches("R")){
				TFTPclientRRQ r = new TFTPclientRRQ(server, fileName, mode);}
			//process write request
			else if(type.matches("W")){
				TFTPclientWRQ w = new TFTPclientWRQ(server, fileName, mode);
			}
			else{throw new UseException("wrong command. \n--Usage-- \nocter mode:  TFTPClient [host] [Type(R/W?)] [filename] \nother mode:  TFTPClient [host] [Type(R/W?)] [filename] [mode]");}
			
		} catch (UnknownHostException e) {
			System.out.println("Unknown host " + host);
		} catch (UseException e) {
			System.out.println(e.getMessage());
		}
	}
}