package com.ssamples.cassandra;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;

public class DemoSelect {

	public static void main(String args[]) {

		String serverIP = "127.0.0.1";
		String keyspace = "killrvideo";

		Cluster cluster = Cluster.builder().addContactPoints(serverIP).build();

		Session session = cluster.connect(keyspace);
		String cqlStatement = "SELECT * FROM users";
		for (Row row : session.execute(cqlStatement)) {
			System.out.println(row.toString());
		}
        	System.out.println("Completed reading table");
		com.datastax.driver.core.PreparedStatement pStmt = session.prepare("INSERT INTO USERS (STATE, ID, NAME, STATE_NAME) "
				+ "VALUES (?,?,?,?);");
		BoundStatement boundStatement = new BoundStatement(pStmt);
		session.execute(boundStatement.bind("MA",3,"Harvard","Massachusetts"));
                pStmt = session.prepare("SELECT * FROM USERS WHERE STATE = ?;");
                boundStatement = new BoundStatement(pStmt);
                ResultSet rs = session.execute(boundStatement.bind("MA"));
                for (Row r : rs)
                    System.out.println(r.getString("NAME"));
                session.close();
                cluster.close();
	}

}
