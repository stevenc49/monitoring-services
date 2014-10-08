import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;

public class MonitorLoop {

	public static void main(String[] args) throws Exception {

		Logger log = Logger.getLogger(MonitorLoop.class);
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
		
        List<String> workflowUrls = new ArrayList<String>();
        
        //epermit endpoints
        BufferedReader br = new BufferedReader(new FileReader("endpoints.txt"));
        String line;
        while ((line = br.readLine()) != null) {
        	workflowUrls.add(line);
        }
        br.close();

        //start loop
        while(true) {
        	
        	log.info("================================================================================");
        	
        	for(String workflowUrl: workflowUrls) {
        		
        		DateTime start = new DateTime();
        		log.info("Checking workflow at URL : " + workflowUrl);
        		log.info("Response: " + httpGet("http://localhost:8888/monitorServices/checkWorkflow?url="+workflowUrl));
        		
        		DateTime end = new DateTime();
        		Interval interval = new Interval(start, end);
        		Period period = interval.toPeriod();
        		log.info("Duration: " + PeriodFormat.getDefault().print(period));
        		
        		log.info("");
        	}
        	
        	log.info("================================================================================");
        	
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
