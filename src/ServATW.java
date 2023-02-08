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
    private HashMap<String, Integer> moviecapacity = new HashMap<>();

    private HashMap<String, HashMap<String, Integer>> customer = new HashMap<>();
    private HashMap<String, Integer> moviesbookedbycustomer = new HashMap<>();
    
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
        boolean movieexists = moviecapacity.containsKey(movieID);
        moviecapacity.merge(movieID, bookingcapacity,Integer::sum);
        movies.put(movieName, moviecapacity);
        
        if(movieexists==true){
            return bookingcapacity+" capacity has been added to "+movieName+" with Movie ID " +movieID;
        }
        return movieName+" with MovieID "+ movieID +" has been created with "+bookingcapacity + " capacity";
    }
    
    @Override
    public String removeMovieSlots(String movieID, String movieName) throws RemoteException {
        boolean movieexists = moviecapacity.containsKey(movieID);
        if(movieexists==false){
            return movieName+" with MovieID "+movieID +" does not exist!";
        }
        moviecapacity.remove(movieID);
        movies.put(movieName, moviecapacity);
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
        Iterator it = moviecapacity.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry pair = (Map.Entry) it.next();
            listallshows.add(pair.getKey()+" with "+pair.getValue()+" capacity");
            it.remove();
        }
        return listallshows.toString();
    }
    
    @Override
    public String bookMovieTicket(String CustomerID, String movieID, String movieName, int Numberoftickets) throws RemoteException{
        if(!movies.containsKey(movieName) && !moviecapacity.containsKey(movieID)){
            return "MovieID/MovieName does not exist!";
        }
        int totaltickets = (movies.get(movieName)).getOrDefault(movieID,Integer.MAX_VALUE);
        
        if(Numberoftickets>totaltickets){
            return "Lesser slots availabe, you can only book "+totaltickets+" for this show";
        }

        totaltickets = totaltickets - Numberoftickets;
        moviesbookedbycustomer.merge(movieID, Numberoftickets, Integer::sum);
        customer.put(CustomerID, moviesbookedbycustomer);
        
        //updaing movies hashmap
        moviecapacity.put(movieID, totaltickets);
        movies.put(movieName, moviecapacity);
        ///
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
        return allbookedshows.toString();
    }
    
    @Override
    public String cancelMovieTickets(String CustomerID, String movieID, String movieName, int Numberoftickets) throws RemoteException {
        
        int ticketsbookedbycustomer = moviesbookedbycustomer.getOrDefault(movieID,0);
        if(ticketsbookedbycustomer<Numberoftickets){
            return "Tickets booked by the user are lesser than the entered amount, you can book "+totaltickets+" tickets for this show";
        }
        ticketsbookedbycustomer = ticketsbookedbycustomer - Numberoftickets;
        moviesbookedbycustomer.put(movieID, ticketsbookedbycustomer);
        
        (movies.get(movieName)).put(movieID, ((movies.get(movieName)).get(movieID)) - Numberoftickets);
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

        return "customer";
    }

    @Override
    public String verifyMovieID(String movieID) throws RemoteException{
        // TODO Auto-generated method stub
        return null;
    }
}