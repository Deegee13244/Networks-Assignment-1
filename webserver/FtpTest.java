public class FtpTest {
    public static void main(String args[]) {
        String username = "ftp";
        String password = "ftp";

        // Initialize the FTP client
        FtpClient ftpClient = new FtpClient();

        try {
            // Connect to the FTP server
            ftpClient.connect(username, password);

            // Download the file "ftp_test.txt" from the FTP home directory
            ftpClient.getFile("ftp_test.txt");

            // Disconnect from the FTP server
            ftpClient.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
