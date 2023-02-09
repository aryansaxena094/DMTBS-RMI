import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;


public class ServVER extends UnicastRemoteObject implements RMIs {
    private static String ServerName = "Verdun";
    
    private static ArrayList<String> admin = new ArrayList<String>();

    private static HashMap<String, HashMap<String, Integer>> movies = new HashMap<>();
    private static HashMap<String, HashMap<String, Integer>> customer = new HashMap<>();
    
    //portfor RMI
    static int RMIport = 4001;

    //own port
    //VERDUN PORTS
    static int alwaysonport = 7000;
    static int dataincoming = 7001;
    static int dataoutgoing = 7002;
    
    //server1 ports
    //ATWATER PORTS
    static int serv1alwaysonport = 5000;
    static int serv1dataincoming = 5001;
    static int serv1dataoutgoing = 5002;
    
    //datafrom server2 ports
    //OUTREMONT PORTS
    static int serv2alwaysonport = 6000;
    static int serv2dataincoming = 6001;
    static int serv2dataoutgoing = 6002;

    
    protected ServVER() throws RemoteException {
        super();
        admin.add("VER9499");
    }
    
    public static void main(String[] args) throws RemoteException, AlreadyBoundException, IOException {
        Registry reg = LocateRegistry.createRegistry(RMIport);
        reg.bind("VER", new ServATW());
        System.out.println("Verdun Server is running!");
        
        try (//UDP Server OPEN PORT ALWAYS OPEN
        DatagramSocket ATWSocket = new DatagramSocket(alwaysonport)) 
        {
            byte[] receivedata = new byte[1024];
            
            //Keeping UDP Port Open for Recieving
            while(true){
                DatagramPacket receivePacket = new DatagramPacket(receivedata, receivedata.length);
                ATWSocket.receive(receivePacket);
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
        return movieName+" with MovieID "+ movieID +" has been created with "+bookingcapacity + " capacity";
    }
    
    @Override
    public String removeMovieSlots(String movieID, String movieName) throws RemoteException {
        
        boolean movieexists = movies.containsKey(movieName);
        if(movieexists==true){
            boolean movieIDexists = (movies.get(movieName)).containsKey(movieID);
            if(movieIDexists==true){
                (movies.get(movieName)).remove(movieID);
                return movieName+" with MovieID "+movieID +" has been removed";
            } else {
                return "Movie ID not found for movie name: " + movieName;
            }
        } else {
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
            String sendingrequest = "L"+"VER"+movieName;
            byte[] senddata = new byte[1024];
            senddata = sendingrequest.getBytes();

            byte[] receivedataserv1 = new byte[1024];
            byte[] receivedataserv2 = new byte[1024];

            InetAddress ip = InetAddress.getLocalHost();
            
            //sending request to serv1
            DatagramSocket sendingrequesttoserv1 = new DatagramSocket(serv1alwaysonport);
            DatagramPacket sendreqtorserv1 = new DatagramPacket(senddata, senddata.length, ip, serv1alwaysonport);
            sendingrequesttoserv1.send(sendreqtorserv1);
            sendingrequesttoserv1.close();

            //getting values from serv1
            DatagramSocket gettingdatafromserv1 = new DatagramSocket(dataincoming);
            DatagramPacket packetfromserv1 =new DatagramPacket(receivedataserv1,receivedataserv1.length);

            gettingdatafromserv1.receive(packetfromserv1);
            output = output + " "+ packetfromserv1.toString();

            gettingdatafromserv1.close();

            //sending request to serv2
            DatagramSocket sendingrequesttoserv2 = new DatagramSocket(serv2alwaysonport);
            DatagramPacket sendreqtorserv2 = new DatagramPacket(senddata, senddata.length, ip, serv2alwaysonport);
            sendingrequesttoserv2.send(sendreqtorserv2);
            sendingrequesttoserv2.close();

            //getting values from serv1
            DatagramSocket gettingdatafromserv2 = new DatagramSocket(dataincoming);
            DatagramPacket packetfromserv2 =new DatagramPacket(receivedataserv2,receivedataserv2.length);

            gettingdatafromserv2.receive(packetfromserv2);
            output = output + " "+ packetfromserv2.toString();
            
            gettingdatafromserv2.close();
            
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        
        return ServerName + " : " + listallshows.toString() + " " + output;   
    }
    
    
    //Customer End
    @Override
    public String bookMovieTicket(String CustomerID, String movieID, String movieName, int Numberoftickets) throws RemoteException
    {
        if(!movies.containsKey(movieName) && !(movies.get(movieName)).containsKey(movieID)){
            return "MovieID/MovieName does not exist!";
        }
        int totaltickets = (movies.get(movieName)).get(movieID);
        
        if(Numberoftickets>totaltickets){
            return "Lesser slots availabe, you can only book "+totaltickets+" for this show";
        }
        
        if (!customer.containsKey(CustomerID)) {
            customer.put(CustomerID, new HashMap<>());
        }
        
        totaltickets = totaltickets - Numberoftickets;
        
        HashMap<String, Integer> moviesbookedbycustomer = customer.get(CustomerID);
        moviesbookedbycustomer.put(movieID, moviesbookedbycustomer.getOrDefault(movieID, 0) + Numberoftickets);
        
        (movies.get(movieName)).put(movieID, totaltickets);
        return Numberoftickets+ " Tickets have been booked to "+movieID+" show of "+movieName;
    }
    
    
    @Override
    public String getBookingSchedule(String CustomerID) throws RemoteException {
        if(!customer.containsKey(CustomerID))
        {
            return "No shows booked in "+ServerName;
        }
        
        HashMap<String, Integer> customerBookings = customer.get(CustomerID);
        ArrayList<String> allbookedshows = new ArrayList<>();
        
        for (Map.Entry<String, Integer> booking : customerBookings.entrySet()) {
            allbookedshows.add(booking.getValue() + " tickets for " + booking.getKey());
        }
        
        return allbookedshows.toString();
    }
    
    @Override
    public String cancelMovieTickets(String CustomerID, String movieID, String movieName, int Numberoftickets) throws RemoteException {
        if (!customer.containsKey(CustomerID)) {
            return "Customer ID not found.";
        }
        
        HashMap<String, Integer> customerBookings = customer.get(CustomerID);
        if (!customerBookings.containsKey(movieID)) {
            return "No tickets found for the movie with ID " + movieID;
        }
        
        if (customerBookings.get(movieID) < Numberoftickets) {
            return "Only " + customerBookings.get(movieID) + " tickets found for the movie with ID " + movieID;
        }
        
        if (!movies.containsKey(movieName)) {
            return "Movie with name " + movieName + " not found.";
        }
        
        HashMap<String, Integer> movieBookings = movies.get(movieName);
        if (!movieBookings.containsKey(movieID)) {
            return "Movie with ID " + movieID + " not found.";
        }
        
        customerBookings.put(movieID, customerBookings.get(movieID) - Numberoftickets);
        movieBookings.put(movieID, movieBookings.get(movieID) + Numberoftickets);
        
        return Numberoftickets + " tickets has been cancelled for " + movieName + " with MovieID " + movieID;
    }
    
    @Override
    public String verifyID(String ID) throws RemoteException{
        
        if(ID.length() < 8 || ID.charAt(3)!='A' &&  ID.charAt(3)!='C' && ID.charAt(3)!='a' && ID.charAt(3)!='c')
        {
            return "This entered ID is invalid";
        }
        
        if(ID.charAt(3)=='A' || ID.charAt(3)=='a'){
            //admincheck
            if(admin.contains(ID)){
                //checking if admin exists in db
                return "Welcome Admin!";
            }
        }
        
        if(ID.charAt(3)=='C' || ID.charAt(3)=='c'){
            //admincheck
            return "Welcome Customer!";
        }
        return "This entered ID is invalid";
    }
    
    @Override
    public String verifyMovieID(String movieID) throws RemoteException{
        if (movieID.length() != 8) {
            return "Invalid movie ID: Must be 8 characters long";
        }

        char session = movieID.charAt(3);
        if (session != 'M' && session != 'A' && session != 'E') {
            return "Invalid movie ID: Fourth character must be M (morning), A (afternoon), or E (evening)";
        }
        
        int day = Integer.parseInt(movieID.substring(4, 6));
        int month = Integer.parseInt(movieID.substring(6, 8));
        if (month < 1 || month > 12 || day < 1 || day > 31) {
            return "Invalid movie ID: Invalid date format";
        }
        return "Valid movie ID";
    }
    
    //UDP
    public static void listMovieServertoServer (String movieName, String serverrequest) throws RemoteException{
        String output = "";
        ArrayList<String> listallshows = new ArrayList<String>();
        Map<String, Integer> tempMap = new HashMap<>(movies.get(movieName));
        for (Map.Entry<String, Integer> entry : tempMap.entrySet()) {
            listallshows.add(entry.getKey() + " with " + entry.getValue() + " capacity");
        }
        output =  ServerName +" : "+ listallshows.toString();
        
        try {
            byte[] senddata = new byte[1024];
            senddata = output.getBytes();
            InetAddress ip = InetAddress.getLocalHost();
            DatagramPacket sendpacket = new DatagramPacket(senddata, senddata.length, ip, 6999);
            
            if(serverrequest.equalsIgnoreCase("ATW"))
            {
                DatagramSocket toserv = new DatagramSocket(serv1dataincoming);
                toserv.send(sendpacket);
                toserv.close();
            }
            else if(serverrequest.equalsIgnoreCase("OUT"))
            {
                //sending to the server that asked
                DatagramSocket toserv = new DatagramSocket(serv2dataincoming);
                toserv.send(sendpacket);
                toserv.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }


    //adding admin

    public String addadmin(String adminID){

        if(adminID.length() < 8 || adminID.charAt(3)!='A'  && adminID.charAt(3)!='a')
        {
            return "This entered ID is invalid";
        }

        if(!adminID.substring(0,4).equalsIgnoreCase("VER")){
            return "This admin cannot be created in "+ServerName;
        }

        if(admin.contains(adminID)){
            return "Admin already exists!";
        }

        admin.add(adminID);
        return "Admin has been successfully added";
    }




}