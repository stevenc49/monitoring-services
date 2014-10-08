package backup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;

import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * @author Steve
 *
 * This API server provides services to check:
 * - if a URL is returning a HTTP 200
 * - if a Geocortex workflow is returning an error or not
 * - if a Windows Service is running or not
 * - if a database instance is up or not
 * 
 * This class wraps all the different services above and returns 'OK' or 'NOK'.
 * It will also emit a socket.io to the Angular Frontend (later)
 * 
 */
public class MonitorServices {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8888), 0);
        server.createContext("/monitorServices", new homePage());
        server.createContext("/monitorServices/pingUrl", new pingUrl());
        server.createContext("/monitorServices/checkWorkflow", new checkWorkflow());
        server.createContext("/monitorServices/checkWindowsService", new checkWindowsService());
        server.start();
        
        
        System.out.println("start loop");
        while(true) {
        	
        }
        
    }

    /**
     * @author scheong
     *
     * http://localhost:8888/monitorServices/checkWindowsService?server=cheetah-vm1&service=epermitFrontendDev
     */
    static class checkWindowsService implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
        	
        	String myResponse = "NOK";
        	String url = t.getRequestURI().toString();
        	
        	int indexOfBeginningOfServer = url.indexOf("server=");
        	int indexOfEndOfServer = url.indexOf("&");
        	int indexOfBeginningOfService = url.indexOf("service=");
        	String server = url.substring(indexOfBeginningOfServer + 7, indexOfEndOfServer);
        	String service = url.substring(indexOfBeginningOfService + 8);
        	
        	String[] command = {"sc", "\\\\"+server, "query", service};
        	
    		try {
				ProcessBuilder pb = new ProcessBuilder(command);
				pb.redirectErrorStream(true);
				Process process = pb.start();
				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line;
				while ((line = reader.readLine()) != null)
					if(line.contains("STATE")) {
						line = line.trim();
						String status = line.substring(line.lastIndexOf(" ")+1);
						
						if(status.equals("RUNNING")) {
							myResponse = "OK";
						}
					}
				process.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        	
        	
    		t.sendResponseHeaders(200, myResponse.length());
            OutputStream os = t.getResponseBody();
            os.write(myResponse.getBytes());
            os.close();
        }
    }
    
    /**
     * @author Steve
     *
     * This web service checks the Geocortex workflow provided by the 'url' parameter and returns 'OK'/'NOK' based on whether an error is found.
     * Note: the url is expected to respond with a json object.
     * 
     * Examples:
     * http://localhost:8000/checkWorkflow?url=http://localhost:3000/goodRequest.json
     * http://localhost:8000/checkWorkflow?url=http://localhost:3000/badRequest.json
     * http://localhost:8000/checkWorkflow?url=http://restdemos2.geocortex.com/Geocortex/Essentials/Sandbox/REST/sites/AZDOT/workflows/0/run?f=pjson
     * http://localhost:8000/checkWorkflow?url=http://restdemos2.geocortex.com/Geocortex/Essentials/Sandbox/Rest/sites/Total/workflows/wells/run?f=pjson
     * 
     */
    static class checkWorkflow implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
        	
        	String myResponse = "";
        	String url = t.getRequestURI().toString();
        	int indexOfBeginningOfUrl = url.indexOf("url=");
        	url = url.substring(indexOfBeginningOfUrl + 4);
        	
        	System.out.println("\nChecking workflow at URL : " + url);
        	
    		try {
    			
    			//check http 200
    			if(checkHttp200(url).equals("OK")) {
    				
    				//if 200, then see if json response has error
        			String urlResponse = httpGet(url);
    				
    				JSONObject jsonResponse = new JSONObject(urlResponse.toString());
    				if(jsonResponse.has("error")) {
    					myResponse = "NOK";
    				} else {
    					myResponse = "OK";
    				}
    			} else {
    				myResponse = "NOK";
    			}
    			

			} catch (Exception e) {
				myResponse = e.getStackTrace().toString();
	            t.sendResponseHeaders(200, myResponse.length());
			}
    		
    		System.out.println("Response : " + myResponse);
    		
    		t.sendResponseHeaders(200, myResponse.length());
            OutputStream os = t.getResponseBody();
            os.write(myResponse.getBytes());
            os.close();
        }
    }
    
    /**
     * @author Steve
     *
     * This web service checks if 'url' responds with a HTTP 200.
     * 
     * Example:
     * http://localhost:8000/pingUrl?url=http://www.google.ca		returns 'OK'
     * http://localhost:8000/pingUrl?url=http://www.google.caasdf	returns 'NOK'
     *
     */
    static class pingUrl implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
        	
        	String response = "";
        	String url = t.getRequestURI().toString();
        	int indexOfBeginningOfUrl = url.indexOf("url=");
        	url = url.substring(indexOfBeginningOfUrl + 4);
        	
    		try {
    			response = checkHttp200(url);
    			
    			System.out.println("\nSending 'GET' request to URL : " + url);
    			System.out.println("Response : " + response);
    			
			} catch (Exception e) {
	            t.sendResponseHeaders(200, response.length());
			}
    		
    		t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
    
    static class homePage implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
        	
        	String response = "usage: ";
    		
    		t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
    
    ///////////////////////////////////////
    //			helper methods
    ///////////////////////////////////////
    
	private static String checkHttp200(String url) throws Exception {
		String response;
		int httpStatusCode = getHttpStatusCode(url);
		
		if(httpStatusCode==200) {
		    response = "OK";
		} else {
			response = "NOK";
		}
		return response;
	}
    
	private static int getHttpStatusCode(String url) throws Exception {
		
		URL obj = new URL(url);
		HttpURLConnection con;
		int responseCode = 0;
		
		try {
			con = (HttpURLConnection) obj.openConnection();
			responseCode = con.getResponseCode();
		} catch (Exception e) {
			responseCode = 404;
		}
		
		return responseCode;
	}
	
	private static String httpGet(String url) throws Exception {
		
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", "Mozilla/5.0");

		int responseCode = con.getResponseCode();
		if(responseCode == 200) {
			
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			return response.toString();
		}
		else {
			return null;
		}
	}
    
}