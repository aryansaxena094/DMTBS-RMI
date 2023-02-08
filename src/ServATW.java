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
    // private HashMap<String, Integer> moviesbookedbycustomer = new HashMap<>();
    
    private int totaltickets;
    
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

        //FIX MULTIPLE MOVIE THING
        //MERGE FUNCTION HAS PROBLEM
        ///testing
        System.out.println("Movies Hashmap: "+movies.toString());
        System.out.println("Hashmap with Customer Values: "+ customer.toString());
        ///
        
        
        
        
        // if(movieexists==true){
        //     return bookingcapacity+" capacity has been added to "+movieName+" with Movie ID " +movieID;
        // }
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
        
        ///testing
        System.out.println("Movies Hashmap: "+movies.toString());
        System.out.println("Hashmap with Customer Values: "+ customer.toString());
        ///
        
        return movieName+" with MovieID "+movieID +" has been removed";
        
        //if while removing customers have booked then scenario!!
    }
    
    @Override
    public String listMovieShows(String movieName) throws RemoteException {
        boolean movieexists = movies.containsKey(movieName);
        if(movieexists==false){
            return "No shows with this Movie Name";
        }
        ArrayList<String> listallshows = new ArrayList<String>();
        Iterator it = (movies.get(movieName)).entrySet().iterator();
        while(it.hasNext()){
            Map.Entry pair = (Map.Entry) it.next();
            listallshows.add(pair.getKey()+" with "+pair.getValue()+" capacity");
            it.remove();
        }
        
        ///testing
        System.out.println("Movies Hashmap: "+movies.toString());
        System.out.println("Hashmap with Customer Values: "+ customer.toString());
        ///
        
        return listallshows.toString();
    }
    

    //Customer End
    @Override
    public String bookMovieTicket(String CustomerID, String movieID, String movieName, int Numberoftickets) throws RemoteException{
        if(!movies.containsKey(movieName) && !(movies.get(movieName)).containsKey(movieID)){
            return "MovieID/MovieName does not exist!";
        }
        int totaltickets = (movies.get(movieName)).get(movieID);
        
        if(Numberoftickets>totaltickets){
            return "Lesser slots availabe, you can only book "+totaltickets+" for this show";
        }
        
        totaltickets = totaltickets - Numberoftickets;

        HashMap<String, Integer> moviesbookedbycustomer = new HashMap<>();
        moviesbookedbycustomer.put(movieID, customer.get(CustomerID).getOrDefault(movieID, 0) + Numberoftickets);
        
        customer.put(CustomerID, moviesbookedbycustomer);
        
        //updaing movies hashmap
        (movies.get(movieName)).put(movieID, totaltickets);
        
        ///testing
        System.out.println("Movies Hashmap: "+movies.toString());
        System.out.println("Hashmap with MovieID & tickets: "+ moviesbookedbycustomer.toString());
        System.out.println("Hashmap with Customer Values: "+ customer.toString());
        ///
        
        return Numberoftickets+ " Tickets have been booked to "+movieID+" show of "+movieName;
    }
    
    @Override
    public String getBookingSchedule(String CustomerID) throws RemoteException {
        if(!customer.containsKey(CustomerID))
        {
            return "No shows booked in "+ServerName;
        }
        
        Iterator it = (customer.get(CustomerID)).entrySet().iterator();
        ArrayList<String> allbookedshows = new ArrayList<>();
        
        while(it.hasNext()){
            Map.Entry pair = (Map.Entry) it.next();
            allbookedshows.add(pair.getValue()+" tickets for "+ pair.getKey());
            it.remove();
        }
        
        ///testing
        System.out.println("Movies Hashmap: "+movies.toString());
        System.out.println("Hashmap with Customer Values: "+ customer.toString());
        ///
        
        
        
        
        
        
        return allbookedshows.toString();
    }
    
    @Override
    public String cancelMovieTickets(String CustomerID, String movieID, String movieName, int Numberoftickets) throws RemoteException {
        
        int ticketsbookedbycustomer = (customer.get(CustomerID)).getOrDefault(movieID,0);
        if(ticketsbookedbycustomer<Numberoftickets){
            return "Tickets booked by the user are lesser than the entered amount, you can book "+totaltickets+" tickets for this show";
        }
        ticketsbookedbycustomer = ticketsbookedbycustomer - Numberoftickets;
        (customer.get(CustomerID)).put(movieID, ticketsbookedbycustomer);
        
        (movies.get(movieName)).put(movieID, ((movies.get(movieName)).get(movieID)) - Numberoftickets);
        
        ///testing
        System.out.println("Movies Hashmap: "+movies.toString());
        System.out.println("Hashmap with Customer Values: "+ customer.toString());
        ///
        
        
        
        
        return Numberoftickets+" tickets has been cancelled for "+movieName+" with MovieID "+movieID;
    }
    
    @Override
    public String verifyID(String ID) throws RemoteException{
        
        if(ID.length() < 8 || ID.charAt(3)!='A' &&  ID.charAt(3)!='C' && ID.charAt(3)!='a' && ID.charAt(3)!='c')
        {
            return "invalid";
        }
        
        if(ID.charAt(3)=='A' || ID.charAt(3)=='a'){
            //admincheck
            if(admin.contains(ID)){
                //checking if admin exists in db
                return "admin";
            }
        }
        
        if(ID.charAt(3)=='C' || ID.charAt(3)=='c'){
            //admincheck
            return "customer";
        }
        return "invalid";
    }
    
    @Override
    public String verifyMovieID(String movieID) throws RemoteException{
        //verifying the movie ID
        
        
        
        
        return null;
    }
}