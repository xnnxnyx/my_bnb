package Reports;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Report {

  public static int Listing_ID = 0;
  private static int Booking_ID;
  private static int Host_ID, Renter_ID;
  private static Connection connection = ConnectionEstablish.ConnectToJDBC.getMySqlConnection();
  private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private static Scanner scan = new Scanner(System.in);
  private static Statement st;
  private static ResultSet rs;
  private static String zipcode = null;
  private static String room_type = null;
  private static int type;
  private static String apt_name = null, city = null, country = null;
  private static int longitude;
  private static int latitude;
  public static int price;
  public static String startDate;
  public static String endDate;
  private static int wifi, washer, ac, kitchen, dryer;
  private static String[] room_types = {"Apartment", "House", "Room"};
  static Statement sql;

  /*
  Total number of bookings in a specific date range by city.
Total number of bookings in a specific date range by postal code within a city.
Total number of listings per country, per country and city, per country, city and postal code.
Rank the hosts by the total number of listings they have overall per country.
Rank the hosts by the total number of listings they have overall per city.

For every city and country a report should provide the hosts that have a number of listings that is more than 10% of the number of listings in that city and country.
Rank the renters by the number of bookings in a specific time period.
Rank the renters by the number of bookings in a specific time period per city.(renters that have made at least 2 booking in the year).
Hosts and renters with the largest number of cancellations within a year.
A report that presents for each listing the set of most popular noun phrases associated with the listing (noun phrases from review).
   */

  public static void dateRangeAndCity() throws SQLException, InterruptedException {

    System.out.print("Specify the date range\n");
    System.out.print("Start Date (YYYY-MM-DD): ");
    startDate = scan.nextLine();
    System.out.print("End Date (YYYY-MM-DD): ");
    endDate = scan.nextLine();

    String sqlQ = "select count(*), l.city from bookings as b join listings as l on l.listing_id = b.listing_id where start <= '" + startDate + "' and end >= '" + endDate + "' group by l.city\n";

    rs = sql.executeQuery(sqlQ);
    System.out.println("Total number of bookings between " + startDate + " and " + endDate + " in: ");
    while (rs.next()) {
      city = rs.getString("city");
      System.out.print(city + ": " + rs.getInt("count(*)"));
    }

  }

  public static void dateRangeAndCityAndPostalCode() throws SQLException, InterruptedException {
    System.out.print("Specify the date range\n");
    System.out.print("Start Date (YYYY-MM-DD): ");
    startDate = scan.nextLine();
    System.out.print("End Date (YYYY-MM-DD): ");
    endDate = scan.nextLine();

    String sqlQ = "select count(*), l.city, l.postal_code from bookings as b join listings as l on l.listing_id = b.listing_id where start <= '"+startDate+"' and end >= '"+endDate+"' group by l.city, l.postal_code order by l.city\n";
    rs = sql.executeQuery(sqlQ);
    ArrayList<String> cities = new ArrayList<String>();
    String cit;
    System.out.println("Total number of bookings between " + startDate + " and " + endDate + " in: ");
    while (rs.next()) {
      cit = rs.getString("city");
      if(cities.contains(cit)){
        System.out.print("Postal code: " + rs.getString("postal_code") + " is: " + rs.getInt("count(*)"));
      }
      else{
        System.out.println("--------------------------------------------------------------");
        System.out.println("City: "+ cit);
        cities.add(cit);
        System.out.print("Postal code: " + rs.getString("postal_code") + " is: " + rs.getInt("count(*)"));
      }
    }
  }

  public static void listingsPerCountryCityPostalCode() throws SQLException, InterruptedException {
    System.out.print("Enter 1 to get listings per Country, 2 for listings per country and city and 3 for listings per country and city and postal code: ");
    int choice = scan.nextInt();
    if (choice == 1) {
      String sqlQ = "select count(*) as per_country, l.country from listings l group by l.country\n";
      rs = sql.executeQuery(sqlQ);
      System.out.println("Total number of listings per country is: ") ;
      while (rs.next()) {
        country = rs.getString("country");
        System.out.print(country + ": " + rs.getInt("count(*)"));
      }
    } else if (choice == 2) {
      String sqlQ = "select count(*) as per_country_and_city, l.country, l.city from listings l group by l.country, l.city order by l.city\n";
      rs = sql.executeQuery(sqlQ);
      while (rs.next()) {
        country = rs.getString("country");
        city = rs.getString("city");
        System.out.print("Total number of listings for country " + country + " and city " + city + " is: " + rs.getInt("count(*)"));
      }
    } else {
      String sqlQ = "select count(*) as per_country_city_and_postal_code, l.country, l.city, l.postal_code from listings l group by l.country, l.city, l.postal_code\n";
      rs = sql.executeQuery(sqlQ);
      while (rs.next()) {
        country = rs.getString("country");
        city = rs.getString("city");
        zipcode = rs.getString("postal_code");
        System.out.print("Total number of listings for country " + country + " and city " + city + " with postal code " + zipcode + " is: " + rs.getInt("count(*)"));
      }
    }
  }


  public static void rankHosts() throws SQLException {
    System.out.print("Enter 1 to rank hosts by city and 2 to rank hosts by country: ");
    int choice = scan.nextInt();
    if (choice == 1) {
      String sqlQ = "select count(*), o.sin as Host_ID, l.city from listings l join owns o on l.listing_id = o.listing_id group by l.city, o.sin order by l.city, count(*)\n" +
              "desc\n";
      rs = sql.executeQuery(sqlQ);
      int i = 1;
      ArrayList<String> cities = new ArrayList<String>();
      String cit;
      while (rs.next()) {
        cit = rs.getString("city");
        if (cities.contains(cit)) {
          System.out.println(i + ". Host ID: " + rs.getString("host_id") + " with" + rs.getString("count(*)") + "listings.");
          i++;
        } else {
          System.out.println("-------------------------------------------------------------------------------");
          System.out.println("City: " + cit);
          cities.add(cit);
          i = 1;
          System.out.println(i + ". Host ID: " + rs.getString("host_id") + " with" + rs.getString("count(*)") + "listings.");
        }
      }
    } else {
      String sqlQ = "select count(*), o.sin as Host_ID, l.county from listings l join owns o on l.listing_id = o.listing_id group by l.country, o.sin order by l.country, count(*)\n" +
              "desc\n";
      rs = sql.executeQuery(sqlQ);
      int i = 1;
      ArrayList<String> countries = new ArrayList<String>();
      String con;
      while (rs.next()) {
        con = rs.getString("country");
        if (countries.contains(con)) {
          System.out.println(i + ". Host ID: " + rs.getString("host_id") + " with" + rs.getString("count(*)") + "listings.");
          i++;
        } else {
          System.out.println("-------------------------------------------------------------------------------");
          System.out.println("Country: " + con);
          countries.add(con);
          i = 1;
          System.out.println(i + ". Host ID: " + rs.getString("host_id") + " with" + rs.getString("count(*)") + "listings.");
        }
      }
    }

  }
}
