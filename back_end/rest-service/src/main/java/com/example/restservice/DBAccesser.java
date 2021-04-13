package com.example.restservice;
import com.sun.org.apache.xpath.internal.operations.Bool;

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
		getConnection(true);
	}


	public void getConnection(boolean pogdb) {
		Connection conn = null;
		String url= "nothing";
		try {

		    String driver ="com.mysql.cj.jdbc.Driver";

			if(!pogdb) {
				url = "jdbc:mysql://localhost:3308/pog";
			}
			else{
				url = "jdbc:mysql://localhost:3308/pogdb";
			}
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
			System.out.println("finally connected to " + url);
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

	public ArrayList<GSData> getChildrenById(String pid){
		ArrayList<GSData> children = new ArrayList<GSData>();
		try{
			//ArrayList<String> childrenIds = new ArrayList<String>();
			//String sql = "SELECT cited_by.citer FROM pogdb.cited_by WHERE cited_by.cited = ?";
			String sql = "SELECT * FROM (SELECT * FROM (SELECT paper_id as pid, title, year, doc_url, source_url, summary, cited_by_url FROM (SELECT cited_by.citer FROM pogdb.cited_by WHERE cited_by.cited = ?) as citers join pogdb.papers on citers.citer = papers.paper_id) as paperdata join pogdb.authored_by on authored_by.paper_id = paperdata.pid) as authid join pogdb.authors on authid.author_id = authors.author_id Order by paper_id;";
			PreparedStatement stmt = this.conn.prepareStatement(sql);
			stmt.setString(1, pid);

			ResultSet rs = stmt.executeQuery();

			//String sql2 = "Select count(*) as count FROM (SELECT cited_by.citer FROM pogdb.cited_by WHERE cited_by.cited = ?) as citers join pogdb.cited_by on citers.citer = cited_by.cited GROUP BY cited;";
			String sql2 = "SELECT count(*) as count FROM pogdb.cited_by WHERE cited_by.cited = ? GROUP BY cited_by.cited;";
			PreparedStatement stmt2 = this.conn.prepareStatement(sql2);
			//stmt2.setString(1, pid);
			//ResultSet rs2 = stmt2.executeQuery();
			//ArrayList<Integer> citedCounts = new ArrayList<Integer>();
			//while(rs2.next()) {
			//	citedCounts.add(rs2.getInt("count"));
			//}

			String prevId = "";
			String currId = "";
			int i = 0;
			int citedCount = 0;
			while(rs.next()) {

				//childrenIds.add(rs.getString("citer"));
				currId = rs.getString("paper_id");
				//if (prevId != currId){
				if(!prevId.equals(currId)){
					prevId = currId;
					i++;
					GSData childGSD = new GSData(rs.getString("title"), rs.getInt("year"));
					childGSD.scholar_id = currId;
					childGSD.doc_url = rs.getString("doc_url");
					childGSD.authors = new ArrayList<String>();
					childGSD.authors.add(rs.getString("author_fname") + " " + rs.getString("author_lname"));
					childGSD.author_urls = new ArrayList<String>();
					childGSD.author_urls.add(rs.getString("author_url"));
					childGSD.source_url = rs.getString("source_url");
					childGSD.summary = rs.getString("summary");
					childGSD.cited_by_url = rs.getString("cited_by_url");
					childGSD.parentId = pid;
					stmt2.setString(1, currId);
					ResultSet rs2 = stmt2.executeQuery();

					if(rs2.next()) {
						citedCount = (rs2.getInt("count"));
					}
					else{
						citedCount = 0;
					}
					childGSD.cited_by_count = citedCount;


					children.add(childGSD);
				}
				else{
					//Add authors and urls
					children.get(i-1).authors.add(rs.getString("author_fname") + " " + rs.getString("author_lname"));
					children.get(i-1).author_urls.add(rs.getString("author_url"));
				}
			}
		}
		catch (SQLException e){
			e.printStackTrace();
		}
		catch (Exception e){
			e.printStackTrace();
		}
		finally {
			return children;
		}
	}

	public int getPaperId(String title){
		int id = -1;

		try {
			String regex = "%" + title.replaceAll(" ", "%") + "%";
			String sql = "SELECT * FROM pog.papers where papers.title like ?";
			PreparedStatement stmt = this.conn.prepareStatement(sql);
			stmt.setString(1, regex);

			ResultSet rs = stmt.executeQuery();

			while(rs.next()) {
				id = rs.getInt("paper_id");
			}

		}
		catch (SQLException e){
			e.printStackTrace();
		}
		finally {
			return id;
		}
	}

	public int getPaperId(String title, int year){
		int id = -1;

		try {
			String regex = "%" + title.replaceAll(" ", "%") + "%";
			String sql = "SELECT * FROM pog.papers where papers.title like ? and year = ?";
			PreparedStatement stmt = this.conn.prepareStatement(sql);
			stmt.setString(1, regex);
			stmt.setInt(2, year);
			ResultSet rs = stmt.executeQuery();

			while(rs.next()) {
				id = rs.getInt("paper_id");
			}

		}
		catch (SQLException e){
			e.printStackTrace();
		}
		finally {
			return id;
		}
	}

	public int getPaperRow(int idNumber, GSData gsd){
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
		int count = 0;
		try {
			String sql = "SELECT * FROM pog.papers where papers.paper_id = ?";
			PreparedStatement stmt = this.conn.prepareStatement(sql);
			stmt.setInt(1, idNumber);
			ResultSet rs = stmt.executeQuery();


			while(rs.next()) {
				count++;
				id = rs.getInt("paper_id");
				title = rs.getString("title");
				year = rs.getInt("year");
				doc_url = rs.getString("doc_url");
				source_url = rs.getString("source_url");
				summary = rs.getString("summary");
				cited_by_url = rs.getString("cited_by_url");
			}
			//gsd = new GSData(title, year);
			gsd.title = title;
			gsd.year = year;
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
			return count;
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

	public void insertNewEntry(GSData gsd, int pid) {
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
			String doc = gsd.doc_url;
			if(doc.length() > 105){
				doc = doc.substring(0, 105);
			}
			stmt.setString(4, doc);
			String source = gsd.source_url;
			if(source.length() > 105){
				source = source.substring(0, 105);
			}
			stmt.setString(5, source);
			String summary = gsd.summary;
			if(summary.length() > 350) {
				summary = gsd.summary.substring(0, 350);
			}
			stmt.setString(6, summary);
			//stmt.setString(7, gsd.makeCitedByUrl());
			String cited_by_url = gsd.cited_by_url;
			if(gsd.cited_by_url.length() > 105){
				cited_by_url = gsd.cited_by_url.substring(0,105);
			}
			stmt.setString(7, cited_by_url);
			stmt.executeUpdate();

			//Will get rid of when using google scholar id
			String sql3 = "SELECT paper_id FROM pog.papers WHERE title = ? AND year = ?";
			PreparedStatement stmt3 = this.conn.prepareStatement(sql3);
			stmt3.setString(1, title);
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
			int count = 0;
			for(String author : gsd.authors) {
				fname = author.substring(0, author.indexOf(" "));
				lname = author.substring(author.indexOf(" ")+1);
				String aurl = gsd.author_urls.get(count);
				if(aurl.length() > 105){
					aurl = aurl.substring(0, 105);
				}
				stmt2.setString(1, aurl);
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
				count++;
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
				String cdoc = citer.doc_url;
				if(cdoc.length() > 105){
					cdoc = cdoc.substring(0, 105);
				}
				stmt.setString(4, cdoc);
				String csource = citer.source_url;
				if(csource.length() > 105){
					csource = csource.substring(0, 105);
				}
				stmt.setString(4, csource);
				String csummary = citer.summary;
				if(csummary.length() > 350) {
					csummary = citer.summary.substring(0, 350);
				}
				stmt.setString(6, summary);
				String ccited = citer.cited_by_url;
				if(ccited.length() > 105){
					ccited = citer.cited_by_url.substring(0,105);
				}
				stmt.setString(7, ccited);
				stmt.executeUpdate();
				
				stmt3.setString(1, title);
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

			//String sql7 = "SELECT pog.cited_by (cited, citer) VALUES (?, ?)";
			//PreparedStatement stmt7 = conn.prepareStatement(sql6);

			//String sql8 = "INSERT INTO pog.cited_by (cited, citer) VALUES (?, ?)";
			//PreparedStatement stmt8 = conn.prepareStatement(sql8);
			stmt6.setInt(1, pid);
			stmt6.setInt(2, paper_id);
			stmt6.executeUpdate();

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} finally {
			System.out.println("Done");
			//closeconnection(conn);
		}
	}

	public void insertNewEntry2(GSData gsd, String pid) {
		int paper_id = -1;
		//Insert new paper w/ authors
		try {
			String indbsql = "SELECT year FROM pogdb.papers WHERE papers.paper_id = ?";
			PreparedStatement stmt0 = this.conn.prepareStatement(indbsql);
			stmt0.setString(1, gsd.scholar_id);

			ResultSet rs = stmt0.executeQuery();
			if(rs.next()) {
				return;
			}

			String sql = "INSERT INTO pogdb.papers (paper_id, title, year, doc_url, source_url, summary, cited_by_url) VALUES (?, ?, ?, ?, ?, ?, ?);";
			PreparedStatement stmt = this.conn.prepareStatement(sql);


			//Add Query Url to insert statement
			//String url = gsd.makeQueryUrl();

			stmt.setString(1, gsd.scholar_id);

			//Add title to insert statement
			String title = gsd.title;
			if(title.length() > 256) {
				title = title.substring(0, 256);
			}
			stmt.setString(2, title);

			//Add year to insert statement
			stmt.setInt(3, gsd.year);
			//Add document url to insert statement
			String doc = gsd.doc_url;
			if(doc.length() > 105){
				doc = doc.substring(0, 105);
			}
			stmt.setString(4, doc);
			String source = gsd.source_url;
			if(source.length() > 105){
				source = source.substring(0, 105);
			}
			stmt.setString(5, source);
			String summary = gsd.summary;
			if(summary.length() > 350) {
				summary = gsd.summary.substring(0, 350);
			}
			stmt.setString(6, summary);
			//stmt.setString(7, gsd.makeCitedByUrl());
			String cited_by_url = gsd.cited_by_url;
			if(gsd.cited_by_url.length() > 105){
				cited_by_url = gsd.cited_by_url.substring(0,105);
			}
			stmt.setString(7, cited_by_url);
			stmt.executeUpdate();

			//Will get rid of when using google scholar id
			//String sql3 = "SELECT paper_id FROM pogdb.papers WHERE title = ? AND year = ?";
			//PreparedStatement stmt3 = this.conn.prepareStatement(sql3);
			//stmt3.setString(1, title);
			//stmt3.setInt(2, gsd.year);
			//rs = stmt3.executeQuery();

			//while(rs.next()) {
			//	paper_id = rs.getInt("paper_id");
			//}

			String sql2 = "INSERT INTO pogdb.authors (author_url, author_fname, author_lname) VALUES (?, ?, ?)";
			PreparedStatement stmt2 = conn.prepareStatement(sql2);

			String sql4 = "SELECT author_id FROM pogdb.authors WHERE author_fname = ? AND author_lname = ?";
			PreparedStatement stmt4 = conn.prepareStatement(sql4);

			String sql5 = "INSERT INTO pogdb.authored_by (paper_id, author_id) VALUES (?, ?)";
			PreparedStatement stmt5 = conn.prepareStatement(sql5);

			String fname = "";
			String lname = "";
			int author_id = -1;
			int count = 0;
			for(String author : gsd.authors) {
				fname = author.substring(0, author.indexOf(" "));
				lname = author.substring(author.indexOf(" ")+1);

				stmt4.setString(1, fname);
				stmt4.setString(2, lname);
				ResultSet rs2 = stmt4.executeQuery();
				Boolean inTable = false;
				while(rs2.next()) {
					author_id = rs2.getInt("author_id");
					inTable = true;
				}
				if(!inTable) {
					String aurl = gsd.author_urls.get(count);
					if (aurl.length() > 105) {
						aurl = aurl.substring(0, 105);
					}
					stmt2.setString(1, aurl);
					stmt2.setString(2, fname);
					stmt2.setString(3, lname);
					stmt2.executeUpdate();

					stmt4.setString(1, fname);
					stmt4.setString(2, lname);
					rs2 = stmt4.executeQuery();
					while (rs2.next()) {
						author_id = rs2.getInt("author_id");
					}
				}
				stmt5.setString(1, gsd.scholar_id);
				stmt5.setInt(2, author_id);
				stmt5.executeUpdate();
				count++;
			}



			String[] citer_ids = new String[gsd.citers.length];
			int i = 0;
			for(GSData citer : gsd.citers) {
				String cscholar_id = citer.scholar_id;
				stmt.setString(1, cscholar_id);
				title = citer.title;
				if(title.length() > 256) {
					title = title.substring(0, 256);
				}
				stmt.setString(2, title);
				stmt.setInt(3, citer.year);
				String cdoc = citer.doc_url;
				if(cdoc.length() > 105){
					cdoc = cdoc.substring(0, 105);
				}
				stmt.setString(4, cdoc);
				String csource = citer.source_url;
				if(csource.length() > 105){
					csource = csource.substring(0, 105);
				}
				stmt.setString(4, csource);
				String csummary = citer.summary;
				if(csummary.length() > 350) {
					csummary = citer.summary.substring(0, 350);
				}
				stmt.setString(6, summary);
				String ccited = citer.cited_by_url;
				if(ccited.length() > 105){
					ccited = citer.cited_by_url.substring(0,105);
				}
				stmt.setString(7, ccited);
				stmt.executeUpdate();

				//stmt3.setString(1, title);
				//stmt3.setInt(2, citer.year);
				//ResultSet rs3 = stmt3.executeQuery();

				//while(rs3.next()) {
				//	citer_ids[i] = rs3.getInt("paper_id");
				//}
				citer_ids[i] = citer.scholar_id;
				i++;
			}

			String sql6 = "INSERT INTO pogdb.cited_by (cited, citer) VALUES (?, ?)";
			PreparedStatement stmt6 = conn.prepareStatement(sql6);

			for(String citer_id : citer_ids) {
				stmt6.setString(1, gsd.scholar_id);
				stmt6.setString(2, citer_id);
				stmt6.executeUpdate();
			}

			//String sql7 = "SELECT pogdb.cited_by (cited, citer) VALUES (?, ?)";
			//PreparedStatement stmt7 = conn.prepareStatement(sql6);

			//String sql8 = "INSERT INTO pogdb.cited_by (cited, citer) VALUES (?, ?)";
			//PreparedStatement stmt8 = conn.prepareStatement(sql8);
			stmt0.setString(1, pid);

			rs = stmt0.executeQuery();
			if(rs.next()) {
				stmt6.setString(1, pid);
				stmt6.setString(2, gsd.scholar_id);
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
