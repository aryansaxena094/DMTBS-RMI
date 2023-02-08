import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;


public class ServATW extends UnicastRemoteObject implements RMIs {
    private String ServerName = "Atwater";
    ArrayList<String> admin = new ArrayList<String>();
    private HashMap<String, HashMap<String, Integer>> movies = new HashMap<>();
    private HashMap<String, HashMap<String, Integer>> customer = new HashMap<>();

    protected ServATW() throws RemoteException {
        super();
        admin.add("ATWA9499");
    }
    
    public static void main(String[] args) throws RemoteException, AlreadyBoundException {
        Registry reg = LocateRegistry.createRegistry(5099);
        reg.bind("ATW", new ServATW());
        System.out.println("Atwater Server is running!");
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
            }
        }
        return movieName+" with MovieID "+movieID +" has been removed";
        
        //if customers have booked scenario
    }
    
    @Override
    public String listMovieShows(String movieName) throws RemoteException {
        boolean movieexists = movies.containsKey(movieName);
        if(movieexists==false){
            return "No shows with this Movie Name";
        }
        ArrayList<String> listallshows = new ArrayList<String>();
        Map<String, Integer> tempMap = new HashMap<>(movies.get(movieName));
        for (Map.Entry<String, Integer> entry : tempMap.entrySet()) {
            listallshows.add(entry.getKey() + " with " + entry.getValue() + " capacity");
        }
        return listallshows.toString();
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
}