/**
* Assignment 1
* Brad Didier
**/
import java.io.* ;
import java.net.* ;
import java.util.* ;
public final class WebServer
{
    public static void main(String argv[]) throws Exception
    {
        //set the port number
        int port = 6789;

        //establish the listen socket
        ServerSocket sock = new ServerSocket(port);
        Socket toSend = null;

        //Process HTTP service requests in an infinite loop
        while(true) {
            //Listen for a TCP connection request
            toSend = sock.accept();

            //Construct an object to process the HTTP request message
            HttpRequest request = new HttpRequest(toSend);

            //Create a new thread to process the request
            Thread thread = new Thread(request);

            //Start the thread
            thread.start();
        }
    }

}
final class HttpRequest implements Runnable
{
    final static String CRLF = "\r\n";
    Socket socket;

    //Constructor
    public HttpRequest(Socket socket) throws Exception
    {
        this.socket = socket;
    }

    //Implement the run() method of the Runnable interface
    public void run() 
    {
        try {
            processRequest();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void processRequest() throws Exception
    {
        //Get a reference to the socket's input and output streams
        InputStream is = new DataInputStream(socket.getInputStream());
        DataOutputStream os = new DataOutputStream(socket.getOutputStream());

        //Set up input stream filters
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        //Get the request line of the HTTP request message
        String requestLine = br.readLine();

        //Display the request line
        System.out.println();
        System.out.println("Request:");
        System.out.println("--------------");
        System.out.println(requestLine);

        //Get and display the header lines
        String headerLine = null;
        while ((headerLine = br.readLine()).length() != 0) {
            System.out.println(headerLine);
        }

        //Extract the filename from the request line
        StringTokenizer tokens = new StringTokenizer(requestLine);
        tokens.nextToken(); //skip over the method, which should be GET
        String fileName = tokens.nextToken();

        //Prepend a "." so that file request is within current directory
        fileName = "." + fileName;

        //Open the request file
        FileInputStream fis = null;
        boolean fileExists = true;
        try{
            fis = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            fileExists = false;
        }

        //Construct the response message
        String statusLine = null;
        String contentTypeLine = null;
        String entityBody = null;
        if (fileExists) {
            statusLine = "HTTP/1.1 200 OK" + CRLF;
            contentTypeLine = "Content-type: " + contentType(fileName) + CRLF;
        } else {
            // If the file requested is any type other than a text (.txt) file, 
            // report an error to the web client
            if (!contentType(fileName).equalsIgnoreCase("text/plain")) {
                statusLine = "HTTP/1.1 404 Not Found" + CRLF;
                contentTypeLine = "Content-type: text/html" + CRLF;
                entityBody = "<HTML>" + 
                    "<HEAD><TITLE>Not Found</TITLE></HEAD>" + 
                    "<BODY>Not Found</BODY></HTML>";
            } else {
                //else retrieve the text (.txt) file from local FTP server
                statusLine = "HTTP/1.1 200 OK" + CRLF;
                contentTypeLine = "Content-type: text/plain" + CRLF;

                // Create an instance of the FTP client
                FtpClient ftpClient = new FtpClient();

                // Connect to the FTP server with credentials
                ftpClient.connect("ftp", "ftp");

                // Retrieve the file from the FTP server (make sure it's uploaded to your FTP server under your user's directory)
                ftpClient.getFile(fileName);

                // Disconnect from the FTP server
                ftpClient.disconnect();

                // assign input stream to read the recently ftp-downloaded file
                fis = new FileInputStream(fileName);
            }
        }

        System.out.println("\nResponse:");
        System.out.println("--------------");

        //Send the status line
        os.writeBytes(statusLine);
        System.out.println(statusLine);

        //Send the content type line
        os.writeBytes(contentTypeLine);
        System.out.println(contentTypeLine);

        //Send a blank line to indicate end of header lines
        os.writeBytes(CRLF);

        //Send the entity body
        if (fileExists) {
            sendBytes(fis, os);
            fis.close();
        } else {
            if (!contentType(fileName).equalsIgnoreCase("text/plain")) {
                os.writeBytes(entityBody);
            }
            else {
                sendBytes(fis, os);
            }
        }

        //Close streams and socket
        os.close();
        br.close();
        socket.close();
    }

    private static void sendBytes(FileInputStream fis, OutputStream os)
    throws Exception
    {
        //Construct a 1K buffer to hold bytes on their way to the socket
        byte[] buffer = new byte[1024];
        int bytes = 0;

        //Copy requested file into the socket's output stream
        while((bytes = fis.read(buffer)) != -1) {
            os.write(buffer, 0, bytes);
        }
    }

    private String contentType(String fileName) {
        if( fileName.endsWith(".htm") || fileName.endsWith(".html")  )
			return "text/html";
		if ( fileName.endsWith(".txt") )
			return "text/plain";
		if ( fileName.endsWith(".jpg") )
			return "image/jpeg";
		if ( fileName.endsWith(".png") )
			return "image/png";
		if ( fileName.endsWith(".gif") )
			return "image/gif";
		return "application/octet-stream";
    }
}