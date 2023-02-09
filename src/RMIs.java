import java.rmi.Remote;
import java.rmi.RemoteException;
import java.text.ParseException;

interface RMIs extends Remote{
    //Authentication
    public String verifyID(String ID) throws RemoteException;
    public String verifyMovieID(String movieID) throws RemoteException, ParseException;
    
    //Admin
    public String addMovieSlots(String movieID ,String movieName, int bookingcapacity) throws RemoteException;
    public String removeMovieSlots(String movieID ,String movieName) throws RemoteException;
    public String listMovieShows(String movieName) throws RemoteException;

    //adding admin
    public String addadmin(String adminID) throws RemoteException;

    //Customer
    public String bookMovieTicket(String CustomerID ,String movieID, String movieName, int Numberoftickets) throws RemoteException;
    public String getBookingSchedule(String CustomerID) throws RemoteException;
    public String cancelMovieTickets(String CustomerID, String movieID ,String movieName, int Numberoftickets) throws RemoteException;
}
