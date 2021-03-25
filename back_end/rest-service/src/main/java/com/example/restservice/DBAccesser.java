package com.example.restservice;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Scanner;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
//import mysql;

public class DBAccesser {
	private Connection conn;

	DBAccesser(){
		getConnection();
	}


	public void getConnection() { 
		Connection conn = null;
		
		try {
		    String driver ="com.mysql.cj.jdbc.Driver";

			String url = "jdbc:mysql://localhost:3308/pog";
		    String user      = "root";
		    String password  = "dataLynx5!";
		 	Class.forName(driver);
		    // create a connection to the database
		    conn = DriverManager.getConnection(url, user, password);
		} 
		catch(SQLException e){
			System.out.println("Something went wrong1");
			System.out.println(e.getMessage());
		}
		catch(Exception e) { //SQLException e,
			System.out.println("Something went wrong");
		   System.out.println(e.getMessage());
		} 
		
		finally {
			System.out.println("finally");
			this.conn = conn;
		}
		
	}

	public void closeconnection() {
		if(conn != null){
        try {
			this.conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}}
	}

	public void getPaperRow(int idNumber, GSData gsd){
		//GSData gsd = new GSData();
		//String[] authors = {};
		//String[] links = {};
		//int cited_by_count = -1;
		String cited_by_url = "";
		String doc_url = "";
		int id = -1;
		String source_url = "";
		String summary = "";
		String title = "";
		int year = -1;

		try {
			String sql = "SELECT * FROM pog.papers where papers.paperid = ?";
			PreparedStatement stmt = this.conn.prepareStatement(sql);
			stmt.setInt(1, idNumber);
			ResultSet rs = stmt.executeQuery();

			while(rs.next()) {
				id = rs.getInt("paper_id");
				title = rs.getString("title");
				year = rs.getInt("year");
				doc_url = rs.getString("doc_url");
				source_url = rs.getString("source_url");
				summary = rs.getString("summary");
				cited_by_url = rs.getString("cited_by_url");
			}
			gsd = new GSData(title, year);
			gsd.id = id;
			gsd.summary = summary;
			gsd.doc_url = doc_url;
			gsd.source_url = source_url;
			gsd.cited_by_url = cited_by_url;
		}
		catch (SQLException e){
			e.printStackTrace();
		}
		finally {
			return;
		}

	}

	public void getCitedCount(int idNumber, GSData gsd){

		int cited_by_count = -1;


		try {
			String sql = "SELECT count(*) AS count FROM pog.cited_by WHERE cited_by.cited = ?";
			PreparedStatement stmt = this.conn.prepareStatement(sql);
			stmt.setInt(1, idNumber);
			ResultSet rs = stmt.executeQuery();

			while(rs.next()) {
				gsd.cited_by_count = rs.getInt("count");
			}
		}
		catch (SQLException e){
			e.printStackTrace();
		}
		finally {
			return;
		}

	}

	public Integer[] getCitedIds(int idNumber){

		ArrayList<Integer> cids = new ArrayList<Integer>();


		try {
			String sql = "SELECT cited_by.citer AS citer_id FROM pog.cited_by WHERE cited_by.cited = ?";
			PreparedStatement stmt = this.conn.prepareStatement(sql);
			stmt.setInt(1, idNumber);
			ResultSet rs = stmt.executeQuery();

			while(rs.next()) {
				cids.add(new Integer(rs.getInt("citer_id")));
			}
		}
		catch (SQLException e){
			e.printStackTrace();
		}
		finally {
			System.out.println("here");
			//Integer[] temp = ((Integer[]) cids.toArray());
			Integer[] temp = new Integer[cids.size()];
			for (int i = 0; i < cids.size(); i++){
				temp[i]  = cids.get(i);

			}
			return temp;
		}

	}

	public void getAuthors(int idNumber, GSData gsd){
		ArrayList<String> authors = new ArrayList<String>();
		ArrayList<String> author_urls = new ArrayList<String>();
		try {
			String sql = "SELECT * FROM pog.authored_by JOIN pog.authors ON authored_by.author_id = authors.author_id where authored_by.paper_id = ?;";
			PreparedStatement stmt = this.conn.prepareStatement(sql);
			stmt.setInt(1, idNumber);
			ResultSet rs = stmt.executeQuery();
			//rs.
			while(rs.next()) {
				authors.add(rs.getString("author_fname") + " " + rs.getString("author_lname"));
				author_urls.add(rs.getString("author_url"));
			}
		}
		catch (SQLException e){
			e.printStackTrace();
		}
		finally {
			//String[] author_temp = new String[authors.size()];
			//String[] author_url_temp = new String[author_urls.size()];
			//gsd.authors = (String[]) authors.toArray();
			//gsd.author_urls = (String[])  author_urls.toArray();
			gsd.authors = authors;
			gsd.author_urls = author_urls;
			/*for(int i = 0; i < authors.size(); i++){
				gsd.authors.add(authors.get(i));
				gsd.author_urls.add(author_urls.get(i));
			}*/

			return;
		}
	}

	public void insertNewEntry(GSData gsd) {
		//String queryUrlPre = "https://scholar.google.com/scholar?hl=en&as_sdt=0%2C43&q=";
		//String queryUrlPost = "&btnG=";
		int paper_id = -1;
		
		
		//Insert new paper w/ authors
		try {
			
			String sql = "INSERT INTO pog.papers (query_url, title, year, doc_url, source_url, summary, cited_by_url) VALUES (?, ?, ?, ?, ?, ?, ?);";
			PreparedStatement stmt = this.conn.prepareStatement(sql);
			
			//Add Query Url to insert statement
			//String url = gsd.makeQueryUrl();
			String url = gsd.queryUrl;
			if(url.length() > 300) {
				url = url.substring(0, 300);
			}
			stmt.setString(1, url);
			
			//Add title to insert statement
			String title = gsd.title;
			if(title.length() > 256) {
				title = title.substring(0, 256);
			}
			stmt.setString(2, title);
			
			//Add year to insert statement
			stmt.setInt(3, gsd.year);
			//Add document url to insert statement
			stmt.setString(4, gsd.makeDocUrl());
			stmt.setString(5, gsd.makeSourceUrl());
			String summary = gsd.summary;
			if(summary.length() > 350) {
				summary = gsd.summary.substring(0, 350);
			}
			stmt.setString(6, gsd.summary);
			stmt.setString(7, gsd.makeCitedByUrl());
			stmt.executeUpdate();
			
			String sql3 = "SELECT paper_id FROM pog.papers WHERE title = ? AND year = ?";
			PreparedStatement stmt3 = this.conn.prepareStatement(sql3);
			stmt3.setString(1, gsd.title);
			stmt3.setInt(2, gsd.year);
			ResultSet rs = stmt3.executeQuery();
			
			while(rs.next()) {
				paper_id = rs.getInt("paper_id");
			}
			
			String sql2 = "INSERT INTO pog.authors (author_url, author_fname, author_lname) VALUES (?, ?, ?)";
			PreparedStatement stmt2 = conn.prepareStatement(sql2);
			
			String sql4 = "SELECT author_id FROM pog.authors WHERE author_fname = ? AND author_lname = ?";
			PreparedStatement stmt4 = conn.prepareStatement(sql4);
			
			String sql5 = "INSERT INTO pog.authored_by (paper_id, author_id) VALUES (?, ?)";
			PreparedStatement stmt5 = conn.prepareStatement(sql5);
			
			String fname = "";
			String lname = "";
			int author_id = -1;
			for(String author : gsd.authors) {
				fname = author.substring(0, author.indexOf(" "));
				lname = author.substring(author.indexOf(" ")+1);
				stmt2.setString(1, "https://scholar.google.com/citations?user="+fname+lname+"&hl=en&oi=sra");
				stmt2.setString(2, fname);
				stmt2.setString(3, lname);
				stmt2.executeUpdate();
				
				stmt4.setString(1, fname);
				stmt4.setString(2, lname);
				ResultSet rs2 = stmt4.executeQuery();
				while(rs2.next()) {
					author_id = rs2.getInt("author_id");
				}
				stmt5.setInt(1, paper_id);
				stmt5.setInt(2, author_id);
				stmt5.executeUpdate();
			}
			int[] citer_ids = new int[gsd.citers.length];
			int i = 0;
			for(GSData citer : gsd.citers) {
				url = citer.makeQueryUrl();
				if(url.length() > 300) {
					url = url.substring(0, 300);
				}
				stmt.setString(1, url);
				title = citer.title;
				if(title.length() > 256) {
					title = title.substring(0, 256);
				}
				stmt.setString(2, title);
				stmt.setInt(3, citer.year);
				stmt.setString(4, citer.makeDocUrl());
				stmt.setString(5, citer.makeSourceUrl());
				
				stmt.setString(6, "");
				stmt.setString(7, citer.makeCitedByUrl());
				stmt.executeUpdate();
				
				stmt3.setString(1, citer.title);
				stmt3.setInt(2, citer.year);
				ResultSet rs3 = stmt3.executeQuery();
				
				while(rs3.next()) {
					citer_ids[i] = rs3.getInt("paper_id");
				}
				i++;
			}
			
			
			
			String sql6 = "INSERT INTO pog.cited_by (cited, citer) VALUES (?, ?)";
			PreparedStatement stmt6 = conn.prepareStatement(sql6);
			
			for(int citer_id : citer_ids) {
				stmt6.setInt(1, paper_id);
				stmt6.setInt(2, citer_id);
				stmt6.executeUpdate();
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} finally {
			System.out.println("Done");
			//closeconnection(conn);
		}
	}
}
