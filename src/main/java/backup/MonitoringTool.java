package backup;

import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MonitoringTool {

	private static final String QUERY_FOR_JSON_PARAM = "?f=json";
	
	private static final String ARCGIS_REST_SERVICES_URL = "http://pgtsvm4:6080/arcgis/rest/services/epermit/MapServer";
	private static final String GEOCORTEX_WORKFLOWS_URL = "http://orca1.pacificgeotech.local/Geocortex/Essentials/REST/sites/epermit/workflows";

	private static final String GEOCORTEX_DISPLAY_DEV_URL = "http://orca1.pacificgeotech.local/Geocortex/Essentials/REST/sites/epermitDisplayDev";
	private static final String PRETTY_BASE_LAYER = "http://maps.gov.bc.ca/arcserver/rest/services/Province/web_mercator_cache/MapServer";
	private static final String fail_arcgis_url = "http://maps.gov.bc.ca/arcgis/rest/services/Province/web_mercator_cache/MapServer/tile/6/0/0";
	///http://pgtsvm4:6080/arcgis/rest/services/epermit/MapServer/23/query?where=1%3D1&f=pjson
	private static final String CLIENT_BASE_LAYER = "http://maps.gov.bc.ca/arcserver/rest/services/mpcm/bcgw/MapServer";
	
	private static final String EPAY_URL = "http://cheetah-vm2:30180/epay/module/core/module/login/ui/jsf/login.jsf";
	private static final String EPERMIT_DEV_URL = "http://cheetah-vm1:30080/epermit/module/core/module/dashboard/ui/jsf/dashboard.jsf";
	private static final String EPERMIT_TEST_URL = "http://cheetah-vm1:31080/epermit/module/core/module/dashboard/ui/jsf/dashboard.jsf";
	
	//http://restdemos2.geocortex.com/Geocortex/Essentials/Sandbox/Rest/sites/Total/workflows/wells/run?f=pjson		//error
	//http://restdemos2.geocortex.com/Geocortex/Essentials/Sandbox/REST/sites/AZDOT/workflows/0/run#				//good

	
	public static void main(String[] args) throws Exception {

		pingDatabase("dev");
		pingDatabase("test");
		
		pingApplicationURL(ARCGIS_REST_SERVICES_URL + QUERY_FOR_JSON_PARAM);
		pingApplicationURL(GEOCORTEX_WORKFLOWS_URL + QUERY_FOR_JSON_PARAM);
		
		pingApplicationURL(EPAY_URL);
		pingApplicationURL(EPERMIT_DEV_URL);
		pingApplicationURL(EPERMIT_TEST_URL);
		
		pingApplicationURL(GEOCORTEX_DISPLAY_DEV_URL);
		pingApplicationURL(PRETTY_BASE_LAYER);
		pingApplicationURL(CLIENT_BASE_LAYER);
	}
	
	public static void pingDatabase(String env) throws Exception {
		
		System.out.println("Pinging '" + env + "' database and querying for all tables:");
		
		Class.forName("oracle.jdbc.driver.OracleDriver");
		
		Connection con = DriverManager.getConnection ("jdbc:oracle:thin:@WILDDOG:1521:" + env, "EPM", "EPM");
    	Statement queryer = con.createStatement();
        ResultSet rs =  queryer.executeQuery( "SELECT count(*) "
        									+ "FROM all_tables "
        									+ "WHERE TABLE_NAME LIKE 'EPM%'" );
        
        try {
			while (rs.next()) {
				System.out.println(rs.getString(1) + " tables found.");
			}
		} catch (SQLException e) {
			System.out.println("Database is down");
		}
		
        System.out.println();
		con.close();
	}
	
	public static void pingApplicationURL(String applicationURL) throws Exception {

		
		int httpStatusCode = getHttpStatusCode(applicationURL);
		
		if(httpStatusCode==200) {
		} else {
			System.out.println( applicationURL + " is down");
		}
	}
	
	private static int getHttpStatusCode(String url) throws Exception {
		
		URL obj = new URL(url);
		HttpURLConnection con;
		int responseCode = 0;
		
		try {
			con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
			responseCode = con.getResponseCode();
		} catch (Exception e) {
			responseCode = 404;
		}
		
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);
		return responseCode;
	}
}
