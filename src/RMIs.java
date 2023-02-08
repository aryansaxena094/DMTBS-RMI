import java.rmi.Remote;
import java.rmi.RemoteException;

interface RMIs extends Remote{
    //Admin
    public String addMovieSlots(String movieID ,String movieName, int bookingcapacity) throws RemoteException;
    public String removeMovieSlots(String movieID ,String movieName) throws RemoteException;
    public String listMovieShows(String movieName) throws RemoteException;

    //Customer
    public String bookMovieTicket(String CustomerID ,String movieID, String movieName, int Numberoftickets) throws RemoteException;
    public String getBookingSchedule(String CustomerID) throws RemoteException;
    public String cancelMovieTickets(String CustomerID, String movieID ,String movieName, int Numberoftickets) throws RemoteException;
}