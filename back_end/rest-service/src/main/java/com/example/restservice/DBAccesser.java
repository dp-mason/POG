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
				//url = "jdbc:mysql://localhost:3308/pog";
				url = "jdbc:mysql://localhost:3306/pog";
			}
			else{
				//url = "jdbc:mysql://localhost:3308/pogdb";
				url = "jdbc:mysql://localhost:3306/pogdb?autoReconnect=true&useSSL=false";
			}
		    //String user      = "root";
		    String user = "demotest";
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
			String sql = "SELECT * FROM (SELECT * FROM (SELECT paper_id as pid, title, year, doc_url, source_url, summary, cited_by_url, count FROM (SELECT cited_by.citer FROM pogdb.cited_by WHERE cited_by.cited = ?) as citers join pogdb.papers on citers.citer = papers.paper_id) as paperdata join pogdb.authored_by on authored_by.paper_id = paperdata.pid) as authid join pogdb.authors on authid.author_id = authors.author_id Order by paper_id;";
			PreparedStatement stmt = this.conn.prepareStatement(sql);
			stmt.setString(1, pid);

			ResultSet rs = stmt.executeQuery();

			String prevId = "";
			String currId = "";
			int i = 0;
			int citedCount = 0;
			while(rs.next()) {
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
					citedCount = (rs.getInt("count"));

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

	public void insertNewEntry2(GSData gsd, String pid) {
		int paper_id = -1;
		if (this.conn == null){
			System.out.println("...");
			return;
		}
		//Insert new paper w/ authors
		try {
			//Check if paper already in database
			String indbsql = "SELECT year FROM pogdb.papers WHERE papers.paper_id = ?";
			PreparedStatement stmt0 = this.conn.prepareStatement(indbsql);
			stmt0.setString(1, gsd.scholar_id);

			ResultSet rs = stmt0.executeQuery();

			//Add row to cited_by table to keep citation relationship
			String sql6 = "INSERT INTO pogdb.cited_by (cited, citer) VALUES (?, ?)";
			PreparedStatement stmt6 = conn.prepareStatement(sql6);

			//See if citation relationship between this paper and cited paper is already in table
			String sqlcitepair = "SELECT count(*) as pairs FROM pogdb.cited_by WHERE cited_by.cited = ? and cited_by.citer = ?";
			PreparedStatement citepairstmt = conn.prepareStatement(sqlcitepair);
			citepairstmt.setString(1, pid);
			citepairstmt.setString(2, gsd.scholar_id);
			ResultSet rscite = citepairstmt.executeQuery();
			Boolean pairInTable = false;

			if(rscite.next()){
				if(rscite.getInt("pairs") > 0){
					pairInTable = true;
				}
			}

			//If paper already is in the db, no need to add it again to the papers table
			if(rs.next()) {
				//if parent is not valid
				if (pid == "none"){
						return;
				}
				stmt0.setString(1, pid);

				rs = stmt0.executeQuery();
				//If parent is also in the db and the parent and citer do not have a row in cited_by, add it
				if(rs.next() && !pairInTable) {
					stmt6.setString(1, pid);
					stmt6.setString(2, gsd.scholar_id);
					stmt6.executeUpdate();
				}
				return;
			}
			//Add row to paper table
			String sql = "INSERT INTO pogdb.papers (paper_id, title, year, doc_url, source_url, summary, cited_by_url, count) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
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

			stmt.setInt(8, gsd.cited_by_count);
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

			//TODO: change to author url
			//String sql4 = "SELECT author_id FROM pogdb.authors WHERE author_fname = ? AND author_lname = ?";
			String sql4 = "SELECT author_id FROM pogdb.authors WHERE author_url = ?";
			PreparedStatement stmt4 = conn.prepareStatement(sql4);

			String sql5 = "INSERT INTO pogdb.authored_by (paper_id, author_id) VALUES (?, ?)";
			PreparedStatement stmt5 = conn.prepareStatement(sql5);

			String fname = "";
			String lname = "";
			int author_id = -1;
			//int count = 0;
			//for(String author : gsd.authors) {
			//for(String authorUrl : gsd.author_urls) {
			for(int a = 0; a < gsd.authors.size(); a++){
				fname = gsd.authors.get(a).substring(0, gsd.authors.get(a).indexOf(" "));
				lname = gsd.authors.get(a).substring(gsd.authors.get(a).indexOf(" ")+1);

				if(fname.length() > 50){
					fname = fname.substring(0,50);
				}
				if(lname.length() > 50){
					lname = lname.substring(0,50);
				}
				//TODO: Make sure not adding duplicate authors (use author fname and lname or author url)

				//stmt4.setString(1, fname);
				//stmt4.setString(2, lname);
				String authorUrl = gsd.author_urls.get(a);
				if(authorUrl.length() > 105){
					authorUrl = authorUrl.substring(0,105);
				}
				stmt4.setString(1, authorUrl);
				ResultSet rs2 = stmt4.executeQuery();
				Boolean inTable = false;
				while(rs2.next()) {
					author_id = rs2.getInt("author_id");
					inTable = true;
				}
				if(!inTable) {
					//String aurl = gsd.author_urls.get(count);
					//if (aurl.length() > 105) {
					//	aurl = aurl.substring(0, 105);
					//}
					stmt2.setString(1, authorUrl);
					stmt2.setString(2, fname);
					stmt2.setString(3, lname);
					stmt2.executeUpdate();

					//stmt4.setString(1, fname);
					//stmt4.setString(2, lname);
					stmt4.setString(1, authorUrl);
					rs2 = stmt4.executeQuery();
					while (rs2.next()) {
						author_id = rs2.getInt("author_id");
					}
				}

				stmt5.setString(1, gsd.scholar_id);
				stmt5.setInt(2, author_id);
				stmt5.executeUpdate();
				//count++;
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



			for(String citer_id : citer_ids) {
				stmt6.setString(1, gsd.scholar_id);
				stmt6.setString(2, citer_id);
				stmt6.executeUpdate();
			}

			//String sql7 = "SELECT pogdb.cited_by (cited, citer) VALUES (?, ?)";
			//PreparedStatement stmt7 = conn.prepareStatement(sql6);

			//String sql8 = "INSERT INTO pogdb.cited_by (cited, citer) VALUES (?, ?)";
			//PreparedStatement stmt8 = conn.prepareStatement(sql8);

			//If we have cited paper id, add row to the cited_by table
			if(pid != "none") {
				stmt0.setString(1, pid);

				rs = stmt0.executeQuery();
				if (rs.next()) {
					stmt6.setString(1, pid);
					stmt6.setString(2, gsd.scholar_id);
					stmt6.executeUpdate();
				}
			}

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} finally {
			System.out.println("Done");
			//closeconnection(conn);
		}
	}
}
