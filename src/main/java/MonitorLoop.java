import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MonitorLoop {

	public static void main(String[] args) throws Exception {

        List<String> workflowUrls = new ArrayList<String>();
        workflowUrls.add("http://localhost:3000/workflows/good.json");
        workflowUrls.add("http://localhost:3000/workflows/wferror.json");
        workflowUrls.add("http://localhost:3000/workflows/error.json");
        
        while(true) {
        	
        	for(String workflowUrl: workflowUrls) {
        		System.out.println("Checking workflow at URL : " + workflowUrl);
        		System.out.println(httpGet("http://localhost:8888/monitorServices/checkWorkflow?url="+workflowUrl));
        		System.out.println();
        	}
        	
        	
        	Thread.sleep(5000);
        }
		
		
	}

	public static String httpGet(String url) throws Exception {
		
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
