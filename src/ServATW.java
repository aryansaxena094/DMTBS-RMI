import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


public class ServATW extends UnicastRemoteObject implements RMIs {
    private static String ServerName = "Atwater";
    
    private static ArrayList<String> admin = new ArrayList<String>();
    
    private static HashMap<String, HashMap<String, Integer>> movies = new HashMap<>();
    private static HashMap<String, HashMap<String, Integer>> customer = new HashMap<>();
    
    //portfor RMI
    static int RMIport = 5005;
    
    //own port
    //ATWATER PORTS
    static int ATW_ALONP = 4003;
    static int ATW_DATA = 4004;
    
    //server1 ports
    //OUTREMONT PORTS
    static int OUT_ALONP = 4005;
    static int OUT_DATA = 4006;
    
    //datafrom server2 ports
    //VERDUN PORTS
    static int VER_ALONP = 4007;
    static int VER_DATA = 4008;
    
    
    protected ServATW() throws RemoteException 
    {
        super();
        admin.add("ATWA9499");
    }
    
    public static void main(String[] args) throws RemoteException, AlreadyBoundException, IOException {
        Registry reg = LocateRegistry.createRegistry(RMIport);
        reg.bind("ATW", new ServATW());
        System.out.println("Atwater Server is running!");
        
        try (//UDP Server OPEN PORT ALWAYS OPEN
        DatagramSocket Socket = new DatagramSocket(ATW_ALONP)) 
        {
            byte[] receivedata = new byte[1024];
            
            //Keeping UDP Port Open for Recieving
            while(true){
                DatagramPacket receivePacket = new DatagramPacket(receivedata, receivedata.length);
                Socket.receive(receivePacket);
                String recieved = receivePacket.toString();
                
                //sample recieved: LATWAVATAR
                if(recieved.charAt(0)=='L' || recieved.charAt(0)=='l'){
                    String curmoviename = recieved.substring(4);
                    String requestfromserver = recieved.substring(1,4);
                    
                    listMovieServertoServer(curmoviename,requestfromserver);
                }
                else if(recieved.charAt(0)=='C' || recieved.charAt(0)=='c'){
                    //cancellation open  communication link
                }
            }
        }
    }
    
    @Override
    public String addMovieSlots(String movieID, String movieName, int bookingcapacity) throws RemoteException {
        
        boolean movieExists = movies.containsKey(movieName);
        if (movieExists) {
            HashMap<String, Integer> movieCapacity = movies.get(movieName);
            boolean movieIDExists = movieCapacity.containsKey(movieID);
            if (movieIDExists) {
                movieCapacity.merge(movieID, bookingcapacity, Integer::sum);
            } else {
                movieCapacity.put(movieID, bookingcapacity);
            }
            movies.put(movieName, movieCapacity);
        } else {
            HashMap<String, Integer> movieCapacity = new HashMap<>();
            movieCapacity.put(movieID, bookingcapacity);
            movies.put(movieName, movieCapacity);
        }
        print();
        serverlogwriter("ADDING MOVIE SLOT: ", movieName + " : " + movieID + " with " + bookingcapacity + " capacity", true);
        return movieName+" with MovieID "+ movieID +" has been created with "+bookingcapacity + " capacity";
    }
    
    @Override
    public String removeMovieSlots(String movieID, String movieName) throws RemoteException {
        
        boolean movieexists = movies.containsKey(movieName);
        if(movieexists==true){
            boolean movieIDexists = (movies.get(movieName)).containsKey(movieID);
            if(movieIDexists==true){
                (movies.get(movieName)).remove(movieID);
                
                print();
                
                serverlogwriter("DELETING MOVIE SLOT:", movieName + " : " + movieID, true);
                return movieName+" with MovieID "+movieID +" has been removed";
            } else {
                serverlogwriter("DELETING MOVIE SLOT:", "Movie ID not found for movie name: " + movieName, false);
                return "Movie ID not found for movie name: " + movieName;
            }
        } else {
            serverlogwriter("DELETING MOVIE SLOT:", "Movie Name not found for movie name: " + movieName, false);
            return "Movie name not found: " + movieName;
        }
        
        //if customers have booked scenario
    }
    
    
    @Override
    public String listMovieShows(String movieName) throws RemoteException {
        
        ArrayList<String> listallshows = new ArrayList<String>();
        Map<String, Integer> tempMap = new HashMap<>(movies.get(movieName));
        String output = "";
        
        //for own server
        for (Map.Entry<String, Integer> entry : tempMap.entrySet()) {
            listallshows.add(entry.getKey() + " with " + entry.getValue() + " capacity");
        }
        
        //checking other servers
        try {
            
            DatagramSocket sendingrequesttoserv1 = new DatagramSocket();
            
            String sendingrequest = "L"+"ATW"+movieName;
            byte[] senddata = new byte[1024];
            byte[] receivedataserv1 = new byte[1024];
            
            senddata = sendingrequest.getBytes();
            
            InetAddress ip = InetAddress.getLocalHost();
            
            DatagramPacket sendreqtorserv1 = new DatagramPacket(senddata, senddata.length, ip, OUT_ALONP);
            
            sendingrequesttoserv1.send(sendreqtorserv1);
            
            DatagramSocket gettingdatafromserv1 = new DatagramSocket(ATW_DATA);
            
            DatagramPacket packetfromserv1 = null;
            
            String recvdata = "";
            while(recvdata.isBlank()){
                packetfromserv1 = new DatagramPacket(receivedataserv1, receivedataserv1.length);
                gettingdatafromserv1.receive(packetfromserv1);
                recvdata = new String(packetfromserv1.getData(), 0, packetfromserv1.getLength(), StandardCharsets.UTF_8);
            }
            gettingdatafromserv1.close();
            System.out.println(recvdata);
            output = output + " "+ recvdata;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        serverlogwriter("LISTING SHOWS:", movieName, true);
        return ServerName + " : " + listallshows.toString() + " " + output;   
    }
    
    
    //Customer End
    @Override
    public String bookMovieTicket(String CustomerID, String movieID, String movieName, int Numberoftickets) throws RemoteException
    {
        if(!movies.containsKey(movieName) && !(movies.get(movieName)).containsKey(movieID)){
            serverlogwriter("Booking movie ticket:", "Customer:"+CustomerID + " : " + movieName + " : " + movieID, false);
            return "MovieID/MovieName does not exist!";
        }
        int totaltickets = (movies.get(movieName)).get(movieID);
        
        if(Numberoftickets>totaltickets){
            serverlogwriter("Booking movie ticket:", "Customer:"+CustomerID + " : " + movieName + " : " + movieID, false);
            return "Lesser slots availabe, you can only book "+totaltickets+" for this show";
        }
        
        if (!customer.containsKey(CustomerID)) {
            customer.put(CustomerID, new HashMap<>());
        }
        
        totaltickets = totaltickets - Numberoftickets;
        
        HashMap<String, Integer> moviesbookedbycustomer = customer.get(CustomerID);
        moviesbookedbycustomer.put(movieID, moviesbookedbycustomer.getOrDefault(movieID, 0) + Numberoftickets);
        
        
        (movies.get(movieName)).put(movieID, totaltickets);
        
        print();
        
        serverlogwriter("Booking movie ticket:", "Customer:"+CustomerID + " : " + movieName + " : " + movieID, true);
        return Numberoftickets+ " Tickets have been booked to "+movieID+" show of "+movieName;
    }
    
    
    
    @Override
    public String getBookingSchedule(String CustomerID) throws RemoteException {
        if(!customer.containsKey(CustomerID))
        {
            serverlogwriter("GETTING BOOKING SCHEDULE:", "No shows booked in "+ServerName, false);
            return "No shows booked in "+ServerName;
        }
        
        HashMap<String, Integer> customerBookings = customer.get(CustomerID);
        ArrayList<String> allbookedshows = new ArrayList<>();
        
        for (Map.Entry<String, Integer> booking : customerBookings.entrySet()) {
            allbookedshows.add(booking.getValue() + " tickets for " + booking.getKey());
        }
        
        print();
        
        serverlogwriter("GETTING BOOKING SCHEDULE FOR:", CustomerID, true);
        return allbookedshows.toString();
    }
    
    @Override
    public String cancelMovieTickets(String CustomerID, String movieID, String movieName, int Numberoftickets) throws RemoteException {
        if (!customer.containsKey(CustomerID)) {
            serverlogwriter("CANCEL MOVIE TICKET", "Customer ID not found "+ CustomerID,false);
            return "Customer ID not found.";
        }
        
        HashMap<String, Integer> customerBookings = customer.get(CustomerID);
        if (!customerBookings.containsKey(movieID)) {
            serverlogwriter("CANCEL MOVIE TICKET", "No tickets found for the movie with ID " + movieID,false);
            return "No tickets found for the movie with ID " + movieID;
        }
        
        if (customerBookings.get(movieID) < Numberoftickets) {
            serverlogwriter("CANCEL MOVIE TICKET", "Only " + customerBookings.get(movieID) + " tickets found for the movie with ID " + movieID,false);
            return "Only " + customerBookings.get(movieID) + " tickets found for the movie with ID " + movieID;
        }
        
        if (!movies.containsKey(movieName)) {
            serverlogwriter("CANCEL MOVIE TICKET", "Movie with name " + movieName + " not found.",false);
            return "Movie with name " + movieName + " not found.";
        }
        
        HashMap<String, Integer> movieBookings = movies.get(movieName);
        if (!movieBookings.containsKey(movieID)) {
            serverlogwriter("CANCEL MOVIE TICKET", "Movie with ID " + movieID + " not found.",false);
            return "Movie with ID " + movieID + " not found.";
        }
        
        customerBookings.put(movieID, customerBookings.get(movieID) - Numberoftickets);
        movieBookings.put(movieID, movieBookings.get(movieID) + Numberoftickets);
        
        serverlogwriter("INFO", Numberoftickets + " tickets has been cancelled for " + movieName + " with MovieID " + movieID,true);
        
        print();
        
        return Numberoftickets + " tickets has been cancelled for " + movieName + " with MovieID " + movieID;
    }
    
    @Override
    public String verifyID(String ID) throws RemoteException{
        if(ID.length() < 8 || ID.charAt(3)!='A' &&  ID.charAt(3)!='C' && ID.charAt(3)!='a' && ID.charAt(3)!='c')
        {
            serverlogwriter("VERIFY ID", "Invalid ID "+ ID, false);
            return "This entered ID is invalid";
        }
        
        if(ID.charAt(3)=='A' || ID.charAt(3)=='a'){
            //admincheck
            if(admin.contains(ID)){
                //checking if admin exists in db
                serverlogwriter("VERIFY ID", "Welcome Admin with ID "+ ID, true);
                return "Welcome Admin!";
            }
            else {
                serverlogwriter("VERIFY ID", "Admin with ID "+ ID +" not found", false);
                return "Admin with ID "+ ID +" not found";
            }
        }
        
        if(ID.charAt(3)=='C' || ID.charAt(3)=='c'){
            //admincheck
            serverlogwriter("VERIFY ID", "Welcome Customer with ID "+ ID, true);
            return "Welcome Customer!";
        }
        serverlogwriter("VERIFY ID", "Invalid ID "+ ID, false);
        return "This entered ID is invalid";
    }
    
    @Override
    public String verifyMovieID(String movieID) throws RemoteException, ParseException{
        if (movieID.length() != 10) {
            serverlogwriter("VERIFY MOVIE ID", "Invalid movie ID: Must be 10 characters long", false);
            return "Invalid movie ID: Must be 10 characters long";
        }
        
        char session = movieID.charAt(3);
        if (session != 'M' && session != 'A' && session != 'E') {
            serverlogwriter("VERIFY MOVIE ID", "Invalid movie ID: Fourth character must be M (morning), A (afternoon), or E (evening)", false);
            return "Invalid movie ID: Fourth character must be M (morning), A (afternoon), or E (evening)";
        }
        
        int day = Integer.parseInt(movieID.substring(4, 6));
        int month = Integer.parseInt(movieID.substring(6, 8));
        int year = Integer.parseInt("20"+movieID.substring(8,10));
        
        if (month < 1 || month > 12 || day < 1 || day > 31) {
            serverlogwriter("VERIFY MOVIE ID", "Invalid movie ID: Invalid date format", false);
            return "Invalid movie ID: Invalid date format";
        }
        
        String datestring = String.format("%02d%02d%04d", day, month, year);
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy");
        Date date1 = dateFormat.parse(datestring);
        
        Date currentdate = new Date(System.currentTimeMillis());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentdate);
        calendar.add(Calendar.DATE, 7);
        Date oneWeekFromNow = calendar.getTime();
        
        System.out.println(date1);
        System.out.println(currentdate);
        System.out.println(oneWeekFromNow);
        
        if (date1.before(currentdate) || date1.after(oneWeekFromNow)) {
            serverlogwriter("VERIFY MOVIE ID", "You can only access tickets for dates within the next 7 days from today.", false);
            return "You can only access tickets for dates within the next 7 days from today.";
        }
        
        serverlogwriter("VERIFY MOVIE ID", "Valid movie ID: " + movieID, true);
        return "Valid";
    }
    
    
    //UDP
    public static void listMovieServertoServer (String movieName, String serverrequest) throws RemoteException{
        String output = "";
        ArrayList<String> listallshows = new ArrayList<String>();
        Map<String, Integer> tempMap = new HashMap<>(movies.getOrDefault(movieName,new HashMap<>()));
        if(!tempMap.isEmpty())
        {
            for (Map.Entry<String, Integer> entry : tempMap.entrySet()) {
                listallshows.add(entry.getKey() + " with " + entry.getValue() + " capacity");
            }
        }
        output =  ServerName +" : "+ listallshows.toString();
        boolean sendPacketStatus = false;
        try {
            byte[] senddata = new byte[1024];
            senddata = output.getBytes();
            InetAddress ip = InetAddress.getLocalHost();
            DatagramSocket toserv = new DatagramSocket();
            if(serverrequest.equalsIgnoreCase("ATW"))
            {
                DatagramPacket sendpacket = new DatagramPacket(senddata, senddata.length, ip, ATW_DATA);
                toserv.send(sendpacket);
                sendPacketStatus = true;
            }
            else if(serverrequest.equalsIgnoreCase("VER"))
            {
                DatagramPacket sendpacket = new DatagramPacket(senddata, senddata.length, ip, VER_DATA);
                toserv.send(sendpacket);
                sendPacketStatus = true;
            }
            toserv.close();
        } catch (Exception e) {
            sendPacketStatus = false;
        }
        finally{
            serverlogwriter("SEND PACKET SERVER TO SERVER(UDP)", output, sendPacketStatus);
        }
    }
    
    //adding admin
    
    public String addadmin(String adminID) throws RemoteException{
        if(adminID.length() < 8 || adminID.charAt(3)!='A'  && adminID.charAt(3)!='a')
        {
            serverlogwriter("addadmin", adminID, false);
            return "This entered ID is invalid";
        }
        
        if(!adminID.substring(0,3).equalsIgnoreCase("ATW")){
            serverlogwriter("addadmin", adminID, false);
            return "This admin cannot be created in "+ServerName;
        }
        
        
        if(admin.contains(adminID)){
            serverlogwriter("addadmin", adminID, false);
            return "Admin already exists!";
        }
        
        admin.add(adminID);
        serverlogwriter("addadmin", adminID, true);
        return "Admin has been successfully added";
    }
    
    
    
    public static void print(){
        System.out.println(ServerName+" Hashmaps");
        System.out.println("Movies Hashmap: "+movies);
        System.out.println("Customer Hashmap: "+customer);
    }
    
    
    public static void serverlogwriter(String requesttype, String ID, boolean status){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH-mm-ss");
        Date date = new Date();
        String dateStr = dateFormat.format(date);
        String timeStr = timeFormat.format(date);
        String logFilePath = "/Users/aryansaxena/Desktop/DSDA1/DMTS/logs/Atwater" + dateStr + ".txt";
        File logFile = new File(logFilePath);
        String logMessage = "DATE: "+ dateStr + " | " + "TIME: " + timeStr + " | " + "REQUEST TYPE: " + requesttype + " | " + ID + " | " + "STATUS: "+(status ? "success" : "failure");
        try {
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(logFile, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(
            "_________________________________________________________________________________________"
            +"\n"+
            logMessage
            +"\n"+
            "_________________________________________________________________________________________");
            bufferedWriter.newLine();
            bufferedWriter.close();
        } catch (IOException e) {
            System.out.println("Error writing to log file: " + e.getMessage());
        }
    }
    
}