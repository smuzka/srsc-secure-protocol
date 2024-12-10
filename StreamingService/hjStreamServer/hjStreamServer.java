/*
* hjStreamServer.java 
* Streaming server: emitter of video streams (movies)
* Can send in unicast or multicast IP for client listeners
* that can play in real time the transmitted movies
*/

import java.io.*;
import java.net.*;
import java.util.Arrays;

import DSTP.EncryptedDatagramPacket;
import DSTP.EncryptedDatagramSocket;
import SHP.ConnectionResult;
import SHP.SHPServer;

class hjStreamServer {

	static public void main( String []args ) throws Exception {

		ConnectionResult connectionResult = SHPServer.initConnection(
				"srscProject/src/main/resources/",
				12345
		);

		int size;
		int count = 0;
 		long time;
        DataInputStream g = new DataInputStream( new FileInputStream("StreamingService/hjStreamServer/movies/" + connectionResult.fileName()) );
		byte[] buff = null;
		EncryptedDatagramSocket s = new EncryptedDatagramSocket();
		InetSocketAddress addr =
		    new InetSocketAddress("127.0.0.1",connectionResult.serverPort());
		long t0 = System.nanoTime(); // tempo de referencia
		long q0 = 0;

		while ( g.available() > 0 ) {
			size = g.readShort();
			time = g.readLong();
			if ( count == 0 ) q0 = time; // tempo de referencia no stream
			count += 1;
			buff = new byte[size];
			g.readFully(buff, 0, size );
			EncryptedDatagramPacket p=new EncryptedDatagramPacket(buff,buff.length,addr);
			p.setData(buff, 0, size );
			p.setSocketAddress( addr );
			long t = System.nanoTime();
			Thread.sleep( Math.max(0, ((time-q0)-(t-t0))/1000000) );
			s.send( p );
			//System.out.print( "." );
		}

		System.out.println("\nEND ! packets with frames sent: "+count);
	}

}
