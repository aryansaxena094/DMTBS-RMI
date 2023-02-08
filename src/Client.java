import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class Client {
    
    public static void intro()
    {
        System.out.println("***************************************************");
        System.out.println("**** Distributed Movie Ticketing Service(DMTS) ****");
        System.out.println("***************************************************");
        
    }
    
    public static void conclusion(){
        System.out.println("***************************************************");
        System.out.println("************ Thank you for using DMTS! ************");
        System.out.println("***************************************************");
    }
    
    public static void adminmenu(String ID){
        System.out.println("\r\nHey Admin: "+ID);
        System.out.println("Please select your actions: ");
        System.out.println("1. Add Movie Slots");
        System.out.println("2. Remove Mobie Slots");
        System.out.println("3. List Movie Shows Availability");
        System.out.println("4. Add Another Admin");
        System.out.println("5. Use Different ID");
        System.out.println("6. EXIT \r\n");
    }
    
    public static void customermenu(String ID){
        System.out.println("\r\nHey Customer: "+ID);
        System.out.println("1. Book Movie Ticket");
        System.out.println("2. Get Booking Schedule");
        System.out.println("3. Cancel Movie Tickets");
        System.out.println("4. Use Different ID");
        System.out.println("5. EXIT \r\n");
    }
    
    public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException {
        intro();
        Scanner sc = new Scanner(System.in);
        String ID = "";
        //Connecting to Servers
        RMIs servatw = (RMIs) Naming.lookup("rmi://localhost:5099/ATW");
        
        program:while(true){
            String verification = "This entered ID is invalid";
            
            while(verification.equalsIgnoreCase("This entered ID is invalid")){
                System.out.println("Please Enter your ID: ");
                ID = sc.nextLine();
                
                if(ID.length() > 3)
                {
                    if(ID.substring(0,3).equalsIgnoreCase("ATW")){
                        verification = servatw.verifyID(ID);
                    }
                    else if(ID.substring(0,2).equalsIgnoreCase("VER"))
                    {

                    }
                    else if(ID.substring(0,2).equalsIgnoreCase("OUT")) {
                        
                    }
                }
                else
                {
                    verification = "invalid";
                }
                System.out.println(verification);
            }
            user:while(true){

                //variables (most used)
                String movieID = "";
                String movieName = "";
                
                if(verification.equalsIgnoreCase("Welcome Admin!")){
                    adminmenu(ID);
                    int menuinp = Integer.parseInt(sc.nextLine());
                    //admin menu
                    switch(menuinp){
                        case 1:{
                            //addingtotheserver
                            //general variables
                            String whichadmin = ID.substring(0,3);
                            int bookingcapacity = 0;

                            while(true)
                            {
                                System.out.println("Enter MovieID: ");
                                movieID = sc.nextLine();
                                if(movieID.substring(0,3).equals(whichadmin)){
                                    break;
                                }
                                System.out.println("You are not allowed to access this server!");
                            }
                            System.out.println("Enter Name of the Movie: ");
                            movieName = sc.nextLine();
                            System.out.println("Enter the Capacity you wish to add: ");
                            bookingcapacity = Integer.parseInt(sc.nextLine());
                            
                            //accessing RMI
                            
                            if(whichadmin.equalsIgnoreCase("ATW")){
                                //adding to atwater
                                System.out.println("\n\r"+servatw.addMovieSlots(movieID,movieName,bookingcapacity));
                            }
                            else if(whichadmin.equalsIgnoreCase("VER")){
                                //adding to verdun
                            }
                            else if(whichadmin.equalsIgnoreCase("OUT")){
                                //adding to outremont
                            }
                        }
                        break;
                        
                        case 2:{
                            //deleting from the server
                            //general variables
                            String whichadmin = ID.substring(0,3);

                            while(true)
                            {
                                System.out.println("Enter MovieID: ");
                                movieID = sc.nextLine();
                                if(movieID.substring(0,3).equals(whichadmin)){
                                    break;
                                }
                                System.out.println("You are not allowed to access this server!");
                            }
                            System.out.println("Enter Name of the Movie: ");
                            movieName = sc.nextLine();
                            
                            if(whichadmin.equalsIgnoreCase("ATW")){
                                System.out.println("\n\r"+servatw.removeMovieSlots(movieID,movieName));
                            }
                            else if(whichadmin.equalsIgnoreCase("VER")){
                                //removing from verdun
                            }
                            else if(whichadmin.equalsIgnoreCase("OUT")){
                                //removing from outremont
                            }
                            
                        }
                        break;
                        
                        case 3:{
                            System.out.println("Enter the Movie Name of which you wish to see the shows for: ");
                            movieName = sc.nextLine();
                            System.out.println("Atwater: ");
                            System.out.println("\n\r"+servatw.listMovieShows(movieName));
                            
                            //write for other two servers too!
                            // RMIs servatw = (RMIs) Naming.lookup("rmi://localhost:5099/ATW");
                            // System.out.println("\n\r"+servatw.listMovieShows(movieName));
                            
                            // RMIs servatw = (RMIs) Naming.lookup("rmi://localhost:5099/ATW");
                            // System.out.println("\n\r"+servatw.listMovieShows(movieName));
                            
                        }
                        break;
                        
                        case 4:{
                            
                        }
                        break;
                        
                        case 5:{
                            break user;
                        }
                        
                        case 6:{
                            break program;
                        }
                        default: break;
                    }
                }
                else if(verification.equalsIgnoreCase("Welcome Customer!"))
                {//customer menu
                    customermenu(ID);
                    int menuinp = Integer.parseInt(sc.nextLine());
                    switch(menuinp){
                        case 1:{
                            //booking movie ticket
                            //variables
                            int ticketstobebooked = 0;
                            
                            System.out.println("Name of the Movie: ");
                            movieName = sc.nextLine();
                            System.out.println("MovieID: ");
                            movieID = sc.nextLine();
                            System.out.println("Enter the nubmer of tickets to be booked: ");
                            ticketstobebooked = Integer.parseInt(sc.nextLine());

                            System.out.println(servatw.bookMovieTicket(ID,movieID,movieName,ticketstobebooked));
                            
                        }
                        break;
                        case 2:{
                            System.out.println(servatw.getBookingSchedule(ID));
                        }
                        break;
                        case 3:{
                            //CANCELLING THE MOVIE TICKET
                            int Numberoftickets = 0;

                            System.out.println("If you wish to cancel a movie ticket, please enter - ");

                            System.out.println("Movie ID of the ticket booked: ");
                            movieID = sc.nextLine();
                            System.out.println("Name of the Movie booked");
                            movieName = sc.nextLine();
                            System.out.println("Number of tickets you wish to cancel");
                            Numberoftickets = Integer.parseInt(sc.nextLine());
                            
                            System.out.println(servatw.cancelMovieTickets(ID, movieID, movieName, Numberoftickets));                            
                        }
                        break;
                        case 4:{
                            break user;
                        }
                        case 5: {
                            break program;
                        }
                        default:
                    }
                }       
            }   
        }
        
        //end code
        conclusion();
        sc.close();
    }
}
