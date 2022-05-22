package ITM_ERP_package;
import java.sql.*;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
	public static void main(String[] args) {
		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch(ClassNotFoundException e) {
			System.out.println("Driver not found");
			return;
		}
		
		//System.out.println("MySQL JDBC Driver Registered!");
		Connection connection = null;
		
		try {
			connection = DriverManager.getConnection("jdbc:mysql://localhost/itm_erp_db","root","123");
			//System.out.println("Connection with database successful");
		}
		catch(SQLException e)
		{
			System.out.println("Connexion Failed! Check output console");
			return;
		}
		
		boolean exit = false;
		Scanner scanner = new Scanner(System.in);
		while(exit==false) {
			
			System.out.println("\nPlease enter one of the following command:");
			System.out.println("    - Create new customer");
			System.out.println("    - Create new product");
			System.out.println("    - Create new sale");
			System.out.println("    - Search product");
			System.out.println("    - Total sales by postal code");
			System.out.println("    - Quit");
			String userInput = scanner.nextLine();
						
			if(userInput.equals("quit")){
				exit = true;
			}
			else {
				userInputAnalysis(userInput, connection, scanner);
			}
		}
		scanner.close();
	}
	
	/*
	 * Analyze the text encoded by the user and respond appropriately
	 * 
	 * Parameter:
	 * @param: userInput: the string containing the command the user wants to use
	 * @param: connection: the connection to the database
	 * 
	 */
	public static void userInputAnalysis(String userInput, Connection connection, Scanner scanner)
	{
		String[] words = userInput.split(" ");
		if(words[0].toLowerCase().equals("create"))
		{
			try {
				if(words[2].toLowerCase().equals("customer"))
				{
					createNewCustomer(connection, scanner);
				}
				else if(words[2].toLowerCase().equals("product"))
				{
					createNewProduct(connection, scanner);
				}
				else if(words[2].toLowerCase().equals("sale"))
				{
					createNewSale(connection, scanner);
				}
				else { //the user did not write a correct word
					System.out.println("your third word must be customer or product or sale");
				}
			} catch(Exception e)
			{
				System.out.println("Please write a correct command");
			}
		}
		
		else if (words[0].toLowerCase().equals("search"))
		{
			try {
				if(words[1].toLowerCase().equals("product"))
				{
					searchProduct(connection,scanner);
				}
				else { //the user did not write a correct word
					System.out.println("your second word must be product");
				}
			} catch(Exception e)
			{
				System.out.println("Incorrect command: Please write what you are looking for");
			}
		}
		else if  (userInput.toLowerCase().equals("total sales by postal code")) {
			TotalSalesperPostalCode(connection); 
		}
		else 
		{
			System.out.println("Please write a valid command");
		}	
	}
	
	/*
	 * Create a new customer in the database. Check also all mistakes the user might do
	 * 
	 * Parameter:
	 * @param: connection: the connection to the database
	 */
	public static void createNewCustomer(Connection connection, Scanner scanner)
	{
		System.out.println("Creation of a new customer :"); 
		
		String lastname, forename;
		int idCustomer;
		
		do { 
			System.out.println("Please enter the lastname");
			lastname = validateNotEmptyString(scanner);
			
			System.out.println("Please enter the forename");
			forename = validateNotEmptyString(scanner);
			
			idCustomer = getCustomerId(connection, lastname, forename); //if the customer already exists, show message
			if(idCustomer != -1) {
				System.out.println("This customer already exists");
			}
		}
		while(idCustomer != -1); //while the entered new customer already exists, keep asking
		
		
		System.out.println("Please enter the street name");
		String streetname = validateNotEmptyString(scanner);
		
		System.out.println("Please enter the house number");
		int housenumber = validatePositiveNumber(scanner);
		
		System.out.println("Please enter the city");
		String city = validateNotEmptyString(scanner);
		
		System.out.println("Please enter the postal code");
		int postalcode = validatePositiveNumber(scanner);
		
		System.out.println("Please enter the email address");
		String email = scanner.nextLine();
		while(!isValidEmailAddress(email)) {
			System.out.println("You must enter a valid email adress");
			email = scanner.nextLine();
		}
		
		System.out.println("Please enter the credit limit");
		int creditlimit = validatePositiveNumber(scanner);
		
		if(connection !=null) {
			try {
				String query = "INSERT INTO customers(lastname, forename, streetname, housenumber, city, postalcode, email, creditlimit) VALUES (?,?,?,?,?,?,?,?);";
				PreparedStatement pstmt = connection.prepareStatement(query);
				pstmt.setString(1, lastname);
				pstmt.setString(2, forename);
				pstmt.setString(3, streetname);
				pstmt.setInt(4, housenumber);
				pstmt.setString(5, city);
				pstmt.setInt(6, postalcode);
				pstmt.setString(7, email);
				pstmt.setInt(8, creditlimit);
				pstmt.executeUpdate();
				System.out.println("Customer successfully registered");
			}
			catch(Exception e) {
				System.out.println("An error occured");
			}
		}
	}
	
	/*
	 * Create a new product in the database. Check also all mistakes the user might do
	 * 
	 * Parameter:
	 * @param: connection: the connection to the database
	 */
	public static void createNewProduct(Connection connection, Scanner scanner)
	{		
		System.out.println("Creation of a new product :"); 
		
		System.out.println("Please enter the name");
		String name = validateNotEmptyString(scanner);
		
		String code;
		do {
			System.out.println("Please enter the code (max 6 characters)");
			code = scanner.nextLine();
			if(productExists(connection,code))
				System.out.println("This code product already exists in the database");
		} while(productExists(connection,code));
		
        System.out.print("Please enter the selling price: \n");
		int sellingprice = validatePositiveNumber(scanner);

		System.out.println("Please enter the stock level");
		int stocklevel = validatePositiveNumber(scanner);
		
		if(connection !=null) {
			try {
						
				String query = "INSERT INTO products(name, code, sellingprice, stocklevel) VALUES (?, ?, ?, ?);";
				PreparedStatement pstmt = connection.prepareStatement(query);
				pstmt.setString(1, name);
				pstmt.setString(2, code);
				pstmt.setInt(3, sellingprice);
				pstmt.setInt(4, stocklevel);
				pstmt.executeUpdate();
				System.out.println("Product successfully registered");
			}
			catch(Exception e) {
				System.out.println("An error occured");
			}
		}
	}
	
	/*
	 * Create a new sale in the database. Check also all mistakes the user might do.
	 * 
	 * Parameter:
	 * @param: connection: the connection to the database.
	 */
	public static void createNewSale(Connection connection, Scanner scanner) throws SQLException
	{	
		//Creation of the sale
		System.out.println("Creation of a new sale :"); 
		
		String lastname, forename;
		boolean userExists = false;
		int idcustomer;
		
		do {
			System.out.println("Please enter the lastname");
			lastname = validateNotEmptyString(scanner);
			System.out.println("Please enter the forename");
			forename = validateNotEmptyString(scanner); 
			
			idcustomer = getCustomerId(connection, lastname, forename);
			if(idcustomer != -1)
				userExists = true;
			else {
				System.out.println("Customer not found in the database");
			}
		}
		while(!userExists); //as long as the customer does not exists, keep asking
		
		//Addition of the different products and quantities
		Hashtable<String, Integer> shoppingBasket = new Hashtable<String, Integer>();
		Set<String> keys = shoppingBasket.keySet();
		boolean orderIsFinished = false; 
		String codeproduct;
		int quantity=0, totalamount =0;
		int creditlimit = getCreditLimit(connection,idcustomer);
		do {
			do {
				System.out.println("Please enter the code of the product");
				codeproduct = scanner.nextLine();
			}
			while(!productExists(connection, codeproduct)); //if the product doesn't exist in the database, keep asking
			
			do {
				System.out.println("Please enter the quantity of the product. If you want to cancel this purchase, please write cancel");
				if(scanner.hasNextInt()) {
					quantity = validatePositiveNumber(scanner);
				} else if(scanner.nextLine().equals("cancel")) {
					quantity =0;
					break;
				}
			}
			while(!quantitySuperiorToZero(quantity) || !quantityInferiorToStockLevel(connection, quantity, codeproduct) 
					|| CreditLimitExceeded(creditlimit, getSellingPrice(connection, codeproduct), quantity)); // if one condition of the business logic is not respected
			
			boolean existsInBasket = false; 
			for(String key: keys){ //verification if the product encoded is already present in the shopping basket
				if (key.equals(codeproduct) ) {
					existsInBasket = true; 
					int oldQuantity = shoppingBasket.get(key); 
					int newQuantity = oldQuantity + quantity;
					shoppingBasket.replace(codeproduct, newQuantity);
				}
	        }
			if (!existsInBasket && (quantity !=0))
				shoppingBasket.put(codeproduct, quantity);
			
			creditlimit -= quantity*getSellingPrice(connection, codeproduct);
			
			//show the shopping basket
			int price =0;
			totalamount = 0;
			System.out.println("_______________________________________________");
			System.out.printf("%-25s%-10s%-10s\n", "name", "quantity","total price");
			
	        for(String key: keys){
	        	price = shoppingBasket.get(key)*getSellingPrice(connection, key);
	            System.out.printf("%-25s%-10s%-10s\n", key, shoppingBasket.get(key), price);
	            totalamount += price ; 
	        }
	        System.out.println("_______________________________________________");
	        System.out.println("Total amount "+ totalamount +"€");
			 
	        String buyMore="";
			do{
				System.out.println("Do you want to buy more products? yes or no");
				buyMore = scanner.nextLine();
				if(buyMore.equals("no"))
					orderIsFinished =true;
			}while(!(buyMore.equals("yes") || buyMore.equals("no")));
			
		}
		while(!orderIsFinished);
		
		System.out.println("Do you confirm your sale? yes or no");
		String confirmation = scanner.nextLine();
		if(confirmation.equals("yes")) {
			//insert new sale
			int idsale = insertNewSale(connection,idcustomer);
			
			//insert details 
			for(String key: keys){
				insertNewDetail(connection,idsale, key, shoppingBasket.get(key));
				//update stocklevel 
				updateStockLevel(connection,key, shoppingBasket.get(key));
	        }			
			//update credit limit
			updateCreditLimit(connection,idcustomer,totalamount);
		}
	}
	
	/*
	 * Search a product in the database. The user can also choose to see one or several product, 
	 * ordered or not by name or price and see the result in ascending or descending order
	 * 
	 * Parameter:
	 * @param: connection: the connection to the database
	 * @param: scanner: the scanner asking the user to encode commands
	 */
	public static void searchProduct(Connection connection, Scanner scanner) 
	{
		System.out.println("Search for one or more products :"); 
		
		System.out.println("Do you want to see all the products ? yes or no");
		String allProducts = validateNotEmptyString(scanner);
		
		String name =""; 
		if (allProducts.equals("no")) {
			System.out.println("Please enter the name of the product");
			name = validateNotEmptyString(scanner);
			if(!productNameExists(connection, name)) {
				System.out.println("No products were found in the database with this name");
				return;
			}
		}
		
		System.out.println("Please enter if you want to sort the data ? yes or no"); 
		String sorted = validateNotEmptyString(scanner); 
		
		String choice_way="";
		String choice_by="";
		if (sorted.contentEquals("yes")) {
			System.out.println("Please enter your choice (1 or 2) : \n 1) order by product name \n 2) order by selling price");
			choice_by = validateNotEmptyString(scanner);
			
			System.out.println("Please enter your choice (1 or 2) : \n 1) in ascending order  \n 2) in descending order");
			choice_way = validateNotEmptyString(scanner);
		}
		
		if (connection != null)	{
			try {
				Statement stmt =connection.createStatement();
				ResultSet rs;
				String querySelect = "SELECT `name`,`code`, `sellingprice`,`stocklevel` FROM `products`";
				if (allProducts.equals("no")) {
					querySelect += "WHERE `name` LIKE '%"+name+"%'";
				} 
				
				if(choice_by.contentEquals("1")) {
					querySelect += "ORDER BY `name`";
				}
				else if(choice_by.contentEquals("2")) {
					querySelect += " ORDER by `sellingprice`"; 				
				}	
				
				if(choice_way.contentEquals("1")) {
					querySelect += " ASC;";
				}
				else if(choice_way.contentEquals("2")) {
					querySelect += " DESC;"; 
					
				}else {
					querySelect += ";";
				}
												
				rs = stmt.executeQuery(querySelect);
				System.out.println("_____________________________________________________________");
				System.out.printf("%-25s%-10s%-15s%-10s\n", "name", "code", "selling price","stock level");
				while ( rs.next() ) {
					String product = rs.getString("name");
	                String code = rs.getString("code");
	                int sellingprice = rs.getInt("sellingprice");
	                int stocklevel = rs.getInt("stocklevel");
	                System.out.printf("%-25s%-10s%-15s%-10s\n", product, code, sellingprice, stocklevel);
				}
				System.out.println("_____________________________________________________________");
			}
				
			catch(Exception e) {
				System.out.println("An error occured");
			}
		}	
	}
	
	/*
	 * Enables the user to see a dataframe containing all postal code and the total sales spend by 
	 * customers living in these areas. The results also show where customers have not bought anything
	 * 
	 *  Parameter:
	 *  @param: connection: the connection to the database
	 */
	public static void TotalSalesperPostalCode(Connection connection) 
	{
		try {
			Statement stmt = connection.createStatement();
			ResultSet rs;
			String sql = "SELECT customers.postalcode, SUM(details.quantity*products.sellingprice) AS totalsales "
					+ "FROM sales, products, customers, details "
					+ "WHERE customers.id = sales.idcustomer and products.code = details.codeproduct and sales.id = details.idsale "
					+ "GROUP BY customers.postalcode "
					+ "UNION "
					+ "SELECT customers.postalcode, 0 "
					+ "FROM customers "
					+ "WHERE postalcode not in (SELECT customers.postalcode "
											 + "FROM sales, customers "
											 + "WHERE customers.id = sales.idcustomer)"
					+ "ORDER BY totalsales DESC;";
			
			rs = stmt.executeQuery(sql);
			System.out.println("______________________________________________");
			System.out.printf("%-15s%-10s\n", "postal code", "total sales");
			while ( rs.next()) {
				int postalCode = rs.getInt("postalcode");
				int totalSales = rs.getInt("totalsales"); 
                System.out.printf("%-15s%-10s\n", postalCode, totalSales);
			}
			System.out.println("______________________________________________");
				
		} catch (SQLException e) {
			System.out.println("An error occured");
			e.printStackTrace();
		}
	}
	
	/*
	 * Return the id of a customer based on his lastname and forename. 
	 * Return -1 if the customer is not in the database
	 * 
	 * Parameter:
	 * @param connection: the connection to the database
	 * @param lastname: the lastname of the customer
	 * @param forename: the forename of the customer
	 * 
	 * Return:
	 * @return id: the id of the customer, -1 if the customer does not exists in the database
	 */
	public static int getCustomerId(Connection connection, String lastname, String forename)
	{
		int id = -1;
		try {
			Statement stmt = connection.createStatement();
			String sql = "SELECT id FROM customers WHERE lastname = '" + lastname + "' AND forename = '" + forename + "';";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
			    id = rs.getInt("id");
			}
			return id;
		}
		catch(Exception e) {
			return -1;
		}
	}
	
	/*
	 * Check if the quantity is superior to 0
	 * 
	 * Parameter:
	 * @param quantity: the quantity the customer is bying 
	 * 
	 * Return:
	 * @return boolean: True if the quantity is superior to 0, false otherwise
	 */
	public static boolean quantitySuperiorToZero(int quantity)
	{
		if(quantity <=0)
			System.out.println("The quantity must be superior to 0");
		return (quantity>0);
	}
	
	/*
	 * Return true if the quantity of the product is superior to the stock level of this product in the database.
	 * 
	 * Parameter:
	 * @param connection: the connection to the database
	 * @param quantity: the quantity the customer wants to buy
	 * @param codeproduct: the code of the product the customer wants to buy
	 * 
	 * Return:
	 * @return boolean: True if quantity superior to stock level of the product, false otherwise
	 */
	public static boolean quantityInferiorToStockLevel(Connection connection, int quantity, String codeproduct)
	{
		try {
			int stocklevel = 0; 
			Statement stmt =connection.createStatement();
			ResultSet rs;
			String sql = "SELECT stocklevel FROM products WHERE code = '"+ codeproduct + "';";
			rs = stmt.executeQuery(sql);
			while ( rs.next() ) {
				stocklevel = rs.getInt("stocklevel"); 
	        }
			if(quantity>stocklevel)
				System.out.println("The quantity is superior to the stocklevel (max: " + stocklevel + ")"); 
			return (quantity<=stocklevel);
		} 
		catch(Exception e) {
			System.out.println(e);
			return false;
		}
	}
	
	 /*
	  * Return True if a product exists in the database, false otherwwise
	  * 
	  * Parameters:
	  * @param connection: the connection to the database
	  * @param codeproduc: the code of the product the customer wants to buy
	  * 
	  * Return:
	  * @return boolean: True if the product exists in the database, false otherwise
	  */
	public static boolean productExists(Connection connection, String codeproduct)
	{
		try {
			Statement stmt = connection.createStatement();
			String sql = "SELECT code FROM products WHERE code = '" + codeproduct + "';";
			ResultSet rs = stmt.executeQuery(sql);
			int count = 0;
			while (rs.next()) {
				count++;
			}
			return (count==1);
		}
		catch(Exception e) {
			System.out.println(e);
			return false;
		}
	}
	
	/*
	 * Return the credit limit of a customer based on his ID
	 * 
	 * Parameter:
	 * @param connection: the connection to the database
	 * @param idcustomer: the id of the customer 
	 * 
	 * Return:
	 * @return creditLimit: the credit limit of the customer with the corresponding ID
	 */
	public static int getCreditLimit(Connection connection, int idcustomer)
	{
		int creditLimit = 0;
		try {
			Statement stmt = connection.createStatement();
			String sql = "SELECT creditlimit FROM customers WHERE id = '" + idcustomer + "';";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
			    creditLimit = rs.getInt("creditlimit");
			}
			return creditLimit;
		}
		catch(Exception e) {
			System.out.println(e);
			return creditLimit;
		}
	}
	
	/*
	 * Return the selling price of a product base on its code
	 * 
	 * Parameter:
	 * @param connection: the connection to the database
	 * @param codeproduct: the code of the product
	 * 
	 * Return:
	 * @return sellingprice: the selling price of the product with the corresponding code
	 */
	public static int getSellingPrice(Connection connection, String codeproduct)
	{
		int sellingprice = 0;
		try {
			Statement stmt = connection.createStatement();
			String sql = "SELECT sellingprice FROM products WHERE code = '" + codeproduct + "';";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				sellingprice = rs.getInt("sellingprice");
			}
			return sellingprice;
		}
		catch(Exception e) {
			System.out.println(e);
			return sellingprice;
		}
	}
	
	/*
	 * Return true if the credit limit is inferior to the selling price of a product times the quantity of this product
	 * 
	 * Parameters:
	 * @param creditlimit: the updated credit limit of the customer (current credit limit minus current purchases already present in the basket)
	 * @param sellingprice: the selling price of the product
	 * @param: quantity: the quantity desired by the customer of the product 
	 * 
	 * Return:
	 * @return boolean: True if the credit limit is exceeded, false otherwise
	 */
	public static boolean CreditLimitExceeded(int updatedCreditlimit, int sellingprice, int quantity) {
		if(updatedCreditlimit<sellingprice*quantity)
			System.out.println("The credit limit ("+ updatedCreditlimit+") must be superior to the total amount ("+sellingprice*quantity+")");
		return(updatedCreditlimit<sellingprice*quantity);
	}
	
	/*
	 * Return the id of a new sales if everything worked well, -1 otherwise
	 * 
	 * Parameter: 
	 * @param connection: the connection to the database
	 * @param idcustomer: the ID of the customer that wants to buy something
	 * 
	 * Return:
	 * @return: idsale: the ID of the new sale created, -1 if an error occured during the process
	 */
	public static int insertNewSale(Connection connection, int idcustomer ) {
		int idsale = -1; 
			try {
				String query = "INSERT INTO sales(idcustomer) VALUES (?);";
				PreparedStatement pstmt = connection.prepareStatement(query);
				pstmt.setInt(1, idcustomer);
				pstmt.executeUpdate();
				System.out.println("Sales successfully registered");
				
				Statement stmt = connection.createStatement();
				String queryId =  "SELECT id FROM sales WHERE idcustomer = '" + idcustomer + "';";
				ResultSet rs = stmt.executeQuery(queryId);
				while (rs.next()) {
					idsale = rs.getInt("id");
				}
				return idsale;	
			}
			catch(Exception e) {
				System.out.println("An error occured");
				return idsale; 
			}
	}
	
	/*
	 * Insert a new detail of a sale in the database
	 * 
	 * Parameters:
	 * @param connection: the connection to the database
	 * @param idsale: the ID of the sale that which is going to be composed of the new created detail
	 * @param codeproduct: the code of the product of the detail
	 * @param quantity: the quantity bought of the product
	 */
	public static void insertNewDetail(Connection connection,int idsale, String codeproduct, int quantity ) {
		try {
			String query = "INSERT INTO details(idsale, codeproduct, quantity) VALUES (?, ?,?);";
			PreparedStatement pstmt = connection.prepareStatement(query);
			pstmt.setInt(1, idsale);
			pstmt.setString(2, codeproduct);
			pstmt.setInt(3, quantity);
			pstmt.executeUpdate();
			System.out.println("Details for the purchased product " +codeproduct +" successfully registered");
		}
		catch(Exception e) {
			System.out.println("An error occured");
			e.printStackTrace();
		}
	}
	
	/*
	 * Update the stock level when a product has been bought
	 * 
	 * Parameters:
	 * @param connection: the connection to the database
	 * @param codeproduct: the code of the product that needs to be updated
	 * @param quantity: the quantity of the product
	 */
	public static void updateStockLevel(Connection connection, String codeproduct, int quantity ) {
		try {
			String query = "UPDATE `products` SET`stocklevel`= `stocklevel`- ? WHERE `code` = ?;";
			PreparedStatement pstmt = connection.prepareStatement(query);
			pstmt.setInt(1, quantity);
			pstmt.setString(2, codeproduct);
			pstmt.executeUpdate();
			System.out.println("StockLevel successfully updated");
		}
		catch(Exception e) {
			System.out.println("An error occured");
			e.printStackTrace();
		}
	}
	
	/*
	 * Update the credit limit of a customer who bought a product
	 * 
	 * Parameters:
	 * @param connection: the connection to the database
	 * @param idcustomer: the ID of the customer
	 * @param totalamount: the total amount that needs to be reduced from the credit limit of the customer
	 */
	public static void updateCreditLimit(Connection connection, int idcustomer, int totalamount ) {
		try {
			String query = "UPDATE `customers` SET`creditlimit`= `creditlimit`- ? WHERE `id` = ?;";
			PreparedStatement pstmt = connection.prepareStatement(query);
			pstmt.setInt(1, totalamount);
			pstmt.setInt(2, idcustomer);
			pstmt.executeUpdate();
			System.out.println("CreditLimit successfully updated");
		}
		catch(Exception e) {
			System.out.println("An error occured");
			e.printStackTrace();
		}
	}
	
	/*
	 * Return true if the email encoded is a valid email, false otherwise
	 * 
	 * Parameter:
	 * @param email: The String containing the email encoded
	 * 
	 * Return
	 * @return validEmail: True if the email is valid, false otherwise
	 */
	public static boolean isValidEmailAddress(String email) {
        String regexEmail = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
        Pattern pattern = Pattern.compile(regexEmail, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        boolean validEmail = matcher.find();
        return validEmail;
    }
	
	/*
	 * keep asking the user to enter a valid positive number
	 * 
	 * Parameter:
	 * @param: scanner: the scanner asking the user to encode commands
	 * 
	 * Return:
	 * @return number: a positive number
	 */
	public static int validatePositiveNumber(Scanner scanner) {
        int number;
        do {
            while (!scanner.hasNextInt()) {
                String input = scanner.next();
                System.out.printf("\"%s\" is not a valid number.\n", input);
            }
            number = scanner.nextInt();
            if(number<0)
            	System.out.println("The encoded number must be positive");
        } while (number < 0);
        scanner.nextLine();
        return number;
    }
	
	/*
	 * Return the string encoded by the user. This String is certified not empty
	 * 
	 * Parameter:
	 * @param scanner: the scanner asking the user to encode commands
	 * 
	 * Return:
	 * @return string: the String certified not empty
	 */
	public static String validateNotEmptyString(Scanner scanner) {
		String string; 
		do {
			string = scanner.nextLine();
			if(string.isEmpty()|| string.equals(" "))
				System.out.println("Please enter something");
		} while(string.isEmpty());
		return string;
	}
	
	/*
	 * Return true if at least one product of the database has a name containing the word, false otherwise
	 * 
	 * Parameter:
	 * @param connection: the connection to the database
	 * @param name: the word to look for in the database. It can either be at the beginning or in the middle of names of products
	 * 
	 * Return:
	 * @return boolean: true if at least one product of the database has a name containing the word, false otherwise
	 */
	public static boolean productNameExists(Connection connection, String name)
	{
		try {
			Statement stmt = connection.createStatement();
			String sql = "SELECT `name` FROM `products` WHERE `name` LIKE '%"+name+"%'";
			ResultSet rs = stmt.executeQuery(sql);
			int count = 0;
			while (rs.next()) {
				count++;
			}
			return (count>=1);
		}
		catch(Exception e) {
			System.out.println(e);
			return false;
		}
	}
	
}
	