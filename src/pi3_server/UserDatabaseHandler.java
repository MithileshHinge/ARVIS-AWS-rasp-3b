package pi3_server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserDatabaseHandler {

	private static final String DATABASE_NAME = "UserDatabase.db", TABLE_NAME = "users";
	String url = "jdbc:sqlite:" + DATABASE_NAME;
	
	UserDatabaseHandler(){
		String createTable = " CREATE TABLE IF NOT EXISTS "+ TABLE_NAME +" (\n"
							+" id integer PRIMARY KEY, \n"
							+" hashid text, \n"
							+" username text, \n"
							+" password text, \n"
							+" email text \n"
							+" );";
							
		try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement()){
			if (conn != null){
				stmt.execute(createTable);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public boolean addSystem(String hashID){
		String insert = "INSERT INTO " + TABLE_NAME + "(hashid, username, password, email) VALUES(?,?,?,?)";
		 
        try (Connection conn = DriverManager.getConnection(url); PreparedStatement pstmt = conn.prepareStatement(insert)) {
            pstmt.setString(1, hashID);
            pstmt.setString(2, null);
            pstmt.setString(3, null);
            pstmt.setString(4, null);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
		return true;
	}
	
	public boolean checkRegistered(String hashID){
		try (Connection conn = DriverManager.getConnection(url)){
			String checkRegistered = "SELECT * FROM " + TABLE_NAME + " WHERE hashid = ?;";
			PreparedStatement pst = conn.prepareStatement(checkRegistered);
			pst.setString(1, hashID);
			
			ResultSet rs = pst.executeQuery();
			if (rs.next()){
				String username = rs.getString("username");
				if (username != null){
					return true;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean registerUser(String username, String password, String hashID, String email){
		String register = "UPDATE " + TABLE_NAME + " SET username = ? , password = ? , email = ? WHERE hashid = ?";
		try(Connection conn = DriverManager.getConnection(url)){
			PreparedStatement pst = conn.prepareStatement(register);
			pst.setString(1, username);
			pst.setString(2, password);
			pst.setString(3, email);
			pst.setString(4, hashID);
			
			pst.executeUpdate();
		} catch (SQLException e){
			e.printStackTrace();
		}
		return true;
	}
	
	public boolean verifyUser(String username, String password, String hashID){
		try (Connection conn = DriverManager.getConnection(url)){
			String checkRegistered = "SELECT * FROM " + TABLE_NAME + " WHERE hashid = ?;";
			PreparedStatement pst = conn.prepareStatement(checkRegistered);
			pst.setString(1, hashID);
			
			ResultSet rs = pst.executeQuery();
			if (rs.next()){
				String usernameFetched = rs.getString("username");
				String passwordFetched = rs.getString("password");
				if (usernameFetched.equals(username) && passwordFetched.equals(password)){
					return true;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		
		return false;
	}
	
	public String getHashID(String username, String password){
		try (Connection conn = DriverManager.getConnection(url)){
			String checkRegistered = "SELECT * FROM " + TABLE_NAME + " WHERE username = ? AND password = ?;";
			PreparedStatement pst = conn.prepareStatement(checkRegistered);
			pst.setString(1, username);
			pst.setString(2, password);
			
			ResultSet rs = pst.executeQuery();
			if (rs.next()){
				return rs.getString("hashid");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public String getEmail(String hashid){
		try (Connection conn = DriverManager.getConnection(url)){
			String checkRegistered = "SELECT * FROM " + TABLE_NAME + " WHERE hashid = ?;";
			PreparedStatement pst = conn.prepareStatement(checkRegistered);
			pst.setString(1, hashid);
			
			ResultSet rs = pst.executeQuery();
			if (rs.next()){
				return rs.getString("email");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public String[] getEntryFromHashID(String hashid){
		try (Connection conn = DriverManager.getConnection(url)){
			String checkRegistered = "SELECT * FROM " + TABLE_NAME + " WHERE hashid = ?;";
			PreparedStatement pst = conn.prepareStatement(checkRegistered);
			pst.setString(1, hashid);
			
			ResultSet rs = pst.executeQuery();
			if (rs.next()){
				return new String[]{rs.getString("hashid"), rs.getString("username"), rs.getString("password"), rs.getString("email")};
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}
}
